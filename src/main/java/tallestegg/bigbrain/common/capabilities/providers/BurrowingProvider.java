package tallestegg.bigbrain.common.capabilities.providers;

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
import tallestegg.bigbrain.common.capabilities.BigBrainCapabilities;
import tallestegg.bigbrain.common.capabilities.implementations.BurrowCapability;

@Mod.EventBusSubscriber(modid = BigBrain.MODID)
public class BurrowingProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static final ResourceLocation IDENTIFIER = new ResourceLocation(BigBrain.MODID, "burrowing");
    private final BurrowCapability.BurrowingImplementation backend = new BurrowCapability.BurrowingImplementation();
    private final LazyOptional<BurrowCapability> optionalData = LazyOptional.of(() -> backend);

    public BurrowingProvider() {
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return BigBrainCapabilities.BURROW_TRACKER.orEmpty(cap, this.optionalData);
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
