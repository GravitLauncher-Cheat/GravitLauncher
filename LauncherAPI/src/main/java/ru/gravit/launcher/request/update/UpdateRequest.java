// 
// Decompiled by Procyon v0.5.36
// 

package ru.gravit.launcher.request.update;

import java.time.temporal.Temporal;
import java.time.Duration;
import java.security.SignatureException;
import ru.gravit.launcher.Launcher;
import ru.gravit.launcher.serialize.signed.SignedObjectHolder;
import ru.gravit.launcher.serialize.HOutput;
import ru.gravit.launcher.serialize.HInput;
import java.nio.file.attribute.FileAttribute;
import ru.gravit.launcher.request.RequestType;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.io.EOFException;
import ru.gravit.utils.helper.SecurityHelper;
import java.io.InputStream;
import ru.gravit.launcher.hasher.HashedFile;
import java.io.IOException;
import java.nio.file.Files;
import ru.gravit.launcher.LauncherAPI;
import java.util.Objects;
import ru.gravit.utils.helper.IOHelper;
import ru.gravit.launcher.LauncherConfig;
import ru.gravit.launcher.request.websockets.LegacyRequestBridge;
import java.util.Iterator;
import ru.gravit.launcher.hasher.HashedEntry;
import java.util.Map;
import ru.gravit.launcher.request.UpdateAction;
import java.util.Queue;
import java.time.Instant;
import ru.gravit.launcher.hasher.HashedDir;
import ru.gravit.launcher.hasher.FileNameMatcher;
import java.nio.file.Path;
import ru.gravit.launcher.request.websockets.RequestInterface;
import ru.gravit.launcher.events.request.UpdateRequestEvent;
import ru.gravit.launcher.request.Request;

public final class UpdateRequest extends Request<UpdateRequestEvent> implements RequestInterface
{
    private final String dirName;
    private final Path dir;
    private final FileNameMatcher matcher;
    private final boolean digest;
    private volatile State.Callback stateCallback;
    private HashedDir localDir;
    private long totalDownloaded;
    private long totalSize;
    private Instant startTime;
    
    @Override
    public String getType() {
        return "update";
    }
    
    private static void fillActionsQueue(final Queue<UpdateAction> queue, final HashedDir mismatch) {
        for (final Map.Entry<String, HashedEntry> mapEntry : mismatch.map().entrySet()) {
            final String name = mapEntry.getKey();
            final HashedEntry entry = mapEntry.getValue();
            final HashedEntry.Type entryType = entry.getType();
            switch (entryType) {
                case DIR: {
                    queue.add(new UpdateAction(UpdateAction.Type.CD, name, entry));
                    fillActionsQueue(queue, (HashedDir)entry);
                    queue.add(UpdateAction.CD_BACK);
                    continue;
                }
                case FILE: {
                    queue.add(new UpdateAction(UpdateAction.Type.GET, name, entry));
                    continue;
                }
                default: {
                    throw new AssertionError((Object)("Unsupported hashed entry type: " + entryType.name()));
                }
            }
        }
    }
    
    public UpdateRequestEvent requestWebSockets() throws Exception {
        return (UpdateRequestEvent)LegacyRequestBridge.sendRequest(this);
    }
    
    @LauncherAPI
    public UpdateRequest(final LauncherConfig config, final String dirName, final Path dir, final FileNameMatcher matcher, final boolean digest) {
        super(config);
        this.dirName = IOHelper.verifyFileName(dirName);
        this.dir = Objects.requireNonNull(dir, "dir");
        this.matcher = matcher;
        this.digest = digest;
    }
    
    @LauncherAPI
    public UpdateRequest(final String dirName, final Path dir, final FileNameMatcher matcher, final boolean digest) {
        this(null, dirName, dir, matcher, digest);
    }
    
    private void deleteExtraDir(final Path subDir, final HashedDir subHDir, final boolean flag) throws IOException {
        for (final Map.Entry<String, HashedEntry> mapEntry : subHDir.map().entrySet()) {
            final String name = mapEntry.getKey();
            final Path path = subDir.resolve(name);
            final HashedEntry entry = mapEntry.getValue();
            final HashedEntry.Type entryType = entry.getType();
            switch (entryType) {
                case FILE: {
                    this.updateState(IOHelper.toString(path), 0L, 0L);
                    Files.delete(path);
                    continue;
                }
                case DIR: {
                    this.deleteExtraDir(path, (HashedDir)entry, flag || entry.flag);
                    continue;
                }
                default: {
                    throw new AssertionError((Object)("Unsupported hashed entry type: " + entryType.name()));
                }
            }
        }
        if (flag) {
            this.updateState(IOHelper.toString(subDir), 0L, 0L);
            Files.delete(subDir);
        }
    }
    
    private void downloadFile(final Path file, final HashedFile hFile, final InputStream input) throws IOException {
        final String filePath = IOHelper.toString(this.dir.relativize(file));
        this.updateState(filePath, 0L, hFile.size);
        final MessageDigest digest = this.digest ? SecurityHelper.newDigest(SecurityHelper.DigestAlgorithm.MD5) : null;
        try (final OutputStream fileOutput = IOHelper.newOutput(file)) {
            long downloaded = 0L;
            final byte[] bytes = IOHelper.newBuffer();
            while (downloaded < hFile.size) {
                final int remaining = (int)Math.min(hFile.size - downloaded, bytes.length);
                final int length = input.read(bytes, 0, remaining);
                if (length < 0) {
                    throw new EOFException(String.format("%d bytes remaining", hFile.size - downloaded));
                }
                fileOutput.write(bytes, 0, length);
                if (digest != null) {
                    digest.update(bytes, 0, length);
                }
                downloaded += length;
                this.totalDownloaded += length;
                this.updateState(filePath, downloaded, hFile.size);
            }
        }
        if (digest != null) {
            final byte[] digestBytes = digest.digest();
            if (!hFile.isSameDigest(digestBytes)) {
                throw new SecurityException(String.format("File digest mismatch: '%s'", filePath));
            }
        }
    }
    
