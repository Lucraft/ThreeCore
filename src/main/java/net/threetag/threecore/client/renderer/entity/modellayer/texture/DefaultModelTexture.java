package net.threetag.threecore.client.renderer.entity.modellayer.texture;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.threetag.threecore.client.renderer.entity.modellayer.IModelLayerContext;
import net.threetag.threecore.client.renderer.entity.modellayer.texture.transformer.ITextureTransformer;
import net.threetag.threecore.client.renderer.entity.modellayer.texture.transformer.TransformedTexture;
import net.threetag.threecore.client.renderer.entity.modellayer.texture.variable.ITextureVariable;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class DefaultModelTexture extends ModelLayerTexture {

    private final String base;
    @Nullable
    private final String output;
    private final Map<String, ITextureVariable> textureVariableMap = Maps.newHashMap();
    private List<ITextureTransformer> transformers = Lists.newLinkedList();

    public DefaultModelTexture(String base, @Nullable String output) {
        this.base = base;
        this.output = output;
    }

    @Override
    public ResourceLocation getTexture(IModelLayerContext context) {
        if (this.output == null || this.output.isEmpty() || this.transformers.isEmpty()) {
            return new ResourceLocation(replaceVariables(this.base, context, this.textureVariableMap));
        }

        ResourceLocation output = new ResourceLocation(replaceVariables(this.output, context, this.textureVariableMap));

        if (Minecraft.getInstance().getTextureManager().getTexture(output) == null) {
            String s = replaceVariables(base, context, this.textureVariableMap);
            ResourceLocation texture = new ResourceLocation(s);
            Minecraft.getInstance().getTextureManager().loadTexture(output, new TransformedTexture(texture, this.transformers, transformerPath -> replaceVariables(transformerPath, context, this.textureVariableMap)));
        }

        return output;
    }

    @Override
    public ModelLayerTexture transform(ITextureTransformer textureTransformer) {
        this.transformers.add(textureTransformer);
        return this;
    }

    public DefaultModelTexture addVariable(String key, ITextureVariable textureVariable) {
        this.textureVariableMap.put(key, textureVariable);
        return this;
    }

    public static String replaceVariables(String base, IModelLayerContext context, Map<String, ITextureVariable> textureVariableMap) {
        for (Map.Entry<String, ITextureVariable> entry : textureVariableMap.entrySet()) {
            ITextureVariable variable = entry.getValue();
            base = variable.replace(base, entry.getKey(), context);
        }

        return base;
    }
}
