package tallestegg.bigbrain.entity.ai.goals;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.ICrossbowUser;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.item.CrossbowItem;

public class PressureEntityWithMultishotCrossbowGoal<T extends MonsterEntity & IRangedAttackMob & ICrossbowUser> extends RangedCrossbowAttackGoal<T> {
    private final T field_220748_a;

    public PressureEntityWithMultishotCrossbowGoal(T shooter, double speed, float p_i50322_4_) {
        super(shooter, speed, p_i50322_4_);
        this.field_220748_a = shooter;
    }

    @Override
    public boolean shouldExecute() {
        return this.func_220746_h() && this.func_220745_g();
    }

    private boolean func_220745_g() {
        return EnchantmentHelper.getEnchantmentLevel(Enchantments.MULTISHOT, this.field_220748_a.getHeldItemMainhand()) > 0 && this.canEquip();
    }

    private boolean func_220746_h() {
        return this.field_220748_a.getAttackTarget() != null && this.field_220748_a.getAttackTarget().isAlive();
    }

    public boolean canEquip() {
        return field_220748_a.func_233634_a_((p_233632_1_) -> {
            return p_233632_1_ instanceof CrossbowItem;
        });
    }

}