    @Override
    public Integer getLegacyType() {
        return RequestType.UPDATE.getNumber();
    }
    
    @Override
    public UpdateRequestEvent request() throws Exception {
        Files.createDirectories(this.dir, (FileAttribute<?>[])new FileAttribute[0]);
        this.localDir = new HashedDir(this.dir, this.matcher, false, this.digest);
        return super.request();
    }
    
    @Override
    protected UpdateRequestEvent requestDo(final HInput input, final HOutput output) throws IOException, SignatureException {
        output.writeString(this.dirName, 255);
        output.flush();
        this.readError(input);
        final SignedObjectHolder<HashedDir> remoteHDirHolder = new SignedObjectHolder<HashedDir>(input, this.config.publicKey, HashedDir::new);
        final HashedDir hackHackedDir = remoteHDirHolder.object;
        Launcher.profile.pushOptionalFile(hackHackedDir, !Launcher.profile.isUpdateFastCheck());
        final HashedDir.Diff diff = hackHackedDir.diff(this.localDir, this.matcher);
        this.totalSize = diff.mismatch.size();
        final boolean compress = input.readBoolean();
        return new UpdateRequestEvent(remoteHDirHolder.object);
    }
    
    @LauncherAPI
    public void setStateCallback(final State.Callback callback) {
        this.stateCallback = callback;
    }
    
    private void updateState(final String filePath, final long fileDownloaded, final long fileSize) {
        if (this.stateCallback != null) {
            this.stateCallback.call(new State(filePath, fileDownloaded, fileSize, this.totalDownloaded, this.totalSize, Duration.between(this.startTime, Instant.now())));
        }
    }
    
    public static final class State
    {
        @LauncherAPI
        public final long fileDownloaded;
        @LauncherAPI
        public final long fileSize;
        @LauncherAPI
        public final long totalDownloaded;
        @LauncherAPI
        public final long totalSize;
        @LauncherAPI
        public final String filePath;
        @LauncherAPI
        public final Duration duration;
        
        public State(final String filePath, final long fileDownloaded, final long fileSize, final long totalDownloaded, final long totalSize, final Duration duration) {
            this.filePath = filePath;
            this.fileDownloaded = fileDownloaded;
            this.fileSize = fileSize;
            this.totalDownloaded = totalDownloaded;
            this.totalSize = totalSize;
            this.duration = duration;
        }
        
        @LauncherAPI
        public double getBps() {
            final long seconds = this.duration.getSeconds();
            if (seconds == 0L) {
                return -1.0;
            }
            return this.totalDownloaded / (double)seconds;
        }
        
        @LauncherAPI
        public Duration getEstimatedTime() {
            final double bps = this.getBps();
            if (bps <= 0.0) {
                return null;
            }
            return Duration.ofSeconds((long)(this.getTotalRemaining() / bps));
        }
        
        @LauncherAPI
        public double getFileDownloadedKiB() {
            return this.fileDownloaded / 1024.0;
        }
        
        @LauncherAPI
        public double getFileDownloadedMiB() {
            return this.getFileDownloadedKiB() / 1024.0;
        }
        
        @LauncherAPI
        public double getFileDownloadedPart() {
            if (this.fileSize == 0L) {
                return 0.0;
            }
            return this.fileDownloaded / (double)this.fileSize;
        }
        
        @LauncherAPI
        public long getFileRemaining() {
            return this.fileSize - this.fileDownloaded;
        }
        
        @LauncherAPI
        public double getFileRemainingKiB() {
            return this.getFileRemaining() / 1024.0;
        }
        
        @LauncherAPI
        public double getFileRemainingMiB() {
            return this.getFileRemainingKiB() / 1024.0;
        }
        
        @LauncherAPI
        public double getFileSizeKiB() {
            return this.fileSize / 1024.0;
        }
        
        @LauncherAPI
        public double getFileSizeMiB() {
            return this.getFileSizeKiB() / 1024.0;
        }
        
        @LauncherAPI
        public double getTotalDownloadedKiB() {
            return this.totalDownloaded / 1024.0;
        }
        
        @LauncherAPI
        public double getTotalDownloadedMiB() {
            return this.getTotalDownloadedKiB() / 1024.0;
        }
        
        @LauncherAPI
        public double getTotalDownloadedPart() {
            if (this.totalSize == 0L) {
                return 0.0;
            }
            return this.totalDownloaded / (double)this.totalSize;
        }
        
        @LauncherAPI
        public long getTotalRemaining() {
            return this.totalSize - this.totalDownloaded;
        }
        
        @LauncherAPI
        public double getTotalRemainingKiB() {
            return this.getTotalRemaining() / 1024.0;
        }
        
        @LauncherAPI
        public double getTotalRemainingMiB() {
            return this.getTotalRemainingKiB() / 1024.0;
        }
        
        @LauncherAPI
        public double getTotalSizeKiB() {
            return this.totalSize / 1024.0;
        }
        
        @LauncherAPI
        public double getTotalSizeMiB() {
            return this.getTotalSizeKiB() / 1024.0;
        }
        
        @FunctionalInterface
        public interface Callback
        {
            @LauncherAPI
            void call(final State p0);
        }
    }
}
