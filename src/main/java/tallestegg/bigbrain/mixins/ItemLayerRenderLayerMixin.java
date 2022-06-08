package tallestegg.bigbrain.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpyglassItem;

@Mixin(ItemInHandLayer.class)
public abstract class ItemLayerRenderLayerMixin<T extends LivingEntity, M extends EntityModel<T> & ArmedModel>
        extends RenderLayer<T, M> {

    public ItemLayerRenderLayerMixin(RenderLayerParent<T, M> p_117346_) {
        super(p_117346_);
    }

    @Inject(at = @At("HEAD"), method = "renderArmWithItem", cancellable = true)
    public void renderArmWithItem(LivingEntity entity, ItemStack stack, ItemTransforms.TransformType p_174527_,
            HumanoidArm p_174528_, PoseStack p_174529_, MultiBufferSource p_174530_, int p_174531_, CallbackInfo info) {
        if (entity.getUseItem().getItem() instanceof SpyglassItem && entity.getUseItem() == stack) {
            this.renderArmWithSpyglass(entity, stack, p_174528_, p_174529_, p_174530_, p_174531_);
            info.cancel();
        }
    }

    private void renderArmWithSpyglass(LivingEntity p_174518_, ItemStack p_174519_, HumanoidArm p_174520_,
            PoseStack p_174521_, MultiBufferSource p_174522_, int p_174523_) {
        p_174521_.pushPose();
        ModelPart modelpart = ((HeadedModel) this.getParentModel()).getHead();
        float f = modelpart.xRot;
        modelpart.xRot = Mth.clamp(modelpart.xRot, (-(float) Math.PI / 6F), ((float) Math.PI / 2F));
        modelpart.translateAndRotate(p_174521_);
        modelpart.xRot = f;
        CustomHeadLayer.translateToHead(p_174521_, false);
        boolean flag = p_174520_ == HumanoidArm.LEFT;
        p_174521_.translate((double) ((flag ? -2.5F : 2.5F) / 16.0F), -0.0625D, 0.0D);
        Minecraft.getInstance().gameRenderer.itemInHandRenderer.renderItem(p_174518_, p_174519_,
                ItemTransforms.TransformType.HEAD, false, p_174521_, p_174522_, p_174523_);
        p_174521_.popPose();
    }
}
