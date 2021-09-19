// 
// Decompiled by Procyon v0.5.36
// 

package ru.gravit.launcher.modules;

import java.net.URL;

public interface ModulesManager extends AutoCloseable
{
    void initModules();
    
    void load(final Module p0);
    
    void loadModule(final URL p0) throws Exception;
    
    void loadModule(final URL p0, final String p1) throws Exception;
    
    void postInitModules();
    
    void preInitModules();
    
    void finishModules();
    
    void printModules();
    
    void sort();
    
    void registerModule(final Module p0);
}
