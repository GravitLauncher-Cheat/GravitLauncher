package ru.gravit.launcher.client;

import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import ru.gravit.utils.helper.JVMHelper;
import java.nio.file.Paths;
import java.io.IOException;
import ru.gravit.utils.helper.IOHelper;
import ru.gravit.launcher.LauncherAPI;
import java.nio.file.Path;

public class DirBridge
{
    public static final String USE_CUSTOMDIR_PROPERTY = "launcher.usecustomdir";
    public static final String CUSTOMDIR_PROPERTY = "launcher.customdir";
    public static final String USE_OPTDIR_PROPERTY = "launcher.useoptdir";
    @LauncherAPI
    public static Path dir;
    @LauncherAPI
    public static Path dirUpdates;
    @LauncherAPI
    public static Path defaultUpdatesDir;
    @LauncherAPI
    public static boolean useLegacyDir;
    
    @LauncherAPI
    public static void move(final Path newDir) throws IOException {
        IOHelper.move(DirBridge.dirUpdates, newDir);
        DirBridge.dirUpdates = newDir;
    }
    
    @LauncherAPI
    public static Path getAppDataDir() throws IOException {
        final boolean isCustomDir = Boolean.getBoolean(System.getProperty("launcher.usecustomdir", "false"));
        if (isCustomDir) {
            return Paths.get(System.getProperty("launcher.customdir"), new String[0]);
        }
        if (JVMHelper.OS_TYPE == JVMHelper.OS.LINUX) {
            final boolean isOpt = Boolean.getBoolean(System.getProperty("launcher.useoptdir", "false"));
            if (isOpt) {
                final Path opt = Paths.get("/", new String[0]).resolve("opt");
                if (!IOHelper.isDir(opt)) {
                    Files.createDirectories(opt, (FileAttribute<?>[])new FileAttribute[0]);
                }
                return opt;
            }
            final Path local = IOHelper.HOME_DIR.resolve(".minecraftlauncher");
            if (!IOHelper.isDir(local)) {
                Files.createDirectories(local, (FileAttribute<?>[])new FileAttribute[0]);
            }
            return local;
        }
        else {
            if (JVMHelper.OS_TYPE == JVMHelper.OS.MUSTDIE) {
                final Path appdata = IOHelper.HOME_DIR.resolve("AppData").resolve("Roaming");
                if (!IOHelper.isDir(appdata)) {
                    Files.createDirectories(appdata, (FileAttribute<?>[])new FileAttribute[0]);
                }
                return appdata;
            }
            if (JVMHelper.OS_TYPE == JVMHelper.OS.MACOSX) {
                final Path minecraft = IOHelper.HOME_DIR.resolve("minecraft");
                if (!IOHelper.isDir(minecraft)) {
                    Files.createDirectories(minecraft, (FileAttribute<?>[])new FileAttribute[0]);
                }
                return minecraft;
            }
            return IOHelper.HOME_DIR;
        }
    }
    
    @LauncherAPI
    public static Path getLauncherDir(final String projectname) throws IOException {
        return getAppDataDir().resolve(projectname);
    }
    
    @LauncherAPI
    public static Path getGuardDir() {
        return DirBridge.dir.resolve("guard");
    }
    
    @LauncherAPI
    public static Path getLegacyLauncherDir(final String projectname) {
        return IOHelper.HOME_DIR.resolve(projectname);
    }
    
    @LauncherAPI
    public static void setUseLegacyDir(final boolean b) {
        DirBridge.useLegacyDir = b;
    }
}
