package tallestegg.bigbrain.capablities;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class LoafStorage implements IStorage<ILoaf> {

    @Override
    public INBT writeNBT(Capability<ILoaf> capability, ILoaf instance, Direction side) {
        CompoundNBT tag = new CompoundNBT();
        tag.putBoolean("Loaf", instance.isLoafing());
        return tag;
    }

    @Override
    public void readNBT(Capability<ILoaf> capability, ILoaf instance, Direction side, INBT nbt) {
        CompoundNBT tag = (CompoundNBT) nbt;
        instance.setLoafing(tag.getBoolean("Loaf"));
    }

}
