package ru.gravit.launcher.request.auth;

import ru.gravit.launcher.serialize.HOutput;
import ru.gravit.launcher.serialize.HInput;
import java.net.InetSocketAddress;
import ru.gravit.launcher.Launcher;
import ru.gravit.launcher.request.RequestType;
import ru.gravit.launcher.request.Request;

public class ChangeServerRequest extends Request
{
    @Override
    public Integer getLegacyType() {
        return RequestType.CHANGESERVER.getNumber();
    }
    
    public boolean change(final Result result) {
        if (!result.needChange) {
            return false;
        }
        Launcher.getConfig().address = InetSocketAddress.createUnresolved(result.address, result.port);
        return true;
    }
    
    @Override
    protected Result requestDo(final HInput input, final HOutput output) throws Exception {
        this.readError(input);
        final Result result = new Result();
        result.needChange = input.readBoolean();
        if (result.needChange) {
            result.address = input.readString(255);
            result.port = input.readInt();
        }
        if (result.needChange) {
            this.change(result);
        }
        return result;
    }
    
    public class Result
    {
        public boolean needChange;
        public String address;
        public int port;
    }
}
