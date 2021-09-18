// 
// Decompiled by Procyon v0.5.36
// 

package ru.gravit.launcher.request;

import ru.gravit.utils.helper.SecurityHelper;
import java.net.Socket;
import ru.gravit.launcher.serialize.HOutput;
import java.net.SocketAddress;
import ru.gravit.utils.helper.IOHelper;
import java.io.IOException;
import ru.gravit.launcher.serialize.HInput;
import ru.gravit.launcher.Launcher;
import java.util.concurrent.atomic.AtomicBoolean;
import ru.gravit.launcher.LauncherAPI;
import ru.gravit.launcher.LauncherConfig;

public abstract class Request<R>
{
    private static long session;
    @LauncherAPI
    protected final transient LauncherConfig config;
    private final transient AtomicBoolean started;
    
    public static void setSession(final long session) {
        Request.session = session;
    }
    
    public static long getSession() {
        return Request.session;
    }
    
    @LauncherAPI
    public static void requestError(final String message) throws RequestException {
        throw new RequestException(message);
    }
    
    @LauncherAPI
    protected Request() {
        this(null);
    }
    
    @LauncherAPI
    protected Request(final LauncherConfig config) {
        this.started = new AtomicBoolean(false);
        this.config = ((config == null) ? Launcher.getConfig() : config);
    }
    
    @LauncherAPI
    public abstract Integer getLegacyType();
    
    @LauncherAPI
    protected final void readError(final HInput input) throws IOException {
        final String error = input.readString(0);
        if (!error.isEmpty()) {
            requestError(error);
        }
    }
    
    @LauncherAPI
    public R request() throws Exception {
        if (!this.started.compareAndSet(false, true)) {
            throw new IllegalStateException("Request already started");
        }
        R wsResult = null;
        if (this.config.isNettyEnabled) {
            wsResult = this.requestWebSockets();
        }
        if (wsResult != null) {
            return wsResult;
        }
        try (final Socket socket = IOHelper.newSocket()) {
            socket.connect(IOHelper.resolve(this.config.address));
            try (final HInput input = new HInput(socket.getInputStream());
                 final HOutput output = new HOutput(socket.getOutputStream())) {
                this.writeHandshake(input, output);
                return this.requestDo(input, output);
            }
        }
    }
    
    protected R requestWebSockets() throws Exception {
        return null;
    }
    
    @LauncherAPI
    protected abstract R requestDo(final HInput p0, final HOutput p1) throws Exception;
    
    private void writeHandshake(final HInput input, final HOutput output) throws IOException {
        output.writeInt(-1576685468);
        output.writeBigInteger(this.config.publicKey.getModulus(), 257);
        output.writeLong(Request.session);
        output.writeVarInt(this.getLegacyType());
        output.flush();
        if (!input.readBoolean()) {
            requestError("Serverside not accepted this connection");
        }
    }
    
    static {
        Request.session = SecurityHelper.secureRandom.nextLong();
    }
}
