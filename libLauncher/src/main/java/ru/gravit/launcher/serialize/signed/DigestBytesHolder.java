package ru.gravit.launcher.serialize.signed;

import ru.gravit.launcher.serialize.HOutput;
import java.io.IOException;
import ru.gravit.launcher.serialize.HInput;
import ru.gravit.launcher.LauncherAPI;
import java.security.SignatureException;
import java.util.Arrays;
import ru.gravit.utils.helper.SecurityHelper;
import ru.gravit.launcher.serialize.stream.StreamObject;

public class DigestBytesHolder extends StreamObject
{
    protected final byte[] bytes;
    private final byte[] digest;
    
    @LauncherAPI
    public DigestBytesHolder(final byte[] bytes, final byte[] digest, final SecurityHelper.DigestAlgorithm algorithm) throws SignatureException {
        if (Arrays.equals(SecurityHelper.digest(algorithm, bytes), digest)) {
            throw new SignatureException("Invalid digest");
        }
        this.bytes = bytes.clone();
        this.digest = digest.clone();
    }
    
    @LauncherAPI
    public DigestBytesHolder(final byte[] bytes, final SecurityHelper.DigestAlgorithm algorithm) {
        this.bytes = bytes.clone();
        this.digest = SecurityHelper.digest(algorithm, bytes);
    }
    
    @LauncherAPI
    public DigestBytesHolder(final HInput input, final SecurityHelper.DigestAlgorithm algorithm) throws IOException, SignatureException {
        this(input.readByteArray(0), input.readByteArray(-256), algorithm);
    }
    
    @LauncherAPI
    public final byte[] getBytes() {
        return this.bytes.clone();
    }
    
    @LauncherAPI
    public final byte[] getDigest() {
        return this.digest.clone();
    }
    
    @Override
    public final void write(final HOutput output) throws IOException {
        output.writeByteArray(this.bytes, 0);
        output.writeByteArray(this.digest, -256);
    }
}
