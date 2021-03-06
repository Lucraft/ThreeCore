package net.threetag.threecore.ability;

import net.minecraft.util.LazyValue;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.threetag.threecore.client.renderer.entity.modellayer.*;
import net.threetag.threecore.util.threedata.ResourceLocationThreeData;
import net.threetag.threecore.util.threedata.ThreeData;

import java.util.Collections;
import java.util.List;

public class ModelLayerAbility extends Ability implements IModelLayerProvider {

    public static ThreeData<ResourceLocation> MODEL_LAYER = new ResourceLocationThreeData("model_layer").enableSetting("Determines the id for the model layer this ability will look for. The model layer json file must be in 'assets/<namespace>/model_layers'.");
    @OnlyIn(Dist.CLIENT)
    private LazyValue<IModelLayer> modelLayer = new LazyValue<>(() -> ModelLayerLoader.getModelLayer(this.dataManager.get(MODEL_LAYER)));

    public ModelLayerAbility() {
        super(AbilityType.MODEL_LAYER);
    }

    @Override
    public void registerData() {
        super.registerData();
        this.dataManager.register(MODEL_LAYER, new ResourceLocation("hello", "world"));
    }

    @Override
    public boolean isEffect() {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public List<IModelLayer> getModelLayers(IModelLayerContext context) {
        return Collections.singletonList(modelLayer.getValue());
    }
}
