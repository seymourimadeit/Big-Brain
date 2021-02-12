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
        compound.putInt("BucklerUseTimer", this.getBucklerUseTimer());
    }

    @Inject(at = @At(value = "TAIL"), method = "readAdditional")
    public void readAdditional(CompoundNBT compound, CallbackInfo info) {
        this.setBucklerDashing(compound.getBoolean("BucklerDashing"));
        this.setCooldown(compound.getInt("ChargeCooldown"));
        this.setBucklerUseTimer(compound.getInt("BucklerUseTimer"));
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
