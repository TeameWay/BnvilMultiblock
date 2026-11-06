package com.teameway.bnvilmb.multiblock.machine;

import com.teameway.bnvilmb.recipe.CompiledRecipe;
import com.teameway.bnvilmb.recipe.MultiblockRecipeIndex;
import com.teameway.bnvilmb.recipe.TypeIndex;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/**
 * 机器线程上的一个任务
 *
 * <p>保存配方 ID、进度与并行倍数；配方按 ID 惰性找回，缓存绑定索引版本，重载后重新解析以避免执行旧数据。</p>
 */
public class MachineJob {
    private final Identifier recipeId;
    private final int batch;
    private int progress;
    private int cooldown;
    private @Nullable CompiledRecipe recipe;
    private long recipeVersion = -1L;
    private int duration;

    /**
     * 按配方 ID 创建任务，进度从指定值开始
     *
     * @param recipeId 配方 ID
     * @param batch 并行倍数，小于 1 时按 1 处理
     * @param progress 已进行的 tick 数，小于 0 时按 0 处理
     */
    public MachineJob(Identifier recipeId, int batch, int progress) {
        this.recipeId = recipeId;
        this.batch = Math.max(1, batch);
        this.progress = Math.max(0, progress);
    }

    /**
     * 用已解析的配方创建任务
     *
     * <p>耗时直接取 {@link CompiledRecipe#time()}，进度从 0 开始。</p>
     *
     * @param recipe 已解析的配方
     * @param batch 并行倍数
     */
    public MachineJob(CompiledRecipe recipe, int batch) {
        this(recipe.id(), batch, 0);
        this.recipe = recipe;
        this.recipeVersion = -1L;
        this.duration = recipe.time();
    }

    /**
     * @return 配方 ID，重载后按它重新查找配方
     */
    public Identifier recipeId() {
        return this.recipeId;
    }

    /**
     * @return 本任务的并行倍数，至少为 1
     */
    public int batch() {
        return this.batch;
    }

    /**
     * @return 已累计的 tick 数，冷却期间不增加
     */
    public int progress() {
        return this.progress;
    }

    /**
     * @return 最近一次解析得到的单周期耗时，从未解析时为 0
     */
    public int duration() {
        return this.duration;
    }

    /**
     * 按 ID 解析配方，索引版本变化时重新查找
     *
     * <p>命中后同时更新 {@link #duration}；索引里查不到该 ID 时清空缓存。</p>
     *
     * @param index 配方索引，{@code version()} 用于判断缓存是否过期
     * @param machine 机器类型
     * @return 配方，找不到返回 {@code null}
     */
    public @Nullable CompiledRecipe resolve(MultiblockRecipeIndex index, Identifier machine) {
        if (this.recipe != null && this.recipeVersion == index.version()) {
            return this.recipe;
        }
        TypeIndex type = index.type(machine);
        if (type == null) {
            this.recipe = null;
            return null;
        }
        for (CompiledRecipe candidate : type.recipes()) {
            if (candidate.id().equals(this.recipeId)) {
                this.recipe = candidate;
                this.recipeVersion = index.version();
                this.duration = candidate.time();
                return candidate;
            }
        }
        this.recipe = null;
        return null;
    }

    /**
     * 推进一个 tick
     *
     * <p>冷却未结束时只递减冷却并返回 {@code false}。</p>
     *
     * @param time 配方总时间
     * @return 进度是否已达到总时间
     */
    public boolean advance(int time) {
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }
        this.progress++;
        return this.progress >= time;
    }

    /**
     * 进入冷却，期间不推进进度
     *
     * <p>进度同时回退 1 tick，最低到 0。</p>
     *
     * @param ticks 冷却 tick 数
     */
    public void block(int ticks) {
        this.cooldown = ticks;
        this.progress = Math.max(0, this.progress - 1);
    }
}
