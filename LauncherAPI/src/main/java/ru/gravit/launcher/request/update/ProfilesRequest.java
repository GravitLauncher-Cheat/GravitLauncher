package ru.gravit.launcher.request.update;

import java.util.List;
import ru.gravit.launcher.Launcher;
import ru.gravit.launcher.profiles.ClientProfile;
import java.util.ArrayList;
import ru.gravit.launcher.serialize.HOutput;
import ru.gravit.launcher.serialize.HInput;
import ru.gravit.launcher.request.websockets.LegacyRequestBridge;
import ru.gravit.launcher.request.RequestType;
import ru.gravit.launcher.LauncherAPI;
import ru.gravit.launcher.LauncherConfig;
import ru.gravit.launcher.request.websockets.RequestInterface;
import ru.gravit.launcher.events.request.ProfilesRequestEvent;
import ru.gravit.launcher.request.Request;

public final class ProfilesRequest extends Request<ProfilesRequestEvent> implements RequestInterface
{
    @LauncherAPI
    public ProfilesRequest() {
        this(null);
    }
    
    @LauncherAPI
    public ProfilesRequest(final LauncherConfig config) {
        super(config);
    }
    
    @Override
    public Integer getLegacyType() {
        return RequestType.PROFILES.getNumber();
    }
    
    public ProfilesRequestEvent requestWebSockets() throws Exception {
        return (ProfilesRequestEvent)LegacyRequestBridge.sendRequest(this);
    }
    
    @Override
    protected ProfilesRequestEvent requestDo(final HInput input, final HOutput output) throws Exception {
        output.writeBoolean(true);
        output.flush();
        this.readError(input);
        final int count = input.readLength(0);
        //input.readLength(0);
        final List<ClientProfile> profiles = new ArrayList<ClientProfile>(count);
        for (int i = 0; i < count; ++i) {
            final String prof = input.readString(0);
            profiles.add(Launcher.gson.fromJson(prof, ClientProfile.class));
        }
        return new ProfilesRequestEvent(profiles);
    }
    
    @Override
    public String getType() {
        return "profiles";
    }
}
