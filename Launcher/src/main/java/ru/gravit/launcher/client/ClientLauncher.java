package ru.gravit.launcher.client;

import ru.gravit.launcher.*;
import ru.gravit.launcher.serialize.stream.StreamObject;

import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import com.google.gson.GsonBuilder;
import java.util.function.Function;

import ru.gravit.launcher.hasher.FileNameMatcher;
import java.net.URL;
import ru.gravit.launcher.hasher.DirWatcher;
import ru.gravit.launcher.request.Request;
import ru.gravit.launcher.serialize.HInput;
import ru.gravit.launcher.gui.JSRuntimeProvider;

import java.net.Socket;
import ru.gravit.utils.helper.EnvHelper;
import ru.gravit.utils.helper.IOHelper;
import ru.gravit.utils.helper.CommonHelper;
import java.io.IOException;
import ru.gravit.launcher.serialize.HOutput;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import ru.gravit.launcher.hasher.HashedDir;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles;
import java.util.LinkedList;
import javax.swing.JOptionPane;
import ru.gravit.utils.helper.LogHelper;
import ru.gravit.utils.helper.JVMHelper;
import ru.gravit.launcher.profiles.PlayerProfile;
import ru.gravit.utils.helper.SecurityHelper;

import java.util.Collections;
import ru.gravit.launcher.profiles.ClientProfile;
import java.util.Collection;
import ru.gravit.utils.PublicURLClassLoader;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

import com.google.gson.Gson;

public final class ClientLauncher
{
    private static Gson gson;
    private static final String[] EMPTY_ARRAY;
    private static final String SOCKET_HOST = "127.0.0.1";
    private static final int SOCKET_PORT;
    private static final String MAGICAL_INTEL_OPTION = "-XX:HeapDumpPath=ThisTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump";
    private static final boolean isUsingWrapper;
    private static final boolean isDownloadJava;
    private static Path JavaBinPath;
    private static final Set<PosixFilePermission> BIN_POSIX_PERMISSIONS;
    private static final Path NATIVES_DIR;
    private static final Path RESOURCEPACKS_DIR;
    private static PublicURLClassLoader classLoader;
    private static Process process;
    private static boolean clientStarted;
    private static Thread writeParamsThread;
    
    public static boolean isDownloadJava() {
        return ClientLauncher.isDownloadJava;
    }
    
    public static Path getJavaBinPath() {
        return ClientLauncher.JavaBinPath;
    }
    
    private static void addClientArgs(final Collection<String> args, final ClientProfile profile, final Params params) {
        final PlayerProfile pp = params.pp;
        final ClientProfile.Version version = profile.getVersion();
        Collections.addAll(args, new String[] { "--username", pp.username });
        if (version.compareTo(ClientProfile.Version.MC172) >= 0) {
            Collections.addAll(args, new String[] { "--uuid", Launcher.toHash(pp.uuid) });
            Collections.addAll(args, new String[] { "--accessToken", params.accessToken });
            if (version.compareTo(ClientProfile.Version.MC1710) >= 0) {
                Collections.addAll(args, new String[] { "--userType", "mojang" });
                final ClientUserProperties properties = new ClientUserProperties();
                if (pp.skin != null) {
                    properties.skinURL = new String[] { pp.skin.url };
                    properties.skinDigest = new String[] { SecurityHelper.toHex(pp.skin.digest) };
                }
                if (pp.cloak != null) {
                    properties.cloakURL = new String[] { pp.cloak.url };
                    properties.cloakDigest = new String[] { SecurityHelper.toHex(pp.cloak.digest) };
                }
                Collections.addAll(args, new String[] { "--userProperties", ClientLauncher.gson.toJson(properties) });
                Collections.addAll(args, new String[] { "--assetIndex", profile.getAssetIndex() });
            }
        }
        else {
            Collections.addAll(args, new String[] { "--session", params.accessToken });
        }
        Collections.addAll(args, new String[] { "--version", profile.getVersion().name });
        Collections.addAll(args, new String[] { "--gameDir", params.clientDir.toString() });
        Collections.addAll(args, new String[] { "--assetsDir", params.assetDir.toString() });
        Collections.addAll(args, new String[] { "--resourcePackDir", params.clientDir.resolve(ClientLauncher.RESOURCEPACKS_DIR).toString() });
        if (version.compareTo(ClientProfile.Version.MC194) >= 0) {
            Collections.addAll(args, new String[] { "--versionType", "Launcher v" + Launcher.getVersion().getVersionString() });
        }
        if (params.autoEnter) {
            Collections.addAll(args, new String[] { "--server", profile.getServerAddress() });
            Collections.addAll(args, new String[] { "--port", Integer.toString(profile.getServerPort()) });
        }
        profile.pushOptionalClientArgs(args);
        if (params.fullScreen) {
            Collections.addAll(args, new String[] { "--fullscreen", Boolean.toString(true) });
        }
        if (params.width > 0 && params.height > 0) {
            Collections.addAll(args, new String[] { "--width", Integer.toString(params.width) });
            Collections.addAll(args, new String[] { "--height", Integer.toString(params.height) });
        }
        if (LauncherConfig.config.liteloader) {
            Collections.addAll(args, new String[]{"--tweakClass", "com.mumfrey.liteloader.launch.LiteLoaderTweaker"});
        }
    }
    
