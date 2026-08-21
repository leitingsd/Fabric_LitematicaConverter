package net.leitingsd.litematicaconversion.converter.core;

public class BlockPos extends Vec3i {
    public static final BlockPos ZERO = new BlockPos(0, 0, 0);

    public BlockPos(int x, int y, int z) {
        super(x, y, z);
    }

    public BlockPos(Vec3i source) {
        super(source.getX(), source.getY(), source.getZ());
    }

    public BlockPos offset(int dx, int dy, int dz) {
        return new BlockPos(this.getX() + dx, this.getY() + dy, this.getZ() + dz);
    }

    public BlockPos subtract(BlockPos other) {
        return new BlockPos(this.getX() - other.getX(), this.getY() - other.getY(), this.getZ() - other.getZ());
    }
}
