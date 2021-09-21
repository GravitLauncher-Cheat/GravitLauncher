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
import java.io.IOException;
import org.json.simple.parser.ParseException;
import java.io.FileReader;
import java.lang.Boolean;

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
    public static ClientProfile profile;
    @LauncherAPI
    private static final Pattern UUID_PATTERN;
    public static final Version.Type RELEASE;
    public static GsonBuilder gsonBuilder;
    public static Gson gson;
    private static HInput input;
    
    @LauncherAPI
    public static LauncherConfig getConfig() {
        //HInput input;
        boolean devMode = false;
        LauncherConfig config = Launcher.CONFIG.get();
        if (config == null) {
            try {
                if (devMode == false)
                {
                    Path zipfile = Paths.get(IOHelper.getCodeSource(Launcher.class).getParent().resolve("Launcher-original.jar").toUri());
                    FileSystem fs = FileSystems.newFileSystem(zipfile, null);
                    input = new HInput(IOHelper.newInput(fs.getPath("/config.bin")));
                }
                if (devMode == true)
                {
                    input = new HInput(IOHelper.newInput(IOHelper.getCodeSource(Launcher.class).getParent().resolve("config.bin")));
                }
            }
            //InvalidKeySpecException
            catch (IOException ex2) {
                final Exception e = ex2;
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
        boolean devMode = false;
        try {
            JSONParser parser = new JSONParser();
            JSONObject data = (JSONObject) parser.parse(new FileReader(IOHelper.getCodeSource(Launcher.class).getParent().resolve("config.json").toString()));
            devMode = (boolean) data.get("devMode");
        }
        catch (IOException | ParseException e)
        {
            System.out.println("Ошибка при чтении devMode.");
        }
        URL url = null;
        final LauncherConfig config = getConfig();
        final byte[] validDigest = config.runtime.get(name);
        //if (validDigest == null) {
            //throw new NoSuchFileException(name);
        //}
        if(devMode == false)
        {
            Path zipfile = Paths.get(IOHelper.getCodeSource(Launcher.class).getParent().resolve("Launcher-original.jar").toUri());
            FileSystem fs = FileSystems.newFileSystem(zipfile, null);
            url = fs.getPath("runtime/" + name).toUri().toURL();
        }
        if(devMode == true)
        {
            url = IOHelper.getCodeSource(Launcher.class).getParent().resolve("runtime/" + name).toUri().toURL();
        }
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
