package tallestegg.bigbrain.mixins;

import java.util.UUID;
import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import tallestegg.bigbrain.entity.IBucklerUser;
import tallestegg.bigbrain.items.BucklerItem;

// TODO convert this into a capability when i port to 1.17
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements IBucklerUser {
    private static final UUID CHARGE_SPEED_UUID = UUID.fromString("A2F995E8-B25A-4883-B9D0-93A676DC4045");
    private static final UUID KNOCKBACK_RESISTANCE_UUID = UUID.fromString("93E74BB2-05A5-4AC0-8DF5-A55768208A95");
    private static final AttributeModifier CHARGE_SPEED_BOOST = new AttributeModifier(CHARGE_SPEED_UUID, "Charge speed boost", 9.0D, AttributeModifier.Operation.MULTIPLY_BASE);
    private static final AttributeModifier KNOCKBACK_RESISTANCE = new AttributeModifier(KNOCKBACK_RESISTANCE_UUID, "Knockback reduction", 1.0D, AttributeModifier.Operation.ADDITION);
    private static final EntityDataAccessor<Boolean> DASHING = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.BOOLEAN);

    @Unique
    private int cooldown;

    @Unique
    private int bucklerUseTimer;

    @Shadow
    protected ItemStack useItem;

    public LivingEntityMixin(EntityType<?> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
    }

    @Inject(at = @At(value = "RETURN"), cancellable = true, method = "isDamageSourceBlocked")
    public void isDamageSourceBlocked(DamageSource damageSourceIn, CallbackInfoReturnable<Boolean> info) {
        boolean flag = false;
        if (!damageSourceIn.isBypassArmor() && this.isBlocking() && !flag && this.useItem.getItem() instanceof BucklerItem)
            info.setReturnValue(false);

    }

    @Inject(at = @At(value = "TAIL"), method = "addAdditionalSaveData")
    public void writeAdditional(CompoundTag compound, CallbackInfo info) {
        compound.putBoolean("BucklerDashing", this.isBucklerDashing());
        compound.putInt("ChargeCooldown", this.getCooldown());
        compound.putInt("BucklerUseTimer", this.getBucklerUseTimer());
    }

    @Inject(at = @At(value = "TAIL"), method = "readAdditionalSaveData")
    public void readAdditional(CompoundTag compound, CallbackInfo info) {
        this.setBucklerDashing(compound.getBoolean("BucklerDashing"));
        this.setCooldown(compound.getInt("ChargeCooldown"));
        this.setBucklerUseTimer(compound.getInt("BucklerUseTimer"));
    }

    @Inject(at = @At(value = "TAIL"), method = "defineSynchedData")
    protected void defineSynchedData(CallbackInfo info) {
        this.entityData.define(DASHING, false);
    }

    public int getCooldown() {
        return this.cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    public void setBucklerDashing(boolean dashing) {
        if (!dashing) {
            AttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
            AttributeInstance knockback = this.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
            if (speed == null || knockback == null) {
                return;
            }
            knockback.removeModifier(KNOCKBACK_RESISTANCE);
            if ((LivingEntity) (Object) this instanceof Player)
                speed.removeModifier(CHARGE_SPEED_BOOST);
            this.setBucklerUseTimer(0);
        }
        if (dashing) {
            AttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
            AttributeInstance knockback = this.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
            if (speed == null || knockback == null) {
                return;
            }
            knockback.removeModifier(KNOCKBACK_RESISTANCE);
            knockback.addTransientModifier(KNOCKBACK_RESISTANCE);
            if ((LivingEntity) (Object) this instanceof Player) {
                speed.removeModifier(CHARGE_SPEED_BOOST);
                speed.addTransientModifier(CHARGE_SPEED_BOOST);
            }
        }
        this.entityData.set(DASHING, dashing);
    }

    public boolean isBucklerDashing() {
        return this.entityData.get(DASHING);
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
    public abstract AttributeInstance getAttribute(Attribute p_21052_);

    @Shadow
    protected abstract boolean isBlocking();

    @Shadow public abstract boolean isHolding(Predicate<Item> itemPredicate);
}
