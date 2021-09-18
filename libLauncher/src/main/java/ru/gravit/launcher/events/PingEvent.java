package ru.gravit.launcher.events;

import java.util.UUID;
import ru.gravit.utils.event.EventInterface;

public final class PingEvent implements EventInterface
{
    private static final UUID uuid;
    
    @Override
    public UUID getUUID() {
        return PingEvent.uuid;
    }
    
    static {
        uuid = UUID.fromString("7c8be7e7-82ce-4c99-84cd-ee8fcce1b509");
    }
}
