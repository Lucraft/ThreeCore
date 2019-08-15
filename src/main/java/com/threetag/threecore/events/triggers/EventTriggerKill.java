package com.threetag.threecore.events.triggers;

import com.threetag.threecore.ThreeCore;
import com.threetag.threecore.events.EventType;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

/**
 * Created by Nictogen on 2019-07-29.
 */
public class EventTriggerKill extends EventTriggerType
{
	public ResourceLocation registryName = new ResourceLocation(ThreeCore.MODID, "trigger_kill");

	@Override public boolean isTriggered(EventType.Event event)
	{
		//TODO check for identifier
		return event.spawnedEntities.stream().noneMatch(Entity::isAlive);
	}

	@Override public EventTriggerType setRegistryName(ResourceLocation name)
	{
		this.registryName = name;
		return this;
	}

	@Nullable @Override public ResourceLocation getRegistryName()
	{
		return this.registryName;
	}
}
