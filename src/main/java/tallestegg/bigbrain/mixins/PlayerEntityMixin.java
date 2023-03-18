package tallestegg.bigbrain.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import tallestegg.bigbrain.common.entity.IOneCriticalAfterCharge;

@Mixin(Player.class)
public abstract class PlayerEntityMixin extends LivingEntity implements IOneCriticalAfterCharge {
    private static final EntityDataAccessor<Boolean> CRITICAL = SynchedEntityData.defineId(Player.class, EntityDataSerializers.BOOLEAN);
    
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> type, Level worldIn) {
        super(type, worldIn);
    }

    @Inject(at = @At(value = "TAIL"), method = "defineSynchedData")
    protected void defineSynchedData(CallbackInfo info) {
        this.entityData.define(CRITICAL, false);
    }

    @Override
    public void swing(InteractionHand handIn, boolean updateSelf) {
        super.swing(handIn, updateSelf);
        if (this.isCritical())
            this.setCritical(false);
    }

    @Inject(at = @At(value = "TAIL"), method = "addAdditionalSaveData")
    public void writeAdditionalSaveData(CompoundTag compound, CallbackInfo info) {
        compound.putBoolean("Critical", this.isCritical());
    }

    @Inject(at = @At(value = "TAIL"), method = "readAdditionalSaveData")
    public void readAdditionalSaveData(CompoundTag compound, CallbackInfo info) {
        this.setCritical(compound.getBoolean("Critical"));
    }

    public boolean isCritical() {
        return this.entityData.get(CRITICAL);
    }

    public void setCritical(boolean critical) {
        this.entityData.set(CRITICAL, critical);
    }
}
