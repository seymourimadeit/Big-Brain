package tallestegg.bigbrain.networking;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.NetworkRegistry;
import net.neoforged.neoforge.network.simple.SimpleChannel;
import tallestegg.bigbrain.BigBrain;
import tallestegg.bigbrain.common.capabilities.BigBrainCapabilities;

public class BigBrainNetworking {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(BigBrain.MODID, "main"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    public static void syncBurrow(BurrowingCapabilityPacket msg) {
        Entity entity = Minecraft.getInstance().level.getEntity(msg.getEntityId());
        if (entity != null && entity instanceof LivingEntity living) {
            living.setData(BigBrainCapabilities.BURROWING.get(), msg.getBurrow());
        }
    }

    public static void registerPackets() {
        int id = 0;
        INSTANCE.messageBuilder(BurrowingCapabilityPacket.class, 0).encoder(BurrowingCapabilityPacket::encode).decoder(BurrowingCapabilityPacket::decode).consumerMainThread(BurrowingCapabilityPacket::handle).add();
    }
}
