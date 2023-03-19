package tallestegg.bigbrain.client;

import net.minecraft.client.renderer.entity.DrownedRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tallestegg.bigbrain.BigBrainConfig;
import tallestegg.bigbrain.client.renderers.layers.DrownedGlowLayer;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class BigBrainRendererEvents {
    @SubscribeEvent
    public static void addRenderLayers(EntityRenderersEvent.AddLayers event) {
        if (BigBrainConfig.CLIENT.drownedGlow.get()) {
            if (event.getRenderer(EntityType.DROWNED) instanceof DrownedRenderer renderer)
                renderer.addLayer(new DrownedGlowLayer<>(renderer));
        }
    }
}
