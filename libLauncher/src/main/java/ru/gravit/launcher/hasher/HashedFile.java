package ru.gravit.launcher.hasher;

import ru.gravit.launcher.serialize.HOutput;
import ru.gravit.utils.helper.IOHelper;
import java.util.Arrays;
import java.nio.file.Path;
import ru.gravit.utils.helper.VerifyHelper;
import java.io.IOException;
import ru.gravit.launcher.serialize.HInput;
import ru.gravit.launcher.LauncherAPI;
import ru.gravit.utils.helper.SecurityHelper;

public final class HashedFile extends HashedEntry
{
    public static final SecurityHelper.DigestAlgorithm DIGEST_ALGO;
    @LauncherAPI
    public final long size;
    private final byte[] digest;
    
    @LauncherAPI
    public HashedFile(final HInput input) throws IOException {
        this(input.readVarLong(), (byte[])(input.readBoolean() ? input.readByteArray(-HashedFile.DIGEST_ALGO.bytes) : null));
    }
    
    @LauncherAPI
    public HashedFile(final long size, final byte[] digest) {
        this.size = VerifyHelper.verifyLong(size, VerifyHelper.L_NOT_NEGATIVE, "Illegal size: " + size);
        this.digest = (byte[])((digest == null) ? null : ((byte[])HashedFile.DIGEST_ALGO.verify(digest).clone()));
    }
    
    @LauncherAPI
    public HashedFile(final Path file, final long size, final boolean digest) throws IOException {
        this(size, (byte[])(digest ? SecurityHelper.digest(HashedFile.DIGEST_ALGO, file) : null));
    }
    
    @Override
    public Type getType() {
        return Type.FILE;
    }
    
    @LauncherAPI
    public boolean isSame(final HashedFile o) {
        return this.size == o.size && (this.digest == null || o.digest == null || Arrays.equals(this.digest, o.digest));
    }
    
    @LauncherAPI
    public boolean isSame(final Path file, final boolean digest) throws IOException {
        if (this.size != IOHelper.readAttributes(file).size()) {
            return false;
        }
        if (!digest || this.digest == null) {
            return true;
        }
        final byte[] actualDigest = SecurityHelper.digest(HashedFile.DIGEST_ALGO, file);
        return Arrays.equals(this.digest, actualDigest);
    }
    
    @LauncherAPI
    public boolean isSameDigest(final byte[] digest) {
        return this.digest == null || digest == null || Arrays.equals(this.digest, digest);
    }
    
    @Override
    public long size() {
        return this.size;
    }
    
    @Override
    public void write(final HOutput output) throws IOException {
        output.writeVarLong(this.size);
        output.writeBoolean(this.digest != null);
        if (this.digest != null) {
            output.writeByteArray(this.digest, -HashedFile.DIGEST_ALGO.bytes);
        }
    }
    
    static {
        DIGEST_ALGO = SecurityHelper.DigestAlgorithm.MD5;
    }
}
