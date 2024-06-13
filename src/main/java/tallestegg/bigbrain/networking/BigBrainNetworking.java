package tallestegg.bigbrain.networking;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import tallestegg.bigbrain.common.capabilities.BigBrainCapabilities;

public class BigBrainNetworking {
    public static void syncBurrow(BurrowingCapabilityPacket msg) {
        Entity entity = Minecraft.getInstance().level.getEntity(msg.entityId());
        if (entity != null && entity instanceof LivingEntity living) {
            living.setData(BigBrainCapabilities.BURROWING.get(), msg.burrow());
        }
    }
}
