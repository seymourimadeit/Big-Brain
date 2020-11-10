package tallestegg.bigbrain.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.monster.AbstractIllagerEntity;
import net.minecraft.entity.monster.PillagerEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;

@Mixin(PillagerEntity.class)
public abstract class PillagerEntityMixin extends AbstractIllagerEntity {
    @Shadow
    private static final DataParameter<Boolean> DATA_CHARGING_STATE = EntityDataManager.createKey(PillagerEntity.class, DataSerializers.BOOLEAN);
    
    protected PillagerEntityMixin(EntityType<? extends AbstractIllagerEntity> type, World worldIn) {
        super(type, worldIn);
    }

    @Overwrite
    public void setCharging(boolean isCharging) {
        if (!CrossbowItem.isCharged(getActiveItemStack())) {
            this.dataManager.set(DATA_CHARGING_STATE, isCharging);
        }
    }
}
