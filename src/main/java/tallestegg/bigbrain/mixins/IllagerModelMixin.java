package tallestegg.bigbrain.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.item.SpyglassItem;

@Mixin(IllagerModel.class)
public abstract class IllagerModelMixin<T extends AbstractIllager> extends HierarchicalModel<T> {

    @Shadow
    @Final
    private ModelPart leftArm;

    @Shadow
    @Final
    private ModelPart rightArm;

    @Shadow
    @Final
    private ModelPart head;

    @Inject(at = @At("TAIL"), method = "setupAnim")
    public void setupAnim(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch, CallbackInfo info) {
        if (entityIn.getMainArm() == HumanoidArm.RIGHT) {
            this.zoomInAnimRight(InteractionHand.MAIN_HAND, entityIn);
            this.zoomInAnimLeft(InteractionHand.OFF_HAND, entityIn);
        } else if (entityIn.getMainArm() == HumanoidArm.LEFT) {
            this.zoomInAnimLeft(InteractionHand.MAIN_HAND, entityIn);
            this.zoomInAnimRight(InteractionHand.OFF_HAND, entityIn);
        }
    }

    public void zoomInAnimRight(InteractionHand hand, T entityIn) {
        if (entityIn.getUsedItemHand() == hand && entityIn.getUseItem().getItem() instanceof SpyglassItem) {
            this.rightArm.xRot = Mth.clamp(this.head.xRot - 1.9198622F - (entityIn.isCrouching() ? 0.2617994F : 0.0F),
                    -2.4F, 3.3F);
            this.rightArm.yRot = this.head.yRot - 0.2617994F;
        }
    }

    public void zoomInAnimLeft(InteractionHand hand, T entityIn) {
        if (entityIn.getUsedItemHand() == hand && entityIn.getUseItem().getItem() instanceof SpyglassItem) {
            this.leftArm.xRot = Mth.clamp(this.head.xRot - 1.9198622F - (entityIn.isCrouching() ? 0.2617994F : 0.0F),
                    -2.4F, 3.3F);
            this.leftArm.yRot = this.head.yRot + 0.2617994F;
        }
    }
}
