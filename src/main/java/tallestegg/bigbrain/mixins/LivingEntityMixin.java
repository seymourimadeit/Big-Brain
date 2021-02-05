package tallestegg.bigbrain.mixins;

import java.util.Random;
import java.util.UUID;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
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
import net.minecraft.potion.Effects;
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

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements IBucklerUser {
    private static final UUID CHARGE_SPEED_UUID = UUID.fromString("A2F995E8-B25A-4883-B9D0-93A676DC4045");
    private static final UUID KNOCKBACK_RESISTANCE_UUID = UUID.fromString("93E74BB2-05A5-4AC0-8DF5-A55768208A95");
    private static final AttributeModifier CHARGE_SPEED_BOOST = new AttributeModifier(CHARGE_SPEED_UUID, "Charge speed boost", 9.0D, AttributeModifier.Operation.MULTIPLY_BASE);
    private static final AttributeModifier KNOCKBACK_RESISTANCE = new AttributeModifier(KNOCKBACK_RESISTANCE_UUID, "Knockback reduction", 1.0D, AttributeModifier.Operation.ADDITION);
    private static final DataParameter<Boolean> DASHING = EntityDataManager.createKey(LivingEntity.class, DataSerializers.BOOLEAN);

    @Unique
    private int cooldown;

    @Unique
    private int bucklerUseTimer;

    @Shadow
    protected ItemStack activeItemStack;

    public LivingEntityMixin(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
    }

    @Inject(at = @At(value = "TAIL"), cancellable = true, method = "collideWithEntity")
    protected void collideWithEntity(Entity entityIn, CallbackInfo info) {
        if (this.isBucklerDashing() && BigBrainEnchantments.getBucklerEnchantsOnHands(BigBrainEnchantments.TURNING.get(), (LivingEntity) (Object) this) == 0) {
            int bangLevel = BigBrainEnchantments.getBucklerEnchantsOnHands(BigBrainEnchantments.BANG.get(), (LivingEntity) (Object) this);
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
            }
            if (bangLevel == 0) {
                entityIn.attackEntityFrom(DamageSource.causeMobDamage((LivingEntity) (Object) this), f);
                ((LivingEntity) entityIn).applyKnockback(f1 * 0.8F, (double) MathHelper.sin(this.rotationYaw * ((float) Math.PI / 180F)), (double) (-MathHelper.cos(this.rotationYaw * ((float) Math.PI / 180F))));
                if (entityIn instanceof PlayerEntity && ((PlayerEntity) entityIn).getActiveItemStack().isShield(((PlayerEntity) entityIn)))
                    ((PlayerEntity) entityIn).disableShield(true);
            } else {
                Hand hand = this.getHeldItemMainhand().getItem() instanceof BucklerItem ? Hand.MAIN_HAND : Hand.OFF_HAND;
                ItemStack stack = this.getHeldItem(hand);
                stack.damageItem(10 * bangLevel, ((LivingEntity) (Object) this), (player1) -> { // We will need feedback on this.
                    player1.sendBreakAnimation(hand);
                    if ((LivingEntity) (Object) this instanceof PlayerEntity)
                        net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem((PlayerEntity) (Object) this, this.activeItemStack, hand);
                });
                Explosion.Mode mode = BigBrainConfig.BangBlockDestruction ? Explosion.Mode.BREAK : Explosion.Mode.NONE;
                this.world.createExplosion((Entity) null, DamageSource.causeExplosionDamage((LivingEntity) (Object) this), (ExplosionContext) null, this.getPosX(), this.getPosY(), this.getPosZ(), (float) bangLevel * 1.0F, false, mode);
                this.setBucklerDashing(false);
            }
            this.setLastAttackedEntity(entityIn);
            if (this instanceof IOneCriticalAfterCharge)
                ((IOneCriticalAfterCharge) this).setCritical(BigBrainEnchantments.getBucklerEnchantsOnHands(BigBrainEnchantments.BANG.get(), (LivingEntity) (Object) this) == 0);
        }
    }

    @Shadow
    protected abstract Random getRNG();

    @Inject(at = @At(value = "RETURN"), cancellable = true, method = "getVisibilityMultiplier")
    public void getVisibilityMultiplier(@Nullable Entity lookingEntity, CallbackInfoReturnable<Double> info) {
        if (lookingEntity != null && ((LivingEntity) lookingEntity).isPotionActive(Effects.BLINDNESS))
            info.setReturnValue(info.getReturnValueD() * 0.3D);
    }

    @Inject(at = @At(value = "RETURN"), cancellable = true, method = "canBlockDamageSource")
    public void canBlockDamageSource(DamageSource damageSourceIn, CallbackInfoReturnable<Boolean> info) {
        boolean flag = false;
        if (!damageSourceIn.isUnblockable() && this.isActiveItemStackBlocking() && !flag && this.activeItemStack.getItem() instanceof BucklerItem) {
            info.setReturnValue(false);
        }
    }

    @Inject(at = @At(value = "TAIL"), method = "writeAdditional")
    public void writeAdditional(CompoundNBT compound, CallbackInfo info) {
        compound.putBoolean("BucklerDashing", this.isBucklerDashing());
        compound.putInt("ChargeCooldown", this.getCooldown());
    }

    @Inject(at = @At(value = "TAIL"), method = "readAdditional")
    public void readAdditional(CompoundNBT compound, CallbackInfo info) {
        this.setBucklerDashing(compound.getBoolean("BucklerDashing"));
        this.setCooldown(compound.getInt("ChargeCooldown"));
    }

    @Inject(at = @At(value = "TAIL"), method = "registerData")
    protected void registerData(CallbackInfo info) {
        this.dataManager.register(DASHING, false);
    }

    public int getCooldown() {
        return this.cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    public void setBucklerDashing(boolean dashing) {
        if (!dashing) {
            ModifiableAttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
            ModifiableAttributeInstance knockback = this.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
            if (speed == null || knockback == null) {
                return;
            }
            knockback.removeModifier(KNOCKBACK_RESISTANCE);
            if ((LivingEntity) (Object) this instanceof PlayerEntity)
                speed.removeModifier(CHARGE_SPEED_BOOST);
        }
        if (dashing) {
            ModifiableAttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
            ModifiableAttributeInstance knockback = this.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
            if (speed == null || knockback == null) {
                return;
            }
            knockback.removeModifier(KNOCKBACK_RESISTANCE);
            knockback.applyNonPersistentModifier(KNOCKBACK_RESISTANCE);
            if ((LivingEntity) (Object) this instanceof PlayerEntity) {
                speed.removeModifier(CHARGE_SPEED_BOOST);
                speed.applyNonPersistentModifier(CHARGE_SPEED_BOOST);
            }
        }
        this.dataManager.set(DASHING, dashing);
    }

    public boolean isBucklerDashing() {
        return this.dataManager.get(DASHING);
    }

    @Override
    public int getBucklerUseTimer() {
        return this.bucklerUseTimer;
    }

    @Override
    public void setBucklerUseTimer(int bucklerUseTimer) {
        this.bucklerUseTimer = bucklerUseTimer;
    }

    @Shadow
    protected abstract void setLastAttackedEntity(Entity entityIn);

    @Shadow
    protected abstract void resetActiveHand();

    @Shadow
    protected abstract ModifiableAttributeInstance getAttribute(Attribute knockbackResistance);

    @Shadow
    protected abstract ItemStack getHeldItem(Hand hand);

    @Shadow
    protected abstract ItemStack getHeldItemMainhand();

    @Shadow
    protected abstract boolean isActiveItemStackBlocking();
}
