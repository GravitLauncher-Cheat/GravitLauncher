package ru.gravit.launcher;

import ru.gravit.launcher.serialize.HOutput;
import java.io.IOException;
import ru.gravit.launcher.serialize.HInput;

public class ClientPermissions
{
    public static final ClientPermissions DEFAULT;
    @LauncherAPI
    public boolean canAdmin;
    @LauncherAPI
    public boolean canServer;
    @LauncherAPI
    public boolean canUSR1;
    @LauncherAPI
    public boolean canUSR2;
    @LauncherAPI
    public boolean canUSR3;
    @LauncherAPI
    public boolean canBot;
    
    public ClientPermissions(final HInput input) throws IOException {
        this.canAdmin = false;
        this.canServer = false;
        this.canUSR1 = false;
        this.canUSR2 = false;
        this.canUSR3 = false;
        this.canBot = false;
        this.canAdmin = input.readBoolean();
        this.canServer = input.readBoolean();
        this.canUSR1 = input.readBoolean();
        this.canUSR2 = input.readBoolean();
        this.canUSR3 = input.readBoolean();
        this.canBot = input.readBoolean();
    }
    
    public ClientPermissions() {
        this.canAdmin = false;
        this.canServer = false;
        this.canUSR1 = false;
        this.canUSR2 = false;
        this.canUSR3 = false;
        this.canBot = false;
        this.canAdmin = false;
        this.canServer = false;
        this.canUSR1 = false;
        this.canUSR2 = false;
        this.canUSR3 = false;
        this.canBot = false;
    }
    
    public ClientPermissions(final long data) {
        this.canAdmin = false;
        this.canServer = false;
        this.canUSR1 = false;
        this.canUSR2 = false;
        this.canUSR3 = false;
        this.canBot = false;
        this.canAdmin = ((data & 0x1L) != 0x0L);
        this.canServer = ((data & 0x2L) != 0x0L);
        this.canUSR1 = ((data & 0x4L) != 0x0L);
        this.canUSR2 = ((data & 0x8L) != 0x0L);
        this.canUSR3 = ((data & 0x10L) != 0x0L);
        this.canBot = ((data & 0x20L) != 0x0L);
    }
    
    @LauncherAPI
    public long toLong() {
        long result = 0L;
        result |= (this.canAdmin ? 0 : 1);
        result |= (this.canServer ? 0L : 2L);
        result |= (this.canUSR1 ? 0L : 4L);
        result |= (this.canUSR2 ? 0L : 8L);
        result |= (this.canUSR3 ? 0L : 16L);
        result |= (this.canBot ? 0L : 32L);
        return result;
    }
    
    public static ClientPermissions getSuperuserAccount() {
        final ClientPermissions perm = new ClientPermissions();
        perm.canServer = true;
        perm.canAdmin = true;
        return perm;
    }
    
    public void write(final HOutput output) throws IOException {
        output.writeBoolean(this.canAdmin);
        output.writeBoolean(this.canServer);
        output.writeBoolean(this.canUSR1);
        output.writeBoolean(this.canUSR2);
        output.writeBoolean(this.canUSR3);
        output.writeBoolean(this.canBot);
    }
    
    static {
        DEFAULT = new ClientPermissions();
    }
}
