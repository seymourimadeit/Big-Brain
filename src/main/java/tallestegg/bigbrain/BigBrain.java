package tallestegg.bigbrain;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import tallestegg.bigbrain.client.BigBrainSounds;
import tallestegg.bigbrain.common.capabilities.BigBrainCapabilities;
import tallestegg.bigbrain.networking.BigBrainNetworking;
import tallestegg.bigbrain.networking.BurrowingCapabilityPacket;

@Mod(BigBrain.MODID)
public class BigBrain {
    public static final String MODID = "bigbrain";

    public BigBrain(IEventBus modEventBus, Dist dist, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, BigBrainConfig.COMMON_SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, BigBrainConfig.CLIENT_SPEC);
        BigBrainSounds.SOUNDS.register(modEventBus);
        BigBrainCapabilities.ATTACHMENT_TYPES.register(modEventBus);
    }

    private void registerPackets(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(BurrowingCapabilityPacket.TYPE, BurrowingCapabilityPacket.STREAM_CODEC, BurrowingCapabilityPacket::handle);
    }
}
