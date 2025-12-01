package tallestegg.bigbrain.mixins;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Husk.class)
public abstract class HuskMixin extends Zombie {
    public HuskMixin(EntityType<? extends Zombie> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public boolean canRiderInteract() {
        return true;
    }

    @Override
    protected float getStandingEyeHeight(Pose pPose, EntityDimensions pSize) {
        if (pPose == Pose.SWIMMING)
            return 0.5F;
        else
            return super.getStandingEyeHeight(pPose, pSize);
    }

    @Override
    public EntityDimensions getDimensions(Pose pPose) {
        if (pPose == Pose.SWIMMING) {
            return EntityDimensions.scalable(1.0F, 1.5F);
        } else {
            return super.getDimensions(pPose);
        }
    }
}
