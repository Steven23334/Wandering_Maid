package com.github.steven23334.tlm_wandering_maid.compat.trade;

import com.github.steven23334.tlm_wandering_maid.TlmWanderingMaidMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record TradingMaidActionC2SPacket(int traderId, UUID maidId, Action action) implements CustomPacketPayload {
    public enum Action { BUY, SELL }

    public static final CustomPacketPayload.Type<TradingMaidActionC2SPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(
                    TlmWanderingMaidMod.MOD_ID, "trading_maid_action"));

    public static final StreamCodec<FriendlyByteBuf, TradingMaidActionC2SPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(FriendlyByteBuf buf, TradingMaidActionC2SPacket pkt) {
            buf.writeVarInt(pkt.traderId);
            buf.writeUUID(pkt.maidId);
            buf.writeEnum(pkt.action);
        }

        @Override
        public TradingMaidActionC2SPacket decode(FriendlyByteBuf buf) {
            return new TradingMaidActionC2SPacket(buf.readVarInt(), buf.readUUID(), buf.readEnum(Action.class));
        }
    };

    public static void handle(TradingMaidActionC2SPacket message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                TradingMaidManager.handleTradingAction(player, message.traderId, message.maidId, message.action);
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}