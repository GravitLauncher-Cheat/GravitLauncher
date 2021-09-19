package ru.gravit.launcher.profiles;

import ru.gravit.launcher.serialize.HOutput;
import java.util.Objects;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.io.IOException;
import ru.gravit.utils.helper.IOHelper;
import ru.gravit.launcher.serialize.HInput;
import ru.gravit.launcher.LauncherAPI;
import ru.gravit.utils.helper.SecurityHelper;
import ru.gravit.launcher.serialize.stream.StreamObject;

public final class Texture extends StreamObject
{
    private static final SecurityHelper.DigestAlgorithm DIGEST_ALGO;
    @LauncherAPI
    public final String url;
    @LauncherAPI
    public final byte[] digest;
    
    @LauncherAPI
    public Texture(final HInput input) throws IOException {
        this.url = IOHelper.verifyURL(input.readASCII(2048));
        this.digest = input.readByteArray(-Texture.DIGEST_ALGO.bytes);
    }
    
    @LauncherAPI
    public Texture(final String url, final boolean cloak) throws IOException {
        this.url = IOHelper.verifyURL(url);
		byte[] texture;
        try (final InputStream input = IOHelper.newInput(new URL(url))) {
            texture = IOHelper.read(input);
        }
        try (final ByteArrayInputStream input2 = new ByteArrayInputStream(texture)) {
            IOHelper.readTexture(input2, cloak);
        }
        this.digest = SecurityHelper.digest(Texture.DIGEST_ALGO, new URL(url));
    }
    
    @LauncherAPI
    public Texture(final String url, final byte[] digest) {
        this.url = IOHelper.verifyURL(url);
        this.digest = Objects.requireNonNull(digest, "digest");
    }
    
    @Override
    public void write(final HOutput output) throws IOException {
        output.writeASCII(this.url, 2048);
        output.writeByteArray(this.digest, -Texture.DIGEST_ALGO.bytes);
    }
    
    static {
        DIGEST_ALGO = SecurityHelper.DigestAlgorithm.SHA256;
    }
}
