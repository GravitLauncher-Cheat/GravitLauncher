package ru.gravit.launcher.events.request;

import java.util.UUID;
import ru.gravit.launcher.request.ResultInterface;
import ru.gravit.utils.event.EventInterface;

public class JoinServerRequestEvent implements EventInterface, ResultInterface
{
    private static final UUID uuid;
    public boolean allow;
    
    public JoinServerRequestEvent(final boolean allow) {
        this.allow = allow;
    }
    
    @Override
    public UUID getUUID() {
        return JoinServerRequestEvent.uuid;
    }
    
    @Override
    public String getType() {
        return "joinServer";
    }
    
    static {
        uuid = UUID.fromString("2a12e7b5-3f4a-4891-a2f9-ea141c8e1995");
    }
}
