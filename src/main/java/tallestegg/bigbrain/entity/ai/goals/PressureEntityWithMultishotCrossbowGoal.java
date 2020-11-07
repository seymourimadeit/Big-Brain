package tallestegg.bigbrain.entity.ai.goals;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.ICrossbowUser;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.entity.monster.MonsterEntity;

public class PressureEntityWithMultishotCrossbowGoal<T extends MonsterEntity & IRangedAttackMob & ICrossbowUser> extends RangedCrossbowAttackGoal<T> {
    private final T field_220748_a;
    
    public PressureEntityWithMultishotCrossbowGoal(T shooter, double speed, float p_i50322_4_) {
        super(shooter, 1.0D, 2.0F);
        this.field_220748_a = shooter;
    }
    
    @Override
    public boolean shouldExecute() {
        return super.shouldExecute() && EnchantmentHelper.getEnchantmentLevel(Enchantments.MULTISHOT, this.field_220748_a.getHeldItemMainhand()) > 0;
    }

}
