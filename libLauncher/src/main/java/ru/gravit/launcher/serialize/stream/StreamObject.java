package ru.gravit.launcher.serialize.stream;

import ru.gravit.launcher.serialize.HInput;
import ru.gravit.launcher.LauncherAPI;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import ru.gravit.launcher.serialize.HOutput;
import ru.gravit.utils.helper.IOHelper;

public abstract class StreamObject
{
    @LauncherAPI
    public final byte[] write() throws IOException {
        try (final ByteArrayOutputStream array = IOHelper.newByteArrayOutput()) {
            try (final HOutput output = new HOutput(array)) {
                this.write(output);
            }
            return array.toByteArray();
        }
    }
    
    @LauncherAPI
    public abstract void write(final HOutput p0) throws IOException;
    
    @FunctionalInterface
    public interface Adapter<O extends StreamObject>
    {
        @LauncherAPI
        O convert(final HInput p0) throws IOException;
    }
}
