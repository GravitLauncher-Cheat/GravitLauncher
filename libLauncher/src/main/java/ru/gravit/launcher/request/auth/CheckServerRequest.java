// 
// Decompiled by Procyon v0.5.36
// 

package ru.gravit.launcher.request.auth;

import java.io.IOException;
import ru.gravit.launcher.profiles.PlayerProfile;
import ru.gravit.utils.helper.LogHelper;
import ru.gravit.launcher.Launcher;
import ru.gravit.launcher.serialize.HOutput;
import ru.gravit.launcher.serialize.HInput;
import ru.gravit.launcher.request.RequestType;
import ru.gravit.launcher.LauncherAPI;
import ru.gravit.utils.helper.VerifyHelper;
import ru.gravit.launcher.LauncherConfig;
import ru.gravit.launcher.events.request.CheckServerRequestEvent;
import ru.gravit.launcher.request.Request;

public final class CheckServerRequest extends Request<CheckServerRequestEvent>
{
    private final String username;
    private final String serverID;
    
    @LauncherAPI
    public CheckServerRequest(final LauncherConfig config, final String username, final String serverID) {
        super(config);
        this.username = VerifyHelper.verifyUsername(username);
        this.serverID = VerifyHelper.verifyServerID(serverID);
    }
    
    @LauncherAPI
    public CheckServerRequest(final String username, final String serverID) {
        this(null, username, serverID);
    }
    
    @Override
    public Integer getLegacyType() {
        return RequestType.CHECK_SERVER.getNumber();
    }
    
    @Override
    protected CheckServerRequestEvent requestDo(final HInput input, final HOutput output) throws IOException {
        output.writeString(this.username, 1024);
        output.writeASCII(this.serverID, 128);
        if (Launcher.profile == null) {
            LogHelper.error("Profile is null. Title is not net.");
            output.writeString("", 128);
        }
        else {
            output.writeString(Launcher.profile.getTitle(), 128);
        }
        output.flush();
        this.readError(input);
        return input.readBoolean() ? new CheckServerRequestEvent(new PlayerProfile(input)) : null;
    }
}
