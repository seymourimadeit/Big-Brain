package tallestegg.bigbrain.client.renderers.layers;

import net.minecraft.client.model.monster.zombie.DrownedModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import tallestegg.bigbrain.BigBrain;

public class DrownedGlowLayer<T extends ZombieRenderState, M extends DrownedModel> extends EyesLayer {
    private static final RenderType DROWNED_EYES = RenderTypes.eyes(Identifier.fromNamespaceAndPath(BigBrain.MODID, "textures/entity/zombie/drowned_glow_layer.png"));

    public DrownedGlowLayer(RenderLayerParent<T, M> p_117507_) {
        super(p_117507_);
    }

    public RenderType renderType() {
        return DROWNED_EYES;
    }
}
