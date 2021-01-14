package tallestegg.bigbrain.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.entity.passive.WaterMobEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

@Mixin(SquidEntity.class)
public class SquidEntityMixin extends WaterMobEntity {
    protected SquidEntityMixin(EntityType<? extends WaterMobEntity> type, World p_i48565_2_) {
        super(type, p_i48565_2_);
    }

    @Inject(at = @At(value = "HEAD", target = "net/minecraft/world/server/ServerWorld.spawnParticle(Lnet/minecraft/particles/IParticleData;DDDIDDDD)I"), method = "squirtInk()V")
    public void squirtInk(CallbackInfo info) {
        Vector3d vector3d = this.func_207400_b(new Vector3d(0.0D, -1.0D, 0.0D)).add(this.getPosX(), this.getPosY(), this.getPosZ());
        AreaEffectCloudEntity areaeffectcloudentity = new AreaEffectCloudEntity(this.world, vector3d.getX(), vector3d.getY(), vector3d.getZ());
        areaeffectcloudentity.setOwner(this);
        areaeffectcloudentity.setParticleData(ParticleTypes.SQUID_INK);
        areaeffectcloudentity.setRadius(2.5F);
        areaeffectcloudentity.setRadiusOnUse(-0.5F);
        areaeffectcloudentity.setWaitTime(10);
        areaeffectcloudentity.setDuration(areaeffectcloudentity.getDuration() / 2);
        areaeffectcloudentity.setRadiusPerTick(-areaeffectcloudentity.getRadius() / (float) areaeffectcloudentity.getDuration());
        areaeffectcloudentity.addEffect(new EffectInstance(Effects.BLINDNESS, 50, 50, false, false));
        this.world.addEntity(areaeffectcloudentity);
    }

    @Shadow
    private Vector3d func_207400_b(Vector3d p_207400_1_) {
        return null;
    }

}
