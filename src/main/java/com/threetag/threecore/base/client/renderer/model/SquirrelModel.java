package com.threetag.threecore.base.client.renderer.model;

import com.threetag.threecore.base.entity.SquirrelEntity;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.entity.model.WolfModel;
import net.minecraft.client.renderer.model.ModelBox;
import net.minecraft.entity.Pose;
import net.minecraft.util.math.MathHelper;

public class SquirrelModel<T extends SquirrelEntity> extends EntityModel<T> {

    private final RendererModel body;
    private final RendererModel tail;
    private final RendererModel chest;
    private final RendererModel leg_front_right;
    private final RendererModel head;
    private final RendererModel leg_front_left;
    private final RendererModel leg_back_left;
    private final RendererModel leg_back_right;

    public SquirrelModel() {
        textureWidth = 64;
        textureHeight = 64;

        body = new RendererModel(this);
        body.setRotationPoint(0.0F, 20.0F, 0.0F);

        tail = new RendererModel(this);
        tail.setRotationPoint(0.0F, -1.0F, 4.0F);
        body.addChild(tail);
        tail.cubeList.add(new ModelBox(tail, 11, 39, -1.5F, 0.0F, 0.0F, 3, 2, 2, 0.0F, false));
        tail.cubeList.add(new ModelBox(tail, 11, 39, -1.5F, -7.0F, 0.0F, 3, 2, 2, 0.0F, false));
        tail.cubeList.add(new ModelBox(tail, 11, 39, -1.5F, -5.0F, 0.5F, 3, 5, 2, 0.0F, false));

        chest = new RendererModel(this);
        chest.setRotationPoint(0.0F, 1.0F, 0.5F);
        body.addChild(chest);
        chest.cubeList.add(new ModelBox(chest, 20, 8, -2.0F, -2.0F, -3.5F, 4, 4, 7, 0.0F, false));

        leg_front_right = new RendererModel(this);
        leg_front_right.setRotationPoint(-1.5F, 3.0F, -2.5F);
        body.addChild(leg_front_right);
        leg_front_right.cubeList.add(new ModelBox(leg_front_right, 11, 35, -0.5F, 0.0F, -0.5F, 1, 1, 1, 0.0F, false));

        head = new RendererModel(this);
        head.setRotationPoint(0.0F, -1.0F, -3.0F);
        body.addChild(head);
        head.cubeList.add(new ModelBox(head, 25, 23, -2.0F, -2.0F, -4.0F, 4, 4, 4, 0.0F, false));
        head.cubeList.add(new ModelBox(head, 0, 0, -2.0F, -3.0F, -1.0F, 1, 1, 1, 0.0F, false));
        head.cubeList.add(new ModelBox(head, 0, 0, 1.0F, -3.0F, -1.0F, 1, 1, 1, 0.0F, false));

        leg_front_left = new RendererModel(this);
        leg_front_left.setRotationPoint(1.5F, 3.0F, -2.5F);
        body.addChild(leg_front_left);
        leg_front_left.cubeList.add(new ModelBox(leg_front_left, 11, 30, -0.5F, 0.0F, -0.5F, 1, 1, 1, 0.0F, false));

        leg_back_left = new RendererModel(this);
        leg_back_left.setRotationPoint(1.5F, 3.0F, 3.5F);
        body.addChild(leg_back_left);
        leg_back_left.cubeList.add(new ModelBox(leg_back_left, 11, 25, -0.5F, 0.0F, -0.5F, 1, 1, 1, 0.0F, false));

        leg_back_right = new RendererModel(this);
        leg_back_right.setRotationPoint(-1.5F, 3.0F, 3.5F);
        body.addChild(leg_back_right);
        leg_back_right.cubeList.add(new ModelBox(leg_back_right, 11, 21, -0.5F, 0.0F, -0.5F, 1, 1, 1, 0.0F, false));
    }

    @Override
    public void render(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        body.render(scale);
    }

    @Override
    public void setLivingAnimations(T entityIn, float limbSwing, float limbSwingAmount, float partialTick) {

    }

    @Override
    public void setRotationAngles(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
        boolean standing = limbSwingAmount <= 0.02F;

        float f0 = 5F;
        float f1 = 4F;
        this.leg_front_left.rotateAngleX = MathHelper.cos(limbSwing * f0) * f1 * limbSwingAmount;
        this.leg_back_left.rotateAngleX = MathHelper.cos(limbSwing * f0 + (float) Math.PI) * f1 * limbSwingAmount;
        this.leg_back_right.rotateAngleX = MathHelper.cos(limbSwing * f0 + (float) Math.PI) * f1 * limbSwingAmount;
        this.leg_front_right.rotateAngleX = MathHelper.cos(limbSwing * f0) * f1 * limbSwingAmount;
        this.tail.rotationPointX = -1;

        if ((entityIn.prevPosX == entityIn.posX && entityIn.prevPosZ == entityIn.posZ) || entityIn.isBesideClimbableBlock()) {
            this.body.rotateAngleX = (float) Math.toRadians(-90F);
            this.tail.rotateAngleX = (float) Math.toRadians(60F);
            this.tail.rotateAngleZ = 0;
            this.tail.rotationPointZ = 2;
            this.head.rotateAngleX = (float) (headPitch * ((float) Math.PI / 180F) + Math.toRadians(90F));
            this.head.rotateAngleY = 0;
            this.head.rotateAngleZ = netHeadYaw * ((float) Math.PI / 180F);
            this.head.rotationPointY = 0;
            this.head.rotationPointZ = -5;
            this.leg_front_right.rotationPointZ = -2;
            this.leg_front_left.rotationPointZ = -2;
        } else {
            this.body.rotateAngleX = 0F;
            this.head.rotateAngleX = headPitch * ((float) Math.PI / 180F);
            this.head.rotateAngleY = netHeadYaw * ((float) Math.PI / 180F);
            this.head.rotateAngleZ = 0;
            this.head.rotationPointY = -1;
            this.head.rotationPointZ = -3;
            this.tail.rotateAngleZ = 0;
            this.tail.rotateAngleY = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
            this.tail.rotationPointZ = 4;
            this.leg_front_right.rotationPointZ = -2.5F;
            this.leg_front_left.rotationPointZ = -2.5F;
        }
    }
}
