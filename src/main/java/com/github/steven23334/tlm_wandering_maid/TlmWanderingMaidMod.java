package com.github.steven23334.tlm_wandering_maid;


import com.github.steven23334.tlm_wandering_maid.compat.item.ModItems;
import com.github.steven23334.tlm_wandering_maid.config.WanderingMaidConfig;
import com.github.steven23334.tlm_wandering_maid.init.InitAttachTypes;
import com.github.steven23334.tlm_wandering_maid.network.NetworkRegistryHandler;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(TlmWanderingMaidMod.MOD_ID)
public class TlmWanderingMaidMod {
    public static final String MOD_ID = "tlm_wandering_maid";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public TlmWanderingMaidMod(IEventBus modEventBus, ModContainer modContainer) {
        ModItems.ITEMS.register(modEventBus);
        ModItems.TABS.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.SERVER, WanderingMaidConfig.SPEC,
                MOD_ID + "-Config.toml");

        InitAttachTypes.init(modEventBus);
        modEventBus.addListener(NetworkRegistryHandler::register);

        // ✅ 注册创造模式标签页事件（必须用 modEventBus）
        modEventBus.addListener(this::addItemsToCreativeTabs);

        // 事件总线：流浪女仆 + 交易女仆
        NeoForge.EVENT_BUS.register(new com.github.steven23334.tlm_wandering_maid.compat.wandering.WanderingMaidManager());
        NeoForge.EVENT_BUS.register(new com.github.steven23334.tlm_wandering_maid.compat.trade.TradingMaidManager());

        LOGGER.info("✅ TLM Wandering Maid mod 初始化完成！");
    }

    /**
     * 将流浪女仆之书添加到车万女仆主物品栏的 entity_id_copy 之后
     */
    private void addItemsToCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        ResourceKey<CreativeModeTab> tlmMainTab = ResourceKey.create(
                Registries.CREATIVE_MODE_TAB,
                ResourceLocation.fromNamespaceAndPath("touhou_little_maid", "main")
        );

        // 只处理车万女仆的主物品栏
        if (event.getTabKey() != tlmMainTab) {
            return;
        }

        ResourceLocation anchorId = ResourceLocation.fromNamespaceAndPath(
                "touhou_little_maid", "entity_id_copy"
        );

        // ✅ 用注册表是否包含该 key 来判断，避免 "恒为 true" 的警告
        if (!BuiltInRegistries.ITEM.containsKey(anchorId)) {
            return; // 车万女仆未安装，或物品 ID 不对，跳过
        }

        Item anchorItem = BuiltInRegistries.ITEM.get(anchorId);

        event.insertAfter(
                anchorItem.getDefaultInstance(),
                ModItems.WANDERING_MAID_BOOK.get().getDefaultInstance(),
                CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
        );
    }
}