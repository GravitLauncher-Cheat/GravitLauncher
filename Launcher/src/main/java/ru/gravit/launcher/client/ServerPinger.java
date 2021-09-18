package ru.gravit.launcher.client;

import java.time.temporal.Temporal;
import java.time.Duration;
import com.google.gson.JsonObject;
import java.io.ByteArrayOutputStream;
import ru.gravit.utils.helper.VerifyHelper;
import ru.gravit.utils.helper.LogHelper;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import ru.gravit.utils.helper.IOHelper;
import ru.gravit.launcher.LauncherAPI;
import java.util.Objects;
import ru.gravit.launcher.serialize.HOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import ru.gravit.launcher.serialize.HInput;
import java.time.Instant;
import ru.gravit.launcher.profiles.ClientProfile;
import java.net.InetSocketAddress;
import java.util.regex.Pattern;
import com.google.gson.JsonParser;

public final class ServerPinger
{
    private JsonParser parser;
    private static final String LEGACY_PING_HOST_MAGIC = "§1";
    private static final String LEGACY_PING_HOST_CHANNEL = "MC|PingHost";
    private static final Pattern LEGACY_PING_HOST_DELIMETER;
    private static final int PACKET_LENGTH = 65535;
    private final InetSocketAddress address;
    private final ClientProfile.Version version;
    private final Object cacheLock;
    private Result cache;
    private Exception cacheException;
    private Instant cacheTime;
    
    private static String readUTF16String(final HInput input) throws IOException {
        final int length = input.readUnsignedShort() << 1;
        final byte[] encoded = input.readByteArray(-length);
        return new String(encoded, StandardCharsets.UTF_16BE);
    }
    
    private static void writeUTF16String(final HOutput output, final String s) throws IOException {
        output.writeShort((short)s.length());
        output.stream.write(s.getBytes(StandardCharsets.UTF_16BE));
    }
    
    @LauncherAPI
    public ServerPinger(final InetSocketAddress address, final ClientProfile.Version version) {
        this.parser = new JsonParser();
        this.cacheLock = new Object();
        this.cache = null;
        this.cacheException = null;
        this.cacheTime = null;
        this.address = Objects.requireNonNull(address, "address");
        this.version = Objects.requireNonNull(version, "version");
    }
    
    private Result doPing() throws IOException {
        try (final Socket socket = IOHelper.newSocket()) {
            socket.connect(IOHelper.resolve(this.address), IOHelper.SOCKET_TIMEOUT);
            try (final HInput input = new HInput(socket.getInputStream());
                 final HOutput output = new HOutput(socket.getOutputStream())) {
                return (this.version.compareTo(ClientProfile.Version.MC172) >= 0) ? this.modernPing(input, output) : this.legacyPing(input, output, this.version.compareTo(ClientProfile.Version.MC164) >= 0);
            }
        }
    }
    
    private Result legacyPing(final HInput input, final HOutput output, final boolean mc16) throws IOException {
        output.writeUnsignedByte(254);
        output.writeUnsignedByte(1);
        if (mc16) {
            output.writeUnsignedByte(250);
            writeUTF16String(output, "MC|PingHost");
            byte[] customPayloadPacket;
            try (final ByteArrayOutputStream packetArray = IOHelper.newByteArrayOutput()) {
                try (final HOutput packetOutput = new HOutput(packetArray)) {
                    packetOutput.writeUnsignedByte(this.version.protocol);
                    writeUTF16String(packetOutput, this.address.getHostString());
                    packetOutput.writeInt(this.address.getPort());
                }
                customPayloadPacket = packetArray.toByteArray();
            }
            output.writeShort((short)customPayloadPacket.length);
            output.stream.write(customPayloadPacket);
        }
        output.flush();
        final int kickPacketID = input.readUnsignedByte();
        if (kickPacketID != 255) {
            throw new IOException("Illegal kick packet ID: " + kickPacketID);
        }
        final String response = readUTF16String(input);
        LogHelper.debug("Ping response (legacy): '%s'", response);
        final String[] splitted = ServerPinger.LEGACY_PING_HOST_DELIMETER.split(response);
        if (splitted.length != 6) {
            throw new IOException("Tokens count mismatch");
        }
        final String magic = splitted[0];
        if (!magic.equals("§1")) {
            throw new IOException("Magic file mismatch: " + magic);
        }
        final int protocol = Integer.parseInt(splitted[1]);
        if (protocol != this.version.protocol) {
            throw new IOException("Protocol mismatch: " + protocol);
        }
        final String clientVersion = splitted[2];
        if (!clientVersion.equals(this.version.name)) {
            throw new IOException(String.format("Version mismatch: '%s'", clientVersion));
        }
        final int onlinePlayers = VerifyHelper.verifyInt(Integer.parseInt(splitted[4]), VerifyHelper.NOT_NEGATIVE, "onlinePlayers can't be < 0");
        final int maxPlayers = VerifyHelper.verifyInt(Integer.parseInt(splitted[5]), VerifyHelper.NOT_NEGATIVE, "maxPlayers can't be < 0");
        return new Result(onlinePlayers, maxPlayers, response);
    }
    
