package ru.gravit.launcher.events.request;

import ru.gravit.launcher.LauncherNetworkAPI;
import ru.gravit.launcher.profiles.ClientProfile;
import java.util.List;
import java.util.UUID;
import ru.gravit.launcher.request.ResultInterface;
import ru.gravit.utils.event.EventInterface;

public class ProfilesRequestEvent implements EventInterface, ResultInterface
{
    private static final UUID uuid;
    @LauncherNetworkAPI
    public List<ClientProfile> profiles;
    String error;
    
    public ProfilesRequestEvent(final List<ClientProfile> profiles) {
        this.profiles = profiles;
    }
    
    public ProfilesRequestEvent() {
    }
    
    @Override
    public UUID getUUID() {
        return ProfilesRequestEvent.uuid;
    }
    
    @Override
    public String getType() {
        return "profiles";
    }
    
    static {
        uuid = UUID.fromString("2f26fbdf-598a-46dd-92fc-1699c0e173b1");
    }
}
