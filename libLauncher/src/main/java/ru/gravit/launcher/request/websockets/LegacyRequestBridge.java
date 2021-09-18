package ru.gravit.launcher.request.websockets;

import ru.gravit.launcher.Launcher;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import ru.gravit.launcher.request.RequestException;
import ru.gravit.launcher.events.request.ErrorRequestEvent;
import ru.gravit.utils.helper.LogHelper;
import ru.gravit.launcher.request.ResultInterface;

public class LegacyRequestBridge
{
    public static WaitEventHandler waitEventHandler;
    public static ClientWebSocketService service;
    
    public static ResultInterface sendRequest(final RequestInterface request) throws IOException, InterruptedException {
        final WaitEventHandler.ResultEvent e = new WaitEventHandler.ResultEvent();
        e.type = request.getType();
        LegacyRequestBridge.waitEventHandler.requests.add(e);
        LegacyRequestBridge.service.sendObject(request);
        while (!e.ready) {
            synchronized (e) {
                e.wait();
                LogHelper.debug("WAIT OK");
            }
        }
        final ResultInterface result = e.result;
        LegacyRequestBridge.waitEventHandler.requests.remove(e);
        if (e.result.getType().equals("error")) {
            final ErrorRequestEvent errorRequestEvent = (ErrorRequestEvent)e.result;
            throw new RequestException(errorRequestEvent.error);
        }
        return result;
    }
    
    public static void initWebSockets(final String address) {
        (LegacyRequestBridge.service = new ClientWebSocketService(new GsonBuilder(), address, 5000)).registerResults();
        LegacyRequestBridge.service.registerRequests();
        LegacyRequestBridge.service.registerHandler(LegacyRequestBridge.waitEventHandler);
        try {
            if (!LegacyRequestBridge.service.connectBlocking()) {
                LogHelper.error("Error connecting");
            }
            LogHelper.debug("Connect to %s", address);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    static {
        LegacyRequestBridge.waitEventHandler = new WaitEventHandler();
        if (Launcher.getConfig().nettyPort != 0) {
            initWebSockets(Launcher.getConfig().nettyAddress);
        }
    }
}
