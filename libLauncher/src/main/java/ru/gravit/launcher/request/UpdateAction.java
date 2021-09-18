// 
// Decompiled by Procyon v0.5.36
// 

package ru.gravit.launcher.request;

import ru.gravit.launcher.serialize.stream.EnumSerializer;
import ru.gravit.launcher.serialize.HOutput;
import java.io.IOException;
import ru.gravit.utils.helper.IOHelper;
import ru.gravit.launcher.serialize.HInput;
import ru.gravit.launcher.hasher.HashedEntry;
import ru.gravit.launcher.serialize.stream.StreamObject;

public final class UpdateAction extends StreamObject
{
    public static final UpdateAction CD_BACK;
    public static final UpdateAction FINISH;
    public final Type type;
    public final String name;
    public final HashedEntry entry;
    
    public UpdateAction(final HInput input) throws IOException {
        this.type = Type.read(input);
        this.name = ((this.type == Type.CD || this.type == Type.GET) ? IOHelper.verifyFileName(input.readString(255)) : null);
        this.entry = null;
    }
    
    public UpdateAction(final Type type, final String name, final HashedEntry entry) {
        this.type = type;
        this.name = name;
        this.entry = entry;
    }
    
    @Override
    public void write(final HOutput output) throws IOException {
        EnumSerializer.write(output, this.type);
        if (this.type == Type.CD || this.type == Type.GET) {
            output.writeString(this.name, 255);
        }
    }
    
    static {
        CD_BACK = new UpdateAction(Type.CD_BACK, null, null);
        FINISH = new UpdateAction(Type.FINISH, null, null);
    }
    
    public enum Type implements EnumSerializer.Itf
    {
        CD(1), 
        CD_BACK(2), 
        GET(3), 
        FINISH(255);
        
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
