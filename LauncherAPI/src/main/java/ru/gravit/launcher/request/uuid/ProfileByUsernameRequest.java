// 
// Decompiled by Procyon v0.5.36
// 

package ru.gravit.launcher.request.uuid;

import java.io.IOException;
import ru.gravit.launcher.profiles.PlayerProfile;
import ru.gravit.launcher.Launcher;
import ru.gravit.launcher.serialize.HOutput;
import ru.gravit.launcher.serialize.HInput;
import ru.gravit.launcher.request.RequestType;
import ru.gravit.launcher.LauncherAPI;
import ru.gravit.utils.helper.VerifyHelper;
import ru.gravit.launcher.LauncherConfig;
import ru.gravit.launcher.events.request.ProfileByUsernameRequestEvent;
import ru.gravit.launcher.request.Request;

public final class ProfileByUsernameRequest extends Request<ProfileByUsernameRequestEvent>
{
    private final String username;
    
    @LauncherAPI
    public ProfileByUsernameRequest(final LauncherConfig config, final String username) {
        super(config);
        this.username = VerifyHelper.verifyUsername(username);
    }
    
    @LauncherAPI
    public ProfileByUsernameRequest(final String username) {
        this(null, username);
    }
    
    @Override
    public Integer getLegacyType() {
        return RequestType.PROFILE_BY_USERNAME.getNumber();
    }
    
    @Override
    protected ProfileByUsernameRequestEvent requestDo(final HInput input, final HOutput output) throws IOException {
        output.writeString(this.username, 1024);
        output.writeString(Launcher.profile.getTitle(), 128);
        output.flush();
        return input.readBoolean() ? new ProfileByUsernameRequestEvent(new PlayerProfile(input)) : null;
    }
}
