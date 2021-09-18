package ru.gravit.launcher.ssl;

import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.io.IOException;
import java.security.KeyStoreException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.security.KeyStore;

public class LauncherKeyStore
{
    public static KeyStore getKeyStore(final String keystore, final String password) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        final KeyStore ks = KeyStore.getInstance("JKS");
        try (final InputStream ksIs = new FileInputStream(keystore)) {
            ks.load(ksIs, password.toCharArray());
        }
        return ks;
    }
}
