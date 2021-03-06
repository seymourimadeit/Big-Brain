package tallestegg.bigbrain.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.EquipmentSlotType;
import tallestegg.bigbrain.BigBrainEnchantments;

public class BangEnchantment extends Enchantment {
    public BangEnchantment(Enchantment.Rarity rarity, EquipmentSlotType... slots) {
        super(rarity, BigBrainEnchantments.BUCKLER, slots);
    }

    @Override
    public int getMinEnchantability(int enchantmentLevel) {
        return 1 + (enchantmentLevel - 1) * 10;
    }

    @Override
    public int getMaxEnchantability(int enchantmentLevel) {
        return 50;
    }

    @Override
    public boolean isTreasureEnchantment() {
        return false;
    }

    @Override
    public boolean canVillagerTrade() {
        return false;
    }

    @Override
    public boolean canGenerateInLoot() {
        return true;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public boolean canApplyTogether(Enchantment ench) {
        return ench instanceof TurningEnchantment ? false : super.canApplyTogether(ench);
    }
}
