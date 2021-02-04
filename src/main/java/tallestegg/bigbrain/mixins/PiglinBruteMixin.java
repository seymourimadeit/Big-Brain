package tallestegg.bigbrain.mixins;

import java.util.Map;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.monster.piglin.AbstractPiglinEntity;
import net.minecraft.entity.monster.piglin.PiglinBruteEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import tallestegg.bigbrain.BigBrainConfig;
import tallestegg.bigbrain.BigBrainEnchantments;
import tallestegg.bigbrain.BigBrainItems;
import tallestegg.bigbrain.entity.IBucklerUser;
import tallestegg.bigbrain.items.BucklerItem;

//This is where the magic happens, and by magic, I mean mechanically automated goring into commodities!
@Mixin(PiglinBruteEntity.class)
public abstract class PiglinBruteMixin extends AbstractPiglinEntity implements IBucklerUser {

    protected PiglinBruteMixin(EntityType<? extends AbstractPiglinEntity> type, World worldIn) {
        super(type, worldIn);
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
        this.setCooldown(240);
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
}
