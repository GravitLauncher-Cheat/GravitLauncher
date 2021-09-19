// 
// Decompiled by Procyon v0.5.36
// 

package ru.gravit.launcher.events.request;

import ru.gravit.launcher.LauncherNetworkAPI;
import java.util.UUID;
import ru.gravit.launcher.request.ResultInterface;
import ru.gravit.utils.event.EventInterface;

public class LauncherRequestEvent implements EventInterface, ResultInterface
{
    private static final UUID uuid;
    @LauncherNetworkAPI
    public String url;
    @LauncherNetworkAPI
    public byte[] digest;
    @LauncherNetworkAPI
    public byte[] binary;
    public boolean needUpdate;
    
    public LauncherRequestEvent(final boolean needUpdate, final String url) {
        this.needUpdate = needUpdate;
        this.url = url;
    }
    
    public LauncherRequestEvent(final boolean b, final byte[] digest) {
        this.needUpdate = b;
        this.digest = digest;
    }
    
    public LauncherRequestEvent(final byte[] binary, final byte[] digest) {
        this.binary = binary;
        this.digest = digest;
    }
    
    @Override
    public UUID getUUID() {
        return LauncherRequestEvent.uuid;
    }
    
    @Override
    public String getType() {
        return "launcher";
    }
    
    static {
        uuid = UUID.fromString("d54cc12a-4f59-4f23-9b10-f527fdd2e38f");
    }
}
