package ru.gravit.launcher.profiles;

import java.util.HashMap;
import java.util.Map;
import ru.gravit.utils.helper.VerifyHelper;
import ru.gravit.utils.helper.IOHelper;
import java.net.InetSocketAddress;
import java.io.IOException;
import java.util.Arrays;
import ru.gravit.launcher.hasher.HashedDir;
import java.util.Collection;
import ru.gravit.launcher.profiles.optional.OptionalType;
import java.util.Iterator;
import java.util.HashSet;
import java.util.ArrayList;
import ru.gravit.launcher.profiles.optional.OptionalFile;
import java.util.Set;
import java.util.List;
import ru.gravit.launcher.LauncherAPI;
import ru.gravit.launcher.hasher.FileNameMatcher;

public final class ClientProfile implements Comparable<ClientProfile>
{
    public static final boolean profileCaseSensitive;
    private static final FileNameMatcher ASSET_MATCHER;
    @LauncherAPI
    private String version;
    @LauncherAPI
    private String assetIndex;
    @LauncherAPI
    private String dir;
    @LauncherAPI
    private String assetDir;
    @LauncherAPI
    private int sortIndex;
    @LauncherAPI
    private String title;
    @LauncherAPI
    private String info;
    @LauncherAPI
    private String serverAddress;
    @LauncherAPI
    private int serverPort;
    @LauncherAPI
    private final List<String> update;
    @LauncherAPI
    private final List<String> updateExclusions;
    @LauncherAPI
    private final List<String> updateShared;
    @LauncherAPI
    private final List<String> updateVerify;
    @LauncherAPI
    private final Set<OptionalFile> updateOptional;
    @LauncherAPI
    private boolean updateFastCheck;
    @LauncherAPI
    private boolean useWhitelist;
    @LauncherAPI
    private String mainClass;
    @LauncherAPI
    private final List<String> jvmArgs;
    @LauncherAPI
    private final List<String> classPath;
    @LauncherAPI
    private final List<String> clientArgs;
    @LauncherAPI
    private final List<String> whitelist;
    
    public ClientProfile() {
        this.update = new ArrayList<String>();
        this.updateExclusions = new ArrayList<String>();
        this.updateShared = new ArrayList<String>();
        this.updateVerify = new ArrayList<String>();
        this.updateOptional = new HashSet<OptionalFile>();
        this.jvmArgs = new ArrayList<String>();
        this.classPath = new ArrayList<String>();
        this.clientArgs = new ArrayList<String>();
        this.whitelist = new ArrayList<String>();
    }
    
    @Override
    public int compareTo(final ClientProfile o) {
        return Integer.compare(this.getSortIndex(), o.getSortIndex());
    }
    
    @LauncherAPI
    public String getAssetIndex() {
        return this.assetIndex;
    }
    
    @LauncherAPI
    public FileNameMatcher getAssetUpdateMatcher() {
        return (this.getVersion().compareTo(Version.MC1710) >= 0) ? ClientProfile.ASSET_MATCHER : null;
    }
    
    @LauncherAPI
    public String[] getClassPath() {
        return this.classPath.toArray(new String[0]);
    }
    
    @LauncherAPI
    public String[] getClientArgs() {
        return this.clientArgs.toArray(new String[0]);
    }
    
    @LauncherAPI
    public String getDir() {
        return this.dir;
    }
    
    public void setDir(final String dir) {
        this.dir = dir;
    }
    
    @LauncherAPI
    public String getAssetDir() {
        return this.assetDir;
    }
    
    @LauncherAPI
    public FileNameMatcher getClientUpdateMatcher() {
        final String[] updateArray = this.update.toArray(new String[0]);
        final String[] verifyArray = this.updateVerify.toArray(new String[0]);
        final List<String> excludeList = this.updateExclusions;
        final String[] exclusionsArray = excludeList.toArray(new String[0]);
        return new FileNameMatcher(updateArray, verifyArray, exclusionsArray);
    }
    
    @LauncherAPI
    public String[] getJvmArgs() {
        return this.jvmArgs.toArray(new String[0]);
    }
    
    @LauncherAPI
    public String getMainClass() {
        return this.mainClass;
    }
    
    @LauncherAPI
    public String getServerAddress() {
        return this.serverAddress;
    }
    
    @LauncherAPI
    public Set<OptionalFile> getOptional() {
        return this.updateOptional;
    }
    
