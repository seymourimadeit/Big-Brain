package tallestegg.bigbrain.mixins;

import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import tallestegg.bigbrain.BigBrainConfig;
import tallestegg.bigbrain.BigBrainEnchantments;
import tallestegg.bigbrain.BigBrainItems;
import tallestegg.bigbrain.entity.IBucklerUser;
import tallestegg.bigbrain.items.BucklerItem;

//This is where the magic happens, and by magic, I mean mechanically automated goring into commodities!
@Mixin(PiglinBrute.class)
public abstract class PiglinBruteMixin extends AbstractPiglin implements IBucklerUser {

    protected PiglinBruteMixin(EntityType<? extends AbstractPiglin> type, Level worldIn) {
        super(type, worldIn);
    }

    @Inject(at = @At("TAIL"), method = "finalizeSpawn(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/MobSpawnType;Lnet/minecraft/world/entity/SpawnGroupData;Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/world/entity/SpawnGroupData;")
    public void finalizeSpawn(ServerLevelAccessor level, DifficultyInstance p_35059_, MobSpawnType p_35060_, @Nullable SpawnGroupData p_35061_, @Nullable CompoundTag p_35062_, CallbackInfoReturnable<SpawnGroupData> info) {
        RandomSource randomsource = level.getRandom();
        this.populateDefaultEquipmentEnchantments(randomsource, p_35059_);
    }

    @Inject(at = @At(value = "TAIL"), method = "populateDefaultEquipmentSlots")
    protected void setEquipmentBasedOnDifficulty(RandomSource rSource, DifficultyInstance difficulty, CallbackInfo info) {
        if (!BigBrainConfig.BruteSpawningWithBuckler)
            return;
        this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(BigBrainItems.BUCKLER.get()));
        this.setCooldown(240);
    }

    @Override
    protected void enchantSpawnedWeapon(RandomSource rSource, float p_241844_1_) {
        if (rSource.nextInt(300) == 0) {
            ItemStack itemstack = this.getOffhandItem();
            if (itemstack.getItem() instanceof BucklerItem) {
                Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(itemstack);
                map.putIfAbsent(BigBrainEnchantments.TURNING.get(), 1);
                EnchantmentHelper.setEnchantments(map, itemstack);
                this.setItemSlot(EquipmentSlot.OFFHAND, itemstack);
            }
        }
        if (rSource.nextInt(500) == 0) {
            ItemStack itemstack = this.getOffhandItem();
            if (itemstack.getItem() instanceof BucklerItem) {
                Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(itemstack);
                map.putIfAbsent(BigBrainEnchantments.BANG.get(), 1);
                EnchantmentHelper.setEnchantments(map, itemstack);
                this.setItemSlot(EquipmentSlot.OFFHAND, itemstack);
            }
        }
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHitIn) {
        super.dropCustomDeathLoot(source, looting, recentlyHitIn);
        ItemStack itemstack = this.getOffhandItem();
        if (itemstack.getItem() instanceof BucklerItem) {
            float f = 0.10F;
            boolean flag = f > 1.0F;
            if (!itemstack.isEmpty() && !EnchantmentHelper.hasVanishingCurse(itemstack) && (recentlyHitIn || flag) && Math.max(this.random.nextFloat() - (float) looting * 0.01F, 0.0F) < f) {
                if (!flag && itemstack.isDamageableItem()) {
                    itemstack.setDamageValue(this.random.nextInt(this.random.nextInt(itemstack.getMaxDamage() / 2)));
                }

                this.spawnAtLocation(itemstack);
                this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
            }
        }
    }
}
