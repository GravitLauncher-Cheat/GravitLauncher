package ru.gravit.utils.helper;

import java.lang.invoke.MethodHandles;
import java.lang.management.ManagementFactory;
import java.util.Locale;
import java.util.Collection;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.File;
import java.util.Arrays;
import java.util.Map;
import ru.gravit.launcher.LauncherAPI;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;

public final class JVMHelper
{
    public static final RuntimeMXBean RUNTIME_MXBEAN;
    public static final OperatingSystemMXBean OPERATING_SYSTEM_MXBEAN;
    @LauncherAPI
    public static final OS OS_TYPE;
    @LauncherAPI
    public static final String OS_VERSION;
    @LauncherAPI
    public static final int OS_BITS;
    @LauncherAPI
    public static final int JVM_BITS;
    @LauncherAPI
    public static final int RAM;
    @LauncherAPI
    public static final SecurityManager SECURITY_MANAGER;
    public static final Runtime RUNTIME;
    public static final ClassLoader LOADER;
    
    public static void appendVars(final ProcessBuilder builder, final Map<String, String> vars) {
        builder.environment().putAll(vars);
    }
    
    public static Class<?> firstClass(final String... names) throws ClassNotFoundException {
        final int length = names.length;
        int i = 0;
        while (i < length) {
            final String name = names[i];
            try {
                return Class.forName(name, false, JVMHelper.LOADER);
            }
            catch (ClassNotFoundException ex) {
                ++i;
                continue;
            }
            //break;
        }
        throw new ClassNotFoundException(Arrays.toString(names));
    }
    
    @LauncherAPI
    public static void fullGC() {
        JVMHelper.RUNTIME.gc();
        JVMHelper.RUNTIME.runFinalization();
        LogHelper.debug("Used heap: %d MiB", JVMHelper.RUNTIME.totalMemory() - JVMHelper.RUNTIME.freeMemory() >> 20);
    }
    
    public static String[] getClassPath() {
        return System.getProperty("java.class.path").split(File.pathSeparator);
    }
    
    public static URL[] getClassPathURL() {
        final String[] cp = System.getProperty("java.class.path").split(File.pathSeparator);
        final URL[] list = new URL[cp.length];
        for (int i = 0; i < cp.length; ++i) {
            URL url = null;
            try {
                url = new URL(cp[i]);
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
            }
            list[i] = url;
        }
        return list;
    }
    
    public static void checkStackTrace(final Class<?> mainClass) {
        LogHelper.debug("Testing stacktrace");
        final Exception e = new Exception("Testing stacktrace");
        final StackTraceElement[] list = e.getStackTrace();
        if (!list[list.length - 1].getClassName().equals(mainClass.getName())) {
            throw new SecurityException(String.format("Invalid StackTraceElement: %s", list[list.length - 1].getClassName()));
        }
    }
    
    private static int getCorrectOSArch() {
        if (JVMHelper.OS_TYPE == OS.MUSTDIE) {
            return (System.getenv("ProgramFiles(x86)") == null) ? 32 : 64;
        }
        return System.getProperty("os.arch").contains("64") ? 64 : 32;
    }
    
    @LauncherAPI
    public static String getEnvPropertyCaseSensitive(final String name) {
        return System.getenv().get(name);
    }
    
    private static int getRAMAmount() {
        final int physicalRam = (int)(((com.sun.management.OperatingSystemMXBean)JVMHelper.OPERATING_SYSTEM_MXBEAN).getTotalPhysicalMemorySize() >> 20);
        return Math.min(physicalRam, (JVMHelper.OS_BITS == 32) ? 1536 : 32768);
    }
    
    @LauncherAPI
    public static boolean isJVMMatchesSystemArch() {
        return JVMHelper.JVM_BITS == JVMHelper.OS_BITS;
    }
    
    @LauncherAPI
    public static String jvmProperty(final String name, final String value) {
        return String.format("-D%s=%s", name, value);
    }
    
    @LauncherAPI
    public static String systemToJvmProperty(final String name) {
        return String.format("-D%s=%s", name, System.getProperties().getProperty(name));
    }
    
    @LauncherAPI
    public static void addSystemPropertyToArgs(final Collection<String> args, final String name) {
        final String property = System.getProperty(name);
        if (property != null) {
            args.add(String.format("-D%s=%s", name, property));
        }
    }
    
    public static void verifySystemProperties(final Class<?> mainClass, final boolean requireSystem) {
        Locale.setDefault(Locale.US);
        LogHelper.debug("Verifying class loader");
        if (requireSystem && !mainClass.getClassLoader().equals(JVMHelper.LOADER)) {
            throw new SecurityException("ClassLoader should be system");
        }
        LogHelper.debug("Verifying JVM architecture");
        if (!isJVMMatchesSystemArch()) {
            LogHelper.warning("Java and OS architecture mismatch");
            LogHelper.warning("It's recommended to download %d-bit JRE", JVMHelper.OS_BITS);
        }
    }
    
    private JVMHelper() {
    }
    
    static {
        RUNTIME_MXBEAN = ManagementFactory.getRuntimeMXBean();
        OPERATING_SYSTEM_MXBEAN = ManagementFactory.getOperatingSystemMXBean();
        OS_TYPE = OS.byName(JVMHelper.OPERATING_SYSTEM_MXBEAN.getName());
        OS_VERSION = JVMHelper.OPERATING_SYSTEM_MXBEAN.getVersion();
        OS_BITS = getCorrectOSArch();
        JVM_BITS = Integer.parseInt(System.getProperty("sun.arch.data.model"));
        RAM = getRAMAmount();
        SECURITY_MANAGER = System.getSecurityManager();
        RUNTIME = Runtime.getRuntime();
        LOADER = ClassLoader.getSystemClassLoader();
        try {
            MethodHandles.publicLookup();
        }
        catch (Throwable exc) {
            throw new InternalError(exc);
        }
    }
    
    @LauncherAPI
    public enum OS
    {
        MUSTDIE("mustdie"), 
        LINUX("linux"), 
        MACOSX("macosx");
        
        public final String name;
        
        public static OS byName(final String name) {
            if (name.startsWith("Windows")) {
                return OS.MUSTDIE;
            }
            if (name.startsWith("Linux")) {
                return OS.LINUX;
            }
            if (name.startsWith("Mac OS X")) {
                return OS.MACOSX;
            }
            throw new RuntimeException(String.format("This shit is not yet supported: '%s'", name));
        }
        
        private OS(final String name) {
            this.name = name;
        }
    }
}
