package tallestegg.bigbrain.entity.ai;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.controller.MovementController;
import tallestegg.bigbrain.BigBrainEnchantments;
import tallestegg.bigbrain.entity.IBucklerUser;

public class PiglinBruteMoveController extends MovementController {

    public PiglinBruteMoveController(MobEntity mob) {
        super(mob);
    }

    @Override
    public void tick() {
        if (EnchantmentHelper.getEnchantmentLevel(BigBrainEnchantments.TURNING.get(), mob.getHeldItemOffhand()) > 0 && ((IBucklerUser) mob).isCharging() || !((IBucklerUser) mob).isCharging())
            super.tick();
    }

}
