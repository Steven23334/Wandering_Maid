package com.github.steven23334.tlm_wandering_maid.compat.datagen;

import com.github.steven23334.tlm_wandering_maid.compat.item.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
        super(output, modid, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(ModItems.HUNT_ORDER.get());
        basicItem(ModItems.WANDERING_MAID_BOOK.get());
    }
}