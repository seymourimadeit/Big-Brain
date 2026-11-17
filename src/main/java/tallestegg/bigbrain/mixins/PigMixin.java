package tallestegg.bigbrain.mixins;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import tallestegg.bigbrain.BigBrainConfig;

@Mixin(Pig.class)
public abstract class PigMixin extends Animal {
    protected PigMixin(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void spawnChildFromBreeding(ServerLevel level, Animal mate) {
        RandomSource randomSource = this.getRandom();
        super.spawnChildFromBreeding(level, mate);
        for (int i = 0; i < randomSource.nextIntBetweenInclusive(BigBrainConfig.COMMON.minPigBabiesBred.get(), BigBrainConfig.COMMON.maxPigBabiesBred.get()); i++) {
            super.spawnChildFromBreeding(level, mate);
        }
    }
}
