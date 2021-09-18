// 
// Decompiled by Procyon v0.5.36
// 

package ru.gravit.launcher.request.websockets;

import java.util.Iterator;
import ru.gravit.utils.helper.LogHelper;
import ru.gravit.launcher.request.ResultInterface;
import java.util.HashSet;

public class WaitEventHandler implements ClientWebSocketService.EventHandler
{
    public HashSet<ResultEvent> requests;
    
    public WaitEventHandler() {
        this.requests = new HashSet<ResultEvent>();
    }
    
    @Override
    public void process(final ResultInterface result) {
        LogHelper.debug("Processing event %s type", result.getType());
        for (final ResultEvent r : this.requests) {
            LogHelper.subDebug("Processing %s", r.type);
            if (r.type.equals(result.getType()) || result.getType().equals("error")) {
                LogHelper.debug("Event %s type", r.type);
                synchronized (r) {
                    r.result = result;
                    r.ready = true;
                    r.notifyAll();
                }
            }
        }
    }
    
    public static class ResultEvent
    {
        public ResultInterface result;
        public String type;
        public boolean ready;
    }
}
