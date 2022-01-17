package tallestegg.bigbrain;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import tallestegg.bigbrain.entity.IBucklerUser;
import tallestegg.bigbrain.items.BucklerItem;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = BigBrain.MODID)
public class BigBrainClientEvents {
    public static final Method preRenderCallback = ObfuscationReflectionHelper.findMethod(LivingEntityRenderer.class, "m_7546_", LivingEntity.class, PoseStack.class, float.class);
    
    @SubscribeEvent
    public static void onMovementKeyPressed(MovementInputUpdateEvent event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (((IBucklerUser) player).isBucklerDashing()) {
            event.getInput().jumping = false;
            event.getInput().leftImpulse = 0;
        }
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        PoseStack mStack = event.getPoseStack();
        ItemStack stack = event.getItemStack();
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        float partialTicks = event.getPartialTicks();
        if (stack.getItem() instanceof BucklerItem && (player.isUsingItem() && player.getUseItem() == stack || ((IBucklerUser) player).isBucklerDashing() && BucklerItem.isReady(stack))) {
            boolean mainHand = event.getHand() == InteractionHand.MAIN_HAND;
            HumanoidArm handside = mainHand ? player.getMainArm() : player.getMainArm().getOpposite();
            boolean rightHanded = handside == HumanoidArm.RIGHT;
            float f7 = (float) stack.getUseDuration() - ((float) player.getUseItemRemainingTicks() - partialTicks + 1.0F);
            float f11 = f7 / 10.0F;
            if (f11 > 1.0F) {
                f11 = 1.0F;
            }
            mStack.pushPose();
            int i = rightHanded ? 1 : -1;
            mStack.translate((double) ((float) i * 0.56F), (double) (-0.52F + event.getEquipProgress() * -0.6F), (double) -0.72F);
            mStack.translate(f11 * 0.2D, 0.0D, f11 * 0.2D);
            ItemTransforms.TransformType transform = !rightHanded ? ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND : ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND;
            Minecraft.getInstance().getItemInHandRenderer().renderItem(player, stack, transform, !rightHanded, mStack, event.getMultiBufferSource(), event.getPackedLight());
            mStack.popPose();
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEntityRenderPre(RenderLivingEvent.Post<LivingEntity, EntityModel<LivingEntity>> event) {
        LivingEntity entityIn = (LivingEntity) event.getEntity();
        LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>> renderer = event.getRenderer();
        EntityModel<LivingEntity> model = renderer.getModel();
        PoseStack stack = event.getPoseStack();
        if (!BigBrainConfig.RenderAfterImage)
            return;
        if (((IBucklerUser) entityIn).isBucklerDashing()) {
            for (int i = 0; i < 5; i++) {
                if (i != 0) {
                    stack.pushPose();
                    model.attackTime = entityIn.getAttackAnim(event.getPartialTick());
                    boolean shouldSit = entityIn.isPassenger() && (entityIn.getVehicle() != null && entityIn.getVehicle().shouldRiderSit());
                    model.riding = shouldSit;
                    model.young = entityIn.isBaby();
                    float f = Mth.rotLerp(event.getPartialTick(), entityIn.yBodyRotO, entityIn.yBodyRot);
                    float f1 = Mth.rotLerp(event.getPartialTick(), entityIn.yHeadRotO, entityIn.yHeadRot);
                    float f2 = f1 - f;
                    if (shouldSit && entityIn.getVehicle() instanceof LivingEntity) {
                        LivingEntity livingentity = (LivingEntity) entityIn.getVehicle();
                        f = Mth.rotLerp(event.getPartialTick(), livingentity.yBodyRotO, livingentity.yBodyRot);
                        f2 = f1 - f;
                        float f3 = Mth.wrapDegrees(f2);
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

                    float f6 = Mth.lerp(event.getPartialTick(), entityIn.xRotO, entityIn.getXRot());
                    if (entityIn.getPose() == Pose.SLEEPING) {
                        Direction direction = entityIn.getBedOrientation();
                        if (direction != null) {
                            float f4 = entityIn.getEyeHeight(Pose.STANDING) - 0.1F;
                            stack.translate((double) ((float) (-direction.getStepX()) * f4), 0.0D, (double) ((float) (-direction.getStepZ()) * f4));
                        }
                    }
                    float f7 = (float) entityIn.tickCount + event.getPartialTick();
                    stack.mulPose(Vector3f.YP.rotationDegrees(180.0F - f));
                    try {
                        preRenderCallback.invoke(renderer, entityIn, stack, event.getPartialTick());
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                        new RuntimeException("Big Brain has failed to invoke preRenderCallback via reflection.");
                    }
                    stack.scale(-1.0F, -1.0F, 1.0F);
                    double motionZ = Math.abs(entityIn.getDeltaMovement().z());
                    stack.translate(0.0D, (double) -1.501F, i * motionZ * 4 / ((IBucklerUser) entityIn).getBucklerUseTimer());
                    float f8 = 0.0F;
                    float f5 = 0.0F;
                    if (!shouldSit && entityIn.isAlive()) {
                        f8 = Mth.lerp(event.getPartialTick(), entityIn.animationSpeedOld, entityIn.animationSpeed);
                        f5 = entityIn.animationPosition - entityIn.animationSpeed * (1.0F - event.getPartialTick());
                        if (entityIn.isBaby()) {
                            f5 *= 3.0F;
                        }

                        if (f8 > 1.0F) {
                            f8 = 1.0F;
                        }
                    }

                    model.prepareMobModel(entityIn, f5, f8, event.getPartialTick());
                    model.setupAnim(entityIn, f5, f8, f7, f2, f6);
                    Minecraft minecraft = Minecraft.getInstance();
                    boolean flag = !entityIn.isInvisible();
                    boolean flag1 = !flag && !entityIn.isInvisibleTo(minecraft.player);
                    boolean flag2 = minecraft.shouldEntityAppearGlowing(entityIn);
                    RenderType rendertype = BigBrainClientEvents.getRenderType(entityIn, renderer, model, flag, flag1, flag2);
                    if (rendertype != null) {
                        VertexConsumer ivertexbuilder = event.getMultiBufferSource().getBuffer(rendertype);
                        int overlay = LivingEntityRenderer.getOverlayCoords(entityIn, 0.0F);
                        model.renderToBuffer(stack, ivertexbuilder, event.getPackedLight(), overlay, 1.0F, 1.0F, 1.0F, 0.3F / i + 1.0F);
                    }
                    if (!entityIn.isSpectator()) {
                        if (BigBrainConfig.RenderEntityLayersDuringAfterImage) {
                            for (RenderLayer <LivingEntity, EntityModel<LivingEntity>> layerrenderer : renderer.layers) {
                                layerrenderer.render(stack, event.getMultiBufferSource(), event.getPackedLight(), entityIn, f5, f8, event.getPartialTick(), f7, f2, f6);
                            }
                        }
                    }
                    stack.popPose();
                }
            }
        }
    }

    public static RenderType getRenderType(LivingEntity p_230496_1_, LivingEntityRenderer<LivingEntity, ?> renderer, EntityModel<?> model, boolean p_230496_2_, boolean p_230496_3_, boolean p_230496_4_) {
        ResourceLocation resourcelocation = renderer.getTextureLocation(p_230496_1_);
        if (p_230496_3_) {
            return RenderType.itemEntityTranslucentCull(resourcelocation);
        } else if (p_230496_2_) {
            return RenderType.entityTranslucent(resourcelocation);
        } else {
            return p_230496_4_ ? RenderType.outline(resourcelocation) : null;
        }
    }
}
