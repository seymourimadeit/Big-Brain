package tallestegg.bigbrain;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import tallestegg.bigbrain.networking.PlayerCriticalPacket;

public class BigBrainPackets {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(BigBrain.MODID, "main"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    public static void registerPackets() {
        int id = 0;
        INSTANCE.registerMessage(id++, PlayerCriticalPacket.class, PlayerCriticalPacket::encode, PlayerCriticalPacket::decode, PlayerCriticalPacket::handle);
    }
}
