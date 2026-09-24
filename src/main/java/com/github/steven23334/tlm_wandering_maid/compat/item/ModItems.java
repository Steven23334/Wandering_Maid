package com.github.steven23334.tlm_wandering_maid.compat.item;

import com.github.steven23334.tlm_wandering_maid.TlmWanderingMaidMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(BuiltInRegistries.ITEM, TlmWanderingMaidMod.MOD_ID);



    public static final Supplier<Item> WANDERING_MAID_BOOK = ITEMS.register("wandering_maid_book",
            () -> new WanderingMaidBookItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TlmWanderingMaidMod.MOD_ID);

}