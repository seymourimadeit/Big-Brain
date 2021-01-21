package tallestegg.bigbrain.mixins;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effects;
import net.minecraft.world.World;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
        // TODO Auto-generated constructor stub
    }

    @Inject(at = @At(value = "RETURN"), cancellable = true, method = "getVisibilityMultiplier")
    public void getVisibilityMultiplier(@Nullable Entity lookingEntity, CallbackInfoReturnable<Double> info) {
        if (lookingEntity != null && ((LivingEntity) lookingEntity).isPotionActive(Effects.BLINDNESS))
            info.setReturnValue(info.getReturnValueD() * 0.3D);
    }
}
