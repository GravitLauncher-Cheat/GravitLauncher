package ru.gravit.launcher.managers;

import java.nio.file.FileVisitResult;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.SimpleFileVisitor;
import java.net.URISyntaxException;
import java.util.jar.JarFile;
import java.nio.file.Paths;
import java.net.URL;
import java.util.Iterator;
import java.io.IOException;
import java.nio.file.FileVisitor;
import ru.gravit.utils.helper.IOHelper;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import ru.gravit.utils.helper.LogHelper;
import java.nio.file.Path;
import ru.gravit.launcher.modules.ModuleContext;
import ru.gravit.utils.PublicURLClassLoader;
import ru.gravit.launcher.modules.Module;
import java.util.ArrayList;
import ru.gravit.launcher.modules.ModulesManager;

public class SimpleModuleManager implements ModulesManager
{
    public ArrayList<Module> modules;
    public PublicURLClassLoader classloader;
    protected ModuleContext context;
    
    public void autoload(final Path dir) throws IOException {
        LogHelper.info("Load modules");
        if (Files.notExists(dir, new LinkOption[0])) {
            Files.createDirectory(dir, (FileAttribute<?>[])new FileAttribute[0]);
        }
        IOHelper.walk(dir, new ModulesVisitor(), true);
        this.sort();
        LogHelper.info("Loaded %d modules", this.modules.size());
    }
    
    @Override
    public void sort() {
        this.modules.sort((m1, m2) -> {
            final int p1 = m1.getPriority();
            final int p2 = m2.getPriority();
            return Integer.compare(p2, p1);
        });
    }
    
    @Override
    public void close() {
        for (final Module m : this.modules) {
            try {
                m.close();
            }
            catch (Throwable t) {
                if (m.getName() != null) {
                    LogHelper.error("Error in stopping module: %s", m.getName());
                }
                else {
                    LogHelper.error("Error in stopping one of modules");
                }
                LogHelper.error(t);
            }
        }
    }
    
    @Override
    public void initModules() {
        for (final Module m : this.modules) {
            m.init(this.context);
            LogHelper.info("Module %s version: %s init", m.getName(), m.getVersion());
        }
    }
    
    @Override
    public void load(final Module module) {
        this.modules.add(module);
    }
    
    public void loadModuleFull(final URL jarpath) throws ClassNotFoundException, IllegalAccessException, InstantiationException, URISyntaxException, IOException {
        try (final JarFile f = new JarFile(Paths.get(jarpath.toURI()).toFile())) {
            this.classloader.addURL(jarpath);
            final Module module = (Module)Class.forName(f.getManifest().getMainAttributes().getValue("Main-Class"), true, this.classloader).newInstance();
            this.modules.add(module);
            module.preInit(this.context);
            module.init(this.context);
            module.postInit(this.context);
            module.finish(this.context);
            LogHelper.info("Module %s version: %s loaded", module.getName(), module.getVersion());
        }
    }
    
    @Override
    public void loadModule(final URL jarpath) throws Exception {
        try (final JarFile f = new JarFile(Paths.get(jarpath.toURI()).toFile())) {
            this.loadModule(jarpath, f.getManifest().getMainAttributes().getValue("Main-Class"));
        }
    }
    
    @Override
    public void loadModule(final URL jarpath, final String classname) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        this.classloader.addURL(jarpath);
        final Module module = (Module)Class.forName(classname, true, this.classloader).newInstance();
        this.modules.add(module);
        LogHelper.info("Module %s version: %s loaded", module.getName(), module.getVersion());
    }
    
    @Override
    public void postInitModules() {
        for (final Module m : this.modules) {
            m.postInit(this.context);
            LogHelper.info("Module %s version: %s post-init", m.getName(), m.getVersion());
        }
    }
    
    @Override
    public void preInitModules() {
        for (final Module m : this.modules) {
            m.preInit(this.context);
            LogHelper.info("Module %s version: %s pre-init", m.getName(), m.getVersion());
        }
    }
    
    @Override
    public void printModules() {
        for (final Module m : this.modules) {
            LogHelper.info("Module %s version: %s", m.getName(), m.getVersion());
        }
        LogHelper.info("Loaded %d modules", this.modules.size());
    }
    
    @Override
    public void registerModule(final Module module) {
        this.modules.add(module);
        LogHelper.info("Module %s version: %s registered", module.getName(), module.getVersion());
    }
    
    @Override
    public void finishModules() {
        for (final Module m : this.modules) {
            m.finish(this.context);
            LogHelper.info("Module %s version: %s finished initialization", m.getName(), m.getVersion());
        }
    }
    
    protected final class ModulesVisitor extends SimpleFileVisitor<Path>
    {
        private ModulesVisitor() {
        }
        
        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
            if (file.toFile().getName().endsWith(".jar")) {
                try (final JarFile f = new JarFile(file.toFile())) {
                    SimpleModuleManager.this.loadModule(file.toUri().toURL(), f.getManifest().getMainAttributes().getValue("Main-Class"));
                }
                catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex2) {
                    final ReflectiveOperationException ex;
                    final ReflectiveOperationException e = ex2;
                    LogHelper.error(e);
                }
            }
            return super.visitFile(file, attrs);
        }
    }
}
