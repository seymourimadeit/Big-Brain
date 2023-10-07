package tallestegg.bigbrain.networking;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.SimpleChannel;
import tallestegg.bigbrain.BigBrain;
import tallestegg.bigbrain.common.capabilities.BigBrainCapabilities;
import tallestegg.bigbrain.common.capabilities.implementations.BurrowCapability;

public class BigBrainNetworking {
    private static final Integer PROTOCOL_VERSION = 1;
    public static final SimpleChannel INSTANCE = ChannelBuilder.named(BigBrain.MODID).networkProtocolVersion(PROTOCOL_VERSION).clientAcceptedVersions(Channel.VersionTest.exact(PROTOCOL_VERSION)).serverAcceptedVersions(Channel.VersionTest.exact(PROTOCOL_VERSION)).simpleChannel();


    public static void syncBurrow(BurrowingCapabilityPacket msg) {
        Entity entity = Minecraft.getInstance().level.getEntity(msg.getEntityId());
        if (entity != null && entity instanceof LivingEntity living) {
            BurrowCapability burrow = BigBrainCapabilities.getBurrowing(living);
            burrow.setBurrowing(msg.getBurrow());
        }
    }

    public static void registerPackets() {
        int id = 0;
        INSTANCE.messageBuilder(BurrowingCapabilityPacket.class, 0).encoder(BurrowingCapabilityPacket::encode).decoder(BurrowingCapabilityPacket::decode).consumerMainThread(BurrowingCapabilityPacket::handle).add();
    }
}
