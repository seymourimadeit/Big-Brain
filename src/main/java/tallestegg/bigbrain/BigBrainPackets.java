package tallestegg.bigbrain;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import tallestegg.bigbrain.entity.IOneCriticalAfterCharge;
import tallestegg.bigbrain.networking.PlayerCriticalPacket;

public class BigBrainPackets {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(BigBrain.MODID, "main"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    public static void registerPackets() {
        int id = 0;
        INSTANCE.registerMessage(id++, PlayerCriticalPacket.class, PlayerCriticalPacket::encode, PlayerCriticalPacket::decode, PlayerCriticalPacket::handle);
    }
    
    @OnlyIn(Dist.CLIENT) //This should be removed when I find a better solution.
    public static void makePlayerNotCritical(PlayerCriticalPacket packet) {
        PlayerEntity player = Minecraft.getInstance().player;
        if (player != null) {
            ((IOneCriticalAfterCharge)player).setCritical(false);
        }
    }
}
