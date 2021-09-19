package ru.gravit.launcher.request.update;

import ru.gravit.launcher.serialize.HOutput;
import ru.gravit.launcher.serialize.HInput;
import ru.gravit.launcher.request.RequestType;
import ru.gravit.utils.helper.SecurityHelper;
import ru.gravit.launcher.Launcher;
import ru.gravit.launcher.request.websockets.LegacyRequestBridge;
import java.io.IOException;
import ru.gravit.utils.helper.IOHelper;
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
    
    @LauncherAPI
    public static void update(final LauncherConfig config, final LauncherRequestEvent result) throws IOException {}
    
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
        try {
            final Path launcherPath = IOHelper.getCodeSource(Launcher.class).getParent().resolve("Launcher-original.jar");
            this.digest = SecurityHelper.digest(SecurityHelper.DigestAlgorithm.SHA512, launcherPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public Integer getLegacyType() {
        return RequestType.LAUNCHER.getNumber();
    }
    
    @Override
    protected LauncherRequestEvent requestDo(final HInput input, final HOutput output) throws Exception {
        output.writeBoolean(false);
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
}
