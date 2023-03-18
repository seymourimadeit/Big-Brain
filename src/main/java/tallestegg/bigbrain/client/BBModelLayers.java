package tallestegg.bigbrain.client;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tallestegg.bigbrain.BigBrain;
import tallestegg.bigbrain.client.model.ModelGoldenBuckler;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BBModelLayers {
    public static ModelLayerLocation BUCKLER = new ModelLayerLocation(new ResourceLocation(BigBrain.MODID + "buckler"),
            "buckler");

    @SubscribeEvent
    public static void layerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(BUCKLER, ModelGoldenBuckler::createLayer);
    }
}
