package ru.gravit.launcher.client;

import javax.crypto.IllegalBlockSizeException;
import javax.crypto.BadPaddingException;
import ru.gravit.utils.helper.SecurityHelper;
import java.util.Iterator;
import ru.gravit.launcher.Launcher;
import ru.gravit.utils.helper.JVMHelper;
import ru.gravit.launcher.serialize.HOutput;
import java.security.SignatureException;
import java.io.IOException;
import ru.gravit.launcher.serialize.HInput;
import ru.gravit.utils.helper.IOHelper;
import ru.gravit.utils.helper.LogHelper;
import java.util.HashMap;
import java.util.LinkedList;
import ru.gravit.launcher.hasher.HashedDir;
import java.util.Map;
import ru.gravit.launcher.profiles.ClientProfile;
import java.util.List;
import ru.gravit.launcher.LauncherAPI;
import java.nio.file.Path;

public class LauncherSettings
{
    public static int settingsMagic;
    @LauncherAPI
    public Path file;
    @LauncherAPI
    public String login;
    @LauncherAPI
    public byte[] rsaPassword;
    @LauncherAPI
    public int profile;
    @LauncherAPI
    public Path updatesDir;
    @LauncherAPI
    public boolean autoEnter;
    @LauncherAPI
    public boolean fullScreen;
    @LauncherAPI
    public boolean offline;
    @LauncherAPI
    public int ram;
    @LauncherAPI
    public byte[] lastDigest;
    @LauncherAPI
    public List<ClientProfile> lastProfiles;
    @LauncherAPI
    public Map<String, HashedDir> lastHDirs;
    
    public LauncherSettings() {
        this.file = DirBridge.dir.resolve("settings.bin");
        this.lastProfiles = new LinkedList<ClientProfile>();
        this.lastHDirs = new HashMap<String, HashedDir>(16);
    }
    
    @LauncherAPI
    public void load() throws SignatureException {
        LogHelper.debug("Loading settings file");
        try (final HInput input = new HInput(IOHelper.newInput(this.file))) {
            this.read(input);
        }
        catch (IOException e) {
            LogHelper.error(e);
            this.setDefault();
        }
    }
    
    @LauncherAPI
    public void save() {
        LogHelper.debug("Save settings file");
        try (final HOutput output = new HOutput(IOHelper.newOutput(this.file))) {
            this.write(output);
        }
        catch (IOException e) {
            LogHelper.error(e);
            this.setDefault();
        }
    }
    
    @LauncherAPI
    public void read(final HInput input) throws IOException, SignatureException {
        final int magic = input.readInt();
        if (magic != LauncherSettings.settingsMagic) {
            this.setDefault();
            LogHelper.warning("Settings magic mismatch: " + Integer.toString(magic, 16));
            return;
        }
        final boolean debug = input.readBoolean();
        if (!LogHelper.isDebugEnabled() && debug) {
            LogHelper.setDebugEnabled(true);
        }
        this.login = (input.readBoolean() ? input.readString(255) : null);
        this.rsaPassword = (byte[])(input.readBoolean() ? input.readByteArray(IOHelper.BUFFER_SIZE) : null);
        this.profile = input.readLength(0);
        this.updatesDir = IOHelper.toPath(input.readString(0));
        DirBridge.dirUpdates = this.updatesDir;
        this.autoEnter = input.readBoolean();
        this.fullScreen = input.readBoolean();
        this.setRAM(input.readLength(JVMHelper.RAM));
        this.lastDigest = (byte[])(input.readBoolean() ? input.readByteArray(0) : null);
        this.lastProfiles.clear();
        for (int lastProfilesCount = input.readLength(0), i = 0; i < lastProfilesCount; ++i) {
            this.lastProfiles.add(Launcher.gson.fromJson(input.readString(0), ClientProfile.class));
        }
        this.lastHDirs.clear();
        for (int lastHDirsCount = input.readLength(0), j = 0; j < lastHDirsCount; ++j) {
            final String name = IOHelper.verifyFileName(input.readString(255));
            final HashedDir hdir = new HashedDir(input);
            this.lastHDirs.put(name, hdir);
        }
    }
    
    @LauncherAPI
    public void write(final HOutput output) throws IOException {
        output.writeInt(LauncherSettings.settingsMagic);
        output.writeBoolean(LogHelper.isDebugEnabled());
        output.writeBoolean(this.login != null);
        if (this.login != null) {
            output.writeString(this.login, 255);
        }
        output.writeBoolean(this.rsaPassword != null);
        if (this.rsaPassword != null) {
            output.writeByteArray(this.rsaPassword, IOHelper.BUFFER_SIZE);
        }
        output.writeLength(this.profile, 0);
        output.writeString(IOHelper.toString(this.updatesDir), 0);
        output.writeBoolean(this.autoEnter);
        output.writeBoolean(this.fullScreen);
        output.writeLength(this.ram, JVMHelper.RAM);
        output.writeBoolean(this.lastDigest != null);
        if (this.lastDigest != null) {
            output.writeByteArray(this.lastDigest, 0);
        }
        output.writeLength(this.lastProfiles.size(), 0);
        for (final ClientProfile profile : this.lastProfiles) {
            output.writeString(Launcher.gson.toJson(profile), 0);
        }
        output.writeLength(this.lastHDirs.size(), 0);
        for (final Map.Entry<String, HashedDir> entry : this.lastHDirs.entrySet()) {
            output.writeString(entry.getKey(), 0);
            entry.getValue().write(output);
        }
    }
    
    @LauncherAPI
    public void setRAM(final int ram) {
        this.ram = Math.min(ram / 256 * 256, JVMHelper.RAM);
    }
    
    @LauncherAPI
    public void setDefault() {
        this.login = null;
        this.rsaPassword = null;
        this.profile = 0;
        this.updatesDir = DirBridge.defaultUpdatesDir;
        this.autoEnter = false;
        this.fullScreen = false;
        this.setRAM(1024);
        this.lastDigest = null;
        this.lastProfiles.clear();
        this.lastHDirs.clear();
    }
    
    @LauncherAPI
    public byte[] setPassword(final String password) throws BadPaddingException, IllegalBlockSizeException {
        final byte[] encrypted = SecurityHelper.newRSAEncryptCipher(Launcher.getConfig().publicKey).doFinal(IOHelper.encode(password));
        return encrypted;
    }
    
    static {
        LauncherSettings.settingsMagic = 789994;
    }
}
