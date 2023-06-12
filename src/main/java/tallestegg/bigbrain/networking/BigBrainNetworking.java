package tallestegg.bigbrain.networking;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import tallestegg.bigbrain.BigBrain;
import tallestegg.bigbrain.common.capabilities.BigBrainCapabilities;
import tallestegg.bigbrain.common.capabilities.implementations.BurrowCapability;

public class BigBrainNetworking {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(BigBrain.MODID, "main"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);


    public static void syncBurrow(BurrowingCapabilityPacket msg) {
        Entity entity = Minecraft.getInstance().level.getEntity(msg.getEntityId());
        if (entity != null && entity instanceof LivingEntity living) {
            BurrowCapability burrow = BigBrainCapabilities.getBurrowing(living);
            burrow.setBurrowing(msg.getBurrow());
        }
    }

    public static void registerPackets() {
        int id = 0;
        INSTANCE.registerMessage(id++, BurrowingCapabilityPacket.class, BurrowingCapabilityPacket::encode, BurrowingCapabilityPacket::decode, BurrowingCapabilityPacket::handle);
    }
}
