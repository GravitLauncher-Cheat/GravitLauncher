package ru.gravit.launcher.request.update;

import java.security.interfaces.RSAPublicKey;
import ru.gravit.launcher.Launcher;
import ru.gravit.launcher.profiles.ClientProfile;
import java.util.Collections;
import ru.gravit.launcher.serialize.HOutput;
import ru.gravit.launcher.serialize.HInput;
import ru.gravit.launcher.request.RequestType;
import java.io.IOException;
import java.security.SignatureException;
import java.util.List;
import ru.gravit.utils.helper.JVMHelper;
import ru.gravit.utils.helper.LogHelper;
import ru.gravit.utils.helper.IOHelper;
import java.util.ArrayList;
import ru.gravit.utils.helper.SecurityHelper;
import ru.gravit.launcher.LauncherConfig;
import ru.gravit.launcher.LauncherAPI;
import java.nio.file.Path;
import ru.gravit.launcher.request.Request;

public final class LegacyLauncherRequest extends Request
{
    @LauncherAPI
    public static final Path BINARY_PATH;
    @LauncherAPI
    public static final boolean EXE_BINARY;
    
    @LauncherAPI
    public static void update(final LauncherConfig config, final Result result) throws SignatureException, IOException {
        SecurityHelper.verifySign(result.binary, result.sign, config.publicKey);
        final List<String> args = new ArrayList<String>(8);
        args.add(IOHelper.resolveJavaBin(null).toString());
        if (LogHelper.isDebugEnabled()) {
            args.add(JVMHelper.jvmProperty("launcher.debug", Boolean.toString(LogHelper.isDebugEnabled())));
        }
        args.add("-jar");
        args.add(LegacyLauncherRequest.BINARY_PATH.toString());
        final ProcessBuilder builder = new ProcessBuilder((String[])args.toArray(new String[0]));
        builder.inheritIO();
        IOHelper.write(LegacyLauncherRequest.BINARY_PATH, result.binary);
        builder.start();
        JVMHelper.RUNTIME.exit(255);
        throw new AssertionError((Object)"Why Launcher wasn't restarted?!");
    }
    
    @LauncherAPI
    public LegacyLauncherRequest() {
        this(null);
    }
    
    @LauncherAPI
    public LegacyLauncherRequest(final LauncherConfig config) {
        super(config);
    }
    
    @Override
    public Integer getLegacyType() {
        return RequestType.LEGACYLAUNCHER.getNumber();
    }
    
    @Override
    protected Result requestDo(final HInput input, final HOutput output) throws Exception {
        output.writeBoolean(LegacyLauncherRequest.EXE_BINARY);
        output.flush();
        this.readError(input);
        final RSAPublicKey publicKey = this.config.publicKey;
        final byte[] sign = input.readByteArray(-256);
        final boolean shouldUpdate = !SecurityHelper.isValidSign(LegacyLauncherRequest.BINARY_PATH, sign, publicKey);
        output.writeBoolean(shouldUpdate);
        output.flush();
        if (shouldUpdate) {
            final byte[] binary = input.readByteArray(0);
            SecurityHelper.verifySign(binary, sign, this.config.publicKey);
            return new Result(binary, sign, Collections.emptyList());
        }
        final int count = input.readLength(0);
        final List<ClientProfile> profiles = new ArrayList<ClientProfile>(count);
        for (int i = 0; i < count; ++i) {
            final String prof = input.readString(0);
            profiles.add(Launcher.gson.fromJson(prof, ClientProfile.class));
        }
        return new Result(null, sign, profiles);
    }
    
    static {
        BINARY_PATH = IOHelper.getCodeSource(Launcher.class);
        EXE_BINARY = IOHelper.hasExtension(LegacyLauncherRequest.BINARY_PATH, "exe");
    }
    
    public static final class Result
    {
        @LauncherAPI
        public final List<ClientProfile> profiles;
        private final byte[] binary;
        private final byte[] sign;
        
        public Result(final byte[] binary, final byte[] sign, final List<ClientProfile> profiles) {
            this.binary = (byte[])((binary == null) ? null : ((byte[])binary.clone()));
            this.sign = sign.clone();
            this.profiles = Collections.unmodifiableList((List<? extends ClientProfile>)profiles);
        }
        
        @LauncherAPI
        public byte[] getBinary() {
            return (byte[])((this.binary == null) ? null : ((byte[])this.binary.clone()));
        }
        
        @LauncherAPI
        public byte[] getSign() {
            return this.sign.clone();
        }
    }
}
