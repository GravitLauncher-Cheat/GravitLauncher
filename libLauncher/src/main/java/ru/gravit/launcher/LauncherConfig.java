// 
// Decompiled by Procyon v0.5.36
// 

package ru.gravit.launcher;

import java.util.Iterator;
import java.util.Set;
import ru.gravit.launcher.serialize.HOutput;
import java.util.Objects;
import java.security.spec.InvalidKeySpecException;
import java.io.IOException;
import java.util.Collections;
import ru.gravit.utils.helper.VerifyHelper;
import java.util.HashMap;
import ru.gravit.utils.helper.SecurityHelper;
import ru.gravit.launcher.serialize.HInput;
import java.util.Map;
import java.security.interfaces.RSAPublicKey;
import java.net.InetSocketAddress;
import ru.gravit.launcher.serialize.stream.StreamObject;

public final class LauncherConfig extends StreamObject
{
    private static final AutogenConfig config;
    @LauncherAPI
    public InetSocketAddress address;
    public String nettyAddress;
    public int nettyPort;
    @LauncherAPI
    public final String projectname;
    public final int clientPort;
    public String secretKeyClient;
    @LauncherAPI
    public final RSAPublicKey publicKey;
    @LauncherAPI
    public final Map<String, byte[]> runtime;
    public final boolean isUsingWrapper;
    public final boolean isDownloadJava;
    public final boolean isWarningMissArchJava;
    public final boolean isNettyEnabled;
    public final String guardLicenseName;
    public final String guardLicenseKey;
    public final String guardLicenseEncryptKey;
    
    public static AutogenConfig getAutogenConfig() {
        return LauncherConfig.config;
    }
    
    @LauncherAPI
    public LauncherConfig(final HInput input) throws IOException, InvalidKeySpecException {
        this.address = InetSocketAddress.createUnresolved(LauncherConfig.config.address, LauncherConfig.config.port);
        this.publicKey = SecurityHelper.toPublicRSAKey(input.readByteArray(2048));
        this.projectname = LauncherConfig.config.projectname;
        this.clientPort = LauncherConfig.config.clientPort;
        this.secretKeyClient = LauncherConfig.config.secretKeyClient;
        this.isDownloadJava = LauncherConfig.config.isDownloadJava;
        this.isUsingWrapper = LauncherConfig.config.isUsingWrapper;
        this.isWarningMissArchJava = LauncherConfig.config.isWarningMissArchJava;
        this.guardLicenseEncryptKey = LauncherConfig.config.guardLicenseEncryptKey;
        this.guardLicenseKey = LauncherConfig.config.guardLicenseKey;
        this.guardLicenseName = LauncherConfig.config.guardLicenseName;
        this.nettyPort = LauncherConfig.config.nettyPort;
        this.nettyAddress = LauncherConfig.config.nettyAddress;
        this.isNettyEnabled = LauncherConfig.config.isNettyEnabled;
        LauncherEnvironment env;
        if (LauncherConfig.config.env == 0) {
            env = LauncherEnvironment.DEV;
        }
        else if (LauncherConfig.config.env == 1) {
            env = LauncherEnvironment.DEBUG;
        }
        else if (LauncherConfig.config.env == 2) {
            env = LauncherEnvironment.STD;
        }
        else if (LauncherConfig.config.env == 3) {
            env = LauncherEnvironment.PROD;
        }
        else {
            env = LauncherEnvironment.STD;
        }
        Launcher.applyLauncherEnv(env);
        final int count = input.readLength(0);
        final Map<String, byte[]> localResources = new HashMap<String, byte[]>(count);
        for (int i = 0; i < count; ++i) {
            final String name = input.readString(255);
            VerifyHelper.putIfAbsent(localResources, name, input.readByteArray(2048), String.format("Duplicate runtime resource: '%s'", name));
        }
        this.runtime = Collections.unmodifiableMap((Map<? extends String, ? extends byte[]>)localResources);
    }
    
    @LauncherAPI
    public LauncherConfig(final String address, final int port, final RSAPublicKey publicKey, final Map<String, byte[]> runtime, final String projectname) {
        this.address = InetSocketAddress.createUnresolved(address, port);
        this.publicKey = Objects.requireNonNull(publicKey, "publicKey");
        this.runtime = Collections.unmodifiableMap((Map<? extends String, ? extends byte[]>)new HashMap<String, byte[]>(runtime));
        this.projectname = projectname;
        this.clientPort = 32148;
        this.guardLicenseName = "FREE";
        this.guardLicenseKey = "AAAA-BBBB-CCCC-DDDD";
        this.guardLicenseEncryptKey = "12345";
        this.isUsingWrapper = true;
        this.isDownloadJava = false;
        this.isWarningMissArchJava = true;
        this.isNettyEnabled = false;
    }
    
    @LauncherAPI
    public LauncherConfig(final String address, final int port, final RSAPublicKey publicKey, final Map<String, byte[]> runtime) {
        this.address = InetSocketAddress.createUnresolved(address, port);
        this.publicKey = Objects.requireNonNull(publicKey, "publicKey");
        this.runtime = Collections.unmodifiableMap((Map<? extends String, ? extends byte[]>)new HashMap<String, byte[]>(runtime));
        this.projectname = "Minecraft";
        this.guardLicenseName = "FREE";
        this.guardLicenseKey = "AAAA-BBBB-CCCC-DDDD";
        this.guardLicenseEncryptKey = "12345";
        this.clientPort = 32148;
        this.isUsingWrapper = true;
        this.isDownloadJava = false;
        this.isWarningMissArchJava = true;
        this.isNettyEnabled = false;
    }
    
    @Override
    public void write(final HOutput output) throws IOException {
        output.writeByteArray(this.publicKey.getEncoded(), 2048);
        final Set<Map.Entry<String, byte[]>> entrySet = this.runtime.entrySet();
        output.writeLength(entrySet.size(), 0);
        for (final Map.Entry<String, byte[]> entry : this.runtime.entrySet()) {
            output.writeString(entry.getKey(), 255);
            output.writeByteArray(entry.getValue(), 2048);
        }
    }
    
    static {
        config = new AutogenConfig();
    }
    
    public enum LauncherEnvironment
    {
        DEV, 
        DEBUG, 
        STD, 
        PROD;
    }
}
