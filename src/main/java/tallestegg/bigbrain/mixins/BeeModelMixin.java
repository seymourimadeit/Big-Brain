package tallestegg.bigbrain.mixins;


import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.animal.bee.BeeModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.BeeRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.bee.Bee;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tallestegg.bigbrain.BigBrainConfig;

@Mixin(BeeModel.class)
public abstract class BeeModelMixin<T extends BeeRenderState> extends EntityModel<T> {
    @Shadow
    @Final
    private ModelPart bone;

    protected BeeModelMixin(ModelPart root) {
        super(root);
    }

    @Inject(at = @At(value = "TAIL"), method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/BeeRenderState;)V", cancellable = true)
    public void setupAnim(BeeRenderState state, CallbackInfo ci) {
        boolean flag = state.isOnGround;
        if (!flag && !state.isAngry && BigBrainConfig.CLIENT.bedrockBeeAnim.get()) {
            float f1 = Mth.cos(state.ageInTicks * 0.18F);
            bone.y = bone.y + (Mth.cos(state.ageInTicks) * 3.6F) / 24F;
            bone.yRot = 0.1F + f1 * (float) Math.PI * 0.095F;
        }
    }
}
