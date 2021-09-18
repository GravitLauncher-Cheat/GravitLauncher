package ru.gravit.launcher.serialize;

import java.util.UUID;
import java.math.BigInteger;
import ru.gravit.utils.helper.IOHelper;
import java.io.IOException;
import java.util.Objects;
import ru.gravit.launcher.LauncherAPI;
import java.io.OutputStream;
import java.io.Flushable;

public final class HOutput implements AutoCloseable, Flushable
{
    @LauncherAPI
    public final OutputStream stream;
    
    @LauncherAPI
    public HOutput(final OutputStream stream) {
        this.stream = Objects.requireNonNull(stream, "stream");
    }
    
    @Override
    public void close() throws IOException {
        this.stream.close();
    }
    
    @Override
    public void flush() throws IOException {
        this.stream.flush();
    }
    
    @LauncherAPI
    public void writeASCII(final String s, final int maxBytes) throws IOException {
        this.writeByteArray(IOHelper.encodeASCII(s), maxBytes);
    }
    
    @LauncherAPI
    public void writeBigInteger(final BigInteger bi, final int max) throws IOException {
        this.writeByteArray(bi.toByteArray(), max);
    }
    
    @LauncherAPI
    public void writeBoolean(final boolean b) throws IOException {
        this.writeUnsignedByte(b ? 1 : 0);
    }
    
    @LauncherAPI
    public void writeByteArray(final byte[] bytes, final int max) throws IOException {
        this.writeLength(bytes.length, max);
        this.stream.write(bytes);
    }
    
    @LauncherAPI
    public void writeInt(final int i) throws IOException {
        this.writeUnsignedByte(i >>> 24 & 0xFF);
        this.writeUnsignedByte(i >>> 16 & 0xFF);
        this.writeUnsignedByte(i >>> 8 & 0xFF);
        this.writeUnsignedByte(i & 0xFF);
    }
    
    @LauncherAPI
    public void writeLength(final int length, final int max) throws IOException {
        IOHelper.verifyLength(length, max);
        if (max >= 0) {
            this.writeVarInt(length);
        }
    }
    
    @LauncherAPI
    public void writeLong(final long l) throws IOException {
        this.writeInt((int)(l >> 32));
        this.writeInt((int)l);
    }
    
    @LauncherAPI
    public void writeShort(final short s) throws IOException {
        this.writeUnsignedByte(s >>> 8 & 0xFF);
        this.writeUnsignedByte(s & 0xFF);
    }
    
    @LauncherAPI
    public void writeString(final String s, final int maxBytes) throws IOException {
        this.writeByteArray(IOHelper.encode(s), maxBytes);
    }
    
    @LauncherAPI
    public void writeUnsignedByte(final int b) throws IOException {
        this.stream.write(b);
    }
    
    @LauncherAPI
    public void writeUUID(final UUID uuid) throws IOException {
        this.writeLong(uuid.getMostSignificantBits());
        this.writeLong(uuid.getLeastSignificantBits());
    }
    
    @LauncherAPI
    public void writeVarInt(int i) throws IOException {
        while (((long)i & 0xFFFFFFFFFFFFFF80L) != 0x0L) {
            this.writeUnsignedByte((i & 0x7F) | 0x80);
            i >>>= 7;
        }
        this.writeUnsignedByte(i);
    }
    
    @LauncherAPI
    public void writeVarLong(long l) throws IOException {
        while ((l & 0xFFFFFFFFFFFFFF80L) != 0x0L) {
            this.writeUnsignedByte(((int)l & 0x7F) | 0x80);
            l >>>= 7;
        }
        this.writeUnsignedByte((int)l);
    }
}
