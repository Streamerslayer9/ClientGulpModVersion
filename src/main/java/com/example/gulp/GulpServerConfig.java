package com.example.gulp;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Server settings for multiplayer admins (config file: serverconfig/gulp-server.toml inside each world,
 * or the world's serverconfig folder on a dedicated server). Only the server reads these.
 */
public final class GulpServerConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue HARD_MODE_ENABLED;
    public static final ForgeConfigSpec.DoubleValue STRUGGLE_DAMAGE_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue XP_MULTIPLIER;
    public static final ForgeConfigSpec.BooleanValue ITEM_STORAGE_ENABLED;
    public static final ForgeConfigSpec.BooleanValue PLAYER_SWALLOWING_ENABLED;
    public static final ForgeConfigSpec.IntValue PLAYER_MAX_HOLD_SECONDS;
    public static final ForgeConfigSpec.BooleanValue ITEM_DIGESTION_ENABLED;
    public static final ForgeConfigSpec.BooleanValue TRASH_DISSOLVE_ENABLED;
    public static final ForgeConfigSpec.BooleanValue DECONSTRUCT_ENCHANTED;
    public static final ForgeConfigSpec.BooleanValue UNIVERSAL_DIGESTION_ENABLED;
    public static final ForgeConfigSpec.BooleanValue MCA_ENABLED;
    public static final ForgeConfigSpec.DoubleValue MCA_REACTION_MULTIPLIER;
    public static final ForgeConfigSpec.BooleanValue MCA_LOCKOUT_ENABLED;
    public static final ForgeConfigSpec.IntValue MCA_PREDATOR_LOCK;
    public static final ForgeConfigSpec.IntValue MCA_HEARTS_LOCK;
    public static final ForgeConfigSpec.IntValue MCA_PREDATOR_DECAY_SECONDS;
    public static final ForgeConfigSpec.BooleanValue MCA_PANIC;
    public static final ForgeConfigSpec.BooleanValue MCA_STATUS_MESSAGE;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        HARD_MODE_ENABLED = b
                .comment("If false, nobody can digest mobs: hard mode and the Digest button are turned off (soft mode only).")
                .define("hardModeEnabled", true);
        STRUGGLE_DAMAGE_MULTIPLIER = b
                .comment("Multiplies the damage swallowed mobs deal to you in hard mode. 0 = they never hurt you, 1 = normal.")
                .defineInRange("struggleDamageMultiplier", 1.0, 0.0, 10.0);
        XP_MULTIPLIER = b
                .comment("Multiplies all stomach XP gained. 0.5 = half as fast, 2 = twice as fast.")
                .defineInRange("xpMultiplier", 1.0, 0.0, 10.0);
        ITEM_STORAGE_ENABLED = b
                .comment("If false, the stomach can't store items (no item swallowing and no Storage screen).")
                .define("itemStorageEnabled", true);
        PLAYER_SWALLOWING_ENABLED = b
                .comment("If false, players can't swallow each other at all.",
                        "Even when true, a player can only be swallowed if they turned on 'Let players swallow me' themselves,",
                        "swallowed players are only held (never harmed or digested), and they can leave any time.")
                .define("playerSwallowingEnabled", true);
        PLAYER_MAX_HOLD_SECONDS = b
                .comment("Longest a swallowed player is held before they are let out automatically.")
                .defineInRange("playerMaxHoldSeconds", 300, 10, 3600);
        ITEM_DIGESTION_ENABLED = b
                .comment("Hard mode only: gear in the item storage (tools, weapons, armor) is broken down into its crafting ingredients over time.",
                        "If false, stored items are never touched by hard mode.")
                .define("itemDigestionEnabled", true);
        TRASH_DISSOLVE_ENABLED = b
                .comment("Hard mode only: items in the tag gulp:stomach_trash (cobblestone, dirt, seeds ...) dissolve over time.")
                .define("trashDissolveEnabled", true);
        DECONSTRUCT_ENCHANTED = b
                .comment("If true, enchanted gear is broken down too (the enchantments are lost). If false it is left alone.")
                .define("deconstructEnchanted", false);
        UNIVERSAL_DIGESTION_ENABLED = b
                .comment("If false, the Eat buttons on the storage screen only eat food (Iron Gut / Universal Digestion does nothing).",
                        "Otherwise any item can be eaten: every 32 items restores hunger (see the Iron Gut perk).")
                .define("universalDigestionEnabled", true);
        MCA_ENABLED = b
                .comment("Minecraft Comes Alive reactions (only does anything if MCA is installed): swallowing villagers changes",
                        "how they feel about you, and a 'predator' status makes frightened villagers refuse to talk to you.")
                .define("mcaIntegrationEnabled", true);
        MCA_REACTION_MULTIPLIER = b
                .comment("Scales every change to villagers' hearts and mood. 0 = villagers never react, 2 = twice as strongly.")
                .defineInRange("mcaReactionMultiplier", 1.0, 0.0, 5.0);
        MCA_LOCKOUT_ENABLED = b
                .comment("If false, villagers never refuse to talk, trade or marry because of your predator status.")
                .define("mcaLockoutEnabled", true);
        MCA_PREDATOR_LOCK = b
                .comment("Predator status at which frightened villagers refuse to talk to you.",
                        "Swallowing a villager in hard mode adds about 15, digesting one about 25.")
                .defineInRange("mcaPredatorLockThreshold", 30, 1, 200);
        MCA_HEARTS_LOCK = b
                .comment("A villager whose hearts for you are at or below this also refuses to talk while your predator status is above 0.")
                .defineInRange("mcaHeartsLockThreshold", -30, -500, 0);
        MCA_PREDATOR_DECAY_SECONDS = b
                .comment("Seconds for the predator status to drop by 1 (at the default 20, a status of 40 fades in about 13 minutes).")
                .defineInRange("mcaPredatorDecaySeconds", 20, 1, 3600);
        MCA_PANIC = b
                .comment("If true, villagers who see you swallow or digest a villager run from you for a few seconds.")
                .define("mcaVillagersPanic", true);
        MCA_STATUS_MESSAGE = b
                .comment("Test/dev helper: when a player joins, show a chat line saying whether this mod connected to Minecraft Comes Alive.",
                        "Set to false to turn it off once you've confirmed it works.")
                .define("mcaStatusMessage", true);
        SPEC = b.build();
    }

    private GulpServerConfig() { }
}
