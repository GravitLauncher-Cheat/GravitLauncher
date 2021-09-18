package ru.gravit.utils.helper;

import java.nio.file.NoSuchFileException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.LinkOption;
import java.util.Arrays;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URL;

public final class UnpackHelper
{
    public static boolean unpack(final URL resource, final Path target) throws IOException {
        if (IOHelper.isFile(target) && matches(target, resource)) {
            return false;
        }
        Files.deleteIfExists(target);
        IOHelper.createParentDirs(target);
        try (final InputStream in = IOHelper.newInput(resource)) {
            IOHelper.transfer(in, target);
        }
        return true;
    }
    
    private static boolean matches(final Path target, final URL in) {
        try {
            return Arrays.equals(SecurityHelper.digest(SecurityHelper.DigestAlgorithm.SHA256, in), SecurityHelper.digest(SecurityHelper.DigestAlgorithm.SHA256, target));
        }
        catch (IOException e) {
            return false;
        }
    }
    
    public static boolean unpackZipNoCheck(final URL resource, final Path target) throws IOException {
        if (Files.isDirectory(target, new LinkOption[0])) {
            return false;
        }
        Files.deleteIfExists(target);
        Files.createDirectory(target, (FileAttribute<?>[])new FileAttribute[0]);
        try (final ZipInputStream input = IOHelper.newZipInput(resource)) {
            for (ZipEntry entry = input.getNextEntry(); entry != null; entry = input.getNextEntry()) {
                if (!entry.isDirectory()) {
                    IOHelper.transfer(input, target.resolve(IOHelper.toPath(entry.getName())));
                }
            }
        }
        return true;
    }
    
    public static boolean unpackZipNoCheck(final String resource, final Path target) throws IOException {
        try {
            if (Files.isDirectory(target, new LinkOption[0])) {
                return false;
            }
            Files.deleteIfExists(target);
            Files.createDirectory(target, (FileAttribute<?>[])new FileAttribute[0]);
            try (final ZipInputStream input = IOHelper.newZipInput(IOHelper.getResourceURL(resource))) {
                for (ZipEntry entry = input.getNextEntry(); entry != null; entry = input.getNextEntry()) {
                    if (!entry.isDirectory()) {
                        IOHelper.transfer(input, target.resolve(IOHelper.toPath(entry.getName())));
                    }
                }
            }
            return true;
        }
        catch (NoSuchFileException e) {
            return true;
        }
    }
}
