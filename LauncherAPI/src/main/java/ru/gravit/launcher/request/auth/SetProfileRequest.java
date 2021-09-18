// 
// Decompiled by Procyon v0.5.36
// 

package ru.gravit.launcher.request.auth;

import ru.gravit.launcher.serialize.HOutput;
import ru.gravit.launcher.serialize.HInput;
import ru.gravit.launcher.request.RequestType;
import ru.gravit.launcher.request.websockets.LegacyRequestBridge;
import ru.gravit.launcher.LauncherConfig;
import ru.gravit.launcher.profiles.ClientProfile;
import ru.gravit.launcher.request.websockets.RequestInterface;
import ru.gravit.launcher.events.request.SetProfileRequestEvent;
import ru.gravit.launcher.request.Request;

public class SetProfileRequest extends Request<SetProfileRequestEvent> implements RequestInterface
{
    private transient ClientProfile profile;
    public String client;
    
    public SetProfileRequest(final LauncherConfig config, final ClientProfile profile) {
        super(config);
        this.profile = profile;
        this.client = profile.getTitle();
    }
    
    public SetProfileRequestEvent requestWebSockets() throws Exception {
        return (SetProfileRequestEvent)LegacyRequestBridge.sendRequest(this);
    }
    
    @Override
    public Integer getLegacyType() {
        return RequestType.SETPROFILE.getNumber();
    }
    
    @Override
    protected SetProfileRequestEvent requestDo(final HInput input, final HOutput output) throws Exception {
        output.writeString(this.profile.getTitle(), 128);
        this.readError(input);
        return new SetProfileRequestEvent(this.profile);
    }
    
    @Override
    public String getType() {
        return "setProfile";
    }
}
