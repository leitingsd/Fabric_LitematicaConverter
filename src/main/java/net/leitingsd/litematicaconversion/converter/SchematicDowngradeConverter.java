package net.leitingsd.litematicaconversion.converter;

import net.leitingsd.litematicaconversion.converter.core.BlockPos;
import net.leitingsd.litematicaconversion.nbt.*;

public class SchematicDowngradeConverter {

    public static CompoundTag downgradeEntity_to_1_20_4(CompoundTag oldEntity, int minecraftDataVersion) {
        CompoundTag newEntity = new CompoundTag();

        if (!oldEntity.contains("id")) {
            return oldEntity;
        }

        for (String key : oldEntity.keySet()) {
            switch (key) {
                case "x": newEntity.putInt("x", oldEntity.getInt("x")); break;
                case "y": newEntity.putInt("y", oldEntity.getInt("y")); break;
                case "z": newEntity.putInt("z", oldEntity.getInt("z")); break;
                case "id": newEntity.putString("id", oldEntity.getString("id")); break;

                case "attributes": newEntity.put("Attributes", processAttributes(oldEntity.get(key), minecraftDataVersion)); break;

                case "Items": newEntity.put("Items", processItemsList(oldEntity.getList("Items", Tag.TAG_COMPOUND), minecraftDataVersion)); break;
                case "ArmorItems": newEntity.put("ArmorItems", processItemsList(oldEntity.getList("ArmorItems", Tag.TAG_COMPOUND), minecraftDataVersion)); break;
                case "HandItems": newEntity.put("HandItems", processItemsList(oldEntity.getList("HandItems", Tag.TAG_COMPOUND), minecraftDataVersion)); break;
                case "Item": newEntity.put("Item", processSingleItem(oldEntity.get("Item"), minecraftDataVersion)); break;
                case "Inventory": newEntity.put("Inventory", processItemsList(oldEntity.getList("Inventory", Tag.TAG_COMPOUND), minecraftDataVersion)); break;
                case "SaddleItem": newEntity.put("SaddleItem", processSingleItem(oldEntity.get("SaddleItem"), minecraftDataVersion)); break;

                case "Offers": newEntity.put("Offers", processOffers(oldEntity.getCompound("Offers"), minecraftDataVersion)); break;

                case "leash": processLeash(oldEntity.get("leash"), newEntity); break;

                case "equipment": newEntity.merge(processEntityEquipment(oldEntity.get("equipment"), minecraftDataVersion)); break;
                case "drop_chances": newEntity.merge(processEntityDropChances(oldEntity.get("drop_chances"))); break;
                case "fall_distance": newEntity.putFloat("FallDistance", oldEntity.getFloat("fall_distance")); break;

                case "block_pos": processBlockPosTag(readBlockPosFromIntArray(oldEntity, key), "Tile", newEntity); break;
                case "home_pos": processBlockPosTag(readBlockPosFromIntArray(oldEntity, key), "HomePos", newEntity); break;
                case "sleeping_pos": processBlockPosTag(readBlockPosFromIntArray(oldEntity, key), "Sleeping", newEntity); break;

                case "has_egg": newEntity.putBoolean("HasEgg", oldEntity.getBoolean("has_egg")); break;
                case "life_ticks": newEntity.putInt("LifeTicks", oldEntity.getInt("life_ticks")); break;

                default: newEntity.put(key, oldEntity.get(key).copy()); break;
            }
        }

        return newEntity;
    }

