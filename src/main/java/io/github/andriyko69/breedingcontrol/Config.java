package io.github.andriyko69.breedingcontrol;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = BreedingControl.MOD_ID)
public final class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.ConfigValue<List<? extends String>> NOT_BREEDABLE_ENTRIES =
            BUILDER.comment(
                    "List of entity ids or entity_type tags that cannot be bred.",
                    "Examples:",
                    "  \"minecraft:cow\"",
                    "  \"#minecraft:animals\""
            ).defineListAllowEmpty("notBreedableMobs", List.of(), Config::isValidEntry);

    public static final ModConfigSpec.BooleanValue IS_HEALING_WITH_BREED_FOOD_ALLOWED =
            BUILDER.comment(
                    "If true: when using breeding food on a blocked animal that is hurt, it will heal (and consume 1 item) but will NOT enter love mode."
            ).define("allowHealingWithBreedFood", true);

    public static final ModConfigSpec.DoubleValue HEAL_AMOUNT =
            BUILDER.comment("How much to heal when allowHealingWithBreedFood=true (vanilla hearts are 2 HP).")
                    .defineInRange("healAmount", 2.0D, 0.0D, 1000.0D);

    public static final ModConfigSpec.BooleanValue IS_BABY_FEEDING_ALLOWED =
            BUILDER.comment("If true: babies are not blocked (feeding may speed up growth).")
                    .define("allowBabyFeeding", true);

    static final ModConfigSpec SPEC = BUILDER.build();

    private static volatile Set<EntityType<?>> BLOCKED_TYPES = Collections.emptySet();
    private static volatile List<TagKey<EntityType<?>>> BLOCKED_TAGS = List.of();

    public static volatile boolean isHealingWithBreedFoodAllowed = true;
    public static volatile float healAmount = 2.0F;
    public static volatile boolean isBabyFeedingAllowed = true;

    private static boolean isValidEntry(Object obj) {
        if (!(obj instanceof String s)) return false;

        String raw = s.startsWith("#") ? s.substring(1) : s;
        ResourceLocation rl = ResourceLocation.tryParse(raw);
        if (rl == null) return false;

        if (!s.startsWith("#")) {
            return BuiltInRegistries.ENTITY_TYPE.containsKey(rl);
        }
        return true;
    }

    public static boolean isAllowed(EntityType<?> type) {
        if (BLOCKED_TYPES.contains(type)) return false;

        for (TagKey<EntityType<?>> tag : BLOCKED_TAGS) {
            if (type.is(tag)) return false;
        }
        return true;
    }

    @SubscribeEvent
    static void onConfigLoading(ModConfigEvent.Loading event) {
        bake(event.getConfig());
    }

    @SubscribeEvent
    static void onConfigReloading(ModConfigEvent.Reloading event) {
        bake(event.getConfig());
    }

    private static void bake(ModConfig config) {
        if (config.getSpec() != SPEC) return;
        if (config.getType() != ModConfig.Type.COMMON) return;

        List<? extends String> entries = NOT_BREEDABLE_ENTRIES.get();

        BLOCKED_TYPES = entries.stream()
                .filter(s -> !s.startsWith("#"))
                .map(ResourceLocation::tryParse)
                .filter(rl -> rl != null && BuiltInRegistries.ENTITY_TYPE.containsKey(rl))
                .map(BuiltInRegistries.ENTITY_TYPE::get)
                .collect(Collectors.toUnmodifiableSet());

        BLOCKED_TAGS = entries.stream()
                .filter(s -> s.startsWith("#"))
                .map(s -> ResourceLocation.tryParse(s.substring(1)))
                .filter(Objects::nonNull)
                .map(rl -> TagKey.create(Registries.ENTITY_TYPE, rl))
                .toList();

        isHealingWithBreedFoodAllowed = IS_HEALING_WITH_BREED_FOOD_ALLOWED.get();
        healAmount = (float) (double) HEAL_AMOUNT.get();
        isBabyFeedingAllowed = IS_BABY_FEEDING_ALLOWED.get();
    }
}