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
import java.util.Objects;
import ru.gravit.launcher.LauncherConfig;
import java.util.UUID;
import ru.gravit.launcher.events.request.ProfileByUUIDRequestEvent;
import ru.gravit.launcher.request.Request;

public final class ProfileByUUIDRequest extends Request<ProfileByUUIDRequestEvent>
{
    private final UUID uuid;
    
    @LauncherAPI
    public ProfileByUUIDRequest(final LauncherConfig config, final UUID uuid) {
        super(config);
        this.uuid = Objects.requireNonNull(uuid, "uuid");
    }
    
    @LauncherAPI
    public ProfileByUUIDRequest(final UUID uuid) {
        this(null, uuid);
    }
    
    @Override
    public Integer getLegacyType() {
        return RequestType.PROFILE_BY_UUID.getNumber();
    }
    
    @Override
    protected ProfileByUUIDRequestEvent requestDo(final HInput input, final HOutput output) throws IOException {
        output.writeUUID(this.uuid);
        output.writeString(Launcher.profile.getTitle(), 128);
        output.flush();
        return input.readBoolean() ? new ProfileByUUIDRequestEvent(new PlayerProfile(input)) : null;
    }
}
