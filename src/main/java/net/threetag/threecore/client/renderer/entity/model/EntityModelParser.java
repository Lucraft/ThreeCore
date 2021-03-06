package net.threetag.threecore.client.renderer.entity.model;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.threetag.threecore.util.TCJsonUtil;

import java.util.List;
import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public class EntityModelParser implements Function<JsonObject, EntityModel> {

    @Override
    public EntityModel apply(JsonObject jsonObject) {
        ParsedModel model = new ParsedModel();
        model.textureWidth = JSONUtils.getInt(jsonObject, "texture_width", 64);
        model.textureHeight = JSONUtils.getInt(jsonObject, "texture_height", 32);

        if (JSONUtils.hasField(jsonObject, "cubes")) {
            JsonArray cubes = JSONUtils.getJsonArray(jsonObject, "cubes");

            for (int i = 0; i < cubes.size(); i++) {
                JsonObject cubeJson = cubes.get(i).getAsJsonObject();
                model.addCube(parseRendererModel(cubeJson, model));
            }
        }

        return model;
    }

    public static NamedModelRenderer parseRendererModel(JsonObject json, Model model) {
        int[] textureOffsets = TCJsonUtil.getIntArray(json, 2, "texture_offset", 0, 0);
        NamedModelRenderer rendererModel = new NamedModelRenderer(JSONUtils.getString(json, "name", ""), model, textureOffsets[0], textureOffsets[1]);
        float[] offsets = TCJsonUtil.getFloatArray(json, 3, "offset", 0, 0, 0);
        float[] rotationPoint = TCJsonUtil.getFloatArray(json, 3, "rotation_point", 0, 0, 0);
        float[] rotation = TCJsonUtil.getFloatArray(json, 3, "rotation", 0, 0, 0);
        int[] size = TCJsonUtil.getIntArray(json, 3, "size", 1, 1, 1);
        rendererModel.addBox(offsets[0], offsets[1], offsets[2], size[0], size[1], size[2], JSONUtils.getFloat(json, "scale", 0F));
        rendererModel.setRotationPoint(rotationPoint[0], rotationPoint[1], rotationPoint[2]);
        rendererModel.rotateAngleX = (float) Math.toRadians(rotation[0]);
        rendererModel.rotateAngleY = (float) Math.toRadians(rotation[1]);
        rendererModel.rotateAngleZ = (float) Math.toRadians(rotation[2]);
        rendererModel.mirror = JSONUtils.getBoolean(json, "mirror", false);
        if (JSONUtils.hasField(json, "children")) {
            JsonArray children = JSONUtils.getJsonArray(json, "children");
            for (int i = 0; i < children.size(); i++) {
                rendererModel.addChild(parseRendererModel(children.get(i).getAsJsonObject(), model));
            }
        }

        return rendererModel;
    }

    public static class ParsedModel extends EntityModel {

        public List<NamedModelRenderer> cubes = Lists.newLinkedList();

        public ParsedModel(List<NamedModelRenderer> cubes) {
            this.cubes = cubes;
        }

        public ParsedModel() {

        }

        public ParsedModel addCube(NamedModelRenderer rendererModel) {
            this.cubes.add(rendererModel);
            return this;
        }

        @Override
        public void setRotationAngles(Entity entity, float v, float v1, float v2, float v3, float v4) {

        }

        public NamedModelRenderer getNamedPart(String name) {
            for (NamedModelRenderer modelRenderer : this.cubes) {
                if (modelRenderer.getName().equals(name)) {
                    return modelRenderer;
                }
            }
            return null;
        }

        @Override
        public void render(MatrixStack matrixStack, IVertexBuilder vertexBuilder, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
            RenderSystem.enableBlend();
            for (ModelRenderer cube : this.cubes) {
                cube.render(matrixStack, vertexBuilder, packedLightIn, packedOverlayIn, red, green, blue, alpha);
            }
            RenderSystem.disableBlend();
        }
    }

}