    public static CompoundTag downgradeBlockEntity_to_1_20_4(CompoundTag oldTE, int minecraftDataVersion) {
        CompoundTag newTE = new CompoundTag();
        oldTE = SchematicConversionMaps.checkForIdTag(oldTE);

        for (String key : oldTE.keySet()) {
            switch (key) {
                case "x": newTE.putInt("x", oldTE.getInt("x")); break;
                case "y": newTE.putInt("y", oldTE.getInt("y")); break;
                case "z": newTE.putInt("z", oldTE.getInt("z")); break;
                case "id": newTE.putString("id", oldTE.getString("id")); break;

                case "Items": newTE.put("Items", processItemsList(oldTE.getList("Items", Tag.TAG_COMPOUND), minecraftDataVersion)); break;
                case "patterns": newTE.put("Patterns", processBannerPatterns(oldTE.getList("patterns", Tag.TAG_COMPOUND))); break;
                case "profile": newTE.put("SkullOwner", processSkullProfile(oldTE.getCompound("profile"), newTE, minecraftDataVersion)); break;
                case "bees": newTE.put("Bees", processBeesTag(oldTE.get("bees"), minecraftDataVersion)); break;
                case "item": newTE.put("item", processSingleItem(oldTE.get("item"), minecraftDataVersion)); break;
                case "RecordItem": newTE.put("RecordItem", processSingleItem(oldTE.get("RecordItem"), minecraftDataVersion)); break;
                case "Book": newTE.put("Book", processSingleItem(oldTE.get("Book"), minecraftDataVersion)); break;
                case "custom_name": newTE.putString("CustomName", processCustomNameTag(oldTE, key)); break;
                case "components":
                    CompoundTag components = oldTE.getCompound("components");
                    if (components.contains("minecraft:item_name", Tag.TAG_STRING)) {
                        newTE.putString("CustomName", components.getString("minecraft:item_name"));
                    }
                    break;

                default: newTE.put(key, oldTE.get(key).copy()); break;
            }
        }

        return newTE;
    }

    // 旗帜图案处理
    private static ListTag processBannerPatterns(ListTag oldPatterns) {
        ListTag newPatterns = new ListTag();
        for (int i = 0; i < oldPatterns.size(); i++) {
            CompoundTag oldPattern = oldPatterns.getCompound(i);
            CompoundTag newPattern = new CompoundTag();

            String colorName = oldPattern.getString("color");
            String patternId = oldPattern.getString("pattern");

            newPattern.putInt("Color", getColorId(colorName));
            newPattern.putString("Pattern", getPatternShortCode(patternId));

            newPatterns.add(newPattern);
        }
        return newPatterns;
    }

    private static int getColorId(String colorName) {
        switch (colorName) {
            case "white": return 0;
            case "orange": return 1;
            case "magenta": return 2;
            case "light_blue": return 3;
            case "yellow": return 4;
            case "lime": return 5;
            case "pink": return 6;
            case "gray": return 7;
            case "light_gray": return 8;
            case "cyan": return 9;
            case "purple": return 10;
            case "blue": return 11;
            case "brown": return 12;
            case "green": return 13;
            case "red": return 14;
            case "black": return 15;
            default: return 0;
        }
    }

    private static String getPatternShortCode(String patternId) {
        if (patternId.startsWith("minecraft:")) {
            patternId = patternId.substring(10);
        }

        switch (patternId) {
            case "base": return "b";
            case "square_bottom_left": return "bl";
            case "square_bottom_right": return "br";
            case "square_top_left": return "tl";
            case "square_top_right": return "tr";
            case "stripe_bottom": return "bs";
            case "stripe_top": return "ts";
            case "stripe_left": return "ls";
            case "stripe_right": return "rs";
            case "stripe_center": return "cs";
            case "stripe_middle": return "ms";
            case "stripe_downright": return "drs";
            case "stripe_downleft": return "dls";
            case "small_stripes": return "ss";
            case "cross": return "cr";
            case "straight_cross": return "sc";
            case "triangle_bottom": return "bt";
            case "triangle_top": return "tt";
            case "triangles_bottom": return "bts";
            case "triangles_top": return "tts";
            case "diagonal_left": return "ld";
            case "diagonal_up_right": return "rd";
            case "diagonal_up_left": return "lud";
            case "diagonal_right": return "rud";
            case "circle": return "mc";
            case "rhombus": return "mr";
            case "half_vertical": return "vh";
            case "half_horizontal": return "hh";
            case "half_vertical_right": return "vhr";
            case "half_horizontal_bottom": return "hhb";
            case "border": return "bo";
            case "curly_border": return "cbo";
            case "gradient": return "gra";
            case "gradient_up": return "gru";
            case "bricks": return "bri";
            case "globe": return "glb";
            case "creeper": return "cre";
            case "skull": return "sku";
            case "flower": return "flo";
            case "mojang": return "moj";
            case "piglin": return "pig";
            case "flow": return "flw";
            case "guster": return "gus";
            default: return "b";
        }
    }

