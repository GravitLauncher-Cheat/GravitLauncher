package ru.gravit.utils.downloader;

import java.security.cert.Certificate;
import java.util.Collections;
import java.util.Iterator;
import javax.net.ssl.HttpsURLConnection;
import ru.gravit.utils.helper.LogHelper;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import ru.gravit.utils.helper.IOHelper;
import java.io.IOException;
import java.util.HashMap;
import java.net.HttpURLConnection;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.net.URL;
import java.io.File;
import java.util.Map;

public class Downloader implements Runnable
{
    public static final Map<String, String> requestClient;
    public static final int INTERVAL = 300;
    private final File file;
    private final URL url;
    private final String method;
    public final Map<String, String> requestProps;
    public AtomicInteger writed;
    public final AtomicBoolean interrupt;
    public final AtomicBoolean interrupted;
    public AtomicReference<Throwable> ex;
    private final int skip;
    private final Handler handler;
    private HttpURLConnection connect;
    
    public Downloader(final URL url, final File file) {
        this.writed = new AtomicInteger(0);
        this.interrupt = new AtomicBoolean(false);
        this.interrupted = new AtomicBoolean(false);
        this.ex = new AtomicReference<Throwable>(null);
        this.connect = null;
        this.requestProps = new HashMap<String, String>(Downloader.requestClient);
        this.file = file;
        this.url = url;
        this.skip = 0;
        this.handler = null;
        this.method = null;
    }
    
    public Downloader(final URL url, final File file, final int skip) {
        this.writed = new AtomicInteger(0);
        this.interrupt = new AtomicBoolean(false);
        this.interrupted = new AtomicBoolean(false);
        this.ex = new AtomicReference<Throwable>(null);
        this.connect = null;
        this.requestProps = new HashMap<String, String>(Downloader.requestClient);
        this.file = file;
        this.url = url;
        this.skip = skip;
        this.handler = null;
        this.method = null;
    }
    
    public Downloader(final URL url, final File file, final Handler handler) {
        this.writed = new AtomicInteger(0);
        this.interrupt = new AtomicBoolean(false);
        this.interrupted = new AtomicBoolean(false);
        this.ex = new AtomicReference<Throwable>(null);
        this.connect = null;
        this.requestProps = new HashMap<String, String>(Downloader.requestClient);
        this.file = file;
        this.url = url;
        this.skip = 0;
        this.handler = handler;
        this.method = null;
    }
    
    public Downloader(final URL url, final File file, final int skip, final Handler handler) {
        this.writed = new AtomicInteger(0);
        this.interrupt = new AtomicBoolean(false);
        this.interrupted = new AtomicBoolean(false);
        this.ex = new AtomicReference<Throwable>(null);
        this.connect = null;
        this.requestProps = new HashMap<String, String>(Downloader.requestClient);
        this.file = file;
        this.url = url;
        this.skip = skip;
        this.handler = handler;
        this.method = null;
    }
    
    public Downloader(final URL url, final File file, final int skip, final Handler handler, final Map<String, String> requestProps) {
        this.writed = new AtomicInteger(0);
        this.interrupt = new AtomicBoolean(false);
        this.interrupted = new AtomicBoolean(false);
        this.ex = new AtomicReference<Throwable>(null);
        this.connect = null;
        this.requestProps = new HashMap<String, String>(requestProps);
        this.file = file;
        this.url = url;
        this.skip = skip;
        this.handler = handler;
        this.method = null;
    }
    
    public Downloader(final URL url, final File file, final int skip, final Handler handler, final Map<String, String> requestProps, final String method) {
        this.writed = new AtomicInteger(0);
        this.interrupt = new AtomicBoolean(false);
        this.interrupted = new AtomicBoolean(false);
        this.ex = new AtomicReference<Throwable>(null);
        this.connect = null;
        this.requestProps = new HashMap<String, String>(requestProps);
        this.file = file;
        this.url = url;
        this.skip = skip;
        this.handler = handler;
        this.method = method;
    }
    
    public Downloader(final URL url, final File file, final int skip, final Handler handler, final String method) {
        this.writed = new AtomicInteger(0);
        this.interrupt = new AtomicBoolean(false);
        this.interrupted = new AtomicBoolean(false);
        this.ex = new AtomicReference<Throwable>(null);
        this.connect = null;
        this.requestProps = new HashMap<String, String>(Downloader.requestClient);
        this.file = file;
        this.url = url;
        this.skip = skip;
        this.handler = handler;
        this.method = method;
    }
    
