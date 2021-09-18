// 
// Decompiled by Procyon v0.5.36
// 

package ru.gravit.launcher.request.websockets;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonParseException;
import com.google.gson.JsonDeserializationContext;
import java.lang.reflect.Type;
import com.google.gson.JsonElement;
import com.google.gson.JsonDeserializer;
import ru.gravit.launcher.request.ResultInterface;
import com.google.gson.JsonSerializer;

public class JsonResultAdapter implements JsonSerializer<ResultInterface>, JsonDeserializer<ResultInterface>
{
    private final ClientWebSocketService service;
    private static final String PROP_NAME = "type";
    
    public JsonResultAdapter(final ClientWebSocketService service) {
        this.service = service;
    }
    
    @Override
    public ResultInterface deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        final String typename = json.getAsJsonObject().getAsJsonPrimitive("type").getAsString();
        final Class<? extends ResultInterface> cls = this.service.getResultClass(typename);
        return context.deserialize(json, cls);
    }
    
    @Override
    public JsonElement serialize(final ResultInterface src, final Type typeOfSrc, final JsonSerializationContext context) {
        final JsonObject jo = context.serialize(src).getAsJsonObject();
        final String classPath = src.getType();
        jo.add("type", new JsonPrimitive(classPath));
        return jo;
    }
}
