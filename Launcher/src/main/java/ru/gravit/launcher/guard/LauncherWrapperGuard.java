package ru.gravit.launcher.guard;

import ru.gravit.launcher.LauncherConfig;
import java.util.Map;
import java.util.Collection;
import java.util.Collections;
import ru.gravit.launcher.client.ClientLauncherContext;
import java.io.IOException;
import ru.gravit.utils.helper.UnpackHelper;
import ru.gravit.utils.helper.IOHelper;
import java.nio.file.Paths;
import ru.gravit.launcher.client.DirBridge;
import ru.gravit.launcher.Launcher;
import ru.gravit.utils.helper.JVMHelper;
import java.nio.file.Path;

public class LauncherWrapperGuard implements LauncherGuardInterface
{
    public String protectToken;
    
    @Override
    public String getName() {
        return "wrapper";
    }
    
    @Override
    public Path getJavaBinPath() {
        if (JVMHelper.OS_TYPE == JVMHelper.OS.MUSTDIE) {
            final String projectName = Launcher.getConfig().projectname;
            final String wrapperUnpackName = (JVMHelper.JVM_BITS == 64) ? projectName.concat("64.exe") : projectName.concat("32.exe");
            return DirBridge.getGuardDir().resolve(wrapperUnpackName);
        }
        return IOHelper.resolveJavaBin(Paths.get(System.getProperty("java.home"), new String[0]));
    }
    
    @Override
    public int getClientJVMBits() {
        return JVMHelper.JVM_BITS;
    }
    
    @Override
    public void init(final boolean clientInstance) {
        try {
            final String wrapperName = (JVMHelper.JVM_BITS == 64) ? "wrapper64.exe" : "wrapper32.exe";
            final String projectName = Launcher.getConfig().projectname;
            final String wrapperUnpackName = (JVMHelper.JVM_BITS == 64) ? projectName.concat("64.exe") : projectName.concat("32.exe");
            final String antiInjectName = (JVMHelper.JVM_BITS == 64) ? "AntiInject64.dll" : "AntiInject32.dll";
            UnpackHelper.unpack(Launcher.getResourceURL(wrapperName, "guard"), DirBridge.getGuardDir().resolve(wrapperUnpackName));
            UnpackHelper.unpack(Launcher.getResourceURL(antiInjectName, "guard"), DirBridge.getGuardDir().resolve(antiInjectName));
        }
        catch (IOException e) {
            throw new SecurityException(e);
        }
    }
    
    @Override
    public void addCustomParams(final ClientLauncherContext context) {
        Collections.addAll(context.args, new String[] { "-Djava.class.path=".concat(context.pathLauncher) });
    }
    
    @Override
    public void addCustomEnv(final ClientLauncherContext context) {
        final Map<String, String> env = context.builder.environment();
        env.put("JAVA_HOME", System.getProperty("java.home"));
        final LauncherConfig config = Launcher.getConfig();
        env.put("GUARD_USERNAME", context.playerProfile.username);
        env.put("GUARD_PUBLICKEY", config.publicKey.getModulus().toString(16));
        env.put("GUARD_PROJECTNAME", config.projectname);
        if (this.protectToken != null) {
            env.put("GUARD_TOKEN", this.protectToken);
        }
        if (config.guardLicenseName != null) {
            env.put("GUARD_LICENSE_NAME", config.guardLicenseName);
        }
        if (config.guardLicenseKey != null) {
            env.put("GUARD_LICENSE_KEY", config.guardLicenseKey);
        }
    }
    
    @Override
    public void setProtectToken(final String token) {
        this.protectToken = token;
    }
}
