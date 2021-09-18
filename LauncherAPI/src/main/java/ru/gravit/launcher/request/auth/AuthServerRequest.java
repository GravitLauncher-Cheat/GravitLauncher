// 
// Decompiled by Procyon v0.5.36
// 

package ru.gravit.launcher.request.auth;

import ru.gravit.launcher.profiles.PlayerProfile;
import java.io.IOException;
import ru.gravit.launcher.serialize.HOutput;
import ru.gravit.launcher.serialize.HInput;
import ru.gravit.launcher.request.RequestType;
import ru.gravit.launcher.LauncherAPI;
import ru.gravit.utils.helper.VerifyHelper;
import ru.gravit.launcher.LauncherConfig;
import ru.gravit.launcher.ClientPermissions;
import ru.gravit.launcher.request.Request;

public final class AuthServerRequest extends Request<ClientPermissions>
{
    private final String login;
    private final byte[] encryptedPassword;
    private final String auth_id;
    private final String title;
    
    @LauncherAPI
    public AuthServerRequest(final LauncherConfig config, final String login, final byte[] encryptedPassword) {
        super(config);
        this.login = VerifyHelper.verify(login, VerifyHelper.NOT_EMPTY, "Login can't be empty");
        this.encryptedPassword = encryptedPassword.clone();
        this.auth_id = "";
        this.title = "";
    }
    
    @LauncherAPI
    public AuthServerRequest(final LauncherConfig config, final String login, final byte[] encryptedPassword, final String auth_id) {
        super(config);
        this.login = VerifyHelper.verify(login, VerifyHelper.NOT_EMPTY, "Login can't be empty");
        this.encryptedPassword = encryptedPassword.clone();
        this.auth_id = auth_id;
        this.title = "";
    }
    
    @LauncherAPI
    public AuthServerRequest(final LauncherConfig config, final String login, final byte[] encryptedPassword, final String auth_id, final String title) {
        super(config);
        this.login = VerifyHelper.verify(login, VerifyHelper.NOT_EMPTY, "Login can't be empty");
        this.encryptedPassword = encryptedPassword.clone();
        this.auth_id = auth_id;
        this.title = title;
    }
    
    @LauncherAPI
    public AuthServerRequest(final String login, final byte[] encryptedPassword) {
        this(null, login, encryptedPassword);
    }
    
    @LauncherAPI
    public AuthServerRequest(final String login, final byte[] encryptedPassword, final String auth_id) {
        this(null, login, encryptedPassword, auth_id);
    }
    
    @Override
    public Integer getLegacyType() {
        return RequestType.SERVERAUTH.getNumber();
    }
    
    @Override
    protected ClientPermissions requestDo(final HInput input, final HOutput output) throws IOException {
        output.writeString(this.login, 1024);
        output.writeString(this.title, 128);
        output.writeString(this.auth_id, 128);
        output.writeByteArray(this.encryptedPassword, 2048);
        output.flush();
        this.readError(input);
        return new ClientPermissions(input);
    }
    
    public static final class Result
    {
        @LauncherAPI
        public final PlayerProfile pp;
        @LauncherAPI
        public final String accessToken;
        
        private Result(final PlayerProfile pp, final String accessToken) {
            this.pp = pp;
            this.accessToken = accessToken;
        }
    }
}
