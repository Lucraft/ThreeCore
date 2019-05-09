package com.threetag.threecore.util.client.events;

import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Cancelable;

public class AnimationEvent extends LivingEvent {
	
	protected RenderLivingBase<EntityLivingBase> renderer;
	
	public AnimationEvent(EntityLivingBase entity, RenderLivingBase<EntityLivingBase> renderer) {
		super(entity);
		this.renderer = renderer;
	}
	
	public RenderLivingBase<EntityLivingBase> getRenderer() {
		return renderer;
	}
	
	public enum ModelSetRotationAnglesEventType {
		PRE, POST
	}
	
	@Cancelable
	public static class SetRotationAngles extends EntityEvent {
		
		public final ModelSetRotationAnglesEventType type;
		public ModelBiped model;
		public float limbSwing;
		public float limbSwingAmount;
		public float partialTicks;
		public float ageInTicks;
		public float netHeadYaw;
		public float headPitch;
		
		public SetRotationAngles(Entity entity, ModelBiped model, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, ModelSetRotationAnglesEventType type) {
			super(entity);
			this.model = model;
			this.limbSwing = limbSwing;
			this.limbSwingAmount = limbSwingAmount;
			this.partialTicks = partialTicks;
			this.ageInTicks = ageInTicks;
			this.netHeadYaw = netHeadYaw;
			this.headPitch = headPitch;
			this.type = type;
		}
		
	}
	
}