    // 头颅处理
    private static CompoundTag processSkullProfile(CompoundTag oldProfile, CompoundTag parent, int version) {
        CompoundTag newProfile = new CompoundTag();

        if (oldProfile.contains("id", Tag.TAG_INT_ARRAY)) {
            newProfile.putIntArray("Id", oldProfile.getIntArray("id"));
        }

        if (oldProfile.contains("name", Tag.TAG_STRING)) {
            newProfile.putString("Name", oldProfile.getString("name"));
        }

        if (oldProfile.contains("properties", Tag.TAG_LIST)) {
            ListTag oldProps = oldProfile.getList("properties", Tag.TAG_COMPOUND);
            CompoundTag newProps = new CompoundTag();
            ListTag textures = new ListTag();

            for (int i = 0; i < oldProps.size(); i++) {
                CompoundTag prop = oldProps.getCompound(i);
                if (prop.getString("name").equals("textures")) {
                    CompoundTag newTexture = new CompoundTag();
                    if (prop.contains("signature")) newTexture.putString("Signature", prop.getString("signature"));
                    if (prop.contains("value")) newTexture.putString("Value", prop.getString("value"));
                    textures.add(newTexture);
                }
            }

            if (!textures.isEmpty()) {
                newProps.put("textures", textures);
            }
            newProfile.put("Properties", newProps);
        }

        return newProfile;
    }

    // 物品处理
    private static ListTag processItemsList(ListTag oldItems, int version) {
        ListTag newItems = new ListTag();
        for (int i = 0; i < oldItems.size(); i++) {
            newItems.add(processSingleItem(oldItems.get(i), version));
        }
        return newItems;
    }

    private static Tag processSingleItem(Tag itemEntry, int version) {
        if (!(itemEntry instanceof CompoundTag)) return itemEntry.copy();

        CompoundTag oldItem = (CompoundTag) itemEntry;
        CompoundTag newItem = new CompoundTag();

        if (oldItem.contains("id", Tag.TAG_STRING)) newItem.putString("id", oldItem.getString("id"));
        if (oldItem.contains("Slot", Tag.TAG_BYTE)) newItem.putByte("Slot", oldItem.getByte("Slot"));
        if (oldItem.contains("Count", Tag.TAG_BYTE)) newItem.putByte("Count", oldItem.getByte("Count"));
        else if (oldItem.contains("count", Tag.TAG_INT)) newItem.putByte("Count", (byte)oldItem.getInt("count"));

        CompoundTag finalTag = new CompoundTag();

        if (oldItem.contains("tag", Tag.TAG_COMPOUND)) {
            finalTag.merge(oldItem.getCompound("tag"));
        }

        if (oldItem.contains("components", Tag.TAG_COMPOUND)) {
            CompoundTag componentsTag = processComponentsTag(oldItem.getCompound("components"), newItem.getString("id"), version);
            finalTag.merge(componentsTag);
        }

        if (finalTag.contains("BlockEntityTag", Tag.TAG_COMPOUND)) {
            CompoundTag beTag = finalTag.getCompound("BlockEntityTag");
            if (beTag.contains("Items", Tag.TAG_LIST)) {
                beTag.put("Items", processItemsList(beTag.getList("Items", Tag.TAG_COMPOUND), version));
            }
        }

        if (!finalTag.isEmpty()) {
            newItem.put("tag", finalTag);
        }

        return newItem;
    }

