package ru.gravit.launcher.guard;

import ru.gravit.launcher.client.ClientLauncherContext;
import java.nio.file.Path;

public interface LauncherGuardInterface
{
    String getName();
    
    Path getJavaBinPath();
    
    int getClientJVMBits();
    
    void init(final boolean p0);
    
    void addCustomParams(final ClientLauncherContext p0);
    
    void addCustomEnv(final ClientLauncherContext p0);
    
    void setProtectToken(final String p0);
}
