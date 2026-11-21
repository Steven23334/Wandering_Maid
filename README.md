# Touhou Little Maid: Wandering Maid and Maid Trading

[![Available on GitHub](https://wsrv.nl/?url=https%3A%2F%2Fcdn.jsdelivr.net%2Fnpm%2F%40intergrav%2Fdevins-badges%403%2Fassets%2Fcozy%2Favailable%2Fgithub_vector.svg&n=-1)](https://github.com/Steven23334/Wandering_Maid)
[![Available for Touhou Little Maid](https://cdn.modrinth.com/data/cached_images/ea5dc160571134bd0ea89ac52075b542fb331e46_0.webp)](https://modrinth.com/project/R0bDWFAW)

This is an addon for **Touhou Little Maid** on NeoForge 1.21.1. It adds wandering maids who seek you out in the wild, wandering traders who bring maids to market, and a craftable book for managing your personal wandering-maid toggle and skin pool.
(This mod is derived from [Touhou Little Maid: Love & Loathe](https://www.curseforge.com/minecraft/mc-mods/touhou-little-maid-love-loathe).)

## ✨ Features

### Wandering Maid

- In the Overworld, a wandering maid may appear 12–32 blocks away, preferably in front of you, and glows for 60 seconds so she is easy to spot.
- She walks up to you, sits down, and waits for your answer. Only the player she was bound to can interact with her.
- Right-click her to open the request screen:
  - **Accept** — adopts her through Touhou Little Maid's taming flow (your maid limit still applies) and gives you a random gift of 2 items.
  - **Reject** — she leaves. If you walk away while she is waiting, she follows and waits again, up to 3 times; after that she is permanently rejected.
- Rejected maids are cleaned up after a configurable delay (30 seconds by default) instead of piling up in the world.
- Killing her before adoption makes her drop 3–5 random items.
- Adoption gifts and death drops have separate on/off switches and share the same item blacklist/whitelist.
- Each player has an independent spawn timer and a personal on/off toggle. The toggle is controlled from the Wandering Maid Book; players who never changed it use the server's `defaultEnabled` value.

### Wandering Trader Maid Trading

- While trader maid trading is enabled, wandering traders also receive four extra offers: **Wandering Maid Book** (2 emeralds), **Shrine** (5–6 emeralds), **Soul Spell** (1 emerald), and **Ultramarine Orb Elixir** (25 emeralds, unbreakable).
- If stocking is enabled, 1–3 maids for sale appear leashed near the trader.
- A **Maid Trade** button appears on the merchant screen and opens a two-column, paged market:
  - **For Sale** — buy a maid with emeralds. She walks to you, sits down, and greets her new owner. Creative mode buys for free; your maid limit still applies.
  - **My Maids** — sell an owned maid within 32 blocks. Her inventory, baubles, and equipment drop where she stands, and the trader resells her. You receive the configured reward (by default: one Favorability Tool if you do not already have one, 1–3 netherite ingots, and 3–8 enchanted golden apples).
- After the last maid is bought, the trader can restock a limited number of times (1 by default), bringing 1–3 new maids.
- If the trader dies, stock maids become ownerless free maids by default (configurable to delete them instead). If the trader despawns without dying, the stock maids are removed.

### Wandering Maid Book & Skin Pool

- Crafted shapeless from **Book and Quill + Emerald**. While trader maid trading is enabled, wandering traders also sell it for 2 emeralds.
- Using it opens the skin pool screen: click a model to add or remove it, preview it, and see the selected count. The selection is saved when the screen closes and is stored per player.
- Wandering maids use the target player's pool. A trader's stock uses the pool of the player who first initialized that trader. If the pool is empty, the default model is used.
- The checkbox on the screen toggles your personal wandering-maid events; players who have never changed it use the server's `defaultEnabled` setting.

## 🎯 Purpose

- Add a more organic, event-driven way to obtain maids beyond crafting or breeding.
- Give players direct control over which models wandering and trading maids use.
- Provide modpack authors with configurable spawn rates, prices, drops, restock behavior, and reward items.
- Add more fun and strategy to maid gameplay and server economies.

## ⚠️ Requirements

- **Minecraft**: 1.21.1
- **NeoForge**: 21.1.209 or newer
- **Touhou Little Maid**: required (developed against 1.5.3) — [CurseForge](https://www.curseforge.com/minecraft/mc-mods/touhou-little-maid)
- **Steven's Mod API**: 1.0.0 or newer, required and not bundled — [GitHub](https://github.com/Steven23334/Steven_Mod_API)

## 🔧 Installation

1. Install the required mods and NeoForge version listed above.
2. Place the `.jar` files in the `.minecraft/mods` folder.
3. Launch the game.

## 🎮 Commands

Requires permission level 2:

```
/tlm_wandering_maid wanderingmaid spawn
/tlm_wandering_maid wanderingmaid spawn <player>
```

Manually triggers a wandering maid event for the target player (or yourself). The command ignores the wandering-maid master switch, the personal toggle, the spawn interval/chance, and the global alive cap, but it still requires the Overworld.

## ⚙️ Configuration

Server-side config file: `config/tlm_wandering_maid-Config.toml` on a dedicated server; in single-player it lives in the world's `serverconfig` folder. It can also be opened from the in-game mod config screen where available.

### Master Switches

TOML section: `[master_switches]`

| Key | Default | Description |
| --- | --- | --- |
| `master_switches.wanderingMaidEnabled` | `true` | Enables automatic wandering maid events. Turning it off does not affect existing maids or the spawn command. |
| `master_switches.wanderingTraderMaidTradeEnabled` | `true` | Enables stock maids, the extra trader offers, and the Maid Trade screen. |

### Wandering Maid

TOML section: `[wandering_maid]`

| Key | Default | Description |
| --- | --- | --- |
| `defaultEnabled` | `true` | Default personal toggle for players who never changed it. |
| `spawnIntervalMinutes` | `5` | Minutes between spawn attempts per player (0–60). `0` disables automatic events; the command still works. |
| `spawnChancePercent` | `25` | Chance per attempt, per player (0–100). |
| `maidsPerPlayer` | `1` | Maids spawned per event (1–10). |
| `maxAliveMaids` | `12` | Global cap for automatic events (1–1024). The command ignores it. |
| `rejectedCleanupEnabled` | `true` | Remove rejected maids from the world. |
| `rejectedCleanupDelaySeconds` | `30` | Delay before a rejected maid is removed (0–600). |
| `adoptGiftEnabled` | `true` | Give a random gift when a wandering maid is adopted. |
| `deathDropEnabled` | `true` | Drop random items when a player kills a wandering maid. |
| `dropBlacklist` | `[]` | Item IDs excluded from gifts and death drops, for example `minecraft:bedrock`; takes precedence over the whitelist. |
| `dropWhitelist` | `[]` | If non-empty, only these item IDs are used for gifts and death drops. Empty means all items that are not blacklisted. |

### Trader Maid Trading

TOML section: `[wandering_trader_maid_trade]`

| Key | Default | Description |
| --- | --- | --- |
| `stockWeight` | `100` | `0` disables maid stock; any value above 0 makes newly initialized traders carry 1–3 maids. |
| `priceMin` | `1` | Minimum emerald price (1–999). |
| `priceMax` | `16` | Maximum emerald price (1–999). If lower than `priceMin`, it is clamped to `priceMin`. |
| `freeMaidOnTraderDeath` | `true` | Release stock maids as ownerless maids when the trader dies; `false` deletes them. |
| `restockTimes` | `1` | Maximum restocks after selling out (0–99). `0` disables restocking. |

### Sale Rewards

| Key | Default | Description |
| --- | --- | --- |
| `sellRewardMainItem` | `touhou_little_maid:favorability_tool_full` | Main reward item ID. Leave blank or use an invalid ID to disable it. |
| `sellRewardMainCount` | `1` | Main reward count (1–64). |
| `sellRewardMainOnlyIfMissing` | `true` | Only give the main reward if the player does not already have that item. |
| `sellRewardExtraItems` | `minecraft:netherite_ingot;1;3`, `minecraft:enchanted_golden_apple;3;8` | Extra rewards, one per entry, in `item_id;min;max` format. Reversed min/max are swapped; both 0 skips the entry; invalid entries are ignored. |

The sale reward settings are under the nested `wandering_trader_maid_trade.wandering_give_things_config` section.

## 🧩 Compatibility Notice: Touhou Little Maid: Love & Loathe

This mod does **not** guarantee full compatibility with **Touhou Little Maid: Love & Loathe**.

Both mods provide their own config screens with feature toggles for maid behavior, spawning, and interaction, but these toggles are **independent and unaware of each other** — there is no auto-sync or priority handling. Enabling both at the same time may cause overlapping features, behavioral conflicts, or unexpected results.

**Please choose for yourself which mod's features to enable in each config screen.** You assume the risk of mixing them. If issues occur, try disabling one mod's toggles first to isolate the cause.

## 📜 License

- Assets and Code: [MIT License](https://mit-license.org/)

## 🙏 Authors

- Programmer: Steven23334
- Original Programmer: JumDa5he

---

# 车万女仆：流浪女仆与女仆交易

[![可在 GitHub 上获取](https://wsrv.nl/?url=https%3A%2F%2Fcdn.jsdelivr.net%2Fnpm%2F%40intergrav%2Fdevins-badges%403%2Fassets%2Fcozy%2Favailable%2Fgithub_vector.svg&n=-1)](https://github.com/Steven23334/Wandering_Maid)
[![适用于车万女仆](https://cdn.modrinth.com/data/cached_images/ea5dc160571134bd0ea89ac52075b542fb331e46_0.webp)](https://modrinth.com/project/R0bDWFAW)

这是为 **车万女仆 (Touhou Little Maid)** 开发的 NeoForge 1.21.1 拓展。它为世界带来主动寻找你的流浪女仆、带着女仆来售卖的流浪商人，以及一本用于管理个人流浪女仆开关与皮肤池的可合成书籍。
（此模组为[车万女仆：爱憎分明](https://www.curseforge.com/minecraft/mc-mods/touhou-little-maid-love-loathe)的衍生品。）

## ✨ 主要功能

### 流浪女仆

- 主世界中可能在你附近 12~32 格处出现一位流浪女仆，优先出现在你面前的方向，并带有 60 秒发光效果，方便你发现她。
- 她会主动走到你面前坐下并等待答复。只有她生成时锁定的玩家才能与她交互。
- 右键她打开请求界面：
  - **接受** —— 按车万女仆的驯服流程收留她（仍受女仆数量上限限制），并获得 2 件随机赠礼。
  - **拒绝** —— 她会离开。等待期间如果你走远，她会再次追上并等待，最多重复 3 次；之后她会永久离开。
- 被拒绝的流浪女仆会按配置延迟清除（默认 30 秒），不会长期堆积在世界中。
- 如果在收留前杀死她，她会额外掉落 3~5 件随机物品。
- 收留赠礼与死亡掉落有各自独立的开关，并共用同一份物品黑名单/白名单。
- 每名玩家有独立的触发计时器和个人开关。个人开关在流浪女仆之书中调整；从未修改过的玩家使用服务端的 `defaultEnabled` 默认值。

### 流浪商人女仆交易

- 开启商人女仆交易后，流浪商人会额外出售 4 种商品：**流浪女仆之书**（2 绿宝石）、**神龛**（5~6 绿宝石）、**魂符**（1 绿宝石）、**绀珠之药**（25 绿宝石，无法破坏）。
- 开启待售生成后，商人附近会拴着 1~3 名待售女仆。
- 与商人交易时界面会出现 **女仆交易** 按钮，打开分页的双栏市场：
  - **待售女仆** —— 用绿宝石购买。买下的女仆会走到你身边坐下，并向新主人问好。创造模式可免费购买；仍受女仆数量上限限制。
  - **我的女仆** —— 出售 32 格内属于自己的女仆。她的物品栏、饰品与装备会掉落在原地，随后由商人重新出售。你会获得配置的奖励（默认：背包中没有时给予 1 个好感度工具，1~3 个下界合金锭，3~8 个附魔金苹果）。
- 买空最后一名待售女仆后，商人可以按配置补货有限次数（默认 1 次），每次补充 1~3 名女仆。
- 商人死亡时，默认将待售女仆释放为无主自由女仆（可配置为直接删除）。若商人未死亡而是消失，待售女仆会被移除。

### 流浪女仆之书与皮肤池

- 使用 **书与笔 + 绿宝石** 无序合成。开启商人女仆交易后，流浪商人也会以 2 绿宝石出售。
- 使用后打开皮肤池界面：点击模型即可加入/移出皮肤池，可预览模型，并显示已选数量。关闭界面时自动保存，按玩家分别存储。
- 流浪女仆会从目标玩家的皮肤池中随机抽取外观；商人的待售女仆则使用首次初始化该商人的玩家的皮肤池。若皮肤池为空，则使用默认模型。
- 界面上的复选框可切换你的个人流浪女仆事件开关；未修改过该开关的玩家使用服务端 `defaultEnabled` 默认值。

## 🎯 模组用途

- 提供一种更自然、事件驱动的女仆获取方式，不再局限于合成或繁殖。
- 让玩家自由控制流浪女仆与待售女仆使用哪些模型。
- 为整合包作者提供可配置的生成概率、价格、掉落、补货行为与奖励物品。
- 增加女仆玩法的趣味性，也为服务器经济提供新的交互点。

## ⚠️ 前置要求

- **Minecraft**：1.21.1
- **NeoForge**：21.1.209 或更高
- **车万女仆 (Touhou Little Maid)**：必需（基于 1.5.3 开发）—— [CurseForge](https://www.curseforge.com/minecraft/mc-mods/touhou-little-maid)
- **Steven's Mod API**：1.0.0 或更高，必需且未打包在本模组中 —— [GitHub](https://github.com/Steven23334/Steven_Mod_API)

## 🔧 安装方法

1. 安装上方列出的前置模组与 NeoForge 版本。
2. 将 `.jar` 文件放入 `.minecraft/mods` 文件夹。
3. 启动游戏即可。

## 🎮 指令

需要权限等级 2：

```
/tlm_wandering_maid wanderingmaid spawn
/tlm_wandering_maid wanderingmaid spawn <player>
```

手动为指定玩家（或自己）触发一次流浪女仆事件。该指令不受流浪女仆总开关、个人开关、触发间隔/概率和全服存活上限限制，但仍要求目标处于主世界。

## ⚙️ 配置

服务端配置文件：专用服务器为 `config/tlm_wandering_maid-Config.toml`；单人游戏中位于存档的 `serverconfig` 目录。也可以通过游戏内的模组配置界面打开（如可用）。

### 总开关

TOML 段：`[master_switches]`

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `master_switches.wanderingMaidEnabled` | `true` | 是否自动触发流浪女仆事件。关闭后不影响已存在的流浪女仆，也不影响生成指令。 |
| `master_switches.wanderingTraderMaidTradeEnabled` | `true` | 是否生成待售女仆、添加商人额外商品并启用女仆交易界面。 |

### 流浪女仆

TOML 段：`[wandering_maid]`

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `defaultEnabled` | `true` | 从未修改过个人开关的玩家所使用的默认值。 |
| `spawnIntervalMinutes` | `5` | 每名玩家两次触发尝试的间隔分钟数（0~60）。`0` 关闭自动触发，指令仍可用。 |
| `spawnChancePercent` | `25` | 每次尝试的生成概率（0~100），按玩家独立判定。 |
| `maidsPerPlayer` | `1` | 每次事件生成的女仆数量（1~10）。 |
| `maxAliveMaids` | `12` | 自动事件的全服同时存活上限（1~1024）。指令不受此限制。 |
| `rejectedCleanupEnabled` | `true` | 是否自动清除被拒绝的流浪女仆。 |
| `rejectedCleanupDelaySeconds` | `30` | 被拒绝后多久清除（0~600 秒）。 |
| `adoptGiftEnabled` | `true` | 收留流浪女仆时是否给予随机赠礼。 |
| `deathDropEnabled` | `true` | 玩家杀死流浪女仆时是否额外掉落随机物品。 |
| `dropBlacklist` | `[]` | 赠礼与死亡掉落的物品黑名单，例如 `minecraft:bedrock`；优先于白名单。 |
| `dropWhitelist` | `[]` | 非空时，赠礼与死亡掉落只从中抽取物品；留空表示允许所有非黑名单物品。 |

### 商人女仆交易

TOML 段：`[wandering_trader_maid_trade]`

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `stockWeight` | `100` | `0` 关闭待售女仆生成；大于 0 时，新初始化的商人携带 1~3 名待售女仆。 |
| `priceMin` | `1` | 最低绿宝石价格（1~999）。 |
| `priceMax` | `16` | 最高绿宝石价格（1~999）。低于最低价时会按最低价处理。 |
| `freeMaidOnTraderDeath` | `true` | 商人死亡时是否把待售女仆释放为无主女仆；`false` 则直接删除。 |
| `restockTimes` | `1` | 卖空后最多补货次数（0~99）。`0` 表示不补货。 |

### 卖出女仆奖励

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `sellRewardMainItem` | `touhou_little_maid:favorability_tool_full` | 主奖励物品 ID。留空或填写无效 ID 时不发放。 |
| `sellRewardMainCount` | `1` | 主奖励数量（1~64）。 |
| `sellRewardMainOnlyIfMissing` | `true` | 仅在玩家背包中没有该物品时发放主奖励。 |
| `sellRewardExtraItems` | `minecraft:netherite_ingot;1;3`、`minecraft:enchanted_golden_apple;3;8` | 额外奖励列表，每项格式为 `物品ID;最少数量;最多数量`。最小/最大写反会自动交换；两者都为 0 时跳过该项；无效项会被忽略。 |

卖出奖励相关配置位于嵌套的 `wandering_trader_maid_trade.wandering_give_things_config` 段。

## 🧩 兼容性提示：Touhou Little Maid: Love & Loathe

本模组**不保证**与 **Touhou Little Maid: Love & Loathe** 的完全兼容性。

两者的配置界面中均包含对女仆行为、生成与交互的功能开关，但**彼此独立、互不感知**，不存在自动同步或优先级协调。同时启用时可能出现功能重叠、行为冲突或预期之外的结果。

**请自行在双方配置界面中择优挑选要启用哪个模组的功能**，并自行承担混用带来的风险。若遇到异常，建议先分别关闭其中一个模组的功能开关进行排查。

## 📜 许可证

- 资产与代码：[MIT License](https://mit-license.org/)

## 🙏 作者

- 程序：Steven23334
- 原作者：JumDa5he