    private static CompoundTag processComponentsTag(CompoundTag components, String itemId, int version) {
        CompoundTag outTag = new CompoundTag();

        for (String key : components.keySet()) {
            switch (key) {
                case "minecraft:custom_name":
                    String json = components.getString(key);
                    CompoundTag display = outTag.getCompoundOrEmpty("display");
                    display.putString("Name", json);
                    outTag.put("display", display);
                    break;
                case "minecraft:item_name":
                    String itemNameJson = components.getString(key);
                    CompoundTag displayItem = outTag.getCompoundOrEmpty("display");
                    displayItem.putString("Name", itemNameJson);
                    outTag.put("display", displayItem);
                    break;
                case "minecraft:damage":
                    outTag.putInt("Damage", components.getInt(key));
                    break;
                case "minecraft:enchantments":
                    outTag.put("Enchantments", processEnchantments(components.get(key)));
                    break;
                case "minecraft:dyed_color":
                    CompoundTag displayColor = outTag.getCompoundOrEmpty("display");
                    displayColor.putInt("color", components.getCompound(key).getInt("rgb"));
                    outTag.put("display", displayColor);
                    break;
                case "minecraft:container":
                    ListTag containerItems = components.getList(key, Tag.TAG_COMPOUND);
                    ListTag flatItems = new ListTag();

                    for (int i = 0; i < containerItems.size(); i++) {
                        CompoundTag entry = containerItems.getCompound(i);
                        if (entry.contains("item", Tag.TAG_COMPOUND)) {
                            CompoundTag itemData = entry.getCompound("item");
                            CompoundTag processedItem = (CompoundTag) processSingleItem(itemData, version);

                            if (entry.contains("slot", Tag.TAG_INT)) {
                                processedItem.putByte("Slot", (byte) entry.getInt("slot"));
                            }

                            flatItems.add(processedItem);
                        }
                    }

                    CompoundTag blockEntityTag = outTag.getCompoundOrEmpty("BlockEntityTag");
                    blockEntityTag.put("Items", flatItems);
                    outTag.put("BlockEntityTag", blockEntityTag);
                    break;
                case "minecraft:block_entity_data":
                    CompoundTag beData = components.getCompound(key);
                    CompoundTag beTag = outTag.getCompoundOrEmpty("BlockEntityTag");
                    beTag.merge(downgradeBlockEntity_to_1_20_4(beData, version));
                    outTag.put("BlockEntityTag", beTag);
                    break;

                case "minecraft:writable_book_content":
                    CompoundTag writableContent = components.getCompound(key);
                    if (writableContent.contains("pages", Tag.TAG_LIST)) {
                        outTag.put("pages", convertBookPages(writableContent.getList("pages", Tag.TAG_COMPOUND)));
                    }
                    break;
                case "minecraft:written_book_content":
                    CompoundTag writtenContent = components.getCompound(key);
                    if (writtenContent.contains("author", Tag.TAG_STRING)) {
                        outTag.putString("author", writtenContent.getString("author"));
                    }

                    String title = "";
                    if (writtenContent.contains("title", Tag.TAG_COMPOUND)) {
                        title = writtenContent.getCompound("title").getString("raw");
                    } else if (writtenContent.contains("title", Tag.TAG_STRING)) {
                        title = writtenContent.getString("title");
                    }

                    if (!title.isEmpty()) {
                        outTag.putString("title", title);
                        outTag.putString("filtered_title", title);
                    }

                    if (writtenContent.contains("generation", Tag.TAG_INT)) {
                        outTag.putInt("generation", writtenContent.getInt("generation"));
                    }
                    if (writtenContent.contains("resolved", Tag.TAG_BYTE)) {
                        outTag.putBoolean("resolved", writtenContent.getBoolean("resolved"));
                    }
                    if (writtenContent.contains("pages", Tag.TAG_LIST)) {
                        outTag.put("pages", convertBookPages(writtenContent.getList("pages", Tag.TAG_COMPOUND)));
                    }
                    break;
            }
        }
        return outTag;
    }

    private static ListTag convertBookPages(ListTag componentPages) {
        ListTag stringPages = new ListTag();
        for (int i = 0; i < componentPages.size(); i++) {
            Tag pageEntry = componentPages.get(i);
            if (pageEntry instanceof CompoundTag) {
                CompoundTag page = (CompoundTag) pageEntry;
                if (page.contains("raw", Tag.TAG_STRING)) {
                    stringPages.add(new StringTag(page.getString("raw")));
                } else if (page.contains("text", Tag.TAG_STRING)) {
                    stringPages.add(new StringTag(page.getString("text")));
                } else {
                    stringPages.add(new StringTag("{\"text\":\"\"}"));
                }
            } else if (pageEntry instanceof StringTag) {
                stringPages.add(pageEntry.copy());
            }
        }
        return stringPages;
    }

