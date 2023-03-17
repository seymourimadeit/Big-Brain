package tallestegg.bigbrain.mixins;

import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.BeeModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Bee;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tallestegg.bigbrain.BigBrain;
import tallestegg.bigbrain.BigBrainConfig;

@Mixin(BeeModel.class)
public abstract class BeeModelMixin<T extends Bee> extends AgeableListModel<T> {
    @Shadow
    private ModelPart bone;

    @Inject(at = @At(value = "TAIL"), method = "setupAnim(Lnet/minecraft/world/entity/animal/Bee;FFFFF)V", cancellable = true)
    public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch, CallbackInfo ci) {
        boolean flag = pEntity.isOnGround() && pEntity.getDeltaMovement().lengthSqr() < 1.0E-7D;
        if (!flag && !pEntity.isAngry() && BigBrainConfig.CLIENT.bedrockBeeAnim.get()) {
            float f1 = Mth.cos(pAgeInTicks * 0.18F);
            bone.y = bone.y + (Mth.cos(pAgeInTicks) * 3.6F) / 24F;
            bone.yRot = 0.1F + f1 * (float) Math.PI * 0.095F;
        }
    }
}
