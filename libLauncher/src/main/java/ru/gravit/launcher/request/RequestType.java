// 
// Decompiled by Procyon v0.5.36
// 

package ru.gravit.launcher.request;

import ru.gravit.launcher.LauncherAPI;
import java.io.IOException;
import ru.gravit.launcher.serialize.HInput;
import ru.gravit.launcher.serialize.stream.EnumSerializer;

public enum RequestType implements EnumSerializer.Itf
{
    PING(0), 
    LEGACYLAUNCHER(1), 
    UPDATE(2), 
    UPDATE_LIST(3), 
    AUTH(4), 
    JOIN_SERVER(5), 
    CHECK_SERVER(6), 
    PROFILE_BY_USERNAME(7), 
    PROFILE_BY_UUID(8), 
    BATCH_PROFILE_BY_USERNAME(9), 
    PROFILES(10), 
    SERVERAUTH(11), 
    SETPROFILE(12), 
    LAUNCHER(13), 
    CHANGESERVER(14), 
    EXECCOMMAND(15), 
    CUSTOM(255);
    
    private static final EnumSerializer<RequestType> SERIALIZER;
    private final int n;
    
    @LauncherAPI
    public static RequestType read(final HInput input) throws IOException {
        return RequestType.SERIALIZER.read(input);
    }
    
    private RequestType(final int n) {
        this.n = n;
    }
    
    @Override
    public int getNumber() {
        return this.n;
    }
    
    static {
        SERIALIZER = new EnumSerializer<RequestType>(RequestType.class);
    }
}
