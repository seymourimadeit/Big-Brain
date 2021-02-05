package tallestegg.bigbrain;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.config.ModConfig;

@EventBusSubscriber(modid = BigBrain.MODID, bus = EventBusSubscriber.Bus.MOD)
public class BigBrainConfig {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final CommonConfig COMMON;
    static {
        {
            final Pair<CommonConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(CommonConfig::new);
            COMMON = specPair.getLeft();
            COMMON_SPEC = specPair.getRight();
        }
    }
    public static Boolean PillagerCover;
    public static Boolean PillagerMultishot;
    public static Boolean PigBreeding;
    public static Boolean MobsAttackAllVillagers;
    public static Boolean BruteSpawningWithBuckler;
    public static Boolean BangBlockDestruction;
    public static Boolean PolarBearFish;
    public static Integer BucklerCooldown;
    public static Integer BucklerRunTime;
    public static List<String> MobBlackList;

    public static void bakeCommonConfig() {
        PillagerCover = COMMON.PillagerCover.get();
        PigBreeding = COMMON.PigBreeding.get();
        PillagerMultishot = COMMON.PillagerMultishot.get();
        MobsAttackAllVillagers = COMMON.MobsAttackAllVillagers.get();
        MobBlackList = COMMON.MobBlackList.get();
        BruteSpawningWithBuckler = COMMON.BruteBuckler.get();
        BucklerCooldown = COMMON.BucklerCooldown.get();
        BucklerRunTime = COMMON.BucklerRunTime.get();
        BangBlockDestruction = COMMON.BangBlockDestruction.get();
        PolarBearFish = COMMON.PolarBearFish.get();
    }

    @SubscribeEvent
    public static void onModConfigEvent(final ModConfig.ModConfigEvent configEvent) {
        if (configEvent.getConfig().getSpec() == BigBrainConfig.COMMON_SPEC) {
            bakeCommonConfig();
        }
    }

    public static class CommonConfig {

        public final ForgeConfigSpec.BooleanValue PillagerCover;
        public final ForgeConfigSpec.BooleanValue PillagerMultishot;
        public final ForgeConfigSpec.BooleanValue PigBreeding;
        public final ForgeConfigSpec.BooleanValue MobsAttackAllVillagers;
        public final ForgeConfigSpec.BooleanValue BruteBuckler;
        public final ForgeConfigSpec.BooleanValue BangBlockDestruction;
        public final ForgeConfigSpec.BooleanValue PolarBearFish;
        public final ForgeConfigSpec.IntValue BucklerCooldown;
        public final ForgeConfigSpec.IntValue BucklerRunTime;
        public final ForgeConfigSpec.ConfigValue<List<String>> MobBlackList;

        public CommonConfig(ForgeConfigSpec.Builder builder) {
            PigBreeding = builder.translation(BigBrain.MODID + ".config.pigBreeding").define("Have pigs give birth to multiple babies?", true);
            PillagerCover = builder.translation(BigBrain.MODID + ".config.pillagerCover").define("Have pillagers run while reloading?", true);
            PillagerMultishot = builder.translation(BigBrain.MODID + ".config.pillagerMultishot").define("Have pillagers go closer to you if they have a multishot crossbow?", true);
            PolarBearFish = builder.translation(BigBrain.MODID + ".config.polarBearFish").define("Have polar bears attack fish?", true);
            BangBlockDestruction = builder.translation(BigBrain.MODID + ".config.blockBoom").define("Have the explosion spawned while using the Bang! enchant destroy blocks?", false);
            BruteBuckler = builder.translation(BigBrain.MODID + ".config.bruteBuckler").define("Have brutes spawn with bucklers?", true);
            MobsAttackAllVillagers = builder.translation(BigBrain.MODID + ".config.attackvillagers").define("Have all mobs attack villagers?", false);
            MobBlackList = builder.translation(BigBrain.MODID + ".config.blacklist").comment("Any mob id in this list will not attack villagers if the config option for that is on.").define("Mob BlackList", new ArrayList<>());
            BucklerCooldown = builder.translation(BigBrain.MODID + ".config.bucklerCoolDown").defineInRange("How long should the buckler's cooldown be in ticks?", 240, Integer.MIN_VALUE, Integer.MAX_VALUE);
            BucklerRunTime = builder.translation(BigBrain.MODID + ".config.bucklerRunTime").defineInRange("How long should the buckler's charge move be in ticks?", 15, Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
    }
}
