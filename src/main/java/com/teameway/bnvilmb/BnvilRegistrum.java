package com.teameway.bnvilmb;

import com.teameway.bnvilmb.init.BnvilRegistries;
import com.teameway.bnvilmb.multiblock.MultiblockMachine;
import com.teameway.bnvilmb.multiblock.MultiblockType;
import com.teameway.bnvilmb.multiblock.StructureController;
import dev.anvilcraft.lib.v2.registrum.AbstractRegistrum;
import dev.anvilcraft.lib.v2.registrum.builders.NoConfigBuilder;
import dev.anvilcraft.lib.v2.util.nullness.NonNullSupplier;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.BiFunction;

/**
 * 模组自己的 Registrum
 *
 * <p>在 Registrum 自带的方块与物品入口之外，补上多方块机器、机器类型与控制器的注册入口，
 * 条目写进 {@link BnvilRegistries} 自建的注册表。</p>
 */
public class BnvilRegistrum extends AbstractRegistrum<BnvilRegistrum> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BnvilRegistrum.class);

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
                LOGGER.error(hashtags.toString());
                LOGGER.error(message);
                LOGGER.error(hashtags.toString());
            }
        );
        return registrum;
    }

    /**
     * 注册一个多方块控制器
     *
     * <p>入口名同时作为结构定义 ID 的路径，决定定义 JSON 的文件名；工厂收到的定义 ID 也用它拼出来。</p>
     *
     * @param name 入口名
     * @param block 控制器方块
     * @param factory 控制器工厂，入参为控制器方块与定义 ID
     * @param <T> 控制器类型
     * @return 构建器，{@code register()} 得到 {@code RegistryEntry}
     */
    public <T extends StructureController> NoConfigBuilder<StructureController, T, BnvilRegistrum> multiblockController(
        String name,
        NonNullSupplier<? extends Block> block,
        BiFunction<Block, Identifier, T> factory
    ) {
        Identifier definitionId = Identifier.fromNamespaceAndPath(this.getModid(), name);
        return this.generic(
            name,
            BnvilRegistries.MULTIBLOCK_CONTROLLERS,
            () -> factory.apply(block.get(), definitionId)
        );
    }

    /**
     * 注册一个多方块机器类型
     *
     * @param name 类型名，同时用作结构注册表 tag 的路径
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
     * <p>声明进入 {@code bnvil_multiblock:multiblocks}，数据生成遍历该注册表输出结构模板 JSON。</p>
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
