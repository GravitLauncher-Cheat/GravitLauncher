package ru.gravit.launcher.guard;

import java.nio.file.Path;
import ru.gravit.launcher.client.ClientLauncher;

public class LauncherGuardManager
{
    public static LauncherGuardInterface guard;
    
    public static void initGuard(final boolean clientInstance) {
        if (ClientLauncher.isUsingWrapper()) {
            LauncherGuardManager.guard = new LauncherWrapperGuard();
        }
        else if (ClientLauncher.isDownloadJava()) {
            LauncherGuardManager.guard = new LauncherJavaGuard();
        }
        else {
            LauncherGuardManager.guard = new LauncherNoGuard();
        }
        LauncherGuardManager.guard.init(clientInstance);
    }
    
    public static Path getGuardJavaBinPath() {
        return LauncherGuardManager.guard.getJavaBinPath();
    }
}
