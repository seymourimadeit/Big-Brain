package tallestegg.bigbrain.client;

import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import tallestegg.bigbrain.BigBrain;

@EventBusSubscriber(value = Dist.CLIENT, modid = BigBrain.MODID)
public class BigBrainClientEvents {
    @SubscribeEvent
    public static void onEntityRenderPre(RenderLivingEvent.Pre<LivingEntity, LivingEntityRenderState, EntityModel<LivingEntityRenderState>> event) {
        LivingEntityRenderState entityIn = event.getRenderState();
        if (entityIn instanceof ZombieRenderState husk) {
            if (husk.getRenderDataOrDefault(BigBrainRendererEvents.BURROWING, false).booleanValue() && husk.swimAmount > 0.0F) {
                float targetXRot = -90.0F - husk.xRot;
                float xAngle = Mth.lerp(husk.swimAmount, 0.0F, targetXRot);
                event.getPoseStack().rotate(Axis.YP.rotationDegrees(180.0F - husk.bodyRot));
                event.getPoseStack().rotate(Axis.ZP.rotationDegrees(180.0F - husk.bodyRot));
                event.getPoseStack().rotate(Axis.XP.rotationDegrees(xAngle));
                if (husk.isVisuallySwimming)
                    event.getPoseStack().translate(0.0F, -1.0F, 0.3F);
                husk.shadowPieces.clear();
            }
        }
    }
}
