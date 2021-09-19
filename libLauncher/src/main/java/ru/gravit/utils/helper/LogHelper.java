package ru.gravit.utils.helper;

import org.fusesource.jansi.AnsiOutputStream;
import java.io.OutputStream;
import org.fusesource.jansi.AnsiConsole;
import java.util.Map;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Locale;
import ru.gravit.launcher.Launcher;
import org.fusesource.jansi.Ansi;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.time.temporal.TemporalAccessor;
import java.time.LocalDateTime;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.time.format.DateTimeFormatter;
import ru.gravit.launcher.LauncherAPI;

public final class LogHelper
{
    @LauncherAPI
    public static final String DEBUG_PROPERTY = "launcher.debug";
    @LauncherAPI
    public static final String DEV_PROPERTY = "launcher.dev";
    @LauncherAPI
    public static final String STACKTRACE_PROPERTY = "launcher.stacktrace";
    @LauncherAPI
    public static final String NO_JANSI_PROPERTY = "launcher.noJAnsi";
    @LauncherAPI
    public static final boolean JANSI;
    private static final DateTimeFormatter DATE_TIME_FORMATTER;
    private static final AtomicBoolean DEBUG_ENABLED;
    private static final AtomicBoolean STACKTRACE_ENABLED;
    private static final AtomicBoolean DEV_ENABLED;
    private static final Set<OutputEnity> OUTPUTS;
    private static final OutputEnity STD_OUTPUT;
    
    private LogHelper() {
    }
    
    @LauncherAPI
    public static void addOutput(final OutputEnity output) {
        LogHelper.OUTPUTS.add(Objects.requireNonNull(output, "output"));
    }
    
    @LauncherAPI
    public static void addOutput(final Output output, final OutputTypes type) {
        LogHelper.OUTPUTS.add(new OutputEnity(Objects.requireNonNull(output, "output"), type));
    }
    
    @LauncherAPI
    public static void addOutput(final Path file) throws IOException {
        if (LogHelper.JANSI) {
            addOutput(new JAnsiOutput(IOHelper.newOutput(file, true)), OutputTypes.JANSI);
        }
        else {
            addOutput(IOHelper.newWriter(file, true));
        }
    }
    
    @LauncherAPI
    public static void addOutput(final Writer writer) {
        addOutput(new WriterOutput(writer), OutputTypes.PLAIN);
    }
    
    @LauncherAPI
    public static void debug(final String message) {
        if (isDebugEnabled()) {
            log(Level.DEBUG, message, false);
        }
    }
    
    @LauncherAPI
    public static void dev(final String message) {
        if (isDevEnabled()) {
            log(Level.DEV, message, false);
        }
    }
    
    @LauncherAPI
    public static void debug(final String format, final Object... args) {
        debug(String.format(format, args));
    }
    
    @LauncherAPI
    public static void dev(final String format, final Object... args) {
        debug(String.format(format, args));
    }
    
    @LauncherAPI
    public static void error(final Throwable exc) {
        error(isStacktraceEnabled() ? toString(exc) : exc.toString());
    }
    
    @LauncherAPI
    public static void error(final String message) {
        log(Level.ERROR, message, false);
    }
    
    @LauncherAPI
    public static void error(final String format, final Object... args) {
        error(String.format(format, args));
    }
    
    @LauncherAPI
    public static void info(final String message) {
        log(Level.INFO, message, false);
    }
    
    @LauncherAPI
    public static void info(final String format, final Object... args) {
        info(String.format(format, args));
    }
    
    @LauncherAPI
    public static boolean isDebugEnabled() {
        return LogHelper.DEBUG_ENABLED.get();
    }
    
    @LauncherAPI
    public static void setDebugEnabled(final boolean debugEnabled) {
        LogHelper.DEBUG_ENABLED.set(debugEnabled);
    }
    
    @LauncherAPI
    public static boolean isStacktraceEnabled() {
        return LogHelper.STACKTRACE_ENABLED.get();
    }
    
    @LauncherAPI
    public static boolean isDevEnabled() {
        return LogHelper.DEV_ENABLED.get();
    }
    
    @LauncherAPI
    public static void setStacktraceEnabled(final boolean stacktraceEnabled) {
        LogHelper.STACKTRACE_ENABLED.set(stacktraceEnabled);
    }
    
    @LauncherAPI
    public static void setDevEnabled(final boolean stacktraceEnabled) {
        LogHelper.DEV_ENABLED.set(stacktraceEnabled);
    }
    
