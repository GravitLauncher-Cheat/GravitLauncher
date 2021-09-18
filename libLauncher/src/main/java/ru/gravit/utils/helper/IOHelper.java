package ru.gravit.utils.helper;

import java.nio.file.FileVisitResult;
import java.nio.file.SimpleFileVisitor;
import java.util.Collections;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.FileSystems;
import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URISyntaxException;
import java.net.URI;
import java.util.regex.Matcher;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import java.awt.image.BufferedImage;
import java.nio.file.attribute.BasicFileAttributes;
import java.io.EOFException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.BufferedWriter;
import java.io.FileDescriptor;
import java.net.SocketException;
import java.net.Socket;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.util.zip.Inflater;
import java.util.zip.Deflater;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.io.ByteArrayOutputStream;
import java.nio.file.InvalidPathException;
import java.nio.file.DirectoryStream;
import java.nio.file.NoSuchFileException;
import ru.gravit.launcher.Launcher;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.file.Paths;
import java.nio.file.FileVisitor;
import java.nio.file.attribute.FileAttribute;
import java.io.IOException;
import java.nio.file.Files;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.OutputStream;
import java.io.InputStream;
import java.util.regex.Pattern;
import java.nio.file.FileVisitOption;
import java.util.Set;
import java.nio.file.CopyOption;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.FileSystem;
import ru.gravit.launcher.LauncherAPI;
import java.nio.charset.Charset;

public final class IOHelper
{
    @LauncherAPI
    public static final Charset UNICODE_CHARSET;
    @LauncherAPI
    public static final Charset ASCII_CHARSET;
    @LauncherAPI
    public static final int SOCKET_TIMEOUT;
    @LauncherAPI
    public static final int HTTP_TIMEOUT;
    @LauncherAPI
    public static final int BUFFER_SIZE;
    @LauncherAPI
    public static final String CROSS_SEPARATOR = "/";
    @LauncherAPI
    public static final FileSystem FS;
    @LauncherAPI
    public static final String PLATFORM_SEPARATOR;
    @LauncherAPI
    public static final boolean POSIX;
    @LauncherAPI
    public static final Path JVM_DIR;
    @LauncherAPI
    public static final Path HOME_DIR;
    @LauncherAPI
    public static final Path WORKING_DIR;
    private static final OpenOption[] READ_OPTIONS;
    private static final OpenOption[] WRITE_OPTIONS;
    private static final OpenOption[] APPEND_OPTIONS;
    private static final LinkOption[] LINK_OPTIONS;
    private static final CopyOption[] COPY_OPTIONS;
    private static final Set<FileVisitOption> WALK_OPTIONS;
    private static final Pattern CROSS_SEPARATOR_PATTERN;
    private static final Pattern PLATFORM_SEPARATOR_PATTERN;
    
    @LauncherAPI
    public static void close(final AutoCloseable closeable) {
        try {
            closeable.close();
        }
        catch (Exception exc) {
            LogHelper.error(exc);
        }
    }
    
    @LauncherAPI
    public static void close(final InputStream in) {
        try {
            in.close();
        }
        catch (Exception ex) {}
    }
    
    @LauncherAPI
    public static void close(final OutputStream out) {
        try {
            out.flush();
            out.close();
        }
        catch (Exception ex) {}
    }
    
