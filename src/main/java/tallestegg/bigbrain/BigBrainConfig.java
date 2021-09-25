package tallestegg.bigbrain;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@EventBusSubscriber(modid = BigBrain.MODID, bus = EventBusSubscriber.Bus.MOD)
public class BigBrainConfig {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final CommonConfig COMMON;
    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final ClientConfig CLIENT;
    static {
        {
            final Pair<CommonConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(CommonConfig::new);
            COMMON = specPair.getLeft();
            COMMON_SPEC = specPair.getRight();
        }
        {
            final Pair<ClientConfig, ForgeConfigSpec> specPair1 = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
            CLIENT = specPair1.getLeft();
            CLIENT_SPEC = specPair1.getRight();
        }
    }
    public static Boolean PillagerCover;
    public static Boolean PillagerMultishot;
    public static Boolean MobsAttackAllVillagers;
    public static Boolean BruteSpawningWithBuckler;
    public static Boolean BangBlockDestruction;
    public static Boolean PolarBearFish;
    public static Boolean RenderAfterImage;
    public static Boolean RenderEntityLayersDuringAfterImage;
    public static Boolean snowGolemSlow;
    public static Boolean animalShelter;
    public static Boolean meleeFix;
    public static Integer BucklerCooldown;
    public static Integer BucklerTurningRunTime;
    public static Integer BucklerRunTime;
    public static Integer minPigBabiesBred;
    public static Integer maxPigBabiesBred;
    public static Double mobBlindnessVision;
    public static List<String> MobBlackList;
    public static List<String> AnimalBlackList;
    public static List<String> NightAnimalBlackList;
    public static List<String> RainAnimalBlackList;
    public static List<String> EntitiesThatCanAlsoUseTheBuckler;

    public static void bakeCommonConfig() {
        PillagerCover = COMMON.PillagerCover.get();
        PillagerMultishot = COMMON.PillagerMultishot.get();
        MobsAttackAllVillagers = COMMON.MobsAttackAllVillagers.get();
        MobBlackList = COMMON.MobBlackList.get();
        BruteSpawningWithBuckler = COMMON.BruteBuckler.get();
        BucklerCooldown = COMMON.BucklerCooldown.get();
        BucklerRunTime = COMMON.BucklerRunTime.get();
        BangBlockDestruction = COMMON.BangBlockDestruction.get();
        PolarBearFish = COMMON.PolarBearFish.get();
        BucklerTurningRunTime = COMMON.BucklerTurningRunTime.get();
        minPigBabiesBred = COMMON.minPigBabiesBred.get();
        maxPigBabiesBred = COMMON.maxPigBabiesBred.get();
        snowGolemSlow = COMMON.snowGolemSlow.get();
        AnimalBlackList = COMMON.AnimalCoverBlackList.get();
        NightAnimalBlackList = COMMON.NightCoverBlackList.get();
        RainAnimalBlackList = COMMON.RainAnimalBlackList.get();
        animalShelter = COMMON.animalShelter.get();
        mobBlindnessVision = COMMON.mobBlindnessVision.get();
        meleeFix = COMMON.meleeFix.get();
        EntitiesThatCanAlsoUseTheBuckler = COMMON.EntitiesThatCanAlsoUseTheBuckler.get();
    }

    public static void bakeClientConfig() {
        RenderAfterImage = CLIENT.RenderAfterImage.get();
        RenderEntityLayersDuringAfterImage = CLIENT.RenderEntityLayersDuringAfterImage.get();
    }

    @SubscribeEvent
    public static void onModConfigEvent(final ModConfigEvent.Loading configEvent) {
        if (configEvent.getConfig().getSpec() == BigBrainConfig.COMMON_SPEC) {
            bakeCommonConfig();
        }
        if (configEvent.getConfig().getSpec() == BigBrainConfig.CLIENT_SPEC) {
            bakeClientConfig();
        }
    }

    public static class CommonConfig {
        public final ForgeConfigSpec.BooleanValue PillagerCover;
        public final ForgeConfigSpec.BooleanValue PillagerMultishot;
        public final ForgeConfigSpec.BooleanValue MobsAttackAllVillagers;
        public final ForgeConfigSpec.BooleanValue BruteBuckler;
        public final ForgeConfigSpec.BooleanValue BangBlockDestruction;
        public final ForgeConfigSpec.BooleanValue PolarBearFish;
        public final ForgeConfigSpec.BooleanValue snowGolemSlow;
        public final ForgeConfigSpec.BooleanValue animalShelter;
        public final ForgeConfigSpec.BooleanValue meleeFix;
        public final ForgeConfigSpec.IntValue BucklerCooldown;
        public final ForgeConfigSpec.IntValue BucklerRunTime;
        public final ForgeConfigSpec.IntValue BucklerTurningRunTime;
        public final ForgeConfigSpec.IntValue minPigBabiesBred;
        public final ForgeConfigSpec.IntValue maxPigBabiesBred;
        public final ForgeConfigSpec.DoubleValue mobBlindnessVision;
        public final ForgeConfigSpec.ConfigValue<List<String>> MobBlackList;
        public final ForgeConfigSpec.ConfigValue<List<String>> AnimalCoverBlackList;
        public final ForgeConfigSpec.ConfigValue<List<String>> NightCoverBlackList;
        public final ForgeConfigSpec.ConfigValue<List<String>> RainAnimalBlackList;
        public final ForgeConfigSpec.ConfigValue<List<String>> EntitiesThatCanAlsoUseTheBuckler;

