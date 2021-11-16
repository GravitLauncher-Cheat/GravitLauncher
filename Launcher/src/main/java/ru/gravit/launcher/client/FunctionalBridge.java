package ru.gravit.launcher.client;

import java.util.concurrent.Executors;
import ru.gravit.launcher.events.request.AuthRequestEvent;
import ru.gravit.launcher.managers.HasherManager;
import ru.gravit.launcher.managers.HasherStore;
import javafx.concurrent.Task;
import ru.gravit.launcher.request.websockets.RequestInterface;
import java.io.IOException;
import ru.gravit.launcher.request.update.LegacyLauncherRequest;
import ru.gravit.launcher.request.Request;
import ru.gravit.launcher.hasher.FileNameMatcher;
import ru.gravit.launcher.hasher.HashedDir;
import ru.gravit.launcher.serialize.signed.SignedObjectHolder;
import java.nio.file.Path;
import ru.gravit.launcher.HWID;
import java.util.concurrent.atomic.AtomicReference;
import ru.gravit.launcher.hwid.OshiHWIDProvider;
import java.util.concurrent.ExecutorService;
import ru.gravit.launcher.LauncherAPI;
import ru.gravit.utils.helper.JVMHelper;

public class FunctionalBridge
{
    @LauncherAPI
    public static LauncherSettings settings;
    @LauncherAPI
    public static ExecutorService worker;
    @LauncherAPI
    public static OshiHWIDProvider hwidProvider;
    @LauncherAPI
    public static AtomicReference<HWID> hwid;
    @LauncherAPI
    public static Thread getHWID;
    
    @LauncherAPI
    public static HashedDirRunnable offlineUpdateRequest(final String dirName, final Path dir, final SignedObjectHolder<HashedDir> hdir, final FileNameMatcher matcher, final boolean digest) {
        return () -> {
            if (hdir == null) {
                Request.requestError(String.format("\u0414\u0438\u0440\u0435\u043a\u0442\u043e\u0440\u0438\u0438 '%s' \u043d\u0435\u0442 \u0432 \u043a\u044d\u0448\u0435", dirName));
            }
            ClientLauncher.verifyHDir(dir, hdir.object, matcher, digest);
            return hdir;
        };
    }
    
    @LauncherAPI
    public static LegacyLauncherRequest.Result offlineLauncherRequest() throws IOException {
        if (FunctionalBridge.settings.lastDigest == null || FunctionalBridge.settings.lastProfiles.isEmpty()) {
            Request.requestError("\u0417\u0430\u043f\u0443\u0441\u043a \u0432 \u043e\u0444\u0444\u043b\u0430\u0439\u043d-\u0440\u0435\u0436\u0438\u043c\u0435 \u043d\u0435\u0432\u043e\u0437\u043c\u043e\u0436\u0435\u043d");
        }
        return new LegacyLauncherRequest.Result(null, FunctionalBridge.settings.lastDigest, FunctionalBridge.settings.lastProfiles);
    }
    
    @LauncherAPI
    public static void makeJsonRequest(final RequestInterface request, final Runnable callback) {
    }
    
    @LauncherAPI
    public static void startTask(final Task task) {
        FunctionalBridge.worker.execute((Runnable)task);
    }
    
    @LauncherAPI
    public static HWID getHWID() {
        final HWID hhwid = FunctionalBridge.hwid.get();
        if (hhwid == null) {
            FunctionalBridge.hwid.set(FunctionalBridge.hwidProvider.getHWID());
        }
        return hhwid;
    }
    
    @LauncherAPI
    public static long getTotalMemory() {
        return FunctionalBridge.hwidProvider.getTotalMemory() >> 20;
    }
    
    @LauncherAPI
    public static int getClientJVMBits() {
        return JVMHelper.JVM_BITS;
    }
    
    @LauncherAPI
    public static long getJVMTotalMemory() {
        if (getClientJVMBits() == 32) {
            return Math.min(getTotalMemory(), 1536L);
        }
        return getTotalMemory();
    }
    
    @LauncherAPI
    public static HasherStore getDefaultHasherStore() {
        return HasherManager.getDefaultStore();
    }
    
    @LauncherAPI
    public static void setAuthParams(final AuthRequestEvent event) {

    }
    
    static {
        FunctionalBridge.worker = Executors.newWorkStealingPool();
        FunctionalBridge.hwidProvider = new OshiHWIDProvider();
        FunctionalBridge.hwid = new AtomicReference<HWID>();
        FunctionalBridge.getHWID = null;
    }
    
    @FunctionalInterface
    public interface HashedDirRunnable
    {
        SignedObjectHolder<HashedDir> run() throws Exception;
    }
}
