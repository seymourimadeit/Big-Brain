package tallestegg.bigbrain.client.renderers.layers;

import net.minecraft.client.model.DrownedModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Drowned;
import tallestegg.bigbrain.BigBrain;

public class DrownedGlowLayer<T extends Drowned, M extends DrownedModel<T>> extends EyesLayer<T, M> {
    private static final RenderType DROWNED_EYES = RenderType.eyes(ResourceLocation.fromNamespaceAndPath(BigBrain.MODID, "textures/entity/zombie/drowned_glow_layer.png"));

    public DrownedGlowLayer(RenderLayerParent<T, M> p_117507_) {
        super(p_117507_);
    }

    public RenderType renderType() {
        return DROWNED_EYES;
    }
}
