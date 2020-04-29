package net.threetag.threecore.client.renderer.entity.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.HandSide;
import net.minecraft.util.JSONUtils;
import net.threetag.threecore.util.PlayerUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class BipedModelParser extends EntityModelParser {

    @Override
    public EntityModel apply(JsonObject jsonObject) {
        ParsedBipedModel model = new ParsedBipedModel(parseModelScales(jsonObject.get("scale")), BipedArmType.getFromName(JSONUtils.getString(jsonObject, "arm_type", "default")), JSONUtils.getInt(jsonObject, "texture_width", 64), JSONUtils.getInt(jsonObject, "texture_height", 64));

        if (JSONUtils.hasField(jsonObject, "cubes")) {
            JsonArray cubes = JSONUtils.getJsonArray(jsonObject, "cubes");

            for (int i = 0; i < cubes.size(); i++) {
                JsonObject cubeJson = cubes.get(i).getAsJsonObject();
                RendererModel parent = getPart(JSONUtils.getString(cubeJson, "parent", ""), model);

                if (parent != null)
                    parent.addChild(parseRendererModel(cubeJson, model));
                else
                    model.addCube(parseRendererModel(cubeJson, model));
            }
        }

        if (JSONUtils.hasField(jsonObject, "visibility_overrides")) {
            JsonObject overrides = JSONUtils.getJsonObject(jsonObject, "visibility_overrides");

            overrides.entrySet().forEach(entry -> {
                RendererModel part = getPart(entry.getKey(), model);

                if (part != null) {
                    model.addVisibilityOverride(part, JSONUtils.getBoolean(overrides, entry.getKey()));
                }
            });
        }

        return model;
    }

    public Function<String, Float> parseModelScales(JsonElement jsonElement) {
        if (jsonElement.isJsonPrimitive()) {
            return s -> jsonElement.getAsFloat();
        } else if (jsonElement.isJsonObject()) {
            Map<String, Float> scales = Maps.newHashMap();
            AtomicReference<Float> fallback = new AtomicReference<>(-999F);
            jsonElement.getAsJsonObject().entrySet().forEach(entry -> {
                if (entry.getKey().equalsIgnoreCase("fallback")) {
                    fallback.set(entry.getValue().getAsFloat());
                } else {
                    scales.put(entry.getKey(), entry.getValue().getAsFloat());
                }
            });
            return s -> {
                if (scales.containsKey(s)) {
                    return scales.get(s);
                }
                return fallback.get() <= -999F ? s.equalsIgnoreCase("head") ? 0.5F : 0.25F : fallback.get();
            };
        } else {
            throw new JsonParseException("Model scale must be either a single float or a json object with each model part!");
        }
    }

    public RendererModel getPart(String name, ParsedBipedModel model) {
        if (name.equalsIgnoreCase("head"))
            return model.bipedHead;
        else if (name.equalsIgnoreCase("head_overlay"))
            return model.bipedHeadwear;
        else if (name.equalsIgnoreCase("chest"))
            return model.bipedBody;
        else if (name.equalsIgnoreCase("chest_overlay"))
            return model.bipedBodyWear;
        else if (name.equalsIgnoreCase("right_arm"))
            return model.bipedRightArm;
        else if (name.equalsIgnoreCase("right_arm_overlay"))
            return model.bipedRightArmwear;
        else if (name.equalsIgnoreCase("left_arm"))
            return model.bipedLeftArm;
        else if (name.equalsIgnoreCase("left_arm_overlay"))
            return model.bipedLeftArmwear;
        else if (name.equalsIgnoreCase("right_leg"))
            return model.bipedRightLeg;
        else if (name.equalsIgnoreCase("right_leg_overlay"))
            return model.bipedRightLegwear;
        else if (name.equalsIgnoreCase("left_leg"))
            return model.bipedLeftLeg;
        else if (name.equalsIgnoreCase("left_leg_overlay"))
            return model.bipedLeftLegwear;
        return null;
    }

    public static class ParsedBipedModel<T extends LivingEntity> extends BipedModel<T> implements ISlotDependentVisibility, IArmRenderingModel {

        public List<RendererModel> cubes = Lists.newLinkedList();
        public Map<RendererModel, Boolean> visibilityOverrides = Maps.newHashMap();
        public final RendererModel bipedLeftArmwear;
        public final RendererModel bipedRightArmwear;
        public final RendererModel bipedLeftLegwear;
        public final RendererModel bipedRightLegwear;
        public final RendererModel bipedBodyWear;
        private final BipedArmType bipedArmType;

        public ParsedBipedModel(Function<String, Float> scales, BipedArmType bipedArmType, int textureWidth, int textureHeight) {
            super(0F, 0.0F, textureWidth, textureHeight);
            this.bipedArmType = bipedArmType;

            this.bipedHead = new RendererModel(this, 0, 0);
            this.bipedHead.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, scales.apply("head"));
            this.bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
            this.bipedHeadwear = new RendererModel(this, 32, 0);
            this.bipedHeadwear.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, scales.apply("head") + 0.5F);
            this.bipedHeadwear.setRotationPoint(0.0F, 0.0F, 0.0F);
            this.bipedBody = new RendererModel(this, 16, 16);
            this.bipedBody.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, scales.apply("chest"));
            this.bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
            this.bipedRightLeg = new RendererModel(this, 0, 16);
            this.bipedRightLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, scales.apply("right_leg"));
            this.bipedRightLeg.setRotationPoint(-1.9F, 12.0F, 0.0F);

            if (bipedArmType == BipedArmType.SMALL) {
                this.bipedLeftArm = new RendererModel(this, 32, 48);
                this.bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 3, 12, 4, scales.apply("left_arm"));
                this.bipedLeftArm.setRotationPoint(5.0F, 2.5F, 0.0F);

                this.bipedRightArm = new RendererModel(this, 40, 16);
                this.bipedRightArm.addBox(-2.0F, -2.0F, -2.0F, 3, 12, 4, scales.apply("right_arm"));
                this.bipedRightArm.setRotationPoint(-5.0F, 2.5F, 0.0F);

                this.bipedLeftArmwear = new RendererModel(this, 48, 48);
                this.bipedLeftArmwear.addBox(-1.0F, -2.0F, -2.0F, 3, 12, 4, scales.apply("left_arm") + 0.25F);
                this.bipedLeftArmwear.setRotationPoint(5.0F, 2.5F, 0.0F);

                this.bipedRightArmwear = new RendererModel(this, 40, 32);
                this.bipedRightArmwear.addBox(-2.0F, -2.0F, -2.0F, 3, 12, 4, scales.apply("right_arm") + 0.25F);
                this.bipedRightArmwear.setRotationPoint(-5.0F, 2.5F, 10.0F);
            } else {
                this.bipedLeftArm = new RendererModel(this, 32, 48);
                this.bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, scales.apply("left_arm"));
                this.bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);

                this.bipedRightArm = new RendererModel(this, 40, 16);
                this.bipedRightArm.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, scales.apply("right_arm"));
                this.bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);

                this.bipedLeftArmwear = new RendererModel(this, 48, 48);
                this.bipedLeftArmwear.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, scales.apply("left_arm") + 0.25F);
                this.bipedLeftArmwear.setRotationPoint(5.0F, 2.0F, 0.0F);

                this.bipedRightArmwear = new RendererModel(this, 40, 32);
                this.bipedRightArmwear.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, scales.apply("right_arm") + 0.25F);
                this.bipedRightArmwear.setRotationPoint(-5.0F, 2.0F, 10.0F);
            }

            this.bipedLeftLeg = new RendererModel(this, 16, 48);
            this.bipedLeftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, scales.apply("left_leg"));
            this.bipedLeftLeg.setRotationPoint(1.9F, 12.0F, 0.0F);
            this.bipedLeftLegwear = new RendererModel(this, 0, 48);
            this.bipedLeftLegwear.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, scales.apply("left_leg") + 0.25F);
            this.bipedLeftLegwear.setRotationPoint(1.9F, 12.0F, 0.0F);
            this.bipedRightLegwear = new RendererModel(this, 0, 32);
            this.bipedRightLegwear.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, scales.apply("right_leg") + 0.25F);
            this.bipedRightLegwear.setRotationPoint(-1.9F, 12.0F, 0.0F);
            this.bipedBodyWear = new RendererModel(this, 16, 32);
            this.bipedBodyWear.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, scales.apply("chest") + 0.25F);
            this.bipedBodyWear.setRotationPoint(0.0F, 0.0F, 0.0F);
        }

        public ParsedBipedModel addCube(RendererModel rendererModel) {
            this.cubes.add(rendererModel);
            return this;
        }

        public void addVisibilityOverride(RendererModel rendererModel, boolean visible) {
            this.visibilityOverrides.put(rendererModel, visible);
        }

        @Override
        public void setSlotVisibility(EquipmentSlotType slot) {
            this.setVisible(false);
            switch (slot) {
                case HEAD:
                    this.bipedHead.showModel = true;
                    this.bipedHeadwear.showModel = true;
                    break;
                case CHEST:
                    this.bipedBody.showModel = true;
                    this.bipedBodyWear.showModel = true;
                    this.bipedRightArm.showModel = true;
                    this.bipedRightArmwear.showModel = true;
                    this.bipedLeftArm.showModel = true;
                    this.bipedLeftArmwear.showModel = true;
                    break;
                case LEGS:
                    this.bipedBody.showModel = true;
                    this.bipedBodyWear.showModel = true;
                    this.bipedRightLeg.showModel = true;
                    this.bipedRightLegwear.showModel = true;
                    this.bipedLeftLeg.showModel = true;
                    this.bipedLeftLegwear.showModel = true;
                    break;
                case FEET:
                    this.bipedRightLeg.showModel = true;
                    this.bipedRightLegwear.showModel = true;
                    this.bipedLeftLeg.showModel = true;
                    this.bipedLeftLegwear.showModel = true;
            }

            for (Map.Entry<RendererModel, Boolean> override : this.visibilityOverrides.entrySet()) {
                override.getKey().showModel = override.getValue();
            }
        }

        @Override
        public void render(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
            if (this.bipedArmType == BipedArmType.FIXED) {
                boolean smallArms = entityIn instanceof PlayerEntity && PlayerUtil.hasSmallArms((PlayerEntity) entityIn);
                this.bipedLeftArm.rotationPointY =
                        this.bipedLeftArmwear.rotationPointY =
                                this.bipedRightArm.rotationPointY =
                                        this.bipedRightArmwear.rotationPointY = smallArms ? 2.5F : 2.0F;
            }

            super.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            GlStateManager.pushMatrix();
            if (this.isChild) {
                GlStateManager.scalef(0.5F, 0.5F, 0.5F);
                GlStateManager.translatef(0.0F, 24.0F * scale, 0.0F);
                this.bipedLeftLegwear.render(scale);
                this.bipedRightLegwear.render(scale);
                this.bipedLeftArmwear.render(scale);
                this.bipedRightArmwear.render(scale);
                this.bipedBodyWear.render(scale);
            } else {
                if (entityIn.shouldRenderSneaking()) {
                    GlStateManager.translatef(0.0F, 0.2F, 0.0F);
                }
                this.bipedLeftLegwear.render(scale);
                this.bipedRightLegwear.render(scale);
                this.bipedLeftArmwear.render(scale);
                this.bipedRightArmwear.render(scale);
                this.bipedBodyWear.render(scale);
            }

            for (RendererModel cube : this.cubes) {
                cube.render(scale);
            }

            GlStateManager.popMatrix();
        }

        @Override
        public void setRotationAngles(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {
            if (entityIn instanceof ArmorStandEntity) {
                ArmorStandEntity armorStandEntity = (ArmorStandEntity) entityIn;
                this.bipedRightArm.showModel = this.bipedRightArmwear.showModel = this.bipedLeftArm.showModel = this.bipedLeftArmwear.showModel = this.bipedRightArm.showModel && ((ArmorStandEntity) entityIn).getShowArms();
                this.bipedHead.rotateAngleX = ((float) Math.PI / 180F) * armorStandEntity.getHeadRotation().getX();
                this.bipedHead.rotateAngleY = ((float) Math.PI / 180F) * armorStandEntity.getHeadRotation().getY();
                this.bipedHead.rotateAngleZ = ((float) Math.PI / 180F) * armorStandEntity.getHeadRotation().getZ();
                this.bipedBody.rotateAngleX = ((float) Math.PI / 180F) * armorStandEntity.getBodyRotation().getX();
                this.bipedBody.rotateAngleY = ((float) Math.PI / 180F) * armorStandEntity.getBodyRotation().getY();
                this.bipedBody.rotateAngleZ = ((float) Math.PI / 180F) * armorStandEntity.getBodyRotation().getZ();
                this.bipedLeftArm.rotateAngleX = ((float) Math.PI / 180F) * armorStandEntity.getLeftArmRotation().getX();
                this.bipedLeftArm.rotateAngleY = ((float) Math.PI / 180F) * armorStandEntity.getLeftArmRotation().getY();
                this.bipedLeftArm.rotateAngleZ = ((float) Math.PI / 180F) * armorStandEntity.getLeftArmRotation().getZ();
                this.bipedRightArm.rotateAngleX = ((float) Math.PI / 180F) * armorStandEntity.getRightArmRotation().getX();
                this.bipedRightArm.rotateAngleY = ((float) Math.PI / 180F) * armorStandEntity.getRightArmRotation().getY();
                this.bipedRightArm.rotateAngleZ = ((float) Math.PI / 180F) * armorStandEntity.getRightArmRotation().getZ();
                this.bipedLeftLeg.rotateAngleX = ((float) Math.PI / 180F) * armorStandEntity.getLeftLegRotation().getX();
                this.bipedLeftLeg.rotateAngleY = ((float) Math.PI / 180F) * armorStandEntity.getLeftLegRotation().getY();
                this.bipedLeftLeg.rotateAngleZ = ((float) Math.PI / 180F) * armorStandEntity.getLeftLegRotation().getZ();
                this.bipedRightLeg.rotateAngleX = ((float) Math.PI / 180F) * armorStandEntity.getRightLegRotation().getX();
                this.bipedRightLeg.rotateAngleY = ((float) Math.PI / 180F) * armorStandEntity.getRightLegRotation().getY();
                this.bipedRightLeg.rotateAngleZ = ((float) Math.PI / 180F) * armorStandEntity.getRightLegRotation().getZ();
                this.bipedHeadwear.copyModelAngles(this.bipedHead);

                this.bipedRightArm.rotationPointZ = 0.0F;
                this.bipedRightArm.rotationPointX = -5.0F;
                this.bipedLeftArm.rotationPointZ = 0.0F;
                this.bipedLeftArm.rotationPointX = 5.0F;

                this.bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
                this.bipedBodyWear.setRotationPoint(0.0F, 0.0F, 0.0F);
                this.bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
                this.bipedHeadwear.setRotationPoint(0.0F, 0.0F, 0.0F);
                this.bipedLeftLeg.setRotationPoint(1.9F, 12.0F, 0.0F);
                this.bipedRightLeg.setRotationPoint(-1.9F, 12.0F, 0.0F);
                this.bipedLeftLegwear.setRotationPoint(1.9F, 12.0F, 0.0F);
                this.bipedRightLegwear.setRotationPoint(-1.9F, 12.0F, 0.0F);

                if (bipedArmType == BipedArmType.SMALL) {
                    this.bipedLeftArm.setRotationPoint(5.0F, 2.5F, 0.0F);
                    this.bipedRightArm.setRotationPoint(-5.0F, 2.5F, 0.0F);
                    this.bipedLeftArmwear.setRotationPoint(5.0F, 2.5F, 0.0F);
                    this.bipedRightArmwear.setRotationPoint(-5.0F, 2.5F, 10.0F);
                } else {
                    this.bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
                    this.bipedLeftArmwear.setRotationPoint(5.0F, 2.0F, 0.0F);
                    this.bipedRightArmwear.setRotationPoint(-5.0F, 2.0F, 10.0F);
                }

            } else {
                super.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
                this.bipedHead.rotateAngleZ = 0;
            }

            this.bipedLeftLegwear.copyModelAngles(this.bipedLeftLeg);
            this.bipedRightLegwear.copyModelAngles(this.bipedRightLeg);
            this.bipedLeftArmwear.copyModelAngles(this.bipedLeftArm);
            this.bipedRightArmwear.copyModelAngles(this.bipedRightArm);
            this.bipedBodyWear.copyModelAngles(this.bipedBody);
        }

        @Override
        public void setVisible(boolean visible) {
            super.setVisible(visible);
            this.bipedLeftArmwear.showModel = visible;
            this.bipedRightArmwear.showModel = visible;
            this.bipedLeftLegwear.showModel = visible;
            this.bipedRightLegwear.showModel = visible;
            this.bipedBodyWear.showModel = visible;
        }

        @Override
        public void renderArm(HandSide handSide) {
            if(handSide == HandSide.RIGHT) {
                this.bipedRightArm.rotateAngleX = 0.0F;
                this.bipedRightArm.render(0.0625F);
                this.bipedRightArmwear.rotateAngleX = 0.0F;
                this.bipedRightArmwear.render(0.0625F);
            } else {
                this.bipedLeftArm.rotateAngleX = 0.0F;
                this.bipedLeftArm.render(0.0625F);
                this.bipedLeftArmwear.rotateAngleX = 0.0F;
                this.bipedLeftArmwear.render(0.0625F);
            }
        }
    }

    public enum BipedArmType {

        NORMAL("normal"),
        SMALL("small"),
        FIXED("fixed");

        private final String name;

        BipedArmType(String name) {
            this.name = name;
        }

        public static BipedArmType getFromName(String name) {
            for (BipedArmType type : values()) {
                if (type.name.equalsIgnoreCase(name)) {
                    return type;
                }
            }

            return NORMAL;
        }

    }

}
