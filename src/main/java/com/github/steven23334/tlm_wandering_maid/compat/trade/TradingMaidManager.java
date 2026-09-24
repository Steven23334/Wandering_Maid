package com.github.steven23334.tlm_wandering_maid.compat.trade;

import com.github.steven23334.tlm_wandering_maid.TlmWanderingMaidMod;
import com.github.steven23334.tlm_wandering_maid.compat.item.ModItems;
import com.github.steven23334.tlm_wandering_maid.compat.state.MaidMovementControl;
import com.github.steven23334.tlm_wandering_maid.compat.wandering.WanderingMaidData;
import com.github.steven23334.tlm_wandering_maid.compat.wandering.WanderingMaidSavedData;
import com.github.steven23334.tlm_wandering_maid.config.WanderingMaidConfig;
import com.github.steven23334.tlm_wandering_maid.mixin.accessor.EntityMaidTameInvoker;
import com.github.tartaricacid.touhoulittlemaid.api.event.InteractMaidEvent;
import com.github.tartaricacid.touhoulittlemaid.entity.info.ServerCustomPackLoader;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitItems;
import com.github.tartaricacid.touhoulittlemaid.item.ItemMaidBed;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

public final class TradingMaidManager {
    private static final String DEFAULT_MODEL = "geckolib_winefox";
    private static final String TRADER_INITIALIZED = TlmWanderingMaidMod.MOD_ID + ":maid_market_initialized";
    private static final String ITEM_OFFERS_INITIALIZED = TlmWanderingMaidMod.MOD_ID + ":item_offers_initialized";
    private static final String RESTOCK_COUNT = TlmWanderingMaidMod.MOD_ID + ":maid_market_restock_count";
    private static final String PURCHASE_MOVING = TlmWanderingMaidMod.MOD_ID + ":trading_maid_purchase_moving";
    private static final int TRADER_SCAN_RADIUS = 96;
    private static final int TRADE_RADIUS = 12;
    private static final int SELL_RADIUS = 32;
    private static final long PURCHASE_MOVE_TIMEOUT = 20L * 60L;

    private int tickCounter;

    private static boolean enabled() {
        return WanderingMaidConfig.WANDERING_TRADER_MAID_TRADE_ENABLED.get();
    }

    private static boolean shouldSpawnStock() {
        return enabled() && WanderingMaidConfig.WANDERING_TRADER_MAID_TRADE_WEIGHT.get() > 0;
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        if (++tickCounter % 20 != 0) {
            return;
        }
        MinecraftServer server = event.getServer();
        if (server == null) {
            return;
        }
        for (ServerLevel level : server.getAllLevels()) {
            if (enabled()) {
                initialiseNearbyTraders(level);
                tickPurchasedMaids(level);
                enforceTradingMaids(level);
            }
            // ★ 总开关关闭时不再调用 cleanDisabledMarket，已存在的待售女仆和交易物品保留
        }
    }

    private static void initialiseNearbyTraders(ServerLevel level) {
        Set<UUID> handled = new HashSet<>();
        for (ServerPlayer player : level.players()) {
            AABB area = player.getBoundingBox().inflate(TRADER_SCAN_RADIUS);
            for (WanderingTrader trader : level.getEntitiesOfClass(WanderingTrader.class, area,
                    candidate -> candidate.isAlive() && handled.add(candidate.getUUID()))) {
                initialiseTrader(level, trader, player);
            }
        }
    }

    private static void initialiseTrader(ServerLevel level, WanderingTrader trader, ServerPlayer player) {
        ensureCallResponseOffers(trader);
        CompoundTag traderData = trader.getPersistentData();
        if (traderData.getBoolean(TRADER_INITIALIZED)) {
            return;
        }
        traderData.putBoolean(TRADER_INITIALIZED, true);
        if (!shouldSpawnStock()) {
            return;
        }
        int count = 1 + level.getRandom().nextInt(3);
        for (int i = 0; i < count; i++) {
            spawnStockMaid(level, trader, player);
        }
    }

