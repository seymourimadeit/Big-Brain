package tallestegg.bigbrain.mixins;

import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
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
    public EntityDimensions getDefaultDimensions(Pose pPose) {
        return pPose == Pose.SWIMMING ? EntityDimensions.scalable(1.0F, 1.5F).withEyeHeight(0.5F) : super.getDefaultDimensions(pPose);
    }
}
