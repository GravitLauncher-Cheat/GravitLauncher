// 
// Decompiled by Procyon v0.5.36
// 

package ru.gravit.launcher.request.auth;

import java.io.IOException;
import ru.gravit.launcher.ClientPermissions;
import ru.gravit.launcher.profiles.PlayerProfile;
import ru.gravit.launcher.Launcher;
import ru.gravit.launcher.serialize.HOutput;
import ru.gravit.launcher.serialize.HInput;
import ru.gravit.launcher.request.RequestType;
import ru.gravit.launcher.request.websockets.LegacyRequestBridge;
import ru.gravit.launcher.LauncherAPI;
import ru.gravit.utils.helper.VerifyHelper;
import ru.gravit.launcher.LauncherConfig;
import ru.gravit.launcher.HWID;
import ru.gravit.launcher.request.websockets.RequestInterface;
import ru.gravit.launcher.events.request.AuthRequestEvent;
import ru.gravit.launcher.request.Request;

public final class AuthRequest extends Request<AuthRequestEvent> implements RequestInterface
{
    private final String login;
    private final byte[] encryptedPassword;
    private final String auth_id;
    private final HWID hwid;
    private final String customText;
    
    @LauncherAPI
    public AuthRequest(final LauncherConfig config, final String login, final byte[] password, final HWID hwid) {
        super(config);
        this.login = VerifyHelper.verify(login, VerifyHelper.NOT_EMPTY, "Login can't be empty");
        this.encryptedPassword = password.clone();
        this.hwid = hwid;
        this.customText = "";
        this.auth_id = "";
    }
    
    @LauncherAPI
    public AuthRequest(final LauncherConfig config, final String login, final byte[] password, final HWID hwid, final String auth_id) {
        super(config);
        this.login = VerifyHelper.verify(login, VerifyHelper.NOT_EMPTY, "Login can't be empty");
        this.encryptedPassword = password.clone();
        this.hwid = hwid;
        this.auth_id = auth_id;
        this.customText = "";
    }
    
    @LauncherAPI
    public AuthRequest(final LauncherConfig config, final String login, final byte[] password, final HWID hwid, final String customText, final String auth_id) {
        super(config);
        this.login = VerifyHelper.verify(login, VerifyHelper.NOT_EMPTY, "Login can't be empty");
        this.encryptedPassword = password.clone();
        this.hwid = hwid;
        this.auth_id = auth_id;
        this.customText = customText;
    }
    
    @LauncherAPI
    public AuthRequest(final String login, final byte[] password, final HWID hwid) {
        this(null, login, password, hwid);
    }
    
    public AuthRequestEvent requestWebSockets() throws Exception {
        return (AuthRequestEvent)LegacyRequestBridge.sendRequest(this);
    }
    
    @LauncherAPI
    public AuthRequest(final String login, final byte[] password, final HWID hwid, final String auth_id) {
        this(null, login, password, hwid, auth_id);
    }
    
    @Override
    public Integer getLegacyType() {
        return RequestType.AUTH.getNumber();
    }
    
    @Override
    protected AuthRequestEvent requestDo(final HInput input, final HOutput output) throws IOException {
        output.writeString(this.login, 1024);
        output.writeBoolean(Launcher.profile != null);
        if (Launcher.profile != null) {
            output.writeString(Launcher.profile.getTitle(), 128);
        }
        output.writeString(this.auth_id, 128);
        output.writeString(this.hwid.getSerializeString(), 0);
        output.writeByteArray(this.encryptedPassword, 2048);
        output.writeString(this.customText, 512);
        output.flush();
        this.readError(input);
        final PlayerProfile pp = new PlayerProfile(input);
        final String accessToken = input.readASCII(-32);
        final ClientPermissions permissions = new ClientPermissions(input);
        final String protectToken = input.readString(512);
        return new AuthRequestEvent(permissions, pp, accessToken, protectToken);
    }
    
    @Override
    public String getType() {
        return "auth";
    }
}
