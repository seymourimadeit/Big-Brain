package tallestegg.bigbrain.common.items;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import tallestegg.bigbrain.BigBrain;

@Mod.EventBusSubscriber(modid = BigBrain.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BigBrainItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, BigBrain.MODID);
    public static final RegistryObject<BucklerItem> BUCKLER = ITEMS.register("buckler",  () -> new BucklerItem((new Item.Properties()).durability(64)));

    public static ItemStack checkEachHandForBuckler(LivingEntity entity) {
        InteractionHand hand = entity.getMainHandItem().getItem() instanceof BucklerItem ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        ItemStack bucklerItemStack = entity.getItemInHand(hand);
        return bucklerItemStack;
    }
}

