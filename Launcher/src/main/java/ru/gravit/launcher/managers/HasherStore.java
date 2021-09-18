package ru.gravit.launcher.managers;

import java.util.Collection;
import java.io.IOException;
import ru.gravit.utils.helper.LogHelper;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import ru.gravit.utils.helper.IOHelper;
import ru.gravit.launcher.hasher.HashedEntry;
import java.util.Deque;
import java.util.LinkedList;
import ru.gravit.launcher.hasher.FileNameMatcher;
import ru.gravit.launcher.LauncherAPI;
import ru.gravit.launcher.hasher.HashedDir;
import java.nio.file.Path;
import ru.gravit.launcher.profiles.ClientProfile;
import java.util.Map;

public class HasherStore
{
    public Map<String, HasherStoreEnity> store;
    
    @LauncherAPI
    public void addProfileUpdateDir(final ClientProfile profile, final Path dir, final HashedDir hdir) {
        final HasherStoreEnity e = new HasherStoreEnity();
        e.hdir = hdir;
        e.dir = dir;
        e.shared = profile.getShared();
        this.store.put(profile.getTitle(), e);
    }
    
    @LauncherAPI
    public void copyCompareFilesTo(final String name, final Path targetDir, final HashedDir targetHDir, final String[] shared) {
        this.store.forEach((key, e) -> {
            if (!key.equals(name)) {
                FileNameMatcher nm = new FileNameMatcher(shared, null, null);
                HashedDir compare = targetHDir.sideCompare(e.hdir, nm, new LinkedList<String>(), true);
                compare.map().forEach((arg1, arg2) -> this.recurseCopy(arg1, arg2, name, targetDir, e.dir));
            }
        });
    }
    
    @LauncherAPI
    public void recurseCopy(final String filename, final HashedEntry entry, final String name, final Path targetDir, final Path sourceDir) {
        if (!IOHelper.isDir(targetDir)) {
            try {
                Files.createDirectories(targetDir, (FileAttribute<?>[])new FileAttribute[0]);
            }
            catch (IOException e1) {
                LogHelper.error(e1);
            }
        }
        if (entry.getType().equals(HashedEntry.Type.DIR)) {
            ((HashedDir)entry).map().forEach((arg1, arg2) -> this.recurseCopy(arg1, arg2, name, targetDir.resolve(filename), sourceDir.resolve(filename)));
        }
        else if (entry.getType().equals(HashedEntry.Type.FILE)) {
            try {
                IOHelper.copy(sourceDir.resolve(filename), targetDir.resolve(filename));
            }
            catch (IOException e2) {
                LogHelper.error(e2);
            }
        }
    }
    
    public class HasherStoreEnity
    {
        @LauncherAPI
        public HashedDir hdir;
        @LauncherAPI
        public Path dir;
        @LauncherAPI
        public Collection<String> shared;
    }
}
