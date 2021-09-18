// 
// Decompiled by Procyon v0.5.36
// 

package ru.gravit.launcher.ssl;

import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;

public class LauncherTrustManager implements X509TrustManager
{
    @Override
    public void checkClientTrusted(final X509Certificate[] certs, final String authType) {
    }
    
    @Override
    public void checkServerTrusted(final X509Certificate[] certs, final String authType) {
    }
    
    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }
}
