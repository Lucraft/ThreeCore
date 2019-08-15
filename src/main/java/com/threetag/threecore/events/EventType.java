package com.threetag.threecore.events;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.threetag.threecore.ThreeCore;
import com.threetag.threecore.events.effects.TriggerEffect;
import com.threetag.threecore.events.triggers.EventTriggerType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryBuilder;

import javax.annotation.Nullable;
import java.util.ArrayList;

/**
 * Created by Nictogen on 2019-07-26.
 */
//TODO EventTypes with stored entities
public class EventType implements IForgeRegistryEntry<EventType>
{
	public ResourceLocation resourceLocation;
	public ITextComponent name;
	public EntitySpawner[] entitySpawners;
	public EventTrigger[] eventTriggers;

	public EventType(ResourceLocation resourceLocation, EntitySpawner[] entitySpawners, EventTrigger[] eventTriggers){
		this.resourceLocation = resourceLocation;
		this.entitySpawners = entitySpawners;
		this.eventTriggers = eventTriggers;
	}

	public static ForgeRegistry<EventType> REGISTRY;

	public static void onRegisterNewRegistries(RegistryEvent.NewRegistry e) {
		REGISTRY = (ForgeRegistry<EventType>) new RegistryBuilder<EventType>().setName(new ResourceLocation(ThreeCore.MODID, "eventTypes")).setType(
				EventType.class).allowModification().setIDRange(0, 2048).create();
	}

	//TODO time limit?
	public static Event spawnEvent(PlayerEntity player, BlockPos origin, EventType eventType) {
		ArrayList<Entity> entities = new ArrayList<>();
		for (EntitySpawner entitySpawner : eventType.entitySpawners)
			entities.add(entitySpawner.entityType.spawn(player.world, null, null, origin.add(entitySpawner.blockOffset), SpawnReason.EVENT, false, false));
		return new Event(player, origin, entities, eventType);
	}

	@Override public EventType setRegistryName(ResourceLocation name)
	{
		this.resourceLocation = name;
		return this;
	}

	@Nullable @Override public ResourceLocation getRegistryName()
	{
		return this.resourceLocation;
	}

	@Override public Class<EventType> getRegistryType()
	{
		return EventType.class;
	}

	public static class EntitySpawner {
		public EntityType entityType;
		//TODO tag entities with their identifiers
		public String identifier;
		public BlockPos blockOffset;
		//TODO optional attributes

		public EntitySpawner(EntityType entityType, String identifier, BlockPos blockOffset) {
			this.entityType = entityType;
			this.identifier = identifier;
			this.blockOffset = blockOffset;
		}

		public static EntitySpawner loadFromJson(JsonObject json) {
			return new EntitySpawner(ForgeRegistries.ENTITIES.getValue(new ResourceLocation(JSONUtils.getString(json, "type", EntityType.ZOMBIE.getRegistryName().toString()))), JSONUtils.getString(json, "id", ""), new BlockPos(JSONUtils.getInt(json, "x", 0), JSONUtils.getInt(json, "y", 0), JSONUtils.getInt(json, "z", 0)));
		}
	}

	//TODO toasts, names and icons
	public static class EventTrigger {
		public TriggerEffect effect;
		public String[] identifiers;
		public EventTriggerType type;
		public EventTrigger(EventTriggerType triggerType, TriggerEffect effect, String[] identifiers){
			this.effect = effect;
			this.type = triggerType;
			this.identifiers = identifiers;
		}
		public static EventTrigger loadFromJson(JsonObject json) {
			//TODO default triggers and effects
			ArrayList<String> idList = new ArrayList<>();
			for (JsonElement ids : JSONUtils.getJsonArray(json, "ids"))
				idList.add(ids.getAsString());
			return new EventTrigger(EventTriggerType.REGISTRY.getValue(new ResourceLocation(JSONUtils.getString(json, "type"))), TriggerEffect.REGISTRY.getValue(new ResourceLocation(JSONUtils.getString(json, "effect"))), idList.toArray(new String[0]));
		}
	}

	public static class Event {
		public PlayerEntity playerEntity;
		public ArrayList<Entity> spawnedEntities;
		public BlockPos origin;
		public EventType type;

		public Event(PlayerEntity playerEntity, BlockPos origin, ArrayList<Entity> spawnedEntities, EventType type) {
			this.playerEntity = playerEntity;
			this.origin = origin;
			this.spawnedEntities = spawnedEntities;
			this.type = type;
		}

		/**
		 * Checks all of the triggers to see if they are triggered
		 * @return true if event should be removed from global list (not ran anymore)
		 */
		public boolean tick(){
			for (EventTrigger eventTrigger : this.type.eventTriggers)
			{
				if(eventTrigger.type.isTriggered(this)){
					//TODO completed toast
					//TODO remove event if option enabled
					for (Entity spawnedEntity : spawnedEntities)
						spawnedEntity.onKillCommand();
					eventTrigger.effect.onTrigger(this);
					return true;
				}
			}
			return false;
		}

	}

}
