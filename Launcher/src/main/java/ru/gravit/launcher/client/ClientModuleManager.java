package ru.gravit.launcher.client;

import java.net.URL;
import ru.gravit.launcher.modules.Module;
import java.util.ArrayList;
import ru.gravit.launcher.LauncherEngine;
import ru.gravit.launcher.managers.SimpleModuleManager;

public class ClientModuleManager extends SimpleModuleManager
{
    public ClientModuleManager(final LauncherEngine engine) {
        this.context = new ClientModuleContext(engine);
        this.modules = new ArrayList<Module>();
    }
    
    @Override
    public void loadModule(final URL jarpath, final String classname) {
        throw new SecurityException("Custom JAR's load not allowed here");
    }
    
    @Override
    public void loadModuleFull(final URL jarpath) {
        throw new SecurityException("Custom JAR's load not allowed here");
    }
}
