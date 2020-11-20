package tallestegg.bigbrain;

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

    public static void bakeCommonConfig() {
        PillagerCover = COMMON.PillagerCover.get();
        PigBreeding = COMMON.PigBreeding.get();
        PillagerMultishot = COMMON.PillagerMultishot.get();
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

        public CommonConfig(ForgeConfigSpec.Builder builder) {
            PigBreeding = builder.translation(BigBrain.MODID + ".config.pigBreeding").define("Have pigs give birth to multiple babies?", true);
            PillagerCover = builder.translation(BigBrain.MODID + ".config.pillagerCover").define("Have pillagers run while reloading?", true);
            PillagerMultishot = builder.translation(BigBrain.MODID + ".config.pillagerMultishot").define("Have pillagers go closer to you if they have a multishot crossbow?", true);
        }
    }
}
