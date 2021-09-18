package ru.gravit.launcher.profiles.optional;

import ru.gravit.utils.helper.LogHelper;
import ru.gravit.launcher.serialize.HInput;
import java.io.IOException;
import ru.gravit.launcher.serialize.HOutput;
import java.util.Objects;
import java.util.Set;
import ru.gravit.launcher.LauncherAPI;

public class OptionalFile
{
    @LauncherAPI
    public String[] list;
    @LauncherAPI
    public OptionalType type;
    @LauncherAPI
    public boolean mark;
    @LauncherAPI
    public boolean visible;
    @LauncherAPI
    public String name;
    @LauncherAPI
    public String info;
    @LauncherAPI
    public OptionalDepend[] dependenciesFile;
    @LauncherAPI
    public OptionalDepend[] conflictFile;
    @LauncherAPI
    public transient OptionalFile[] dependencies;
    @LauncherAPI
    public transient OptionalFile[] conflict;
    @LauncherAPI
    public int subTreeLevel;
    @LauncherAPI
    public long permissions;
    @LauncherAPI
    public transient Set<OptionalFile> dependenciesCount;
    
    public OptionalFile() {
        this.visible = true;
        this.subTreeLevel = 1;
        this.permissions = 0L;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final OptionalFile that = (OptionalFile)o;
        return Objects.equals(this.name, that.name);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(this.name);
    }
    
    @LauncherAPI
    public OptionalType getType() {
        return OptionalType.FILE;
    }
    
    @LauncherAPI
    public String getName() {
        return this.name;
    }
    
    @LauncherAPI
    public boolean isVisible() {
        return this.visible;
    }
    
    @LauncherAPI
    public boolean isMark() {
        return this.mark;
    }
    
    @LauncherAPI
    public long getPermissions() {
        return this.permissions;
    }
    
    @LauncherAPI
    public void writeType(final HOutput output) throws IOException {
        switch (this.type) {
            case FILE: {
                output.writeInt(1);
                break;
            }
            case CLASSPATH: {
                output.writeInt(2);
                break;
            }
            case JVMARGS: {
                output.writeInt(3);
                break;
            }
            case CLIENTARGS: {
                output.writeInt(4);
                break;
            }
            default: {
                output.writeInt(5);
                break;
            }
        }
    }
    
    @LauncherAPI
    public static OptionalType readType(final HInput input) throws IOException {
        final int t = input.readInt();
        OptionalType type = null;
        switch (t) {
            case 1: {
                type = OptionalType.FILE;
                break;
            }
            case 2: {
                type = OptionalType.CLASSPATH;
                break;
            }
            case 3: {
                type = OptionalType.JVMARGS;
                break;
            }
            case 4: {
                type = OptionalType.CLIENTARGS;
                break;
            }
            default: {
                LogHelper.error("readType failed. Read int %d", t);
                type = OptionalType.FILE;
                break;
            }
        }
        return type;
    }
}
