// 
// Decompiled by Procyon v0.5.36
// 

package ru.gravit.launcher.request.admin;

import ru.gravit.launcher.serialize.HOutput;
import ru.gravit.launcher.serialize.HInput;
import ru.gravit.launcher.request.RequestType;
import ru.gravit.utils.helper.LogHelper;
import ru.gravit.launcher.request.Request;

public class ExecCommandRequest extends Request<Boolean>
{
    public LogHelper.Output loutput;
    public String cmd;
    
    public ExecCommandRequest(final LogHelper.Output output, final String cmd) {
        this.loutput = output;
        this.cmd = cmd;
    }
    
    @Override
    public Integer getLegacyType() {
        return RequestType.EXECCOMMAND.getNumber();
    }
    
    @Override
    protected Boolean requestDo(final HInput input, final HOutput output) throws Exception {
        this.readError(input);
        output.writeString(this.cmd, 2048);
        boolean isContinue = true;
        while (isContinue) {
            isContinue = input.readBoolean();
            if (isContinue) {
                final String log = input.readString(2048);
                if (this.loutput == null) {
                    continue;
                }
                this.loutput.println(log);
            }
        }
        this.readError(input);
        return true;
    }
}
