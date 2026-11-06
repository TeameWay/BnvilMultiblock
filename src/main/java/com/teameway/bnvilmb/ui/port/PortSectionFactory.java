package com.teameway.bnvilmb.ui.port;

import com.teameway.bnvilmb.ui.shell.MainSection;

/**
 * 端口种类到 main 上半部分的工厂
 */
@FunctionalInterface
public interface PortSectionFactory {
    MainSection create(PortUiData data);
}