    @LauncherAPI
    public static void log(final Level level, final String message, final boolean sub) {
        final String dateTime = LogHelper.DATE_TIME_FORMATTER.format(LocalDateTime.now());
        String jansiString = null;
        String plainString = null;
        for (final OutputEnity output : LogHelper.OUTPUTS) {
            if (output.type == OutputTypes.JANSI && LogHelper.JANSI) {
                if (jansiString != null) {
                    output.output.println(jansiString);
                }
                else {
                    jansiString = ansiFormatLog(level, dateTime, message, sub);
                    output.output.println(jansiString);
                }
            }
            else if (plainString != null) {
                output.output.println(plainString);
            }
            else {
                plainString = formatLog(level, message, dateTime, sub);
                output.output.println(plainString);
            }
        }
    }
    
    @LauncherAPI
    public static void printVersion(final String product) {
        String jansiString = null;
        String plainString = null;
        for (final OutputEnity output : LogHelper.OUTPUTS) {
            if (output.type == OutputTypes.JANSI && LogHelper.JANSI) {
                if (jansiString != null) {
                    output.output.println(jansiString);
                }
                else {
                    jansiString = ansiFormatVersion(product);
                    output.output.println(jansiString);
                }
            }
            else if (plainString != null) {
                output.output.println(plainString);
            }
            else {
                plainString = formatVersion(product);
                output.output.println(plainString);
            }
        }
    }
    
    @LauncherAPI
    public static void printLicense(final String product) {
        String jansiString = null;
        String plainString = null;
        for (final OutputEnity output : LogHelper.OUTPUTS) {
            if (output.type == OutputTypes.JANSI && LogHelper.JANSI) {
                if (jansiString != null) {
                    output.output.println(jansiString);
                }
                else {
                    jansiString = ansiFormatLicense(product);
                    output.output.println(jansiString);
                }
            }
            else if (plainString != null) {
                output.output.println(plainString);
            }
            else {
                plainString = formatLicense(product);
                output.output.println(plainString);
            }
        }
    }
    
    @LauncherAPI
    public static boolean removeOutput(final OutputEnity output) {
        return LogHelper.OUTPUTS.remove(output);
    }
    
    @LauncherAPI
    public static boolean removeStdOutput() {
        return removeOutput(LogHelper.STD_OUTPUT);
    }
    
    @LauncherAPI
    public static void subDebug(final String message) {
        if (isDebugEnabled()) {
            log(Level.DEBUG, message, true);
        }
    }
    
    @LauncherAPI
    public static void subDebug(final String format, final Object... args) {
        subDebug(String.format(format, args));
    }
    
    @LauncherAPI
    public static void subInfo(final String message) {
        log(Level.INFO, message, true);
    }
    
    @LauncherAPI
    public static void subInfo(final String format, final Object... args) {
        subInfo(String.format(format, args));
    }
    
    @LauncherAPI
    public static void subWarning(final String message) {
        log(Level.WARNING, message, true);
    }
    
    @LauncherAPI
    public static void subWarning(final String format, final Object... args) {
        subWarning(String.format(format, args));
    }
    
    @LauncherAPI
    public static String toString(final Throwable exc) {
        try (final StringWriter sw = new StringWriter()) {
            try (final PrintWriter pw = new PrintWriter(sw)) {
                exc.printStackTrace(pw);
            }
            return sw.toString();
        }
        catch (IOException e) {
            throw new InternalError(e);
        }
    }
    
    @LauncherAPI
    public static void warning(final String message) {
        log(Level.WARNING, message, false);
    }
    
    @LauncherAPI
    public static void warning(final String format, final Object... args) {
        warning(String.format(format, args));
    }
    
    private static String ansiFormatLog(final Level level, final String dateTime, final String message, final boolean sub) {
        final boolean bright = level != Level.DEBUG;
        Ansi.Color levelColor = null;
        switch (level) {
            case WARNING: {
                levelColor = Ansi.Color.YELLOW;
                break;
            }
            case ERROR: {
                levelColor = Ansi.Color.RED;
                break;
            }
            default: {
                levelColor = Ansi.Color.WHITE;
                break;
            }
        }
        final Ansi ansi = new Ansi();
        ansi.fg(Ansi.Color.WHITE).a(dateTime);
        ansi.fgBright(Ansi.Color.WHITE).a(" [").bold();
        if (bright) {
            ansi.fgBright(levelColor);
        }
        else {
            ansi.fg(levelColor);
        }
        ansi.a((Object)level).boldOff().fgBright(Ansi.Color.WHITE).a("] ");
        if (bright) {
            ansi.fgBright(levelColor);
        }
        else {
            ansi.fg(levelColor);
        }
        if (sub) {
            ansi.a(' ').a(Ansi.Attribute.ITALIC);
        }
        ansi.a(message);
        return ansi.reset().toString();
    }
    
