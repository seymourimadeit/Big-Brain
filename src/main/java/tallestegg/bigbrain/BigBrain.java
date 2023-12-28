package tallestegg.bigbrain;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import tallestegg.bigbrain.client.BigBrainSounds;
import tallestegg.bigbrain.common.capabilities.BigBrainCapabilities;
import tallestegg.bigbrain.networking.BigBrainNetworking;

@Mod(BigBrain.MODID)
public class BigBrain {
    public static final String MODID = "bigbrain";

    public BigBrain(IEventBus modEventBus, Dist dist) {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, BigBrainConfig.COMMON_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, BigBrainConfig.CLIENT_SPEC);
        BigBrainSounds.SOUNDS.register(modEventBus);
        BigBrainCapabilities.ATTACHMENT_TYPES.register(modEventBus);
        BigBrainNetworking.registerPackets();
    }
}
