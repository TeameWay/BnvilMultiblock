package com.teameway.bnvilmb.datagen.lang;

import dev.anvilcraft.lib.v2.registrum.providers.RegistrumLangProvider;

/**
 * 本模组的界面文本
 *
 * <p>键按前缀分组：{@code gui.bnvil_multiblock.controller.*} 是控制器状态、线程与配方信息，
 * {@code port_kind.*} 与 {@code port.*} 是端口名称与端口面板，{@code jei.*} 是 JEI 配方页，
 * {@code slot.count} 是物品槽标题，{@code structure.bnvil_multiblock.error.*} 是结构成型失败的原因。</p>
 *
 * <p>文本里的 {@code %s} 在显示时由调用方填参；进度条目写作 {@code %s%%}，多出的那个百分号
 * 用来在结果里输出字面量 {@code %}。</p>
 */
public class BnvilGuiLangProvider {

    /**
     * 写入全部界面文本
     *
     * @param provider 接收这些键值对的 Registrum 语言 provider
     */
    public static void init(RegistrumLangProvider provider) {
        provider.add("gui.bnvil_multiblock.controller.state.unformed", "Structure not formed");
        provider.add("gui.bnvil_multiblock.controller.state.idle", "Idle");
        provider.add("gui.bnvil_multiblock.controller.state.working", "Working");
        provider.add("gui.bnvil_multiblock.controller.info.variants", "Variants: %s");
        provider.add("gui.bnvil_multiblock.controller.info.parallel", "Up to %s runs per recipe");
        provider.add("gui.bnvil_multiblock.controller.info.threads", "Up to %s recipes at once");
        provider.add("gui.bnvil_multiblock.controller.thread.title", "Thread #%s");
        provider.add("gui.bnvil_multiblock.controller.thread.parallel", "Parallel: %s");
        provider.add("gui.bnvil_multiblock.controller.thread.progress", "Progress: %s%%");
        provider.add("gui.bnvil_multiblock.controller.thread.time.ticks", "%s / %s t");
        provider.add("gui.bnvil_multiblock.controller.thread.time.seconds", "%s / %s s");
        provider.add("gui.bnvil_multiblock.controller.thread.none", "No thread");
        provider.add("gui.bnvil_multiblock.controller.recipe.hint", "Click a thread to view its recipe");
        provider.add("gui.bnvil_multiblock.controller.recipe.unavailable", "Recipe data not ready");
        provider.add("gui.bnvil_multiblock.controller.recipe.hint_rate", "Inputs and outputs are listed per cycle");
        provider.add("gui.bnvil_multiblock.controller.recipe.inputs", "Inputs");
        provider.add("gui.bnvil_multiblock.controller.recipe.outputs", "Outputs");
        provider.add("gui.bnvil_multiblock.controller.recipe.line_item", "%s × %s");
        provider.add("gui.bnvil_multiblock.controller.recipe.line_fluid", "%s × %s");
        provider.add("gui.bnvil_multiblock.controller.recipe.rate_item", "avg %s/t · %s/s");
        provider.add("gui.bnvil_multiblock.controller.recipe.rate_fluid", "avg %s mB/t · %s mB/s");
        provider.add("gui.bnvil_multiblock.port_kind.item_input", "Item Input Bus");
        provider.add("gui.bnvil_multiblock.port_kind.item_output", "Item Output Bus");
        provider.add("gui.bnvil_multiblock.port_kind.fluid_input", "Fluid Input Hatch");
        provider.add("gui.bnvil_multiblock.port_kind.fluid_output", "Fluid Output Hatch");
        provider.add("gui.bnvil_multiblock.port_kind.energy_input", "Energy Input");
        provider.add("gui.bnvil_multiblock.port_kind.energy_output", "Energy Output");
        provider.add("gui.bnvil_multiblock.port_kind.parallel", "Parallel Hatch");
        provider.add("gui.bnvil_multiblock.port_kind.thread", "Thread Hatch");
        provider.add("gui.bnvil_multiblock.port_panel.title", "Ports (%s)");
        provider.add("gui.bnvil_multiblock.port_panel.tier", "Tier: %s");
        provider.add("gui.bnvil_multiblock.port_panel.tooltip", "Shift + Left click to open the port");
        provider.add("gui.bnvil_multiblock.port_panel.toggle", "Toggle port list");
        provider.add("gui.bnvil_multiblock.port_panel.unknown", "Unknown port");
        provider.add("gui.bnvil_multiblock.port.slots", "Slots: %s");
        provider.add("gui.bnvil_multiblock.port.fluid", "Capacity: %s mB");
        provider.add("gui.bnvil_multiblock.port.energy", "%s / %s FE");
        provider.add("gui.bnvil_multiblock.port.threads", "Threads: +%s");
        provider.add("gui.bnvil_multiblock.port.parallels", "Parallel: +%s");
        provider.add("gui.bnvil_multiblock.port.fluid_total", "Fluid total:");
        provider.add("gui.bnvil_multiblock.port.fluid_empty", "Empty");
        provider.add("gui.bnvil_multiblock.energy.current", "Current: %s FE");
        provider.add("gui.bnvil_multiblock.energy.current.raw", "Current FE: %s");
        provider.add("gui.bnvil_multiblock.energy.capacity", "Capacity: %s FE");
        provider.add("gui.bnvil_multiblock.energy.percent", "Stored: %s%%");
        provider.add("gui.bnvil_multiblock.control.parallel", "Parallel");
        provider.add("gui.bnvil_multiblock.control.thread", "Threads");
        provider.add("gui.bnvil_multiblock.control.current.parallel", "Current parallel: %s");
        provider.add("gui.bnvil_multiblock.control.current.thread", "Current threads: %s");
        provider.add("gui.bnvil_multiblock.control.max", "Max: %s");
        provider.add("gui.bnvil_multiblock.control.double", "*2");
        provider.add("gui.bnvil_multiblock.control.half", "/2");
        provider.add("gui.bnvil_multiblock.control.to_max", "Increase to max");
        provider.add("gui.bnvil_multiblock.control.to_min", "Decrease to min");
        provider.add("gui.bnvil_multiblock.button.threads", "Threads");
        provider.add("gui.bnvil_multiblock.button.ports", "Ports");
        provider.add("gui.bnvil_multiblock.panel.threads", "Threads");
        provider.add("gui.bnvil_multiblock.panel.recipe", "Recipe #%s");
        provider.add("gui.bnvil_multiblock.panel.ports", "Ports");
        provider.add("gui.bnvil_multiblock.panel.controllers", "Controllers");
        provider.add("gui.bnvil_multiblock.panel.controller", "Controller");
        provider.add("gui.bnvil_multiblock.panel.controller.tooltip", "Shift + Left click to open the controller");
        provider.add("gui.bnvil_multiblock.panel.empty", "Nothing to show");
        provider.add("gui.bnvil_multiblock.jei.time", "Time: %ss");
        provider.add("gui.bnvil_multiblock.jei.energy_total", "Total: %s FE");
        provider.add("gui.bnvil_multiblock.jei.energy_rate", "Power: %s FE/t");
        provider.add("gui.bnvil_multiblock.jei.no_structure", "Structure preview unavailable");
        provider.add("gui.bnvil_multiblock.jei.required.exact", "Exactly %s");
        provider.add("gui.bnvil_multiblock.jei.required.min", "At least %s");
        provider.add("gui.bnvil_multiblock.jei.required.max", "At most %s");
        provider.add("gui.bnvil_multiblock.jei.required.range", "%s ~ %s");
        provider.add("gui.bnvil_multiblock.jei.layer.all", "ALL");
        provider.add("gui.bnvil_multiblock.jei.layer.count", "H%s");
        provider.add("gui.bnvil_multiblock.jei.highlight.on", "Highlight: On");
        provider.add("gui.bnvil_multiblock.jei.highlight.off", "Highlight: Off");
        provider.add("gui.bnvil_multiblock.jei.multiblock_info", "Multiblock Structure");
        provider.add("gui.bnvil_multiblock.jei.chance", "Chance: %s");
        provider.add("gui.bnvil_multiblock.slot.count", "Count: %s");
        provider.add("structure.bnvil_multiblock.error.block", "Block at %s does not match %s");
        provider.add("structure.bnvil_multiblock.error.port", "Block at %s is not one of %s");
        provider.add("structure.bnvil_multiblock.error.shared", "Port at %s is not shareable");
        provider.add("structure.bnvil_multiblock.error.count", "Not enough %s: %s (need %s)");
        provider.add("structure.bnvil_multiblock.error.max", "Too many %s: %s (max %s)");
        provider.add("structure.bnvil_multiblock.error.unloaded", "Structure area is not fully loaded");
        provider.add("structure.bnvil_multiblock.error.controller", "Controller at %s is missing or invalid");
        provider.add("structure.bnvil_multiblock.error.pattern", "Structure does not satisfy its pattern: %s");
    }

