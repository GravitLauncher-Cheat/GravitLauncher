// 
// Decompiled by Procyon v0.5.36
// 

package ru.gravit.launcher.request.websockets;

import ru.gravit.utils.helper.LogHelper;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import java.util.Map;
import java.net.URI;
import org.java_websocket.client.WebSocketClient;

public class ClientJSONPoint extends WebSocketClient
{
    public ClientJSONPoint(final URI serverUri, final Map<String, String> httpHeaders, final int connectTimeout) {
        super(serverUri, new Draft_6455(), httpHeaders, connectTimeout);
    }
    
    @Override
    public void onOpen(final ServerHandshake handshakedata) {
    }
    
    @Override
    public void onMessage(final String message) {
    }
    
    @Override
    public void onClose(final int code, final String reason, final boolean remote) {
        LogHelper.debug(("Disconnected: " + code + " " + remote + " " + reason != null) ? reason : "no reason");
    }
    
    @Override
    public void onError(final Exception ex) {
        LogHelper.error(ex);
    }
}
