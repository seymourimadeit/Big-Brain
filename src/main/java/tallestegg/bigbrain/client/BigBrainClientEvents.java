package tallestegg.bigbrain.client;

import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Skeleton;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import tallestegg.bigbrain.BigBrain;
import tallestegg.bigbrain.common.capabilities.BigBrainCapabilities;

@EventBusSubscriber(value = Dist.CLIENT, modid = BigBrain.MODID)
public class BigBrainClientEvents {
    @SubscribeEvent
    public static void onEntityRenderPost(RenderLivingEvent.Post<LivingEntity, EntityModel<LivingEntity>> event) {
        LivingEntity entityIn = event.getEntity();
        LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>> renderer = event.getRenderer();
        if (event.getEntity() instanceof Skeleton skeleton && renderer.getModel() instanceof SkeletonModel skeleModel) {
            if (skeleModel.rightArmPose == HumanoidModel.ArmPose.BOW_AND_ARROW || skeleModel.leftArmPose == HumanoidModel.ArmPose.BOW_AND_ARROW) {
                if (skeleton.getDeltaMovement().y() > 0 && skeleton.getDeltaMovement().x() > 0 && skeleton.getDeltaMovement().z() > 0) {
                    skeleModel.rightArmPose = HumanoidModel.ArmPose.EMPTY;
                    skeleModel.leftArmPose = HumanoidModel.ArmPose.EMPTY;
                }
            }
        }
        if (entityIn instanceof Husk husk) {
            if (husk.getSwimAmount(event.getPartialTick()) > 0.0F) {
                event.getPoseStack().popPose();
            }
        }
    }

    @SubscribeEvent
    public static void onEntityRenderPre(RenderLivingEvent.Pre<LivingEntity, EntityModel<LivingEntity>> event) {
        LivingEntity entityIn = event.getEntity();
        if (entityIn instanceof Husk husk) {
            boolean burrowing = husk.getData(BigBrainCapabilities.BURROWING);
            if (burrowing)
                event.setCanceled(false);
            if (husk.getSwimAmount(event.getPartialTick()) > 0.0F) {
                event.getPoseStack().pushPose();
                float f = Mth.rotLerp(event.getPartialTick(), husk.yBodyRotO, husk.yBodyRot);
                float f3 = -90.0F - husk.getXRot();
                float f4 = Mth.lerp(husk.getSwimAmount(event.getPartialTick()), 0.0F, f3);
                event.getPoseStack().mulPose(Axis.YP.rotationDegrees(180.0F - f));
                event.getPoseStack().mulPose(Axis.ZP.rotationDegrees(180.0F - f));
                event.getPoseStack().mulPose(Axis.XP.rotationDegrees(f4));
                if (husk.isVisuallySwimming())
                    event.getPoseStack().translate(0.0F, -1.0F, 0.3F);
            }
        }
    }
}
