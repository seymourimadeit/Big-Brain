package tallestegg.bigbrain;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import tallestegg.bigbrain.items.BucklerItem;

@Mod(BigBrain.MODID)
public class BigBrain {
    public static final String MODID = "bigbrain";

    public BigBrain() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::attributeChange);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, BigBrainConfig.COMMON_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, BigBrainConfig.CLIENT_SPEC);
        BigBrainItems.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        BigBrainSounds.SOUNDS.register(FMLJavaModLoadingContext.get().getModEventBus());
        BigBrainEnchantments.ENCHANTMENTS.register(FMLJavaModLoadingContext.get().getModEventBus());
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(BigBrainItems.BUCKLER.get(), new ResourceLocation("blocking"), (stack, clientWorld, livingEntity, useTime) -> {
                boolean active = livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() == stack || livingEntity != null && BucklerItem.isReady(stack);
                return livingEntity != null && active ? 1.0F : 0.0F;
            });
        });
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
    }

    private void processIMC(final InterModProcessEvent event) {
    }

    private void attributeChange(final EntityAttributeModificationEvent event) {
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class TextureHandler {
        @SuppressWarnings("deprecation")
        @SubscribeEvent
        public static void onStitch(TextureStitchEvent.Pre event) {
            if (event.getMap().location().equals(TextureAtlas.LOCATION_BLOCKS)) {
                event.addSprite(BucklerTexture.BUCKLER_TEXTURE.texture());
            }
        }
    }
}
