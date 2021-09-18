package ru.gravit.launchserver.socket.websocket.json.profile;

import io.netty.channel.ChannelHandlerContext;
import ru.gravit.launcher.events.request.BatchProfileByUsernameRequestEvent;
import ru.gravit.launcher.profiles.PlayerProfile;
import ru.gravit.launchserver.LaunchServer;
import ru.gravit.launchserver.socket.Client;
import ru.gravit.launchserver.socket.websocket.WebSocketService;
import ru.gravit.launchserver.socket.websocket.json.JsonResponseInterface;

import java.util.UUID;

public class BatchProfileByUsername implements JsonResponseInterface {
    class Entry
    {
        String username;
        String client;
    }
    Entry[] list;
    @Override
    public String getType() {
        return "batchProfileByUsername";
    }

    @Override
    public void execute(WebSocketService service, ChannelHandlerContext ctx, Client client) throws Exception {
        BatchProfileByUsernameRequestEvent result = new BatchProfileByUsernameRequestEvent();
        result.playerProfiles = new PlayerProfile[list.length];
        for(int i=0;i<list.length;++i)
        {
            UUID uuid = client.auth.handler.usernameToUUID(list[i].username);
            result.playerProfiles[i] = ProfileByUUIDResponse.getProfile(LaunchServer.server,uuid,list[i].username,list[i].client, client.auth.textureProvider);
        }
        service.sendObject(ctx, result);
    }
}
