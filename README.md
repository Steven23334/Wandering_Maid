# TLM-Wandering-Maid

TLM Wandering Maid
# Touhou Little Maid: Wandering Maid and Maid Trading

[![Available on GitHub](https://wsrv.nl/?url=https%3A%2F%2Fcdn.jsdelivr.net%2Fnpm%2F%40intergrav%2Fdevins-badges%403%2Fassets%2Fcozy%2Favailable%2Fgithub_vector.svg&n=-1)](https://github.com/Steven23334/Wandering_Maid)
[![Available for Touhou Little Maid](https://cdn.modrinth.com/data/cached_images/ea5dc160571134bd0ea89ac52075b542fb331e46_0.webp)](https://modrinth.com/project/R0bDWFAW)

This is an addon for the **Touhou Little Maid** mod. It introduces two new ways to encounter maids: wandering maids who seek you out in the wild, and wandering traders who bring maids for sale.

## 📦 New Content

- **Wandering Maid**  
  A maid may appear near you in the Overworld, approach you on her own, sit down, and wait for your answer. Right-click her to open a request screen — **Accept** to adopt her with a cake and receive a random gift, or **Reject** to let her leave. Repeated rejections make her disappear permanently. Only the player she was bound to can interact with her.

- **Wandering Trader Maid Trading**  
  Wandering traders may bring 1–3 maids for sale. A **Maid Trade** button appears on the merchant screen, opening a two-column market: buy for-sale maids with emeralds, or sell your own nearby maids to the trader for favorability tools, netherite ingots, and enchanted golden apples. Traders can restock a limited number of times.

- **Wandering Maid Book & Skin Pool**  
  A new craftable item (Book and Quill + Emerald) that opens the skin pool screen. Wandering and for-sale maids randomly pick their appearance from your selected pool. If the pool is empty, the default model is used.

## 🎯 Purpose

- Adds a more organic, event-driven way to obtain maids beyond crafting or breeding.
- Gives players direct control over which models wandering and trading maids use.
- Provides modpack authors with configurable spawn rates, prices, drops, and restock behavior.
- Adds more fun and strategy to maid gameplay and server economies.

## 🔧 Installation

1. Make sure the **Touhou Little Maid** mod is installed.
2. Place the `.jar` file of this mod into the `.minecraft/mods` folder.
3. Launch the game.

## 🎮 Commands

Requires permission level 2:

```
/tlm_wandering_maid wanderingmaid spawn
/tlm_wandering_maid wanderingmaid spawn <player>
```

Manually triggers a wandering maid event for the target player (or yourself).

## ⚙️ Configuration

Server-side config: `config/tlm_wandering_maid-Config.toml`

- **Master Switches**: toggle wandering maids and trader maid trading independently.
- **Wandering Maid**: spawn interval, spawn chance, maids per player, drop blacklist/whitelist.
- **Trader Maid Trading**: stock weight, min/max price, restock times, release-on-death behavior.

## ⚠️ Requirements

- **Minecraft Versions**: 1.21.1
- **Required Mod**: [Touhou Little Maid](https://www.curseforge.com/minecraft/mc-mods/touhou-little-maid)

## 📜 License

- Assets and Code: [MIT License](https://mit-license.org/)

## 🙏 Authors

- Programmer: Steven23334

---

# 车万女仆：流浪女仆与女仆交易

[![可在 GitHub 上获取](https://wsrv.nl/?url=https%3A%2F%2Fcdn.jsdelivr.net%2Fnpm%2F%40intergrav%2Fdevins-badges%403%2Fassets%2Fcozy%2Favailable%2Fgithub_vector.svg&n=-1)](https://github.com/Steven23334/Wandering_Maid)
[![适用于车万女仆](https://cdn.modrinth.com/data/cached_images/ea5dc160571134bd0ea89ac52075b542fb331e46_0.webp)](https://modrinth.com/project/R0bDWFAW)

这是一个为 **车万女仆 (Touhou Little Maid)** 模组开发的拓展，为世界带来两种新的女仆邂逅方式：在野外主动寻找你的流浪女仆，以及带着女仆前来售卖的流浪商人。

## 📦 新增内容

- **流浪女仆**  
  主世界中可能在你附近出现一位流浪女仆，她会主动靠近你，在你面前坐下并等待答复。右键她打开请求界面——选择 **接受** 可以用蛋糕收留她并获得随机赠礼，选择 **拒绝** 则会让她离开；多次拒绝后她会永久消失。只有她生成时锁定的玩家才能与她交互。

- **流浪商人女仆交易**  
  流浪商人出现时可能带着 1~3 名待售女仆。与商人交易时界面会出现 **女仆交易** 按钮，打开双栏市场：用绿宝石购买待售女仆，或把附近属于自己的女仆卖给商人，换取好感工具、下界合金锭与附魔金苹果。商人可以按配置补货若干次。

- **流浪女仆之书与皮肤池**  
  新增可合成物品（书与笔 + 绿宝石），使用后打开皮肤池界面。流浪女仆与待售女仆的外观会从你选中的皮肤池中随机抽取；若皮肤池为空，则回退到默认模型。

## 🎯 模组用途

- 提供一种更自然、事件驱动的女仆获取方式，不再局限于合成或繁殖。
- 让玩家自由控制流浪女仆与待售女仆使用哪些模型。
- 为整合包作者提供可配置的生成概率、价格、掉落与补货行为。
- 增加女仆玩法的趣味性，也为服务器经济提供新的交互点。

## 🔧 安装方法

1. 确保已安装 **车万女仆 (Touhou Little Maid)** 模组。
2. 将本模组的 `.jar` 文件放入 `.minecraft/mods` 文件夹。
3. 启动游戏即可。

## 🎮 指令

需要权限等级 2：

```
/tlm_wandering_maid wanderingmaid spawn
/tlm_wandering_maid wanderingmaid spawn <player>
```

手动为指定玩家（或自己）触发一次流浪女仆事件。

## ⚙️ 配置

服务端配置文件：`config/tlm_wandering_maid-Config.toml`

- **总开关**：可分别开关流浪女仆与流浪商人女仆交易。
- **流浪女仆**：触发间隔、生成概率、每名玩家生成数量、掉落黑白名单。
- **商人女仆交易**：待售女仆生成权重、最低/最高价格、补货次数、死亡时是否释放待售女仆。

## ⚠️ 前置要求

- **Minecraft 版本**：1.21.1
- **必需模组**：[车万女仆 (Touhou Little Maid)](https://www.curseforge.com/minecraft/mc-mods/touhou-little-maid)

## 📜 许可证

- 资产与代码:[MIT License](https://mit-license.org/)

## 🙏 作者

- 程序：Steven23334