    @LauncherAPI
    public static void setJavaBinPath(final Path javaBinPath) {
        ClientLauncher.JavaBinPath = javaBinPath;
    }
    
    private static void addClientLegacyArgs(final Collection<String> args, final ClientProfile profile, final Params params) {
        args.add(params.pp.username);
        args.add(params.accessToken);
        Collections.addAll(args, new String[] { "--version", profile.getVersion().name });
        Collections.addAll(args, new String[] { "--gameDir", params.clientDir.toString() });
        Collections.addAll(args, new String[] { "--assetsDir", params.assetDir.toString() });
    }
    
    @LauncherAPI
    public static void checkJVMBitsAndVersion() {
        if (JVMHelper.JVM_BITS != JVMHelper.OS_BITS) {
            final String error = String.format("\u0423 \u0412\u0430\u0441 \u0443\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d\u0430 Java %d, \u043d\u043e \u0412\u0430\u0448\u0430 \u0441\u0438\u0441\u0442\u0435\u043c\u0430 \u043e\u043f\u0440\u0435\u0434\u0435\u043b\u0435\u043d\u0430 \u043a\u0430\u043a %d. \u0423\u0441\u0442\u0430\u043d\u043e\u0432\u0438\u0442\u0435 Java \u043f\u0440\u0430\u0432\u0438\u043b\u044c\u043d\u043e\u0439 \u0440\u0430\u0437\u0440\u044f\u0434\u043d\u043e\u0441\u0442\u0438", JVMHelper.JVM_BITS, JVMHelper.OS_BITS);
            LogHelper.error(error);
            if (Launcher.getConfig().isWarningMissArchJava) {
                JOptionPane.showMessageDialog(null, error);
            }
        }
        final String jvmVersion = JVMHelper.RUNTIME_MXBEAN.getVmVersion();
        LogHelper.info(jvmVersion);
        if (jvmVersion.startsWith("10.") || jvmVersion.startsWith("9.") || jvmVersion.startsWith("11.")) {
            final String error2 = String.format("\u0423 \u0412\u0430\u0441 \u0443\u0441\u0442\u0430\u043d\u043e\u0432\u043b\u0435\u043d\u0430 Java %s. \u0414\u043b\u044f \u043f\u0440\u0430\u0432\u0438\u043b\u044c\u043d\u043e\u0439 \u0440\u0430\u0431\u043e\u0442\u044b \u043d\u0435\u043e\u0431\u0445\u043e\u0434\u0438\u043c\u0430 Java 8", JVMHelper.RUNTIME_MXBEAN.getVmVersion());
            LogHelper.error(error2);
            if (Launcher.getConfig().isWarningMissArchJava) {
                JOptionPane.showMessageDialog(null, error2);
            }
        }
    }
    
    @LauncherAPI
    public static boolean isLaunched() {
        return Launcher.LAUNCHED.get();
    }
    
    public static boolean isUsingWrapper() {
        return JVMHelper.OS_TYPE == JVMHelper.OS.MUSTDIE && ClientLauncher.isUsingWrapper;
    }
    
