package com.teameway.bnvilmb.multiblock.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teameway.bnvilmb.multiblock.port.PortKind;
import dev.anvilcraft.lib.v2.util.predicate.BlockStatePredicate;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * 数据驱动的多方块结构模板
 *
 * <p>网格字符分两类：{@code mapping} 里的字符是固定方块谓词，{@code slots} 里的字符是槽位。
 * 槽位不绑定角色，角色只约束全局数量，因此端口位置可以在槽位之间互换。</p>
 *
 * <p>网格按层书写：一层对应一个 Y 高度，层内每行对应一个 Z，行内每个字符对应一个 X。
 * 空格表示该位置不参与结构；{@code 0} 必须是固定方块并作为原点，其余偏移都相对它计算。
 * 单个轴向尺寸不能超过 {@code MAX_SIZE}，即 64。</p>
 */
public final class StructurePattern {
    /** 单个轴向允许的最大尺寸 */
    private static final int MAX_SIZE = 64;

    /** 字符键映射编解码器，JSON 中使用单字符字符串作键 */
    public static final Codec<Map<Character, BlockStatePredicate>> MAPPING_CODEC =
        Codec.unboundedMap(Codec.STRING, BlockStatePredicate.CODEC)
            .xmap(StructurePattern::toCharMap, StructurePattern::toStringMap);
    /** 字符键槽位编解码器，JSON 中使用单字符字符串作键 */
    public static final Codec<Map<Character, StructureSlot>> SLOT_CODEC =
        Codec.unboundedMap(Codec.STRING, StructureSlot.CODEC)
            .xmap(StructurePattern::toCharSlotMap, StructurePattern::toStringSlotMap);

