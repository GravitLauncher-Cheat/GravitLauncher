package ru.gravit.launcher.managers;

public class HasherManager
{
    public static final HasherStore defaultStore;
    
    public static HasherStore getDefaultStore() {
        return HasherManager.defaultStore;
    }
    
    static {
        defaultStore = new HasherStore();
    }
}
