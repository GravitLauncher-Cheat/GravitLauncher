// 
// Decompiled by Procyon v0.5.36
// 

package ru.gravit.launcher.request.uuid;

import ru.gravit.launcher.profiles.PlayerProfile;
import ru.gravit.launcher.serialize.HOutput;
import ru.gravit.launcher.serialize.HInput;
import ru.gravit.launcher.request.RequestType;
import java.io.IOException;
import ru.gravit.launcher.LauncherAPI;
import ru.gravit.utils.helper.VerifyHelper;
import ru.gravit.utils.helper.IOHelper;
import ru.gravit.launcher.LauncherConfig;
import ru.gravit.launcher.events.request.BatchProfileByUsernameRequestEvent;
import ru.gravit.launcher.request.Request;

public final class BatchProfileByUsernameRequest extends Request<BatchProfileByUsernameRequestEvent>
{
    private final String[] usernames;
    
    @LauncherAPI
    public BatchProfileByUsernameRequest(final LauncherConfig config, final String... usernames) throws IOException {
        super(config);
        this.usernames = usernames.clone();
        IOHelper.verifyLength(this.usernames.length, 128);
        for (final String username : this.usernames) {
            VerifyHelper.verifyUsername(username);
        }
    }
    
    @LauncherAPI
    public BatchProfileByUsernameRequest(final String... usernames) throws IOException {
        this((LauncherConfig)null, usernames);
    }
    
    @Override
    public Integer getLegacyType() {
        return RequestType.BATCH_PROFILE_BY_USERNAME.getNumber();
    }
    
    @Override
    protected BatchProfileByUsernameRequestEvent requestDo(final HInput input, final HOutput output) throws IOException {
        output.writeLength(this.usernames.length, 128);
        for (final String username : this.usernames) {
            output.writeString(username, 1024);
            output.writeString("", 128);
        }
        output.flush();
        final PlayerProfile[] profiles = new PlayerProfile[this.usernames.length];
        for (int i = 0; i < profiles.length; ++i) {
            profiles[i] = (input.readBoolean() ? new PlayerProfile(input) : null);
        }
        return new BatchProfileByUsernameRequestEvent(profiles);
    }
}