    private static void ensureCallResponseOffers(WanderingTrader trader) {
        if (trader.getPersistentData().getBoolean(ITEM_OFFERS_INITIALIZED)) {
            return;
        }

        addOfferIfMissing(trader, ModItems.WANDERING_MAID_BOOK, 12);
        addOfferIfMissing(trader, InitItems.SHRINE, 40 + trader.getRandom().nextInt(21));
        int bedPrice = 4 + trader.getRandom().nextInt(5);
        List<DyeColor> bedColors = List.of(DyeColor.WHITE, DyeColor.BLACK, DyeColor.YELLOW,
                DyeColor.BLUE, DyeColor.GREEN, DyeColor.PURPLE);
        DyeColor first = bedColors.get(trader.getRandom().nextInt(bedColors.size()));
        DyeColor second;
        do {
            second = bedColors.get(trader.getRandom().nextInt(bedColors.size()));
        } while (second == first);
        addBedOfferIfMissing(trader, first, bedPrice);
        addBedOfferIfMissing(trader, second, bedPrice);
        addOfferIfMissing(trader, InitItems.SMART_SLAB_EMPTY, 8 + trader.getRandom().nextInt(9));
        addOfferIfMissing(trader, InitItems.ULTRAMARINE_ORB_ELIXIR, 40);
        addOfferIfMissing(trader, InitItems.EXPLOSION_PROTECT_BAUBLE, 20);
        addOfferIfMissing(trader, InitItems.FIRE_PROTECT_BAUBLE, 18);
        addOfferIfMissing(trader, InitItems.PROJECTILE_PROTECT_BAUBLE, 20);
        addOfferIfMissing(trader, InitItems.MAGIC_PROTECT_BAUBLE, 24);
        addOfferIfMissing(trader, InitItems.FALL_PROTECT_BAUBLE, 14);
        addOfferIfMissing(trader, InitItems.DROWN_PROTECT_BAUBLE, 16);
        addOfferIfMissing(trader, InitItems.NIMBLE_FABRIC, 24);
        addOfferIfMissing(trader, InitItems.ITEM_MAGNET_BAUBLE, 28);
        addOfferIfMissing(trader, InitItems.MUTE_BAUBLE, 12);
        addOfferIfMissing(trader, InitItems.WIRELESS_IO, 32);
        trader.getPersistentData().putBoolean(ITEM_OFFERS_INITIALIZED, true);
    }

    private static void addOfferIfMissing(WanderingTrader trader, Supplier<? extends Item> item, int price) {
        if (trader.getOffers().stream().anyMatch(offer -> offer.getResult().is(item.get()))) {
            return;
        }
        trader.getOffers().add(new MerchantOffer(new ItemCost(Items.EMERALD, price),
                new ItemStack(item.get()), 8, 1, 0.05F));
    }

    private static void addBedOfferIfMissing(WanderingTrader trader, DyeColor color, int price) {
        if (trader.getOffers().stream().anyMatch(offer -> offer.getResult().is(InitItems.MAID_BED.get())
                && ItemMaidBed.getColor(offer.getResult()) == color)) {
            return;
        }
        ItemStack bed = new ItemStack(InitItems.MAID_BED.get());
        ItemMaidBed.setColor(color, bed);
        trader.getOffers().add(new MerchantOffer(new ItemCost(Items.EMERALD, price), bed, 8, 1, 0.05F));
    }

