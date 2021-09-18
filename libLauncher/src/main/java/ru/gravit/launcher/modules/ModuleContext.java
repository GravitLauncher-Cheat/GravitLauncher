package ru.gravit.launcher.modules;

public interface ModuleContext
{
    Type getType();
    
    ModulesManager getModulesManager();
    
    ModulesConfigManager getModulesConfigManager();
    
    public enum Type
    {
        SERVER, 
        CLIENT, 
        LAUNCHSERVER;
    }
}
