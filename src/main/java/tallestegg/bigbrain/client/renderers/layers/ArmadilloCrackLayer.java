package tallestegg.bigbrain.client.renderers.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ArmadilloModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import tallestegg.bigbrain.BigBrain;
import tallestegg.bigbrain.BigBrainConfig;
import tallestegg.bigbrain.common.capabilities.BigBrainCapabilities;

public class ArmadilloCrackLayer extends RenderLayer<Armadillo, ArmadilloModel> {
    private static final RenderType CRACKED = RenderType.entityCutoutNoCull(new ResourceLocation(BigBrain.MODID, "textures/entity/armadillo/cracked.png"));
    //private static final RenderType NAKED = RenderType.entityCutoutNoCull(new ResourceLocation(BigBrain.MODID, "textures/entity/armadillo/naked.png"));

    public ArmadilloCrackLayer(RenderLayerParent<Armadillo, ArmadilloModel> layerParent) {
        super(layerParent);
    }

    public RenderType renderType(Armadillo armadillo) {
        int armor = armadillo.getData(BigBrainCapabilities.SHELL_HEALTH.get());
        if (armor < 13)
            return CRACKED;
        return null;
    }

    @Override
    public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, Armadillo pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        if (renderType(pLivingEntity) == null || BigBrainConfig.COMMON.armadilloShell.get())
            return;
        VertexConsumer vertexconsumer = pBuffer.getBuffer(this.renderType(pLivingEntity));
        int armor = pLivingEntity.getData(BigBrainCapabilities.SHELL_HEALTH.get());
        if ((armor < 13 || armor <= 0) && this.renderType(pLivingEntity) != null)
            this.getParentModel().renderToBuffer(pPoseStack, vertexconsumer, 15728640, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
    }
}
