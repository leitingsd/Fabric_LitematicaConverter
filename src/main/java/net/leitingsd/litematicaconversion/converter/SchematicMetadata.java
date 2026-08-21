package net.leitingsd.litematicaconversion.converter;

import net.leitingsd.litematicaconversion.converter.core.Vec3i;
import net.leitingsd.litematicaconversion.nbt.CompoundTag;
import net.leitingsd.litematicaconversion.nbt.Tag;

public class SchematicMetadata {
    private String name = "?";
    private String author = "?";
    private String description = "";
    private int regionCount;
    private int totalVolume;
    private int totalBlocks;
    private long timeCreated;
    private long timeModified;
    private Vec3i enclosingSize = Vec3i.ZERO;
    private int minecraftDataVersion;

    public void copyFrom(SchematicMetadata other) {
        this.name = other.name;
        this.author = other.author;
        this.description = other.description;
        this.regionCount = other.regionCount;
        this.totalVolume = other.totalVolume;
        this.totalBlocks = other.totalBlocks;
        this.timeCreated = other.timeCreated;
        this.timeModified = other.timeModified;
        this.enclosingSize = other.enclosingSize;
        this.minecraftDataVersion = other.minecraftDataVersion;
    }

    public void setTimeModifiedToNow() {
        this.timeModified = System.currentTimeMillis();
    }

    public int getMinecraftDataVersion() {
        return this.minecraftDataVersion;
    }

    public void setMinecraftDataVersion(int version) {
        this.minecraftDataVersion = version;
    }

    public CompoundTag writeToNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("Name", this.name);
        nbt.putString("Author", this.author);
        nbt.putString("Description", this.description);
        nbt.putInt("RegionCount", this.regionCount);
        nbt.putInt("TotalVolume", this.totalVolume);
        nbt.putInt("TotalBlocks", this.totalBlocks);
        nbt.putLong("TimeCreated", this.timeCreated);
        nbt.putLong("TimeModified", this.timeModified);

        CompoundTag sizeTag = new CompoundTag();
        sizeTag.putInt("x", this.enclosingSize.getX());
        sizeTag.putInt("y", this.enclosingSize.getY());
        sizeTag.putInt("z", this.enclosingSize.getZ());
        nbt.put("EnclosingSize", sizeTag);

        return nbt;
    }

    public void readFromNBT(CompoundTag nbt) {
        this.name = nbt.getStringOr("Name", "?");
        this.author = nbt.getStringOr("Author", "?");
        this.description = nbt.getStringOr("Description", "");
        this.regionCount = nbt.getIntOr("RegionCount", 0);
        this.totalVolume = nbt.getIntOr("TotalVolume", 0);
        this.totalBlocks = nbt.getIntOr("TotalBlocks", 0);
        this.timeCreated = nbt.getLongOr("TimeCreated", -1L);
        this.timeModified = nbt.getLongOr("TimeModified", -1L);

        if (nbt.contains("MinecraftDataVersion", Tag.TAG_INT)) {
            this.minecraftDataVersion = nbt.getInt("MinecraftDataVersion");
        }

        if (nbt.contains("EnclosingSize", Tag.TAG_COMPOUND)) {
            CompoundTag size = nbt.getCompound("EnclosingSize");
            this.enclosingSize = new Vec3i(size.getInt("x"), size.getInt("y"), size.getInt("z"));
        }
    }
}
