package tallestegg.bigbrain.client.renderers.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.animal.armadillo.ArmadilloModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.ArmadilloRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import tallestegg.bigbrain.BigBrain;
import tallestegg.bigbrain.BigBrainConfig;
import tallestegg.bigbrain.client.BigBrainRendererEvents;

public class ArmadilloCrackLayer extends RenderLayer<ArmadilloRenderState, ArmadilloModel> {
    public ArmadilloCrackLayer(RenderLayerParent<ArmadilloRenderState, ArmadilloModel> layerParent) {
        super(layerParent);
    }

    public RenderType renderType(ArmadilloRenderState armadillo) {
        int armor = armadillo.getRenderData(BigBrainRendererEvents.CRACK_LAYER);
        if (armor < 13 && armor > 6) {
            return RenderTypes.entityTranslucent(Identifier.fromNamespaceAndPath(BigBrain.MODID, "textures/entity/armadillo/cracked_low.png"));
        } else if (armor <= 6 && armor > 0) {
            return RenderTypes.entityTranslucent(Identifier.fromNamespaceAndPath(BigBrain.MODID, "textures/entity/armadillo/cracked_medium.png"));
        } else if (armor <= 0) {
            return RenderTypes.entityTranslucent(Identifier.fromNamespaceAndPath(BigBrain.MODID, "textures/entity/armadillo/cracked_high.png"));
        } else {
            return null;
        }
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, ArmadilloRenderState state, float yRot, float xRot) {
        if (renderType(state) == null || !BigBrainConfig.COMMON.armadilloShell.get())
            return;
        submitNodeCollector.order(1).submitModel(this.getParentModel(), state, poseStack, this.renderType(state), lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);

    }
}
