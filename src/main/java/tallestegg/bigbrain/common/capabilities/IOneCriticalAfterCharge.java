package tallestegg.bigbrain.common.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public interface IOneCriticalAfterCharge extends INBTSerializable<CompoundTag> {
    boolean isCritical();

    void setCritical(boolean critical);

    class GuaranteedCriticalHit implements IOneCriticalAfterCharge {
        private boolean isCritical;

        @Override
        public CompoundTag serializeNBT() {
            final CompoundTag tag = new CompoundTag();
            tag.putBoolean("Critical", isCritical());
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            this.setCritical(nbt.getBoolean("Critical"));
        }

        @Override
        public boolean isCritical() {
            return isCritical;
        }

        @Override
        public void setCritical(boolean critical) {
            this.isCritical = critical;
        }
    }
}
