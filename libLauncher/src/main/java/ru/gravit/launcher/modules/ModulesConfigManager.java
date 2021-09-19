package ru.gravit.launcher.modules;

import java.nio.file.Path;

public interface ModulesConfigManager
{
    Path getModuleConfig(final String p0);
    
    Path getModuleConfigDir(final String p0);
}
