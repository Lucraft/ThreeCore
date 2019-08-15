package com.threetag.threecore.events.triggers;

import com.threetag.threecore.ThreeCore;
import com.threetag.threecore.events.EventType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryBuilder;

/**
 * Created by Nictogen on 2019-07-29.
 */
//TODO add option for removing event after triggering
public abstract class EventTriggerType implements IForgeRegistryEntry<EventTriggerType>
{
	public static IForgeRegistry<EventTriggerType> REGISTRY;

	public static void onRegisterNewRegistries(RegistryEvent.NewRegistry e) {
		REGISTRY = new RegistryBuilder<EventTriggerType>().setName(new ResourceLocation(ThreeCore.MODID, "eventTriggerTypes")).setType(
				EventTriggerType.class).setIDRange(0, 2048).create();
	}

	public abstract boolean isTriggered(EventType.Event event);

	@Override public Class<EventTriggerType> getRegistryType()
	{
		return EventTriggerType.class;
	}
}
