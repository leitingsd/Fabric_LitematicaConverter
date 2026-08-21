package net.leitingsd.litematicaconversion.converter;

import net.leitingsd.litematicaconversion.nbt.CompoundTag;
import net.leitingsd.litematicaconversion.nbt.ListTag;
import net.leitingsd.litematicaconversion.nbt.Tag;

public class SchematicConversionMaps {

    public static CompoundTag fixEntityTypesFrom1_21_2(CompoundTag nbt) {
        if (!nbt.contains("id")) {
            return nbt;
        }

        if (nbt.contains("Items")) {
            ListTag items = fixItemsTag(nbt.getList("Items", Tag.TAG_COMPOUND));
            nbt.put("Items", items);
        }

        String id = nbt.getString("id");
        String newId = null;
        String type = "";
        boolean boatFix = false;

        switch (id) {
            case "minecraft:oak_boat":
            case "minecraft:pale_oak_boat":
                newId = "boat";
                type = "oak";
                boatFix = true;
                break;
            case "minecraft:spruce_boat":
                newId = "boat";
                type = "spruce";
                boatFix = true;
                break;
            case "minecraft:birch_boat":
                newId = "boat";
                type = "birch";
                boatFix = true;
                break;
            case "minecraft:jungle_boat":
                newId = "boat";
                type = "jungle";
                boatFix = true;
                break;
            case "minecraft:acacia_boat":
                newId = "boat";
                type = "acacia";
                boatFix = true;
                break;
            case "minecraft:cherry_boat":
                newId = "boat";
                type = "cherry";
                boatFix = true;
                break;
            case "minecraft:dark_oak_boat":
                newId = "boat";
                type = "dark_oak";
                boatFix = true;
                break;
            case "minecraft:mangrove_boat":
                newId = "boat";
                type = "mangrove";
                boatFix = true;
                break;
            case "minecraft:bamboo_raft":
                newId = "boat";
                type = "bamboo";
                boatFix = true;
                break;
            case "minecraft:oak_chest_boat":
            case "minecraft:pale_oak_chest_boat":
                newId = "chest_boat";
                type = "oak";
                boatFix = true;
                break;
            case "minecraft:spruce_chest_boat":
                newId = "chest_boat";
                type = "spruce";
                boatFix = true;
                break;
            case "minecraft:birch_chest_boat":
                newId = "chest_boat";
                type = "birch";
                boatFix = true;
                break;
            case "minecraft:jungle_chest_boat":
                newId = "chest_boat";
                type = "jungle";
                boatFix = true;
                break;
            case "minecraft:acacia_chest_boat":
                newId = "chest_boat";
                type = "acacia";
                boatFix = true;
                break;
            case "minecraft:cherry_chest_boat":
                newId = "chest_boat";
                type = "cherry";
                boatFix = true;
                break;
            case "minecraft:dark_oak_chest_boat":
                newId = "chest_boat";
                type = "dark_oak";
                boatFix = true;
                break;
            case "minecraft:mangrove_chest_boat":
                newId = "chest_boat";
                type = "mangrove";
                boatFix = true;
                break;
            case "minecraft:bamboo_chest_raft":
                newId = "chest_boat";
                type = "bamboo";
                boatFix = true;
                break;
            default:
                if (id.contains("_chest_boat")) {
                    newId = "chest_boat";
                    type = "oak";
                    boatFix = true;
                } else if (id.contains("_boat")) {
                    newId = "boat";
                    type = "oak";
                    boatFix = true;
                }
                break;
        }

        if (newId != null) {
            nbt.putString("id", "minecraft:" + newId);
        }

        if (boatFix) {
            nbt.putString("Type", type);
        }

        return nbt;
    }

    private static ListTag fixItemsTag(ListTag items) {
        ListTag newList = new ListTag();

        for (int i = 0; i < items.size(); i++) {
            CompoundTag itemEntry = fixItemTypesFrom1_21_2(items.getCompound(i));

            if (itemEntry.contains("tag")) {
                CompoundTag tag = itemEntry.getCompound("tag");

                if (tag.contains("BlockEntityTag")) {
                    CompoundTag entityEntry = tag.getCompound("BlockEntityTag");

                    if (entityEntry.contains("Items")) {
                        ListTag nestedItems = fixItemsTag(entityEntry.getList("Items", Tag.TAG_COMPOUND));
                        entityEntry.put("Items", nestedItems);
                    }

                    tag.put("BlockEntityTag", entityEntry);
                }

                itemEntry.put("tag", tag);
            }

            newList.add(itemEntry);
        }

        return newList;
    }