    @LauncherAPI
    public void updateOptionalGraph() {
        for (final OptionalFile file : this.updateOptional) {
            if (file.dependenciesFile != null) {
                file.dependencies = new OptionalFile[file.dependenciesFile.length];
                for (int i = 0; i < file.dependenciesFile.length; ++i) {
                    file.dependencies[i] = this.getOptionalFile(file.dependenciesFile[i].name, file.dependenciesFile[i].type);
                }
            }
            if (file.conflictFile != null) {
                file.conflict = new OptionalFile[file.conflictFile.length];
                for (int i = 0; i < file.conflictFile.length; ++i) {
                    file.conflict[i] = this.getOptionalFile(file.conflictFile[i].name, file.conflictFile[i].type);
                }
            }
        }
    }
    
    @LauncherAPI
    public OptionalFile getOptionalFile(final String file, final OptionalType type) {
        for (final OptionalFile f : this.updateOptional) {
            if (f.type.equals(type) && f.name.equals(file)) {
                return f;
            }
        }
        return null;
    }
    
    @LauncherAPI
    public Collection<String> getShared() {
        return this.updateShared;
    }
    
    @LauncherAPI
    public void markOptional(final String name, final OptionalType type) {
        final OptionalFile file = this.getOptionalFile(name, type);
        if (file == null) {
            throw new SecurityException(String.format("Optional %s not found in optionalList", name));
        }
        this.markOptional(file);
    }
    
    @LauncherAPI
    public void markOptional(final OptionalFile file) {
        if (file.mark) {
            return;
        }
        file.mark = true;
        if (file.dependencies != null) {
            for (final OptionalFile dep : file.dependencies) {
                if (dep.dependenciesCount == null) {
                    dep.dependenciesCount = new HashSet<OptionalFile>();
                }
                dep.dependenciesCount.add(file);
                this.markOptional(dep);
            }
        }
        if (file.conflict != null) {
            for (final OptionalFile conflict : file.conflict) {
                this.unmarkOptional(conflict);
            }
        }
    }
    
    @LauncherAPI
    public void unmarkOptional(final String name, final OptionalType type) {
        final OptionalFile file = this.getOptionalFile(name, type);
        if (file == null) {
            throw new SecurityException(String.format("Optional %s not found in optionalList", name));
        }
        this.unmarkOptional(file);
    }
    
    @LauncherAPI
    public void unmarkOptional(final OptionalFile file) {
        if (!file.mark) {
            return;
        }
        file.mark = false;
        if (file.dependenciesCount != null) {
            for (final OptionalFile f : file.dependenciesCount) {
                this.unmarkOptional(f);
            }
            file.dependenciesCount.clear();
            file.dependenciesCount = null;
        }
        if (file.dependencies != null) {
            for (final OptionalFile f2 : file.dependencies) {
                if (f2.mark) {
                    if (f2.dependenciesCount == null) {
                        this.unmarkOptional(f2);
                    }
                    else if (f2.dependenciesCount.size() <= 1) {
                        f2.dependenciesCount.clear();
                        f2.dependenciesCount = null;
                        this.unmarkOptional(f2);
                    }
                }
            }
        }
    }
    
    public void pushOptionalFile(final HashedDir dir, final boolean digest) {
        for (final OptionalFile opt : this.updateOptional) {
            if (opt.type.equals(OptionalType.FILE) && !opt.mark) {
                for (final String file : opt.list) {
                    dir.removeR(file);
                }
            }
        }
    }
    
    public void pushOptionalJvmArgs(final Collection<String> jvmArgs1) {
        for (final OptionalFile opt : this.updateOptional) {
            if (opt.type.equals(OptionalType.JVMARGS) && opt.mark) {
                jvmArgs1.addAll(Arrays.asList(opt.list));
            }
        }
    }
    
    public void pushOptionalClientArgs(final Collection<String> clientArgs1) {
        for (final OptionalFile opt : this.updateOptional) {
            if (opt.type.equals(OptionalType.CLIENTARGS) && opt.mark) {
                clientArgs1.addAll(Arrays.asList(opt.list));
            }
        }
    }
    
    public void pushOptionalClassPath(final pushOptionalClassPathCallback callback) throws IOException {
        for (final OptionalFile opt : this.updateOptional) {
            if (opt.type.equals(OptionalType.CLASSPATH) && opt.mark) {
                callback.run(opt.list);
            }
        }
    }
    
    @LauncherAPI
    public int getServerPort() {
        return this.serverPort;
    }
    
    @LauncherAPI
    public InetSocketAddress getServerSocketAddress() {
        return InetSocketAddress.createUnresolved(this.getServerAddress(), this.getServerPort());
    }
    
    @LauncherAPI
    public int getSortIndex() {
        return this.sortIndex;
    }
    
    @LauncherAPI
    public String getTitle() {
        return this.title;
    }
    
    @LauncherAPI
    public String getInfo() {
        return this.info;
    }
    
