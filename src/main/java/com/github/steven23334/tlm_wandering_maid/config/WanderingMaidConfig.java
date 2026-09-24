package com.github.steven23334.tlm_wandering_maid.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class WanderingMaidConfig {
    public static final ModConfigSpec SPEC;

    // ===== 总开关 =====
    public static final ModConfigSpec.BooleanValue WANDERING_MAID_ENABLED;
    public static final ModConfigSpec.BooleanValue WANDERING_TRADER_MAID_TRADE_ENABLED;

    // ===== 流浪女仆 =====
    public static final ModConfigSpec.IntValue WANDERING_MAID_INTERVAL_MINUTES;
    public static final ModConfigSpec.IntValue WANDERING_MAID_SPAWN_CHANCE;
    public static final ModConfigSpec.IntValue WANDERING_MAID_COUNT;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> WANDERING_MAID_DROP_BLACKLIST;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> WANDERING_MAID_DROP_WHITELIST;

    // ===== 流浪商人女仆交易（外层）=====
    public static final ModConfigSpec.IntValue WANDERING_TRADER_MAID_TRADE_WEIGHT;
    public static final ModConfigSpec.IntValue WANDERING_TRADER_MAID_TRADE_PRICE_MIN;
    public static final ModConfigSpec.IntValue WANDERING_TRADER_MAID_TRADE_PRICE_MAX;
    public static final ModConfigSpec.BooleanValue WANDERING_TRADER_MAID_FREE_ON_TRADER_DEATH;
    public static final ModConfigSpec.IntValue WANDERING_TRADER_MAID_TRADE_RESTOCK_TIMES;

    // ===== 卖出女仆奖励（内层，嵌套在交易组内）=====
    public static final ModConfigSpec.ConfigValue<String> WANDERING_TRADER_MAID_SELL_REWARD_MAIN_ITEM;
    public static final ModConfigSpec.IntValue WANDERING_TRADER_MAID_SELL_REWARD_MAIN_COUNT;
    public static final ModConfigSpec.BooleanValue WANDERING_TRADER_MAID_SELL_REWARD_MAIN_ONLY_IF_MISSING;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> WANDERING_TRADER_MAID_SELL_REWARD_EXTRA_ITEMS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        // ===== 总开关 =====
        builder.comment("总开关")
                .translation("tlm_wandering_maid.configuration.masterSwitches")
                .push("master_switches");

        WANDERING_MAID_ENABLED = builder
                .comment("流浪女仆功能总开关。关闭后不再自动生成流浪女仆，但响应 /tlm_wandering_maid wanderingmaid spawn 指令；"
                        + "已经存在的流浪女仆不受影响，会继续按原状态机运行，仍然可以正常交互与收留。")
                .translation("tlm_wandering_maid.configuration.wanderingMaidEnabled")
                .define("wanderingMaidEnabled", true);

        WANDERING_TRADER_MAID_TRADE_ENABLED = builder
                .comment("流浪商人女仆交易功能总开关。关闭后不再生成待售女仆、不添加交易物品、不显示交易女仆按钮；"
                        + "已经存在的待售女仆和已注入的交易物品不受影响，仍然可以正常交易。")
                .translation("tlm_wandering_maid.configuration.wanderingTraderMaidTradeEnabled")
                .define("wanderingTraderMaidTradeEnabled", true);

        builder.pop();

        // ===== 流浪女仆 =====
        builder.comment("流浪女仆事件配置")
                .translation("tlm_wandering_maid.configuration.wanderingMaid")
                .push("wandering_maid");

        WANDERING_MAID_INTERVAL_MINUTES = builder
                .comment("每隔多少分钟尝试触发一次流浪女仆事件（范围 0~60 分钟；0 表示关闭自动触发，指令触发不受影响）")
                .translation("tlm_wandering_maid.configuration.wanderingMaidInterval")
                .defineInRange("spawnIntervalMinutes", 5, 0, 60);

        WANDERING_MAID_SPAWN_CHANCE = builder
                .comment("每次到达触发时间后，实际生成流浪女仆事件的概率（范围 0%~100%）")
                .translation("tlm_wandering_maid.configuration.wanderingMaidSpawnChance")
                .defineInRange("spawnChancePercent", 25, 0, 100);

        WANDERING_MAID_COUNT = builder
                .comment("每名玩家在一次流浪事件中生成的女仆数量（范围 1~10）")
                .translation("tlm_wandering_maid.configuration.wanderingMaidCount")
                .defineInRange("maidsPerPlayer", 1, 1, 10);

        WANDERING_MAID_DROP_BLACKLIST = builder
                .comment("流浪女仆死亡掉落与收留赠礼的物品黑名单。填写完整物品 ID，例如 minecraft:bedrock；可无限添加，黑名单优先于白名单")
                .translation("tlm_wandering_maid.configuration.wanderingDropBlacklist")
                .defineListAllowEmpty("dropBlacklist", List::of, () -> "minecraft:bedrock", value -> value instanceof String);

        WANDERING_MAID_DROP_WHITELIST = builder
                .comment("流浪女仆死亡掉落与收留赠礼的物品白名单。留空时允许所有非黑名单物品；填写后只会随机其中的物品，例如 minecraft:diamond")
                .translation("tlm_wandering_maid.configuration.wanderingDropWhitelist")
                .defineListAllowEmpty("dropWhitelist", List::of, () -> "minecraft:bedrock", value -> value instanceof String);

        builder.pop();

        // ===== 流浪商人女仆交易（外层）=====
        builder.comment("流浪商人女仆交易配置")
                .translation("tlm_wandering_maid.configuration.wanderingTraderTrade")
                .push("wandering_trader_maid_trade");

        WANDERING_TRADER_MAID_TRADE_WEIGHT = builder
                .comment("流浪商人出现时，附带待售女仆的权重。范围 0~100；0 表示不再生成待售女仆（但保留交易界面）。总开关关闭时此项无效。")
                .translation("tlm_wandering_maid.configuration.wanderingTraderTradeWeight")
                .defineInRange("stockWeight", 100, 0, 100);

        WANDERING_TRADER_MAID_TRADE_PRICE_MIN = builder
                .comment("待售女仆的最低绿宝石价格（范围 1~999）")
                .translation("tlm_wandering_maid.configuration.wanderingTraderTradePriceMin")
                .defineInRange("priceMin", 1, 1, 999);

        WANDERING_TRADER_MAID_TRADE_PRICE_MAX = builder
                .comment("待售女仆的最高绿宝石价格（范围 1~999，需不低于最低价格）")
                .translation("tlm_wandering_maid.configuration.wanderingTraderTradePriceMax")
                .defineInRange("priceMax", 16, 1, 999);

        WANDERING_TRADER_MAID_FREE_ON_TRADER_DEATH = builder
                .comment("流浪商人死亡时，绑定的待售女仆是否恢复自由身（不死亡）。"
                        + "true：女仆存活，解除交易标记，变成无主自由女仆；"
                        + "false：维持原行为，女仆被直接删除。"
                        + "注意：流浪商人只是离开（未死亡）时，无论此项如何设置，女仆都会被删除。")
                .translation("tlm_wandering_maid.configuration.wanderingTraderMaidFreeOnDeath")
                .define("freeMaidOnTraderDeath", true);

        WANDERING_TRADER_MAID_TRADE_RESTOCK_TIMES = builder
                .comment("每个流浪商人最多可以补货几次。0 表示买空后不再补货；范围 0~99。")
                .translation("tlm_wandering_maid.configuration.wanderingTraderTradeRestockTimes")
                .defineInRange("restockTimes", 1, 0, 99);

        // ===== 卖出女仆奖励（内层）=====
        builder.comment("车万女仆给予物品配置")
                .translation("tlm_wandering_maid.configuration.GiveThingsConfig")
                .push("wandering_give_things_config");

        WANDERING_TRADER_MAID_SELL_REWARD_MAIN_ITEM = builder
                .comment("卖出女仆后给予的主要奖励物品 ID，格式为 命名空间:路径，例如 touhou_little_maid:favorability_tool_full。"
                        + "留空或填写无效 ID 时不会发放该物品。")
                .translation("tlm_wandering_maid.configuration.sellRewardMainItem")
                .define("sellRewardMainItem", "touhou_little_maid:favorability_tool_full");

        WANDERING_TRADER_MAID_SELL_REWARD_MAIN_COUNT = builder
                .comment("主要奖励物品的数量（范围 1~64）")
                .translation("tlm_wandering_maid.configuration.sellRewardMainCount")
                .defineInRange("sellRewardMainCount", 1, 1, 64);

        WANDERING_TRADER_MAID_SELL_REWARD_MAIN_ONLY_IF_MISSING = builder
                .comment("主要奖励物品是否仅在玩家背包中没有时发放。"
                        + "true：背包已有则不重复给；false：每次卖出都给。")
                .translation("tlm_wandering_maid.configuration.sellRewardMainOnlyIfMissing")
                .define("sellRewardMainOnlyIfMissing", true);

        WANDERING_TRADER_MAID_SELL_REWARD_EXTRA_ITEMS = builder
                .comment("卖出女仆后额外给予的物品列表。每项格式为 物品ID;最少数量;最多数量",
                        "数量区间为闭区间，min/max 写反会自动交换；min 和 max 都为 0 时该项不发放。",
                        "Extra items granted on maid sale. Format: ItemID;minAmount;maxAmount.",
                        "Inclusive range; reversed min/max auto-swap; both 0 = item skipped.",
                        "Invalid item IDs skip that entry only.")
                .translation("tlm_wandering_maid.configuration.sellRewardExtraItems")
                .defineListAllowEmpty("sellRewardExtraItems",
                        List.of(
                                "minecraft:netherite_ingot;1;3",
                                "minecraft:enchanted_golden_apple;3;8"
                        ),
                        () -> "minecraft:netherite_ingot;1;3",
                        value -> value instanceof String);

        builder.pop();   // 退出第 2 层（wandering_give_things_config）
        builder.pop();   // 退出第 1 层（wandering_trader_maid_trade）

        SPEC = builder.build();
    }
}