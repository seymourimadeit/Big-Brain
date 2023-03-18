package tallestegg.bigbrain.common.entity.ai.goals;

import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.CrossbowItem;

public class PressureEntityWithMultishotCrossbowGoal<T extends Monster & RangedAttackMob & CrossbowAttackMob>
        extends RangedCrossbowAttackGoal<T> {
    private final T mob;

    public PressureEntityWithMultishotCrossbowGoal(T shooter, double speed, float p_i50322_4_) {
        super(shooter, speed, p_i50322_4_);
        this.mob = shooter;
    }

    @Override
    public boolean canUse() {
        return this.isValidTarget() && this.isHoldingCrossbow();
    }

    private boolean isHoldingCrossbow() {
        return EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MULTISHOT, this.mob.getMainHandItem()) > 0
                && this.mob.isHolding(is -> is.getItem() instanceof CrossbowItem);
    }

    private boolean isValidTarget() {
        return this.mob.getTarget() != null && this.mob.getTarget().isAlive();
    }
}
