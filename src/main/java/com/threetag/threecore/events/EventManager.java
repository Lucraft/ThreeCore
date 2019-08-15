package com.threetag.threecore.events;

import com.google.gson.*;
import com.threetag.threecore.ThreeCore;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.function.Predicate;

/**
 * Created by Nictogen on 2019-07-26.
 */
public class EventManager implements ISelectiveResourceReloadListener
{

	private static Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private static EventManager INSTANCE;
	public static final int resourcePrefix = "events/".length();
	public static final int resourceSuffix = ".json".length();

	public static ArrayList<EventType.Event> events = new ArrayList<>();

	public EventManager() {
		INSTANCE = this;
	}

	@Override
	public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
		EventType.REGISTRY.clear();
		for (ResourceLocation resourcelocation : resourceManager.getAllResourceLocations("events", (name) -> name.endsWith(".json"))) {
			String s = resourcelocation.getPath();
			ResourceLocation resourcelocation1 = new ResourceLocation(resourcelocation.getNamespace(), s.substring(resourcePrefix, s.length() - resourceSuffix));
			try (IResource iresource = resourceManager.getResource(resourcelocation)) {
				EventType eventType = parseEvent(resourcelocation, JSONUtils.fromJson(GSON, new BufferedReader(new InputStreamReader(iresource.getInputStream(), StandardCharsets.UTF_8)), JsonObject.class));
				if (eventType != null)
					EventType.REGISTRY.register(eventType);
			} catch (Throwable throwable) {
				ThreeCore.LOGGER.error("Couldn't read event {} from {}", resourcelocation1, resourcelocation, throwable);
			}
		}
	}

	public EventType parseEvent(ResourceLocation resourceLocation, JsonObject json)
	{
		JsonArray entitySpawnersJSON = JSONUtils.getJsonArray(json, "entities");
		ArrayList<EventType.EntitySpawner> entitySpawners = new ArrayList<>();
		for (JsonElement entitySpawner : entitySpawnersJSON)
			entitySpawners.add(EventType.EntitySpawner.loadFromJson(entitySpawner.getAsJsonObject()));


		JsonArray eventTriggersJSON = JSONUtils.getJsonArray(json, "triggers");
		ArrayList<EventType.EventTrigger> eventTriggers = new ArrayList<>();
		for (JsonElement eventTrigger : eventTriggersJSON)
			eventTriggers.add(EventType.EventTrigger.loadFromJson(eventTrigger.getAsJsonObject()));

		return new EventType(resourceLocation, entitySpawners.toArray(new EventType.EntitySpawner[0]), eventTriggers.toArray(new EventType.EventTrigger[0]));
	}

	public static EventManager getInstance() {
		return INSTANCE;
	}

}
