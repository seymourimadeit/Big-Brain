package tallestegg.bigbrain.mixins;

import java.util.UUID;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.monster.piglin.AbstractPiglinEntity;
import net.minecraft.entity.monster.piglin.PiglinAction;
import net.minecraft.entity.monster.piglin.PiglinBruteEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import tallestegg.bigbrain.BigBrainItems;
import tallestegg.bigbrain.entity.IBucklerUser;
import tallestegg.bigbrain.entity.ai.PiglinBruteLookController;
import tallestegg.bigbrain.entity.ai.PiglinBruteMoveController;
import tallestegg.bigbrain.items.BucklerItem;

//This is where the magic happens, and by magic, I mean mechanically automated goring into commodities!
@Mixin(PiglinBruteEntity.class)
public class PiglinBruteMixin extends AbstractPiglinEntity implements IBucklerUser {
    private static final UUID KNOCKBACK_RESISTANCE_UUID = UUID.fromString("93E74BB2-05A5-4AC0-8DF5-A55768208A95");
    private static final AttributeModifier KNOCKBACK_RESISTANCE = new AttributeModifier(KNOCKBACK_RESISTANCE_UUID, "Knockback Reduction", 0.10D, AttributeModifier.Operation.ADDITION);
    private static final DataParameter<Boolean> CHARGING = EntityDataManager.createKey(PlayerEntity.class, DataSerializers.BOOLEAN);
    @Unique
    private int cooldown;

    @Unique
    private int bucklerUseTimer;

    protected PiglinBruteMixin(EntityType<? extends AbstractPiglinEntity> type, World worldIn) {
        super(type, worldIn);
    }

    @Inject(method = "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/World;)V", at = @At("RETURN"))
    public void onConstructor(EntityType<? extends PiglinBruteEntity> p_i241917_1_, World p_i241917_2_, CallbackInfo info) {
        this.lookController = new PiglinBruteLookController(this);
        this.moveController = new PiglinBruteMoveController(this);
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
            this.setLastAttackedEntity(entityIn);
            this.world.setEntityState(this, (byte) 43);
        }
        super.collideWithEntity(entityIn);
    }

    // We can't use a forge event for this due to the fact we have to do to this
    // stuff on the
    // livingTick() method.
    @Override
    public void livingTick() {
        if (!this.isCharging()) {
            ++this.bucklerUseTimer;
            if (this.bucklerUseTimer > 15)
                this.bucklerUseTimer = 15;
            ++this.cooldown;
            if (this.cooldown > 240)
                this.cooldown = 240;
        }

        if (this.isCharging()) {
            BucklerItem.moveFowards(this);
            this.cooldown--;
            this.bucklerUseTimer--;
        }
        if (bucklerUseTimer <= 0) {
            this.setCharging(false);
            this.cooldown = 0;
            this.bucklerUseTimer = 0;
            this.resetActiveHand();
        }
        if (cooldown <= 0) {
            this.cooldown = 0;
        }
        super.livingTick();
    }

    @Inject(at = @At("HEAD"), method = "onInitialSpawn(Lnet/minecraft/world/IServerWorld;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/entity/SpawnReason;Lnet/minecraft/entity/ILivingEntityData;Lnet/minecraft/nbt/CompoundNBT;)Lnet/minecraft/entity/ILivingEntityData;")
    public ILivingEntityData onInitialSpawn(IServerWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason, @Nullable ILivingEntityData spawnDataIn, @Nullable CompoundNBT dataTag, CallbackInfoReturnable<ILivingEntityData> info) {
        this.setItemStackToSlot(EquipmentSlotType.OFFHAND, new ItemStack(BigBrainItems.BUCKLER.get()));
        this.cooldown = 240;
        return super.onInitialSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
    }

    @Override
    public void handleStatusUpdate(byte id) {
        if (id == 43) {
            for (int i = 0; i < 5; ++i) {
                double d0 = this.rand.nextGaussian() * 0.02D;
                double d1 = this.rand.nextGaussian() * 0.02D;
                double d2 = this.rand.nextGaussian() * 0.02D;
                this.world.addParticle(ParticleTypes.CLOUD, this.getPosXRandom(1.0D), this.getPosYRandom() + 1.0D, this.getPosZRandom(1.0D), d0, d1, d2);
            }
        } else {
            super.handleStatusUpdate(id);
        }
    }

    @Override
    protected void dropSpecialItems(DamageSource source, int looting, boolean recentlyHitIn) {
        super.dropSpecialItems(source, looting, recentlyHitIn);
        ItemStack itemstack = this.getHeldItemOffhand();
        if (itemstack.getItem() instanceof BucklerItem) {
            float f = 0.10F;
            boolean flag = f > 1.0F;
            if (!itemstack.isEmpty() && !EnchantmentHelper.hasVanishingCurse(itemstack) && (recentlyHitIn || flag) && Math.max(this.rand.nextFloat() - (float) looting * 0.01F, 0.0F) < f) {
                if (!flag && itemstack.isDamageable()) {
                    itemstack.setDamage(itemstack.getMaxDamage() - this.rand.nextInt(1 + this.rand.nextInt(Math.max(itemstack.getMaxDamage() - 3, 1))));
                }

                this.entityDropItem(itemstack);
                this.setItemStackToSlot(EquipmentSlotType.OFFHAND, ItemStack.EMPTY);
            }
        }
    }

    @Override
    protected void registerData() {
        super.registerData();
        this.dataManager.register(CHARGING, false);
    }

    @Override
    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        compound.putBoolean("Charging", this.isCharging());
        compound.putInt("BucklerUseTimer", this.bucklerUseTimer);
        compound.putInt("Cooldown", this.cooldown);
    }

    @Override
    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        this.bucklerUseTimer = compound.getInt("BucklerUseTimer");
        this.cooldown = compound.getInt("Cooldown");
        this.setCharging(compound.getBoolean("Charging"));
    }

    public int getCooldown() {
        return this.cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    public void setCharging(boolean charging) {
        if (!charging) {
            ModifiableAttributeInstance knockback = this.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
            if (knockback == null) {
                return;
            }
            knockback.removeModifier(KNOCKBACK_RESISTANCE);
        }
        if (charging) {
            ModifiableAttributeInstance knockback = this.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
            if (knockback == null) {
                return;
            }
            knockback.removeModifier(KNOCKBACK_RESISTANCE);
            knockback.applyNonPersistentModifier(KNOCKBACK_RESISTANCE);
        }
        this.dataManager.set(CHARGING, charging);
    }

    public boolean isCharging() {
        return this.dataManager.get(CHARGING);
    }

    @Shadow
    protected boolean func_234422_eK_() {
        return false;
    }

    @Shadow
    public PiglinAction func_234424_eM_() {
        return null;
    }

    @Shadow
    protected void func_241848_eP() {
    }

    @Override
    public int getBucklerUseTimer() {
        return this.bucklerUseTimer;
    }

    @Override
    public void setBucklerUseTimer(int bucklerUseTimer) {
        this.bucklerUseTimer = bucklerUseTimer;
    }
}
