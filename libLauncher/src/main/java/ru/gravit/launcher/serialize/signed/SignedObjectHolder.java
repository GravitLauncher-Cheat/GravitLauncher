package ru.gravit.launcher.serialize.signed;

import java.security.interfaces.RSAPrivateKey;
import java.security.SignatureException;
import java.io.IOException;
import java.security.interfaces.RSAPublicKey;
import ru.gravit.launcher.serialize.HInput;
import ru.gravit.launcher.LauncherAPI;
import ru.gravit.launcher.serialize.stream.StreamObject;

public final class SignedObjectHolder<O extends StreamObject> extends SignedBytesHolder
{
    @LauncherAPI
    public final O object;
    
    @LauncherAPI
    public SignedObjectHolder(final HInput input, final RSAPublicKey publicKey, final Adapter<O> adapter) throws IOException, SignatureException {
        super(input, publicKey);
        this.object = this.newInstance(adapter);
    }
    
    @LauncherAPI
    public SignedObjectHolder(final O object, final RSAPrivateKey privateKey) throws IOException {
        super(object.write(), privateKey);
        this.object = object;
    }
    
    @Override
    public boolean equals(final Object obj) {
        return obj instanceof SignedObjectHolder && this.object.equals(((SignedObjectHolder)obj).object);
    }
    
    @Override
    public int hashCode() {
        return this.object.hashCode();
    }
    
    @LauncherAPI
    public O newInstance(final Adapter<O> adapter) throws IOException {
        try (final HInput input = new HInput(this.bytes)) {
            return adapter.convert(input);
        }
    }
    
    @Override
    public String toString() {
        return this.object.toString();
    }
}
