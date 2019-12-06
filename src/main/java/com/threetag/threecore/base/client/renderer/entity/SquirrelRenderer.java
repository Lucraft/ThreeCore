package com.threetag.threecore.base.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.threetag.threecore.ThreeCore;
import com.threetag.threecore.base.client.renderer.model.SquirrelModel;
import com.threetag.threecore.base.entity.SquirrelEntity;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class SquirrelRenderer extends MobRenderer<SquirrelEntity, SquirrelModel<SquirrelEntity>> {

    public static final ResourceLocation TEXTURE = new ResourceLocation(ThreeCore.MODID, "textures/entity/squirrel.png");

    public SquirrelRenderer(EntityRendererManager rendererManager) {
        super(rendererManager, new SquirrelModel<>(), 0.3F);
    }

    @Override
    protected void preRenderCallback(SquirrelEntity entity, float partialTickTime) {

    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(SquirrelEntity entity) {
        return TEXTURE;
    }
}
