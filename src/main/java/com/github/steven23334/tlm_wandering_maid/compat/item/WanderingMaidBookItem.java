package com.github.steven23334.tlm_wandering_maid.compat.item;

import com.github.steven23334.tlm_wandering_maid.compat.wandering.WanderingMaidSavedData;
import com.github.steven23334.tlm_wandering_maid.network.OpenWanderingSkinPoolS2CPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

public final class WanderingMaidBookItem extends Item {
    public WanderingMaidBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && serverPlayer.getServer() != null) {
            WanderingMaidSavedData data =
                    WanderingMaidSavedData.get(serverPlayer.getServer().overworld());
            PacketDistributor.sendToPlayer(serverPlayer,
                    new OpenWanderingSkinPoolS2CPacket(data.skinPool(serverPlayer.getUUID())));
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }
}