// 
// Decompiled by Procyon v0.5.36
// 

package ru.gravit.launcher.events.request;

import ru.gravit.launcher.request.ResultInterface;

public class ExecCommandRequestEvent implements ResultInterface
{
    boolean success;
    
    @Override
    public String getType() {
        return "execCmd";
    }
    
    public ExecCommandRequestEvent(final boolean success) {
        this.success = success;
    }
}
