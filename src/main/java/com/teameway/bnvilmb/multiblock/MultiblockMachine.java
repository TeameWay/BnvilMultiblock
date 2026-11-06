package com.teameway.bnvilmb.multiblock;

import com.teameway.bnvilmb.multiblock.pattern.BlockPattern;
import dev.anvilcraft.lib.v2.registrum.util.entry.RegistryEntry;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * 一台多方块机器的声明。
 *
 * <p>结构模板由代码定义，{@code pattern} 延迟求值，只在需要时构建。</p>
 *
 * @param id 机器 ID
 * @param controller 控制器入口
 * @param pattern 结构模板，延迟构建
 */
public record MultiblockMachine(
    Identifier id,
    RegistryEntry<StructureController, ? extends StructureController> controller,
    Supplier<BlockPattern> pattern
) {
    public static Builder builder(RegistryEntry<StructureController, ? extends StructureController> controller) {
        return new Builder(controller);
    }

    public static class Builder {
        private final RegistryEntry<StructureController, ? extends StructureController> controller;
        private @Nullable Supplier<BlockPattern> pattern;

        private Builder(RegistryEntry<StructureController, ? extends StructureController> controller) {
            this.controller = controller;
        }

        public Builder pattern(Supplier<BlockPattern> pattern) {
            this.pattern = pattern;
            return this;
        }

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
