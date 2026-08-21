package net.leitingsd.litematicaconversion.nbt;

import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListTag implements Tag {
    private final List<Tag> list = new ArrayList<>();
    private byte type = TAG_END;

    @Override
    public void write(DataOutput output) throws IOException {
        if (list.isEmpty()) {
            type = TAG_END;
        } else {
            type = list.get(0).getId();
        }
        output.writeByte(type);
        output.writeInt(list.size());
        for (Tag tag : list) {
            tag.write(output);
        }
    }

    @Override
    public byte getId() {
        return TAG_LIST;
    }

    @Override
    public ListTag copy() {
        ListTag copy = new ListTag();
        copy.type = this.type;
        for (Tag tag : list) {
            copy.list.add(tag.copy());
        }
        return copy;
    }

    public void add(Tag tag) {
        if (tag.getId() == TAG_END) {
            return;
        }
        if (type == TAG_END) {
            type = tag.getId();
        } else if (type != tag.getId()) {
            return; // Ignore mismatching types
        }
        list.add(tag);
    }

    public void add(int index, Tag tag) {
        if (tag.getId() == TAG_END) {
            return;
        }
        if (type == TAG_END) {
            type = tag.getId();
        } else if (type != tag.getId()) {
            return;
        }
        list.add(index, tag);
    }

    public void set(int index, Tag tag) {
        if (tag.getId() == TAG_END) {
            return;
        }
        if (type == TAG_END) {
            type = tag.getId();
        } else if (type != tag.getId()) {
            return;
        }
        list.set(index, tag);
    }

    public Tag get(int index) {
        return list.get(index);
    }

    public int size() {
        return list.size();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public byte getElementType() {
        return type;
    }

    public CompoundTag getCompound(int index) {
        Tag tag = list.get(index);
        return tag instanceof CompoundTag ? (CompoundTag) tag : new CompoundTag();
    }

    public CompoundTag getCompoundOrEmpty(int index) {
        return getCompound(index);
    }

    public String getString(int index) {
        Tag tag = list.get(index);
        return tag instanceof StringTag ? ((StringTag) tag).getValue() : "";
    }

    public void addAll(List<CompoundTag> tags) {
        for (CompoundTag tag : tags) {
            add(tag);
        }
    }
}
