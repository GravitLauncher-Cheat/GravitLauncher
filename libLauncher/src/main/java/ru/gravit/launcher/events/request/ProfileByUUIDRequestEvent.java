package ru.gravit.launcher.events.request;

import ru.gravit.launcher.profiles.PlayerProfile;
import ru.gravit.launcher.LauncherNetworkAPI;
import java.util.UUID;
import ru.gravit.launcher.request.ResultInterface;
import ru.gravit.utils.event.EventInterface;

public class ProfileByUUIDRequestEvent implements EventInterface, ResultInterface
{
    private static final UUID uuid;
    @LauncherNetworkAPI
    public String error;
    @LauncherNetworkAPI
    public PlayerProfile playerProfile;
    
    public ProfileByUUIDRequestEvent(final PlayerProfile playerProfile) {
        this.playerProfile = playerProfile;
    }
    
    public ProfileByUUIDRequestEvent() {
    }
    
    @Override
    public UUID getUUID() {
        return ProfileByUUIDRequestEvent.uuid;
    }
    
    @Override
    public String getType() {
        return "profileByUUID";
    }
    
    static {
        uuid = UUID.fromString("b9014cf3-4b95-4d38-8c5f-867f190a18a0");
    }
}
