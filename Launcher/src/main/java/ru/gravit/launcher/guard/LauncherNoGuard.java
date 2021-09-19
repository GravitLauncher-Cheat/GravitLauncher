package ru.gravit.launcher.guard;

import java.util.Collection;
import java.util.Collections;
import ru.gravit.launcher.client.ClientLauncherContext;
import ru.gravit.utils.helper.LogHelper;
import ru.gravit.utils.helper.JVMHelper;
import ru.gravit.utils.helper.IOHelper;
import java.nio.file.Paths;
import java.nio.file.Path;

public class LauncherNoGuard implements LauncherGuardInterface
{
    @Override
    public String getName() {
        return "noGuard";
    }
    
    @Override
    public Path getJavaBinPath() {
        return IOHelper.resolveJavaBin(Paths.get(System.getProperty("java.home"), new String[0]));
    }
    
    @Override
    public int getClientJVMBits() {
        return JVMHelper.JVM_BITS;
    }
    
    @Override
    public void init(final boolean clientInstance) {
        LogHelper.warning("Using noGuard interface");
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
