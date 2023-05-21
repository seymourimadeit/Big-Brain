package tallestegg.bigbrain.common.capabilities.implementations;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public interface BurrowCapability extends INBTSerializable<CompoundTag> {
    boolean isBurrowing();

    void setBurrowing(boolean burrowing);

    boolean isCarrying();

    void setCarrying(boolean carrying);

    class BurrowingImplementation implements BurrowCapability {
        private boolean isBurrowing;
        private boolean carrying;

        @Override
        public CompoundTag serializeNBT() {
            final CompoundTag tag = new CompoundTag();
            tag.putBoolean("Burrowing", isBurrowing());
            tag.putBoolean("Carrying", isCarrying());
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            this.setBurrowing(nbt.getBoolean("Burrowing"));
            this.setCarrying(nbt.getBoolean("Carrying"));
        }

        @Override
        public boolean isBurrowing() {
            return isBurrowing;
        }

        @Override
        public void setBurrowing(boolean burrowing) {
            this.isBurrowing = burrowing;
        }

        @Override
        public boolean isCarrying() {
            return carrying;
        }

        @Override
        public void setCarrying(boolean carrying) {
            this.carrying = carrying;
        }
    }
}
