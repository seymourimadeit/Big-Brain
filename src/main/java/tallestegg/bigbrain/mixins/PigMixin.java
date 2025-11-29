package tallestegg.bigbrain.mixins;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import tallestegg.bigbrain.BigBrainConfig;

@Mixin(Pig.class)
public abstract class PigMixin extends Animal {
    protected PigMixin(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void spawnChildFromBreeding(ServerLevel level, Animal animal) {
        RandomSource randomSource = this.getRandom();
        for (int i = 0; i < BigBrainConfig.COMMON.minPigBabiesBred.get() + randomSource.nextInt(BigBrainConfig.COMMON.maxPigBabiesBred.get() + 1); ++i) {
            super.spawnChildFromBreeding(level, animal);
        }
    }
}
