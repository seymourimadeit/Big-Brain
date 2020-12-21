package tallestegg.bigbrain;

import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.util.ResourceLocation;

public class BucklerTexture {
    @SuppressWarnings("deprecation")
    public static final RenderMaterial BUCKLER_TEXTURE = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE, new ResourceLocation(BigBrain.MODID, "entity/buckler/golden_buckler"));
}