    @LauncherAPI
    public static URL convertToURL(final String url) {
        try {
            return new URL(url);
        }
        catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL", e);
        }
    }
    
    @LauncherAPI
    public static void copy(final Path source, final Path target) throws IOException {
        createParentDirs(target);
        Files.copy(source, target, IOHelper.COPY_OPTIONS);
    }
    
    @LauncherAPI
    public static void createParentDirs(final Path path) throws IOException {
        final Path parent = path.getParent();
        if (parent != null && !isDir(parent)) {
            Files.createDirectories(parent, (FileAttribute<?>[])new FileAttribute[0]);
        }
    }
    
    @LauncherAPI
    public static String decode(final byte[] bytes) {
        return new String(bytes, IOHelper.UNICODE_CHARSET);
    }
    
    @LauncherAPI
    public static String decodeASCII(final byte[] bytes) {
        return new String(bytes, IOHelper.ASCII_CHARSET);
    }
    
    @LauncherAPI
    public static void deleteDir(final Path dir, final boolean self) throws IOException {
        walk(dir, new DeleteDirVisitor(dir, self), true);
    }
    
    @LauncherAPI
    public static byte[] encode(final String s) {
        return s.getBytes(IOHelper.UNICODE_CHARSET);
    }
    
    @LauncherAPI
    public static byte[] encodeASCII(final String s) {
        return s.getBytes(IOHelper.ASCII_CHARSET);
    }
    
    @LauncherAPI
    public static boolean exists(final Path path) {
        return Files.exists(path, IOHelper.LINK_OPTIONS);
    }
    
    @LauncherAPI
    public static Path getCodeSource(final Class<?> clazz) {
        return Paths.get(toURI(clazz.getProtectionDomain().getCodeSource().getLocation()));
    }
    
    @LauncherAPI
    public static String getFileName(final Path path) {
        return path.getFileName().toString();
    }
    
    @LauncherAPI
    public static String getIP(final SocketAddress address) {
        return ((InetSocketAddress)address).getAddress().getHostAddress();
    }
    
    @LauncherAPI
    public static byte[] getResourceBytes(final String name) throws IOException {
        return read(getResourceURL(name));
    }
    
    @LauncherAPI
    public static URL getResourceURL(final String name) throws NoSuchFileException {
        final URL url = Launcher.class.getResource('/' + name);
        if (url == null) {
            throw new NoSuchFileException(name);
        }
        return url;
    }
    
    @LauncherAPI
    public static boolean hasExtension(final Path file, final String extension) {
        return getFileName(file).endsWith('.' + extension);
    }
    
    @LauncherAPI
    public static boolean isDir(final Path path) {
        return Files.isDirectory(path, IOHelper.LINK_OPTIONS);
    }
    
    @LauncherAPI
    public static boolean isEmpty(final Path dir) throws IOException {
        try (final DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            return !stream.iterator().hasNext();
        }
    }
    
    @LauncherAPI
    public static boolean isFile(final Path path) {
        return Files.isRegularFile(path, IOHelper.LINK_OPTIONS);
    }
    
    @LauncherAPI
    public static boolean isValidFileName(final String fileName) {
        return !fileName.equals(".") && !fileName.equals("..") && fileName.chars().noneMatch(ch -> ch == 47 || ch == 92) && isValidPath(fileName);
    }
    
    @LauncherAPI
    public static boolean isValidPath(final String path) {
        try {
            toPath(path);
            return true;
        }
        catch (InvalidPathException ignored) {
            return false;
        }
    }
    
    @LauncherAPI
    public static boolean isValidTextureBounds(final int width, final int height, final boolean cloak) {
        return (width % 64 == 0 && (height << 1 == width || (!cloak && height == width)) && width <= 1024) || (cloak && width % 22 == 0 && height % 17 == 0 && width / 22 == height / 17);
    }
    
    @LauncherAPI
    public static void move(final Path source, final Path target) throws IOException {
        createParentDirs(target);
        Files.move(source, target, IOHelper.COPY_OPTIONS);
    }
    
    @LauncherAPI
    public static byte[] newBuffer() {
        return new byte[IOHelper.BUFFER_SIZE];
    }
    
    @LauncherAPI
    public static ByteArrayOutputStream newByteArrayOutput() {
        return new ByteArrayOutputStream();
    }
    
    @LauncherAPI
    public static char[] newCharBuffer() {
        return new char[IOHelper.BUFFER_SIZE];
    }
    
    @LauncherAPI
    public static URLConnection newConnection(final URL url) throws IOException {
        final URLConnection connection = url.openConnection();
        if (connection instanceof HttpURLConnection) {
            connection.setReadTimeout(IOHelper.HTTP_TIMEOUT);
            connection.setConnectTimeout(IOHelper.HTTP_TIMEOUT);
            connection.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
        }
        else {
            connection.setUseCaches(false);
        }
        connection.setDoInput(true);
        connection.setDoOutput(false);
        return connection;
    }
    
    @LauncherAPI
    public static HttpURLConnection newConnectionPost(final URL url) throws IOException {
        final HttpURLConnection connection = (HttpURLConnection)newConnection(url);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        return connection;
    }
    
    @LauncherAPI
    public static Deflater newDeflater() {
        final Deflater deflater = new Deflater(-1, true);
        deflater.setStrategy(0);
        return deflater;
    }
    
    @LauncherAPI
    public static Inflater newInflater() {
        return new Inflater(true);
    }
    
    @LauncherAPI
    public static InputStream newInput(final Path file) throws IOException {
        return Files.newInputStream(file, IOHelper.READ_OPTIONS);
    }
    
    @LauncherAPI
    public static InputStream newBufferedInput(final Path file) throws IOException {
        return new BufferedInputStream(Files.newInputStream(file, IOHelper.READ_OPTIONS));
    }
    
    @LauncherAPI
    public static InputStream newInput(final URL url) throws IOException {
        return newConnection(url).getInputStream();
    }
    
    @LauncherAPI
    public static BufferedInputStream newBufferedInput(final URL url) throws IOException {
        return new BufferedInputStream(newConnection(url).getInputStream());
    }
    
    @LauncherAPI
    public static OutputStream newOutput(final Path file) throws IOException {
        return newOutput(file, false);
    }
    
    @LauncherAPI
    public static OutputStream newBufferedOutput(final Path file) throws IOException {
        return newBufferedOutput(file, false);
    }
    
    @LauncherAPI
    public static OutputStream newOutput(final Path file, final boolean append) throws IOException {
        createParentDirs(file);
        return Files.newOutputStream(file, append ? IOHelper.APPEND_OPTIONS : IOHelper.WRITE_OPTIONS);
    }
    
    @LauncherAPI
    public static OutputStream newBufferedOutput(final Path file, final boolean append) throws IOException {
        createParentDirs(file);
        return new BufferedOutputStream(Files.newOutputStream(file, append ? IOHelper.APPEND_OPTIONS : IOHelper.WRITE_OPTIONS));
    }
    
    @LauncherAPI
    public static BufferedReader newReader(final InputStream input) {
        return newReader(input, IOHelper.UNICODE_CHARSET);
    }
    
    @LauncherAPI
    public static BufferedReader newReader(final InputStream input, final Charset charset) {
        return new BufferedReader(new InputStreamReader(input, charset));
    }
    
    @LauncherAPI
    public static BufferedReader newReader(final Path file) throws IOException {
        return Files.newBufferedReader(file, IOHelper.UNICODE_CHARSET);
    }
    
    @LauncherAPI
    public static BufferedReader newReader(final URL url) throws IOException {
        final URLConnection connection = newConnection(url);
        final String charset = connection.getContentEncoding();
        return newReader(connection.getInputStream(), (charset == null) ? IOHelper.UNICODE_CHARSET : Charset.forName(charset));
    }
    
    @LauncherAPI
    public static Socket newSocket() throws SocketException {
        final Socket socket = new Socket();
        setSocketFlags(socket);
        return socket;
    }
    
    @LauncherAPI
    public static BufferedWriter newWriter(final FileDescriptor fd) {
        return newWriter(new FileOutputStream(fd));
    }
    
    @LauncherAPI
    public static BufferedWriter newWriter(final OutputStream output) {
        return new BufferedWriter(new OutputStreamWriter(output, IOHelper.UNICODE_CHARSET));
    }
    
    @LauncherAPI
    public static BufferedWriter newWriter(final Path file) throws IOException {
        return newWriter(file, false);
    }
    
    @LauncherAPI
    public static BufferedWriter newWriter(final Path file, final boolean append) throws IOException {
        createParentDirs(file);
        return Files.newBufferedWriter(file, IOHelper.UNICODE_CHARSET, append ? IOHelper.APPEND_OPTIONS : IOHelper.WRITE_OPTIONS);
    }
    
    @LauncherAPI
    public static ZipEntry newZipEntry(final String name) {
        final ZipEntry entry = new ZipEntry(name);
        entry.setTime(0L);
        return entry;
    }
    
    @LauncherAPI
    public static ZipEntry newZipEntry(final ZipEntry entry) {
        return newZipEntry(entry.getName());
    }
    
    @LauncherAPI
    public static ZipInputStream newZipInput(final InputStream input) {
        return new ZipInputStream(input, IOHelper.UNICODE_CHARSET);
    }
    
    @LauncherAPI
    public static ZipInputStream newZipInput(final Path file) throws IOException {
        return newZipInput(newInput(file));
    }
    
    @LauncherAPI
    public static ZipInputStream newZipInput(final URL url) throws IOException {
        return newZipInput(newInput(url));
    }
    
    @LauncherAPI
    public static byte[] read(final InputStream input) throws IOException {
        try (final ByteArrayOutputStream output = newByteArrayOutput()) {
            transfer(input, output);
            return output.toByteArray();
        }
    }
    
    @LauncherAPI
    public static void read(final InputStream input, final byte[] bytes) throws IOException {
        int length;
        for (int offset = 0; offset < bytes.length; offset += length) {
            length = input.read(bytes, offset, bytes.length - offset);
            if (length < 0) {
                throw new EOFException(String.format("%d bytes remaining", bytes.length - offset));
            }
        }
    }
    
    @LauncherAPI
    public static byte[] read(final Path file) throws IOException {
        final long size = readAttributes(file).size();
        if (size > 2147483647L) {
            throw new IOException("File too big");
        }
        final byte[] bytes = new byte[(int)size];
        try (final InputStream input = newInput(file)) {
            read(input, bytes);
        }
        return bytes;
    }
    
    @LauncherAPI
    public static byte[] read(final URL url) throws IOException {
        try (final InputStream input = newInput(url)) {
            return read(input);
        }
    }
    
    @LauncherAPI
    public static BasicFileAttributes readAttributes(final Path path) throws IOException {
        return Files.readAttributes(path, BasicFileAttributes.class, IOHelper.LINK_OPTIONS);
    }
    
    @LauncherAPI
    public static BufferedImage readTexture(final Object input, final boolean cloak) throws IOException {
        final ImageReader reader = ImageIO.getImageReadersByMIMEType("image/png").next();
        try {
            reader.setInput(ImageIO.createImageInputStream(input), false, false);
            final int width = reader.getWidth(0);
            final int height = reader.getHeight(0);
            if (!isValidTextureBounds(width, height, cloak)) {
                throw new IOException(String.format("Invalid texture bounds: %dx%d", width, height));
            }
            return reader.read(0);
        }
        finally {
            reader.dispose();
        }
    }
    
    @LauncherAPI
    public static String request(final URL url) throws IOException {
        return decode(read(url)).trim();
    }
    
    @LauncherAPI
    public static InetSocketAddress resolve(final InetSocketAddress address) {
        if (address.isUnresolved()) {
            return new InetSocketAddress(address.getHostString(), address.getPort());
        }
        return address;
    }
    
    @LauncherAPI
    public static Path resolveIncremental(final Path dir, final String name, final String extension) {
        final Path original = dir.resolve(name + '.' + extension);
        if (!exists(original)) {
            return original;
        }
        int counter = 1;
        Path path;
        while (true) {
            path = dir.resolve(String.format("%s (%d).%s", name, counter, extension));
            if (!exists(path)) {
                break;
            }
            ++counter;
        }
        return path;
    }
    
    @LauncherAPI
    public static Path resolveJavaBin(final Path javaDir) {
        final Path javaBinDir = ((javaDir == null) ? IOHelper.JVM_DIR : javaDir).resolve("bin");
        if (!LogHelper.isDebugEnabled()) {
            final Path javawExe = javaBinDir.resolve("javaw.exe");
            if (isFile(javawExe)) {
                return javawExe;
            }
        }
        final Path javaExe = javaBinDir.resolve("java.exe");
        if (isFile(javaExe)) {
            return javaExe;
        }
        final Path java = javaBinDir.resolve("java");
        if (isFile(java)) {
            return java;
        }
        throw new RuntimeException("Java binary wasn't found");
    }
    
    @LauncherAPI
    public static void setSocketFlags(final Socket socket) throws SocketException {
        socket.setKeepAlive(false);
        socket.setTcpNoDelay(false);
        socket.setReuseAddress(true);
        socket.setSoTimeout(IOHelper.SOCKET_TIMEOUT);
        socket.setTrafficClass(28);
        socket.setPerformancePreferences(1, 0, 2);
    }
    
    @LauncherAPI
    public static String toAbsPathString(final Path path) {
        return toAbsPath(path).toFile().getAbsolutePath();
    }
    
    @LauncherAPI
    public static Path toAbsPath(final Path path) {
        return path.normalize().toAbsolutePath();
    }
    
    @LauncherAPI
    public static byte[] toByteArray(final InputStream in) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream(in.available());
        transfer(in, out);
        return out.toByteArray();
    }
    
    @LauncherAPI
    public static Path toPath(final String path) {
        return Paths.get(IOHelper.CROSS_SEPARATOR_PATTERN.matcher(path).replaceAll(Matcher.quoteReplacement(IOHelper.PLATFORM_SEPARATOR)), new String[0]);
    }
    
    @LauncherAPI
    public static String toString(final Path path) {
        return IOHelper.PLATFORM_SEPARATOR_PATTERN.matcher(path.toString()).replaceAll(Matcher.quoteReplacement("/"));
    }
    
    @LauncherAPI
    public static URI toURI(final URL url) {
        try {
            return url.toURI();
        }
        catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }
    
    @LauncherAPI
    public static URL toURL(final Path path) {
        try {
            return path.toUri().toURL();
        }
        catch (MalformedURLException e) {
            throw new InternalError(e);
        }
    }
    
    @LauncherAPI
    public static void transfer(final byte[] write, final Path file, final boolean append) throws IOException {
        try (final OutputStream out = newOutput(file, append)) {
            out.write(write);
        }
    }
    
    @LauncherAPI
    public static long transfer(final InputStream input, final OutputStream output) throws IOException {
        long transferred = 0L;
        final byte[] buffer = newBuffer();
        for (int length = input.read(buffer); length >= 0; length = input.read(buffer)) {
            output.write(buffer, 0, length);
            transferred += length;
        }
        return transferred;
    }
    
    @LauncherAPI
    public static long transfer(final InputStream input, final Path file) throws IOException {
        return transfer(input, file, false);
    }
    
    @LauncherAPI
    public static long transfer(final InputStream input, final Path file, final boolean append) throws IOException {
        try (final OutputStream output = newOutput(file, append)) {
            return transfer(input, output);
        }
    }
    
    @LauncherAPI
    public static void transfer(final Path file, final OutputStream output) throws IOException {
        try (final InputStream input = newInput(file)) {
            transfer(input, output);
        }
    }
    
    @LauncherAPI
    public static String urlDecode(final String s) {
        try {
            return URLDecoder.decode(s, IOHelper.UNICODE_CHARSET.name());
        }
        catch (UnsupportedEncodingException e) {
            throw new InternalError(e);
        }
    }
    
    @LauncherAPI
    public static String urlEncode(final String s) {
        try {
            return URLEncoder.encode(s, IOHelper.UNICODE_CHARSET.name());
        }
        catch (UnsupportedEncodingException e) {
            throw new InternalError(e);
        }
    }
    
    @LauncherAPI
    public static String verifyFileName(final String fileName) {
        return VerifyHelper.verify(fileName, IOHelper::isValidFileName, String.format("Invalid file name: '%s'", fileName));
    }
    
    @LauncherAPI
    public static int verifyLength(final int length, final int max) throws IOException {
        if (length < 0 || (max < 0 && length != -max) || (max > 0 && length > max)) {
            throw new IOException("Illegal length: " + length);
        }
        return length;
    }
    
    @LauncherAPI
    public static BufferedImage verifyTexture(final BufferedImage skin, final boolean cloak) {
        return VerifyHelper.verify(skin, i -> isValidTextureBounds(i.getWidth(), i.getHeight(), cloak), String.format("Invalid texture bounds: %dx%d", skin.getWidth(), skin.getHeight()));
    }
	
	@LauncherAPI
    public static String verifyURL(String url) {
        try {
            new URL(url).toURI();
            return url;
        } catch (MalformedURLException | URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL", e);
        }
    }
   
    @LauncherAPI
    public static void walk(final Path dir, final FileVisitor<Path> visitor, final boolean hidden) throws IOException {
        Files.walkFileTree(dir, IOHelper.WALK_OPTIONS, Integer.MAX_VALUE, hidden ? visitor : new SkipHiddenVisitor((FileVisitor)visitor));
    }
    
    @LauncherAPI
    public static void write(final Path file, final byte[] bytes) throws IOException {
        createParentDirs(file);
        Files.write(file, bytes, IOHelper.WRITE_OPTIONS);
    }
    
    private IOHelper() {
    }
    
    static {
        UNICODE_CHARSET = StandardCharsets.UTF_8;
        ASCII_CHARSET = StandardCharsets.US_ASCII;
        SOCKET_TIMEOUT = VerifyHelper.verifyInt(Integer.parseUnsignedInt(System.getProperty("launcher.socketTimeout", Integer.toString(30000))), VerifyHelper.POSITIVE, "launcher.socketTimeout can't be <= 0");
        HTTP_TIMEOUT = VerifyHelper.verifyInt(Integer.parseUnsignedInt(System.getProperty("launcher.httpTimeout", Integer.toString(5000))), VerifyHelper.POSITIVE, "launcher.httpTimeout can't be <= 0");
        BUFFER_SIZE = VerifyHelper.verifyInt(Integer.parseUnsignedInt(System.getProperty("launcher.bufferSize", Integer.toString(4096))), VerifyHelper.POSITIVE, "launcher.bufferSize can't be <= 0");
        FS = FileSystems.getDefault();
        PLATFORM_SEPARATOR = IOHelper.FS.getSeparator();
        POSIX = (IOHelper.FS.supportedFileAttributeViews().contains("posix") || IOHelper.FS.supportedFileAttributeViews().contains("Posix"));
        JVM_DIR = Paths.get(System.getProperty("java.home"), new String[0]);
        HOME_DIR = Paths.get(System.getProperty("user.home"), new String[0]);
        WORKING_DIR = Paths.get(System.getProperty("user.dir"), new String[0]);
        READ_OPTIONS = new OpenOption[] { StandardOpenOption.READ };
        WRITE_OPTIONS = new OpenOption[] { StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING };
        APPEND_OPTIONS = new OpenOption[] { StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND };
        LINK_OPTIONS = new LinkOption[0];
        COPY_OPTIONS = new CopyOption[] { StandardCopyOption.REPLACE_EXISTING };
        WALK_OPTIONS = Collections.singleton(FileVisitOption.FOLLOW_LINKS);
        CROSS_SEPARATOR_PATTERN = Pattern.compile("/", 16);
        PLATFORM_SEPARATOR_PATTERN = Pattern.compile(IOHelper.PLATFORM_SEPARATOR, 16);
    }
    
    private static final class DeleteDirVisitor extends SimpleFileVisitor<Path>
    {
        private final Path dir;
        private final boolean self;
        
        private DeleteDirVisitor(final Path dir, final boolean self) {
            this.dir = dir;
            this.self = self;
        }
        
        @Override
        public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
            final FileVisitResult result = super.postVisitDirectory(dir, exc);
            if (this.self || !this.dir.equals(dir)) {
                Files.delete(dir);
            }
            return result;
        }
        
        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return super.visitFile(file, attrs);
        }
    }
    
    private static final class SkipHiddenVisitor implements FileVisitor<Path>
    {
        private final FileVisitor<Path> visitor;
        
        private SkipHiddenVisitor(final FileVisitor<Path> visitor) {
            this.visitor = visitor;
        }
        
        @Override
        public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
            return Files.isHidden(dir) ? FileVisitResult.CONTINUE : this.visitor.postVisitDirectory(dir, exc);
        }
        
        @Override
        public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
            return Files.isHidden(dir) ? FileVisitResult.SKIP_SUBTREE : this.visitor.preVisitDirectory(dir, attrs);
        }
        
        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
            return Files.isHidden(file) ? FileVisitResult.CONTINUE : this.visitor.visitFile(file, attrs);
        }
        
        @Override
        public FileVisitResult visitFileFailed(final Path file, final IOException exc) throws IOException {
            return this.visitor.visitFileFailed(file, exc);
        }
    }
}
