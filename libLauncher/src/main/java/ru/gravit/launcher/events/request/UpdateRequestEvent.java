// 
// Decompiled by Procyon v0.5.36
// 

package ru.gravit.launcher.events.request;

import ru.gravit.launcher.LauncherNetworkAPI;
import ru.gravit.launcher.hasher.HashedDir;
import ru.gravit.launcher.request.ResultInterface;

public class UpdateRequestEvent implements ResultInterface
{
    @LauncherNetworkAPI
    public HashedDir hdir;
    
    @Override
    public String getType() {
        return "update";
    }
    
    public UpdateRequestEvent(final HashedDir hdir) {
        this.hdir = hdir;
    }
}
