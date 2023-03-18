package tallestegg.bigbrain.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import tallestegg.bigbrain.client.BBModelLayers;
import tallestegg.bigbrain.common.items.BigBrainItems;
import tallestegg.bigbrain.client.BucklerTexture;
import tallestegg.bigbrain.client.model.ModelGoldenBuckler;

public class BucklerRenderer extends BlockEntityWithoutLevelRenderer {
    public final ModelGoldenBuckler bucklerModel;

    public BucklerRenderer(BlockEntityRenderDispatcher berd, EntityModelSet set) {
        super(berd, set);
        this.bucklerModel = new ModelGoldenBuckler(set.bakeLayer(BBModelLayers.BUCKLER));
    }

    @Override
    public void renderByItem(ItemStack stack, ItemTransforms.TransformType p_239207_2_, PoseStack matrixStack,
            MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        Item item = stack.getItem();
        if (item == BigBrainItems.BUCKLER.get()) {
            matrixStack.pushPose();
            matrixStack.scale(1.0F, -1.0F, -1.0F);
            Material rendermaterial = BucklerTexture.BUCKLER_TEXTURE;
            VertexConsumer ivertexbuilder = rendermaterial.sprite().wrap(ItemRenderer.getFoilBufferDirect(buffer,
                    this.bucklerModel.renderType(rendermaterial.atlasLocation()), true, stack.hasFoil()));
            this.bucklerModel.root.render(matrixStack, ivertexbuilder, combinedLight, combinedOverlay, 1.0F, 1.0F, 1.0F,
                    1.0F);
            matrixStack.popPose();
        }
    }
}
