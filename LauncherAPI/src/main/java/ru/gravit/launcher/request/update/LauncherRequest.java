package ru.gravit.launcher.request.update;

import ru.gravit.launcher.serialize.HOutput;
import ru.gravit.launcher.serialize.HInput;
import ru.gravit.launcher.request.RequestType;
import ru.gravit.utils.helper.SecurityHelper;
import ru.gravit.launcher.Launcher;
import ru.gravit.launcher.request.websockets.LegacyRequestBridge;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLConnection;
import java.util.List;
import java.net.URL;
import ru.gravit.utils.helper.JVMHelper;
import ru.gravit.utils.helper.LogHelper;
import ru.gravit.utils.helper.IOHelper;
import java.util.ArrayList;
import ru.gravit.launcher.LauncherConfig;
import ru.gravit.launcher.LauncherAPI;
import java.nio.file.Path;
import ru.gravit.launcher.LauncherNetworkAPI;
import ru.gravit.launcher.request.websockets.RequestInterface;
import ru.gravit.launcher.events.request.LauncherRequestEvent;
import ru.gravit.launcher.request.Request;

public final class LauncherRequest extends Request<LauncherRequestEvent> implements RequestInterface
{
    @LauncherNetworkAPI
    public byte[] digest;
    @LauncherNetworkAPI
    public int launcher_type;
    @LauncherAPI
    public static final Path BINARY_PATH;
    @LauncherAPI
    public static final boolean EXE_BINARY;
    
    @LauncherAPI
    public static void update(final LauncherConfig config, final LauncherRequestEvent result) throws IOException {
        final List<String> args = new ArrayList<String>(8);
        args.add(IOHelper.resolveJavaBin(null).toString());
        if (LogHelper.isDebugEnabled()) {
            args.add(JVMHelper.jvmProperty("launcher.debug", Boolean.toString(LogHelper.isDebugEnabled())));
        }
        args.add("-jar");
        args.add(LauncherRequest.BINARY_PATH.toString());
        final ProcessBuilder builder = new ProcessBuilder((String[])args.toArray(new String[0]));
        builder.inheritIO();
        if (result.binary != null) {
            IOHelper.write(LauncherRequest.BINARY_PATH, result.binary);
        }
        else {
            final URLConnection connection = IOHelper.newConnection(new URL(result.url));
            connection.connect();
            try (final OutputStream stream = connection.getOutputStream()) {
                IOHelper.transfer(LauncherRequest.BINARY_PATH, stream);
            }
        }
        builder.start();
        JVMHelper.RUNTIME.exit(255);
        throw new AssertionError((Object)"Why Launcher wasn't restarted?!");
    }
    
    @LauncherAPI
    public LauncherRequest() {
        this(null);
    }
    
    public LauncherRequestEvent requestWebSockets() throws Exception {
        final LauncherRequestEvent result = (LauncherRequestEvent)LegacyRequestBridge.sendRequest(this);
        if (result.needUpdate) {
            update(this.config, result);
        }
        return result;
    }
    
    @LauncherAPI
    public LauncherRequest(final LauncherConfig config) {
        super(config);
        this.launcher_type = (LauncherRequest.EXE_BINARY ? 2 : 1);
        final Path launcherPath = IOHelper.getCodeSource(Launcher.class).getParent().resolve("Dreamfinity.jar");
        try {
            this.digest = SecurityHelper.digest(SecurityHelper.DigestAlgorithm.SHA512, launcherPath);
        }
        catch (IOException e) {
            LogHelper.error(e);
        }
    }
    
    @Override
    public Integer getLegacyType() {
        return RequestType.LAUNCHER.getNumber();
    }
    
    @Override
    protected LauncherRequestEvent requestDo(final HInput input, final HOutput output) throws Exception {
        output.writeBoolean(LauncherRequest.EXE_BINARY);
        output.writeByteArray(this.digest, 0);
        output.flush();
        this.readError(input);
        boolean shouldUpdate = input.readBoolean();
        shouldUpdate = false;
        if (shouldUpdate) {
            final byte[] binary = input.readByteArray(0);
            final LauncherRequestEvent result = new LauncherRequestEvent(binary, this.digest);
            update(Launcher.getConfig(), result);
        }
        return new LauncherRequestEvent(null, this.digest);
    }
    
    @Override
    public String getType() {
        return "launcher";
    }
    
    static {
        BINARY_PATH = IOHelper.getCodeSource(Launcher.class).getParent().resolve("Dreamfinity.jar");
        EXE_BINARY = IOHelper.hasExtension(LauncherRequest.BINARY_PATH, "exe");
    }
}
