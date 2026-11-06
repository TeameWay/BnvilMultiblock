package com.teameway.bnvilmb.datagen.multiblock;

import com.teameway.bnvilmb.init.BnvilRegistries;
import com.teameway.bnvilmb.multiblock.MultiblockMachine;
import com.teameway.bnvilmb.multiblock.structure.StructurePattern;
import dev.anvilcraft.lib.v2.registrum.util.entry.RegistryEntry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.teameway.bnvilmb.BnvilMultiBlock.REGISTRUM;

/**
 * 结构模板数据生成器
 *
 * <p>遍历 {@code bnvil_multiblock:multiblocks} 注册表，每个 {@link MultiblockMachine} 写一个文件，
 * 内容按 {@link StructurePattern#CODEC} 序列化，路径为
 * {@code data/<namespace>/bnvil_multiblock/structure_patterns/<path>.json}。</p>
 */
public class BnvilMultiblockDefinitionProvider implements DataProvider {
    private static final Logger LOGGER = LogManager.getLogger(BnvilMultiblockDefinitionProvider.class);

    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> registries;

    /**
     * 创建 provider
     *
     * <p>两个参数都只保存，等到 {@link #run} 时才使用。</p>
     *
     * @param output 数据包输出目标
     * @param registries 注册表查询，datagen 启动后才会完成
     */
    public BnvilMultiblockDefinitionProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        this.output = output;
        this.registries = registries;
    }

    /**
     * 写出单个机器的结构模板
     *
     * <p>文件名取 {@link MultiblockMachine#id()}，模板从 {@code pattern} supplier 取，
     * 注册表信息用于序列化时解析模板里的 holder。</p>
     *
     * @param cache 数据生成缓存
     * @param registries 注册表查询，序列化时的 holder 解析依赖它
     * @param pathProvider 输出路径
     * @param machine 要导出的机器
     * @return 写入完成的 future
     */
    private static CompletableFuture<?> save(
        CachedOutput cache,
        HolderLookup.Provider registries,
        PackOutput.PathProvider pathProvider,
        MultiblockMachine machine
    ) {
        Identifier id = machine.id();
        StructurePattern pattern = machine.pattern().get();
        return DataProvider.saveStable(cache, registries, StructurePattern.CODEC, pattern, pathProvider.json(id));
    }

    /**
     * 生成全部结构模板
     *
     * <p>路径 provider 由 {@code BnvilRegistries.STRUCTURE_PATTERNS} 创建，机器逐个写出，
     * 最后合并成一个 future。</p>
     *
     * @param cache 数据生成缓存
     * @return 全部文件写完的 future
     */
    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        PackOutput.PathProvider pathProvider = this.output.createRegistryElementsPathProvider(BnvilRegistries.STRUCTURE_PATTERNS);
        return this.registries.thenCompose(lookup -> {
            List<MultiblockMachine> machines = REGISTRUM.getAll(BnvilRegistries.MULTIBLOCKS)
                .stream()
                .map(RegistryEntry::get)
                .toList();
            LOGGER.info("Generating {} structure pattern(s)", machines.size());
            CompletableFuture<?>[] futures = machines.stream()
                .map(machine -> save(cache, lookup, pathProvider, machine))
                .toArray(CompletableFuture[]::new);
            return CompletableFuture.allOf(futures);
        });
    }

    /**
     * @return provider 名称
     */
    @Override
    public String getName() {
        return "BnvilMultiBlock Structure Patterns";
    }
}
