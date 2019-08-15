package com.threetag.threecore.events.effects;

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
public abstract class TriggerEffect implements IForgeRegistryEntry<TriggerEffect>
{
	public static IForgeRegistry<TriggerEffect> REGISTRY;

	public abstract void onTrigger(EventType.Event event);

	public static void onRegisterNewRegistries(RegistryEvent.NewRegistry e) {
		REGISTRY = new RegistryBuilder<TriggerEffect>().setName(new ResourceLocation(ThreeCore.MODID, "triggerEffects")).setType(TriggerEffect.class).setIDRange(0, 2048).create();

	}

	@Override public Class<TriggerEffect> getRegistryType()
	{
		return TriggerEffect.class;
	}
}
