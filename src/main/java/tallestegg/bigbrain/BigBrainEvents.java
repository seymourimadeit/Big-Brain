package tallestegg.bigbrain;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.entity.monster.PillagerEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import tallestegg.bigbrain.entity.ai.goals.PressureEntityWithMultishotCrossbowGoal;
import tallestegg.bigbrain.entity.ai.goals.RunWhileChargingGoal;

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
        if (event.getEntity() instanceof PillagerEntity) {
            PillagerEntity pillager = (PillagerEntity) event.getEntity();
            pillager.targetSelector.addGoal(3, new PressureEntityWithMultishotCrossbowGoal<>(pillager, 1.0D, 2.0F));
            pillager.goalSelector.goals.stream().map(it -> it.inner).filter(it -> it instanceof RangedCrossbowAttackGoal<?>).findFirst().ifPresent(crossbowGoal -> {
                pillager.goalSelector.removeGoal(crossbowGoal);
                pillager.targetSelector.addGoal(3, new RangedCrossbowAttackGoal<>(pillager, 1.0D, 8.0F));
                pillager.goalSelector.addGoal(1, new RunWhileChargingGoal(pillager, 1.6D));
            });
        }
    }
}
