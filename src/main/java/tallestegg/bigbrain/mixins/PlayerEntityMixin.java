package tallestegg.bigbrain.mixins;

import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import tallestegg.bigbrain.entity.IBucklerUser;
import tallestegg.bigbrain.entity.IOneCriticalAfterCharge;
import tallestegg.bigbrain.items.BucklerItem;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements IBucklerUser, IOneCriticalAfterCharge {
    private static final UUID CHARGE_SPEED_UUID = UUID.fromString("A2F995E8-B25A-4883-B9D0-93A676DC4045");
    private static final AttributeModifier CHARGE_SPEED_BOOST = new AttributeModifier(CHARGE_SPEED_UUID, "Charge speed boost", 9.0D, AttributeModifier.Operation.MULTIPLY_TOTAL);
    private static final DataParameter<Boolean> CHARGING = EntityDataManager.createKey(PlayerEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> CRITICAL = EntityDataManager.createKey(PlayerEntity.class, DataSerializers.BOOLEAN);
    
    @Unique
    private int cooldown;

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
                this.setMotion(this.getMotion().mul(0.6D, 1.0D, 0.6D));
            }

            entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), f);
            this.setCritical(true);
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
    
    /*@Override
    public void swing(Hand handIn, boolean updateSelf) {
        super.swing(handIn, updateSelf);
        if (this.isCritical()) 
            this.setCritical(false);
    }*/

    @Inject(at = @At(value = "TAIL"), method = "writeAdditional")
    public void writeAdditional(CompoundNBT compound, CallbackInfo info) {
        compound.putBoolean("Charging", this.isCharging());
        compound.putBoolean("Critical", this.isCritical());
        compound.putInt("ChargeCooldown", this.getCooldown());
    }

    @Inject(at = @At(value = "TAIL"), method = "readAdditional")
    public void readAdditional(CompoundNBT compound, CallbackInfo info) {
        this.setCritical(compound.getBoolean("Critical"));
        this.setCharging(compound.getBoolean("Charging"));
        this.setCooldown(compound.getInt("ChargeCooldown"));
    }
    
    @Inject(at = @At(value = "TAIL"), method = "registerData")
    protected void registerData(CallbackInfo info) {
        this.dataManager.register(CHARGING, false);
        this.dataManager.register(CRITICAL, false);
    }

    public int getCooldown() {
        return this.cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    public boolean isCritical() {
        return this.dataManager.get(CRITICAL);
    }

    public void setCritical(boolean critical) {
        this.dataManager.set(CRITICAL, critical);
    }

    public void setCharging(boolean charging) {
        if (!charging) {
            ModifiableAttributeInstance modifiableattributeinstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
            if (modifiableattributeinstance == null) {
                return;
            }
            modifiableattributeinstance.removeModifier(CHARGE_SPEED_BOOST);
        }
        if (charging) {
            ModifiableAttributeInstance modifiableattributeinstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
            if (modifiableattributeinstance == null) {
                return;
            }
            modifiableattributeinstance.removeModifier(CHARGE_SPEED_BOOST);

            modifiableattributeinstance.applyNonPersistentModifier(CHARGE_SPEED_BOOST);
        }
        this.dataManager.set(CHARGING, charging);
    }

    public boolean isCharging() {
        return this.dataManager.get(CHARGING);
    }
}
