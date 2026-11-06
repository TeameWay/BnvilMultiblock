package com.teameway.bnvilmb.gametest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teameway.bnvilmb.BnvilMultiBlock;
import com.teameway.bnvilmb.init.BnvilRegistries;
import com.teameway.bnvilmb.init.multiblock.BnvilControllerBlocks;
import com.teameway.bnvilmb.init.multiblock.BnvilPorts;
import com.teameway.bnvilmb.multiblock.StructureControllerRegistry;
import com.teameway.bnvilmb.multiblock.controller.ControllerBlockEntity;
import com.teameway.bnvilmb.multiblock.port.MultiblockPort;
import com.teameway.bnvilmb.multiblock.structure.StructureMatcher;
import com.teameway.bnvilmb.multiblock.structure.StructureOrientation;
import com.teameway.bnvilmb.multiblock.structure.StructurePattern;
import com.teameway.bnvilmb.multiblock.structure.StructureRole;
import com.teameway.bnvilmb.multiblock.structure.StructureRotation;
import com.teameway.bnvilmb.multiblock.structure.Substitute;
import dev.anvilcraft.lib.v2.util.predicate.BlockStatePredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 结构成型 GameTest
 *
 * <p>26.3 的 GameTest 改为注册表注册。普通客户端不应注册测试实例，否则登录时会把
 * 只存在于服务端的测试实例同步到客户端而报错。这里用系统属性
 * {@code -Dbnvil_multiblock.tests=true} 控制，只有 GameTestServer 运行配置会打开。</p>
 *
 * <p>七个用例都在 {@code empty5} 空结构里摆放端口与方块，覆盖端口换位、替代方块、
 * 旋转与朝向、加成统计，以及输入过多且缺少输出的反例。</p>
 */
@EventBusSubscriber(modid = BnvilMultiBlock.MODID)
public final class StructureGameTests {
    /**
     * 打开测试注册的系统属性名
     *
     * <p>未设置或为 false 时，两个注册方法都直接返回。</p>
     */
    public static final String ENABLE_PROPERTY = "bnvil_multiblock.tests";

    // 固定测试坐标：控制器在正中心，四个端口摆在同一层的四个角
    private static final BlockPos CONTROLLER = new BlockPos(2, 2, 2);
    private static final BlockPos INPUT = new BlockPos(1, 2, 1);
    private static final BlockPos OUTPUT = new BlockPos(3, 2, 1);
    private static final BlockPos FLUID = new BlockPos(1, 2, 3);
    private static final BlockPos PARALLEL = new BlockPos(3, 2, 3);

    private StructureGameTests() {
    }

    /**
     * 注册测试实例类型 {@code bnvil_multiblock:structure}
     *
     * @param event 注册事件
     */
    @SubscribeEvent
    public static void onRegister(RegisterEvent event) {
        if (!Boolean.getBoolean(ENABLE_PROPERTY)) {
            return;
        }
        event.register(Registries.TEST_INSTANCE_TYPE, BnvilMultiBlock.id("structure"), () -> StructureTest.CODEC);
    }

    /**
     * 注册测试环境与七个用例
     *
     * <p>环境用 {@code AllOf} 空列表；每个用例的结构是 {@code empty5}，超时 200 tick。</p>
     *
     * @param event GameTest 注册事件
     */
    @SubscribeEvent
    public static void register(RegisterGameTestsEvent event) {
        if (!Boolean.getBoolean(ENABLE_PROPERTY)) {
            return;
        }
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(
            BnvilMultiBlock.id("structure"), new TestEnvironmentDefinition.AllOf(List.of())
        );
        register(event, environment, "forms_with_interchanged_ports");
        register(event, environment, "forms_with_swapped_ports");
        register(event, environment, "forms_with_substitute");
        register(event, environment, "rotation_follows_controller");
        register(event, environment, "orientation_changes_layout");
        register(event, environment, "substitute_bonuses");
        register(event, environment, "rejects_too_many_inputs");
    }

