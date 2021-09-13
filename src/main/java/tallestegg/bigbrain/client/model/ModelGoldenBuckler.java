package tallestegg.bigbrain.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;

/**
 * ModelGoldenBuckler - MCVinnyq Created using Tabula 8.0.0
 */
public class ModelGoldenBuckler extends Model {
    public final ModelPart root;
    public final ModelPart base;
    public final ModelPart handle;

    public ModelGoldenBuckler(ModelPart part) {
        super(RenderType::entitySolid);
        this.root = part;
        this.base = part.getChild("base");
        this.handle = part.getChild("handle");
    }
    
    public static LayerDefinition createLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -5.0F, -2.0F, 10.0F, 10.0F, 1.0F), PartPose.ZERO);
        partdefinition.addOrReplaceChild("handle", CubeListBuilder.create().texOffs(0, 11).addBox(-1.0F, -3.0F, -1.0F, 2.0F, 6.0F, 6.0F), PartPose.ZERO);
        return LayerDefinition.create(meshdefinition, 32, 32);
     }

    @Override
    public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        this.root.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
    }

    /**
     * This is a helper function from Tabula to set the rotation of model parts
     */
    public void setRotateAngle(ModelPart modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}
