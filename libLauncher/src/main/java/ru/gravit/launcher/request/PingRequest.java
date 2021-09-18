// 
// Decompiled by Procyon v0.5.36
// 

package ru.gravit.launcher.request;

import java.io.IOException;
import ru.gravit.launcher.serialize.HOutput;
import ru.gravit.launcher.serialize.HInput;
import ru.gravit.launcher.LauncherAPI;
import ru.gravit.launcher.LauncherConfig;

public final class PingRequest extends Request<Void>
{
    @LauncherAPI
    public PingRequest() {
        this(null);
    }
    
    @LauncherAPI
    public PingRequest(final LauncherConfig config) {
        super(config);
    }
    
    @Override
    public Integer getLegacyType() {
        return RequestType.PING.getNumber();
    }
    
    @Override
    protected Void requestDo(final HInput input, final HOutput output) throws IOException {
        final byte pong = (byte)input.readUnsignedByte();
        if (pong != 85) {
            throw new IOException("Illegal ping response: " + pong);
        }
        return null;
    }
}
