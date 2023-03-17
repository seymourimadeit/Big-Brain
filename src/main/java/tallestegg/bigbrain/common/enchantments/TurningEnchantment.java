package tallestegg.bigbrain.common.enchantments;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.entity.EquipmentSlot;

public class TurningEnchantment extends Enchantment {
    public TurningEnchantment(Enchantment.Rarity rarity, EquipmentSlot... slots) {
        super(rarity, BigBrainEnchantments.BUCKLER, slots);
    }

    @Override
    public int getMinCost(int enchantmentLevel) {
        return 1 + (enchantmentLevel - 1) * 10;
    }

    @Override
    public int getMaxCost(int enchantmentLevel) {
        return 15;
    }

    @Override
    public boolean isTreasureOnly() {
        return false;
    }

    @Override
    public boolean isTradeable() {
        return false;
    }

    @Override
    public boolean isDiscoverable() {
        return true;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }
    
    @Override
    public boolean checkCompatibility(Enchantment ench) {
        return ench instanceof BangEnchantment ? false : super.checkCompatibility(ench);
    }
}
