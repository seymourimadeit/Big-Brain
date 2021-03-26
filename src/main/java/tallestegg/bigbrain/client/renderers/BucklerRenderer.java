package tallestegg.bigbrain.client.renderers;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import tallestegg.bigbrain.BigBrainItems;
import tallestegg.bigbrain.BucklerTexture;
import tallestegg.bigbrain.client.model.ModelGoldenBuckler;

public class BucklerRenderer extends ItemStackTileEntityRenderer {
    public final ModelGoldenBuckler bucklerModel = new ModelGoldenBuckler();

    @Override
    public void func_239207_a_(ItemStack stack, ItemCameraTransforms.TransformType p_239207_2_, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
        Item item = stack.getItem();
        if (item == BigBrainItems.BUCKLER.get()) {
            matrixStack.push();
            matrixStack.scale(1.0F, -1.0F, -1.0F);
            RenderMaterial rendermaterial = BucklerTexture.BUCKLER_TEXTURE;
            IVertexBuilder ivertexbuilder = rendermaterial.getSprite().wrapBuffer(ItemRenderer.getEntityGlintVertexBuilder(buffer, this.bucklerModel.getRenderType(rendermaterial.getAtlasLocation()), true, stack.hasEffect()));
            this.bucklerModel.base.render(matrixStack, ivertexbuilder, combinedLight, combinedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
            matrixStack.pop();
        }
    }
}
