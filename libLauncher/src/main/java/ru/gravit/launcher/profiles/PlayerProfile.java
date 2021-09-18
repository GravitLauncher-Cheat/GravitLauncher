package ru.gravit.launcher.profiles;

import ru.gravit.launcher.serialize.HOutput;
import java.util.Objects;
import java.io.IOException;
import ru.gravit.utils.helper.VerifyHelper;
import ru.gravit.launcher.serialize.HInput;
import ru.gravit.utils.helper.IOHelper;
import ru.gravit.launcher.LauncherAPI;
import java.util.UUID;
import ru.gravit.launcher.serialize.stream.StreamObject;

public final class PlayerProfile extends StreamObject
{
    @LauncherAPI
    public final UUID uuid;
    @LauncherAPI
    public final String username;
    @LauncherAPI
    public final Texture skin;
    @LauncherAPI
    public final Texture cloak;
    
    @LauncherAPI
    public static PlayerProfile newOfflineProfile(final String username) {
        return new PlayerProfile(offlineUUID(username), username, null, null);
    }
    
    @LauncherAPI
    public static UUID offlineUUID(final String username) {
        return UUID.nameUUIDFromBytes(IOHelper.encodeASCII("OfflinePlayer:" + username));
    }
    
    @LauncherAPI
    public PlayerProfile(final HInput input) throws IOException {
        this.uuid = input.readUUID();
        this.username = VerifyHelper.verifyUsername(input.readString(64));
        this.skin = (input.readBoolean() ? new Texture(input) : null);
        this.cloak = (input.readBoolean() ? new Texture(input) : null);
    }
    
    @LauncherAPI
    public PlayerProfile(final UUID uuid, final String username, final Texture skin, final Texture cloak) {
        this.uuid = Objects.requireNonNull(uuid, "uuid");
        this.username = VerifyHelper.verifyUsername(username);
        this.skin = skin;
        this.cloak = cloak;
    }
    
    @Override
    public void write(final HOutput output) throws IOException {
        output.writeUUID(this.uuid);
        output.writeString(this.username, 64);
        output.writeBoolean(this.skin != null);
        if (this.skin != null) {
            this.skin.write(output);
        }
        output.writeBoolean(this.cloak != null);
        if (this.cloak != null) {
            this.cloak.write(output);
        }
    }
}
