package ru.gravit.launcher.hasher;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.FileVisitResult;
import java.nio.file.SimpleFileVisitor;
import java.util.Set;
import ru.gravit.launcher.serialize.stream.EnumSerializer;
import ru.gravit.launcher.serialize.HOutput;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import ru.gravit.utils.helper.LogHelper;
import java.util.StringTokenizer;
import java.util.Deque;
import java.util.LinkedList;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.io.IOException;
import ru.gravit.utils.helper.VerifyHelper;
import ru.gravit.utils.helper.IOHelper;
import ru.gravit.launcher.serialize.HInput;
import ru.gravit.launcher.LauncherAPI;
import java.util.HashMap;
import java.util.Map;

public final class HashedDir extends HashedEntry
{
    private final Map<String, HashedEntry> map;
    
    @LauncherAPI
    public HashedDir() {
        this.map = new HashMap<String, HashedEntry>(32);
    }
    
    @LauncherAPI
    public HashedDir(final HInput input) throws IOException {
        this.map = new HashMap<String, HashedEntry>(32);
        for (int entriesCount = input.readLength(0), i = 0; i < entriesCount; ++i) {
            final String name = IOHelper.verifyFileName(input.readString(255));
            final Type type = Type.read(input);
            HashedEntry entry = null;
            switch (type) {
                case FILE: {
                    entry = new HashedFile(input);
                    break;
                }
                case DIR: {
                    entry = new HashedDir(input);
                    break;
                }
                default: {
                    throw new AssertionError((Object)("Unsupported hashed entry type: " + type.name()));
                }
            }
            VerifyHelper.putIfAbsent(this.map, name, entry, String.format("Duplicate dir entry: '%s'", name));
        }
    }
    
    @LauncherAPI
    public HashedDir(final Path dir, final FileNameMatcher matcher, final boolean allowSymlinks, final boolean digest) throws IOException {
        this.map = new HashMap<String, HashedEntry>(32);
        IOHelper.walk(dir, new HashFileVisitor(dir, matcher, allowSymlinks, digest), true);
    }
    
    @LauncherAPI
    public Diff diff(final HashedDir other, final FileNameMatcher matcher) {
        final HashedDir mismatch = this.sideDiff(other, matcher, new LinkedList<String>(), true);
        final HashedDir extra = other.sideDiff(this, matcher, new LinkedList<String>(), false);
        return new Diff(mismatch, extra);
    }
    
    @LauncherAPI
    public Diff compare(final HashedDir other, final FileNameMatcher matcher) {
        final HashedDir mismatch = this.sideDiff(other, matcher, new LinkedList<String>(), true);
        final HashedDir extra = other.sideDiff(this, matcher, new LinkedList<String>(), false);
        return new Diff(mismatch, extra);
    }
    
    public void remove(final String name) {
        this.map.remove(name);
    }
    
    public void removeR(final String name) {
        final LinkedList<String> dirs = new LinkedList<String>();
        final StringTokenizer t = new StringTokenizer(name, "/");
        while (t.hasMoreTokens()) {
            dirs.add(t.nextToken());
        }
        Map<String, HashedEntry> current = this.map;
        for (final String s : dirs) {
            final HashedEntry e = current.get(s);
            if (e == null) {
                LogHelper.debug("Null %s", s);
                for (final String x : current.keySet()) {
                    LogHelper.debug("Contains %s", x);
                }
                break;
            }
            if (e.getType() != Type.DIR) {
                current.remove(s);
                LogHelper.debug("Found filename %s", s);
                break;
            }
            current = ((HashedDir)e).map;
            LogHelper.debug("Found dir %s", s);
        }
    }
    
    @LauncherAPI
    public HashedEntry getEntry(final String name) {
        return this.map.get(name);
    }
    
    @Override
    public Type getType() {
        return Type.DIR;
    }
    
    @LauncherAPI
    public boolean isEmpty() {
        return this.map.isEmpty();
    }
    
    @LauncherAPI
    public Map<String, HashedEntry> map() {
        return Collections.unmodifiableMap((Map<? extends String, ? extends HashedEntry>)this.map);
    }
    
    @LauncherAPI
    public HashedEntry resolve(final Iterable<String> path) {
        HashedEntry current = this;
        for (final String pathEntry : path) {
            if (!(current instanceof HashedDir)) {
                return null;
            }
            current = ((HashedDir)current).map.get(pathEntry);
        }
        return current;
    }
    
    private HashedDir sideDiff(final HashedDir other, final FileNameMatcher matcher, final Deque<String> path, final boolean mismatchList) {
        final HashedDir diff = new HashedDir();
        for (final Map.Entry<String, HashedEntry> mapEntry : this.map.entrySet()) {
            final String name = mapEntry.getKey();
            final HashedEntry entry = mapEntry.getValue();
            path.add(name);
            final boolean shouldUpdate = matcher == null || matcher.shouldUpdate(path);
            final Type type = entry.getType();
            final HashedEntry otherEntry = other.map.get(name);
            if (otherEntry == null || otherEntry.getType() != type) {
                if (shouldUpdate || (mismatchList && otherEntry == null)) {
                    diff.map.put(name, entry);
                    if (!mismatchList) {
                        entry.flag = true;
                    }
                }
                path.removeLast();
            }
            else {
                switch (type) {
                    case FILE: {
                        final HashedFile file = (HashedFile)entry;
                        final HashedFile otherFile = (HashedFile)otherEntry;
                        if (mismatchList && shouldUpdate && !file.isSame(otherFile)) {
                            diff.map.put(name, entry);
                            break;
                        }
                        break;
                    }
                    case DIR: {
                        final HashedDir dir = (HashedDir)entry;
                        final HashedDir otherDir = (HashedDir)otherEntry;
                        if (mismatchList || shouldUpdate) {
                            final HashedDir mismatch = dir.sideDiff(otherDir, matcher, path, mismatchList);
                            if (!mismatch.isEmpty()) {
                                diff.map.put(name, mismatch);
                            }
                            break;
                        }
                        break;
                    }
                    default: {
                        throw new AssertionError((Object)("Unsupported hashed entry type: " + type.name()));
                    }
                }
                path.removeLast();
            }
        }
        return diff;
    }
    
