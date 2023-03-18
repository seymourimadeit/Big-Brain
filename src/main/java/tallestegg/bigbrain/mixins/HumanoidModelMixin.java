package tallestegg.bigbrain.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import tallestegg.bigbrain.common.enchantments.entity.IBucklerUser;
import tallestegg.bigbrain.common.items.BucklerItem;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin<T extends LivingEntity> extends AgeableListModel<T> {
    @Shadow
    @Final
    public ModelPart leftArm;

    @Shadow
    @Final
    public ModelPart rightArm;

    @Inject(at = @At("TAIL"), method = "setupAnim")
    public void setupAnim(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo info) {
        if (entityIn.getMainArm() == HumanoidArm.RIGHT) {
            this.bucklerAnimationsRightArm(InteractionHand.MAIN_HAND, entityIn);
            this.bucklerAnimationsLeftArm(InteractionHand.OFF_HAND, entityIn);
        } else {
            this.bucklerAnimationsLeftArm(InteractionHand.MAIN_HAND, entityIn);
            this.bucklerAnimationsRightArm(InteractionHand.OFF_HAND, entityIn);
        }
    }

    public void bucklerAnimationsLeftArm(InteractionHand hand, T entityIn) {
        if (entityIn.getUsedItemHand() == hand && entityIn.getItemInHand(entityIn.getUsedItemHand()).getItem() instanceof BucklerItem) {
            float useDuration = (float) entityIn.getItemInHand(entityIn.getUsedItemHand()).getUseDuration();
            float useDurationClamped = Mth.clamp((float) entityIn.getTicksUsingItem(), 0.0F, useDuration);
            float result = useDurationClamped / useDuration;
            this.leftArm.yRot = Mth.lerp(result, leftArm.yRot, 1.1466812652970528F);
            this.leftArm.xRot = Mth.lerp(result, leftArm.xRot, this.leftArm.xRot * 0.1F - 1.5F);
        }
        if (entityIn instanceof IBucklerUser)
            if (((IBucklerUser) entityIn).isBucklerDashing() && BucklerItem.isReady(entityIn.getItemInHand(hand))) {
                ItemStack handItems = hand == InteractionHand.MAIN_HAND ? entityIn.getOffhandItem() : entityIn.getMainHandItem();
                if (!handItems.isEmpty()) {
                    this.rightArm.xRot = 0.5F - (float) Math.PI * 0.5F - 0.9424779F;
                    this.rightArm.yRot = ((float) Math.PI / 6F);
                }
                this.leftArm.xRot = this.leftArm.xRot * 0.1F - 1.5F;
                this.leftArm.yRot = 1.1466812652970528F;
            }
    }

    public void bucklerAnimationsRightArm(InteractionHand hand, T entityIn) {
        if (entityIn.getUsedItemHand() == hand && entityIn.getItemInHand(entityIn.getUsedItemHand()).getItem() instanceof BucklerItem) {
            float useDuration = (float) entityIn.getItemInHand(entityIn.getUsedItemHand()).getUseDuration();
            float useDurationClamped = Mth.clamp((float) entityIn.getTicksUsingItem(), 0.0F, useDuration);
            float result = useDurationClamped / useDuration;
            this.rightArm.yRot = Mth.lerp(result, rightArm.yRot, -1.1466812652970528F);
            this.rightArm.xRot = Mth.lerp(result, rightArm.xRot, this.rightArm.xRot * 0.1F - 1.5F);
        }
        if (entityIn instanceof IBucklerUser) {
            if (((IBucklerUser) entityIn).isBucklerDashing() && BucklerItem.isReady(entityIn.getItemInHand(hand))) {
                ItemStack handItems = hand == InteractionHand.MAIN_HAND ? entityIn.getOffhandItem() : entityIn.getMainHandItem();
                if (!handItems.isEmpty()) {
                    this.leftArm.xRot = 0.5F - (float) Math.PI * 0.5F - 0.9424779F;
                    this.leftArm.yRot = (-(float) Math.PI / 6F);
                }
                this.rightArm.xRot = 0.0F * 0.1F - 1.5F;
                this.rightArm.yRot = -1.1466812652970528F;
            }
        }
    }
}
