package com.teameway.bnvilmb.integration.jei.ui;

import com.teameway.bnvilmb.ldlib.slot.CompactItemSlot;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 结构页左侧的方块格
 *
 * <p>在普通格子之上多挂一行结构条件，提示里可以同时看到方块名与“最多/精确/最少”的数量要求。
 * 条件为空时不加这一行。</p>
 */
public class StructureHintSlot extends CompactItemSlot {
    private @Nullable Component hint;

    /**
     * 设置或清除条件行
     *
     * @param hint 条件文本，{@code null} 表示不显示
     * @return 自身，便于链式调用
     */
    public StructureHintSlot hint(@Nullable Component hint) {
        this.hint = hint;
        return this;
    }

    /**
     * 在父类的提示列表末尾追加条件行
     *
     * @param withItemTooltips 是否包含物品自身的提示
     * @return 完整提示列表
     */
    @Override
    public List<Component> getFullTooltipTexts(boolean withItemTooltips) {
        List<Component> tooltips = new ArrayList<>(super.getFullTooltipTexts(withItemTooltips));
        if (this.hint != null) {
            tooltips.add(this.hint.copy().withStyle(ChatFormatting.GRAY));
        }
        return tooltips;
    }
}
