<div align="center">

# Immersive Eating

**让食物在第一人称中拥有专属模型与进食动画。**

[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-62B47A?style=flat-square)](https://www.minecraft.net/)
[![Forge](https://img.shields.io/badge/Forge-47.4.22-EA7F00?style=flat-square)](https://files.minecraftforge.net/)
[![Code License](https://img.shields.io/badge/Code-GPL--3.0--only-blue?style=flat-square)](LICENSE)
[![Assets License](https://img.shields.io/badge/Assets-CC%20BY--NC--ND%204.0-lightgrey?style=flat-square)](LICENSE-ASSETS)

[扩展](#扩展) |
[贡献者](#贡献者) |
[许可](#许可)

</div>

## 简介

Immersive Eating 是一个面向 Minecraft Forge 1.20.1 的第一人称进食动画模组，已与森罗物语（Kaleidoscope Cookery）进行单向联动。

它会为已配置的食物加载独立的 Blockbench/GeckoLib 模型、材质与动画；按下 `U` 后播放进食动画和关键帧音效。动画播放期间会锁定主手快捷栏，结束时使用原版重新装备动画自然地回到手持状态。

### 环境

- Minecraft `1.20.1`
- Forge `47.4.22`
- GeckoLib `4.8.2`
- 森罗物语（Kaleidoscope Cookery）`1.4.1-forge+mc1.20.1`（单向联动）
## 扩展
一个食物扩展由定义、模型、动画和材质组成：
```text
src/main/resources/assets/food/
├── definitions/
│   └── example_food.json
├── geo/
│   └── example_food.geo.json
├── animations/
│   └── example_food.animation.json
└── textures/item/
    └── example_food.png
```

`definitions/example_food.json` 示例：

```json
{
  "item": "examplemod:example_food",
  "invisible": ["exampelBone"],
  "sounds": {
    "动画音乐关键帧名": "food:eat"
  }
}
```
- `item`：要替换渲染的物品 ID。
- `invisible`：待机模型阶段隐藏的 bone 名称。隐藏父 bone 时，其全部子 bone 也会一同隐藏；进食动画播放期间会恢复显示。
- `sounds`：将动画音效关键帧名称映射到游戏声音 ID。
模型、动画、材质文件名必须与定义文件名一致。例如定义文件是 `example_food.json`，则应使用：
```text
geo/example_food.geo.json
animations/example_food.animation.json
textures/item/example_food.png
```
动画文件中的进食动画名称必须为 `eat`。动画结束时，在时间线加入 动画效果-指令-脚本:finished：
## 贡献者
### Programmer

- XianYue

### Art

- 可橙姐 K.C.J
- MLeaf_Ming (枫叶)
- Swiyds_Cold
- hmrrr
- 杨晨七
- Liz
### 以及全体热爱mc的玩家
## 许可

- 代码：[`GPL-3.0-only`](LICENSE)
- `geo`、`animations` 与 `textures` 资源：[`CC BY-NC-ND 4.0`](LICENSE-ASSETS)

详见 [`NOTICE`](NOTICE)。
