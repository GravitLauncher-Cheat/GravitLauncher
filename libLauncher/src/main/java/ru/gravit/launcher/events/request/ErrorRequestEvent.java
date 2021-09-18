// 
// Decompiled by Procyon v0.5.36
// 

package ru.gravit.launcher.events.request;

import java.util.UUID;
import ru.gravit.utils.event.EventInterface;
import ru.gravit.launcher.request.ResultInterface;

public class ErrorRequestEvent implements ResultInterface, EventInterface
{
    public static UUID uuid;
    public final String error;
    
    public ErrorRequestEvent(final String error) {
        this.error = error;
    }
    
    @Override
    public String getType() {
        return "error";
    }
    
    @Override
    public UUID getUUID() {
        return null;
    }
    
    static {
        ErrorRequestEvent.uuid = UUID.fromString("0af22bc7-aa01-4881-bdbb-dc62b3cdac96");
    }
}
