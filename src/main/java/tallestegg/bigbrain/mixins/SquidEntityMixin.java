package tallestegg.bigbrain.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.entity.passive.WaterMobEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

@Mixin(SquidEntity.class)
public class SquidEntityMixin extends WaterMobEntity {
    protected SquidEntityMixin(EntityType<? extends WaterMobEntity> type, World p_i48565_2_) {
        super(type, p_i48565_2_);
    }

    //TODO make squids squirt an AOE cloud instead of just particles
    @Inject(at = @At(value = "HEAD", target = "net/minecraft/world/server/ServerWorld.spawnParticle(Lnet/minecraft/particles/IParticleData;DDDIDDDD)I"), method = "squirtInk()V")
    public void squirtInk(CallbackInfo info) {
        Vector3d vector3d1 = this.func_207400_b(new Vector3d((double)this.rand.nextFloat() * 0.6D - 0.3D, -1.0D, (double)this.rand.nextFloat() * 0.6D - 0.3D));
        Vector3d vector3d2 = vector3d1.scale(0.3D + (double)(this.rand.nextFloat() * 2.0F));
        if (this.getRevengeTarget() != null && this.isPlayerInInk(getRevengeTarget(), vector3d2))
            this.getRevengeTarget().addPotionEffect(new EffectInstance(Effects.BLINDNESS, 40));
    }

    @Shadow
    private Vector3d func_207400_b(Vector3d p_207400_1_) {
        return null;
    }

    @Unique
    public boolean isPlayerInInk(LivingEntity entity, Vector3d vec) {
        Vector3d vector3d2 = entity.getPositionVec();
        if (vector3d2 != null) {
            Vector3d vector3d1 = vector3d2.subtractReverse(this.getPositionVec()).normalize();
            vector3d1 = new Vector3d(vector3d1.x, 0.0D, vector3d1.z);
            if (vector3d1.dotProduct(vec) <= 0.0D)
                return true;
        }
        return false;
    }
}
