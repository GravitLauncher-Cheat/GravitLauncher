package ru.gravit.launcher;

import ru.gravit.launcher.AutogenConfig;
import ru.gravit.launcher.Launcher;
import ru.gravit.launcher.LauncherConfig;
import ru.gravit.launcher.serialize.HOutput;
import ru.gravit.utils.helper.IOHelper;
import ru.gravit.utils.helper.LogHelper;
import ru.gravit.utils.helper.SecurityHelper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.io.*;

final class RuntimeDirVisitor extends SimpleFileVisitor<Path> {
    private final ZipOutputStream output;
    private final Map<String, byte[]> runtime;

    private RuntimeDirVisitor(ZipOutputStream output, Map<String, byte[]> runtime) {
        this.output = output;
        this.runtime = runtime;
    }
}

