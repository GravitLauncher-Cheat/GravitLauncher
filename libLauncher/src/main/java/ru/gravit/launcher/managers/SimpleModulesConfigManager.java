package ru.gravit.launcher.managers;

import java.io.IOException;
import ru.gravit.utils.helper.LogHelper;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import ru.gravit.utils.helper.IOHelper;
import java.nio.file.Path;
import ru.gravit.launcher.modules.ModulesConfigManager;

public class SimpleModulesConfigManager implements ModulesConfigManager
{
    public Path configDir;
    
    public SimpleModulesConfigManager(final Path configDir) {
        this.configDir = configDir;
    }
    
    @Override
    public Path getModuleConfig(final String moduleName) {
        if (!IOHelper.isDir(this.configDir)) {
            try {
                Files.createDirectories(this.configDir, (FileAttribute<?>[])new FileAttribute[0]);
            }
            catch (IOException e) {
                LogHelper.error(e);
            }
        }
        return this.configDir.resolve(moduleName.concat("Config.json"));
    }
    
    @Override
    public Path getModuleConfigDir(final String moduleName) {
        if (!IOHelper.isDir(this.configDir)) {
            try {
                Files.createDirectories(this.configDir, (FileAttribute<?>[])new FileAttribute[0]);
            }
            catch (IOException e) {
                LogHelper.error(e);
            }
        }
        return this.configDir.resolve(moduleName);
    }
}
