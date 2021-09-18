package ru.gravit.launcher.hasher;

import ru.gravit.launcher.LauncherAPI;
import java.util.Collection;

public final class FileNameMatcher
{
    private static final String[] NO_ENTRIES;
    private final String[] update;
    private final String[] verify;
    private final String[] exclusions;
    
    private static boolean anyMatch(final String[] entries, final Collection<String> path) {
        final String jpath = String.join("/", path);
        for (final String e : entries) {
            if (jpath.startsWith(e)) {
                return true;
            }
        }
        return false;
    }
    
    @LauncherAPI
    public FileNameMatcher(final String[] update, final String[] verify, final String[] exclusions) {
        this.update = update;
        this.verify = verify;
        this.exclusions = exclusions;
    }
    
    public boolean shouldUpdate(final Collection<String> path) {
        return (anyMatch(this.update, path) || anyMatch(this.verify, path)) && !anyMatch(this.exclusions, path);
    }
    
    public boolean shouldVerify(final Collection<String> path) {
        return anyMatch(this.verify, path) && !anyMatch(this.exclusions, path);
    }
    
    public FileNameMatcher verifyOnly() {
        return new FileNameMatcher(FileNameMatcher.NO_ENTRIES, this.verify, this.exclusions);
    }
    
    static {
        NO_ENTRIES = new String[0];
    }
}
