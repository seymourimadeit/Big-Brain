package tallestegg.bigbrain;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import tallestegg.bigbrain.enchantments.TurningEnchantment;
import tallestegg.bigbrain.items.BucklerItem;

@Mod.EventBusSubscriber(modid = BigBrain.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BigBrainEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, BigBrain.MODID);
    public static final RegistryObject<Enchantment> TURNING = ENCHANTMENTS.register("turning", () -> new TurningEnchantment(Enchantment.Rarity.RARE, EquipmentSlotType.MAINHAND));

    public static final EnchantmentType BUCKLER = EnchantmentType.create("buckler", (item) -> (item instanceof BucklerItem));

    public static int getTurning(LivingEntity player) {
        return EnchantmentHelper.getMaxEnchantmentLevel(TURNING.get(), player);
    }

    public static int getTurningOnHands(LivingEntity player) {
        Hand hand = player.getHeldItemMainhand().getItem() instanceof BucklerItem ? Hand.MAIN_HAND : Hand.OFF_HAND;
        ItemStack stack = player.getHeldItem(hand);
        return EnchantmentHelper.getEnchantmentLevel(TURNING.get(), stack);
    }
}
