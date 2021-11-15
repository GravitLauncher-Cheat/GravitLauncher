package ru.gravit.launcher;

import ru.gravit.utils.helper.LogHelper;
import java.util.UUID;
import java.util.Arrays;
import ru.gravit.utils.helper.SecurityHelper;
import java.nio.file.*;
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
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.FileReader;

public final class Launcher {
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
    public static ClientProfile profile;
    @LauncherAPI
    private static final Pattern UUID_PATTERN;
    public static final Version.Type RELEASE;
    public static GsonBuilder gsonBuilder;
    public static Gson gson;
    private static HInput input;
    private static boolean devMode = false;

    private static void setDevMode() {
        try {
            JSONParser parser = new JSONParser();
            JSONObject data = (JSONObject) parser.parse(new FileReader(IOHelper.getCodeSource(Launcher.class).getParent().resolve("config.json").toString()));
            devMode = (boolean) data.get("devMode");
        } catch (IOException | ParseException ignored) { System.out.println("Ошибка при чтении devMode."); }
    }

    @LauncherAPI
    public static LauncherConfig getConfig() {
        LauncherConfig config = Launcher.CONFIG.get();
        setDevMode();
        if (config == null) {
            try {
                if (!devMode) {
                    Path zipfile = Paths.get(IOHelper.getCodeSource(Launcher.class).getParent().resolve("Launcher-original.jar").toUri());
                    FileSystem fs = FileSystems.newFileSystem(zipfile, null);
                    input = new HInput(IOHelper.newInput(fs.getPath("/config.bin")));
                } else { input = new HInput(IOHelper.newInput(IOHelper.getCodeSource(Launcher.class).getParent().resolve("config.bin"))); }
                config = new LauncherConfig(input);
                System.out.println("PublicKey: "+config.publicKey.getModulus());
            }
            catch (IOException | InvalidKeySpecException e) {
                throw new SecurityException(e);
            }
            Launcher.CONFIG.set(config);
        }
        return config;
    }
    
    @LauncherAPI
    public static void setConfig(final LauncherConfig cfg) {
        Launcher.CONFIG.set(cfg);
    }

    @LauncherAPI
    public static URL getResourceURL(final String name) throws IOException {
        URL url = null;
        setDevMode();
        if(!devMode) {
            Path zipfile = Paths.get(IOHelper.getCodeSource(Launcher.class).getParent().resolve("Launcher-original.jar").toUri());
            FileSystem fs = FileSystems.newFileSystem(zipfile, null);
            url = fs.getPath("runtime/" + name).toUri().toURL();
        } else { url = IOHelper.getCodeSource(Launcher.class).getParent().resolve("runtime/" + name).toUri().toURL(); }
        return url;
    }
    
    public static URL getResourceURL(final String name, final String prefix) throws IOException {
        Path zipfile = Paths.get(IOHelper.getCodeSource(Launcher.class).getParent().resolve("Launcher-original.jar").toUri());
        FileSystem fs = FileSystems.newFileSystem(zipfile, null);
        final URL url = fs.getPath(prefix + '/' + name).toUri().toURL();
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
