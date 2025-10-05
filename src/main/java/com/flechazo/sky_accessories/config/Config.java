package com.flechazo.sky_accessories.config;

import com.flechazo.sky_accessories.SkyAccessories;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.List;

public class Config {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        COMMON = new Common(builder);
        COMMON_SPEC = builder.build();
    }

    public static class Common {
        public final ForgeConfigSpec.BooleanValue absoluteDefense;
        public final ForgeConfigSpec.BooleanValue absoluteAutonomy;
        public final ForgeConfigSpec.BooleanValue godPermissions;
        public final ForgeConfigSpec.BooleanValue godSuppression;
        public final ForgeConfigSpec.BooleanValue godAttack;
        public final ForgeConfigSpec.BooleanValue eternalTranscendence;
        public final ForgeConfigSpec.IntValue fixedAttackDamage;
        public final ForgeConfigSpec.BooleanValue buffsEnabled;
        public final ForgeConfigSpec.IntValue buffMode;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> positiveEffectIds;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> negativeEffectIds;

        public Common(ForgeConfigSpec.Builder b) {
            b.push("sky_accessories");
            absoluteDefense = b.define("absoluteDefense", true);
            absoluteAutonomy = b.define("absoluteAutonomy", true);
            godPermissions = b.define("godPermissions", true);
            godSuppression = b.define("godSuppression", true);
            godAttack = b.define("godAttack", true);
            eternalTranscendence = b.define("eternalTranscendence", true);
            fixedAttackDamage = b.defineInRange("fixedAttackDamage", 1000, 0, Integer.MAX_VALUE);
            b.pop();
            buffsEnabled = b.define("buffsEnabled", true);
            buffMode = b.defineInRange("buffMode", 3, 0, 3);
            positiveEffectIds = b.defineList("positiveEffectIds",
                    List.of(
                            "minecraft:resistance",
                            "minecraft:regeneration",
                            "minecraft:speed",
                            "minecraft:haste",
                            "minecraft:fire_resistance",
                            "minecraft:water_breathing",
                            "minecraft:strength",
                            "minecraft:health_boost"
                    ), o -> o instanceof String);
            negativeEffectIds = b.defineList("negativeEffectIds",
                    List.of(
                            "minecraft:glowing"
                    ), o -> o instanceof String);
        }
    }

    @Mod.EventBusSubscriber(modid = SkyAccessories.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Events {
        @SubscribeEvent
        public static void onReload(ModConfigEvent evt) {
        }
    }
}
