package tallestegg.bigbrain.capablities;

import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class LoafProvider implements ICapabilitySerializable<INBT> {
    @CapabilityInject(ILoaf.class)
    public static final Capability<ILoaf> LOAF = null;

    private final LazyOptional<ILoaf> instance = LazyOptional.of(LOAF::getDefaultInstance);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return cap == LOAF ? instance.cast() : LazyOptional.empty();
    }

    @Override
    public INBT serializeNBT() {
        return LOAF.getStorage().writeNBT(LOAF, this.instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!")), null);
    }

    @Override
    public void deserializeNBT(INBT nbt) {
        LOAF.getStorage().readNBT(LOAF, this.instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!")), null, nbt);
    }
}
