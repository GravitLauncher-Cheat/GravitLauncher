package ru.gravit.launcher.events.request;

import ru.gravit.launcher.profiles.PlayerProfile;
import ru.gravit.launcher.LauncherNetworkAPI;
import java.util.UUID;
import ru.gravit.launcher.request.ResultInterface;
import ru.gravit.utils.event.EventInterface;

public class ProfileByUsernameRequestEvent implements EventInterface, ResultInterface
{
    private static final UUID uuid;
    @LauncherNetworkAPI
    public String error;
    @LauncherNetworkAPI
    public PlayerProfile playerProfile;
    
    public ProfileByUsernameRequestEvent(final PlayerProfile playerProfile) {
        this.playerProfile = playerProfile;
    }
    
    @Override
    public UUID getUUID() {
        return ProfileByUsernameRequestEvent.uuid;
    }
    
    @Override
    public String getType() {
        return "profileByUsername";
    }
    
    static {
        uuid = UUID.fromString("06204302-ff6b-4779-b97d-541e3bc39aa1");
    }
}
