package com.github.steven23334.tlm_wandering_maid.init;

import com.github.steven23334.tlm_wandering_maid.TlmWanderingMaidMod;
import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class InitAttachTypes {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, TlmWanderingMaidMod.MOD_ID);

    /** 手动实现的同步处理器，兼容 21.1.192+。 */
    public static final AttachmentSyncHandler<Boolean> BOOL_SYNC_HANDLER = new AttachmentSyncHandler<>() {
        @Override
        public void write(RegistryFriendlyByteBuf buf, Boolean attachment, boolean initialSync) {
            buf.writeBoolean(attachment);
        }

        @Override
        public @Nullable Boolean read(IAttachmentHolder holder,
                                      RegistryFriendlyByteBuf buf,
                                      @Nullable Boolean previousValue) {
            return buf.readBoolean();
        }
    };

    public static final Supplier<AttachmentType<Boolean>> SYNCED_WANDERING_SPECIAL =
            ATTACHMENT_TYPES.register("wandering_special", r ->
                    AttachmentType.builder(h -> false)
                            .serialize(Codec.BOOL)
                            .sync(BOOL_SYNC_HANDLER)
                            .build());

    public static final Supplier<AttachmentType<Boolean>> SYNCED_TRADING =
            ATTACHMENT_TYPES.register("trading", r ->
                    AttachmentType.builder(h -> false)
                            .serialize(Codec.BOOL)
                            .sync(BOOL_SYNC_HANDLER)
                            .build());

    public static void init(IEventBus bus) {
        ATTACHMENT_TYPES.register(bus);
    }
}