package tallestegg.bigbrain;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import tallestegg.bigbrain.common.capabilities.IOneCriticalAfterCharge;
import tallestegg.bigbrain.networking.CriticalCapabilityPacket;

public class BigBrainNetworking {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(BigBrain.MODID, "main"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    public static void syncCritical(CriticalCapabilityPacket msg) {
        Entity entity = Minecraft.getInstance().level.getEntity(msg.getEntityId());
        if (entity != null && entity instanceof LivingEntity living) {
            IOneCriticalAfterCharge criticalAfterCharge = BigBrainCapabilities.getGuranteedCritical(living);
            criticalAfterCharge.setCritical(msg.getCrit());
        }
    }

    public static void registerPackets() {
        int id = 0;
        INSTANCE.registerMessage(id++, CriticalCapabilityPacket.class, CriticalCapabilityPacket::encode, CriticalCapabilityPacket::decode, CriticalCapabilityPacket::handle);
    }
}
