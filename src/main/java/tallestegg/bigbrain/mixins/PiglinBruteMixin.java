package tallestegg.bigbrain.mixins;

import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.monster.WitherSkeletonEntity;
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
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.Explosion;
import net.minecraft.world.ExplosionContext;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import tallestegg.bigbrain.BigBrainConfig;
import tallestegg.bigbrain.BigBrainEnchantments;
import tallestegg.bigbrain.BigBrainItems;
import tallestegg.bigbrain.BigBrainSounds;
import tallestegg.bigbrain.entity.IBucklerUser;
import tallestegg.bigbrain.entity.ai.PiglinBruteLookController;
import tallestegg.bigbrain.entity.ai.PiglinBruteMoveController;
import tallestegg.bigbrain.items.BucklerItem;

//This is where the magic happens, and by magic, I mean mechanically automated goring into commodities!
@Mixin(PiglinBruteEntity.class)
public class PiglinBruteMixin extends AbstractPiglinEntity implements IBucklerUser {
    private static final UUID KNOCKBACK_RESISTANCE_UUID = UUID.fromString("93E74BB2-05A5-4AC0-8DF5-A55768208A95");
    private static final AttributeModifier KNOCKBACK_RESISTANCE = new AttributeModifier(KNOCKBACK_RESISTANCE_UUID, "Knockback reduction", 1.0D, AttributeModifier.Operation.ADDITION);
    private static final DataParameter<Boolean> CHARGING = EntityDataManager.createKey(PiglinBruteEntity.class, DataSerializers.BOOLEAN);
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
        if (this.isCharging() && !(EnchantmentHelper.getEnchantmentLevel(BigBrainEnchantments.TURNING.get(), this.getHeldItemOffhand()) > 0)) {
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
                            ((ServerWorld) world).playSound((PlayerEntity) null, (double) this.getPosition().getX(), (double) this.getPosition().getY(), (double) this.getPosition().getZ(), BigBrainSounds.SHIELD_BASH.get(), this.getSoundCategory(), 0.1F,
                                    this.getSoundPitch() + this.rand.nextFloat() * 0.4F);
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
                stack.damageItem(10 * bangLevel, this, (player1) -> {
                    player1.sendBreakAnimation(hand);
                });
                this.world.createExplosion((Entity) null, DamageSource.causeExplosionDamage(this), (ExplosionContext) null, this.getPosX(), this.getPosY(), this.getPosZ(), (float) bangLevel * 1.0F, false, Explosion.Mode.NONE);
                this.setCharging(false);
            }
            this.setLastAttackedEntity(entityIn);
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
            if (this.bucklerUseTimer > BigBrainConfig.BucklerRunTime)
                this.bucklerUseTimer = BigBrainConfig.BucklerRunTime;
            ++this.cooldown;
            if (this.cooldown > BigBrainConfig.BucklerCooldown)
                this.cooldown = BigBrainConfig.BucklerCooldown;
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

    @Inject(at = @At("TAIL"), method = "onInitialSpawn(Lnet/minecraft/world/IServerWorld;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/entity/SpawnReason;Lnet/minecraft/entity/ILivingEntityData;Lnet/minecraft/nbt/CompoundNBT;)Lnet/minecraft/entity/ILivingEntityData;")
    public ILivingEntityData onInitialSpawn(IServerWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason, @Nullable ILivingEntityData spawnDataIn, @Nullable CompoundNBT dataTag, CallbackInfoReturnable<ILivingEntityData> info) {
        this.setEnchantmentBasedOnDifficulty(difficultyIn);
        return super.onInitialSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
    }

    @Inject(at = @At(value = "TAIL"), method = "setEquipmentBasedOnDifficulty")
    protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty, CallbackInfo info) {
        if (!BigBrainConfig.BruteSpawningWithBuckler)
            return;
        this.setItemStackToSlot(EquipmentSlotType.OFFHAND, new ItemStack(BigBrainItems.BUCKLER.get()));
        this.cooldown = 240;
    }

    @Override
    protected void func_241844_w(float p_241844_1_) {
        if (this.rand.nextInt(300) == 0) {
            ItemStack itemstack = this.getHeldItemOffhand();
            if (itemstack.getItem() instanceof BucklerItem) {
                Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(itemstack);
                map.putIfAbsent(BigBrainEnchantments.TURNING.get(), 1);
                EnchantmentHelper.setEnchantments(map, itemstack);
                this.setItemStackToSlot(EquipmentSlotType.OFFHAND, itemstack);
            }
        }
        if (this.rand.nextInt(500) == 0) {
            ItemStack itemstack = this.getHeldItemOffhand();
            if (itemstack.getItem() instanceof BucklerItem) {
                Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(itemstack);
                map.putIfAbsent(BigBrainEnchantments.BANG.get(), 1);
                EnchantmentHelper.setEnchantments(map, itemstack);
                this.setItemStackToSlot(EquipmentSlotType.OFFHAND, itemstack);
            }
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
                    itemstack.setDamage(this.rand.nextInt(this.rand.nextInt(itemstack.getMaxDamage() / 2)));
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
