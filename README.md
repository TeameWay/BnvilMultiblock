# BnvilMultiblock

> 您暂时不应该把此模组加入您的整合包。
> 此模组还处于早期开发阶段，大部分功能由 AI 生成。
> 作者虽然经过简单测试，确认没有崩溃问题 (复杂情景还没测)(其他问题有没有也不知道)。
> 在完全测试完成前，仅作为 vibe/wish coding 产物。
> 总之爆了自己解决，总之爆了自己解决，总之爆了自己解决。

NeoForge 多方块机器框架，从 [NoWay-Core](https://github.com/TeameWay/NoWay-Core) 中拆分而来，可作为独立模组安装，也可作为前置被其他模组依赖。

## 功能

- **结构定义走数据包**：结构形状由 anvillib 的 `anvillib:definitions` 数据包注册表承载，加机器只需加 JSON 与 tag，无需写代码。
- **控制器 + 端口**：控制器方块负责成型判定与运行；端口分为物品总线、流体仓、能量端口、并行仓与线程仓，容量按等级决定。
- **配方索引**：服务端与客户端各自维护 `MultiblockRecipeIndex` 快照，按机器类型与形态做位图筛选，配方重载后整表替换。
- **并行与多线程**：一个控制器可同时跑多条配方，每条线程按并行倍数结算，跨线程用预留账本避免重复占用同一批材料。
- **界面与 JEI**：控制器与端口都有 LDLib2 界面；JEI 按机器类型注册配方分类，并带结构预览。

## 开发

### 加一台新机器

1. 在 `init/multiblock` 注册控制器方块、方块实体、控制器、机器声明与机器类型。
2. 在 `BnvilMultiblockDefinitions` 里用 `MultiblockDefinition.builder()` 声明形状。
3. 运行 `./gradlew runData` 生成 `data/bnvil_multiblock/anvillib/definitions/<name>.json`。
4. 在 `data/bnvil_multiblock/tags/anvillib/definitions/<name>.json` 里登记定义 ID。
5. 在 `data/bnvil_multiblock/recipe/` 下写 `bnvil_multiblock:multiblock` 配方，`machine` 字段填机器类型 ID。

## 依赖

| 依赖                                              | 用途                                                 | 说明          |
|---------------------------------------------------|------------------------------------------------------|---------------|
| [AnvilLib](https://github.com/Anvil-Dev/AnvilLib) | `AbstractRegistrum` 注册体系、多方块定义与控制器 API | 必需，MPL-2.0 |
| [LDLib2](https://github.com/Low-Drag-MC/LDLib2)   | 界面框架                                             | 必需          |
| [JEI](https://github.com/mezz/JustEnoughItems)    | 配方查看                                             | 可选          |

## 许可证

- 代码：[GPL-3.0](LICENSE)
- 基于 NeoForged MDK 模板生成的部分：[MIT](TEMPLATE_LICENSE.txt)

## 致谢

本项目的多方块实现最初写在 [NoWay-Core](https://github.com/TeameWay/NoWay-Core) 中，现拆分至此仓库独立维护。
