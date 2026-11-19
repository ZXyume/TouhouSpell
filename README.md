# [TS] 东方法术：弹幕（Touhou Spell: Danmaku）

**东方法术：弹幕**（Touhou Spell: Danmaku）是一个面向 **Minecraft 1.20.1 / Forge 47** 的东方弹幕模组：结合 Iron's Spells 'n Spellbooks 的法术框架与 Youkais' Homecoming 的弹幕机制，提供以「万花币」为媒介的弹幕战斗体验。

> 本仓库是**源码仓库**（源码由发布版 jar 反编译后重建，仍使用 SRG 名），构建方式见下文「如何构建」。
> 可玩用的 jar 请在右侧 **Releases** 下载，不要把 `build/` 目录提交进仓库。

## 功能概览

| 内容 | 说明 |
|---|---|
| 万花币（法杖） | 三段攻击（Better Combat 数据驱动）、右键发射弹幕、Ctrl 打开弹幕配置界面 |
| 弹幕学学派 | 自定义学派 `touhou_spell:danmaku`，自带「弹幕学强度」属性 |
| 火力提升（power_up） | 按 P 点缩放增伤，发射时额外消耗 P 点 |
| 火力全开（full_firepower） | 增加子机数量与主炮伤害 |
| 飞行术（flymode） | 消耗法力的飞行 |
| 符卡发动（cast_spell_card） | 符卡饰品槽 + 冷却控制 |
| 弹幕学手册 | 3 个固定法术槽，禁止用法术槽升级扩容 |
| 女仆套（胸甲/护腿/靴子） | 用皮革套 + 时之浆 + 弹幕升级球在锻造台合成，提供弹幕学强度与法力加成 |

## 运行依赖

| Mod | 版本要求 | 用途 |
|---|---|---|
| Forge | `47.x`（1.20.1） | 加载器 |
| IRONS SPELLS 'N SPELLBOOKS | `1.20.1-3.16.3` ~ `<3.17` | 法术框架、法力、法术书 |
| Youkais' Homecoming（妖怪们的归家） | `>= 2.8.0` | 弹幕实体、擦弹、伤害体系 |
| Curios API | `>= 5.14.0` | 饰品槽（手册/符卡） |
| GeckoLib | `>= 4.8.4` | 女仆套盔甲模型 |
| Touhou Little Maid（车万女仆） | `>= 1.5.0` | P 点数值与 HUD |
| Better Combat（可选） | `>= 1.9.0` | 万花币的三段攻击动画与音效 |

## 如何构建

本工程**不是** Gradle/MDK 工程，因此必须对着**已安装的生产版 Forge 运行时**编译：

```powershell
# 在仓库根目录
powershell -ExecutionPolicy Bypass -File build.ps1
# 可选参数：-McDir <你的 .minecraft 路径> -Instance <实例文件夹名>
```

脚本会：收集实例 `libraries` + 所有已装 mod jar（含 jarjar 嵌套库）作为 classpath → `javac -proc:none` →
打包 `build/touhou_spell-<version>.jar`（版本号写在 `build.ps1` 顶部）。

> 也就是说**构建需要先装好上表全部依赖**；这是刻意为之（自用构建方式），不是通用 CI 工程。

## 版本记录

见 [CHANGES.md](CHANGES.md)。

## 许可

**All Rights Reserved**（自定义协议，非开源），详见 [LICENSE](LICENSE)。

- ✅ 允许：游玩；**原样**分发未修改的 jar（含付费整合包，但本模组不得作为该整合包的主要付费内容）；仅本地自用的修改；视频/直播/文章
- ❌ 需授权：单独售卖、转载到其它下载站、分发修改版或捆绑本模组的附属、使用名称图标冒充官方
- 📖 源码公开仅为阅读与审计参考；除上述允许项外不得复用或衍生

---

*Author: zxyume*