    /**
     * 写入中文翻译
     *
     * @param provider 中文语言 provider
     */
    public static void initZhCn(ZhCnLangProvider provider) {
        provider.add("block.bnvil_multiblock.energy_input_port", "能源输入仓");
        provider.add("block.bnvil_multiblock.energy_output_port", "能源输出仓");
        provider.add("block.bnvil_multiblock.fluid_input_port", "输入仓");
        provider.add("block.bnvil_multiblock.fluid_output_port", "输出仓");
        provider.add("block.bnvil_multiblock.item_input_port", "输入总线");
        provider.add("block.bnvil_multiblock.item_output_port", "输出总线");
        provider.add("block.bnvil_multiblock.parallel_port", "并行仓");
        provider.add("block.bnvil_multiblock.simple_controller", "简易控制器");
        provider.add("block.bnvil_multiblock.thread_port", "线程仓");
        provider.add("gui.bnvil_multiblock.button.ports", "端口");
        provider.add("gui.bnvil_multiblock.button.threads", "线程");
        provider.add("gui.bnvil_multiblock.control.current.parallel", "当前并行数: %s");
        provider.add("gui.bnvil_multiblock.control.current.thread", "当前线程数: %s");
        provider.add("gui.bnvil_multiblock.control.double", "*2");
        provider.add("gui.bnvil_multiblock.control.half", "/2");
        provider.add("gui.bnvil_multiblock.control.max", "最多: %s");
        provider.add("gui.bnvil_multiblock.control.parallel", "并行");
        provider.add("gui.bnvil_multiblock.control.thread", "线程");
        provider.add("gui.bnvil_multiblock.control.to_max", "增加至上限");
        provider.add("gui.bnvil_multiblock.control.to_min", "减至下限");
        provider.add("gui.bnvil_multiblock.controller.info.parallel", "每种配方至多 %s 次");
        provider.add("gui.bnvil_multiblock.controller.info.threads", "同时处理至多 %s 种不同配方");
        provider.add("gui.bnvil_multiblock.controller.info.variants", "形态：%s");
        provider.add("gui.bnvil_multiblock.controller.recipe.hint", "点击右侧线程查看配方");
        provider.add("gui.bnvil_multiblock.controller.recipe.hint_rate", "输入与产出按单周期列出");
        provider.add("gui.bnvil_multiblock.controller.recipe.inputs", "输入");
        provider.add("gui.bnvil_multiblock.controller.recipe.line_fluid", "%s × %s");
        provider.add("gui.bnvil_multiblock.controller.recipe.line_item", "%s × %s");
        provider.add("gui.bnvil_multiblock.controller.recipe.outputs", "产出");
        provider.add("gui.bnvil_multiblock.controller.recipe.rate_fluid", "平均 %s mB/t · %s mB/s");
        provider.add("gui.bnvil_multiblock.controller.recipe.rate_item", "平均 %s/t · %s/s");
        provider.add("gui.bnvil_multiblock.controller.recipe.unavailable", "配方数据未就绪");
        provider.add("gui.bnvil_multiblock.controller.state.idle", "空闲");
        provider.add("gui.bnvil_multiblock.controller.state.unformed", "结构未成型");
        provider.add("gui.bnvil_multiblock.controller.state.working", "工作中");
        provider.add("gui.bnvil_multiblock.controller.thread.none", "无线程");
        provider.add("gui.bnvil_multiblock.controller.thread.parallel", "并行：%s");
        provider.add("gui.bnvil_multiblock.controller.thread.progress", "进度：%s%%");
        provider.add("gui.bnvil_multiblock.controller.thread.time.seconds", "%s/%s 秒");
        provider.add("gui.bnvil_multiblock.controller.thread.time.ticks", "%s/%s tick");
        provider.add("gui.bnvil_multiblock.controller.thread.title", "线程 #%s");
        provider.add("gui.bnvil_multiblock.energy.capacity", "能够存储：%s FE");
        provider.add("gui.bnvil_multiblock.energy.current", "当前能源：%s FE");
        provider.add("gui.bnvil_multiblock.energy.current.raw", "当前FE: %s");
        provider.add("gui.bnvil_multiblock.energy.percent", "储量：%s%%");
        provider.add("gui.bnvil_multiblock.jei.chance", "概率：%s");
        provider.add("gui.bnvil_multiblock.jei.energy_rate", "耗能功率：%s FE/t");
        provider.add("gui.bnvil_multiblock.jei.energy_total", "总计：%s FE");
        provider.add("gui.bnvil_multiblock.jei.highlight.off", "高亮：关");
        provider.add("gui.bnvil_multiblock.jei.highlight.on", "高亮：开");
        provider.add("gui.bnvil_multiblock.jei.layer.all", "全部");
        provider.add("gui.bnvil_multiblock.jei.layer.count", "H%s");
        provider.add("gui.bnvil_multiblock.jei.multiblock_info", "多方块结构");
        provider.add("gui.bnvil_multiblock.jei.no_structure", "结构预览不可用");
        provider.add("gui.bnvil_multiblock.jei.required.exact", "恰好 %s");
        provider.add("gui.bnvil_multiblock.jei.required.max", "至多 %s");
        provider.add("gui.bnvil_multiblock.jei.required.min", "至少 %s");
        provider.add("gui.bnvil_multiblock.jei.required.range", "%s ~ %s");
        provider.add("gui.bnvil_multiblock.jei.time", "耗时：%s 秒");
        provider.add("gui.bnvil_multiblock.panel.controller", "控制器");
        provider.add("gui.bnvil_multiblock.panel.controller.tooltip", "Shift + 左键点击打开控制器");
        provider.add("gui.bnvil_multiblock.panel.controllers", "控制器");
        provider.add("gui.bnvil_multiblock.panel.empty", "没有可显示的内容");
        provider.add("gui.bnvil_multiblock.panel.ports", "端口");
        provider.add("gui.bnvil_multiblock.panel.recipe", "配方 #%s");
        provider.add("gui.bnvil_multiblock.panel.threads", "线程");
        provider.add("gui.bnvil_multiblock.port.energy", "%s / %s FE");
        provider.add("gui.bnvil_multiblock.port.fluid", "容量：%s mB");
        provider.add("gui.bnvil_multiblock.port.fluid_empty", "空");
        provider.add("gui.bnvil_multiblock.port.fluid_total", "流体总量:");
        provider.add("gui.bnvil_multiblock.port.parallels", "并行：+%s");
        provider.add("gui.bnvil_multiblock.port.slots", "槽位：%s");
        provider.add("gui.bnvil_multiblock.port.threads", "线程：+%s");
        provider.add("gui.bnvil_multiblock.port_kind.energy_input", "能量输入");
        provider.add("gui.bnvil_multiblock.port_kind.energy_output", "能量输出");
        provider.add("gui.bnvil_multiblock.port_kind.fluid_input", "输入仓");
        provider.add("gui.bnvil_multiblock.port_kind.fluid_output", "输出仓");
        provider.add("gui.bnvil_multiblock.port_kind.item_input", "输入总线");
        provider.add("gui.bnvil_multiblock.port_kind.item_output", "输出总线");
        provider.add("gui.bnvil_multiblock.port_kind.parallel", "并行仓");
        provider.add("gui.bnvil_multiblock.port_kind.thread", "线程仓");
        provider.add("gui.bnvil_multiblock.port_panel.tier", "等级：%s");
        provider.add("gui.bnvil_multiblock.port_panel.title", "端口（%s）");
        provider.add("gui.bnvil_multiblock.port_panel.toggle", "开关端口列表");
        provider.add("gui.bnvil_multiblock.port_panel.tooltip", "Shift + 左键点击可打开端口界面");
        provider.add("gui.bnvil_multiblock.port_panel.unknown", "未知端口");
        provider.add("gui.bnvil_multiblock.slot.count", "数量：%s");
        provider.add("item.bnvil_multiblock.energy_input_port", "能源输入仓");
        provider.add("item.bnvil_multiblock.energy_output_port", "能源输出仓");
        provider.add("item.bnvil_multiblock.fluid_input_port", "输入仓");
        provider.add("item.bnvil_multiblock.fluid_output_port", "输出仓");
        provider.add("item.bnvil_multiblock.item_input_port", "输入总线");
        provider.add("item.bnvil_multiblock.item_output_port", "输出总线");
        provider.add("item.bnvil_multiblock.parallel_port", "并行仓");
        provider.add("item.bnvil_multiblock.simple_controller", "简易控制器");
        provider.add("item.bnvil_multiblock.thread_port", "线程仓");
        provider.add("itemGroup.bnvil_multiblock.multiblock", "BnvilMultiblock");
        provider.add("structure.bnvil_multiblock.error.block", "位置 %s 的方块与 %s 不符");
        provider.add("structure.bnvil_multiblock.error.controller", "位置 %s 的控制器缺失或无效");
        provider.add("structure.bnvil_multiblock.error.count", "%s 不足：%s（需要 %s）");
        provider.add("structure.bnvil_multiblock.error.max", "%s 过多：%s（最多 %s）");
        provider.add("structure.bnvil_multiblock.error.pattern", "结构不满足模板：%s");
        provider.add("structure.bnvil_multiblock.error.port", "位置 %s 的方块不属于 %s");
        provider.add("structure.bnvil_multiblock.error.shared", "位置 %s 的端口不可共享");
        provider.add("structure.bnvil_multiblock.error.unloaded", "结构区域未完全加载");
    }
}