    private static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment,
                                 String name) {
        event.registerTest(BnvilMultiBlock.id("structure/" + name), new StructureTest(name, testData(environment)));
    }

    private static TestData<Holder<TestEnvironmentDefinition<?>>> testData(Holder<TestEnvironmentDefinition<?>> environment) {
        return new TestData<>(environment, BnvilMultiBlock.id("empty5"), 200, 0, true);
    }

    private static void formsWithInterchangedPorts(GameTestHelper helper) {
        helper.assertTrue(StructureControllerRegistry.get(BnvilControllerBlocks.SIMPLE_CONTROLLER.get()) != null, "controller registry empty");
        helper.assertTrue(helper.getLevel().registryAccess().lookupOrThrow(BnvilRegistries.STRUCTURE_PATTERNS).listElements().findAny().isPresent(), "structure pattern registry empty");
        placeFrame(helper);
        helper.setBlock(INPUT, BnvilPorts.ITEM_INPUT.get());
        helper.setBlock(OUTPUT, BnvilPorts.ITEM_OUTPUT.get());
        helper.setBlock(FLUID, BnvilPorts.FLUID_INPUT.get());
        helper.setBlock(PARALLEL, BnvilPorts.PARALLEL.get());
        helper.setBlock(CONTROLLER, BnvilControllerBlocks.SIMPLE_CONTROLLER.get());

        helper.succeedWhen(() -> {
            ControllerBlockEntity controller = controller(helper);
            helper.assertTrue(controller != null && controller.runtime() != null,
                "structure did not form: " + (controller == null ? "no controller" : controller.structureError()));
            helper.assertTrue(controller.portCount() == 4, "expected four ports");
            BlockPos controllerPos = helper.absolutePos(CONTROLLER);
            for (BlockPos pos : List.of(INPUT, OUTPUT, FLUID, PARALLEL)) {
                if (helper.getLevel().getBlockEntity(helper.absolutePos(pos)) instanceof MultiblockPort port) {
                    helper.assertTrue(controllerPos.equals(port.controllerPos()), "port ownership not set");
                }
            }
        });
    }

    private static void formsWithSwappedPorts(GameTestHelper helper) {
        placeFrame(helper);
        helper.setBlock(INPUT, BnvilPorts.ITEM_OUTPUT.get());
        helper.setBlock(OUTPUT, BnvilPorts.ITEM_INPUT.get());
        helper.setBlock(FLUID, BnvilPorts.PARALLEL.get());
        helper.setBlock(PARALLEL, BnvilPorts.FLUID_INPUT.get());
        helper.setBlock(CONTROLLER, BnvilControllerBlocks.SIMPLE_CONTROLLER.get());

        helper.succeedWhen(() -> {
            ControllerBlockEntity controller = controller(helper);
            helper.assertTrue(controller != null && controller.runtime() != null,
                "swapped structure did not form: " + (controller == null ? "no controller" : controller.structureError()));
            helper.assertTrue(controller.portCount() == 4, "expected four ports");
        });
    }

    private static void formsWithSubstitute(GameTestHelper helper) {
        placeFrame(helper);
        helper.setBlock(INPUT, BnvilPorts.ITEM_INPUT.get());
        helper.setBlock(OUTPUT, BnvilPorts.ITEM_OUTPUT.get());
        helper.setBlock(FLUID, BnvilPorts.FLUID_INPUT.get());
        helper.setBlock(PARALLEL, Blocks.GOLD_BLOCK);
        helper.setBlock(CONTROLLER, BnvilControllerBlocks.SIMPLE_CONTROLLER.get());

        helper.succeedWhen(() -> {
            ControllerBlockEntity controller = controller(helper);
            helper.assertTrue(controller != null && controller.runtime() != null,
                "substitute structure did not form: " + (controller == null ? "no controller" : controller.structureError()));
        });
    }

    private static void rotationFollowsController(GameTestHelper helper) {
        var north = new Vec3i(0, 0, -1);
        helper.assertTrue(new Vec3i(1, 0, 0).equals(StructureRotation.rotate(
            north, net.minecraft.core.Direction.NORTH, net.minecraft.core.Direction.EAST,
            com.teameway.bnvilmb.multiblock.structure.StructureOrientation.HORIZONTAL)), "east rotation failed");
        helper.assertTrue(new Vec3i(0, 0, 1).equals(StructureRotation.rotate(
            north, Direction.NORTH, Direction.SOUTH,
            StructureOrientation.HORIZONTAL)), "south rotation failed");
        helper.assertTrue(new net.minecraft.core.Vec3i(-1, 0, 0).equals(StructureRotation.rotate(
            north, Direction.NORTH, Direction.WEST,
            StructureOrientation.HORIZONTAL)), "west rotation failed");
        helper.succeed();
    }

    private static void orientationChangesLayout(GameTestHelper helper) {
        var pattern = StructurePattern.builder()
            .orientation(StructureOrientation.HORIZONTAL)
            .canonicalFront(Direction.NORTH)
            .layer("G0")
            .where('G', Blocks.GOLD_BLOCK)
            .controller('0', BnvilControllerBlocks.SIMPLE_CONTROLLER.get())
            .build();
        helper.setBlock(CONTROLLER, BnvilControllerBlocks.SIMPLE_CONTROLLER.get());
        helper.setBlock(new BlockPos(2, 2, 1), Blocks.GOLD_BLOCK);

        var north = StructureMatcher.match(
            helper.getLevel(), helper.absolutePos(CONTROLLER), Direction.EAST, pattern);
        helper.assertTrue(north.formed(), "rotated east layout should match");
        var east = StructureMatcher.match(
            helper.getLevel(), helper.absolutePos(CONTROLLER), Direction.NORTH, pattern);
        helper.assertTrue(!east.formed(), "north layout should not match a rotated structure");
        helper.succeed();
    }

    private static void substituteBonusesAreReported(GameTestHelper helper) {
        var predicate = BlockStatePredicate.builder()
            .of(Blocks.GOLD_BLOCK).build();
        var pattern = StructurePattern.builder()
            .orientation(StructureOrientation.NONE)
            .layer("0G")
            .controller('0', BnvilControllerBlocks.SIMPLE_CONTROLLER.get())
            .slot('G', List.of("parallel"), false)
            .role("parallel", new StructureRole(
                List.of(), List.of(new Substitute(predicate, 2, 1)), 0, -1))
            .build();
        helper.setBlock(CONTROLLER, BnvilControllerBlocks.SIMPLE_CONTROLLER.get());
        helper.setBlock(new BlockPos(3, 2, 2), Blocks.GOLD_BLOCK);

        var match = StructureMatcher.match(
            helper.getLevel(), helper.absolutePos(CONTROLLER), Direction.NORTH, pattern);
        helper.assertTrue(match.formed(), "substitute structure should form");
        helper.assertTrue(match.parallelsBonus() == 2, "parallel bonus should be 2");
        helper.assertTrue(match.threadsBonus() == 1, "thread bonus should be 1");
        helper.succeed();
    }

    private static void rejectsTooManyInputsAndMissingOutput(GameTestHelper helper) {
        placeFrame(helper);
        helper.setBlock(INPUT, BnvilPorts.ITEM_INPUT.get());
        helper.setBlock(new BlockPos(3, 2, 3), BnvilPorts.ITEM_INPUT.get());
        helper.setBlock(new BlockPos(1, 2, 3), BnvilPorts.ITEM_INPUT.get());
        helper.setBlock(OUTPUT, BnvilPorts.PARALLEL.get());
        helper.setBlock(CONTROLLER, BnvilControllerBlocks.SIMPLE_CONTROLLER.get());

        // 成型校验在 setBlock 之后才跑，先空转 40 次轮询再断言控制器没有 runtime
        int[] waited = {0};
        helper.succeedWhen(() -> {
            waited[0]++;
            if (waited[0] < 40) {
                helper.assertTrue(false, "waiting for validation");
            }
            ControllerBlockEntity controller = controller(helper);
            helper.assertTrue(controller == null || controller.runtime() == null, "invalid structure formed");
        });
    }

    /**
     * 搭出测试底座
     *
     * <p>3x3 铁块铺在 y=1，控制器同一层的前后左右各放一个金块。</p>
     */
    private static void placeFrame(GameTestHelper helper) {
        for (int x = 1; x <= 3; x++) {
            for (int z = 1; z <= 3; z++) {
                helper.setBlock(new BlockPos(x, 1, z), Blocks.IRON_BLOCK);
            }
        }
        helper.setBlock(new BlockPos(1, 2, 2), Blocks.GOLD_BLOCK);
        helper.setBlock(new BlockPos(3, 2, 2), Blocks.GOLD_BLOCK);
        helper.setBlock(new BlockPos(2, 2, 1), Blocks.GOLD_BLOCK);
        helper.setBlock(new BlockPos(2, 2, 3), Blocks.GOLD_BLOCK);
    }

    private static ControllerBlockEntity controller(GameTestHelper helper) {
        return helper.getLevel().getBlockEntity(helper.absolutePos(CONTROLLER)) instanceof ControllerBlockEntity controller
               ? controller
               : null;
    }

    /**
     * 可序列化的测试实例
     *
     * <p>只保存测试名与 {@link TestData}，运行方法通过 {@link #TESTS} 查找；这样服务端注册表
     * 同步时不会出现 {@code MapCodec.unit(this)} 产生的 Direct holder。</p>
     */
    private static final class StructureTest extends GameTestInstance {
        private static final Map<String, Consumer<GameTestHelper>> TESTS = Map.of(
            "forms_with_interchanged_ports", StructureGameTests::formsWithInterchangedPorts,
            "forms_with_swapped_ports", StructureGameTests::formsWithSwappedPorts,
            "forms_with_substitute", StructureGameTests::formsWithSubstitute,
            "rotation_follows_controller", StructureGameTests::rotationFollowsController,
            "orientation_changes_layout", StructureGameTests::orientationChangesLayout,
            "substitute_bonuses", StructureGameTests::substituteBonusesAreReported,
            "rejects_too_many_inputs", StructureGameTests::rejectsTooManyInputsAndMissingOutput
        );
        public static final MapCodec<StructureTest> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("test").forGetter(StructureTest::testName),
            TestData.CODEC.fieldOf("test_data").forGetter(StructureTest::data)
        ).apply(instance, StructureTest::new));

        private final String testName;
        private final TestData<Holder<TestEnvironmentDefinition<?>>> data;

        private StructureTest(String testName, TestData<Holder<TestEnvironmentDefinition<?>>> data) {
            super(data);
            this.testName = testName;
            this.data = data;
        }

        private String testName() {
            return this.testName;
        }

        private TestData<Holder<TestEnvironmentDefinition<?>>> data() {
            return this.data;
        }

        /**
         * 查找并执行对应的用例
         *
         * @param helper GameTest 辅助对象
         */
        @Override
        public void run(GameTestHelper helper) {
            Consumer<GameTestHelper> test = TESTS.get(this.testName);
            if (test == null) {
                throw helper.assertionException("Unknown structure test: " + this.testName);
            }
            test.accept(helper);
        }

        /**
         * @return 解码本实例的 {@link #CODEC}
         */
        @Override
        public MapCodec<? extends GameTestInstance> codec() {
            return CODEC;
        }

        @Override
        protected MutableComponent typeDescription() {
            return Component.literal("BnvilMultiblock structure: " + this.testName);
        }
    }
}
