package com.github.steven23334.tlm_wandering_maid.network;

import com.github.steven23334.tlm_wandering_maid.TlmWanderingMaidMod;
import com.github.steven23334.tlm_wandering_maid.compat.client.gui.WanderingMaidSkinPoolScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务端 -> 客户端：打开皮肤池 UI（只带已选，候选由 MaidModelGui 自己提供）。
 */
public record OpenWanderingSkinPoolS2CPacket(List<String> selectedModels) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OpenWanderingSkinPoolS2CPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(
                    TlmWanderingMaidMod.MOD_ID, "open_wandering_skin_pool"));

    public static final StreamCodec<FriendlyByteBuf, OpenWanderingSkinPoolS2CPacket> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public void encode(FriendlyByteBuf buf, OpenWanderingSkinPoolS2CPacket pkt) {
                    buf.writeVarInt(Math.min(pkt.selectedModels.size(), 512));
                    pkt.selectedModels.stream().limit(512).forEach(id -> buf.writeUtf(id, 256));
                }

                @Override
                public @NotNull OpenWanderingSkinPoolS2CPacket decode(FriendlyByteBuf buf) {
                    int size = buf.readVarInt();
                    if (size < 0 || size > 512) {
                        throw new IllegalArgumentException("Invalid size: " + size);
                    }
                    List<String> models = new ArrayList<>(size);
                    for (int i = 0; i < size; i++) {
                        models.add(buf.readUtf(256));
                    }
                    return new OpenWanderingSkinPoolS2CPacket(models);
                }
            };

    public static void handle(OpenWanderingSkinPoolS2CPacket message, IPayloadContext context) {
        context.enqueueWork(() -> WanderingMaidSkinPoolScreen.open(message.selectedModels));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}