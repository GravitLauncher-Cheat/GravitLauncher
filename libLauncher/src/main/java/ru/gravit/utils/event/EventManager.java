package ru.gravit.utils.event;

import ru.gravit.utils.helper.LogHelper;
import java.util.Iterator;
import java.util.Arrays;
import java.util.UUID;
import ru.gravit.utils.helper.CommonHelper;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class EventManager
{
    public static final int QUEUE_MAX_SIZE = 2048;
    public static final int INITIAL_HANDLERS_SIZE = 16;
    private EventExecutor executor;
    private Thread executorThread;
    private AtomicBoolean isStarted;
    public ArrayList<Entry> handlers;
    public BlockingQueue<QueueEntry> queue;
    
    public EventManager() {
        this.isStarted = new AtomicBoolean(false);
        this.handlers = new ArrayList<Entry>(16);
        this.queue = new LinkedBlockingQueue<QueueEntry>(2048);
    }
    
    public synchronized void start() {
        if (this.isStarted.get()) {
            return;
        }
        this.executor = new EventExecutor();
        this.isStarted.set(true);
        (this.executorThread = CommonHelper.newThread("EventExecutor", true, this.executor)).start();
    }
    
    public synchronized void stop() {
        if (!this.isStarted.get()) {
            return;
        }
        this.executorThread.interrupt();
        try {
            this.executorThread.join();
        }
        catch (InterruptedException ex) {}
    }
    
    public int registerHandler(final EventHandler<EventInterface> func, final UUID[] events) {
        if (this.isStarted.get()) {
            throw new IllegalThreadStateException("It is forbidden to add a handler during thread operation.");
        }
        Arrays.sort(events);
        this.handlers.add(new Entry(func, events));
        return this.handlers.size();
    }
    
    public void unregisterHandler(final EventHandler<EventInterface> func) {
        if (this.isStarted.get()) {
            throw new IllegalThreadStateException("It is forbidden to remove a handler during thread operation.");
        }
        this.handlers.removeIf(e -> e.func.equals(func));
    }
    
    public void sendEvent(final UUID key, final EventInterface event, final boolean blocking) {
        if (blocking) {
            this.process(key, event);
        }
        else {
            this.queue.add(new QueueEntry(event, key));
        }
    }
    
    public void process(final UUID key, final EventInterface event) {
        for (final Entry e : this.handlers) {
            if (Arrays.binarySearch(e.events, key) >= 0) {
                e.func.run(key, event);
            }
        }
    }
    
    public class Entry
    {
        EventHandler<EventInterface> func;
        UUID[] events;
        
        public Entry(final EventHandler<EventInterface> func, final UUID[] events) {
            this.func = func;
            this.events = events;
        }
    }
    
    public class QueueEntry
    {
        EventInterface event;
        UUID key;
        
        public QueueEntry(final EventInterface event, final UUID key) {
            this.event = event;
            this.key = key;
        }
    }
    
    public class EventExecutor implements Runnable
    {
        public boolean enable;
        
        public EventExecutor() {
            this.enable = true;
        }
        
        @Override
        public void run() {
            while (this.enable && !Thread.interrupted()) {
                try {
                    final QueueEntry e = EventManager.this.queue.take();
                    EventManager.this.process(e.key, e.event);
                }
                catch (InterruptedException e2) {
                    LogHelper.error(e2);
                }
                Thread.yield();
            }
        }
    }
}
