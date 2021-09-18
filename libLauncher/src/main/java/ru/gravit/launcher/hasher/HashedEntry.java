package ru.gravit.launcher.hasher;

import java.io.IOException;
import ru.gravit.launcher.serialize.HInput;
import ru.gravit.launcher.serialize.stream.EnumSerializer;
import ru.gravit.launcher.LauncherAPI;
import ru.gravit.launcher.serialize.stream.StreamObject;

public abstract class HashedEntry extends StreamObject
{
    @LauncherAPI
    public boolean flag;
    
    @LauncherAPI
    public abstract Type getType();
    
    @LauncherAPI
    public abstract long size();
    
    @LauncherAPI
    public enum Type implements EnumSerializer.Itf
    {
        DIR(1), 
        FILE(2);
        
        private static final EnumSerializer<Type> SERIALIZER;
        private final int n;
        
        public static Type read(final HInput input) throws IOException {
            return Type.SERIALIZER.read(input);
        }
        
        private Type(final int n) {
            this.n = n;
        }
        
        @Override
        public int getNumber() {
            return this.n;
        }
        
        static {
            SERIALIZER = new EnumSerializer<Type>(Type.class);
        }
    }
}
