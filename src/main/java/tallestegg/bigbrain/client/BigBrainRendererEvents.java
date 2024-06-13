package tallestegg.bigbrain.client;

import net.minecraft.client.renderer.entity.DrownedRenderer;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import tallestegg.bigbrain.BigBrainConfig;
import tallestegg.bigbrain.client.renderers.layers.DrownedGlowLayer;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class BigBrainRendererEvents {
    @SubscribeEvent
    public static void addRenderLayers(EntityRenderersEvent.AddLayers event) {
        if (BigBrainConfig.CLIENT.drownedGlow.get()) {
            DrownedRenderer renderer = event.getRenderer(EntityType.DROWNED);
            renderer.addLayer(new DrownedGlowLayer<>(renderer));
        }
    }
}
