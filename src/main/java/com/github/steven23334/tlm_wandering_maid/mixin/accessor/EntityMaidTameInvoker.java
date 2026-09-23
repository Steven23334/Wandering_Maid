package com.github.steven23334.tlm_wandering_maid.mixin.accessor;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EntityMaid.class)
public interface EntityMaidTameInvoker {
    @Invoker(value = "tameMaid", remap = false)
    InteractionResult tlm_wandering_maid$invokeTameMaid(ItemStack stack, Player player);
}