    @LauncherAPI
    public Version getVersion() {
        return Version.byName(this.version);
    }
    
    @LauncherAPI
    public boolean isUpdateFastCheck() {
        return this.updateFastCheck;
    }
    
    @LauncherAPI
    public boolean isWhitelistContains(final String username) {
        return !this.useWhitelist || this.whitelist.stream().anyMatch(ClientProfile.profileCaseSensitive ? (e -> e.equals(username)) : (e -> e.equalsIgnoreCase(username)));
    }
    
    @LauncherAPI
    public void setTitle(final String title) {
        this.title = title;
    }
    
    @LauncherAPI
    public void setInfo(final String info) {
        this.info = info;
    }
    
    @LauncherAPI
    public void setVersion(final Version version) {
        this.version = version.name;
    }
    
    @Override
    public String toString() {
        return this.title;
    }
    
    @LauncherAPI
    public void verify() {
        this.getVersion();
        IOHelper.verifyFileName(this.getAssetIndex());
        VerifyHelper.verify(this.getTitle(), VerifyHelper.NOT_EMPTY, "Profile title can't be empty");
        VerifyHelper.verify(this.getInfo(), VerifyHelper.NOT_EMPTY, "Profile info can't be empty");
        VerifyHelper.verify(this.getServerAddress(), VerifyHelper.NOT_EMPTY, "Server address can't be empty");
        VerifyHelper.verifyInt(this.getServerPort(), VerifyHelper.range(0, 65535), "Illegal server port: " + this.getServerPort());
        VerifyHelper.verify(this.getTitle(), VerifyHelper.NOT_EMPTY, "Main class can't be empty");
        for (final String s : this.classPath) {
            if (s == null) {
                throw new IllegalArgumentException("Found null entry in classPath");
            }
        }
        for (final String s : this.jvmArgs) {
            if (s == null) {
                throw new IllegalArgumentException("Found null entry in jvmArgs");
            }
        }
        for (final String s : this.clientArgs) {
            if (s == null) {
                throw new IllegalArgumentException("Found null entry in clientArgs");
            }
        }
        for (final OptionalFile f : this.updateOptional) {
            if (f == null) {
                throw new IllegalArgumentException("Found null entry in updateOptional");
            }
            if (f.name == null) {
                throw new IllegalArgumentException("Optional: name must not be null");
            }
            if (f.list == null) {
                throw new IllegalArgumentException("Optional: list must not be null");
            }
        }
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = 31 * result + ((this.assetDir == null) ? 0 : this.assetDir.hashCode());
        result = 31 * result + ((this.assetIndex == null) ? 0 : this.assetIndex.hashCode());
        result = 31 * result + ((this.classPath == null) ? 0 : this.classPath.hashCode());
        result = 31 * result + ((this.clientArgs == null) ? 0 : this.clientArgs.hashCode());
        result = 31 * result + ((this.dir == null) ? 0 : this.dir.hashCode());
        result = 31 * result + ((this.jvmArgs == null) ? 0 : this.jvmArgs.hashCode());
        result = 31 * result + ((this.mainClass == null) ? 0 : this.mainClass.hashCode());
        result = 31 * result + ((this.serverAddress == null) ? 0 : this.serverAddress.hashCode());
        result = 31 * result + this.serverPort;
        result = 31 * result + this.sortIndex;
        result = 31 * result + ((this.title == null) ? 0 : this.title.hashCode());
        result = 31 * result + ((this.info == null) ? 0 : this.info.hashCode());
        result = 31 * result + ((this.update == null) ? 0 : this.update.hashCode());
        result = 31 * result + ((this.updateExclusions == null) ? 0 : this.updateExclusions.hashCode());
        result = 31 * result + (this.updateFastCheck ? 1231 : 1237);
        result = 31 * result + ((this.updateOptional == null) ? 0 : this.updateOptional.hashCode());
        result = 31 * result + ((this.updateShared == null) ? 0 : this.updateShared.hashCode());
        result = 31 * result + ((this.updateVerify == null) ? 0 : this.updateVerify.hashCode());
        result = 31 * result + (this.useWhitelist ? 1231 : 1237);
        result = 31 * result + ((this.version == null) ? 0 : this.version.hashCode());
        result = 31 * result + ((this.whitelist == null) ? 0 : this.whitelist.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final ClientProfile other = (ClientProfile)obj;
        if (this.assetDir == null) {
            if (other.assetDir != null) {
                return false;
            }
        }
        else if (!this.assetDir.equals(other.assetDir)) {
            return false;
        }
        if (this.assetIndex == null) {
            if (other.assetIndex != null) {
                return false;
            }
        }
        else if (!this.assetIndex.equals(other.assetIndex)) {
            return false;
        }
        if (this.classPath == null) {
            if (other.classPath != null) {
                return false;
            }
        }
        else if (!this.classPath.equals(other.classPath)) {
            return false;
        }
        if (this.clientArgs == null) {
            if (other.clientArgs != null) {
                return false;
            }
        }
        else if (!this.clientArgs.equals(other.clientArgs)) {
            return false;
        }
        if (this.dir == null) {
            if (other.dir != null) {
                return false;
            }
        }
        else if (!this.dir.equals(other.dir)) {
            return false;
        }
        if (this.jvmArgs == null) {
            if (other.jvmArgs != null) {
                return false;
            }
        }
        else if (!this.jvmArgs.equals(other.jvmArgs)) {
            return false;
        }
        if (this.mainClass == null) {
            if (other.mainClass != null) {
                return false;
            }
        }
        else if (!this.mainClass.equals(other.mainClass)) {
            return false;
        }
        if (this.serverAddress == null) {
            if (other.serverAddress != null) {
                return false;
            }
        }
        else if (!this.serverAddress.equals(other.serverAddress)) {
            return false;
        }
        if (this.serverPort != other.serverPort) {
            return false;
        }
        if (this.sortIndex != other.sortIndex) {
            return false;
        }
        if (this.title == null) {
            if (other.title != null) {
                return false;
            }
        }
        else if (!this.title.equals(other.title)) {
            return false;
        }
        if (this.info == null) {
            if (other.info != null) {
                return false;
            }
        }
        else if (!this.info.equals(other.info)) {
            return false;
        }
        if (this.update == null) {
            if (other.update != null) {
                return false;
            }
        }
        else if (!this.update.equals(other.update)) {
            return false;
        }
        if (this.updateExclusions == null) {
            if (other.updateExclusions != null) {
                return false;
            }
        }
        else if (!this.updateExclusions.equals(other.updateExclusions)) {
            return false;
        }
        if (this.updateFastCheck != other.updateFastCheck) {
            return false;
        }
        if (this.updateOptional == null) {
            if (other.updateOptional != null) {
                return false;
            }
        }
        else if (!this.updateOptional.equals(other.updateOptional)) {
            return false;
        }
        if (this.updateShared == null) {
            if (other.updateShared != null) {
                return false;
            }
        }
        else if (!this.updateShared.equals(other.updateShared)) {
            return false;
        }
        if (this.updateVerify == null) {
            if (other.updateVerify != null) {
                return false;
            }
        }
        else if (!this.updateVerify.equals(other.updateVerify)) {
            return false;
        }
        if (this.useWhitelist != other.useWhitelist) {
            return false;
        }
        if (this.version == null) {
            if (other.version != null) {
                return false;
            }
        }
        else if (!this.version.equals(other.version)) {
            return false;
        }
        if (this.whitelist == null) {
            return other.whitelist == null;
        }
        return this.whitelist.equals(other.whitelist);
    }
    
    static {
        profileCaseSensitive = Boolean.getBoolean("launcher.clientProfile.caseSensitive");
        ASSET_MATCHER = new FileNameMatcher(new String[0], new String[] { "indexes", "objects" }, new String[0]);
    }
    
    @LauncherAPI
    public enum Version
    {
        MC147("1.4.7", 51), 
        MC152("1.5.2", 61), 
        MC164("1.6.4", 78), 
        MC172("1.7.2", 4), 
        MC1710("1.7.10", 5), 
        MC189("1.8.9", 47), 
        MC194("1.9.4", 110), 
        MC1102("1.10.2", 210), 
        MC1112("1.11.2", 316), 
        MC1122("1.12.2", 340), 
        MC113("1.13", 393), 
        MC1131("1.13.1", 401), 
        MC1132("1.13.2", 402);
        
        private static final Map<String, Version> VERSIONS;
        public final String name;
        public final int protocol;
        
        public static Version byName(final String name) {
            return VerifyHelper.getMapValue(Version.VERSIONS, name, String.format("Unknown client version: '%s'", name));
        }
        
        private Version(final String name, final int protocol) {
            this.name = name;
            this.protocol = protocol;
        }
        
        @Override
        public String toString() {
            return "Minecraft " + this.name;
        }
        
        static {
            final Version[] versionsValues = values();
            VERSIONS = new HashMap<String, Version>(versionsValues.length);
            for (final Version version : versionsValues) {
                Version.VERSIONS.put(version.name, version);
            }
        }
    }
    
    @FunctionalInterface
    public interface pushOptionalClassPathCallback
    {
        void run(final String[] p0) throws IOException;
    }
}
