package tallestegg.bigbrain.client;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import tallestegg.bigbrain.BigBrainConfig;
import tallestegg.bigbrain.client.renderers.layers.ArmadilloCrackLayer;
import tallestegg.bigbrain.client.renderers.layers.DrownedGlowLayer;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class BigBrainRendererEvents {
    @SubscribeEvent
    public static void addRenderLayers(EntityRenderersEvent.AddLayers event) {
        EntityRenderer renderer;
        if (BigBrainConfig.CLIENT.drownedGlow.get()) {
            renderer = event.getRenderer(EntityType.DROWNED);
            ((LivingEntityRenderer)renderer).addLayer(new DrownedGlowLayer<>(((LivingEntityRenderer)renderer)));
        }
        renderer = event.getRenderer(EntityType.ARMADILLO);
        ((LivingEntityRenderer)renderer).addLayer(new ArmadilloCrackLayer(((LivingEntityRenderer)renderer)));
    }
}