    private static void launch(final ClientProfile profile, final Params params) throws Throwable {
        final Collection<String> args = new LinkedList<String>();
        if (profile.getVersion().compareTo(ClientProfile.Version.MC164) >= 0) {
            addClientArgs(args, profile, params);
        }
        else {
            addClientLegacyArgs(args, profile, params);
        }
        Collections.addAll(args, profile.getClientArgs());
        LogHelper.debug("Args: " + args);
        final Class<?> mainClass = ClientLauncher.classLoader.loadClass(profile.getMainClass());
        final MethodHandle mainMethod = MethodHandles.publicLookup().findStatic(mainClass, "main", MethodType.methodType(Void.TYPE, String[].class));
        Launcher.LAUNCHED.set(true);
        JVMHelper.fullGC();
        System.setProperty("minecraft.applet.TargetDirectory", params.clientDir.toString());
        mainMethod.invoke((Object)args.toArray(ClientLauncher.EMPTY_ARRAY));
    }
    
    @LauncherAPI
    public static Process launch(final HashedDir assetHDir, final HashedDir clientHDir, final ClientProfile profile, final Params params, final boolean pipeOutput) throws Throwable {
        LogHelper.debug("Writing ClientLauncher params");
        final ClientLauncherContext context = new ClientLauncherContext();
        ClientLauncher.clientStarted = false;
        if (ClientLauncher.writeParamsThread != null && ClientLauncher.writeParamsThread.isAlive()) {
            ClientLauncher.writeParamsThread.interrupt();
        }
        final Throwable t2;
        final Throwable t4;
        (ClientLauncher.writeParamsThread = CommonHelper.newThread("Client params writter", true, () -> {
            try {
                ServerSocket socket = new ServerSocket();
                try {
                    socket.setReuseAddress(true);
                    socket.setSoTimeout(30000);
                    socket.bind(new InetSocketAddress("127.0.0.1", ClientLauncher.SOCKET_PORT));
                    Socket client = socket.accept();
                    if (ClientLauncher.process == null) {
                        LogHelper.error("Process is null");
                    }
                    else if (!ClientLauncher.process.isAlive()) {
                        LogHelper.error("Process is not alive");
                        JOptionPane.showMessageDialog(null, "Client Process crashed", "Launcher", 0);
                    }
                    else {
                        HOutput output = new HOutput(client.getOutputStream());
                        try {
                            params.write(output);
                            output.writeString(Launcher.gson.toJson(profile), 0);
                            assetHDir.write(output);
                            clientHDir.write(output);
                        }
                        catch (Throwable t) {
                            throw t;
                        }
                        finally {
                            if (output != null) {
                                output.close();
                            }
                        }
                        ClientLauncher.clientStarted = true;
                    }
                }
                catch (Throwable t3) {
                    throw t3;
                }
                finally {
                    if (socket != null) {
                            socket.close();
                    }
                }
            }
            catch (IOException e) {
                LogHelper.error(e);
            }
            return;
        })).start();
        checkJVMBitsAndVersion();
        LogHelper.debug("Resolving JVM binary");
        final Path javaBin;
        if (JVMHelper.OS_TYPE == JVMHelper.OS.MUSTDIE) {
            javaBin = IOHelper.resolveJavaBin(ClientLauncher.getJavaBinPath());
        } else {
            javaBin = IOHelper.resolveJavaBin(Paths.get(System.getProperty("java.home"), new String[0]));
        }
        context.javaBin = javaBin;
        context.clientProfile = profile;
        context.playerProfile = params.pp;
        context.args.add(javaBin.toString());
        context.args.add("-XX:HeapDumpPath=ThisTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump");
        if (params.ram > 0 && params.ram <= JVMHelper.RAM) {
            context.args.add("-Xms" + params.ram + 'M');
            context.args.add("-Xmx" + params.ram + 'M');
        }
        context.args.add(JVMHelper.jvmProperty("launcher.debug", Boolean.toString(LogHelper.isDebugEnabled())));
        context.args.add(JVMHelper.jvmProperty("launcher.stacktrace", Boolean.toString(LogHelper.isStacktraceEnabled())));
        context.args.add(JVMHelper.jvmProperty("launcher.dev", Boolean.toString(LogHelper.isDevEnabled())));
        JVMHelper.addSystemPropertyToArgs(context.args, "launcher.customdir");
        JVMHelper.addSystemPropertyToArgs(context.args, "launcher.usecustomdir");
        JVMHelper.addSystemPropertyToArgs(context.args, "launcher.useoptdir");
        if (JVMHelper.OS_TYPE == JVMHelper.OS.MUSTDIE && JVMHelper.OS_VERSION.startsWith("10.")) {
            LogHelper.debug("MustDie 10 fix is applied");
            context.args.add(JVMHelper.jvmProperty("os.name", "Windows 10"));
            context.args.add(JVMHelper.jvmProperty("os.version", "10.0"));
        }
        final String pathLauncher = IOHelper.getCodeSource(ClientLauncher.class).toString();
        context.pathLauncher = pathLauncher;
        Collections.addAll(context.args, profile.getJvmArgs());
        profile.pushOptionalJvmArgs(context.args);
        Collections.addAll(context.args, new String[] { "-Djava.library.path=".concat(params.clientDir.resolve(ClientLauncher.NATIVES_DIR).toString()) });
        Collections.addAll(context.args, new String[] { "-javaagent:".concat(pathLauncher) });
        Collections.addAll(context.args, new String[] { ClientLauncher.class.getName() });
        LogHelper.debug("Commandline: " + context.args);
        LogHelper.debug("Launching client instance");
        final ProcessBuilder builder = new ProcessBuilder(context.args);
        context.builder = builder;
        EnvHelper.addEnv(builder);
        builder.directory(params.clientDir.toFile());
        builder.inheritIO();
        if (pipeOutput) {
            builder.redirectErrorStream(true);
            builder.redirectOutput(ProcessBuilder.Redirect.PIPE);
        }
        ClientLauncher.process = builder.start();
        if (!LogHelper.isDebugEnabled()) {
            int i = 0;
            while (i < 50) {
                if (!ClientLauncher.process.isAlive()) {
                    final int exitCode = ClientLauncher.process.exitValue();
                    LogHelper.error("Process exit code %d", exitCode);
                    if (ClientLauncher.writeParamsThread != null && ClientLauncher.writeParamsThread.isAlive()) {
                        ClientLauncher.writeParamsThread.interrupt();
                        break;
                    }
                    break;
                }
                else {
                    if (ClientLauncher.clientStarted) {
                        break;
                    }
                    Thread.sleep(200L);
                    ++i;
                }
            }
            if (!ClientLauncher.clientStarted) {
                LogHelper.error("Write Client Params not successful. Using debug mode for more information");
            }
        }
        ClientLauncher.clientStarted = false;
        return ClientLauncher.process;
    }
    
