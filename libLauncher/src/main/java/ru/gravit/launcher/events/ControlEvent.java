// 
// Decompiled by Procyon v0.5.36
// 

package ru.gravit.launcher.events;

import java.util.UUID;
import ru.gravit.utils.event.EventInterface;

public class ControlEvent implements EventInterface
{
    private static final UUID uuid;
    public ControlCommand signal;
    
    public ControlEvent(final ControlCommand signal) {
        this.signal = signal;
    }
    
    @Override
    public UUID getUUID() {
        return ControlEvent.uuid;
    }
    
    static {
        uuid = UUID.fromString("f1051a64-0cd0-4ed8-8430-d856a196e91f");
    }
    
    public enum ControlCommand
    {
        STOP, 
        START, 
        PAUSE, 
        CONTINUE, 
        CRASH;
    }
}
