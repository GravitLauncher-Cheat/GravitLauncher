package ru.gravit.launcher.events.request;

import ru.gravit.launcher.profiles.ClientProfile;
import java.util.UUID;
import ru.gravit.utils.event.EventInterface;
import ru.gravit.launcher.request.ResultInterface;

public class SetProfileRequestEvent implements ResultInterface, EventInterface
{
    private static final UUID uuid;
    public ClientProfile newProfile;
    
    public SetProfileRequestEvent(final ClientProfile newProfile) {
        this.newProfile = newProfile;
    }
    
    @Override
    public String getType() {
        return "setProfile";
    }
    
    @Override
    public UUID getUUID() {
        return SetProfileRequestEvent.uuid;
    }
    
    static {
        uuid = UUID.fromString("08c0de9e-4364-4152-9066-8354a3a48541");
    }
}
