package ru.gravit.utils;

import java.net.URL;
import ru.gravit.launcher.LauncherAPI;
import java.net.URLClassLoader;

public class PublicURLClassLoader extends URLClassLoader
{
    @LauncherAPI
    public static ClassLoader systemclassloader;
    public String nativePath;
    
    @LauncherAPI
    public static ClassLoader getSystemClassLoader() {
        return PublicURLClassLoader.systemclassloader;
    }
    
    public PublicURLClassLoader(final URL[] urls) {
        super(urls);
    }
    
    public PublicURLClassLoader(final URL[] urls, final ClassLoader parent) {
        super(urls, parent);
    }
    
    public String findLibrary(final String name) {
        return this.nativePath.concat(name);
    }
    
    public void addURL(final URL url) {
        super.addURL(url);
    }
    
    static {
        PublicURLClassLoader.systemclassloader = ClassLoader.getSystemClassLoader();
    }
}
