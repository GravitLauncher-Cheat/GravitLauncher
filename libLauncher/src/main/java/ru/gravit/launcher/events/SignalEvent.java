package ru.gravit.launcher.events;

import java.util.UUID;
import ru.gravit.utils.event.EventInterface;

public class SignalEvent implements EventInterface
{
    private static final UUID uuid;
    public int signal;
    
    public SignalEvent(final int signal) {
        this.signal = signal;
    }
    
    @Override
    public UUID getUUID() {
        return SignalEvent.uuid;
    }
    
    static {
        uuid = UUID.fromString("edc3afa1-2726-4da3-95c6-7e6994b981e1");
    }
}
