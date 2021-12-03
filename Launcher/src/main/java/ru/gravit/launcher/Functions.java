/*package ru.gravit.launcher;

import ru.gravit.utils.helper.IOHelper;
import ru.gravit.utils.helper.JVMHelper;
import ru.gravit.utils.helper.LogHelper;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Path;
import java.io.IOException;

public class Functions
{
    public static final Path BINARY_PATH = IOHelper.getCodeSource(Launcher.class);

    public static void restartwithproxy(String socksip, String socksport, String httpsip, String httpsport) {
        List<String> args = new ArrayList<>(8);
        args.add(IOHelper.resolveJavaBin(null).toString());
        if (LogHelper.isDebugEnabled())
            args.add(JVMHelper.jvmProperty(LogHelper.DEBUG_PROPERTY, Boolean.toString(LogHelper.isDebugEnabled())));
        args.add("-jar");
        args.add("-DsocksProxyHost=" + socksip);
        args.add("-DsocksProxyPort=" + socksport);
        args.add("-DhttpProxyHost=" + httpsip);
        args.add("-DhttpProxyPort=" + httpsport);
        args.add("-DproxySet=true");
        args.add(BINARY_PATH.toString());
        ProcessBuilder builder = new ProcessBuilder(args.toArray(new String[0]));
        try {builder.start();} catch (IOException e) {System.out.println("IoException");}
        JVMHelper.RUNTIME.exit(255);
    }
}*/