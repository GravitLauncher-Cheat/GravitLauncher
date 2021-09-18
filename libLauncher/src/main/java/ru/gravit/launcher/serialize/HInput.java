// 
// Decompiled by Procyon v0.5.36
// 

package ru.gravit.launcher.serialize;

import java.util.UUID;
import java.io.EOFException;
import java.math.BigInteger;
import ru.gravit.utils.helper.IOHelper;
import java.io.IOException;
import java.util.Objects;
import java.io.ByteArrayInputStream;
import ru.gravit.launcher.LauncherAPI;
import java.io.InputStream;

public final class HInput implements AutoCloseable
{
    @LauncherAPI
    public final InputStream stream;
    
    @LauncherAPI
    public HInput(final byte[] bytes) {
        this.stream = new ByteArrayInputStream(bytes);
    }
    
    @LauncherAPI
    public HInput(final InputStream stream) {
        this.stream = Objects.requireNonNull(stream, "stream");
    }
    
    @Override
    public void close() throws IOException {
        this.stream.close();
    }
    
    @LauncherAPI
    public String readASCII(final int maxBytes) throws IOException {
        return IOHelper.decodeASCII(this.readByteArray(maxBytes));
    }
    
    @LauncherAPI
    public BigInteger readBigInteger(final int maxBytes) throws IOException {
        return new BigInteger(this.readByteArray(maxBytes));
    }
    
    @LauncherAPI
    public boolean readBoolean() throws IOException {
        final int b = this.readUnsignedByte();
        switch (b) {
            case 0: {
                return false;
            }
            case 1: {
                return true;
            }
            default: {
                throw new IOException("Invalid boolean state: " + b);
            }
        }
    }
    
    @LauncherAPI
    public byte[] readByteArray(final int max) throws IOException {
        final byte[] bytes = new byte[this.readLength(max)];
        IOHelper.read(this.stream, bytes);
        return bytes;
    }
    
    @LauncherAPI
    public int readInt() throws IOException {
        return (this.readUnsignedByte() << 24) + (this.readUnsignedByte() << 16) + (this.readUnsignedByte() << 8) + this.readUnsignedByte();
    }
    
    @LauncherAPI
    public int readLength(final int max) throws IOException {
        if (max < 0) {
            return -max;
        }
        return IOHelper.verifyLength(this.readVarInt(), max);
    }
    
    @LauncherAPI
    public long readLong() throws IOException {
        return (long)this.readInt() << 32 | ((long)this.readInt() & 0xFFFFFFFFL);
    }
    
    @LauncherAPI
    public short readShort() throws IOException {
        return (short)((this.readUnsignedByte() << 8) + this.readUnsignedByte());
    }
    
    @LauncherAPI
    public String readString(final int maxBytes) throws IOException {
        return IOHelper.decode(this.readByteArray(maxBytes));
    }
    
    @LauncherAPI
    public int readUnsignedByte() throws IOException {
        final int b = this.stream.read();
        if (b < 0) {
            throw new EOFException("readUnsignedByte");
        }
        return b;
    }
    
    @LauncherAPI
    public int readUnsignedShort() throws IOException {
        return Short.toUnsignedInt(this.readShort());
    }
    
    @LauncherAPI
    public UUID readUUID() throws IOException {
        return new UUID(this.readLong(), this.readLong());
    }
    
    @LauncherAPI
    public int readVarInt() throws IOException {
        int shift = 0;
        int result = 0;
        while (shift < 32) {
            final int b = this.readUnsignedByte();
            result |= (b & 0x7F) << shift;
            if ((b & 0x80) == 0x0) {
                return result;
            }
            shift += 7;
        }
        throw new IOException("VarInt too big");
    }
    
    @LauncherAPI
    public long readVarLong() throws IOException {
        int shift = 0;
        long result = 0L;
        while (shift < 64) {
            final int b = this.readUnsignedByte();
            result |= (long)(b & 0x7F) << shift;
            if ((b & 0x80) == 0x0) {
                return result;
            }
            shift += 7;
        }
        throw new IOException("VarLong too big");
    }
}
