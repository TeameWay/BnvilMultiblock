package com.teameway.bnvilmb.datagen.lang;

import dev.anvilcraft.lib.v2.registrum.providers.RegistrumLangProvider;

/**
 * 本模组的界面文本
 *
 * <p>键按前缀分组：{@code gui.bnvil_multiblock.controller.*} 是控制器状态、线程与配方信息，
 * {@code port_kind.*} 与 {@code port.*} 是端口名称与端口面板，{@code jei.*} 是 JEI 配方页，
 * {@code slot.count} 是物品槽标题，{@code structure.bnvil_multiblock.problem.*} 是结构成型失败的原因。</p>
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
        provider.add("gui.bnvil_multiblock.controller.thread.recipe", "Recipe: %s");
        provider.add("gui.bnvil_multiblock.controller.thread.parallel", "Parallel: %s");
        provider.add("gui.bnvil_multiblock.controller.thread.progress", "Progress: %s%%");
        provider.add("gui.bnvil_multiblock.controller.thread.ticks", "%s/%s t");
        provider.add("gui.bnvil_multiblock.controller.thread.seconds", "%s/%s s");
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
        provider.add("structure.bnvil_multiblock.problem.fixed", "Block at %s,%s,%s does not match the structure");
        provider.add("structure.bnvil_multiblock.problem.slot", "Block at %s,%s,%s is not a valid port or substitute");
        provider.add("structure.bnvil_multiblock.problem.port_not_allowed", "Port at %s,%s,%s is not allowed here");
        provider.add("structure.bnvil_multiblock.problem.port_claimed", "Port at %s,%s,%s belongs to another controller");
        provider.add("structure.bnvil_multiblock.problem.role_min", "Not enough %s: %s (need %s)");
        provider.add("structure.bnvil_multiblock.problem.role_max", "Too many %s: %s (max %s)");
        provider.add("structure.bnvil_multiblock.problem.unloaded", "Structure area is not fully loaded");
        provider.add("structure.bnvil_multiblock.problem.pattern", "Structure does not satisfy its role counts");
    }
}
