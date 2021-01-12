package tallestegg.bigbrain.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.EquipmentSlotType;
import tallestegg.bigbrain.BigBrainEnchantments;

public class TurningEnchantment extends Enchantment {
    public TurningEnchantment(Enchantment.Rarity rarity, EquipmentSlotType... slots) {
        super(rarity, BigBrainEnchantments.BUCKLER, slots);
    }

    @Override
    public int getMinEnchantability(int enchantmentLevel) {
        return enchantmentLevel * 10;
    }

    @Override
    public int getMaxEnchantability(int enchantmentLevel) {
        return this.getMinEnchantability(enchantmentLevel) + 15;
    }

    @Override
    public boolean isTreasureEnchantment() {
        return true;
    }

    @Override
    public boolean canVillagerTrade() {
        return false;
    }

    @Override
    public boolean canGenerateInLoot() {
        return false;
    }

    public int getMaxLevel() {
        return 1;
    }
}
