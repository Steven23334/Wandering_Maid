package com.github.steven23334.tlm_wandering_maid.compat.wandering;

import com.github.steven23334.tlm_wandering_maid.compat.state.MaidMovementControl;
import com.github.steven23334.tlm_wandering_maid.config.WanderingMaidConfig;
import com.github.steven23334.tlm_wandering_maid.mixin.accessor.EntityMaidTameInvoker;
import com.github.steven23334.tlm_wandering_maid.network.OpenWanderingMaidRequestS2CPacket;
import com.github.tartaricacid.touhoulittlemaid.entity.info.ServerCustomPackLoader;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public final class WanderingMaidManager {
    public static final String DEFAULT_MODEL = "geckolib:winefox";
    private static final int SEARCH_MIN_RADIUS = 24;
    private static final int SEARCH_MAX_RADIUS = 48;
    private static final int MAX_ARRIVALS = 3;
    private static final double ARRIVE_DISTANCE = 1.5;
    private static final double RETRY_DISTANCE = 5.0;
    private static final double LEAVE_DISTANCE = 16.0;
    private static final long NO_PROGRESS_TIMEOUT = 20L * 20L;
    private static final long APPROACH_TIMEOUT = 20L * 90L;
    private static final int SPAWN_GLOW_DURATION = 20 * 60;

    /** ★ 优先在玩家面朝方向 ±60° 扇区内尝试次数；失败后回退全方向。 */
    private static final int FORWARD_ATTEMPTS = 18;
    private static final int ANYWHERE_ATTEMPTS = 24;
    private static final double FORWARD_HALF_ARC_DEG = 60.0;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWanderingMaidTick(EntityTickEvent.Pre event) {
        if (event.getEntity() instanceof EntityMaid maid && !maid.level().isClientSide
                && WanderingMaidData.isSpecial(maid) && !WanderingMaidData.mayAccept(maid)) {
            enforceWildState(maid);
        }
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        ServerLevel level = server.overworld();
        long gameTime = level.getGameTime();
        WanderingMaidSavedData data = WanderingMaidSavedData.get(level);

        // ★ 已存在的流浪女仆始终跑状态机，不受总开关影响
        tickAllWanderingMaids(level, data, gameTime);

        // ★ 总开关关闭时，只跳过自动生成
        if (!WanderingMaidConfig.WANDERING_MAID_ENABLED.get()) {
            return;
        }

        long interval = configuredIntervalTicks();
        if (interval <= 0) {
            return;
        }
        if (!level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
            return;
        }

        List<ServerPlayer> candidates = level.players().stream()
                .filter(player -> !player.isSpectator() && player.isAlive())
                .toList();

        // ★ 每个玩家各走各的计时器，互不干扰
        for (ServerPlayer player : candidates) {
            UUID id = player.getUUID();
            long next = data.nextAttemptTick(id);

            if (next <= 0 || next > gameTime + interval) {
                data.setNextAttemptTick(id, gameTime + interval);
                continue;
            }
            if (gameTime < next) {
                continue;
            }
            data.setNextAttemptTick(id, gameTime + interval);

            if (level.getRandom().nextInt(100) >= WanderingMaidConfig.WANDERING_MAID_SPAWN_CHANCE.get()) {
                continue;
            }
            spawnEventForPlayer(player, false);
        }
    }

    private static long configuredIntervalTicks() {
        return WanderingMaidConfig.WANDERING_MAID_INTERVAL_MINUTES.get() * 60L * 20L;
    }

    public static boolean spawnForPlayer(ServerPlayer player, boolean commandTriggered) {
        return spawnEventForPlayer(player, commandTriggered) > 0;
    }

    private static int spawnEventForPlayer(ServerPlayer player, boolean commandTriggered) {
        int alive = countAliveSpecial(player.serverLevel());

        int room;
        if (commandTriggered) {
            room = WanderingMaidConfig.WANDERING_MAID_COUNT.get();
        } else {
            int maxAlive = WanderingMaidConfig.WANDERING_MAID_MAX_ALIVE.get();
            room = maxAlive - alive;
            if (room <= 0) {
                return 0;
            }
        }

        int targets = Math.min(WanderingMaidConfig.WANDERING_MAID_COUNT.get(), room);
        int successes = 0;
        for (int i = 0; i < targets; i++) {
            if (spawnSingleForPlayer(player, commandTriggered && i == 0)) {
                successes++;
            }
        }
        return successes;
    }

    private static int countAliveSpecial(ServerLevel level) {
        int count = 0;
        for (Entity entity : level.getEntities().getAll()) {
            if (entity instanceof EntityMaid maid && maid.isAlive() && WanderingMaidData.isSpecial(maid)) {
                count++;
            }
        }
        return count;
    }

    private static boolean spawnSingleForPlayer(ServerPlayer player, boolean commandTriggered) {
        if (!(player.level() instanceof ServerLevel level) || player.getServer() == null
                || level != player.getServer().overworld()) {
            if (commandTriggered) {
                player.sendSystemMessage(Component.translatable(
                        "message.tlm_wandering_maid.wandering.overworld_only"));
            }
            return false;
        }
        BlockPos spawnPos = findSpawnPosition(level, player);
        if (spawnPos == null) {
            if (commandTriggered) {
                player.sendSystemMessage(Component.translatable(
                        "message.tlm_wandering_maid.wandering.no_spawn_position"));
            }
            return false;
        }
        EntityMaid maid = EntityMaid.TYPE.create(level);
        if (maid == null) {
            return false;
        }
        maid.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5,
                level.getRandom().nextFloat() * 360.0f, 0.0f);
        if (!level.noCollision(maid)) {
            return false;
        }
        maid.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), MobSpawnType.EVENT, null);
        maid.setModelId(selectSharedModel(level, player.getUUID()));
        maid.setPersistenceRequired();
        WanderingMaidData.initialize(maid, player.getUUID(), level.getGameTime());
        maid.addEffect(new MobEffectInstance(MobEffects.GLOWING, SPAWN_GLOW_DURATION, 0, false, false));
        if (!level.addFreshEntity(maid)) {
            return false;
        }
        MaidMovementControl.begin(maid, MaidMovementControl.Reason.WANDERING_WAIT,
                EnumSet.of(MaidMovementControl.Field.PATH, MaidMovementControl.Field.POSE));

        double distance = Math.sqrt(player.distanceToSqr(maid));
        Component message = Component.translatable(
                        commandTriggered
                                ? "message.tlm_wandering_maid.wandering.spawned"
                                : "message.tlm_wandering_maid.wandering.appeared",
                        maid.getName(),
                        String.format("%.1f", distance))
                .withStyle(ChatFormatting.GOLD);   // ★ 整条消息金色
        player.sendSystemMessage(message);
        return true;
    }

    public static String selectSharedModel(ServerLevel level, UUID player) {
        WanderingMaidSavedData data = WanderingMaidSavedData.get(level.getServer().overworld());
        Set<String> available = ServerCustomPackLoader.SERVER_MAID_MODELS.getModelIdSet();
        List<String> pool = data.skinPool(player).stream().filter(available::contains).toList();
        if (!pool.isEmpty()) {
            return pool.get(level.getRandom().nextInt(pool.size()));
        }
        return available.contains(DEFAULT_MODEL) ? DEFAULT_MODEL : available.stream().findFirst().orElse(DEFAULT_MODEL);
    }

    // ============================================================
    // 出生点查找：第一轮「面朝 ±60° 扇区 + 视线无遮挡」，第二轮全方向兜底
    // ============================================================

    private static BlockPos findSpawnPosition(ServerLevel level, ServerPlayer player) {
        Vec3 eye = player.getEyePosition();
        double baseAngle = Math.toRadians(player.getYRot()) + Math.PI / 2.0;

        BlockPos forward = tryFindSpawn(level, player.blockPosition(), eye,
                baseAngle, Math.toRadians(FORWARD_HALF_ARC_DEG), FORWARD_ATTEMPTS);
        if (forward != null) {
            return forward;
        }
        return tryFindSpawn(level, player.blockPosition(), eye,
                0.0, Math.PI, ANYWHERE_ATTEMPTS);
    }

    private static BlockPos tryFindSpawn(ServerLevel level, BlockPos center, Vec3 eye,
                                         double baseAngle, double halfArc, int attempts) {
        for (int attempt = 0; attempt < attempts; attempt++) {
            double angle;
            if (halfArc >= Math.PI) {
                angle = level.getRandom().nextDouble() * Math.PI * 2.0;
            } else {
                double offset = (level.getRandom().nextDouble() * 2.0 - 1.0) * halfArc;
                angle = baseAngle + offset;
            }
            int distance = Mth.nextInt(level.getRandom(), SEARCH_MIN_RADIUS, SEARCH_MAX_RADIUS);
            int x = center.getX() + Mth.floor(Math.cos(angle) * distance);
            int z = center.getZ() + Mth.floor(Math.sin(angle) * distance);

            BlockPos column = new BlockPos(x, center.getY(), z);
            if (!level.isLoaded(column) || !level.getWorldBorder().isWithinBounds(column)) {
                continue;
            }
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos pos = new BlockPos(x, y, z);

            if (!level.getFluidState(pos).isEmpty() || !level.getFluidState(pos.above()).isEmpty()) {
                continue;
            }
            if (!level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) {
                continue;
            }
            if (!level.isEmptyBlock(pos) || !level.isEmptyBlock(pos.above())) {
                continue;
            }
            Vec3 target = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            if (!hasLineOfSight(level, eye, target)) {
                continue;
            }
            return pos;
        }
        return null;
    }

    private static boolean hasLineOfSight(ServerLevel level, Vec3 from, Vec3 to) {
        ClipContext ctx = new ClipContext(from, to,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty());
        BlockHitResult hit = level.clip(ctx);
        return hit.getType() == HitResult.Type.MISS;
    }

    // ============================================================
    // 状态机与其余逻辑
    // ============================================================

    private static void tickAllWanderingMaids(ServerLevel level, WanderingMaidSavedData savedData, long gameTime) {
        // ★ 循环外读一次配置，避免每个实体每 tick 都查 config
        boolean cleanupEnabled = WanderingMaidConfig.WANDERING_MAID_REJECTED_CLEANUP_ENABLED.get();
        long cleanupDelayTicks = WanderingMaidConfig.WANDERING_MAID_REJECTED_CLEANUP_DELAY_SECONDS.get() * 20L;

        for (Entity entity : level.getEntities().getAll()) {
            if (!(entity instanceof EntityMaid maid) || !maid.isAlive() || !WanderingMaidData.isSpecial(maid)) {
                continue;
            }

            // ★ REJECTED：按配置在延迟后清除（关闭开关则保留）
            if (WanderingMaidData.state(maid) == WanderingMaidState.REJECTED) {
                long rejectAt = WanderingMaidData.rejectAt(maid);
                if (rejectAt <= 0) {
                    // 老存档遗留：以「现在」为起点，让它开始倒计时
                    WanderingMaidData.markRejectAt(maid, gameTime);
                } else if (cleanupEnabled && gameTime - rejectAt >= cleanupDelayTicks) {
                    maid.discard();
                }
                continue;
            }

            if (!WanderingMaidData.mayAccept(maid)) {
                enforceWildState(maid);
            }
            tickWanderingMaid(level, maid, savedData, gameTime);
        }
    }

    private static void tickWanderingMaid(ServerLevel level, EntityMaid maid,
                                          WanderingMaidSavedData savedData, long gameTime) {
        WanderingMaidState state = WanderingMaidData.state(maid);
        if (state == WanderingMaidState.REJECTED) {
            return;
        }
        UUID targetId = WanderingMaidData.target(maid).orElse(null);
        ServerPlayer player = targetId == null
                ? null
                : level.getServer().getPlayerList().getPlayer(targetId);
        if (player == null || player.level() != level || !player.isAlive() || player.isSpectator()) {
            expire(maid, savedData);
            return;
        }

        switch (state) {
            case APPROACHING, RETRYING -> tickApproach(maid, player, savedData, gameTime);
            case WAITING -> tickWaiting(maid, player, savedData, gameTime);
            case LEAVING -> tickLeaving(maid, player, savedData, gameTime);
        }
    }

    private static void tickApproach(EntityMaid maid, ServerPlayer player,
                                     WanderingMaidSavedData savedData, long gameTime) {
        if (maid.closerThan(player, ARRIVE_DISTANCE)) {
            stopNavigation(maid);
            maid.setInSittingPose(true);
            maid.setBegging(true);
            WanderingMaidData.incrementArrivals(maid);
            WanderingMaidData.setState(maid, WanderingMaidState.WAITING, gameTime);
            player.displayClientMessage(
                    Component.translatable("message.tlm_wandering_maid.wandering.arrived"), true);
            return;
        }
        if (gameTime - WanderingMaidData.stateSince(maid) > APPROACH_TIMEOUT) {
            expire(maid, savedData);
            return;
        }
        double distanceSqr = maid.distanceToSqr(player);
        if (distanceSqr + 0.25 < WanderingMaidData.closestDistance(maid)) {
            WanderingMaidData.recordProgress(maid, distanceSqr, gameTime);
        } else if (gameTime - WanderingMaidData.lastProgress(maid) > NO_PROGRESS_TIMEOUT) {
            expire(maid, savedData);
            return;
        }
        maid.setInSittingPose(false);
        maid.setBegging(false);
        maid.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        maid.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(player, true));
        maid.getBrain().setMemory(MemoryModuleType.WALK_TARGET,
                new WalkTarget(new EntityTracker(player, false), 0.5f, 2));
        maid.getNavigation().moveTo(player, 0.5d);
    }

    private static void tickWaiting(EntityMaid maid, ServerPlayer player,
                                    WanderingMaidSavedData savedData, long gameTime) {
        stopNavigation(maid);
        maid.setInSittingPose(true);
        maid.setBegging(true);
        maid.getLookControl().setLookAt(player, 30.0f, 30.0f);
        if (maid.closerThan(player, RETRY_DISTANCE)) {
            return;
        }
        maid.setInSittingPose(false);
        maid.setBegging(false);
        if (WanderingMaidData.arrivals(maid) >= MAX_ARRIVALS) {
            rejectPermanently(maid, savedData, gameTime);
        } else {
            WanderingMaidData.setState(maid, WanderingMaidState.RETRYING, gameTime);
        }
    }

    private static void tickLeaving(EntityMaid maid, ServerPlayer player,
                                    WanderingMaidSavedData savedData, long gameTime) {
        maid.setInSittingPose(false);
        maid.setBegging(false);
        if (!maid.closerThan(player, LEAVE_DISTANCE)
                || gameTime - WanderingMaidData.stateSince(maid) > NO_PROGRESS_TIMEOUT) {
            rejectPermanently(maid, savedData, gameTime);
            return;
        }
        Vec3 away = maid.position().subtract(player.position());
        if (away.lengthSqr() < 0.01) {
            away = new Vec3(1, 0, 0);
        }
        Vec3 destination = maid.position().add(away.normalize().scale(LEAVE_DISTANCE));
        BlockPos surface = levelSurface((ServerLevel) maid.level(), BlockPos.containing(destination));
        maid.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(surface));
        maid.getBrain().setMemory(MemoryModuleType.WALK_TARGET,
                new WalkTarget(new BlockPosTracker(surface), 0.5f, 1));
        maid.getNavigation().moveTo(surface.getX() + 0.5, surface.getY(), surface.getZ() + 0.5, 0.5d);
    }

    private static BlockPos levelSurface(ServerLevel level, BlockPos pos) {
        return level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos);
    }

    private static void stopNavigation(EntityMaid maid) {
        maid.getNavigation().stop();
        maid.getBrain().eraseMemory(MemoryModuleType.PATH);
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        maid.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
        maid.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
    }

    private static void expire(EntityMaid maid, WanderingMaidSavedData savedData) {
        WanderingMaidData.markRejectAt(maid, maid.level().getGameTime());   // ★ 新增
        rejectPermanently(maid, savedData, maid.level().getGameTime());
    }

    private static void rejectPermanently(EntityMaid maid, WanderingMaidSavedData savedData, long gameTime) {
        WanderingMaidData.markRejectAt(maid, gameTime);   // ★ 新增（幂等，不会覆盖）
        stopNavigation(maid);
        MaidMovementControl.end(maid, MaidMovementControl.Reason.WANDERING_WAIT);
        maid.setInSittingPose(false);
        maid.setBegging(false);
        WanderingMaidData.setAcceptAuthorized(maid, false);
        WanderingMaidData.setState(maid, WanderingMaidState.REJECTED, gameTime);
        enforceWildState(maid);
    }

    private static void enforceWildState(EntityMaid maid) {
        maid.setTame(false, false);
        maid.setOwnerUUID(null);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !(event.getTarget() instanceof EntityMaid maid)
                || !WanderingMaidData.isSpecial(maid)) {
            return;
        }
        if (!WanderingMaidData.mayAccept(maid)) {
            enforceWildState(maid);
        }
        WanderingMaidState state = WanderingMaidData.state(maid);
        if (state == WanderingMaidState.REJECTED) {
            denyInteraction(event, player, "message.tlm_wandering_maid.wandering.rejected");
            return;
        }
        UUID target = WanderingMaidData.target(maid).orElse(null);
        if (!player.getUUID().equals(target)) {
            denyInteraction(event, player, "message.tlm_wandering_maid.wandering.other_player");
            return;
        }
        if (state != WanderingMaidState.WAITING) {
            denyInteraction(event, player, "message.tlm_wandering_maid.wandering.approaching");
            return;
        }
        PacketDistributor.sendToPlayer(player,
                new OpenWanderingMaidRequestS2CPacket(maid.getId(), maid.getUUID(), maid.getName()));
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    private static void denyInteraction(PlayerInteractEvent.EntityInteract event, ServerPlayer player, String key) {
        player.displayClientMessage(Component.translatable(key), true);
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    public static void handleDecision(ServerPlayer player, UUID maidId, boolean accept) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        Entity entity = level.getEntity(maidId);
        if (!(entity instanceof EntityMaid maid) || !maid.isAlive() || !WanderingMaidData.isSpecial(maid)
                || WanderingMaidData.state(maid) != WanderingMaidState.WAITING
                || WanderingMaidData.target(maid).filter(player.getUUID()::equals).isEmpty()
                || !maid.closerThan(player, 7.0)) {
            return;
        }
        if (player.getServer() == null) {
            return;
        }
        WanderingMaidSavedData savedData = WanderingMaidSavedData.get(player.getServer().overworld());
        if (accept) {
            acceptMaid(player, maid, savedData);
        } else {
            maid.setInSittingPose(false);
            maid.setBegging(false);
            WanderingMaidData.markRejectAt(maid, level.getGameTime());   // ★ 新增
            WanderingMaidData.setState(maid, WanderingMaidState.LEAVING, level.getGameTime());
        }
    }

    private static void acceptMaid(ServerPlayer player, EntityMaid maid, WanderingMaidSavedData savedData) {
        WanderingMaidData.setAcceptAuthorized(maid, true);
        InteractionResult result;
        try {
            result = ((EntityMaidTameInvoker) maid)
                    .tlm_wandering_maid$invokeTameMaid(new ItemStack(Items.CAKE), player);
            if (result.consumesAction() && maid.isOwnedBy(player)) {
                WanderingMaidData.clear(maid);
                stopNavigation(maid);
                MaidMovementControl.end(maid, MaidMovementControl.Reason.WANDERING_WAIT);
                maid.setInSittingPose(false);
                maid.setBegging(false);
                player.sendSystemMessage(Component.translatable(
                        "message.tlm_wandering_maid.wandering.accepted", maid.getName()));
                giveRandomItems(player, (ServerLevel) maid.level(), 2);
                maid.getChatBubbleManager().addTextChatBubble("bubble.tlm_wandering_maid.trade.new_owner");
                return;
            }
        } finally {
            WanderingMaidData.setAcceptAuthorized(maid, false);
        }
        player.sendSystemMessage(Component.translatable(
                "message.tlm_wandering_maid.wandering.tame_failed"));
    }

    @SubscribeEvent
    public void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof EntityMaid maid) || !WanderingMaidData.isSpecial(maid)
                || !(event.getSource().getEntity() instanceof ServerPlayer)
                || !(maid.level() instanceof ServerLevel level)) {
            return;
        }
        for (Item item : selectRandomItems(level, 3 + level.getRandom().nextInt(3))) {
            ItemEntity drop = new ItemEntity(level, maid.getX(), maid.getY() + 0.4, maid.getZ(),
                    item.getDefaultInstance());
            drop.setDefaultPickUpDelay();
            event.getDrops().add(drop);
        }
    }

    private static void giveRandomItems(ServerPlayer player, ServerLevel level, int count) {
        for (Item item : selectRandomItems(level, count)) {
            ItemStack gift = item.getDefaultInstance();
            if (!player.addItem(gift)) {
                player.drop(gift, false);
            }
        }
    }

    private static List<Item> selectRandomItems(ServerLevel level, int requestedCount) {
        Set<String> blacklist = normalizeItemIdList(WanderingMaidConfig.WANDERING_MAID_DROP_BLACKLIST.get());
        Set<String> whitelist = normalizeItemIdList(WanderingMaidConfig.WANDERING_MAID_DROP_WHITELIST.get());
        List<Item> candidates = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            ItemStack stack = item.getDefaultInstance();
            if (item != Items.AIR && !stack.isEmpty() && stack.isItemEnabled(level.enabledFeatures())
                    && isAllowedRandomItem(item, blacklist, whitelist)) {
                candidates.add(item);
            }
        }
        List<Item> selected = new ArrayList<>();
        for (int i = 0; i < requestedCount && !candidates.isEmpty(); i++) {
            selected.add(candidates.remove(level.getRandom().nextInt(candidates.size())));
        }
        return selected;
    }

    private static boolean isAllowedRandomItem(Item item, Set<String> blacklist, Set<String> whitelist) {
        var key = BuiltInRegistries.ITEM.getKey(item);
        if (key == null) {
            return false;
        }
        String itemId = key.toString().toLowerCase(Locale.ROOT);
        if (blacklist.contains(itemId)) {
            return false;
        }
        return whitelist.isEmpty() || whitelist.contains(itemId);
    }

    private static Set<String> normalizeItemIdList(List<? extends String> itemIds) {
        Set<String> normalized = new HashSet<>();
        for (String itemId : itemIds) {
            if (itemId != null && !itemId.isBlank()) {
                normalized.add(itemId.trim().toLowerCase(Locale.ROOT));
            }
        }
        return normalized;
    }

    @SubscribeEvent
    public void onEntityJoin(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof EntityMaid maid) || !(event.getLevel() instanceof ServerLevel level)
                || level != level.getServer().overworld() || !WanderingMaidData.isSpecial(maid)) {
            return;
        }
        enforceWildState(maid);
        if (WanderingMaidData.state(maid) != WanderingMaidState.REJECTED
                && !MaidMovementControl.isActive(maid, MaidMovementControl.Reason.WANDERING_WAIT)) {
            maid.setInSittingPose(false);
            MaidMovementControl.begin(maid, MaidMovementControl.Reason.WANDERING_WAIT,
                    EnumSet.of(MaidMovementControl.Field.PATH, MaidMovementControl.Field.POSE));
        }
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("tlm_wandering_maid")
                .then(Commands.literal("wanderingmaid")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("spawn")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    return spawnForPlayer(player, true) ? 1 : 0;
                                })
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> {
                                            ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                            return spawnForPlayer(player, true) ? 1 : 0;
                                        })))));
    }
}