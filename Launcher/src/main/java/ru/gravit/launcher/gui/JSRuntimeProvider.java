package ru.gravit.launcher.gui;

import javax.script.Invocable;
import javax.script.Bindings;
import javax.script.ScriptException;
import java.io.IOException;
import java.io.BufferedReader;
import java.net.URL;
import java.io.Reader;
import ru.gravit.launcher.LauncherAPI;
import ru.gravit.launcher.JSApplication;
import ru.gravit.launcher.client.LauncherSettings;
import ru.gravit.launcher.client.FunctionalBridge;
import ru.gravit.launcher.client.DirBridge;
import ru.gravit.utils.helper.VerifyHelper;
import ru.gravit.utils.helper.SecurityHelper;
import ru.gravit.utils.helper.LogHelper;
import ru.gravit.utils.helper.JVMHelper;
import ru.gravit.utils.helper.EnvHelper;
import ru.gravit.utils.helper.IOHelper;
import ru.gravit.launcher.profiles.optional.OptionalFile;
import ru.gravit.launcher.serialize.stream.EnumSerializer;
import ru.gravit.launcher.serialize.signed.SignedObjectHolder;
import ru.gravit.launcher.serialize.signed.SignedBytesHolder;
import ru.gravit.launcher.serialize.stream.StreamObject;
import ru.gravit.launcher.serialize.HOutput;
import ru.gravit.launcher.serialize.HInput;
import ru.gravit.launcher.hasher.HashedEntry;
import ru.gravit.launcher.hasher.HashedFile;
import ru.gravit.launcher.hasher.HashedDir;
import ru.gravit.launcher.hasher.FileNameMatcher;
import ru.gravit.launcher.request.uuid.BatchProfileByUsernameRequest;
import ru.gravit.launcher.request.uuid.ProfileByUUIDRequest;
import ru.gravit.launcher.request.uuid.ProfileByUsernameRequest;
import ru.gravit.launcher.request.update.ProfilesRequest;
import ru.gravit.launcher.request.auth.SetProfileRequest;
import ru.gravit.launcher.request.update.LauncherRequest;
import ru.gravit.launcher.request.update.UpdateRequest;
import ru.gravit.launcher.request.auth.CheckServerRequest;
import ru.gravit.launcher.request.auth.JoinServerRequest;
import ru.gravit.launcher.request.auth.AuthRequest;
import ru.gravit.launcher.request.PingRequest;
import ru.gravit.launcher.request.CustomRequest;
import ru.gravit.launcher.request.RequestException;
import ru.gravit.launcher.request.RequestType;
import ru.gravit.launcher.request.Request;
import ru.gravit.launcher.client.ServerPinger;
import ru.gravit.launcher.client.ClientLauncher;
import ru.gravit.launcher.profiles.ClientProfile;
import ru.gravit.launcher.profiles.Texture;
import ru.gravit.launcher.profiles.PlayerProfile;
import ru.gravit.utils.HTTPRequest;
import ru.gravit.launcher.LauncherConfig;
import ru.gravit.launcher.Launcher;
import java.util.Map;
import ru.gravit.utils.helper.CommonHelper;
import javax.script.ScriptEngine;

public class JSRuntimeProvider implements RuntimeProvider
{
    private final ScriptEngine engine;
    private boolean isPreLoaded;
    
    public JSRuntimeProvider() {
        this.engine = CommonHelper.newScriptEngine();
        this.isPreLoaded = false;
    }
    