    public HashedDir sideCompare(final HashedDir other, final FileNameMatcher matcher, final Deque<String> path, final boolean mismatchList) {
        final HashedDir diff = new HashedDir();
        for (final Map.Entry<String, HashedEntry> mapEntry : this.map.entrySet()) {
            final String name = mapEntry.getKey();
            final HashedEntry entry = mapEntry.getValue();
            path.add(name);
            final boolean shouldUpdate = matcher == null || matcher.shouldUpdate(path);
            final Type type = entry.getType();
            final HashedEntry otherEntry = other.map.get(name);
            if (otherEntry == null || otherEntry.getType() != type) {
                if (shouldUpdate || (mismatchList && otherEntry == null)) {
                    diff.map.put(name, entry);
                    if (!mismatchList) {
                        entry.flag = true;
                    }
                }
                path.removeLast();
            }
            else {
                switch (type) {
                    case FILE: {
                        final HashedFile file = (HashedFile)entry;
                        final HashedFile otherFile = (HashedFile)otherEntry;
                        if (mismatchList && shouldUpdate && file.isSame(otherFile)) {
                            diff.map.put(name, entry);
                            break;
                        }
                        break;
                    }
                    case DIR: {
                        final HashedDir dir = (HashedDir)entry;
                        final HashedDir otherDir = (HashedDir)otherEntry;
                        if (mismatchList || shouldUpdate) {
                            final HashedDir mismatch = dir.sideCompare(otherDir, matcher, path, mismatchList);
                            if (!mismatch.isEmpty()) {
                                diff.map.put(name, mismatch);
                            }
                            break;
                        }
                        break;
                    }
                    default: {
                        throw new AssertionError((Object)("Unsupported hashed entry type: " + type.name()));
                    }
                }
                path.removeLast();
            }
        }
        return diff;
    }
    
    @Override
    public long size() {
        return this.map.values().stream().mapToLong(HashedEntry::size).sum();
    }
    
    @Override
    public void write(final HOutput output) throws IOException {
        final Set<Map.Entry<String, HashedEntry>> entries = this.map.entrySet();
        output.writeLength(entries.size(), 0);
        for (final Map.Entry<String, HashedEntry> mapEntry : entries) {
            output.writeString(mapEntry.getKey(), 255);
            final HashedEntry entry = mapEntry.getValue();
            EnumSerializer.write(output, entry.getType());
            entry.write(output);
        }
    }
    
    public static final class Diff
    {
        @LauncherAPI
        public final HashedDir mismatch;
        @LauncherAPI
        public final HashedDir extra;
        
        private Diff(final HashedDir mismatch, final HashedDir extra) {
            this.mismatch = mismatch;
            this.extra = extra;
        }
        
        @LauncherAPI
        public boolean isSame() {
            return this.mismatch.isEmpty() && this.extra.isEmpty();
        }
    }
    
    private final class HashFileVisitor extends SimpleFileVisitor<Path>
    {
        private final Path dir;
        private final FileNameMatcher matcher;
        private final boolean allowSymlinks;
        private final boolean digest;
        private HashedDir current;
        private final Deque<String> path;
        private final Deque<HashedDir> stack;
        
        private HashFileVisitor(final Path dir, final FileNameMatcher matcher, final boolean allowSymlinks, final boolean digest) {
            this.current = HashedDir.this;
            this.path = new LinkedList<String>();
            this.stack = new LinkedList<HashedDir>();
            this.dir = dir;
            this.matcher = matcher;
            this.allowSymlinks = allowSymlinks;
            this.digest = digest;
        }
        
        @Override
        public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
            final FileVisitResult result = super.postVisitDirectory(dir, exc);
            if (this.dir.equals(dir)) {
                return result;
            }
            final HashedDir parent = this.stack.removeLast();
            parent.map.put(this.path.removeLast(), this.current);
            this.current = parent;
            return result;
        }
        
        @Override
        public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
            final FileVisitResult result = super.preVisitDirectory(dir, attrs);
            if (this.dir.equals(dir)) {
                return result;
            }
            if (!this.allowSymlinks && attrs.isSymbolicLink()) {
                throw new SecurityException("Symlinks are not allowed");
            }
            this.stack.add(this.current);
            this.current = new HashedDir();
            this.path.add(IOHelper.getFileName(dir));
            return result;
        }
        
        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
            if (!this.allowSymlinks && attrs.isSymbolicLink()) {
                throw new SecurityException("Symlinks are not allowed");
            }
            this.path.add(IOHelper.getFileName(file));
            final boolean doDigest = this.digest && (this.matcher == null || this.matcher.shouldUpdate(this.path));
            this.current.map.put(this.path.removeLast(), new HashedFile(file, attrs.size(), doDigest));
            return super.visitFile(file, attrs);
        }
    }
}
