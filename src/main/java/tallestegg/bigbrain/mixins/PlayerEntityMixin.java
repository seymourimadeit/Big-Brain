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
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.monster.WitherSkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Explosion;
import net.minecraft.world.ExplosionContext;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import tallestegg.bigbrain.BigBrainConfig;
import tallestegg.bigbrain.BigBrainEnchantments;
import tallestegg.bigbrain.BigBrainSounds;
import tallestegg.bigbrain.entity.IBucklerUser;
import tallestegg.bigbrain.entity.IOneCriticalAfterCharge;
import tallestegg.bigbrain.items.BucklerItem;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements IBucklerUser, IOneCriticalAfterCharge {
    private static final UUID CHARGE_SPEED_UUID = UUID.fromString("A2F995E8-B25A-4883-B9D0-93A676DC4045");
    private static final UUID KNOCKBACK_RESISTANCE_UUID = UUID.fromString("0DE9EFF3-457A-4060-BB03-F520F25713AF");
    private static final AttributeModifier CHARGE_SPEED_BOOST = new AttributeModifier(CHARGE_SPEED_UUID, "Charge speed boost", 9.0D, AttributeModifier.Operation.MULTIPLY_BASE);
    private static final AttributeModifier KNOCKBACK_RESISTANCE = new AttributeModifier(KNOCKBACK_RESISTANCE_UUID, "Knockback reduction", 1.0D, AttributeModifier.Operation.ADDITION);
    private static final DataParameter<Boolean> CHARGING = EntityDataManager.createKey(PlayerEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> CRITICAL = EntityDataManager.createKey(PlayerEntity.class, DataSerializers.BOOLEAN);

    @Unique
    private int cooldown;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> type, World worldIn) {
        super(type, worldIn);
    }

    @Override
    protected void collideWithEntity(Entity entityIn) {
        if (this.isCharging() && BigBrainEnchantments.getBucklerEnchantsOnHands(BigBrainEnchantments.TURNING.get(), this) == 0) {
            int bangLevel = BigBrainEnchantments.getBucklerEnchantsOnHands(BigBrainEnchantments.BANG.get(), this);
            float f = 6.0F + ((float) this.getRNG().nextInt(3));
            float f1 = 2.0F;
            if (f1 > 0.0F && entityIn instanceof LivingEntity) {
                for (int i = 0; i < 10; ++i) {
                    double d0 = this.rand.nextGaussian() * 0.02D;
                    double d1 = this.rand.nextGaussian() * 0.02D;
                    double d2 = this.rand.nextGaussian() * 0.02D;
                    BasicParticleType type = entityIn instanceof WitherEntity || entityIn instanceof WitherSkeletonEntity ? ParticleTypes.SMOKE : ParticleTypes.CLOUD;
                    if (world instanceof ServerWorld) {
                        // Collision is done on the server side, so a server side method must be used.
                        ((ServerWorld) world).spawnParticle(type, entityIn.getPosXRandom(1.0D), entityIn.getPosYRandom() + 1.0D, entityIn.getPosZRandom(1.0D), 1, d0, d1, d2, 1.0D);
                        if (!this.isSilent())
                            ((ServerWorld) world).playSound((PlayerEntity) null, (double) this.getPosition().getX(), (double) this.getPosition().getY(), (double) this.getPosition().getZ(), BigBrainSounds.SHIELD_BASH.get(), this.getSoundCategory(), 0.1F, 0.8F + this.rand.nextFloat() * 0.4F);
                    }
                }
                if (bangLevel == 0)
                    ((LivingEntity) entityIn).applyKnockback(f1 * 0.8F, (double) MathHelper.sin(this.rotationYaw * ((float) Math.PI / 180F)), (double) (-MathHelper.cos(this.rotationYaw * ((float) Math.PI / 180F))));
                this.setMotion(this.getMotion().mul(0.6D, 1.0D, 0.6D));
            }
            if (bangLevel == 0) {
                entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), f);
            } else {
                Hand hand = this.getHeldItemMainhand().getItem() instanceof BucklerItem ? Hand.MAIN_HAND : Hand.OFF_HAND;
                ItemStack stack = this.getHeldItem(hand);
                stack.damageItem(10 * bangLevel, this, (player1) -> { // We will need feedback on this.
                    player1.sendBreakAnimation(hand);
                });
                this.world.createExplosion((Entity) null, DamageSource.causeExplosionDamage(this), (ExplosionContext) null, this.getPosX(), this.getPosY(), this.getPosZ(), (float) bangLevel * 1.0F, false, Explosion.Mode.NONE);
                this.setCharging(false);
            }
            this.setLastAttackedEntity(entityIn);
            this.setCritical(BigBrainEnchantments.getBucklerEnchantsOnHands(BigBrainEnchantments.BANG.get(), this) == 0);
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
            if (this.cooldown > BigBrainConfig.BucklerRunTime)
                this.cooldown = BigBrainConfig.BucklerRunTime;
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

    @Override
    public void swing(Hand handIn, boolean updateSelf) {
        super.swing(handIn, updateSelf);
        if (this.isCritical())
            this.setCritical(false);
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
            ModifiableAttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
            ModifiableAttributeInstance knockback = this.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
            if (speed == null || knockback == null) {
                return;
            }
            knockback.removeModifier(KNOCKBACK_RESISTANCE);
            speed.removeModifier(CHARGE_SPEED_BOOST);
        }
        if (charging) {
            ModifiableAttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
            ModifiableAttributeInstance knockback = this.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
            if (speed == null || knockback == null) {
                return;
            }
            knockback.removeModifier(KNOCKBACK_RESISTANCE);
            knockback.applyNonPersistentModifier(KNOCKBACK_RESISTANCE);
            speed.removeModifier(CHARGE_SPEED_BOOST);
            speed.applyNonPersistentModifier(CHARGE_SPEED_BOOST);
        }
        this.dataManager.set(CHARGING, charging);
    }

    public boolean isCharging() {
        return this.dataManager.get(CHARGING);
    }
}
