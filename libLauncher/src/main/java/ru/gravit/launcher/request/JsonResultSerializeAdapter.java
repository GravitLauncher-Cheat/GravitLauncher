package ru.gravit.launcher.request;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import java.lang.reflect.Type;
import com.google.gson.JsonSerializer;

public class JsonResultSerializeAdapter implements JsonSerializer<ResultInterface>
{
    private static final String PROP_NAME = "type";
    
    @Override
    public JsonElement serialize(final ResultInterface src, final Type typeOfSrc, final JsonSerializationContext context) {
        final JsonObject jo = context.serialize(src).getAsJsonObject();
        final String classPath = src.getType();
        jo.add("type", new JsonPrimitive(classPath));
        return jo;
    }
}
