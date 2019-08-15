package com.threetag.threecore.events;

import com.threetag.threecore.events.effects.TriggerEffect;
import com.threetag.threecore.events.triggers.EventTriggerType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Created by Nictogen on 2019-07-26.
 */
public class ThreeCoreEvents
{
	public static final int EVENT_CHANCE = 100;

	public ThreeCoreEvents(){
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		MinecraftForge.EVENT_BUS.register(this);
	}


	public void setup(FMLCommonSetupEvent event) {

	}



	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event){
		EventManager.events.removeIf(EventType.Event::tick);
	}


	@SubscribeEvent
	public void onLivingUpdate(LivingEvent.LivingUpdateEvent event){

	}

	@SubscribeEvent
	public void onRegisterNewRegistries(RegistryEvent.NewRegistry e) {
		EventTriggerType.onRegisterNewRegistries(e);
		TriggerEffect.onRegisterNewRegistries(e);
		EventType.onRegisterNewRegistries(e);
	}

	//TODO remove this replace with random chance
	@SubscribeEvent
	public void onLivingJump(LivingEvent.LivingJumpEvent event){
		if(event.getEntity() instanceof ServerPlayerEntity){
			EventType.spawnEvent((PlayerEntity) event.getEntity(), event.getEntity().getPosition(), (EventType) EventType.REGISTRY.getValues().toArray()[0]);
		}

	}
}
