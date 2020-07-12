package ptgms.industries.fears;

import org.bukkit.entity.EntityType;

public enum ScaryMobs {
    CREEPER(EntityType.CREEPER.ordinal()),
    SKELETON(EntityType.SKELETON.ordinal()),
    SPIDER(EntityType.SPIDER.ordinal()),
    CAVE_SPIDER(EntityType.CAVE_SPIDER.ordinal()),
    PILLAGER(EntityType.PILLAGER.ordinal()),
    ENDERMAN(EntityType.ENDERMAN.ordinal()),
    WITCH(EntityType.WITCH.ordinal()),
    STRAY(EntityType.STRAY.ordinal()),
    SLIME(EntityType.SLIME.ordinal()),
    ZOMBIE(EntityType.ZOMBIE.ordinal()),
    NONE(1000);

    public final int value;
    ScaryMobs(int label) {
        this.value = label;
    }

    public int getValue() {
        return value;
    }

    //if we want to preserve a switch case here, this must be updated on new releases as ordinals change.
    public static ScaryMobs fromInt(int x)
    {
        switch(x)
        {
            case 45:
                return ScaryMobs.CREEPER;
            case 46:
                return ScaryMobs.SKELETON;
            case 47:
                return ScaryMobs.SPIDER;
            case 54:
                return ScaryMobs.CAVE_SPIDER;
            case 94:
                return ScaryMobs.PILLAGER;
            case 53:
                return ScaryMobs.ENDERMAN;
            case 61:
                return ScaryMobs.WITCH;
            case 5:
                return ScaryMobs.STRAY;
            case 50:
                return ScaryMobs.SLIME;
            case 49:
                return ScaryMobs.ZOMBIE;
            default:
                return NONE;
        }
    }
}
