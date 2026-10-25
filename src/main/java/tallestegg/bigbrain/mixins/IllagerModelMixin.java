package tallestegg.bigbrain.mixins;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.illager.IllagerModel;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tallestegg.bigbrain.client.BigBrainAnimationHelper;

@Mixin(IllagerModel.class)
public abstract class IllagerModelMixin<T extends IllagerRenderState> extends EntityModel<T> {

    @Shadow
    @Final
    private ModelPart leftArm;

    @Shadow
    @Final
    private ModelPart rightArm;

    @Shadow
    @Final
    private ModelPart head;

    protected IllagerModelMixin(ModelPart root) {
        super(root);
    }

    @Inject(at = @At("TAIL"), method = "setupAnim")
    public void setupAnim(T entityIn, CallbackInfo ci) {
        if (entityIn.mainArm == HumanoidArm.RIGHT) {
            this.zoomInAnimRight(InteractionHand.MAIN_HAND, entityIn, entityIn.mainArm);
            this.zoomInAnimLeft(InteractionHand.OFF_HAND, entityIn, entityIn.mainArm);
        } else if (entityIn.mainArm == HumanoidArm.LEFT) {
            this.zoomInAnimLeft(InteractionHand.MAIN_HAND, entityIn, entityIn.mainArm);
            this.zoomInAnimRight(InteractionHand.OFF_HAND, entityIn, entityIn.mainArm);
        }
    }

    public void zoomInAnimRight(InteractionHand hand, T entityIn, HumanoidArm arm) {
        BigBrainAnimationHelper.zoomInAnimRight(this.rightArm, this.head, hand, entityIn, arm);
    }

    public void zoomInAnimLeft(InteractionHand hand, T entityIn, HumanoidArm arm) {
        BigBrainAnimationHelper.zoomInAnimLeft(this.leftArm, this.head, hand, entityIn, arm);
    }
}