    private static CompoundTag fixItemTypesFrom1_21_2(CompoundTag nbt) {
        if (!nbt.contains("id")) {
            return nbt;
        }

        String id = nbt.getString("id");
        String newId = null;

        switch (id) {
            case "minecraft:pale_oak_boat":
                newId = "oak_boat";
                break;
            case "minecraft:pale_oak_chest_boat":
                newId = "oak_chest_boat";
                break;
        }

        if (newId != null) {
            nbt.putString("id", "minecraft:" + newId);
        }

        return nbt;
    }

    public static CompoundTag checkForIdTag(CompoundTag tags) {
        if (tags.contains("id")) {
            return tags;
        }

        if (tags.contains("Id")) {
            tags.putString("id", tags.getString("Id"));
            return tags;
        }

        if (tags.contains("Bees") || tags.contains("bees")) {
            tags.putString("id", "minecraft:beehive");
        } else if (tags.contains("TransferCooldown") && tags.contains("Items")) {
            tags.putString("id", "minecraft:hopper");
        } else if (tags.contains("SkullOwner")) {
            tags.putString("id", "minecraft:skull");
        } else if (tags.contains("Patterns") || tags.contains("patterns")) {
            tags.putString("id", "minecraft:banner");
        } else if (tags.contains("Sherds") || tags.contains("sherds")) {
            tags.putString("id", "minecraft:decorated_pot");
        } else if (tags.contains("last_interacted_slot") && tags.contains("Items")) {
            tags.putString("id", "minecraft:chiseled_bookshelf");
        } else if (tags.contains("CookTime") && tags.contains("Items")) {
            tags.putString("id", "minecraft:furnace");
        } else if (tags.contains("RecordItem")) {
            tags.putString("id", "minecraft:jukebox");
        } else if (tags.contains("Book") || tags.contains("book")) {
            tags.putString("id", "minecraft:lectern");
        } else if (tags.contains("front_text")) {
            tags.putString("id", "minecraft:sign");
        } else if (tags.contains("BrewTime") || tags.contains("Fuel")) {
            tags.putString("id", "minecraft:brewing_stand");
        } else if ((tags.contains("LootTable") && tags.contains("LootTableSeed")) || (tags.contains("hit_direction") || tags.contains("item"))) {
            tags.putString("id", "minecraft:suspicious_sand");
        } else if (tags.contains("SpawnData") || tags.contains("SpawnPotentials")) {
            tags.putString("id", "minecraft:spawner");
        } else if (tags.contains("normal_config")) {
            tags.putString("id", "minecraft:trial_spawner");
        } else if (tags.contains("shared_data")) {
            tags.putString("id", "minecraft:vault");
        } else if (tags.contains("pool") && tags.contains("final_state") && tags.contains("placement_priority")) {
            tags.putString("id", "minecraft:jigsaw");
        } else if (tags.contains("author") && tags.contains("metadata") && tags.contains("showboundingbox")) {
            tags.putString("id", "minecraft:structure_block");
        } else if (tags.contains("ExactTeleport") && tags.contains("Age")) {
            tags.putString("id", "minecraft:end_gateway");
        } else if (tags.contains("Items")) {
            tags.putString("id", "minecraft:chest");
        } else if (tags.contains("last_vibration_frequency") || tags.contains("listener")) {
            tags.putString("id", "minecraft:sculk_sensor");
        } else if (tags.contains("warning_level") || tags.contains("listener")) {
            tags.putString("id", "minecraft:sculk_shrieker");
        } else if (tags.contains("OutputSignal")) {
            tags.putString("id", "minecraft:comparator");
        } else if (tags.contains("facing") || tags.contains("extending")) {
            tags.putString("id", "minecraft:piston");
        } else if (tags.contains("x") && tags.contains("y") && tags.contains("z")) {
            tags.putString("id", "minecraft:piston");
        }

        if (tags.contains("Items")) {
            ListTag items = fixItemsTag(tags.getList("Items", Tag.TAG_COMPOUND));
            tags.put("Items", items);
        }

        return tags;
    }
}
