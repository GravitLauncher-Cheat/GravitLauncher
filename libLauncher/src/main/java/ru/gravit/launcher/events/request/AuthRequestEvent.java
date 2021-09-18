package ru.gravit.launcher.events.request;

import ru.gravit.launcher.profiles.PlayerProfile;
import ru.gravit.launcher.ClientPermissions;
import ru.gravit.launcher.LauncherNetworkAPI;
import java.util.UUID;
import ru.gravit.launcher.request.ResultInterface;
import ru.gravit.utils.event.EventInterface;

public class AuthRequestEvent implements EventInterface, ResultInterface
{
    private static final UUID uuid;
    @LauncherNetworkAPI
    public String error;
    @LauncherNetworkAPI
    public ClientPermissions permissions;
    @LauncherNetworkAPI
    public PlayerProfile playerProfile;
    @LauncherNetworkAPI
    public String accessToken;
    @LauncherNetworkAPI
    public String protectToken;
    
    public AuthRequestEvent() {
    }
    
    public AuthRequestEvent(final PlayerProfile pp, final String accessToken, final ClientPermissions permissions) {
        this.playerProfile = pp;
        this.accessToken = accessToken;
        this.permissions = permissions;
    }
    
    public AuthRequestEvent(final ClientPermissions permissions, final PlayerProfile playerProfile, final String accessToken, final String protectToken) {
        this.permissions = permissions;
        this.playerProfile = playerProfile;
        this.accessToken = accessToken;
        this.protectToken = protectToken;
    }
    
    @Override
    public UUID getUUID() {
        return AuthRequestEvent.uuid;
    }
    
    @Override
    public String getType() {
        return "auth";
    }
    
    static {
        uuid = UUID.fromString("77e1bfd7-adf9-4f5d-87d6-a7dd068deb74");
    }
}
