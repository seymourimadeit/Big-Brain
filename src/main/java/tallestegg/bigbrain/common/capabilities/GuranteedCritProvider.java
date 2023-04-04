package tallestegg.bigbrain.common.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tallestegg.bigbrain.BigBrain;

@Mod.EventBusSubscriber(modid = BigBrain.MODID)
public class GuranteedCritProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static final ResourceLocation IDENTIFIER = new ResourceLocation(BigBrain.MODID, "guranteed_crit");
    private final IOneCriticalAfterCharge.GuaranteedCriticalHit backend = new IOneCriticalAfterCharge.GuaranteedCriticalHit();
    private final LazyOptional<IOneCriticalAfterCharge> optionalData = LazyOptional.of(() -> backend);

    public GuranteedCritProvider() {
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return BigBrainCapabilities.GUARANTEED_CRIT_TRACKER.orEmpty(cap, this.optionalData);
    }

    public void invalidate() {
        this.optionalData.invalidate();
    }

    @Override
    public CompoundTag serializeNBT() {
        return this.backend.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.backend.deserializeNBT(nbt);
    }
}
