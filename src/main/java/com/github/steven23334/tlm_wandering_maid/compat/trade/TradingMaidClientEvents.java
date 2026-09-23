package com.github.steven23334.tlm_wandering_maid.compat.trade;

import com.github.steven23334.tlm_wandering_maid.TlmWanderingMaidMod;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = TlmWanderingMaidMod.MOD_ID, value = Dist.CLIENT)
public final class TradingMaidClientEvents {
    private TradingMaidClientEvents() {
    }

    @SubscribeEvent
    public static void onMerchantScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof MerchantScreen screen)) {
            return;
        }
        int traderId = TradingMaidClientState.currentTraderId();
        if (traderId < 0) {
            return;
        }
        int left = (screen.width - 276) / 2;
        int top = (screen.height - 166) / 2;
        event.addListener(Button.builder(
                        Component.translatable("gui.tlm_wandering_maid.trade.open"), button ->
                                PacketDistributor.sendToServer(new RequestTradingMaidScreenC2SPacket(traderId)))
                .pos(left + 178, top - 23).size(94, 20).build());
        TradingMaidClientState.clear();
    }
}