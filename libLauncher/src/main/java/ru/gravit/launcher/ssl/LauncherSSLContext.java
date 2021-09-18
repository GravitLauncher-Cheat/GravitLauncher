package ru.gravit.launcher.ssl;

import javax.net.ssl.KeyManager;
import java.security.KeyManagementException;
import java.security.UnrecoverableKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import java.security.KeyStore;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;

public class LauncherSSLContext
{
    public SSLServerSocketFactory ssf;
    public SSLSocketFactory sf;
    private SSLContext sc;
    
    public LauncherSSLContext(final KeyStore ks, final String keypassword) throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, KeyManagementException {
        final TrustManager[] trustAllCerts = { new LauncherTrustManager() };
        final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, keypassword.toCharArray());
        final SSLContext sc = SSLContext.getInstance("TLSv1.2");
        sc.init(kmf.getKeyManagers(), trustAllCerts, new SecureRandom());
        this.ssf = sc.getServerSocketFactory();
        this.sf = sc.getSocketFactory();
    }
    
    public LauncherSSLContext() throws NoSuchAlgorithmException, KeyManagementException {
        final TrustManager[] trustAllCerts = { new LauncherTrustManager() };
        final SSLContext sc = SSLContext.getInstance("TLSv1.2");
        sc.init(null, trustAllCerts, new SecureRandom());
        this.ssf = null;
        this.sf = sc.getSocketFactory();
    }
}
