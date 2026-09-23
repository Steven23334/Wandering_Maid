package com.github.steven23334.tlm_wandering_maid.compat.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * 狩猎指令物品。具体行为由事件系统处理。
 */
public class HuntOrderItem extends Item {
    public HuntOrderItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.tlm_wandering_maid.hunt_order"));
    }
}