package tallestegg.bigbrain;

import net.minecraft.client.resources.model.Material;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

public class BucklerTexture {
    @SuppressWarnings("deprecation")
    public static final Material BUCKLER_TEXTURE = new Material(TextureAtlas.LOCATION_BLOCKS, new ResourceLocation(BigBrain.MODID, "entity/buckler/golden_buckler"));
}
