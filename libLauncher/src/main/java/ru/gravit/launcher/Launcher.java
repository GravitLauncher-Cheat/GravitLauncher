package ru.gravit.launcher;

import ru.gravit.utils.helper.LogHelper;
import java.util.UUID;
import java.util.Arrays;
import ru.gravit.utils.helper.SecurityHelper;
import java.nio.file.NoSuchFileException;
import java.net.URL;
import java.security.spec.InvalidKeySpecException;
import java.io.IOException;
import ru.gravit.launcher.serialize.HInput;
import ru.gravit.utils.helper.IOHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.gravit.utils.Version;
import java.util.regex.Pattern;
import ru.gravit.launcher.profiles.ClientProfile;
import ru.gravit.launcher.modules.ModulesManager;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicBoolean;

public final class Launcher
{
    @LauncherAPI
    public static final String SKIN_URL_PROPERTY = "skinURL";
    @LauncherAPI
    public static final String SKIN_DIGEST_PROPERTY = "skinDigest";
    @LauncherAPI
    public static final String CLOAK_URL_PROPERTY = "cloakURL";
    @LauncherAPI
    public static final String CLOAK_DIGEST_PROPERTY = "cloakDigest";
    public static final AtomicBoolean LAUNCHED;
    private static final AtomicReference<LauncherConfig> CONFIG;
    @LauncherAPI
    public static ModulesManager modulesManager;
    @LauncherAPI
    public static final int PROTOCOL_MAGIC_LEGACY = 1917264920;
    @LauncherAPI
    public static final int PROTOCOL_MAGIC = -1576685468;
    @LauncherAPI
    public static final String RUNTIME_DIR = "runtime";
    @LauncherAPI
    public static final String GUARD_DIR = "guard";
    @LauncherAPI
    public static final String CONFIG_FILE = "config.bin";
    @LauncherAPI
    public static ClientProfile profile;
    @LauncherAPI
    public static final String INIT_SCRIPT_FILE = "init.js";
    @LauncherAPI
    public static final String API_SCRIPT_FILE = "engine/api.js";
    public static final String CONFIG_SCRIPT_FILE = "config.js";
    private static final Pattern UUID_PATTERN;
    public static final int MAJOR = 4;
    public static final int MINOR = 5;
    public static final int PATCH = 0;
    public static final int BUILD = 1;
    public static final Version.Type RELEASE;
    public static GsonBuilder gsonBuilder;
    public static Gson gson;
    
    @LauncherAPI
    public static LauncherConfig getConfig() {
        System.out.println("============================================================================");
        System.out.println("LauncherConfig getConfig");
        LauncherConfig config = Launcher.CONFIG.get();
        if (config == null) {
            try (final HInput input = new HInput(IOHelper.newInput(IOHelper.getResourceURL("config.bin")))) {
                config = new LauncherConfig(input);
            }
            catch (IOException | InvalidKeySpecException ex2) {
                //final Exception ex;
                final Exception e = ex2;
                throw new SecurityException(e);
            }
            Launcher.CONFIG.set(config);
        }
        System.out.println("config.publicKey: " + config.publicKey.toString());
        System.out.println("----------------------------------------------------------------------------");
        return config;
    }
    
    @LauncherAPI
    public static void setConfig(final LauncherConfig cfg) {
        Launcher.CONFIG.set(cfg);
    }
    
    @LauncherAPI
    public static URL getResourceURL(final String name) throws IOException {
        final LauncherConfig config = getConfig();
        final byte[] validDigest = config.runtime.get(name);
        if (validDigest == null) {
            throw new NoSuchFileException(name);
        }
        final URL url = IOHelper.getResourceURL("runtime/" + name);
        return url;
    }
    
    public static URL getResourceURL(final String name, final String prefix) throws IOException {
        final LauncherConfig config = getConfig();
        final byte[] validDigest = config.runtime.get(name);
        if (validDigest == null) {
            throw new NoSuchFileException(name);
        }
        final URL url = IOHelper.getResourceURL(prefix + '/' + name);
        if (!Arrays.equals(validDigest, SecurityHelper.digest(SecurityHelper.DigestAlgorithm.MD5, url))) {
            throw new NoSuchFileException(name);
        }
        return url;
    }
    
    @LauncherAPI
    public static String toHash(final UUID uuid) {
        return Launcher.UUID_PATTERN.matcher(uuid.toString()).replaceAll("");
    }
    
    public static Version getVersion() {
        return new Version(4, 5, 0, 1, Launcher.RELEASE);
    }
    
    public static void applyLauncherEnv(final LauncherConfig.LauncherEnvironment env) {
        switch (env) {
            case DEV: {
                LogHelper.setDevEnabled(true);
                LogHelper.setStacktraceEnabled(true);
                LogHelper.setDebugEnabled(true);
                break;
            }
            case DEBUG: {
                LogHelper.setDebugEnabled(true);
                LogHelper.setStacktraceEnabled(true);
            }
            case PROD: {
                LogHelper.setStacktraceEnabled(false);
                LogHelper.setDebugEnabled(false);
                LogHelper.setDevEnabled(false);
                break;
            }
        }
    }
    
    static {
        LAUNCHED = new AtomicBoolean(false);
        CONFIG = new AtomicReference<LauncherConfig>();
        Launcher.modulesManager = null;
        UUID_PATTERN = Pattern.compile("-", 16);
        RELEASE = Version.Type.STABLE;
    }
}