    public Map<String, String> getProps() {
        return this.requestProps;
    }
    
    public void addProp(final String key, final String value) {
        this.requestProps.put(key, value);
    }
    
    public File getFile() {
        return this.file;
    }
    
    public String getMethod() {
        return this.method;
    }
    
    public Handler getHandler() {
        return this.handler;
    }
    
    public void downloadFile() throws IOException {
        if (!this.url.getProtocol().equalsIgnoreCase("http") && !this.url.getProtocol().equalsIgnoreCase("https")) {
            throw new IOException("Invalid protocol.");
        }
        this.interrupted.set(false);
        if (this.url.getProtocol().equalsIgnoreCase("http")) {
            final HttpURLConnection connect = (HttpURLConnection)this.url.openConnection();
            this.connect = connect;
            if (this.method != null) {
                connect.setRequestMethod(this.method);
            }
            for (final Map.Entry<String, String> ent : this.requestProps.entrySet()) {
                connect.setRequestProperty(ent.getKey(), ent.getValue());
            }
            connect.setInstanceFollowRedirects(true);
            if (connect.getResponseCode() < 200 || connect.getResponseCode() >= 300) {
                throw new IOException(String.format("Invalid response of http server %d.", connect.getResponseCode()));
            }
            try (final BufferedInputStream in = new BufferedInputStream(connect.getInputStream(), IOHelper.BUFFER_SIZE);
                 final FileOutputStream fout = new FileOutputStream(this.file, this.skip != 0)) {
                final byte[] data = new byte[IOHelper.BUFFER_SIZE];
                int count = -1;
                final long timestamp = System.currentTimeMillis();
                int writed_local = 0;
                in.skip(this.skip);
                while ((count = in.read(data)) != -1) {
                    fout.write(data, 0, count);
                    writed_local += count;
                    if (System.currentTimeMillis() - timestamp > 300L) {
                        this.writed.set(writed_local);
                        LogHelper.debug("Downloaded %d", writed_local);
                        if (this.interrupt.get()) {
                            break;
                        }
                        continue;
                    }
                }
                LogHelper.debug("Downloaded %d", writed_local);
                this.writed.set(writed_local);
            }
        }
        else {
            final HttpsURLConnection connect2 = (HttpsURLConnection)this.url.openConnection();
            this.connect = connect2;
            if (this.method != null) {
                connect2.setRequestMethod(this.method);
            }
            for (final Map.Entry<String, String> ent : this.requestProps.entrySet()) {
                connect2.setRequestProperty(ent.getKey(), ent.getValue());
            }
            connect2.setInstanceFollowRedirects(true);
            if (this.handler != null) {
                this.handler.check(connect2.getServerCertificates());
            }
            if (connect2.getResponseCode() < 200 || connect2.getResponseCode() >= 300) {
                throw new IOException(String.format("Invalid response of http server %d.", connect2.getResponseCode()));
            }
            try (final BufferedInputStream in = new BufferedInputStream(connect2.getInputStream(), IOHelper.BUFFER_SIZE);
                 final FileOutputStream fout = new FileOutputStream(this.file, this.skip != 0)) {
                final byte[] data = new byte[IOHelper.BUFFER_SIZE];
                int count = -1;
                final long timestamp = System.currentTimeMillis();
                int writed_local = 0;
                in.skip(this.skip);
                while ((count = in.read(data)) != -1) {
                    fout.write(data, 0, count);
                    writed_local += count;
                    if (System.currentTimeMillis() - timestamp > 300L) {
                        this.writed.set(writed_local);
                        LogHelper.debug("Downloaded %d", writed_local);
                        if (this.interrupt.get()) {
                            break;
                        }
                        continue;
                    }
                }
                LogHelper.debug("Downloaded %d", writed_local);
                this.writed.set(writed_local);
            }
        }
        this.interrupted.set(true);
    }
    
    @Override
    public void run() {
        try {
            this.downloadFile();
        }
        catch (Throwable ex) {
            this.ex.set(ex);
            LogHelper.error(ex);
        }
        if (this.connect != null) {
            try {
                this.connect.disconnect();
            }
            catch (Throwable t) {}
        }
    }
    
    static {
        requestClient = Collections.singletonMap("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
    }
    
    @FunctionalInterface
    public interface Handler
    {
        void check(final Certificate[] p0) throws IOException;
    }
}
