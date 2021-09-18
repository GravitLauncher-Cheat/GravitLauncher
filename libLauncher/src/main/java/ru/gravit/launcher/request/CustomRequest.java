// 
// Decompiled by Procyon v0.5.36
// 

package ru.gravit.launcher.request;

import ru.gravit.utils.helper.VerifyHelper;
import ru.gravit.launcher.serialize.HOutput;
import ru.gravit.launcher.serialize.HInput;
import ru.gravit.launcher.LauncherAPI;
import ru.gravit.launcher.LauncherConfig;

public abstract class CustomRequest<T> extends Request<T>
{
    @LauncherAPI
    public CustomRequest() {
        this(null);
    }
    
    @LauncherAPI
    public CustomRequest(final LauncherConfig config) {
        super(config);
    }
    
    @LauncherAPI
    public abstract String getName();
    
    @Override
    public final Integer getLegacyType() {
        return 255;
    }
    
    @Override
    protected final T requestDo(final HInput input, final HOutput output) throws Exception {
        output.writeASCII(VerifyHelper.verifyIDName(this.getName()), 255);
        output.flush();
        return this.requestDoCustom(input, output);
    }
    
    @LauncherAPI
    protected abstract T requestDoCustom(final HInput p0, final HOutput p1);
}
