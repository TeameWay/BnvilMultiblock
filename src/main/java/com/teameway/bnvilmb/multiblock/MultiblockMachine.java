package com.teameway.bnvilmb.multiblock;

import com.teameway.bnvilmb.multiblock.structure.StructurePattern;
import dev.anvilcraft.lib.v2.registrum.util.entry.RegistryEntry;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * 一台多方块机器的声明
 *
 * <p>把结构定义 ID、控制器入口与结构模板绑在一起。{@code pattern} 延迟求值，
 * 只在 datagen 和真正需要模板时调用。</p>
 *
 * @param id 结构定义 ID，同时也是 datagen 输出的文件名
 * @param controller 控制器入口，取自 {@code bnvil_multiblock:multiblock_controllers} 注册表
 * @param pattern 结构模板，延迟构建
 */
public record MultiblockMachine(
    Identifier id,
    RegistryEntry<StructureController, ? extends StructureController> controller,
    Supplier<StructurePattern> pattern
) {
    /**
     * 开始声明一台机器
     *
     * @param controller 控制器入口，其入口名即定义 ID 的路径
     * @return 链式构建器
     */
    public static Builder builder(RegistryEntry<StructureController, ? extends StructureController> controller) {
        return new Builder(controller);
    }

    /**
     * 机器声明的链式构建器
     *
     * <p>控制器在调用 {@link MultiblockMachine#builder} 时传入，模板用 {@link #pattern(Supplier)}
     * 补上；没有模板时 {@link #build} 会抛异常。</p>
     */
    public static class Builder {
        private final RegistryEntry<StructureController, ? extends StructureController> controller;
        private @Nullable Supplier<StructurePattern> pattern;

        private Builder(RegistryEntry<StructureController, ? extends StructureController> controller) {
            this.controller = controller;
        }

        /**
         * 声明结构模板
         *
         * <p>用 {@code StructurePattern.builder()} 链式声明；传入的是 supplier，模板在需要时才构建。</p>
         *
         * @param pattern 结构模板（延迟构建）
         * @return 自身，便于继续链式调用
         */
        public Builder pattern(Supplier<StructurePattern> pattern) {
            this.pattern = pattern;
            return this;
        }

        /**
         * 完成声明
         *
         * <p>定义 ID 取控制器入口的 ID。</p>
         *
         * @return 机器声明
         * @throws NullPointerException 没有先调用 {@link #pattern(Supplier)} 设置结构模板
         */
        public MultiblockMachine build() {
            Identifier id = this.controller.getId();
            return new MultiblockMachine(
                id,
                this.controller,
                Objects.requireNonNull(this.pattern, () -> "Multiblock " + id + " is missing a pattern")
            );
        }
    }
}
