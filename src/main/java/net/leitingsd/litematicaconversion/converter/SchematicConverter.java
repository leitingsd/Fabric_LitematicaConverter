package net.leitingsd.litematicaconversion.converter;

import net.leitingsd.litematicaconversion.converter.core.BlockPos;
import net.leitingsd.litematicaconversion.nbt.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class SchematicConverter {
    public static final int MINECRAFT_DATA_VERSION_1_20_4 = 3700;
    public static final int SCHEMATIC_VERSION_SUB = 1;

    private final Map<String, CompoundTag> blockContainers = new HashMap<>();
    private final Map<String, Map<BlockPos, CompoundTag>> tileEntities = new HashMap<>();
    private final Map<String, List<CompoundTag>> entities = new HashMap<>();
    private final Map<String, BlockPos> subRegionPositions = new HashMap<>();
    private final Map<String, BlockPos> subRegionSizes = new HashMap<>();
    private final Map<String, ListTag> pendingBlockTicks = new HashMap<>();
    private final Map<String, ListTag> pendingFluidTicks = new HashMap<>();

    private final SchematicMetadata metadata = new SchematicMetadata();
    private Path schematicFile;

    private int schematicVersion;

    public SchematicConverter(Path file) {
        this.schematicFile = file;
    }

    public static SchematicConverter createFromFile(Path file) {
        SchematicConverter schematic = new SchematicConverter(file);
        if (schematic.readFromFile()) {
            return schematic;
        }
        return null;
    }

    public static SchematicConverter createEmptySchematicFromExisting(SchematicConverter existing) {
        SchematicConverter newSchematic = new SchematicConverter(null);
        newSchematic.metadata.copyFrom(existing.metadata);
        newSchematic.metadata.setTimeModifiedToNow();
        return newSchematic;
    }

    public int getSchematicVersion() {
        return this.schematicVersion;
    }

    public boolean downgradeV7toV6Schematic(SchematicConverter v7Schematic) {
        for (String regionName : v7Schematic.subRegionPositions.keySet()) {
            if (v7Schematic.blockContainers.containsKey(regionName)) {
                this.blockContainers.put(regionName, v7Schematic.blockContainers.get(regionName).copy());
            }

            Map<BlockPos, CompoundTag> oldTiles = v7Schematic.tileEntities.get(regionName);
            this.tileEntities.put(regionName, this.downgradeTileEntities_to_1_20_4(oldTiles, v7Schematic.metadata.getMinecraftDataVersion()));

            List<CompoundTag> oldEntities = v7Schematic.entities.get(regionName);
            ListTag list = new ListTag();
            if (oldEntities != null) {
                for (CompoundTag tag : oldEntities) {
                    list.add(tag.copy());
                }
            }

            ListTag downgradedList = this.downgradeEntities_to_1_20_4(list, v7Schematic.metadata.getMinecraftDataVersion());

            List<CompoundTag> newEntitiesList = new ArrayList<>();
            for (int i = 0; i < downgradedList.size(); i++) {
                newEntitiesList.add(downgradedList.getCompound(i));
            }
            this.entities.put(regionName, newEntitiesList);

            if (v7Schematic.pendingBlockTicks.containsKey(regionName)) {
                this.pendingBlockTicks.put(regionName, v7Schematic.pendingBlockTicks.get(regionName).copy());
            }
            if (v7Schematic.pendingFluidTicks.containsKey(regionName)) {
                this.pendingFluidTicks.put(regionName, v7Schematic.pendingFluidTicks.get(regionName).copy());
            }
            this.subRegionPositions.put(regionName, v7Schematic.subRegionPositions.get(regionName));
            this.subRegionSizes.put(regionName, v7Schematic.subRegionSizes.get(regionName));
        }

        return true;
    }

    private Map<BlockPos, CompoundTag> downgradeTileEntities_to_1_20_4(Map<BlockPos, CompoundTag> oldTE, int minecraftDataVersion) {
        Map<BlockPos, CompoundTag> newTE = new HashMap<>();
        if (oldTE == null) return newTE;

        for (BlockPos key : oldTE.keySet()) {
            newTE.put(key, SchematicDowngradeConverter.downgradeBlockEntity_to_1_20_4(oldTE.get(key), minecraftDataVersion));
        }

        return newTE;
    }

    private ListTag downgradeEntities_to_1_20_4(ListTag oldEntitiesList, int minecraftDataVersion) {
        ListTag newEntitiesList = new ListTag();

        for (int i = 0; i < oldEntitiesList.size(); i++) {
            CompoundTag entityTag = oldEntitiesList.getCompound(i);
            entityTag = SchematicConversionMaps.fixEntityTypesFrom1_21_2(entityTag);
            newEntitiesList.add(SchematicDowngradeConverter.downgradeEntity_to_1_20_4(entityTag, minecraftDataVersion));
        }

        return newEntitiesList;
    }

    public boolean writeToFile(Path file, boolean v6Format) {
        try {
            CompoundTag root = new CompoundTag();
            root.put("", v6Format ? this.writeToNBT_v6() : this.writeToNBT());

            try (DataOutputStream out = new DataOutputStream(new GZIPOutputStream(Files.newOutputStream(file)))) {
                out.writeByte(Tag.TAG_COMPOUND);
                out.writeUTF("");
                (v6Format ? this.writeToNBT_v6() : this.writeToNBT()).write(out);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public CompoundTag writeToNBT_v6() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("MinecraftDataVersion", MINECRAFT_DATA_VERSION_1_20_4);
        nbt.putInt("Version", 6);
        nbt.putInt("SubVersion", SCHEMATIC_VERSION_SUB);
        nbt.put("Metadata", this.metadata.writeToNBT());
        nbt.put("Regions", this.writeSubRegionsToNBT());
        return nbt;
    }

    public CompoundTag writeToNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("MinecraftDataVersion", this.metadata.getMinecraftDataVersion());
        nbt.putInt("Version", 7);
        nbt.putInt("SubVersion", SCHEMATIC_VERSION_SUB);
        nbt.put("Metadata", this.metadata.writeToNBT());
        nbt.put("Regions", this.writeSubRegionsToNBT());
        return nbt;
    }

    private CompoundTag writeSubRegionsToNBT() {
        CompoundTag wrapper = new CompoundTag();

        for (String regionName : this.subRegionPositions.keySet()) {
            CompoundTag regionTag = new CompoundTag();

            BlockPos pos = this.subRegionPositions.get(regionName);
            regionTag.put("Position", createBlockPosTag(pos));
            BlockPos size = this.subRegionSizes.get(regionName);
            regionTag.put("Size", createBlockPosTag(size));

            if (this.blockContainers.containsKey(regionName)) {
                regionTag.merge(this.blockContainers.get(regionName));
            }

            ListTag tileList = new ListTag();
            if (this.tileEntities.containsKey(regionName)) {
                for (CompoundTag tag : this.tileEntities.get(regionName).values()) {
                    tileList.add(tag);
                }
            }
            regionTag.put("TileEntities", tileList);

            ListTag entityList = new ListTag();
            if (this.entities.containsKey(regionName)) {
                for (CompoundTag tag : this.entities.get(regionName)) {
                    entityList.add(tag);
                }
            }
            regionTag.put("Entities", entityList);

            if (this.pendingBlockTicks.containsKey(regionName)) {
                regionTag.put("PendingBlockTicks", this.pendingBlockTicks.get(regionName));
            }
            if (this.pendingFluidTicks.containsKey(regionName)) {
                regionTag.put("PendingFluidTicks", this.pendingFluidTicks.get(regionName));
            }

            wrapper.put(regionName, regionTag);
        }

        return wrapper;
    }

    private CompoundTag createBlockPosTag(BlockPos pos) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("x", pos.getX());
        tag.putInt("y", pos.getY());
        tag.putInt("z", pos.getZ());
        return tag;
    }

    private BlockPos readBlockPosTag(CompoundTag tag) {
        return new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
    }

    private boolean readFromFile() {
        try (DataInputStream in = new DataInputStream(new GZIPInputStream(Files.newInputStream(schematicFile)))) {
            int type = in.readByte();
            if (type != Tag.TAG_COMPOUND) return false;
            in.readUTF();

            CompoundTag root = readCompound(in);

            return readFromNBT(root);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private CompoundTag readCompound(DataInput in) throws IOException {
        CompoundTag compound = new CompoundTag();
        byte type;
        while ((type = in.readByte()) != Tag.TAG_END) {
            String name = in.readUTF();
            Tag tag = readTag(type, in);
            compound.put(name, tag);
        }
        return compound;
    }

    private Tag readTag(byte type, DataInput in) throws IOException {
        switch (type) {
            case Tag.TAG_BYTE: return new ByteTag(in.readByte());
            case Tag.TAG_SHORT: return new ShortTag(in.readShort());
            case Tag.TAG_INT: return new IntTag(in.readInt());
            case Tag.TAG_LONG: return new LongTag(in.readLong());
            case Tag.TAG_FLOAT: return new FloatTag(in.readFloat());
            case Tag.TAG_DOUBLE: return new DoubleTag(in.readDouble());
            case Tag.TAG_BYTE_ARRAY:
                int len = in.readInt();
                byte[] bytes = new byte[len];
                in.readFully(bytes);
                return new ByteArrayTag(bytes);
            case Tag.TAG_STRING: return new StringTag(in.readUTF());
            case Tag.TAG_LIST:
                byte elemType = in.readByte();
                int listLen = in.readInt();
                ListTag list = new ListTag();
                for (int i = 0; i < listLen; i++) {
                    list.add(readTag(elemType, in));
                }
                return list;
            case Tag.TAG_COMPOUND: return readCompound(in);
            case Tag.TAG_INT_ARRAY:
                int iLen = in.readInt();
                int[] ints = new int[iLen];
                for (int i = 0; i < iLen; i++) ints[i] = in.readInt();
                return new IntArrayTag(ints);
            case Tag.TAG_LONG_ARRAY:
                int lLen = in.readInt();
                long[] longs = new long[lLen];
                for (int i = 0; i < lLen; i++) longs[i] = in.readLong();
                return new LongArrayTag(longs);
            default: throw new IOException("Unknown tag type: " + type);
        }
    }

    private boolean readFromNBT(CompoundTag nbt) {
        if (!nbt.contains("Version")) return false;

        // 记录文件真实的 Version 字段，供转换前的版本判断使用
        this.schematicVersion = nbt.getInt("Version");

        this.metadata.readFromNBT(nbt.getCompound("Metadata"));
        if (nbt.contains("MinecraftDataVersion")) {
            this.metadata.setMinecraftDataVersion(nbt.getInt("MinecraftDataVersion"));
        }

        CompoundTag regions = nbt.getCompound("Regions");
        for (String name : regions.keySet()) {
            CompoundTag regionTag = regions.getCompound(name);

            this.subRegionPositions.put(name, readBlockPosTag(regionTag.getCompound("Position")));
            this.subRegionSizes.put(name, readBlockPosTag(regionTag.getCompound("Size")));

            CompoundTag container = new CompoundTag();
            if (regionTag.contains("BlockStatePalette")) container.put("BlockStatePalette", regionTag.get("BlockStatePalette").copy());
            if (regionTag.contains("BlockStates")) container.put("BlockStates", regionTag.get("BlockStates").copy());
            this.blockContainers.put(name, container);

            Map<BlockPos, CompoundTag> tiles = new HashMap<>();
            ListTag tileList = regionTag.getList("TileEntities", Tag.TAG_COMPOUND);
            for (int i = 0; i < tileList.size(); i++) {
                CompoundTag t = tileList.getCompound(i);
                BlockPos pos = readBlockPosTag(t);
                if (pos != null) tiles.put(pos, t);
            }
            this.tileEntities.put(name, tiles);

            List<CompoundTag> entityList = new ArrayList<>();
            ListTag eList = regionTag.getList("Entities", Tag.TAG_COMPOUND);
            for (int i = 0; i < eList.size(); i++) {
                entityList.add(eList.getCompound(i));
            }
            this.entities.put(name, entityList);

            if (regionTag.contains("PendingBlockTicks")) this.pendingBlockTicks.put(name, regionTag.getList("PendingBlockTicks", Tag.TAG_COMPOUND));
            if (regionTag.contains("PendingFluidTicks")) this.pendingFluidTicks.put(name, regionTag.getList("PendingFluidTicks", Tag.TAG_COMPOUND));
        }

        return true;
    }
}