package com.github.steven23334.tlm_wandering_maid.compat.datagen;

import com.github.steven23334.tlm_wandering_maid.TlmWanderingMaidMod;
import net.minecraft.data.DataGenerator;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;



public class DataGenerators {
    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        generator.getVanillaPack(event.includeClient()).addProvider(output ->
                new ModItemModelProvider(output, TlmWanderingMaidMod.MOD_ID, existingFileHelper));
    }
}