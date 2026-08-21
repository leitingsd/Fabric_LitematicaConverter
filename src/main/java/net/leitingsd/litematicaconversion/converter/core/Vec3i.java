package net.leitingsd.litematicaconversion.converter.core;

public class Vec3i {
    public static final Vec3i ZERO = new Vec3i(0, 0, 0);
    private int x;
    private int y;
    private int z;

    public Vec3i(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }

    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setZ(int z) { this.z = z; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vec3i)) return false;
        Vec3i vec3i = (Vec3i) o;
        return getX() == vec3i.getX() && getY() == vec3i.getY() && getZ() == vec3i.getZ();
    }

    @Override
    public int hashCode() {
        return (this.y + this.z * 31) * 31 + this.x;
    }

    @Override
    public String toString() {
        return String.format("(%d, %d, %d)", x, y, z);
    }
}
