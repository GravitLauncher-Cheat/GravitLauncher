package ru.gravit.launcher.modules;

import ru.gravit.utils.Version;

public interface Module extends AutoCloseable
{
    String getName();
    
    Version getVersion();
    
    int getPriority();
    
    void init(final ModuleContext p0);
    
    void postInit(final ModuleContext p0);
    
    void preInit(final ModuleContext p0);
    
    default void finish(final ModuleContext context) {
    }
}
