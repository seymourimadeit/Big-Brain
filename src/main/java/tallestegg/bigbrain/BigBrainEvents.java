package tallestegg.bigbrain;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.monster.VindicatorEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tallestegg.bigbrain.entity.ai.goals.HuntGoal;

@Mod.EventBusSubscriber(modid = BigBrain.MODID)
public class BigBrainEvents {
    @SubscribeEvent
    public static void onBreed(BabyEntitySpawnEvent event) {
        if (event.getParentA() instanceof PigEntity && event.getParentB() instanceof PigEntity) {
            PigEntity pig = (PigEntity) event.getParentA();
            for (int i = 0; i < 2 + pig.world.rand.nextInt(3); ++i) {
                AgeableEntity baby = EntityType.PIG.create(event.getChild().world);
                baby.copyLocationAndAnglesFrom(pig);
                baby.setChild(true);
                pig.world.addEntity(baby);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof VindicatorEntity) {
            VindicatorEntity vindi = (VindicatorEntity)event.getEntity();
        }
    }
}
