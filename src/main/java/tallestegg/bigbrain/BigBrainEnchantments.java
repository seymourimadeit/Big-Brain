package tallestegg.bigbrain;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import tallestegg.bigbrain.enchantments.BangEnchantment;
import tallestegg.bigbrain.enchantments.TurningEnchantment;
import tallestegg.bigbrain.items.BucklerItem;

@Mod.EventBusSubscriber(modid = BigBrain.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BigBrainEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, BigBrain.MODID);
    public static final RegistryObject<Enchantment> TURNING = ENCHANTMENTS.register("turning", () -> new TurningEnchantment(Enchantment.Rarity.COMMON, EquipmentSlot.MAINHAND));
    public static final RegistryObject<Enchantment> BANG = ENCHANTMENTS.register("bang", () -> new BangEnchantment(Enchantment.Rarity.COMMON, EquipmentSlot.MAINHAND));

    public static final EnchantmentCategory BUCKLER = EnchantmentCategory.create("buckler", (item) -> (item instanceof BucklerItem));

    public static int getTurning(LivingEntity player) {
        return EnchantmentHelper.getEnchantmentLevel(TURNING.get(), player);
    }

    public static int getBucklerEnchantsOnHands(Enchantment enchantment, LivingEntity player) {
        InteractionHand hand = player.getMainHandItem().getItem() instanceof BucklerItem ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        ItemStack stack = player.getItemInHand(hand);
        return EnchantmentHelper.getItemEnchantmentLevel(enchantment, stack);
    }
}
