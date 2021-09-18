// 
// Decompiled by Procyon v0.5.36
// 

package ru.gravit.launcher.request.update;

import java.io.IOException;
import ru.gravit.utils.helper.IOHelper;
import java.util.HashSet;
import ru.gravit.launcher.serialize.HOutput;
import ru.gravit.launcher.serialize.HInput;
import ru.gravit.launcher.request.RequestType;
import ru.gravit.launcher.request.websockets.LegacyRequestBridge;
import ru.gravit.launcher.LauncherAPI;
import ru.gravit.launcher.LauncherConfig;
import ru.gravit.launcher.request.websockets.RequestInterface;
import ru.gravit.launcher.events.request.UpdateListRequestEvent;
import ru.gravit.launcher.request.Request;

public final class UpdateListRequest extends Request<UpdateListRequestEvent> implements RequestInterface
{
    @LauncherAPI
    public UpdateListRequest() {
        this(null);
    }
    
    @LauncherAPI
    public UpdateListRequest(final LauncherConfig config) {
        super(config);
    }
    
    public UpdateListRequestEvent requestWebSockets() throws Exception {
        return (UpdateListRequestEvent)LegacyRequestBridge.sendRequest(this);
    }
    
    @Override
    public Integer getLegacyType() {
        return RequestType.UPDATE_LIST.getNumber();
    }
    
    @Override
    protected UpdateListRequestEvent requestDo(final HInput input, final HOutput output) throws IOException {
        final int count = input.readLength(0);
        final HashSet<String> result = new HashSet<String>(count);
        for (int i = 0; i < count; ++i) {
            result.add(IOHelper.verifyFileName(input.readString(255)));
        }
        return new UpdateListRequestEvent(result);
    }
    
    @Override
    public String getType() {
        return "updateList";
    }
}
