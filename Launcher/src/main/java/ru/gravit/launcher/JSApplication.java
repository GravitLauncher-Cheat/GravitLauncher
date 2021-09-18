package ru.gravit.launcher;

import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Application;

public abstract class JSApplication extends Application
{
    private static final AtomicReference<JSApplication> INSTANCE;
    
    @LauncherAPI
    public static JSApplication getInstance() {
        return JSApplication.INSTANCE.get();
    }
    
    public JSApplication() {
        JSApplication.INSTANCE.set(this);
    }
    
    static {
        INSTANCE = new AtomicReference<JSApplication>();
    }
}
