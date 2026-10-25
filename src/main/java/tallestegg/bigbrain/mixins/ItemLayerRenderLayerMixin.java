package tallestegg.bigbrain.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tallestegg.bigbrain.client.BigBrainAnimationHelper;

@Mixin(ItemInHandLayer.class)
public abstract class ItemLayerRenderLayerMixin<S extends ArmedEntityRenderState, M extends EntityModel<S> & ArmedModel<S>> extends RenderLayer<S, M> {
    public ItemLayerRenderLayerMixin(RenderLayerParent<S, M> renderer) {
        super(renderer);
    }

    @Inject(at = @At("HEAD"), method = "submitArmWithItem", cancellable = true)
    public void renderArmWithItem(S state, ItemStackRenderState item, ItemStack itemStack, HumanoidArm arm, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, CallbackInfo ci) {
        if (state instanceof IllagerRenderState illagerRenderState) {
            HumanoidArm offhandArm = illagerRenderState.mainArm == HumanoidArm.RIGHT ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
            if (BigBrainAnimationHelper.checkIfUsingSpyglass(illagerRenderState, InteractionHand.OFF_HAND) && arm == offhandArm) {
                BigBrainAnimationHelper.renderItemHeldToEye(this, illagerRenderState, item, arm, poseStack, submitNodeCollector, lightCoords);
                ci.cancel();
            }
        }
    }
}
