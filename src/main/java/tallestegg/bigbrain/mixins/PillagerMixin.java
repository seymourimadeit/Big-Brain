package tallestegg.bigbrain.mixins;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

@Mixin(Pillager.class)
public abstract class PillagerMixin extends AbstractIllager {
    protected PillagerMixin(EntityType<? extends AbstractIllager> p_32105_, Level p_32106_) {
        super(p_32105_, p_32106_);
    }

    @Inject(at = @At("TAIL"), method = "finalizeSpawn(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/MobSpawnType;Lnet/minecraft/world/entity/SpawnGroupData;Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/world/entity/SpawnGroupData;")
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData groupData, @Nullable CompoundTag tag, CallbackInfoReturnable<SpawnGroupData> info) {
       if (spawnType == MobSpawnType.PATROL) {
            float chance = 0.50F;
            if (this.isPatrolLeader() && this.level.getRandom().nextFloat() < chance)
                this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SPYGLASS));
            }
        return super.finalizeSpawn(level, difficulty, spawnType, groupData, tag);
    }
}
