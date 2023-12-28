package tallestegg.bigbrain;

import java.util.ArrayList;
import java.util.List;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;



@Mod.EventBusSubscriber(modid = BigBrain.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BigBrainConfig {
    public static final ModConfigSpec COMMON_SPEC;
    public static final CommonConfig COMMON;
    public static final ModConfigSpec CLIENT_SPEC;
    public static final ClientConfig CLIENT;
    static {
        {
            final Pair<CommonConfig, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(CommonConfig::new);
            COMMON = specPair.getLeft();
            COMMON_SPEC = specPair.getRight();
        }
        {
            final Pair<ClientConfig, ModConfigSpec> specPair1 = new ModConfigSpec.Builder().configure(ClientConfig::new);
            CLIENT = specPair1.getLeft();
            CLIENT_SPEC = specPair1.getRight();
        }
    }
    public static Boolean PillagerCover;
    public static Boolean PillagerMultishot;
    public static Boolean MobsAttackAllVillagers;
    public static Boolean PolarBearFish;
    public static Boolean snowGolemSlow;
    public static Boolean animalShelter;
    public static Boolean meleeFix;
    public static Boolean ocelotParrot;
    public static Boolean ocelotPhantom;
    public static Boolean ocelotCreeper;
    public static Boolean sheepRunAway;
    public static Boolean openFenceGate;
    public static float spyGlassPillagerChance;
    public static Integer minPigBabiesBred;
    public static Integer maxPigBabiesBred;
    public static Double mobBlindnessVision;
    public static List<String> MobBlackList;
    public static List<String> AnimalBlackList;
    public static List<String> NightAnimalBlackList;
    public static List<String> RainAnimalBlackList;
    public static List<String> cantOpenFenceGates;

    public static void bakeCommonConfig() {
        PillagerCover = COMMON.PillagerCover.get();
        PillagerMultishot = COMMON.PillagerMultishot.get();
        MobsAttackAllVillagers = COMMON.MobsAttackAllVillagers.get();
        MobBlackList = COMMON.MobBlackList.get();
        PolarBearFish = COMMON.PolarBearFish.get();
        minPigBabiesBred = COMMON.minPigBabiesBred.get();
        maxPigBabiesBred = COMMON.maxPigBabiesBred.get();
        snowGolemSlow = COMMON.snowGolemSlow.get();
        AnimalBlackList = COMMON.AnimalCoverBlackList.get();
        NightAnimalBlackList = COMMON.NightCoverBlackList.get();
        RainAnimalBlackList = COMMON.RainAnimalBlackList.get();
        animalShelter = COMMON.animalShelter.get();
        mobBlindnessVision = COMMON.mobBlindnessVision.get();
        meleeFix = COMMON.meleeFix.get();
        ocelotCreeper = COMMON.ocelotCreeper.get();
        ocelotParrot = COMMON.ocelotParrot.get();
        ocelotPhantom = COMMON.ocelotPhantom.get();
        sheepRunAway = COMMON.sheepRunAway.get();
        spyGlassPillagerChance = COMMON.pillagerSpyGlass.get().floatValue();
        openFenceGate = COMMON.openFenceGates.get();
        cantOpenFenceGates = COMMON.fenceGateBlacklist.get();
    }

    @SubscribeEvent
    public static void onModConfigEvent(final ModConfigEvent.Loading configEvent) {
        if (configEvent.getConfig().getSpec() == BigBrainConfig.COMMON_SPEC) {
            bakeCommonConfig();
        }
    }

    public static class CommonConfig {
        public final ModConfigSpec.BooleanValue PillagerCover;
        public final ModConfigSpec.BooleanValue PillagerMultishot;
        public final ModConfigSpec.BooleanValue MobsAttackAllVillagers;
        public final ModConfigSpec.BooleanValue PolarBearFish;
        public final ModConfigSpec.BooleanValue snowGolemSlow;
        public final ModConfigSpec.BooleanValue animalShelter;
        public final ModConfigSpec.BooleanValue animalPanic;
        public final ModConfigSpec.BooleanValue meleeFix;
        public final ModConfigSpec.BooleanValue ocelotParrot;
        public final ModConfigSpec.BooleanValue ocelotPhantom;
        public final ModConfigSpec.BooleanValue ocelotCreeper;
        public final ModConfigSpec.BooleanValue sheepRunAway;
        public final ModConfigSpec.BooleanValue openFenceGates;
        public final ModConfigSpec.BooleanValue bowAiNew;
        public final ModConfigSpec.BooleanValue huskBurrowing;
        public final ModConfigSpec.BooleanValue jumpAi;
        public final ModConfigSpec.DoubleValue pillagerSpyGlass;
        public final ModConfigSpec.IntValue minPigBabiesBred;
        public final ModConfigSpec.IntValue maxPigBabiesBred;
        public final ModConfigSpec.DoubleValue mobBlindnessVision;
        public final ModConfigSpec.ConfigValue<List<String>> MobBlackList;
        public final ModConfigSpec.ConfigValue<List<String>> AnimalCoverBlackList;
        public final ModConfigSpec.ConfigValue<List<String>> NightCoverBlackList;
        public final ModConfigSpec.ConfigValue<List<String>> RainAnimalBlackList;
        public final ModConfigSpec.ConfigValue<List<String>> fenceGateBlacklist;
        public final ModConfigSpec.ConfigValue<List<String>> bowAiBlackList;
        public final ModConfigSpec.ConfigValue<List<String>> jumpWhiteList;
        public final ModConfigSpec.ConfigValue<List<String>> jumpBlackList;

        public CommonConfig(ModConfigSpec.Builder builder) {
            builder.push("all mobs");
            meleeFix = builder.translation(BigBrain.MODID + ".config.meleeFix").define("Enable the fix for melee cooldowns for mobs?", true);
            mobBlindnessVision = builder.translation(BigBrain.MODID + ".config.blindness").comment("This determines the range a mob will detect other entities if they have the blindness potion, by default entities will only detect targets in a 10 block radius if they are blinded.")
                    .defineInRange("Blindness range", 0.10D, -500.0D, 10000.0D);
            MobsAttackAllVillagers = builder.translation(BigBrain.MODID + ".config.attackvillagers").define("Have all mobs attack villagers?", false);
            MobBlackList = builder.translation(BigBrain.MODID + ".config.blacklist").comment("Any mob id in this list will not attack villagers if the config option for that is on.").define("Mob BlackList", new ArrayList<>());
            openFenceGates = builder.define("Allow mobs to open fence gates if they are already able to open doors", true);
            fenceGateBlacklist = builder.comment("Any mob id input in this list will not open fence gates if they're already able to open doors").define("Fence Gate Opening Blacklist", Lists.newArrayList("minecraft:husk", "minecraft:zombie", "minecraft:vindicator", "minecraft:drowned"));
            bowAiNew = builder.define("Enable new bow ai?", true);
            bowAiBlackList = builder.define("Mobs that don't have the new bow ai", Lists.newArrayList());
            jumpAi = builder.define("Enable jumping ai", true);
            jumpWhiteList = builder.define("List additional mobs that can also utilize jumping", Lists.newArrayList("guardvillagers:guard"));
            jumpBlackList = builder.define("Mobs that don't have the jumping ai", Lists.newArrayList("minecraft:villager"));
            builder.pop();
            builder.push("husk");
            huskBurrowing = builder.define("Enable burrowing attack for husk?", true);
            builder.pop();
            builder.push("pillager");
            PillagerCover = builder.translation(BigBrain.MODID + ".config.pillagerCover").define("Have pillagers run while reloading?", true);
            PillagerMultishot = builder.translation(BigBrain.MODID + ".config.pillagerMultishot").define("Have pillagers go closer to you if they have a multishot crossbow?", true);
            pillagerSpyGlass = builder.defineInRange("Chance of a pillager patrol leader getting a spyglass", 0.50F, 0.0F, 900.0F);
            builder.pop();
            builder.push("animals");
            animalPanic = builder.define("Have animals alert their kin to panic if hurt?", true);
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
            builder.push("ocelots");
            ocelotCreeper = builder.define("Have ocelots attack creepers? (Creepers won't attack back)", true);
            ocelotParrot = builder.define("Have ocelots attack parrots?", true);
            ocelotPhantom = builder.define("Have ocelots attack phantoms?", true);
            builder.pop();
            builder.push("sheep");
            sheepRunAway = builder.define("Have sheep run away from wolves?", true);
            builder.pop();
            builder.push("snow golems");
            snowGolemSlow = builder.translation(BigBrain.MODID + ".config.snowGolemSlow").comment("Freezing time can be added up by successive shots.").define("Allow snow balls to apply 5 seconds of freezing when they hit an entity?", true);
            builder.pop();
        }
    }

    public static class ClientConfig {
        public final ModConfigSpec.BooleanValue bedrockBeeAnim;
        public final ModConfigSpec.BooleanValue drownedGlow;

        public ClientConfig(ModConfigSpec.Builder builder) {
            builder.push("bedrock animations");
            bedrockBeeAnim = builder.define("Allow bees to have a idle animation akin to bedrock", true);
            drownedGlow = builder.define("Allow drowned to render glowing spots, like in bedrock", true);
            builder.pop();
        }
    }
}