    private static void processBlockEntityTag(CompoundTag tag, int version) {
        if (tag.contains("BlockEntityTag", Tag.TAG_COMPOUND)) {
            CompoundTag beTag = tag.getCompound("BlockEntityTag");
            if (beTag.contains("Items", Tag.TAG_LIST)) {
                beTag.put("Items", processItemsList(beTag.getList("Items", Tag.TAG_COMPOUND), version));
            }
        }
    }

    private static CompoundTag processOffers(CompoundTag offers, int version) {
        CompoundTag newOffers = new CompoundTag();
        if (offers.contains("Recipes", Tag.TAG_LIST)) {
            ListTag oldRecipes = offers.getList("Recipes", Tag.TAG_COMPOUND);
            ListTag newRecipes = new ListTag();

            for (int i = 0; i < oldRecipes.size(); i++) {
                CompoundTag oldRecipe = oldRecipes.getCompound(i);
                CompoundTag newRecipe = oldRecipe.copy();

                if (oldRecipe.contains("buy", Tag.TAG_COMPOUND)) newRecipe.put("buy", processSingleItem(oldRecipe.get("buy"), version));
                if (oldRecipe.contains("buyB", Tag.TAG_COMPOUND)) newRecipe.put("buyB", processSingleItem(oldRecipe.get("buyB"), version));
                if (oldRecipe.contains("sell", Tag.TAG_COMPOUND)) newRecipe.put("sell", processSingleItem(oldRecipe.get("sell"), version));

                newRecipes.add(newRecipe);
            }
            newOffers.put("Recipes", newRecipes);
        }
        return newOffers;
    }

    private static void processLeash(Tag leashData, CompoundTag newEntity) {
        if (leashData instanceof CompoundTag) {
            CompoundTag tag = (CompoundTag) leashData;
            CompoundTag newLeash = new CompoundTag();

            if (tag.contains("UUID", Tag.TAG_INT_ARRAY)) {
                newLeash.putIntArray("UUID", tag.getIntArray("UUID"));
            } else if (tag.contains("X") && tag.contains("Y") && tag.contains("Z")) {
                newLeash.putInt("X", tag.getInt("X"));
                newLeash.putInt("Y", tag.getInt("Y"));
                newLeash.putInt("Z", tag.getInt("Z"));
            }

            newEntity.put("Leash", newLeash);
        }
    }

    private static void processBlockPosTag(BlockPos oldPos, String prefix, CompoundTag newTags) {
        if (oldPos != null) {
            newTags.putInt(prefix + "X", oldPos.getX());
            newTags.putInt(prefix + "Y", oldPos.getY());
            newTags.putInt(prefix + "Z", oldPos.getZ());
        }
    }

    private static BlockPos readBlockPosFromIntArray(CompoundTag tag, String key) {
        if (tag.contains(key, Tag.TAG_INT_ARRAY)) {
            int[] arr = tag.getIntArray(key);
            if (arr.length == 3) return new BlockPos(arr[0], arr[1], arr[2]);
        }
        return null;
    }

    private static Tag processAttributes(Tag attrib, int version) { return attrib.copy(); }
    private static Tag processEnchantments(Tag enchants) {
        if (!(enchants instanceof CompoundTag)) return enchants.copy();
        CompoundTag levels = ((CompoundTag)enchants).getCompound("levels");
        ListTag list = new ListTag();
        for (String id : levels.keySet()) {
            CompoundTag entry = new CompoundTag();
            entry.putString("id", id);
            entry.putShort("lvl", (short)levels.getInt(id));
            list.add(entry);
        }
        return list;
    }
    private static CompoundTag processEntityEquipment(Tag equipment, int version) { return new CompoundTag(); }
    private static CompoundTag processEntityDropChances(Tag chances) { return new CompoundTag(); }
    private static Tag processBeesTag(Tag bees, int version) { return bees.copy(); }
    private static Tag processDecoratedPot(Tag pot, int version) { return pot.copy(); }
    private static Tag processRecordItem(Tag record, int version) { return processSingleItem(record, version); }
    private static Tag processBookTag(Tag book, int version) { return processSingleItem(book, version); }
    private static String processCustomNameTag(CompoundTag tag, String key) { return tag.getString(key); }
}
