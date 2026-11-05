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
 * <p>遍历 {@code bnvil_multiblock:multiblocks} 注册表，输出到
 * {@code data/<namespace>/bnvil_multiblock/structure_patterns/<path>.json}。</p>
 */
public class BnvilMultiblockDefinitionProvider implements DataProvider {
    private static final Logger LOGGER = LogManager.getLogger(BnvilMultiblockDefinitionProvider.class);

    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> registries;

    public BnvilMultiblockDefinitionProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        this.output = output;
        this.registries = registries;
    }

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

    @Override
    public String getName() {
        return "BnvilMultiBlock Structure Patterns";
    }
}
