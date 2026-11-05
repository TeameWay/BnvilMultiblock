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
 * 槽位不绑定角色，角色只约束全局数量，因此端口位置可以互换。</p>
 */
public final class StructurePattern {
    private static final int MAX_SIZE = 64;

    public static final Codec<Map<Character, BlockStatePredicate>> MAPPING_CODEC =
        Codec.unboundedMap(Codec.STRING, BlockStatePredicate.CODEC)
            .xmap(StructurePattern::toCharMap, StructurePattern::toStringMap);
    public static final Codec<Map<Character, StructureSlot>> SLOT_CODEC =
        Codec.unboundedMap(Codec.STRING, StructureSlot.CODEC)
            .xmap(StructurePattern::toCharSlotMap, StructurePattern::toStringSlotMap);

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

    public static Builder builder() {
        return new Builder();
    }

    public StructureOrientation orientation() {
        return this.orientation;
    }

    public Direction canonicalFront() {
        return this.canonicalFront;
    }

    public List<List<String>> grid() {
        return this.grid;
    }

    public Map<Character, BlockStatePredicate> mapping() {
        return this.mapping;
    }

    public Map<Character, StructureSlot> slots() {
        return this.slots;
    }

    public Map<String, StructureRole> roles() {
        return this.roles;
    }

    public List<Vec3i> positions() {
        return this.positions;
    }

    public List<Vec3i> slotOffsets() {
        return this.slotOffsets;
    }

    public Vec3i size() {
        return this.size;
    }

    public BlockStatePredicate controller() {
        return this.controller;
    }

    public boolean isSlot(Vec3i offset) {
        return this.slotKeys.containsKey(offset);
    }

    public Character keyAt(Vec3i offset) {
        return this.slotKeys.get(offset);
    }

    public StructureSlot slotAt(Vec3i offset) {
        Character key = this.keyAt(offset);
        return key == null ? null : this.slots.get(key);
    }

    public BlockStatePredicate fixedAt(Vec3i offset) {
        return this.fixedCells.get(offset);
    }

    public List<Vec3i> rotatedSlotOffsets(Direction facing) {
        List<Vec3i> result = new ArrayList<>(this.slotOffsets.size());
        for (Vec3i offset : this.slotOffsets) {
            result.add(StructureRotation.rotate(offset, this.canonicalFront, facing, this.orientation));
        }
        return result;
    }

    /**
     * 网格构建器
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

        public Builder orientation(StructureOrientation orientation) {
            this.orientation = orientation;
            return this;
        }

        public Builder canonicalFront(Direction canonicalFront) {
            this.canonicalFront = canonicalFront;
            return this;
        }

        public Builder layer(String... rows) {
            this.grid.add(List.of(rows));
            return this;
        }

        public Builder where(char key, Block block) {
            return this.where(key, BlockStatePredicate.builder().of(block).build());
        }

        public Builder where(char key, Supplier<? extends Block> block) {
            return this.where(key, block.get());
        }

        public Builder where(char key, BlockStatePredicate predicate) {
            this.mapping.put(key, predicate);
            return this;
        }

        public Builder controller(char key, Block block) {
            return this.where(key, block);
        }

        public Builder slot(char key, StructureSlot slot) {
            this.slots.put(key, slot);
            return this;
        }

        public Builder slot(char key, List<String> roles, boolean allowAir) {
            return this.slot(key, new StructureSlot(roles, allowAir));
        }

        public Builder role(String id, StructureRole role) {
            this.roles.put(id, role);
            return this;
        }

        /**
         * 只接受一种端口种类的角色
         *
         * @param id   角色 ID
         * @param kind 端口种类
         * @param min  最少数量
         * @param max  最多数量，{@code -1} 表示不限
         * @return 自身
         */
        public Builder role(String id, PortKind kind, int min, int max) {
            return this.role(id, new StructureRole(List.of(kind), List.of(), min, max));
        }

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
