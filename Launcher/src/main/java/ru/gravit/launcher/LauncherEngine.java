package ru.gravit.launcher;

import ru.gravit.launcher.client.DirBridge;
import ru.gravit.utils.helper.CommonHelper;
import ru.gravit.launcher.client.FunctionalBridge;
import java.util.Objects;
import ru.gravit.launcher.guard.LauncherGuardManager;
import ru.gravit.launcher.gui.JSRuntimeProvider;
import ru.gravit.launcher.client.ClientModuleManager;
import com.google.gson.GsonBuilder;
import ru.gravit.utils.helper.LogHelper;
import ru.gravit.launcher.gui.RuntimeProvider;
import java.util.concurrent.atomic.AtomicBoolean;

public class LauncherEngine
{
    private final AtomicBoolean started;
    public RuntimeProvider runtimeProvider;
    
    public static void main(final String... args) throws Throwable {
        LogHelper.printVersion("Launcher");
        LogHelper.printLicense("Launcher");
        initGson();
        LogHelper.setStacktraceEnabled(true);
        final long startTime = System.currentTimeMillis();
        try {
            new LauncherEngine().start(args);
        }
        catch (Exception e) {
            LogHelper.error(e);
            return;
        }
        final long endTime = System.currentTimeMillis();
        LogHelper.debug("Launcher started in %dms", endTime - startTime);
    }
    
    public static void initGson() {
        if (Launcher.gson != null) {
            return;
        }
        Launcher.gsonBuilder = new GsonBuilder();
        Launcher.gson = Launcher.gsonBuilder.create();
    }
    
    private LauncherEngine() {
        this.started = new AtomicBoolean(false);
    }
    
    @LauncherAPI
    public void start(final String... args) throws Throwable {
        Launcher.modulesManager = new ClientModuleManager(this);
        LauncherConfig.getAutogenConfig().initModules();
        Launcher.modulesManager.preInitModules();
        if (this.runtimeProvider == null) {
            this.runtimeProvider = new JSRuntimeProvider();
        }
        this.runtimeProvider.init(false);
        this.runtimeProvider.preLoad();
        LauncherGuardManager.initGuard(false);
        Objects.requireNonNull(args, "args");
        if (this.started.getAndSet(true)) {
            throw new IllegalStateException("Launcher has been already started");
        }
        Launcher.modulesManager.initModules();
        this.runtimeProvider.preLoad();
        (FunctionalBridge.getHWID = CommonHelper.newThread("GetHWID Thread", true, FunctionalBridge::getHWID)).start();
        LogHelper.debug("Dir: %s", DirBridge.dir);
        this.runtimeProvider.run(args);
    }
    
    public static LauncherEngine clientInstance() {
        return new LauncherEngine();
    }
}
