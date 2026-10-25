package tallestegg.bigbrain.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpyglassItem;

// Mainly for easier debugging for mixin animations
public class BigBrainAnimationHelper {
    public static void zoomInAnimRight(ModelPart armPart, ModelPart head, InteractionHand hand, IllagerRenderState entityIn, HumanoidArm arm) {
        if (checkIfUsingSpyglass(entityIn, hand)) {
            armPart.xRot = Mth.clamp(head.xRot - 1.9198622F - (entityIn.isCrouching ? 0.2617994F : 0.0F),
                    -2.4F, 3.3F);
            armPart.yRot = head.yRot - 0.2617994F;
        }
    }

    public static void zoomInAnimLeft(ModelPart armPart, ModelPart head, InteractionHand hand, IllagerRenderState entityIn, HumanoidArm arm) {
        if (checkIfUsingSpyglass(entityIn, hand)) {
            armPart.xRot = Mth.clamp(head.xRot - 1.9198622F - (entityIn.isCrouching ? 0.2617994F : 0.0F),
                    -2.4F, 3.3F);
            armPart.yRot = head.yRot + 0.2617994F;
        }
    }

    public static boolean checkIfUsingSpyglass(IllagerRenderState entityIn, InteractionHand hand) {
        ItemStack offhandStack = entityIn.getRenderData(BigBrainRendererEvents.OFFHAND_STACK);
        int useTicks = entityIn.getRenderData(BigBrainRendererEvents.STACK_USE_TICKS); // IllagerRenderState doesn't correctly input this value
        InteractionHand correctHand = entityIn.getRenderData(BigBrainRendererEvents.CORRECT_USE_ITEM_HAND);
        return useTicks > 0 && offhandStack.getItem() instanceof SpyglassItem && (hand == InteractionHand.OFF_HAND && correctHand == InteractionHand.OFF_HAND);
    }

    public static void renderItemHeldToEye(RenderLayer render, IllagerRenderState state, ItemStackRenderState item, HumanoidArm arm, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords) {
        poseStack.pushPose();
        render.getParentModel().root().translateAndRotate(poseStack);
        if (render.getParentModel() instanceof HeadedModel headedModel) {
            ModelPart head = headedModel.getHead();
            float previousXRot = head.xRot;
            head.xRot = Mth.clamp(head.xRot, (float) (-Math.PI / 6), (float) (Math.PI / 2));
            head.translateAndRotate(poseStack);
            head.xRot = previousXRot;
            CustomHeadLayer.translateToHead(poseStack, CustomHeadLayer.Transforms.DEFAULT);
            boolean isLeftHand = arm == HumanoidArm.LEFT;
            poseStack.translate((isLeftHand ? -2.5F : 2.5F) / 16.0F, -0.0625F, -0.73F);
            poseStack.rotate(Axis.XP.rotationDegrees(90.0F));
            float scale = 1.5F;
            poseStack.scale(scale, scale, scale);
            item.submit(poseStack, submitNodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
            poseStack.popPose();
        }
    }
}