    @LauncherAPI
    public static void addLauncherClassBindings(final Map<String, Object> bindings) {
        bindings.put("LauncherClass", Launcher.class);
        bindings.put("LauncherConfigClass", LauncherConfig.class);
        bindings.put("HTTPRequestClass", HTTPRequest.class);
        bindings.put("PlayerProfileClass", PlayerProfile.class);
        bindings.put("PlayerProfileTextureClass", Texture.class);
        bindings.put("ClientProfileClass", ClientProfile.class);
        bindings.put("ClientProfileVersionClass", ClientProfile.Version.class);
        bindings.put("ClientLauncherClass", ClientLauncher.class);
        bindings.put("ClientLauncherParamsClass", ClientLauncher.Params.class);
        bindings.put("ServerPingerClass", ServerPinger.class);
        bindings.put("RequestClass", Request.class);
        bindings.put("RequestTypeClass", RequestType.class);
        bindings.put("RequestExceptionClass", RequestException.class);
        bindings.put("CustomRequestClass", CustomRequest.class);
        bindings.put("PingRequestClass", PingRequest.class);
        bindings.put("AuthRequestClass", AuthRequest.class);
        bindings.put("JoinServerRequestClass", JoinServerRequest.class);
        bindings.put("CheckServerRequestClass", CheckServerRequest.class);
        bindings.put("UpdateRequestClass", UpdateRequest.class);
        bindings.put("LauncherRequestClass", LauncherRequest.class);
        bindings.put("SetProfileRequestClass", SetProfileRequest.class);
        bindings.put("ProfilesRequestClass", ProfilesRequest.class);
        bindings.put("ProfileByUsernameRequestClass", ProfileByUsernameRequest.class);
        bindings.put("ProfileByUUIDRequestClass", ProfileByUUIDRequest.class);
        bindings.put("BatchProfileByUsernameRequestClass", BatchProfileByUsernameRequest.class);
        bindings.put("FileNameMatcherClass", FileNameMatcher.class);
        bindings.put("HashedDirClass", HashedDir.class);
        bindings.put("HashedFileClass", HashedFile.class);
        bindings.put("HashedEntryTypeClass", HashedEntry.Type.class);
        bindings.put("HInputClass", HInput.class);
        bindings.put("HOutputClass", HOutput.class);
        bindings.put("StreamObjectClass", StreamObject.class);
        bindings.put("StreamObjectAdapterClass", StreamObject.Adapter.class);
        bindings.put("SignedBytesHolderClass", SignedBytesHolder.class);
        bindings.put("SignedObjectHolderClass", SignedObjectHolder.class);
        bindings.put("EnumSerializerClass", EnumSerializer.class);
        bindings.put("OptionalFileClass", OptionalFile.class);
        bindings.put("CommonHelperClass", CommonHelper.class);
        bindings.put("IOHelperClass", IOHelper.class);
        bindings.put("EnvHelperClass", EnvHelper.class);
        bindings.put("JVMHelperClass", JVMHelper.class);
        bindings.put("JVMHelperOSClass", JVMHelper.OS.class);
        bindings.put("LogHelperClass", LogHelper.class);
        bindings.put("LogHelperOutputClass", LogHelper.Output.class);
        bindings.put("SecurityHelperClass", SecurityHelper.class);
        bindings.put("DigestAlgorithmClass", SecurityHelper.DigestAlgorithm.class);
        bindings.put("VerifyHelperClass", VerifyHelper.class);
        bindings.put("DirBridgeClass", DirBridge.class);
        bindings.put("FunctionalBridgeClass", FunctionalBridge.class);
        bindings.put("LauncherSettingsClass", LauncherSettings.class);
        try {
            Class.forName("javafx.application.Application");
            bindings.put("JSApplicationClass", JSApplication.class);
        }
        catch (ClassNotFoundException ignored) {
            LogHelper.warning("JavaFX API isn't available");
        }
    }
    
    @LauncherAPI
    public Object loadScript(final String path) throws IOException, ScriptException {
        final URL url = Launcher.getResourceURL(path);
        LogHelper.debug("Loading script: '%s'", url);
        try (final BufferedReader reader = IOHelper.newReader(url)) {
            return this.engine.eval(reader, this.engine.getBindings(100));
        }
    }
    
    private void setScriptBindings() {
        LogHelper.info("Setting up script engine bindings");
        final Bindings bindings = this.engine.getBindings(100);
        bindings.put("launcher", (Object)this);
        addLauncherClassBindings(bindings);
    }
    
    @Override
    public void run(final String[] args) throws ScriptException, NoSuchMethodException, IOException {
        this.preLoad();
        this.loadScript("init.js");
        LogHelper.info("Invoking start() function");
        Launcher.modulesManager.postInitModules();
        ((Invocable)this.engine).invokeFunction("start", args);
        Launcher.modulesManager.finishModules();
    }
    
    @Override
    public void preLoad() throws IOException, ScriptException {
        if (!this.isPreLoaded) {
            this.loadScript("engine/api.js");
            this.loadScript("config.js");
            this.isPreLoaded = true;
        }
    }
    
    @Override
    public void init(final boolean clientInstance) {
        this.setScriptBindings();
    }
}
