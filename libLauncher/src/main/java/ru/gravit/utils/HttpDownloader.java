package ru.gravit.utils;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.io.InputStream;
import ru.gravit.utils.helper.IOHelper;
import java.nio.file.Path;
import ru.gravit.utils.helper.LogHelper;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Observable;

public final class HttpDownloader extends Observable
{
    public static final int BUFER_SIZE = 8192;
    public static final int INTERVAL = 300;
    public AtomicInteger writed;
    private String filename;
    public Thread thread;
    
    public HttpDownloader(final URL url, final String file) {
        this.writed = new AtomicInteger(0);
        final Runnable run = () -> {
            try {
                this.downloadFile(url, this.filename = file);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return;
        };
        final Thread downloader = new Thread(run);
        (this.thread = downloader).start();
    }
    
    public synchronized String getFilename() {
        return this.filename;
    }
    
    public void downloadFile(final URL url, final String file) throws IOException {
        try (final BufferedInputStream in = new BufferedInputStream(url.openStream());
             final FileOutputStream fout = new FileOutputStream(file)) {
            final byte[] data = new byte[8192];
            final long timestamp = System.currentTimeMillis();
            int writed_local = 0;
            int count;
            while ((count = in.read(data, 0, 8192)) != -1) {
                fout.write(data, 0, count);
                writed_local += count;
                if (System.currentTimeMillis() - timestamp > 300L) {
                    this.writed.set(writed_local);
                    LogHelper.debug("Downloaded %d", writed_local);
                }
            }
            this.writed.set(writed_local);
        }
    }
    
    public static void downloadZip(final URL url, final Path dir) throws IOException {
        try (final ZipInputStream input = IOHelper.newZipInput(url)) {
            for (ZipEntry entry = input.getNextEntry(); entry != null; entry = input.getNextEntry()) {
                if (!entry.isDirectory()) {
                    final String name = entry.getName();
                    LogHelper.subInfo("Downloading file: '%s'", name);
                    IOHelper.transfer(input, dir.resolve(IOHelper.toPath(name)));
                }
            }
        }
    }
}
