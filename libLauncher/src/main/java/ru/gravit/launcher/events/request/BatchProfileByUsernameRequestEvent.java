package ru.gravit.launcher.events.request;

import ru.gravit.launcher.profiles.PlayerProfile;
import ru.gravit.launcher.LauncherNetworkAPI;
import java.util.UUID;
import ru.gravit.launcher.request.ResultInterface;
import ru.gravit.utils.event.EventInterface;

public class BatchProfileByUsernameRequestEvent implements EventInterface, ResultInterface
{
    private static final UUID uuid;
    @LauncherNetworkAPI
    public String error;
    @LauncherNetworkAPI
    public PlayerProfile[] playerProfiles;
    
    public BatchProfileByUsernameRequestEvent(final PlayerProfile[] profiles) {
        this.playerProfiles = profiles;
    }
    
    public BatchProfileByUsernameRequestEvent() {
    }
    
    @Override
    public UUID getUUID() {
        return BatchProfileByUsernameRequestEvent.uuid;
    }
    
    @Override
    public String getType() {
        return "batchProfileByUsername";
    }
    
    static {
        uuid = UUID.fromString("c1d6729e-be2c-48cc-b5ae-af8c012232c3");
    }
}