    @LauncherAPI
    public static void main(final String... args) throws Throwable {
        final LauncherEngine engine = LauncherEngine.clientInstance();
        Launcher.modulesManager = new ClientModuleManager(engine);
        LauncherConfig.getAutogenConfig().initModules();
        initGson();
        Launcher.modulesManager.preInitModules();
        JVMHelper.verifySystemProperties(ClientLauncher.class, true);
        EnvHelper.checkDangerousParams();
        JVMHelper.checkStackTrace(ClientLauncher.class);
        LogHelper.printVersion("Client Launcher");
        if (engine.runtimeProvider == null) {
            engine.runtimeProvider = new JSRuntimeProvider();
        }
        engine.runtimeProvider.init(true);
        engine.runtimeProvider.preLoad();
        LogHelper.debug("Reading ClientLauncher params");
        Params params;
        ClientProfile profile;
        HashedDir assetHDir;
        HashedDir clientHDir;
        try (final Socket socket = IOHelper.newSocket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", ClientLauncher.SOCKET_PORT));
            try (final HInput input = new HInput(socket.getInputStream())) {
                params = new Params(input);
                profile = ClientLauncher.gson.fromJson(input.readString(0), ClientProfile.class);
                assetHDir = new HashedDir(input);
                clientHDir = new HashedDir(input);
            }
        }
        catch (IOException ex) {
            LogHelper.error(ex);
            System.exit(-98);
            return;
        }
        Launcher.profile = profile;
        Request.setSession(params.session);
        checkJVMBitsAndVersion();
        Launcher.modulesManager.initModules();
        LogHelper.debug("Verifying ClientLauncher sign and classpath");
        final LinkedList<Path> classPath = resolveClassPathList(params.clientDir, profile.getClassPath());
        if (LauncherConfig.config.liteloader) {
            LauncherAgent.addJVMClassPath(IOHelper.getCodeSource(Launcher.class).getParent().resolve("liteloader-" + profile.getVersion() + ".jar").toString().replace("Minecraft ", ""));
        }
        for (final Path classpathURL : classPath) {
            LauncherAgent.addJVMClassPath(classpathURL.toAbsolutePath().toString());
        }
        profile.pushOptionalClassPath(cp -> {
            LinkedList<Path> optionalClassPath = resolveClassPathList(params.clientDir, cp);
            optionalClassPath.iterator();
            return;
        });
        final URL[] classpathurls = resolveClassPath(params.clientDir, profile.getClassPath());
        ClientLauncher.classLoader = new PublicURLClassLoader(classpathurls, ClassLoader.getSystemClassLoader());
        Thread.currentThread().setContextClassLoader(ClientLauncher.classLoader);
        ClientLauncher.classLoader.nativePath = params.clientDir.resolve(ClientLauncher.NATIVES_DIR).toString();
        PublicURLClassLoader.systemclassloader = ClientLauncher.classLoader;
        final boolean digest = !profile.isUpdateFastCheck();
        LogHelper.debug("Starting JVM and client WatchService");
        final FileNameMatcher assetMatcher = profile.getAssetUpdateMatcher();
        final FileNameMatcher clientMatcher = profile.getClientUpdateMatcher();
        try (final DirWatcher assetWatcher = new DirWatcher(params.assetDir, assetHDir, assetMatcher, digest);
             final DirWatcher clientWatcher = new DirWatcher(params.clientDir, clientHDir, clientMatcher, digest)) {
            Launcher.profile.pushOptionalFile(clientHDir, false);
            Launcher.modulesManager.postInitModules();
            CommonHelper.newThread("Asset Directory Watcher", true, assetWatcher).start();
            CommonHelper.newThread("Client Directory Watcher", true, clientWatcher).start();
            verifyHDir(params.assetDir, assetHDir, assetMatcher, digest);
            verifyHDir(params.clientDir, clientHDir, clientMatcher, digest);
            launch(profile, params);
        }
    }
    
