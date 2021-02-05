package tallestegg.bigbrain.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tallestegg.bigbrain.BigBrain;
import tallestegg.bigbrain.entity.IBucklerUser;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD, modid = BigBrain.MODID)
public class BigBrainClientEvents {
    @SubscribeEvent
    public static void onMovementKeyPressed(InputUpdateEvent event) {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if (((IBucklerUser) player).isBucklerDashing()) {
            event.getMovementInput().jump = false;
            event.getMovementInput().moveStrafe = 0;
        }
    }
}
