package com.teameway.bnvilmb;

import com.teameway.bnvilmb.init.BnvilRegistries;
import com.teameway.bnvilmb.multiblock.MultiblockMachine;
import com.teameway.bnvilmb.multiblock.MultiblockType;
import com.teameway.bnvilmb.multiblock.StructureController;
import dev.anvilcraft.lib.v2.registrum.AbstractRegistrum;
import dev.anvilcraft.lib.v2.registrum.builders.NoConfigBuilder;
import dev.anvilcraft.lib.v2.util.nullness.NonNullSupplier;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;

import java.util.Optional;
import java.util.function.Function;

/**
 * 模组自己的 Registrum
 *
 * <p>在 Registrum 自带的方块与物品入口之外，补上多方块机器、机器类型与控制器的注册入口，
 * 条目写进 {@link BnvilRegistries} 自建的注册表。其他模组可以调用 {@link #create(String)}
 * 创建自己的实例，条目 ID 使用该模组的命名空间。</p>
 */
@Slf4j
public class BnvilRegistrum extends AbstractRegistrum<BnvilRegistrum> {

    private BnvilRegistrum(String modid) {
        super(modid);
    }

    /**
     * 创建注册入口，并挂上模组事件总线
     *
     * <p>取不到模组容器时把错误信息写进日志，不抛异常。</p>
     *
     * @param modid 模组 ID
     * @return Registrum 实例
     */
    public static BnvilRegistrum create(String modid) {
        BnvilRegistrum registrum = new BnvilRegistrum(modid);
        Optional<IEventBus> modEventBus = ModList.get().getModContainerById(modid).map(ModContainer::getEventBus);
        modEventBus.ifPresentOrElse(
            registrum::registerEventListeners, () -> {
                String message = "# [BnvilRegistrum] Failed to register eventListeners for mod " + modid
                                 + ", This should be reported to this mod's dev #";
                StringBuilder hashtags = new StringBuilder().repeat("#", message.length());
                log.error(hashtags.toString());
                log.error(message);
                log.error(hashtags.toString());
            }
        );
        return registrum;
    }

    /**
     * 注册一个多方块控制器
     *
     * <p>结构模板由代码定义，工厂只接收控制器方块。</p>
     *
     * @param name 入口名
     * @param block 控制器方块
     * @param factory 控制器工厂
     * @param <T> 控制器类型
     * @return 构建器，{@code register()} 得到 {@code RegistryEntry}
     */
    public <T extends StructureController> NoConfigBuilder<StructureController, T, BnvilRegistrum> multiblockController(
        String name,
        NonNullSupplier<? extends Block> block,
        Function<Block, T> factory
    ) {
        return this.generic(
            name,
            BnvilRegistries.MULTIBLOCK_CONTROLLERS,
            () -> factory.apply(block.get())
        );
    }

    /**
     * 注册一个多方块机器类型
     *
     * @param name 类型名，同时用作 pattern 的 variant ID
     * @param factory 类型工厂，条目求值时才调用
     * @return 构建器
     */
    public NoConfigBuilder<MultiblockType, MultiblockType, BnvilRegistrum> multiblockType(
        String name,
        NonNullSupplier<MultiblockType> factory
    ) {
        return this.generic(name, BnvilRegistries.MULTIBLOCK_TYPES, factory);
    }

    /**
     * 注册一台多方块机器声明
     *
     * <p>声明进入 {@code bnvil_multiblock:multiblocks}，供 JEI 与结构预览枚举。</p>
     *
     * @param name 入口名
     * @param factory 机器声明工厂，结构模板延迟到需要时才构建
     * @return 构建器，{@code register()} 得到 {@code RegistryEntry}
     */
    public NoConfigBuilder<MultiblockMachine, MultiblockMachine, BnvilRegistrum> multiblock(
        String name,
        NonNullSupplier<MultiblockMachine> factory
    ) {
        return this.generic(name, BnvilRegistries.MULTIBLOCKS, factory);
    }
}