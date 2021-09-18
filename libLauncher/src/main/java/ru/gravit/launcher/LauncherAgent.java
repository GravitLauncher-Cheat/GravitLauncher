// 
// Decompiled by Procyon v0.5.36
// 

package ru.gravit.launcher;

import java.io.IOException;
import java.util.jar.JarFile;
import ru.gravit.utils.helper.LogHelper;
import java.lang.instrument.Instrumentation;

@LauncherAPI
public final class LauncherAgent
{
    private static boolean isAgentStarted;
    public static Instrumentation inst;
    
    public static void addJVMClassPath(final String path) throws IOException {
        LogHelper.debug("Launcher Agent addJVMClassPath");
        LauncherAgent.inst.appendToSystemClassLoaderSearch(new JarFile(path));
    }
    
    public boolean isAgentStarted() {
        return LauncherAgent.isAgentStarted;
    }
    
    public static void premain(final String agentArgument, final Instrumentation instrumentation) {
        System.out.println("Launcher Agent");
        LauncherAgent.inst = instrumentation;
        LauncherAgent.isAgentStarted = true;
    }
    
    public static boolean isStarted() {
        return LauncherAgent.isAgentStarted;
    }
    
    static {
        LauncherAgent.isAgentStarted = false;
    }
}