    /** 结构模板编解码器，用于数据包读取与数据生成 */
    public static final Codec<StructurePattern> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        StructureOrientation.CODEC.optionalFieldOf("orientation", StructureOrientation.NONE)
            .forGetter(StructurePattern::orientation),
        Direction.CODEC.optionalFieldOf("canonical_front", Direction.NORTH)
            .forGetter(StructurePattern::canonicalFront),
        Codec.STRING.listOf().listOf().fieldOf("grid").forGetter(StructurePattern::grid),
        MAPPING_CODEC.fieldOf("mapping").forGetter(StructurePattern::mapping),
        SLOT_CODEC.optionalFieldOf("slots", Map.of()).forGetter(StructurePattern::slots),
        Codec.unboundedMap(Codec.STRING, StructureRole.CODEC).fieldOf("roles").forGetter(StructurePattern::roles)
    ).apply(instance, StructurePattern::new));

    private final StructureOrientation orientation;
    private final Direction canonicalFront;
    private final List<List<String>> grid;
    private final Map<Character, BlockStatePredicate> mapping;
    private final Map<Character, StructureSlot> slots;
    private final Map<String, StructureRole> roles;
    private final Map<Vec3i, BlockStatePredicate> fixedCells;
    private final Map<Vec3i, Character> slotKeys;
    private final List<Vec3i> positions;
    private final List<Vec3i> slotOffsets;
    private final Vec3i size;
    private final BlockStatePredicate controller;

    /**
     * 校验网格并建立索引
     *
     * <p>构造时复制全部集合，按 Y、Z、X 的顺序遍历网格，把每个非空格字符换算成相对控制器
     * {@code 0} 的偏移，然后调用 {@link #validate()} 检查槽位与角色约束。</p>
     *
     * @param orientation 朝向模式
     * @param canonicalFront 主朝向，{@code HORIZONTAL} 下必须是水平方向
     * @param grid 分层网格，外层下标为 Y，中层为 Z，字符串内下标为 X
     * @param mapping 固定方块字符到谓词的映射
     * @param slots 槽位字符到槽位定义的映射
     * @param roles 角色 ID 到角色定义的映射
     * @throws IllegalArgumentException 网格形状不合法、找不到 {@code 0} 或 {@code 0} 出现在多行、字符未定义、
     *     槽位与方块字符冲突、角色或槽位约束不成立，或单个轴向尺寸超过 {@code MAX_SIZE}
     */
    public StructurePattern(
        StructureOrientation orientation,
        Direction canonicalFront,
        List<List<String>> grid,
        Map<Character, BlockStatePredicate> mapping,
        Map<Character, StructureSlot> slots,
        Map<String, StructureRole> roles
    ) {
        this.orientation = orientation;
        this.canonicalFront = canonicalFront;
        this.grid = copyGrid(grid);
        this.mapping = Map.copyOf(mapping);
        this.slots = Map.copyOf(slots);
        this.roles = Map.copyOf(roles);

        if (this.orientation == StructureOrientation.HORIZONTAL && !canonicalFront.getAxis().isHorizontal()) {
            throw new IllegalArgumentException("canonical_front must be horizontal when orientation is horizontal");
        }

        Vec3i controllerPos = findController();
        Map<Vec3i, BlockStatePredicate> builtFixed = new LinkedHashMap<>();
        Map<Vec3i, Character> builtSlotKeys = new LinkedHashMap<>();
        List<Vec3i> builtPositions = new ArrayList<>();
        List<Vec3i> builtSlots = new ArrayList<>();
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;

        for (int y = 0; y < this.grid.size(); y++) {
            List<String> layer = this.grid.get(y);
            for (int z = 0; z < layer.size(); z++) {
                String row = layer.get(z);
                for (int x = 0; x < row.length(); x++) {
                    char key = row.charAt(x);
                    if (key == ' ') {
                        continue;
                    }
                    Vec3i pos = new Vec3i(x - controllerPos.getX(), y - controllerPos.getY(), z - controllerPos.getZ());
                    builtPositions.add(pos);
                    if (this.slots.containsKey(key)) {
                        if (this.mapping.containsKey(key)) {
                            throw new IllegalArgumentException("Grid key '" + key + "' is both a block and a slot");
                        }
                        builtSlotKeys.put(pos, key);
                        builtSlots.add(pos);
                    } else {
                        BlockStatePredicate predicate = this.mapping.get(key);
                        if (predicate == null) {
                            throw new IllegalArgumentException("Undefined mapping key '" + key + "'");
                        }
                        builtFixed.put(pos, predicate);
                    }
                    minX = Math.min(minX, pos.getX());
                    minY = Math.min(minY, pos.getY());
                    minZ = Math.min(minZ, pos.getZ());
                    maxX = Math.max(maxX, pos.getX());
                    maxY = Math.max(maxY, pos.getY());
                    maxZ = Math.max(maxZ, pos.getZ());
                }
            }
        }

        BlockStatePredicate controllerPredicate = builtFixed.get(Vec3i.ZERO);
        if (controllerPredicate == null || builtSlotKeys.containsKey(Vec3i.ZERO)) {
            throw new IllegalArgumentException("Controller '0' must be a fixed cell at the origin");
        }
        this.controller = controllerPredicate;
        this.fixedCells = Collections.unmodifiableMap(builtFixed);
        this.slotKeys = Collections.unmodifiableMap(builtSlotKeys);
        this.positions = List.copyOf(builtPositions);
        this.slotOffsets = List.copyOf(builtSlots);
        this.size = new Vec3i(maxX - minX + 1, maxY - minY + 1, maxZ - minZ + 1);
        if (this.size.getX() > MAX_SIZE || this.size.getY() > MAX_SIZE || this.size.getZ() > MAX_SIZE) {
            throw new IllegalArgumentException("Structure is too large: " + this.size);
        }
        this.validate();
    }

    /**
     * 检查槽位与角色约束
     *
     * <p>要求所有槽位定义至少被使用一次，角色的下限总和不超过槽位数；有下限的角色必须被某个
     * 槽位引用，且每个角色至少接受一种端口或替代方块；同一槽位内的两个角色不能提供同一种端口。
     * 任一条件不满足时抛异常。</p>
     */
    private void validate() {
        if (this.positions.isEmpty()) {
            throw new IllegalArgumentException("Structure is empty");
        }
        Set<Character> usedSlots = new LinkedHashSet<>(this.slotKeys.values());
        for (Character key : this.slots.keySet()) {
            if (!usedSlots.contains(key)) {
                throw new IllegalArgumentException("Slot definition '" + key + "' is never used");
            }
        }
        int minTotal = 0;
        for (Map.Entry<String, StructureRole> entry : this.roles.entrySet()) {
            StructureRole role = entry.getValue();
            minTotal += role.min();
            boolean referenced = false;
            for (StructureSlot slot : this.slots.values()) {
                if (slot.roles().contains(entry.getKey())) {
                    referenced = true;
                    break;
                }
            }
            if (!referenced && role.min() > 0) {
                throw new IllegalArgumentException("Role '" + entry.getKey() + "' has a minimum but is not used by any slot");
            }
            if (role.portKinds().isEmpty() && role.substitutes().isEmpty()) {
                throw new IllegalArgumentException("Role '" + entry.getKey() + "' accepts nothing");
            }
        }
        if (minTotal > this.slotOffsets.size()) {
            throw new IllegalArgumentException("Sum of role minimums " + minTotal + " exceeds slot count " + this.slotOffsets.size());
        }
        for (Map.Entry<Character, StructureSlot> entry : this.slots.entrySet()) {
            Map<PortKind, String> portKinds = new LinkedHashMap<>();
            for (String roleId : entry.getValue().roles()) {
                StructureRole role = this.roles.get(roleId);
                if (role == null) {
                    throw new IllegalArgumentException("Slot references unknown role '" + roleId + "'");
                }
                for (PortKind kind : role.portKinds()) {
                    String previous = portKinds.putIfAbsent(kind, roleId);
                    if (previous != null && !previous.equals(roleId)) {
                        throw new IllegalArgumentException(
                            "Slot '" + entry.getKey() + "' accepts port kind '" + kind.getSerializedName()
                            + "' from both roles '" + previous + "' and '" + roleId + "'"
                        );
                    }
                }
            }
        }
    }

    /**
     * 定位控制器字符 {@code 0}
     *
     * @return {@code 0} 的网格坐标，作为所有偏移的原点
     * @throws IllegalArgumentException 找不到 {@code 0}，或有多行包含 {@code 0}
     */
    private Vec3i findController() {
        Vec3i found = null;
        for (int y = 0; y < this.grid.size(); y++) {
            List<String> layer = this.grid.get(y);
            for (int z = 0; z < layer.size(); z++) {
                String row = layer.get(z);
                int x = row.indexOf('0');
                if (x < 0) {
                    continue;
                }
                if (found != null) {
                    throw new IllegalArgumentException("Multiple controller '0' symbols");
                }
                found = new Vec3i(x, y, z);
            }
        }
        if (found == null) {
            throw new IllegalArgumentException("Structure has no controller '0'");
        }
        return found;
    }

    /**
     * 复制网格并校验形状
     *
     * <p>所有层必须有相同行数，所有行必须有相同宽度；空网格或空层直接抛异常。</p>
     */
    private static List<List<String>> copyGrid(List<List<String>> grid) {
        if (grid.isEmpty()) {
            throw new IllegalArgumentException("Grid is empty");
        }
        List<List<String>> copied = new ArrayList<>(grid.size());
        int width = -1;
        int height = -1;
        for (List<String> layer : grid) {
            if (layer.isEmpty()) {
                throw new IllegalArgumentException("Layer is empty");
            }
            if (width < 0) {
                width = layer.getFirst().length();
                height = layer.size();
            }
            if (layer.size() != height) {
                throw new IllegalArgumentException("All layers must have the same row count");
            }
            List<String> rows = new ArrayList<>(layer.size());
            for (String row : layer) {
                if (row.length() != width) {
                    throw new IllegalArgumentException("All rows must have the same width");
                }
                rows.add(row);
            }
            copied.add(List.copyOf(rows));
        }
        return List.copyOf(copied);
    }

    private static Map<Character, BlockStatePredicate> toCharMap(Map<String, BlockStatePredicate> map) {
        Map<Character, BlockStatePredicate> result = new LinkedHashMap<>();
        map.forEach((key, value) -> {
            if (key.length() != 1) {
                throw new IllegalArgumentException("Mapping key must be one character: " + key);
            }
            result.put(key.charAt(0), value);
        });
        return result;
    }

    private static Map<String, BlockStatePredicate> toStringMap(Map<Character, BlockStatePredicate> map) {
        Map<String, BlockStatePredicate> result = new LinkedHashMap<>();
        map.forEach((key, value) -> result.put(String.valueOf(key), value));
        return result;
    }

    private static Map<Character, StructureSlot> toCharSlotMap(Map<String, StructureSlot> map) {
        Map<Character, StructureSlot> result = new LinkedHashMap<>();
        map.forEach((key, value) -> {
            if (key.length() != 1) {
                throw new IllegalArgumentException("Slot key must be one character: " + key);
            }
            result.put(key.charAt(0), value);
        });
        return result;
    }

    private static Map<String, StructureSlot> toStringSlotMap(Map<Character, StructureSlot> map) {
        Map<String, StructureSlot> result = new LinkedHashMap<>();
        map.forEach((key, value) -> result.put(String.valueOf(key), value));
        return result;
    }

    /**
     * @return 新的构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * @return 朝向模式
     */
    public StructureOrientation orientation() {
        return this.orientation;
    }

    /**
     * @return 主朝向，网格坐标按这个方向定义
     */
    public Direction canonicalFront() {
        return this.canonicalFront;
    }

    /**
     * @return 分层网格，只读
     */
    public List<List<String>> grid() {
        return this.grid;
    }

    /**
     * @return 固定方块字符到谓词的映射
     */
    public Map<Character, BlockStatePredicate> mapping() {
        return this.mapping;
    }

    /**
     * @return 槽位字符到槽位定义的映射
     */
    public Map<Character, StructureSlot> slots() {
        return this.slots;
    }

    /**
     * @return 角色 ID 到角色定义的映射
     */
    public Map<String, StructureRole> roles() {
        return this.roles;
    }

    /**
     * @return 所有非空格字符相对控制器的偏移，按 Y、Z、X 遍历顺序
     */
    public List<Vec3i> positions() {
        return this.positions;
    }

    /**
     * @return 槽位偏移，顺序与 {@link #positions()} 中的槽位项一致
     */
    public List<Vec3i> slotOffsets() {
        return this.slotOffsets;
    }

    /**
     * @return 结构包围盒尺寸
     */
    public Vec3i size() {
        return this.size;
    }

    /**
     * @return 原点 {@code 0} 处的固定方块谓词
     */
    public BlockStatePredicate controller() {
        return this.controller;
    }

    /**
     * @param offset 相对控制器的偏移
     * @return 该偏移是否为槽位
     */
    public boolean isSlot(Vec3i offset) {
        return this.slotKeys.containsKey(offset);
    }

    /**
     * @param offset 相对控制器的偏移
     * @return 槽位字符，非槽位时为 {@code null}
     */
    public Character keyAt(Vec3i offset) {
        return this.slotKeys.get(offset);
    }

    /**
     * @param offset 相对控制器的偏移
     * @return 槽位定义，非槽位时为 {@code null}
     */
    public StructureSlot slotAt(Vec3i offset) {
        Character key = this.keyAt(offset);
        return key == null ? null : this.slots.get(key);
    }

    /**
     * @param offset 相对控制器的偏移
     * @return 固定方块谓词，非固定位置时为 {@code null}
     */
    public BlockStatePredicate fixedAt(Vec3i offset) {
        return this.fixedCells.get(offset);
    }

    /**
     * 把槽位偏移旋转到指定朝向
     *
     * @param facing 控制器当前正面
     * @return 旋转后的槽位偏移
     */
    public List<Vec3i> rotatedSlotOffsets(Direction facing) {
        List<Vec3i> result = new ArrayList<>(this.slotOffsets.size());
        for (Vec3i offset : this.slotOffsets) {
            result.add(StructureRotation.rotate(offset, this.canonicalFront, facing, this.orientation));
        }
        return result;
    }

    /**
     * 结构模板构建器
     *
     * <p>{@link #layer(String...)} 每次追加一层，{@link #build()} 时统一复制并校验。
     * 构建前必须为字符 {@code 0} 注册固定方块映射。</p>
     */
    public static final class Builder {
        private final List<List<String>> grid = new ArrayList<>();
        private final Map<Character, BlockStatePredicate> mapping = new LinkedHashMap<>();
        private final Map<Character, StructureSlot> slots = new LinkedHashMap<>();
        private final Map<String, StructureRole> roles = new LinkedHashMap<>();
        private StructureOrientation orientation = StructureOrientation.NONE;
        private Direction canonicalFront = Direction.NORTH;

        private Builder() {
        }

        /**
         * @param orientation 朝向模式
         * @return 自身
         */
        public Builder orientation(StructureOrientation orientation) {
            this.orientation = orientation;
            return this;
        }

        /**
         * @param canonicalFront 主朝向
         * @return 自身
         */
        public Builder canonicalFront(Direction canonicalFront) {
            this.canonicalFront = canonicalFront;
            return this;
        }

        /**
         * 追加一层网格
         *
         * @param rows 该层的行，参数顺序即 Z 轴顺序
         * @return 自身
         */
        public Builder layer(String... rows) {
            this.grid.add(List.of(rows));
            return this;
        }

        /**
         * 把字符映射到只匹配指定方块的谓词
         *
         * @param key 单字符键
         * @param block 方块
         * @return 自身
         */
        public Builder where(char key, Block block) {
            return this.where(key, BlockStatePredicate.builder().of(block).build());
        }

        /**
         * 把字符映射到指定方块提供的谓词，调用时立即从 supplier 取值
         *
         * @param key 单字符键
         * @param block 方块提供者
         * @return 自身
         */
        public Builder where(char key, Supplier<? extends Block> block) {
            return this.where(key, block.get());
        }

        /**
         * @param key 单字符键
         * @param predicate 方块谓词
         * @return 自身
         */
        public Builder where(char key, BlockStatePredicate predicate) {
            this.mapping.put(key, predicate);
            return this;
        }

        /**
         * 登记控制器方块字符
         *
         * @param key 单字符键
         * @param block 控制器方块
         * @return 自身
         */
        public Builder controller(char key, Block block) {
            return this.where(key, block);
        }

        /**
         * @param key 单字符键
         * @param slot 槽位定义
         * @return 自身
         */
        public Builder slot(char key, StructureSlot slot) {
            this.slots.put(key, slot);
            return this;
        }

        /**
         * 用角色 ID 列表创建槽位
         *
         * @param key 单字符键
         * @param roles 允许的角色 ID，顺序决定候选与诊断顺序
         * @param allowAir 是否允许该位置为空
         * @return 自身
         */
        public Builder slot(char key, List<String> roles, boolean allowAir) {
            return this.slot(key, new StructureSlot(roles, allowAir));
        }

        /**
         * @param id 角色 ID
         * @param role 角色定义
         * @return 自身
         */
        public Builder role(String id, StructureRole role) {
            this.roles.put(id, role);
            return this;
        }

        /**
         * 只接受一种端口种类的角色
         *
         * @param id 角色 ID
         * @param kind 端口种类
         * @param min 最少数量
         * @param max 最多数量，{@code -1} 表示不限
         * @return 自身
         */
        public Builder role(String id, PortKind kind, int min, int max) {
            return this.role(id, new StructureRole(List.of(kind), List.of(), min, max));
        }

        /**
         * 用当前内容构造结构模板
         *
         * @return 新的结构模板
         * @throws IllegalArgumentException 没有为 {@code 0} 注册映射，或模板校验失败
         */
        public StructurePattern build() {
            if (this.mapping.get('0') == null) {
                throw new IllegalArgumentException("Controller '0' is not mapped");
            }
            return new StructurePattern(
                this.orientation,
                this.canonicalFront,
                List.copyOf(this.grid),
                Map.copyOf(this.mapping),
                Map.copyOf(this.slots),
                Map.copyOf(this.roles)
            );
        }
    }
}
