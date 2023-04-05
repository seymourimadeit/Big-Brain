package tallestegg.bigbrain.common.capabilities.implementations;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public interface BurrowCapability extends INBTSerializable<CompoundTag> {
    boolean isBurrowing();

    void setBurrowing(boolean burrowing);

    class BurrowingImplementation implements BurrowCapability {
        private boolean isBurrowing;

        @Override
        public CompoundTag serializeNBT() {
            final CompoundTag tag = new CompoundTag();
            tag.putBoolean("Burrowing", isBurrowing());
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            this.setBurrowing(nbt.getBoolean("Burrowing"));
        }

        @Override
        public boolean isBurrowing() {
            return isBurrowing;
        }

        @Override
        public void setBurrowing(boolean burrowing) {
            this.isBurrowing = burrowing;
        }
    }
}