    private static URL[] resolveClassPath(final Path clientDir, final String... classPath) throws IOException {
        final Collection<Path> result = new LinkedList<Path>();
        for (final String classPathEntry : classPath) {
            final Path path = clientDir.resolve(IOHelper.toPath(classPathEntry));
            if (IOHelper.isDir(path)) {
                IOHelper.walk(path, new ClassPathFileVisitor((Collection)result), false);
            }
            else {
                result.add(path);
            }
        }
        return result.stream().map((Function<? super Path, ?>)IOHelper::toURL).toArray(URL[]::new);
    }
    
    private static LinkedList<Path> resolveClassPathList(final Path clientDir, final String... classPath) throws IOException {
        final LinkedList<Path> result = new LinkedList<Path>();
        for (final String classPathEntry : classPath) {
            final Path path = clientDir.resolve(IOHelper.toPath(classPathEntry));
            if (IOHelper.isDir(path)) {
                IOHelper.walk(path, new ClassPathFileVisitor((Collection)result), false);
            }
            else {
                result.add(path);
            }
        }
        return result;
    }
    
    public static void initGson() {
        if (Launcher.gson != null) {
            return;
        }
        Launcher.gsonBuilder = new GsonBuilder();
        Launcher.gson = Launcher.gsonBuilder.create();
    }
    
    @LauncherAPI
    public static void setProfile(final ClientProfile profile) {
        Launcher.profile = profile;
        LogHelper.debug("New Profile name: %s", profile.getTitle());
    }
    
    public static void verifyHDir(final Path dir, final HashedDir hdir, FileNameMatcher matcher, final boolean digest) throws IOException {
        if (matcher != null) {
            matcher = matcher.verifyOnly();
        }
        final HashedDir currentHDir = new HashedDir(dir, matcher, true, digest);
    }
    
    private ClientLauncher() {
    }
    
