package tallestegg.bigbrain.entity.ai;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.controller.LookController;
import net.minecraft.util.math.vector.Vector3d;
import tallestegg.bigbrain.BigBrainEnchantments;
import tallestegg.bigbrain.entity.IBucklerUser;

public class PiglinBruteLookController extends LookController {
    public PiglinBruteLookController(MobEntity mob) {
        super(mob);
    }

    @Override
    public void tick() {
        if (((IBucklerUser) mob).isCharging() && EnchantmentHelper.getEnchantmentLevel(BigBrainEnchantments.TURNING.get(), mob.getHeldItemOffhand()) > 0 || (!((IBucklerUser) mob).isCharging())) {
            super.tick();
        }
    }

    @Override   
    public void setLookPosition(Vector3d lookVector) {
        if (((IBucklerUser) mob).isCharging() && EnchantmentHelper.getEnchantmentLevel(BigBrainEnchantments.TURNING.get(), mob.getHeldItemOffhand()) > 0 || (!((IBucklerUser) mob).isCharging())) {
            super.setLookPosition(lookVector);
        }
    }

    @Override
    protected boolean shouldResetPitch() {
        return ((IBucklerUser) mob).isCharging() && EnchantmentHelper.getEnchantmentLevel(BigBrainEnchantments.TURNING.get(), mob.getHeldItemOffhand()) > 0 || !((IBucklerUser) mob).isCharging();
    }

}
