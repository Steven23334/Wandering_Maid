package com.github.steven23334.tlm_wandering_maid;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = TlmWanderingMaidMod.MOD_ID, dist = Dist.CLIENT)public class TlmWanderingMaidClient {
    public TlmWanderingMaidClient(ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}