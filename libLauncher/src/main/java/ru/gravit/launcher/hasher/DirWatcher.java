// 
// Decompiled by Procyon v0.5.36
// 

package ru.gravit.launcher.hasher;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.FileVisitResult;
import java.nio.file.SimpleFileVisitor;
import ru.gravit.utils.helper.JVMHelper;
import java.nio.file.ClosedWatchServiceException;
import java.util.Collection;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.io.IOException;
import ru.gravit.launcher.LauncherAPI;
import java.nio.file.FileVisitor;
import ru.gravit.utils.helper.IOHelper;
import java.util.Objects;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Deque;
import ru.gravit.utils.NativeJVMHalt;
import net.minecraftforge.fml.SafeExitJVM;
import cpw.mods.fml.SafeExitJVMLegacy;
import ru.gravit.utils.helper.LogHelper;
import java.nio.file.WatchService;
import java.nio.file.Path;
import java.nio.file.WatchEvent;

public final class DirWatcher implements Runnable, AutoCloseable
{
    public static final boolean FILE_TREE_SUPPORTED;
    private static final WatchEvent.Kind<?>[] KINDS;
    private final Path dir;
    private final HashedDir hdir;
    private final FileNameMatcher matcher;
    private final WatchService service;
    private final boolean digest;
    
    private static void handleError(final Throwable e) {
        LogHelper.error(e);
        try {
            SafeExitJVMLegacy.exit(-123);
        }
        catch (Throwable t) {}
        try {
            SafeExitJVM.exit(-123);
        }
        catch (Throwable t2) {}
        final NativeJVMHalt halt = new NativeJVMHalt(-123);
        halt.halt();
    }
    
    private static Deque<String> toPath(final Iterable<Path> path) {
        final Deque<String> result = new LinkedList<String>();
        for (final Path pe : path) {
            result.add(pe.toString());
        }
        return result;
    }
    
    @LauncherAPI
    public DirWatcher(final Path dir, final HashedDir hdir, final FileNameMatcher matcher, final boolean digest) throws IOException {
        this.dir = Objects.requireNonNull(dir, "dir");
        this.hdir = Objects.requireNonNull(hdir, "hdir");
        this.matcher = matcher;
        this.digest = digest;
        this.service = dir.getFileSystem().newWatchService();
        IOHelper.walk(dir, new RegisterFileVisitor(), true);
        LogHelper.subInfo("DirWatcher %s", dir.toString());
    }
    
    @LauncherAPI
    @Override
    public void close() throws IOException {
        this.service.close();
    }
    
    private void processKey(final WatchKey key) throws IOException {
        final Path watchDir = (Path)key.watchable();
        for (final WatchEvent<?> event : key.pollEvents()) {
            final WatchEvent.Kind<?> kind = event.kind();
            if (kind.equals(StandardWatchEventKinds.OVERFLOW)) {
                if (Boolean.getBoolean("launcher.dirwatcher.ignoreOverflows")) {
                    continue;
                }
                throw new IOException("Overflow");
            }
            else {
                final Path path = watchDir.resolve((Path)event.context());
                final Deque<String> stringPath = toPath(this.dir.relativize(path));
                if (this.matcher != null && !this.matcher.shouldVerify(stringPath)) {
                    continue;
                }
                if (!kind.equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
                    continue;
                }
                final HashedEntry entry = this.hdir.resolve(stringPath);
                if (entry == null) {
                    continue;
                }
                if (entry.getType() != HashedEntry.Type.FILE) {
                    continue;
                }
                if (((HashedFile)entry).isSame(path, this.digest)) {
                    continue;
                }
                continue;
            }
        }
        key.reset();
    }
    
    private void processLoop() throws IOException, InterruptedException {
        LogHelper.debug("WatchService start processing");
        while (!Thread.interrupted()) {
            this.processKey(this.service.take());
        }
        LogHelper.debug("WatchService closed");
    }
    
    @LauncherAPI
    @Override
    public void run() {
        try {
            this.processLoop();
        }
        catch (InterruptedException | ClosedWatchServiceException ex2) {
            final Exception ex;
            final Exception ignored = ex2;
            LogHelper.debug("WatchService closed 2");
        }
        catch (Throwable exc) {
            handleError(exc);
        }
    }
    
    static {
        FILE_TREE_SUPPORTED = (JVMHelper.OS_TYPE == JVMHelper.OS.MUSTDIE);
        KINDS = new WatchEvent.Kind[] { StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE };
    }
    
    private final class RegisterFileVisitor extends SimpleFileVisitor<Path>
    {
        private final Deque<String> path;
        
        private RegisterFileVisitor() {
            this.path = new LinkedList<String>();
        }
        
        @Override
        public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
            final FileVisitResult result = super.postVisitDirectory(dir, exc);
            if (!DirWatcher.this.dir.equals(dir)) {
                this.path.removeLast();
            }
            return result;
        }
        
        @Override
        public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
            final FileVisitResult result = super.preVisitDirectory(dir, attrs);
            if (DirWatcher.this.dir.equals(dir)) {
                dir.register(DirWatcher.this.service, (WatchEvent.Kind<?>[])DirWatcher.KINDS);
                return result;
            }
            this.path.add(IOHelper.getFileName(dir));
            dir.register(DirWatcher.this.service, (WatchEvent.Kind<?>[])DirWatcher.KINDS);
            return result;
        }
    }
}