    private static String ansiFormatVersion(final String product) {
        return new Ansi().bold().fgBright(Ansi.Color.MAGENTA).a("GravitLauncher ").fgBright(Ansi.Color.BLUE).a("(fork sashok724's Launcher) ").fgBright(Ansi.Color.CYAN).a(product).fgBright(Ansi.Color.WHITE).a(" v").fgBright(Ansi.Color.BLUE).a(Launcher.getVersion().toString()).fgBright(Ansi.Color.WHITE).a(" (build #").fgBright(Ansi.Color.RED).a(Launcher.getVersion().build).fgBright(Ansi.Color.WHITE).a(')').reset().toString();
    }
    
    private static String ansiFormatLicense(final String product) {
        return new Ansi().bold().fgBright(Ansi.Color.MAGENTA).a("License for ").fgBright(Ansi.Color.CYAN).a(product).fgBright(Ansi.Color.WHITE).a(" GPLv3").fgBright(Ansi.Color.WHITE).a(". SourceCode: ").fgBright(Ansi.Color.YELLOW).a("https://github.com/GravitLauncher/Launcher").reset().toString();
    }
    
    private static String formatLog(final Level level, String message, final String dateTime, final boolean sub) {
        if (sub) {
            message = ' ' + message;
        }
        return dateTime + " [" + level.name + "] " + message;
    }
    
    private static String formatVersion(final String product) {
        return String.format("GravitLauncher (fork sashok724's Launcher) %s v%s", product, Launcher.getVersion().toString());
    }
    
    private static String formatLicense(final String product) {
        return String.format("License for %s GPLv3. SourceCode: https://github.com/GravitLauncher/Launcher", product);
    }
    
    static {
        DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss", Locale.US);
        DEBUG_ENABLED = new AtomicBoolean(Boolean.getBoolean("launcher.debug"));
        STACKTRACE_ENABLED = new AtomicBoolean(Boolean.getBoolean("launcher.stacktrace"));
        DEV_ENABLED = new AtomicBoolean(Boolean.getBoolean("launcher.dev"));
        OUTPUTS = Collections.newSetFromMap(new ConcurrentHashMap<OutputEnity, Boolean>(2));
        boolean jansi;
        try {
            if (Boolean.getBoolean("launcher.noJAnsi")) {
                jansi = false;
            }
            else {
                Class.forName("org.fusesource.jansi.Ansi");
                AnsiConsole.systemInstall();
                jansi = true;
            }
        }
        catch (ClassNotFoundException ignored) {
            jansi = false;
        }
        JANSI = jansi;
        addOutput(STD_OUTPUT = new OutputEnity(System.out::println, LogHelper.JANSI ? OutputTypes.JANSI : OutputTypes.PLAIN));
        final String logFile = System.getProperty("launcher.logFile");
        if (logFile != null) {
            try {
                addOutput(IOHelper.toPath(logFile));
            }
            catch (IOException e) {
                error(e);
            }
        }
    }
    
    public static class OutputEnity
    {
        public Output output;
        public OutputTypes type;
        
        public OutputEnity(final Output output, final OutputTypes type) {
            this.output = output;
            this.type = type;
        }
    }
    
    public enum OutputTypes
    {
        PLAIN, 
        JANSI, 
        HTML;
    }
    
    @LauncherAPI
    public enum Level
    {
        DEV("DEV"), 
        DEBUG("DEBUG"), 
        INFO("INFO"), 
        WARNING("WARN"), 
        ERROR("ERROR");
        
        public final String name;
        
        private Level(final String name) {
            this.name = name;
        }
        
        @Override
        public String toString() {
            return this.name;
        }
    }
    
    private static final class JAnsiOutput extends WriterOutput
    {
        private JAnsiOutput(final OutputStream output) {
            super((Writer)IOHelper.newWriter((OutputStream)new AnsiOutputStream(output)));
        }
    }
    
    private static class WriterOutput implements Output, AutoCloseable
    {
        private final Writer writer;
        
        private WriterOutput(final Writer writer) {
            this.writer = writer;
        }
        
        @Override
        public void close() throws IOException {
            this.writer.close();
        }
        
        @Override
        public void println(final String message) {
            try {
                this.writer.write(message + System.lineSeparator());
                this.writer.flush();
            }
            catch (IOException ex) {}
        }
    }
    
    @FunctionalInterface
    @LauncherAPI
    public interface Output
    {
        void println(final String p0);
    }
}
