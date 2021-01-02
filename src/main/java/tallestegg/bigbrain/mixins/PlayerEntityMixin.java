package tallestegg.bigbrain.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import tallestegg.bigbrain.entity.IBucklerUser;
import tallestegg.bigbrain.items.BucklerItem;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements IBucklerUser {
    @Unique
    private int cooldown;

    @Unique
    private boolean charging;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> type, World worldIn) {
        super(type, worldIn);
    }

    @Override
    protected void collideWithEntity(Entity entityIn) {
        if (this.isCharging()) {
            float f = 5.0F + this.getRNG().nextInt(1);
            float f1 = 2.0F;
            if (f1 > 0.0F && entityIn instanceof LivingEntity) {
                ((LivingEntity) entityIn).applyKnockback(f1 * 0.5F, (double) MathHelper.sin(this.rotationYaw * ((float) Math.PI / 180F)), (double) (-MathHelper.cos(this.rotationYaw * ((float) Math.PI / 180F))));
                this.setMotion(this.getMotion().mul(0.6D, 5.0D, 0.6D));
            }

            entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), f);
        }
        super.collideWithEntity(entityIn);
    }

    // We can't use a forge event for this due to the fact we have to do to this
    // stuff on the
    // livingTick() method.
    @Inject(at = @At(value = "TAIL"), method = "livingTick()V")
    public void livingTick(CallbackInfo info) {
        if (!this.isCharging()) {
            ++this.cooldown;
            if (this.cooldown > 15)
                this.cooldown = 15;
        }

        if (this.isCharging()) {
            BucklerItem.moveFowards(this);
            this.cooldown--;
        }
        if (cooldown == 0 || cooldown < 0) {
            this.setCharging(false);
            this.cooldown = 0;
        }
    }

    public int getCooldown() {
        return this.cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    public void setCharging(boolean charging) {
        this.charging = charging;
        if (!charging) {
            ModifiableAttributeInstance modifiableattributeinstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
            if (modifiableattributeinstance == null) {
                return;
            }
            modifiableattributeinstance.removeModifier(BucklerItem.CHARGE_SPEED_BOOST);
        }
    }

    public boolean isCharging() {
        return this.charging;
    }
}
