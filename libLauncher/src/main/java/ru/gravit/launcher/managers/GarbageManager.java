package ru.gravit.launcher.managers;

import java.util.TimerTask;
import ru.gravit.launcher.NeedGarbageCollection;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Timer;

public class GarbageManager
{
    private static final Timer timer;
    private static final ArrayList<Entry> NEED_GARBARE_COLLECTION;
    
    public static void gc() {
        for (final Entry gc : GarbageManager.NEED_GARBARE_COLLECTION) {
            gc.invoke.garbageCollection();
        }
    }
    
    public static void registerNeedGC(final NeedGarbageCollection gc) {
        GarbageManager.NEED_GARBARE_COLLECTION.add(new Entry(gc, 0L));
    }
    
    public static void registerNeedGC(final NeedGarbageCollection gc, final long time) {
        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                gc.garbageCollection();
            }
        };
        GarbageManager.timer.schedule(task, time);
        GarbageManager.NEED_GARBARE_COLLECTION.add(new Entry(gc, time));
    }
    
    public static void unregisterNeedGC(final NeedGarbageCollection gc) {
        GarbageManager.NEED_GARBARE_COLLECTION.removeIf(e -> e.invoke == gc);
    }
    
    static {
        timer = new Timer("GarbageTimer");
        NEED_GARBARE_COLLECTION = new ArrayList<Entry>();
    }
    
    static class Entry
    {
        NeedGarbageCollection invoke;
        long timer;
        
        public Entry(final NeedGarbageCollection invoke, final long timer) {
            this.invoke = invoke;
            this.timer = timer;
        }
    }
}
