// 
// Decompiled by Procyon v0.5.36
// 

package ru.gravit.launcher.hasher;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonParseException;
import com.google.gson.JsonDeserializationContext;
import java.lang.reflect.Type;
import com.google.gson.JsonElement;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

public class HashedEntryAdapter implements JsonSerializer<HashedEntry>, JsonDeserializer<HashedEntry>
{
    private static final String PROP_NAME = "type";
    
    @Override
    public HashedEntry deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        final String typename = json.getAsJsonObject().getAsJsonPrimitive("type").getAsString();
        Class<?> cls = null;
        if (typename.equals("dir")) {
            cls = HashedDir.class;
        }
        if (typename.equals("file")) {
            cls = HashedFile.class;
        }
        return context.deserialize(json, cls);
    }
    
    @Override
    public JsonElement serialize(final HashedEntry src, final Type typeOfSrc, final JsonSerializationContext context) {
        final JsonObject jo = context.serialize(src).getAsJsonObject();
        final HashedEntry.Type type = src.getType();
        if (type == HashedEntry.Type.DIR) {
            jo.add("type", new JsonPrimitive("dir"));
        }
        if (type == HashedEntry.Type.FILE) {
            jo.add("type", new JsonPrimitive("file"));
        }
        return jo;
    }
}
