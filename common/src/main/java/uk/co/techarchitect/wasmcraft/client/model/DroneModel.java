package uk.co.techarchitect.wasmcraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.HumanoidArm;
import uk.co.techarchitect.wasmcraft.entity.DroneEntity;

public class DroneModel extends EntityModel<DroneEntity> implements ArmedModel {
    private final ModelPart body;
    private final ModelPart rotorFrontLeft;
    private final ModelPart rotorFrontRight;
    private final ModelPart rotorBackLeft;
    private final ModelPart rotorBackRight;
    private final ModelPart itemHolder;

    public DroneModel(ModelPart root) {
        this.body = root.getChild("body");
        this.rotorFrontLeft = root.getChild("rotor_front_left");
        this.rotorFrontRight = root.getChild("rotor_front_right");
        this.rotorBackLeft = root.getChild("rotor_back_left");
        this.rotorBackRight = root.getChild("rotor_back_right");
        this.itemHolder = root.getChild("item_holder");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 16)
                        .addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.offset(0.0F, 20.0F, 0.0F));

        partdefinition.addOrReplaceChild("rotor_front_left",
                CubeListBuilder.create()
                        .texOffs(0, 32)
                        .addBox(-2.0F, -0.5F, -2.0F, 4.0F, 1.0F, 4.0F),
                PartPose.offset(-5.0F, 17.0F, -5.0F));

        partdefinition.addOrReplaceChild("rotor_front_right",
                CubeListBuilder.create()
                        .texOffs(8, 32)
                        .addBox(-2.0F, -0.5F, -2.0F, 4.0F, 1.0F, 4.0F),
                PartPose.offset(5.0F, 17.0F, -5.0F));

        partdefinition.addOrReplaceChild("rotor_back_left",
                CubeListBuilder.create()
                        .texOffs(16, 32)
                        .addBox(-2.0F, -0.5F, -2.0F, 4.0F, 1.0F, 4.0F),
                PartPose.offset(-5.0F, 17.0F, 5.0F));

        partdefinition.addOrReplaceChild("rotor_back_right",
                CubeListBuilder.create()
                        .texOffs(24, 32)
                        .addBox(-2.0F, -0.5F, -2.0F, 4.0F, 1.0F, 4.0F),
                PartPose.offset(5.0F, 17.0F, 5.0F));

        partdefinition.addOrReplaceChild("item_holder",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 18.0F, -5.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(DroneEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float rotorRotation = ageInTicks * 0.5F;
        this.rotorFrontLeft.yRot = rotorRotation;
        this.rotorFrontRight.yRot = -rotorRotation;
        this.rotorBackLeft.yRot = -rotorRotation;
        this.rotorBackRight.yRot = rotorRotation;

        float bob = (float) Math.sin(ageInTicks * 0.1F) * 0.05F;
        this.body.y = 20.0F + bob;

        float attackAnim = entity.getAttackAnim(limbSwing);
        if (attackAnim > 0) {
            float swingAngle = (float) Math.sin(attackAnim * Math.PI);
            this.body.xRot = swingAngle * 0.4F;
            this.itemHolder.xRot = swingAngle * -0.5F;
        } else {
            this.body.xRot = 0.0F;
            this.itemHolder.xRot = 0.0F;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        body.render(poseStack, buffer, packedLight, packedOverlay, color);
        rotorFrontLeft.render(poseStack, buffer, packedLight, packedOverlay, color);
        rotorFrontRight.render(poseStack, buffer, packedLight, packedOverlay, color);
        rotorBackLeft.render(poseStack, buffer, packedLight, packedOverlay, color);
        rotorBackRight.render(poseStack, buffer, packedLight, packedOverlay, color);
    }

    @Override
    public void translateToHand(HumanoidArm arm, PoseStack poseStack) {
        this.itemHolder.translateAndRotate(poseStack);
    }
}
