package com.threetag.threecore.events.effects;

import com.threetag.threecore.ThreeCore;
import com.threetag.threecore.events.EventType;
import com.threetag.threecore.karma.capability.CapabilityKarma;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

/**
 * Created by Nictogen on 2019-07-31.
 */
public class TriggerEffectKarmaBonus extends TriggerEffect
{
	public ResourceLocation resourceLocation = new ResourceLocation(ThreeCore.MODID, "triggerEffect_karma");

	@Override public TriggerEffect setRegistryName(ResourceLocation name)
	{
		this.resourceLocation = name;
		return this;
	}

	@Nullable @Override public ResourceLocation getRegistryName()
	{
		return this.resourceLocation;
	}

	@Override public void onTrigger(EventType.Event event)
	{
		CapabilityKarma.addKarma(event.playerEntity, 100);
	}
}
