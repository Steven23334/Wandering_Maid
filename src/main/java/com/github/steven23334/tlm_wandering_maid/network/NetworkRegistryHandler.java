package com.github.steven23334.tlm_wandering_maid.network;

import com.github.steven23334.tlm_wandering_maid.TlmWanderingMaidMod;
import com.github.steven23334.tlm_wandering_maid.compat.trade.EnableTradingMaidButtonS2CPacket;
import com.github.steven23334.tlm_wandering_maid.compat.trade.OpenTradingMaidScreenS2CPacket;
import com.github.steven23334.tlm_wandering_maid.compat.trade.RequestTradingMaidScreenC2SPacket;
import com.github.steven23334.tlm_wandering_maid.compat.trade.TradingMaidActionC2SPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import com.github.steven23334.tlm_wandering_maid.network.UpdateWanderingSkinPoolC2SPacket;

public final class NetworkRegistryHandler {
    private NetworkRegistryHandler() {
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(TlmWanderingMaidMod.MOD_ID);

        // 流浪女仆
        registrar.playToClient(
                OpenWanderingMaidRequestS2CPacket.TYPE,
                OpenWanderingMaidRequestS2CPacket.STREAM_CODEC,
                OpenWanderingMaidRequestS2CPacket::handle);
        registrar.playToClient(
                OpenWanderingSkinPoolS2CPacket.TYPE,
                OpenWanderingSkinPoolS2CPacket.STREAM_CODEC,
                OpenWanderingSkinPoolS2CPacket::handle);
        registrar.playToServer(
                WanderingMaidDecisionC2SPacket.TYPE,
                WanderingMaidDecisionC2SPacket.STREAM_CODEC,
                WanderingMaidDecisionC2SPacket::handle);
        registrar.playToServer(
                UpdateWanderingSkinPoolC2SPacket.TYPE,
                UpdateWanderingSkinPoolC2SPacket.STREAM_CODEC,
                UpdateWanderingSkinPoolC2SPacket::handle);

        // 交易女仆
        registrar.playToClient(
                EnableTradingMaidButtonS2CPacket.TYPE,
                EnableTradingMaidButtonS2CPacket.STREAM_CODEC,
                EnableTradingMaidButtonS2CPacket::handle);
        registrar.playToClient(
                OpenTradingMaidScreenS2CPacket.TYPE,
                OpenTradingMaidScreenS2CPacket.STREAM_CODEC,
                OpenTradingMaidScreenS2CPacket::handle);
        registrar.playToServer(
                RequestTradingMaidScreenC2SPacket.TYPE,
                RequestTradingMaidScreenC2SPacket.STREAM_CODEC,
                RequestTradingMaidScreenC2SPacket::handle);
        registrar.playToServer(
                TradingMaidActionC2SPacket.TYPE,
                TradingMaidActionC2SPacket.STREAM_CODEC,
                TradingMaidActionC2SPacket::handle);
    }
}