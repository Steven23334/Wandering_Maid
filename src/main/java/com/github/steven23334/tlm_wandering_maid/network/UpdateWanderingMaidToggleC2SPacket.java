package com.github.steven23334.tlm_wandering_maid.network;

import com.github.steven23334.tlm_wandering_maid.TlmWanderingMaidMod;
import com.github.steven23334.tlm_wandering_maid.compat.wandering.WanderingMaidSavedData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/** 客户端 -> 服务端：修改玩家个人的流浪女仆开关。 */
public record UpdateWanderingMaidToggleC2SPacket(boolean enabled) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<UpdateWanderingMaidToggleC2SPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(
                    TlmWanderingMaidMod.MOD_ID, "update_wandering_maid_toggle"));

    public static final StreamCodec<FriendlyByteBuf, UpdateWanderingMaidToggleC2SPacket> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public void encode(FriendlyByteBuf buf, UpdateWanderingMaidToggleC2SPacket pkt) {
                    buf.writeBoolean(pkt.enabled);
                }

                @Override
                public @NotNull UpdateWanderingMaidToggleC2SPacket decode(FriendlyByteBuf buf) {
                    return new UpdateWanderingMaidToggleC2SPacket(buf.readBoolean());
                }
            };

    public static void handle(UpdateWanderingMaidToggleC2SPacket message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player && player.getServer() != null) {
                WanderingMaidSavedData.get(player.getServer().overworld())
                        .setEnabledFor(player.getUUID(), message.enabled);
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}