    private Result modernPing(final HInput input, final HOutput output) throws IOException {
        byte[] handshakePacket;
        try (final ByteArrayOutputStream packetArray = IOHelper.newByteArrayOutput()) {
            try (final HOutput packetOutput = new HOutput(packetArray)) {
                packetOutput.writeVarInt(0);
                packetOutput.writeVarInt(this.version.protocol);
                packetOutput.writeString(this.address.getHostString(), 0);
                packetOutput.writeShort((short)this.address.getPort());
                packetOutput.writeVarInt(1);
            }
            handshakePacket = packetArray.toByteArray();
        }
        output.writeByteArray(handshakePacket, 65535);
        output.writeVarInt(1);
        output.writeVarInt(0);
        output.flush();
        int ab;
        for (ab = 0; ab <= 0; ab = IOHelper.verifyLength(input.readVarInt(), 65535)) {}
        final byte[] statusPacket = input.readByteArray(-ab);
        String response;
        try (final HInput packetInput = new HInput(statusPacket)) {
            final int statusPacketID = packetInput.readVarInt();
            if (statusPacketID != 0) {
                throw new IOException("Illegal status packet ID: " + statusPacketID);
            }
            response = packetInput.readString(65535);
            LogHelper.debug("Ping response (modern): '%s'", response);
        }
        final JsonObject object = this.parser.parse(response).getAsJsonObject();
        final JsonObject playersObject = object.get("players").getAsJsonObject();
        final int online = playersObject.get("online").getAsInt();
        final int max = playersObject.get("max").getAsInt();
        return new Result(online, max, response);
    }
    
    @LauncherAPI
    public Result ping() throws IOException {
        Instant now = Instant.now();
        synchronized (cacheLock) {
            // Update ping cache
            if (cacheTime == null || Duration.between(now, cacheTime).toMillis() >= IOHelper.SOCKET_TIMEOUT) {
                cacheTime = now;
                try {
                    cache = doPing();
                    cacheException = null;
                } catch (IOException | IllegalArgumentException /* Protocol error */ e) {
                    cache = null;
                    cacheException = e;
                }
            }

            // Verify is result available
            if (cache == null) {
                if (cacheException instanceof IOException)
                    throw (IOException) cacheException;
                if (cacheException instanceof IllegalArgumentException)
                    throw (IllegalArgumentException) cacheException;
                cacheException = new IOException("Unavailable");
                throw (IOException) cacheException;
            }

            // We're done
            return cache;
        }
    }
    
    static {
        LEGACY_PING_HOST_DELIMETER = Pattern.compile("\u0000", 16);
    }
    
    public static final class Result
    {
        @LauncherAPI
        public final int onlinePlayers;
        @LauncherAPI
        public final int maxPlayers;
        @LauncherAPI
        public final String raw;
        
        public Result(final int onlinePlayers, final int maxPlayers, final String raw) {
            this.onlinePlayers = VerifyHelper.verifyInt(onlinePlayers, VerifyHelper.NOT_NEGATIVE, "onlinePlayers can't be < 0");
            this.maxPlayers = VerifyHelper.verifyInt(maxPlayers, VerifyHelper.NOT_NEGATIVE, "maxPlayers can't be < 0");
            this.raw = raw;
        }
        
        @LauncherAPI
        public boolean isOverfilled() {
            return this.onlinePlayers >= this.maxPlayers;
        }
    }
}
