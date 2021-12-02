package tallestegg.bigbrain;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import tallestegg.bigbrain.items.BucklerItem;

@Mod.EventBusSubscriber(modid = BigBrain.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BigBrainItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, BigBrain.MODID);
    public static final RegistryObject<BucklerItem> BUCKLER = ITEMS.register("buckler",  () -> new BucklerItem((new Item.Properties()).durability(64).tab(CreativeModeTab.TAB_COMBAT)));
}

