package com.github.steven23334.tlm_wandering_maid;


import com.github.steven23334.tlm_wandering_maid.compat.item.ModItems;
import com.github.steven23334.tlm_wandering_maid.config.WanderingMaidConfig;
import com.github.steven23334.tlm_wandering_maid.init.InitAttachTypes;
import com.github.steven23334.tlm_wandering_maid.network.NetworkRegistryHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(TlmWanderingMaidMod.MOD_ID)
public class TlmWanderingMaidMod {
    public static final String MOD_ID = "tlm_wandering_maid";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public TlmWanderingMaidMod(IEventBus modEventBus, ModContainer modContainer) {
        ModItems.ITEMS.register(modEventBus);
        ModItems.TABS.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, WanderingMaidConfig.SPEC,
                MOD_ID + "-wandering.toml");

        InitAttachTypes.init(modEventBus);
        modEventBus.addListener(NetworkRegistryHandler::register);

        // 事件总线：流浪女仆 + 交易女仆
        NeoForge.EVENT_BUS.register(new com.github.steven23334.tlm_wandering_maid.compat.wandering.WanderingMaidManager());
        NeoForge.EVENT_BUS.register(new com.github.steven23334.tlm_wandering_maid.compat.trade.TradingMaidManager());

        LOGGER.info("✅ TLM Wandering Maid mod 初始化完成！");
    }
}