    private static void spawnStockMaid(ServerLevel level, WanderingTrader trader, ServerPlayer skinOwner) {
        EntityMaid maid = EntityMaid.TYPE.create(level);
        if (maid == null) {
            return;
        }
        double angle = level.getRandom().nextDouble() * Math.PI * 2.0;
        double distance = 2.0 + level.getRandom().nextDouble() * 2.0;
        BlockPos pos = BlockPos.containing(trader.getX() + Math.cos(angle) * distance,
                trader.getY(), trader.getZ() + Math.sin(angle) * distance);
        maid.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                level.getRandom().nextFloat() * 360.0F, 0.0F);
        maid.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null);
        maid.setModelId(selectSharedModel(level, skinOwner.getUUID()));
        maid.setPersistenceRequired();
        prepareForSale(maid, trader, randomPrice(level));
        if (level.addFreshEntity(maid)) {
            maid.setLeashedTo(trader, true);
        }
    }

    private static String selectSharedModel(ServerLevel level, UUID playerId) {
        WanderingMaidSavedData savedData = WanderingMaidSavedData.get(level.getServer().overworld());
        Set<String> available = ServerCustomPackLoader.SERVER_MAID_MODELS.getModelIdSet();
        List<String> pool = savedData.skinPool(playerId).stream().filter(available::contains).toList();
        if (!pool.isEmpty()) {
            return pool.get(level.getRandom().nextInt(pool.size()));
        }
        return available.contains(DEFAULT_MODEL) ? DEFAULT_MODEL : available.stream().findFirst().orElse(DEFAULT_MODEL);
    }

    private static int randomPrice(ServerLevel level) {
        int min = WanderingMaidConfig.WANDERING_TRADER_MAID_TRADE_PRICE_MIN.get();
        int max = Math.max(min, WanderingMaidConfig.WANDERING_TRADER_MAID_TRADE_PRICE_MAX.get());
        return min + level.getRandom().nextInt(max - min + 1);
    }

    private static void prepareForSale(EntityMaid maid, WanderingTrader trader, int price) {
        maid.stopUsingItem();
        maid.setTarget(null);
        maid.setBegging(false);
        maid.setInSittingPose(false);
        maid.setTame(false, false);
        maid.setOwnerUUID(null);
        TradingMaidData.initialize(maid, trader.getUUID(), price);
    }

    private static void enforceTradingMaids(ServerLevel level) {
        Set<UUID> handled = new HashSet<>();
        for (ServerPlayer player : level.players()) {
            for (EntityMaid maid : level.getEntitiesOfClass(EntityMaid.class,
                    player.getBoundingBox().inflate(TRADER_SCAN_RADIUS),
                    candidate -> TradingMaidData.isTrading(candidate) && handled.add(candidate.getUUID()))) {
                maid.setTame(false, false);
                maid.setOwnerUUID(null);
                maid.setTarget(null);
                maid.setBegging(false);
                Entity linkedTrader = TradingMaidData.trader(maid).map(level::getEntity).orElse(null);
                if (!(linkedTrader instanceof WanderingTrader trader) || !trader.isAlive()) {
                    maid.discard();
                }
            }
        }
    }

    private static void tickPurchasedMaids(ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            for (EntityMaid maid : level.getEntitiesOfClass(EntityMaid.class,
                    player.getBoundingBox().inflate(64), m -> m.getPersistentData().hasUUID(PURCHASE_MOVING))) {
                UUID ownerId = maid.getPersistentData().getUUID(PURCHASE_MOVING);
                Player owner = level.getPlayerByUUID(ownerId);
                long deadline = MaidMovementControl.getDeadline(maid, MaidMovementControl.Reason.PURCHASE_MOVING);
                if (owner == null || !maid.isAlive() || !maid.isTame()
                        || !ownerId.equals(maid.getOwnerUUID()) || level.getGameTime() >= deadline) {
                    maid.setBegging(false);
                    MaidMovementControl.clearNavigation(maid);
                    MaidMovementControl.end(maid, MaidMovementControl.Reason.PURCHASE_MOVING);
                    maid.getPersistentData().remove(PURCHASE_MOVING);
                    continue;
                }
                if (maid.distanceToSqr(owner) > 2.25) {
                    maid.setBegging(true);
                    BehaviorUtils.setWalkAndLookTargetMemories(maid, owner, 0.6F, 1);
                } else {
                    maid.setBegging(false);
                    maid.getNavigation().stop();
                    MaidMovementControl.end(maid, MaidMovementControl.Reason.PURCHASE_MOVING);
                    maid.setInSittingPose(true);
                    maid.getPersistentData().remove(PURCHASE_MOVING);
                    maid.getChatBubbleManager().addTextChatBubble(
                            "bubble.tlm_wandering_maid.trade.greeting");
                }
            }
        }
    }

    @SubscribeEvent
    public void onInteractMaid(InteractMaidEvent event) {
        if (TradingMaidData.isTrading(event.getMaid())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onDirectMaidInteraction(PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget() instanceof EntityMaid maid && TradingMaidData.isTrading(maid)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    @SubscribeEvent
    public void onTraderInteraction(PlayerInteractEvent.EntityInteract event) {
        if (!enabled() || !(event.getEntity() instanceof ServerPlayer player)
                || !(event.getTarget() instanceof WanderingTrader trader)
                || !(trader.level() instanceof ServerLevel level)) {
            return;
        }
        initialiseTrader(level, trader, player);
        PacketDistributor.sendToPlayer(player, new EnableTradingMaidButtonS2CPacket(trader.getId()));
    }

    public static void sendTradingScreen(ServerPlayer player, int traderId) {
        WanderingTrader trader = validTrader(player, traderId);
        if (trader == null) {
            return;
        }
        ServerLevel level = player.serverLevel();
        List<OpenTradingMaidScreenS2CPacket.MaidInfo> buys = level.getEntitiesOfClass(EntityMaid.class,
                        trader.getBoundingBox().inflate(TRADE_RADIUS), maid -> maid.isAlive()
                                && TradingMaidData.isTrading(maid)
                                && TradingMaidData.trader(maid).filter(trader.getUUID()::equals).isPresent())
                .stream().sorted(Comparator.comparingInt(TradingMaidData::price))
                .map(TradingMaidManager::toBuyInfo).toList();
        List<OpenTradingMaidScreenS2CPacket.MaidInfo> sells = level.getEntitiesOfClass(EntityMaid.class,
                        player.getBoundingBox().inflate(SELL_RADIUS), maid -> maid.isAlive() && maid.isOwnedBy(player)
                                && !TradingMaidData.isTrading(maid) && !WanderingMaidData.isSpecial(maid))
                .stream().sorted(Comparator.comparing(maid -> maid.getDisplayName().getString()))
                .map(TradingMaidManager::toSellInfo).toList();
        PacketDistributor.sendToPlayer(player,
                new OpenTradingMaidScreenS2CPacket(traderId, buys, sells));
    }

    private static OpenTradingMaidScreenS2CPacket.MaidInfo toBuyInfo(EntityMaid maid) {
        return maidInfo(maid, TradingMaidData.price(maid));
    }

    private static OpenTradingMaidScreenS2CPacket.MaidInfo toSellInfo(EntityMaid maid) {
        return maidInfo(maid, favorabilityToolCount(maid));
    }

    private static OpenTradingMaidScreenS2CPacket.MaidInfo maidInfo(EntityMaid maid, int value) {
        return new OpenTradingMaidScreenS2CPacket.MaidInfo(maid.getId(), maid.getUUID(), maid.getDisplayName(),
                maid.getModelId(), maid.hasCustomName(), value);
    }

    public static void handleTradingAction(ServerPlayer player, int traderId, UUID maidId,
                                           TradingMaidActionC2SPacket.Action action) {
        WanderingTrader trader = validTrader(player, traderId);
        if (trader == null) {
            return;
        }
        Entity entity = player.serverLevel().getEntity(maidId);
        if (!(entity instanceof EntityMaid maid) || !maid.isAlive()) {
            sendTradingScreen(player, traderId);
            return;
        }
        if (action == TradingMaidActionC2SPacket.Action.BUY) {
            buySelectedMaid(player, trader, maid);
        } else {
            sellSelectedMaid(player, trader, maid);
        }
        sendTradingScreen(player, traderId);
    }

    private static WanderingTrader validTrader(ServerPlayer player, int traderId) {
        if (!enabled()) {
            return null;
        }
        Entity entity = player.serverLevel().getEntity(traderId);
        if (!(entity instanceof WanderingTrader trader) || !trader.isAlive()
                || player.distanceToSqr(trader) > TRADE_RADIUS * TRADE_RADIUS) {
            return null;
        }
        return trader;
    }

    private static void buySelectedMaid(ServerPlayer buyer, WanderingTrader trader, EntityMaid maid) {
        if (!TradingMaidData.isTrading(maid)
                || TradingMaidData.trader(maid).filter(trader.getUUID()::equals).isEmpty()
                || maid.distanceToSqr(trader) > TRADE_RADIUS * TRADE_RADIUS) {
            return;
        }
        int price = TradingMaidData.price(maid);
        if (!buyer.isCreative() && buyer.getInventory().countItem(Items.EMERALD) < price) {
            buyer.displayClientMessage(Component.translatable(
                    "message.tlm_wandering_maid.trade.not_enough", price), true);
            return;
        }
        TradingMaidData.setPurchaseAuthorized(maid, true);
        InteractionResult result;
        try {
            result = ((EntityMaidTameInvoker) maid)
                    .tlm_wandering_maid$invokeTameMaid(new ItemStack(Items.CAKE), buyer);
        } finally {
            TradingMaidData.setPurchaseAuthorized(maid, false);
        }
        if (!result.consumesAction() || !maid.isOwnedBy(buyer)) {
            buyer.sendSystemMessage(Component.translatable(
                    "message.tlm_wandering_maid.trade.tame_failed"));
            return;
        }
        if (!buyer.isCreative()) {
            buyer.getInventory().clearOrCountMatchingItems(stack -> stack.is(Items.EMERALD), price,
                    buyer.inventoryMenu.getCraftSlots());
        }
        maid.dropLeash(true, false);
        TradingMaidData.clear(maid);
        MaidMovementControl.begin(maid, MaidMovementControl.Reason.PURCHASE_MOVING,
                java.util.EnumSet.of(MaidMovementControl.Field.PATH, MaidMovementControl.Field.POSE));
        maid.setInSittingPose(false);
        maid.setBegging(true);
        maid.getPersistentData().putUUID(PURCHASE_MOVING, buyer.getUUID());
        MaidMovementControl.setDeadline(maid, MaidMovementControl.Reason.PURCHASE_MOVING,
                maid.level().getGameTime() + PURCHASE_MOVE_TIMEOUT);
        BehaviorUtils.setWalkAndLookTargetMemories(maid, buyer, 0.6F, 1);
        buyer.sendSystemMessage(Component.translatable(
                "message.tlm_wandering_maid.trade.bought", price));
        maid.getChatBubbleManager().addTextChatBubble("bubble.tlm_wandering_maid.trade.new_owner");
        refreshStockIfNeeded(buyer, trader);
    }

    private static void sellSelectedMaid(ServerPlayer seller, WanderingTrader trader, EntityMaid maid) {
        if (!maid.isOwnedBy(seller) || TradingMaidData.isTrading(maid) || WanderingMaidData.isSpecial(maid)
                || maid.distanceToSqr(seller) > SELL_RADIUS * SELL_RADIUS) {
            return;
        }
        dropAllMaidItems(maid);
        maid.dropLeash(true, false);
        maid.teleportTo(trader.getX() + 1.25, trader.getY(), trader.getZ() + 1.25);
        prepareForSale(maid, trader, randomPrice(seller.serverLevel()));
        maid.setLeashedTo(trader, true);
        giveSaleReward(seller, seller.serverLevel());
        seller.sendSystemMessage(Component.translatable("message.tlm_wandering_maid.trade.sold"));
        maid.getChatBubbleManager().addTextChatBubble("bubble.tlm_wandering_maid.trade.sold");
    }

    private static int favorabilityToolCount(EntityMaid maid) {
        return Math.max(1, maid.getFavorability() / 64 + 1);
    }


    private static void giveSaleReward(ServerPlayer player, ServerLevel level) {
        // ===== 主要奖励物品（可配置 ID） =====
        String mainItemId = WanderingMaidConfig.WANDERING_TRADER_MAID_SELL_REWARD_MAIN_ITEM.get();
        Item mainItem = Items.AIR;
        if (mainItemId != null && !mainItemId.isBlank()) {
            ResourceLocation id = ResourceLocation.tryParse(mainItemId.trim());
            if (id != null) {
                mainItem = BuiltInRegistries.ITEM.get(id);
            }
        }

        if (mainItem != Items.AIR) {
            int mainCount = WanderingMaidConfig.WANDERING_TRADER_MAID_SELL_REWARD_MAIN_COUNT.get();
            boolean onlyIfMissing = WanderingMaidConfig.WANDERING_TRADER_MAID_SELL_REWARD_MAIN_ONLY_IF_MISSING.get();
            boolean shouldGive = !onlyIfMissing || player.getInventory().countItem(mainItem) == 0;
            if (shouldGive) {
                give(player, new ItemStack(mainItem, mainCount));
            }
        }

        // ===== 附属奖励物品列表 =====
        for (String entry : WanderingMaidConfig.WANDERING_TRADER_MAID_SELL_REWARD_EXTRA_ITEMS.get()) {
            giveExtraReward(player, level, entry);
        }
    }

    /** 解析 "物品ID;min;max" 并发放。格式错误或物品无效时跳过。 */
    private static void giveExtraReward(ServerPlayer player, ServerLevel level, String entry) {
        if (entry == null || entry.isBlank()) {
            return;
        }
        String[] parts = entry.split(";");
        if (parts.length != 3) {
            return;
        }
        ResourceLocation id = ResourceLocation.tryParse(parts[0].trim());
        if (id == null) {
            return;
        }
        Item item = BuiltInRegistries.ITEM.get(id);
        if (item == Items.AIR) {
            return;
        }
        int min;
        int max;
        try {
            min = Integer.parseInt(parts[1].trim());
            max = Integer.parseInt(parts[2].trim());
        } catch (NumberFormatException e) {
            return;
        }
        giveRandomRange(player, level, item, min, max);
    }

    /** 在 [min, max] 闭区间内随机一个数量并发放；min/max 都为 0 时不发放。 */
    private static void giveRandomRange(ServerPlayer player, ServerLevel level, Item item, int min, int max) {
        int lo = Math.min(min, max);
        int hi = Math.max(min, max);
        if (hi <= 0) {
            return;
        }
        int count = lo + level.getRandom().nextInt(hi - lo + 1);
        if (count > 0) {
            give(player, new ItemStack(item, count));
        }
    }

    private static void give(ServerPlayer player, ItemStack stack) {
        if (!player.addItem(stack)) {
            player.drop(stack, false);
        }
    }

    private static void refreshStockIfNeeded(ServerPlayer buyer, WanderingTrader trader) {
        ServerLevel level = buyer.serverLevel();
        CompoundTag data = trader.getPersistentData();

        int maxRestock = WanderingMaidConfig.WANDERING_TRADER_MAID_TRADE_RESTOCK_TIMES.get();
        int currentCount = data.getInt(RESTOCK_COUNT);
        if (currentCount >= maxRestock || !shouldSpawnStock()) {
            return;
        }

        boolean hasStock = !level.getEntitiesOfClass(EntityMaid.class, trader.getBoundingBox().inflate(TRADE_RADIUS),
                candidate -> candidate.isAlive() && TradingMaidData.isTrading(candidate)
                        && TradingMaidData.trader(candidate).filter(trader.getUUID()::equals).isPresent()).isEmpty();
        if (hasStock) {
            return;
        }

        data.putInt(RESTOCK_COUNT, currentCount + 1);
        int count = 1 + level.getRandom().nextInt(3);
        for (int i = 0; i < count; i++) {
            spawnStockMaid(level, trader, buyer);
        }
    }

    private static void dropAllMaidItems(EntityMaid maid) {
        dropHandler(maid, maid.getMaidInv());
        dropHandler(maid, maid.getMaidBauble());
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = maid.getItemBySlot(slot);
            if (!stack.isEmpty()) {
                maid.spawnAtLocation(stack.copy());
                maid.setItemSlot(slot, ItemStack.EMPTY);
            }
        }
    }

    private static void dropHandler(EntityMaid maid, IItemHandler handler) {
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                ItemStack extracted = handler.extractItem(slot, stack.getCount(), false);
                if (!extracted.isEmpty()) {
                    maid.spawnAtLocation(extracted);
                }
            }
        }
    }

    @SubscribeEvent
    public void onMaidJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof EntityMaid maid && TradingMaidData.isTrading(maid)) {
            maid.setTame(false, false);
            maid.setOwnerUUID(null);
        }
    }

    @SubscribeEvent
    public void onTraderDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof WanderingTrader trader)
                || !(trader.level() instanceof ServerLevel level)) {
            return;
        }
        AABB area = trader.getBoundingBox().inflate(64);
        if (WanderingMaidConfig.WANDERING_TRADER_MAID_FREE_ON_TRADER_DEATH.get()) {
            releaseLinkedMaids(level, trader.getUUID(), area);
        } else {
            removeLinkedMaids(level, trader.getUUID(), area);
        }
    }

    @SubscribeEvent    public void onTraderLeave(EntityLeaveLevelEvent event) {
        if (!(event.getEntity() instanceof WanderingTrader trader)
                || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        Entity.RemovalReason reason = trader.getRemovalReason();
        if (reason == Entity.RemovalReason.UNLOADED_TO_CHUNK || reason == Entity.RemovalReason.CHANGED_DIMENSION) {
            return;
        }
        // ★ 商人只是离开（未死亡）时，无论配置如何，都删除绑定的女仆
        removeLinkedMaids(level, trader.getUUID(), trader.getBoundingBox().inflate(64));
    }

    /** 旧行为：直接删除绑定到该商人的待售女仆。 */
    private static void removeLinkedMaids(ServerLevel level, UUID traderId, AABB area) {
        List<EntityMaid> linked = new ArrayList<>(level.getEntitiesOfClass(EntityMaid.class, area,
                maid -> TradingMaidData.isTrading(maid)
                        && TradingMaidData.trader(maid).filter(traderId::equals).isPresent()));
        linked.forEach(Entity::discard);
    }

    /** 商人死亡时：把绑定的待售女仆恢复为无主自由女仆，而不是删除。 */
    private static void releaseLinkedMaids(ServerLevel level, UUID traderId, AABB area) {
        List<EntityMaid> linked = new ArrayList<>(level.getEntitiesOfClass(EntityMaid.class, area,
                maid -> TradingMaidData.isTrading(maid)
                        && TradingMaidData.trader(maid).filter(traderId::equals).isPresent()));
        for (EntityMaid maid : linked) {
            TradingMaidData.clear(maid);
            maid.dropLeash(true, false);
            maid.setTame(false, false);
            maid.setOwnerUUID(null);
            maid.setTarget(null);
            maid.setBegging(false);
            maid.setInSittingPose(false);
            MaidMovementControl.end(maid, MaidMovementControl.Reason.PURCHASE_MOVING);
            maid.getPersistentData().remove(PURCHASE_MOVING);
        }
    }
}