        public CommonConfig(ForgeConfigSpec.Builder builder) {
            builder.push("all mobs");
            meleeFix = builder.translation(BigBrain.MODID + ".config.meleeFix").define("Enable the fix for melee cooldowns for mobs?", true);
            mobBlindnessVision = builder.translation(BigBrain.MODID + ".config.blindness").comment("This determines the range a mob will detect other entities if they have the blindness potion, by default entities will only detect targets in a 10 block radius if they are blinded.")
                    .defineInRange("Blindness range", 0.10D, -500.0D, 10000.0D);
            MobsAttackAllVillagers = builder.translation(BigBrain.MODID + ".config.attackvillagers").define("Have all mobs attack villagers?", false);
            MobBlackList = builder.translation(BigBrain.MODID + ".config.blacklist").comment("Any mob id in this list will not attack villagers if the config option for that is on.").define("Mob BlackList", new ArrayList<>());
            EntitiesThatCanAlsoUseTheBuckler = builder.translation(BigBrain.MODID + ".config.mobBucklerWhiteList").comment("Any mob id input in this list will be able to use the buckler").define("Mobs that can also use the buckler", Lists.newArrayList("guardvillagers:guard"));
            builder.pop();
            builder.push("buckler");
            BangBlockDestruction = builder.translation(BigBrain.MODID + ".config.blockBoom").define("Have the explosion spawned while using the Bang! enchant destroy blocks?", false);
            BruteBuckler = builder.translation(BigBrain.MODID + ".config.bruteBuckler").define("Have brutes spawn with bucklers?", true);
            BucklerCooldown = builder.translation(BigBrain.MODID + ".config.bucklerCoolDown").defineInRange("How long should the buckler's cooldown be in ticks?", 240, Integer.MIN_VALUE, Integer.MAX_VALUE);
            BucklerRunTime = builder.translation(BigBrain.MODID + ".config.bucklerRunTime").defineInRange("How long should the buckler's charge move be in ticks?", 15, Integer.MIN_VALUE, Integer.MAX_VALUE); // Thinking of removing this in 1.17.
            BucklerTurningRunTime = builder.translation(BigBrain.MODID + ".config.bucklerRunTime").defineInRange("How long should the buckler's charge move if you have the turning enchant be in ticks?", 30, Integer.MIN_VALUE, Integer.MAX_VALUE);
            builder.pop();
            builder.push("pillager");
            PillagerCover = builder.translation(BigBrain.MODID + ".config.pillagerCover").define("Have pillagers run while reloading?", true);
            PillagerMultishot = builder.translation(BigBrain.MODID + ".config.pillagerMultishot").define("Have pillagers go closer to you if they have a multishot crossbow?", true);
            builder.pop();
            builder.push("animals");
            AnimalCoverBlackList = builder.translation(BigBrain.MODID + ".config.animalBlacklist").comment("Any mob id in this list will not attempt to find an area to stay in while it's raining or at night.").define("Animal BlackList", Lists.newArrayList("minecraft:fox", "minecraft:wolf", "minecraft:turtle", "minecraft:polar_bear", "minecraft:axolotl"));
            NightCoverBlackList = builder.translation(BigBrain.MODID + ".config.animalNightBlacklist").comment("Any mob id in this list will not attempt to find an area to stay in while it's night.").define("Animal Night BlackList", Lists.newArrayList("minecraft:cat"));
            RainAnimalBlackList = builder.translation(BigBrain.MODID + ".config.animalRainBlacklist").comment("Any mob id in this list will not attempt to find an area to stay in while it's raining.").define("Animal Raining BlackList", Lists.newArrayList());
            animalShelter = builder.translation(BigBrain.MODID + ".config.animalShelter").define("Animals seek shelter?", true);
            builder.push("pigs");
            minPigBabiesBred = builder.translation(BigBrain.MODID + ".config.minPigs").defineInRange("What is the minimium amount of extra piglets that could be bred?", 1, Integer.MIN_VALUE, Integer.MAX_VALUE);
            maxPigBabiesBred = builder.translation(BigBrain.MODID + ".config.maxPigs").defineInRange("What is the maxmium amount of extra piglets that could be bred?", 4, Integer.MIN_VALUE, Integer.MAX_VALUE);
            builder.pop();
            builder.pop();
            builder.push("polar bears");
            PolarBearFish = builder.translation(BigBrain.MODID + ".config.polarBearFish").define("Have polar bears attack fish?", true);
            builder.pop();
            builder.push("snow golems");
            snowGolemSlow = builder.translation(BigBrain.MODID + ".config.snowGolemSlow").comment("Freezing time can be added up by successive shots.").define("Allow snow balls to apply 5 seconds of freezing when they hit an entity?", true);
            builder.pop();
        }
    }

    public static class ClientConfig {
        public final ForgeConfigSpec.BooleanValue RenderAfterImage;
        public final ForgeConfigSpec.BooleanValue RenderEntityLayersDuringAfterImage;

        public ClientConfig(ForgeConfigSpec.Builder builder) {
            builder.push("after image");
            RenderAfterImage = builder.translation(BigBrain.MODID + ".config.afterImage").define("Render an after image while an entity is charging with a buckler?", true);
            RenderEntityLayersDuringAfterImage = builder.translation(BigBrain.MODID + ".config.entityLayers").comment("Keep in mind this won't affect their opacity due to technical reasons.").define("Render entity layers while rendering the after image?", false);
            builder.pop();
        }
    }
}
