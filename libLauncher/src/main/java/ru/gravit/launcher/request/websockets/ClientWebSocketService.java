// 
// Decompiled by Procyon v0.5.36
// 

package ru.gravit.launcher.request.websockets;

import java.io.IOException;
import ru.gravit.launcher.events.request.UpdateRequestEvent;
import ru.gravit.launcher.events.request.ErrorRequestEvent;
import ru.gravit.launcher.events.request.UpdateListRequestEvent;
import ru.gravit.launcher.events.request.SetProfileRequestEvent;
import ru.gravit.launcher.events.request.ProfilesRequestEvent;
import ru.gravit.launcher.events.request.BatchProfileByUsernameRequestEvent;
import ru.gravit.launcher.events.request.ProfileByUUIDRequestEvent;
import ru.gravit.launcher.events.request.ProfileByUsernameRequestEvent;
import ru.gravit.launcher.events.request.LauncherRequestEvent;
import ru.gravit.launcher.events.request.JoinServerRequestEvent;
import ru.gravit.launcher.events.request.CheckServerRequestEvent;
import ru.gravit.launcher.events.request.AuthRequestEvent;
import ru.gravit.launcher.events.request.EchoRequestEvent;
import java.util.Iterator;
import ru.gravit.utils.helper.LogHelper;
import java.net.URI;
import ru.gravit.launcher.hasher.HashedEntryAdapter;
import ru.gravit.launcher.hasher.HashedEntry;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import ru.gravit.launcher.request.ResultInterface;
import java.util.HashMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ClientWebSocketService extends ClientJSONPoint
{
    public final GsonBuilder gsonBuilder;
    public final Gson gson;
    private HashMap<String, Class<? extends RequestInterface>> requests;
    private HashMap<String, Class<? extends ResultInterface>> results;
    private HashSet<EventHandler> handlers;
    
    public ClientWebSocketService(final GsonBuilder gsonBuilder, final String address, final int i) {
        super(createURL(address), Collections.emptyMap(), i);
        this.requests = new HashMap<String, Class<? extends RequestInterface>>();
        this.results = new HashMap<String, Class<? extends ResultInterface>>();
        this.handlers = new HashSet<EventHandler>();
        (this.gsonBuilder = gsonBuilder).registerTypeAdapter(RequestInterface.class, new JsonRequestAdapter(this));
        this.gsonBuilder.registerTypeAdapter(ResultInterface.class, new JsonResultAdapter(this));
        this.gsonBuilder.registerTypeAdapter(HashedEntry.class, new HashedEntryAdapter());
        this.gson = gsonBuilder.create();
    }
    
    private static URI createURL(final String address) {
        try {
            final URI u = new URI(address);
            return u;
        }
        catch (Throwable e) {
            LogHelper.error(e);
            return null;
        }
    }
    
    @Override
    public void onMessage(final String message) {
        final ResultInterface result = this.gson.fromJson(message, ResultInterface.class);
        for (final EventHandler handler : this.handlers) {
            handler.process(result);
        }
    }
    
    @Override
    public void onError(final Exception e) {
        LogHelper.error(e);
    }
    
    public Class<? extends RequestInterface> getRequestClass(final String key) {
        return this.requests.get(key);
    }
    
    public Class<? extends ResultInterface> getResultClass(final String key) {
        return this.results.get(key);
    }
    
    public void registerRequest(final String key, final Class<? extends RequestInterface> clazz) {
        this.requests.put(key, clazz);
    }
    
    public void registerRequests() {
    }
    
    public void registerResult(final String key, final Class<? extends ResultInterface> clazz) {
        this.results.put(key, clazz);
    }
    
    public void registerResults() {
        this.registerResult("echo", EchoRequestEvent.class);
        this.registerResult("auth", AuthRequestEvent.class);
        this.registerResult("checkServer", CheckServerRequestEvent.class);
        this.registerResult("joinServer", JoinServerRequestEvent.class);
        this.registerResult("launcher", LauncherRequestEvent.class);
        this.registerResult("profileByUsername", ProfileByUsernameRequestEvent.class);
        this.registerResult("profileByUUID", ProfileByUUIDRequestEvent.class);
        this.registerResult("batchProfileByUsername", BatchProfileByUsernameRequestEvent.class);
        this.registerResult("profiles", ProfilesRequestEvent.class);
        this.registerResult("setProfile", SetProfileRequestEvent.class);
        this.registerResult("updateList", UpdateListRequestEvent.class);
        this.registerResult("error", ErrorRequestEvent.class);
        this.registerResult("update", UpdateRequestEvent.class);
    }
    
    public void registerHandler(final EventHandler eventHandler) {
        this.handlers.add(eventHandler);
    }
    
    public void sendObject(final Object obj) throws IOException {
        this.send(this.gson.toJson(obj, RequestInterface.class));
    }
    
    public void sendObject(final Object obj, final Type type) throws IOException {
        this.send(this.gson.toJson(obj, type));
    }
    
    @FunctionalInterface
    public interface EventHandler
    {
        void process(final ResultInterface p0);
    }
}