    static {
        ClientLauncher.gson = new Gson();
        EMPTY_ARRAY = new String[0];
        SOCKET_PORT = Launcher.getConfig().clientPort;
        isUsingWrapper = Launcher.getConfig().isUsingWrapper;
        isDownloadJava = Launcher.getConfig().isDownloadJava;
        BIN_POSIX_PERMISSIONS = Collections.unmodifiableSet((Set<? extends PosixFilePermission>)EnumSet.of(PosixFilePermission.OWNER_READ, new PosixFilePermission[] { PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.GROUP_READ, PosixFilePermission.GROUP_EXECUTE, PosixFilePermission.OTHERS_READ, PosixFilePermission.OTHERS_EXECUTE }));
        NATIVES_DIR = IOHelper.toPath("natives");
        RESOURCEPACKS_DIR = IOHelper.toPath("resourcepacks");
        ClientLauncher.process = null;
        ClientLauncher.clientStarted = false;
    }
    
    private static final class ClassPathFileVisitor extends SimpleFileVisitor<Path>
    {
        private final Collection<Path> result;
        
        private ClassPathFileVisitor(final Collection<Path> result) {
            this.result = result;
        }
        
        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
            if (IOHelper.hasExtension(file, "jar") || IOHelper.hasExtension(file, "zip")) {
                this.result.add(file);
            }
            return super.visitFile(file, attrs);
        }
    }
    
    public static final class Params extends StreamObject
    {
        @LauncherAPI
        public final Path assetDir;
        @LauncherAPI
        public final Path clientDir;
        @LauncherAPI
        public final PlayerProfile pp;
        @LauncherAPI
        public final String accessToken;
        @LauncherAPI
        public final boolean autoEnter;
        @LauncherAPI
        public final boolean fullScreen;
        @LauncherAPI
        public final int ram;
        @LauncherAPI
        public final int width;
        @LauncherAPI
        public final int height;
        private final byte[] launcherDigest;
        @LauncherAPI
        public final long session;
        
        @LauncherAPI
        public Params(final byte[] launcherDigest, final Path assetDir, final Path clientDir, final PlayerProfile pp, final String accessToken, final boolean autoEnter, final boolean fullScreen, final int ram, final int width, final int height) {
            this.launcherDigest = launcherDigest.clone();
            this.assetDir = assetDir;
            this.clientDir = clientDir;
            this.pp = pp;
            this.accessToken = SecurityHelper.verifyToken(accessToken);
            this.autoEnter = autoEnter;
            this.fullScreen = fullScreen;
            this.ram = ram;
            this.width = width;
            this.height = height;
            this.session = Request.getSession();
        }
        
        @LauncherAPI
        public Params(final HInput input) throws Exception {
            this.launcherDigest = input.readByteArray(0);
            this.session = input.readLong();
            this.assetDir = IOHelper.toPath(input.readString(0));
            this.clientDir = IOHelper.toPath(input.readString(0));
            this.pp = new PlayerProfile(input);
            final byte[] encryptedAccessToken = input.readByteArray(2048);
            final String accessTokenD = new String(SecurityHelper.decrypt(Launcher.getConfig().secretKeyClient.getBytes(), encryptedAccessToken));
            this.accessToken = accessTokenD;
            this.autoEnter = input.readBoolean();
            this.fullScreen = input.readBoolean();
            this.ram = input.readVarInt();
            this.width = input.readVarInt();
            this.height = input.readVarInt();
        }
        
        @Override
        public void write(final HOutput output) throws IOException {
            output.writeByteArray(this.launcherDigest, 0);
            output.writeLong(this.session);
            output.writeString(this.assetDir.toString(), 0);
            output.writeString(this.clientDir.toString(), 0);
            this.pp.write(output);
            try {
                output.writeByteArray(SecurityHelper.encrypt(Launcher.getConfig().secretKeyClient.getBytes(), this.accessToken.getBytes()), 2048);
            }
            catch (Exception e) {
                LogHelper.error(e);
            }
            output.writeBoolean(this.autoEnter);
            output.writeBoolean(this.fullScreen);
            output.writeVarInt(this.ram);
            output.writeVarInt(this.width);
            output.writeVarInt(this.height);
        }
    }
    
    public static class ClientUserProperties
    {
        @LauncherAPI
        String[] skinURL;
        @LauncherAPI
        String[] skinDigest;
        @LauncherAPI
        String[] cloakURL;
        @LauncherAPI
        String[] cloakDigest;
    }
}
