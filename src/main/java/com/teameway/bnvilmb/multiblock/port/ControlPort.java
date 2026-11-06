package com.teameway.bnvilmb.multiblock.port;

/**
 * 可调节提供量的控制端口
 *
 * <p>并行仓与线程仓共用这个接口。{@link #provided()} 是当前实际提供的增量，
 * 上限由端口等级决定；界面上的加减按钮通过它读写。</p>
 */
public interface ControlPort extends MultiblockPort {
    /**
     * @return 当前提供的增量，范围是 0 到 {@link #maxProvided()}
     */
    int provided();

    /**
     * @return 等级允许的最大增量
     */
    int maxProvided();

    /**
     * 修改当前提供的增量，实现方负责夹取范围并同步
     *
     * @param value 目标值
     */
    void setProvided(int value);
}
