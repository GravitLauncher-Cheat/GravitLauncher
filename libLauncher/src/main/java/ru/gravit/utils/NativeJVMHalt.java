package ru.gravit.utils;

import ru.gravit.utils.helper.LogHelper;

public final class NativeJVMHalt
{
    public int haltCode;
    
    public NativeJVMHalt(final int haltCode) {
        this.haltCode = haltCode;
        LogHelper.error("JVM exit code %d", haltCode);
        this.halt();
    }
    
    public native void halt();
}
