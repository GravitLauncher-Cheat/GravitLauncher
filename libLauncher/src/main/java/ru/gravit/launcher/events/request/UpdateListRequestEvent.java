// 
// Decompiled by Procyon v0.5.36
// 

package ru.gravit.launcher.events.request;

import ru.gravit.launcher.LauncherNetworkAPI;
import java.util.HashSet;
import java.util.UUID;
import ru.gravit.launcher.request.ResultInterface;
import ru.gravit.utils.event.EventInterface;

public class UpdateListRequestEvent implements EventInterface, ResultInterface
{
    private static final UUID uuid;
    @LauncherNetworkAPI
    public final HashSet<String> dirs;
    
    public UpdateListRequestEvent(final HashSet<String> dirs) {
        this.dirs = dirs;
    }
    
    @Override
    public UUID getUUID() {
        return UpdateListRequestEvent.uuid;
    }
    
    @Override
    public String getType() {
        return "updateList";
    }
    
    static {
        uuid = UUID.fromString("5fa836ae-6b61-401c-96ac-d8396f07ec6b");
    }
}
