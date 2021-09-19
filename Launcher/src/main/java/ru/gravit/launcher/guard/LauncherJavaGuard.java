package ru.gravit.launcher.guard;

import java.util.Collection;
import java.util.Collections;
import ru.gravit.launcher.client.ClientLauncherContext;
import java.nio.file.Paths;
import ru.gravit.utils.helper.IOHelper;
import ru.gravit.launcher.client.ClientLauncher;
import ru.gravit.utils.helper.JVMHelper;
import java.nio.file.Path;

public class LauncherJavaGuard implements LauncherGuardInterface
{
    @Override
    public String getName() {
        return "java";
    }
    
    @Override
    public Path getJavaBinPath() {
        if (JVMHelper.OS_TYPE == JVMHelper.OS.MUSTDIE) {
            return IOHelper.resolveJavaBin(ClientLauncher.getJavaBinPath());
        }
        return IOHelper.resolveJavaBin(Paths.get(System.getProperty("java.home"), new String[0]));
    }
    
    @Override
    public int getClientJVMBits() {
        return JVMHelper.OS_BITS;
    }
    
    @Override
    public void init(final boolean clientInstance) {
    }
    
    @Override
    public void addCustomParams(final ClientLauncherContext context) {
        Collections.addAll(context.args, new String[] { "-cp" });
        Collections.addAll(context.args, new String[] { context.pathLauncher });
    }
    
    @Override
    public void addCustomEnv(final ClientLauncherContext context) {
    }
    
    @Override
    public void setProtectToken(final String token) {
    }
}
