// 
// Decompiled by Procyon v0.5.36
// 

package ru.gravit.launcher.serialize.signed;

import ru.gravit.launcher.serialize.HOutput;
import java.io.IOException;
import ru.gravit.launcher.serialize.HInput;
import java.security.interfaces.RSAPrivateKey;
import java.security.SignatureException;
import ru.gravit.launcher.LauncherAPI;
import ru.gravit.utils.helper.SecurityHelper;
import java.security.interfaces.RSAPublicKey;
import ru.gravit.launcher.serialize.stream.StreamObject;

public class SignedBytesHolder extends StreamObject
{
    protected final byte[] bytes;
    private final byte[] sign;
    
    @LauncherAPI
    public SignedBytesHolder(final byte[] bytes, final byte[] sign, final RSAPublicKey publicKey) throws SignatureException {
        SecurityHelper.verifySign(bytes, sign, publicKey);
        this.bytes = bytes.clone();
        this.sign = sign.clone();
    }
    
    @LauncherAPI
    public SignedBytesHolder(final byte[] bytes, final RSAPrivateKey privateKey) {
        this.bytes = bytes.clone();
        this.sign = SecurityHelper.sign(bytes, privateKey);
    }
    
    @LauncherAPI
    public SignedBytesHolder(final HInput input, final RSAPublicKey publicKey) throws IOException, SignatureException {
        this(input.readByteArray(0), input.readByteArray(-256), publicKey);
    }
    
    @LauncherAPI
    public final byte[] getBytes() {
        return this.bytes.clone();
    }
    
    @LauncherAPI
    public final byte[] getSign() {
        return this.sign.clone();
    }
    
    @Override
    public final void write(final HOutput output) throws IOException {
        output.writeByteArray(this.bytes, 0);
        output.writeByteArray(this.sign, -256);
    }
}
