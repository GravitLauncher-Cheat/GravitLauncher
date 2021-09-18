// 
// Decompiled by Procyon v0.5.36
// 

package ru.gravit.launcher;

public interface HWID
{
    String getSerializeString();
    
    int getLevel();
    
    int compare(final HWID p0);
    
    boolean isNull();
}
