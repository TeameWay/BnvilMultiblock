# BnvilMultiblock

> 您暂时不应该把此模组加入您的整合包。
> 此模组还处于早期开发阶段，大部分功能由 AI 生成。
> 作者虽然经过简单测试，确认没有崩溃问题 (复杂情景还没测)(其他问题有没有也不知道)。
> 在完全测试完成前，仅作为 vibe/wish coding 产物。
> 总之爆了自己解决，总之爆了自己解决，总之爆了自己解决。

NeoForge 多方块机器框架，从 [NoWay-Core](https://github.com/TeameWay/NoWay-Core) 中拆分而来，可作为独立模组安装，也可作为前置被其他模组依赖。

## 功能

- **结构定义走数据包**：结构形状由 `bnvil_multiblock:structure_patterns` 数据包注册表承载；网格里的槽位只声明允许的角色与全局数量，端口位置可以互换。
- **控制器 + 端口**：控制器方块负责结构生命周期与运行，可配置为无朝向或水平四向；端口分为物品总线、流体仓、能量端口、并行仓与线程仓，容量按等级决定。端口有归属，一个端口只属于一台控制器。
- **配方索引**：服务端与客户端各自维护 `MultiblockRecipeIndex` 快照，按机器类型与形态做位图筛选，配方重载后整表替换。
- **并行与多线程**：一个控制器可同时跑多条配方，每条线程按并行倍数结算，跨线程用预留账本避免重复占用同一批材料。
- **界面与 JEI**：控制器与端口都有 LDLib2 界面；JEI 按机器类型注册配方分类，并带结构预览。

## 开发

### 加一台新机器

1. 在 `init/multiblock` 注册控制器方块、方块实体、控制器、机器声明与机器类型。
2. 在 `BnvilMultiblockDefinitions` 里用 `StructurePattern.builder()` 声明网格：`layer(...)` 写层，`where(...)` 写固定方块，`slot(...)` 写槽位，`role(...)` 写角色与数量。
3. 运行 `./gradlew runData` 生成 `data/bnvil_multiblock/bnvil_multiblock/structure_patterns/<name>.json`。
4. 在 `data/bnvil_multiblock/tags/bnvil_multiblock/structure_patterns/<name>.json` 里登记结构 ID；`cooled` 这类形态标签也放在这里。
5. 在 `data/bnvil_multiblock/recipe/` 下写 `bnvil_multiblock:multiblock` 配方，`machine` 字段填机器类型 ID。

槽位示例：

```json
{
  "grid": [["AAA", "AAA", "AAA"], ["$B$", "B0B", "$B$"]],
  "mapping": {
    "0": { "blocks": "bnvil_multiblock:simple_controller" },
    "A": { "blocks": "minecraft:iron_block" },
    "B": { "blocks": "minecraft:gold_block" }
  },
  "slots": {
    "$": { "roles": ["item_input", "item_output", "fluid_input", "parallel"] }
  },
  "roles": {
    "item_input": { "port_kinds": ["item_input"], "min": 0, "max": 2 },
    "item_output": { "port_kinds": ["item_output"], "min": 1 },
    "fluid_input": { "port_kinds": ["fluid_input"], "min": 1, "max": 2 },
    "parallel": {
      "port_kinds": ["parallel"],
      "substitutes": [
        { "predicate": { "blocks": "minecraft:gold_block" }, "parallels": 0 },
        { "predicate": { "blocks": "minecraft:iron_block" }, "parallels": 0 }
      ]
    }
  }
}
```

> 旧版本使用 `anvillib:definitions` 的固定坐标格式；本版本已替换为 `bnvil_multiblock:structure_patterns`。旧存档中的 anvillib 多方块状态不再兼容，请使用新世界或自行清理对应 SavedData。

## 测试

- `src/test/java` 放纯 JUnit 测试，运行 `./gradlew test`。
- GameTest 在 `src/main/java/.../gametest`，默认不注册；只有带 `-Dbnvil_multiblock.tests=true` 时才注册。IDEA 的 `GameTestServer` 运行配置已经设置该属性。
- 普通客户端不会注册测试实例，避免登录时同步 `minecraft:test_instance` 报 `Direct holder` 错误。

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

特别感谢[铁砧工艺开发组](https://github.com/Anvil-Dev)和[古镇天](https://github.com/Gu-ZT)，他们为此项目提供了依赖支持以及 AI 支持。
