package tallestegg.bigbrain.client;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import tallestegg.bigbrain.BigBrain;
import tallestegg.bigbrain.BigBrainConfig;
import tallestegg.bigbrain.entity.IBucklerUser;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = BigBrain.MODID)
public class BigBrainClientEvents {
    @SubscribeEvent
    public static void onMovementKeyPressed(InputUpdateEvent event) {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if (((IBucklerUser) player).isBucklerDashing()) {
            event.getMovementInput().jump = false;
            event.getMovementInput().moveStrafe = 0;
        }
    }

    public static final Method preRenderCallback = ObfuscationReflectionHelper.findMethod(LivingRenderer.class, "func_225620_a_", LivingEntity.class, MatrixStack.class, float.class);

    @SubscribeEvent
    public static void onEntityRenderPre(RenderLivingEvent.Pre<LivingEntity, EntityModel<LivingEntity>> event) {
        LivingEntity entityIn = (LivingEntity) event.getEntity();
        if (!BigBrainConfig.RenderAfterImage)
            return;
        if (((IBucklerUser) entityIn).isBucklerDashing()) {
            for (int i = 0; i < 5; i++) {
                if (i != 0) {
                    event.getMatrixStack().push();
                    event.getRenderer().getEntityModel().swingProgress = entityIn.getSwingProgress(event.getPartialRenderTick());
                    boolean shouldSit = entityIn.isPassenger() && (entityIn.getRidingEntity() != null && entityIn.getRidingEntity().shouldRiderSit());
                    event.getRenderer().getEntityModel().isSitting = shouldSit;
                    event.getRenderer().getEntityModel().isChild = entityIn.isChild();
                    float f = MathHelper.interpolateAngle(event.getPartialRenderTick(), entityIn.prevRenderYawOffset, entityIn.renderYawOffset);
                    float f1 = MathHelper.interpolateAngle(event.getPartialRenderTick(), entityIn.prevRotationYawHead, entityIn.rotationYawHead);
                    float f2 = f1 - f;
                    if (shouldSit && entityIn.getRidingEntity() instanceof LivingEntity) {
                        LivingEntity livingentity = (LivingEntity) entityIn.getRidingEntity();
                        f = MathHelper.interpolateAngle(event.getPartialRenderTick(), livingentity.prevRenderYawOffset, livingentity.renderYawOffset);
                        f2 = f1 - f;
                        float f3 = MathHelper.wrapDegrees(f2);
                        if (f3 < -85.0F) {
                            f3 = -85.0F;
                        }

                        if (f3 >= 85.0F) {
                            f3 = 85.0F;
                        }

                        f = f1 - f3;
                        if (f3 * f3 > 2500.0F) {
                            f += f3 * 0.2F;
                        }

                        f2 = f1 - f;
                    }

                    float f6 = MathHelper.lerp(event.getPartialRenderTick(), entityIn.prevRotationPitch, entityIn.rotationPitch);
                    if (entityIn.getPose() == Pose.SLEEPING) {
                        Direction direction = entityIn.getBedDirection();
                        if (direction != null) {
                            float f4 = entityIn.getEyeHeight(Pose.STANDING) - 0.1F;
                            event.getMatrixStack().translate((double) ((float) (-direction.getXOffset()) * f4), 0.0D, (double) ((float) (-direction.getZOffset()) * f4));
                        }
                    }
                    float f7 = (float) entityIn.ticksExisted + event.getPartialRenderTick();
                    event.getMatrixStack().rotate(Vector3f.YP.rotationDegrees(180.0F - f));
                    event.getMatrixStack().scale(-1.0F, -1.0F, 1.0F);
                    event.getMatrixStack().translate(0.0D, -1.50D, i - 0.3F * entityIn.getMotion().getZ());
                    try {
                        preRenderCallback.invoke(event.getRenderer(), entityIn, event.getMatrixStack(), event.getPartialRenderTick());
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    float f8 = 0.0F;
                    float f5 = 0.0F;
                    if (!shouldSit && entityIn.isAlive()) {
                        f8 = MathHelper.lerp(event.getPartialRenderTick(), entityIn.prevLimbSwingAmount, entityIn.limbSwingAmount);
                        f5 = entityIn.limbSwing - entityIn.limbSwingAmount * (1.0F - event.getPartialRenderTick());
                        if (entityIn.isChild()) {
                            f5 *= 3.0F;
                        }

                        if (f8 > 1.0F) {
                            f8 = 1.0F;
                        }
                    }

                    event.getRenderer().getEntityModel().setLivingAnimations(entityIn, f5, f8, event.getPartialRenderTick());
                    event.getRenderer().getEntityModel().setRotationAngles(entityIn, f5, f8, f7, f2, f6);
                    Minecraft minecraft = Minecraft.getInstance();
                    boolean flag = !entityIn.isInvisible();
                    boolean flag1 = !flag && !entityIn.isInvisibleToPlayer(minecraft.player);
                    boolean flag2 = minecraft.isEntityGlowing(entityIn);
                    RenderType rendertype = event.getRenderer().getEntityModel().getRenderType(event.getRenderer().getEntityTexture(entityIn));
                    if (rendertype != null) {
                        IVertexBuilder ivertexbuilder = event.getBuffers().getBuffer(rendertype);
                        int overlay = LivingRenderer.getPackedOverlay(entityIn, 0.0F);
                        event.getRenderer().getEntityModel().render(event.getMatrixStack(), ivertexbuilder, event.getLight(), overlay, 1.0F, 1.0F, 1.0F, 0.3F / i + 1.0F);
                    }
                    if (!entityIn.isSpectator()) {
                        if (BigBrainConfig.RenderEntityLayersDuringAfterImage) {
                            for (LayerRenderer<LivingEntity, EntityModel<LivingEntity>> layerrenderer : event.getRenderer().layerRenderers) {
                                layerrenderer.render(event.getMatrixStack(), event.getBuffers(), event.getLight(), entityIn, f5, f8, event.getPartialRenderTick(), f7, f2, f6);
                            }
                        }
                    }
                    event.getMatrixStack().pop();
                }
            }
        }
    }
}
