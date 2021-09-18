package ru.gravit.launcher.gui;

public interface RuntimeProvider
{
    void run(final String[] p0) throws Exception;
    
    void preLoad() throws Exception;
    
    void init(final boolean p0) throws Exception;
}
