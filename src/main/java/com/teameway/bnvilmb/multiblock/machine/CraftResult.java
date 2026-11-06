package com.teameway.bnvilmb.multiblock.machine;

/**
 * 配方执行结果
 *
 * <p>区分材料不足与输出堵塞，机器状态机据此决定重新搜索配方还是原地等待。
 * 输出堵塞与执行失败的冷却时长见 {@code MachineRuntime} 里的常量。</p>
 */
public enum CraftResult {
    /**
     * 输入与产出都完成，事务已提交
     */
    SUCCESS,
    /**
     * 输入在事务期间被别处拿走，需要重新匹配
     */
    MISSING_INPUT,
    /**
     * 输出池放不下，机器应进入堵塞状态
     */
    OUTPUT_BLOCKED,
    /**
     * 执行过程抛出异常，机器进入较长冷却
     */
    FAILED
}
