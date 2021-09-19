// 
// Decompiled by Procyon v0.5.36
// 

package ru.gravit.launcher.events.request;

import ru.gravit.launcher.LauncherNetworkAPI;
import java.util.UUID;
import ru.gravit.utils.event.EventInterface;
import ru.gravit.launcher.request.ResultInterface;

public class EchoRequestEvent implements ResultInterface, EventInterface
{
    private static final UUID uuid;
    @LauncherNetworkAPI
    public String echo;
    
    public EchoRequestEvent(final String echo) {
        this.echo = echo;
    }
    
    @Override
    public String getType() {
        return "echo";
    }
    
    @Override
    public UUID getUUID() {
        return EchoRequestEvent.uuid;
    }
    
    static {
        uuid = UUID.fromString("0a1f820f-7cd5-47a5-ae0e-17492e0e1fe1");
    }
}
