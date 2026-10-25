package tallestegg.bigbrain.client;

import com.google.common.reflect.TypeToken;
import net.minecraft.client.renderer.entity.ArmadilloRenderer;
import net.minecraft.client.renderer.entity.DrownedRenderer;
import net.minecraft.client.renderer.entity.HuskRenderer;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.illager.AbstractIllager;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import tallestegg.bigbrain.BigBrain;
import tallestegg.bigbrain.BigBrainConfig;
import tallestegg.bigbrain.client.renderers.layers.ArmadilloCrackLayer;
import tallestegg.bigbrain.client.renderers.layers.DrownedGlowLayer;
import tallestegg.bigbrain.common.capabilities.BigBrainCapabilities;

@EventBusSubscriber(value = Dist.CLIENT)
public class BigBrainRendererEvents {
    public static final ContextKey<Integer> CRACK_LAYER = new ContextKey<>(Identifier.fromNamespaceAndPath(BigBrain.MODID, "crack_layer_context"));
    public static final ContextKey<Boolean> BURROWING = new ContextKey<>(Identifier.fromNamespaceAndPath(BigBrain.MODID, "burrowing"));
    public static final ContextKey<ItemStack> OFFHAND_STACK = new ContextKey<>(Identifier.fromNamespaceAndPath(BigBrain.MODID, "offhand_stack"));
    public static final ContextKey<Integer> STACK_USE_TICKS = new ContextKey<>(Identifier.fromNamespaceAndPath(BigBrain.MODID, "offhand_stack_use_ticks"));
    public static final ContextKey<InteractionHand> CORRECT_USE_ITEM_HAND = new ContextKey<>(Identifier.fromNamespaceAndPath(BigBrain.MODID, "correct_interaction_hand"));

    @SubscribeEvent
    public static void addRenderLayers(EntityRenderersEvent.AddLayers event) {
        if (BigBrainConfig.CLIENT.drownedGlow.get()) {
            DrownedRenderer renderer = event.getRenderer(EntityTypes.DROWNED);
            renderer.addLayer(new DrownedGlowLayer<>(renderer));
        }
        ArmadilloRenderer renderer = event.getRenderer(EntityTypes.ARMADILLO);
        renderer.addLayer(new ArmadilloCrackLayer(renderer));
    }

    @SubscribeEvent
    public static void registerRenderStateModifiers(RegisterRenderStateModifiersEvent event) {
        event.registerEntityModifier(new TypeToken<ArmadilloRenderer>() {
        }, (entity, state) -> state.setRenderData(CRACK_LAYER, entity.getExistingDataOrNull(BigBrainCapabilities.SHELL_HEALTH.get())));
        event.registerEntityModifier(new TypeToken<HuskRenderer>() {
        }, (entity, state) -> state.setRenderData(BURROWING, entity.getExistingDataOrNull(BigBrainCapabilities.BURROWING.get())));
        event.registerEntityModifier(new TypeToken<IllagerRenderer<AbstractIllager, IllagerRenderState>>() {
        }, (entity, state) -> {
            state.setRenderData(OFFHAND_STACK, entity.getOffhandItem());
            state.setRenderData(STACK_USE_TICKS, entity.getUseItemRemainingTicks());
            state.setRenderData(CORRECT_USE_ITEM_HAND, entity.getUsedItemHand());
        });
    }
}
