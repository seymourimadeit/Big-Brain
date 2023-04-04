package tallestegg.bigbrain;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import tallestegg.bigbrain.client.BigBrainSounds;
import tallestegg.bigbrain.common.enchantments.BigBrainEnchantments;
import tallestegg.bigbrain.common.items.BigBrainItems;
import tallestegg.bigbrain.common.items.BucklerItem;
import tallestegg.bigbrain.networking.BigBrainNetworking;

@Mod(BigBrain.MODID)
public class BigBrain {
    public static final String MODID = "bigbrain";

    public BigBrain() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::addCreativeTabs);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, BigBrainConfig.COMMON_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, BigBrainConfig.CLIENT_SPEC);
        BigBrainItems.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        BigBrainSounds.SOUNDS.register(FMLJavaModLoadingContext.get().getModEventBus());
        BigBrainEnchantments.ENCHANTMENTS.register(FMLJavaModLoadingContext.get().getModEventBus());
        BigBrainNetworking.registerPackets();
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
    }

    private void addCreativeTabs(final CreativeModeTabEvent.BuildContents event) {
        if (event.getTab() == CreativeModeTabs.COMBAT)
            event.accept(BigBrainItems.BUCKLER.get());
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new ItemModelHandler());
    }

    public static class ItemModelHandler {
        public ItemModelHandler() {
            ItemProperties.register(BigBrainItems.BUCKLER.get(), new ResourceLocation("blocking"),
                    (stack, clientWorld, livingEntity, useTime) -> {
                        boolean active = livingEntity != null && livingEntity.isUsingItem()
                                && livingEntity.getUseItem() == stack
                                || livingEntity != null && BucklerItem.isReady(stack);
                        return livingEntity != null && active ? 1.0F : 0.0F;
                    });
        }
    }
}
