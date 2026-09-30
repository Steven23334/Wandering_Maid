package com.github.steven23334.tlm_wandering_maid;

import com.github.steven23334.tlm_wandering_maid.compat.item.ModItems;
import com.github.steven23334.tlm_wandering_maid.config.WanderingMaidConfig;
import com.github.steven23334.tlm_wandering_maid.init.InitAttachTypes;
import com.github.steven23334.tlm_wandering_maid.network.NetworkRegistryHandler;
import com.github.steven23334.steven_mod_api.ApiTabContributors;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

@Mod(TlmWanderingMaidMod.MOD_ID)
public class TlmWanderingMaidMod {
    public static final String MOD_ID = "tlm_wandering_maid";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public TlmWanderingMaidMod(IEventBus modEventBus, ModContainer modContainer) {
        ModItems.ITEMS.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.SERVER, WanderingMaidConfig.SPEC,
                MOD_ID + "-Config.toml");

        InitAttachTypes.init(modEventBus);
        modEventBus.addListener(NetworkRegistryHandler::register);

        NeoForge.EVENT_BUS.register(new com.github.steven23334.tlm_wandering_maid.compat.wandering.WanderingMaidManager());
        NeoForge.EVENT_BUS.register(new com.github.steven23334.tlm_wandering_maid.compat.trade.TradingMaidManager());

        // 贡献到 API 的标签页
        ApiTabContributors.register(() -> List.of(
                new ItemStack(ModItems.WANDERING_MAID_BOOK.get())
        ));

        LOGGER.info("✅ TLM Wandering Maid mod 初始化完成！");
    }
}