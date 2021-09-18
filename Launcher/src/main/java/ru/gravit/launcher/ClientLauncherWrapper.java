package ru.gravit.launcher;

import java.io.IOException;
import java.util.List;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Collection;
import ru.gravit.launcher.client.ClientLauncher;
import java.util.LinkedList;
import ru.gravit.utils.helper.IOHelper;
import java.nio.file.Paths;
import ru.gravit.utils.helper.EnvHelper;
import ru.gravit.utils.helper.JVMHelper;
import ru.gravit.utils.helper.LogHelper;

public class ClientLauncherWrapper
{
    public static void main(final String[] arguments) throws IOException, InterruptedException {
        LogHelper.printVersion("Launcher");
        LogHelper.printLicense("Launcher");
        LogHelper.info("Restart Launcher with JavaAgent...");
        LogHelper.info("If need debug output use -Dlauncher.debug=true");
        LogHelper.info("If need stacktrace output use -Dlauncher.stacktrace=true");
        JVMHelper.checkStackTrace(ClientLauncherWrapper.class);
        JVMHelper.verifySystemProperties(Launcher.class, true);
        EnvHelper.checkDangerousParams();
        LogHelper.debug("Restart Launcher");
        final ProcessBuilder processBuilder = new ProcessBuilder(new String[0]);
        if (LogHelper.isDebugEnabled()) {
            processBuilder.inheritIO();
        }
        final Path javaBin = IOHelper.resolveJavaBin(Paths.get(System.getProperty("java.home"), new String[0]));
        final List<String> args = new LinkedList<String>();
        args.add(javaBin.toString());
        final String pathLauncher = IOHelper.getCodeSource(ClientLauncher.class).toString();
        args.add(JVMHelper.jvmProperty("launcher.debug", Boolean.toString(LogHelper.isDebugEnabled())));
        args.add(JVMHelper.jvmProperty("launcher.stacktrace", Boolean.toString(LogHelper.isStacktraceEnabled())));
        JVMHelper.addSystemPropertyToArgs(args, "launcher.customdir");
        JVMHelper.addSystemPropertyToArgs(args, "launcher.usecustomdir");
        JVMHelper.addSystemPropertyToArgs(args, "launcher.useoptdir");
        Collections.addAll(args, new String[] { "-javaagent:".concat(pathLauncher) });
        Collections.addAll(args, new String[] { "-cp" });
        Collections.addAll(args, new String[] { pathLauncher });
        Collections.addAll(args, new String[] { LauncherEngine.class.getName() });
        EnvHelper.addEnv(processBuilder);
        LogHelper.debug("Commandline: " + args);
        processBuilder.command(args);
        final Process process = processBuilder.start();
        if (!LogHelper.isDebugEnabled()) {
            Thread.sleep(3000L);
            if (!process.isAlive()) {
                final int errorcode = process.exitValue();
                if (errorcode != 0) {
                    LogHelper.error("Process exit with error code: %d", errorcode);
                }
                else {
                    LogHelper.info("Process exit with code 0");
                }
            }
            else {
                LogHelper.debug("Process started success");
            }
        }
        else {
            process.waitFor();
        }
    }
}
