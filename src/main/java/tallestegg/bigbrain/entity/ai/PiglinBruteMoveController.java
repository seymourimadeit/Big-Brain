package tallestegg.bigbrain.entity.ai;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.controller.MovementController;
import tallestegg.bigbrain.entity.IBucklerUser;

public class PiglinBruteMoveController extends MovementController {

    public PiglinBruteMoveController(MobEntity mob) {
        super(mob);
    }

    @Override
    public void tick() {
        if (!((IBucklerUser) mob).isCharging())
            super.tick();
    }

}
