// 
// Decompiled by Procyon v0.5.36
// 

package ru.gravit.launcher.request.auth;

import java.io.IOException;
import ru.gravit.launcher.serialize.HOutput;
import ru.gravit.launcher.serialize.HInput;
import ru.gravit.launcher.request.RequestType;
import ru.gravit.launcher.LauncherAPI;
import ru.gravit.utils.helper.SecurityHelper;
import ru.gravit.utils.helper.VerifyHelper;
import ru.gravit.launcher.LauncherConfig;
import ru.gravit.launcher.events.request.JoinServerRequestEvent;
import ru.gravit.launcher.request.Request;

public final class JoinServerRequest extends Request<JoinServerRequestEvent>
{
    private final String username;
    private final String accessToken;
    private final String serverID;
    
    @LauncherAPI
    public JoinServerRequest(final LauncherConfig config, final String username, final String accessToken, final String serverID) {
        super(config);
        this.username = VerifyHelper.verifyUsername(username);
        this.accessToken = SecurityHelper.verifyToken(accessToken);
        this.serverID = VerifyHelper.verifyServerID(serverID);
    }
    
    @LauncherAPI
    public JoinServerRequest(final String username, final String accessToken, final String serverID) {
        this(null, username, accessToken, serverID);
    }
    
    @Override
    public Integer getLegacyType() {
        return RequestType.JOIN_SERVER.getNumber();
    }
    
    @Override
    protected JoinServerRequestEvent requestDo(final HInput input, final HOutput output) throws IOException {
        output.writeString(this.username, 1024);
        output.writeASCII(this.accessToken, -32);
        output.writeASCII(this.serverID, 128);
        output.flush();
        this.readError(input);
        return new JoinServerRequestEvent(input.readBoolean());
    }
}
