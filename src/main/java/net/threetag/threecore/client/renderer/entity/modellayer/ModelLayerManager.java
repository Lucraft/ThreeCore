package net.threetag.threecore.client.renderer.entity.modellayer;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.NonNullFunction;
import net.minecraftforge.fml.ModList;
import net.threetag.threecore.ThreeCore;
import net.threetag.threecore.ability.Ability;
import net.threetag.threecore.ability.AbilityHelper;
import net.threetag.threecore.client.renderer.entity.modellayer.predicates.*;
import net.threetag.threecore.client.renderer.entity.modellayer.texture.DefaultModelTexture;
import net.threetag.threecore.client.renderer.entity.modellayer.texture.ModelLayerTexture;
import net.threetag.threecore.client.renderer.entity.modellayer.texture.transformer.AlphaMaskTextureTransformer;
import net.threetag.threecore.client.renderer.entity.modellayer.texture.transformer.ITextureTransformer;
import net.threetag.threecore.client.renderer.entity.modellayer.texture.transformer.OverlayTextureTransformer;
import net.threetag.threecore.client.renderer.entity.modellayer.texture.variable.*;
import net.threetag.threecore.compat.curios.DefaultCuriosHandler;
import net.threetag.threecore.util.PlayerUtil;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ModelLayerManager {

    public static List<Function<LivingEntity, List<Pair<IModelLayer, IModelLayerContext>>>> MODEL_LAYER_PROVIDERS = Lists.newArrayList();

    private static final Map<ResourceLocation, NonNullFunction<JsonObject, IModelLayerPredicate>> PREDICATES = Maps.newHashMap();
    private static final Map<ResourceLocation, NonNullFunction<JsonObject, IModelLayer>> MODEL_LAYERS = Maps.newHashMap();
    private static final Map<ResourceLocation, NonNullFunction<JsonObject, ModelLayerTexture>> MODEL_LAYER_TEXTURES = Maps.newHashMap();
    private static final Map<ResourceLocation, NonNullFunction<JsonObject, ITextureVariable>> TEXTURE_VARIABLES = Maps.newHashMap();
    private static final Map<ResourceLocation, NonNullFunction<JsonObject, ITextureTransformer>> TEXTURE_TRANSFORMERS = Maps.newHashMap();

    static {
        // Model Layer Providers

        // Armor
        registerLayerProvider(entity -> {
            List<Pair<IModelLayer, IModelLayerContext>> list = Lists.newArrayList();
            for (EquipmentSlotType slot : EquipmentSlotType.values()) {
                if (slot.getSlotType() == EquipmentSlotType.Group.ARMOR) {
                    ItemStack stack = entity.getItemStackFromSlot(slot);

                    if (stack.getItem() instanceof IModelLayerProvider) {
                        ModelLayerContext context = new ModelLayerContext(entity, stack, slot);
                        for (IModelLayer layer : ((IModelLayerProvider) stack.getItem()).getModelLayers(context)) {
                            list.add(Pair.of(layer, context));
                        }
                    }
                }
            }
            return list;
        });

        // Abilities
        registerLayerProvider(entity -> {
            ModelLayerContext context = new ModelLayerContext(entity);
            List<Pair<IModelLayer, IModelLayerContext>> list = Lists.newArrayList();
            for (Ability ability : AbilityHelper.getAbilities(entity)) {
                if (ability instanceof IModelLayerProvider) {
                    for (IModelLayer layer : ((IModelLayerProvider) ability).getModelLayers(context)) {
                        list.add(Pair.of(layer, context));
                    }
                }
            }
            return list;
        });

        // Curios
        if (ModList.get().isLoaded("curios")) {
            registerLayerProvider(entity -> {
                List<Pair<IModelLayer, IModelLayerContext>> list = Lists.newArrayList();
                for (String id : DefaultCuriosHandler.INSTANCE.getSlotTypeIds()) {
                    for (ItemStack stack : DefaultCuriosHandler.INSTANCE.getItemsInSlot(entity, id)) {
                        if (stack.getItem() instanceof IModelLayerProvider) {
                            ModelLayerContext context = new ModelLayerContext(entity, stack);
                            for (IModelLayer layer : ((IModelLayerProvider) stack.getItem()).getModelLayers(context)) {
                                list.add(Pair.of(layer, context));
                            }
                        }
                    }
                }
                return list;
            });
        }

        // ----------------------------------------------------------------------------------------------------------------------------------------------
        // Layer Types

        // Default Layer
        registerModelLayer(new ResourceLocation(ThreeCore.MODID, "default"), ModelLayer::parse);

        // Compound Layer
        registerModelLayer(new ResourceLocation(ThreeCore.MODID, "compound"), CompoundModelLayer::parse);

        // Cape
        registerModelLayer(new ResourceLocation(ThreeCore.MODID, "cape"), CapeModelLayer::parse);

        // ----------------------------------------------------------------------------------------------------------------------------------------------
        // Texture Types

        // Default
        registerModelTexture(new ResourceLocation(ThreeCore.MODID, "default"), j -> {
            DefaultModelTexture texture = new DefaultModelTexture(JSONUtils.getString(j, "base"), JSONUtils.getString(j, "output", ""));
            if (JSONUtils.hasField(j, "variables")) {
                JsonObject variables = JSONUtils.getJsonObject(j, "variables");
                variables.entrySet().forEach(e -> {
                    ITextureVariable textureVariable = parseTextureVariable(e.getValue().getAsJsonObject());
                    if (textureVariable != null) {
                        texture.addVariable(e.getKey(), textureVariable);
                    } else {
                        ThreeCore.LOGGER.warn("Texture variable type '" + JSONUtils.getString(e.getValue().getAsJsonObject(), "type") + "' does not exist!");
                    }
                });
            }
            return texture;
        });

        // ----------------------------------------------------------------------------------------------------------------------------------------------
        // Transformers

        // Alpha Mask
        registerTextureTransformer(new ResourceLocation(ThreeCore.MODID, "alpha_mask"), j -> new AlphaMaskTextureTransformer(JSONUtils.getString(j, "mask")));

        // Overlay
        registerTextureTransformer(new ResourceLocation(ThreeCore.MODID, "overlay"), j -> new OverlayTextureTransformer(JSONUtils.getString(j, "overlay")));

        // ----------------------------------------------------------------------------------------------------------------------------------------------
        // Variables

        // Entity Ticks
        registerTextureVariable(new ResourceLocation(ThreeCore.MODID, "entity_ticks"), EntityTicksTextureVariable::new);

        // Integer NBT
        registerTextureVariable(new ResourceLocation(ThreeCore.MODID, "integer_nbt"), j -> new IntegerNbtTextureVariable(JSONUtils.getString(j, "nbt_tag"), j));

        // Small Arms
        registerTextureVariable(new ResourceLocation(ThreeCore.MODID, "small_arms"), j -> new SmallArmsTextureVariable(JSONUtils.getString(j, "normal_arms_value", null), JSONUtils.getString(j, "small_arms_value", null)));

        //Ability Data
        registerTextureVariable(new ResourceLocation(ThreeCore.MODID, "ability_data"), AbilityDataTextureVariable::new);
        // ----------------------------------------------------------------------------------------------------------------------------------------------
        // Predicates

        // Not
        registerPredicate(new ResourceLocation(ThreeCore.MODID, "not"), j -> new NotPredicate(parsePredicate(JSONUtils.getJsonObject(j, "predicate"))));

        // Or
        registerPredicate(new ResourceLocation(ThreeCore.MODID, "or"), OrPredicate::parse);

        // Sneaking
        registerPredicate(new ResourceLocation(ThreeCore.MODID, "sneaking"), j -> context -> context.getAsEntity().isCrouching());

        // Damage
        registerPredicate(new ResourceLocation(ThreeCore.MODID, "durability"), j -> new ItemDurabilityPredicate(JSONUtils.getFloat(j, "min", 0F), JSONUtils.getFloat(j, "max", 1F)));

        // Small Arms
        registerPredicate(new ResourceLocation(ThreeCore.MODID, "small_arms"), j -> c -> c.getAsEntity() instanceof PlayerEntity && PlayerUtil.hasSmallArms((PlayerEntity) c.getAsEntity()));

        // Is Size Changing
        registerPredicate(new ResourceLocation(ThreeCore.MODID, "is_size_changing"), j -> new IsSizeChangingPredicate());

        // Size
        registerPredicate(new ResourceLocation(ThreeCore.MODID, "size"), j -> new SizePredicate(JSONUtils.getFloat(j, "min", 1F), JSONUtils.getFloat(j, "max", 1F)));

        // Karma
        registerPredicate(new ResourceLocation(ThreeCore.MODID, "karma"), j -> new KarmaPredicate(JSONUtils.getInt(j, "min", 0), JSONUtils.getInt(j, "max", 0)));

        // Integer NBT
        registerPredicate(new ResourceLocation(ThreeCore.MODID, "integer_nbt"), IntegerNbtPredicate::parse);

        // Flying
        registerPredicate(new ResourceLocation(ThreeCore.MODID, "flying"), j -> new FlyingPredicate());

        // Entity Type
        registerPredicate(new ResourceLocation(ThreeCore.MODID, "entity_type"), j -> new EntityTypePredicate(new ResourceLocation(JSONUtils.getString(j, "entity_type"))));

        // Entity Tag
        registerPredicate(new ResourceLocation(ThreeCore.MODID, "entity_tag"), j -> new EntityTagPredicate(new ResourceLocation(JSONUtils.getString(j, "entity_tag"))));
    }

    public static List<Function<LivingEntity, List<Pair<IModelLayer, IModelLayerContext>>>> getModelLayerProviders() {
        return MODEL_LAYER_PROVIDERS;
    }

    public static void forEachLayer(LivingEntity entity, BiConsumer<IModelLayer, IModelLayerContext> consumer) {
        List<Function<LivingEntity, List<Pair<IModelLayer, IModelLayerContext>>>> providers = ModelLayerManager.getModelLayerProviders();

        for (Function<LivingEntity, List<Pair<IModelLayer, IModelLayerContext>>> function : providers) {
            List<Pair<IModelLayer, IModelLayerContext>> layers = function.apply(entity);
            for (Pair<IModelLayer, IModelLayerContext> pair : layers) {
                consumer.accept(pair.getFirst(), pair.getSecond());
            }
        }
    }

    public static void registerLayerProvider(Function<LivingEntity, List<Pair<IModelLayer, IModelLayerContext>>> function) {
        Preconditions.checkNotNull(function);
        MODEL_LAYER_PROVIDERS.add(function);
    }

    public static void registerPredicate(ResourceLocation id, NonNullFunction<JsonObject, IModelLayerPredicate> function) {
        Preconditions.checkNotNull(id);
        Preconditions.checkNotNull(function);
        PREDICATES.put(id, function);
    }

    public static void registerModelLayer(ResourceLocation id, NonNullFunction<JsonObject, IModelLayer> function) {
        Preconditions.checkNotNull(id);
        Preconditions.checkNotNull(function);
        MODEL_LAYERS.put(id, function);
    }

    public static void registerModelTexture(ResourceLocation id, NonNullFunction<JsonObject, ModelLayerTexture> function) {
        Preconditions.checkNotNull(id);
        Preconditions.checkNotNull(function);
        MODEL_LAYER_TEXTURES.put(id, function);
    }

    public static void registerTextureVariable(ResourceLocation id, NonNullFunction<JsonObject, ITextureVariable> textureVariable) {
        Preconditions.checkNotNull(id);
        Preconditions.checkNotNull(textureVariable);
        TEXTURE_VARIABLES.put(id, textureVariable);
    }

    public static void registerTextureTransformer(ResourceLocation id, NonNullFunction<JsonObject, ITextureTransformer> function) {
        Preconditions.checkNotNull(id);
        Preconditions.checkNotNull(function);
        TEXTURE_TRANSFORMERS.put(id, function);
    }

    public static ModelLayerTexture parseTexture(JsonObject jsonObject) {
        ResourceLocation id = new ResourceLocation(JSONUtils.getString(jsonObject, "type", ThreeCore.MODID + ":default"));
        NonNullFunction<JsonObject, ModelLayerTexture> function = MODEL_LAYER_TEXTURES.get(id);
        return function != null ? function.apply(jsonObject) : null;
    }

    public static ITextureTransformer parseTextureTransformer(JsonObject jsonObject) {
        ResourceLocation id = new ResourceLocation(JSONUtils.getString(jsonObject, "type"));
        NonNullFunction<JsonObject, ITextureTransformer> function = TEXTURE_TRANSFORMERS.get(id);
        return function != null ? function.apply(jsonObject) : null;
    }

    public static ITextureVariable parseTextureVariable(JsonObject jsonObject) {
        ResourceLocation id = new ResourceLocation(JSONUtils.getString(jsonObject, "type"));
        NonNullFunction<JsonObject, ITextureVariable> function = TEXTURE_VARIABLES.get(id);
        return function != null ? function.apply(jsonObject) : null;
    }

    public static IModelLayerPredicate parsePredicate(JsonObject json) {
        NonNullFunction<JsonObject, IModelLayerPredicate> function = PREDICATES.get(new ResourceLocation(JSONUtils.getString(json, "type", "")));
        return function != null ? function.apply(json) : null;
    }

    public static IModelLayer parseLayer(JsonElement jsonElement) {
        if (jsonElement.isJsonPrimitive()) {
            return ModelLayerLoader.getModelLayer(new ResourceLocation(jsonElement.getAsString()));
        }

        JsonObject json = jsonElement.getAsJsonObject();
        NonNullFunction<JsonObject, IModelLayer> function = MODEL_LAYERS.get(new ResourceLocation(JSONUtils.getString(json, "type", ThreeCore.MODID + ":default")));

        if (function == null)
            return null;

        IModelLayer layer = function.apply(json);
        if (JSONUtils.hasField(json, "predicates")) {
            JsonArray predicateArray = JSONUtils.getJsonArray(json, "predicates");
            for (int i = 0; i < predicateArray.size(); i++) {
                IModelLayerPredicate predicate = parsePredicate(predicateArray.get(i).getAsJsonObject());
                if (predicate != null)
                    layer.addPredicate(predicate);
            }
        }
        return layer;
    }

    public static boolean arePredicatesFulFilled(List<IModelLayerPredicate> predicates, IModelLayerContext context) {
        for (IModelLayerPredicate predicate : predicates) {
            if (!predicate.test(context))
                return false;
        }
        return true;
    }

}
