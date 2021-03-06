package net.threetag.threecore.util.threedata;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.JSONUtils;

public class BooleanThreeData extends ThreeData<Boolean> {

    public BooleanThreeData(String key) {
        super(key);
    }

    @Override
    public Boolean parseValue(JsonObject jsonObject, Boolean defaultValue) {
        return JSONUtils.getBoolean(jsonObject, this.jsonKey, defaultValue);
    }

    @Override
    public void writeToNBT(CompoundNBT nbt, Boolean value) {
        nbt.putBoolean(this.key, value);
    }

    @Override
    public Boolean readFromNBT(CompoundNBT nbt, Boolean defaultValue) {
        if (!nbt.contains(this.key))
            return defaultValue;
        return nbt.getBoolean(this.key);
    }

    @Override
    public JsonElement serializeJson(Boolean value) {
        return new JsonPrimitive(value);
    }
}
