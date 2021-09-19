package ru.gravit.launcher.serialize.stream;

import ru.gravit.launcher.serialize.HInput;
import ru.gravit.utils.helper.VerifyHelper;
import java.util.HashMap;
import ru.gravit.launcher.LauncherAPI;
import java.io.IOException;
import ru.gravit.launcher.serialize.HOutput;
import java.util.Map;

public final class EnumSerializer<E extends Enum>
{
    private final Map<Integer, E> map;
    
    @LauncherAPI
    public static void write(final HOutput output, final Itf itf) throws IOException {
        output.writeVarInt(itf.getNumber());
    }
    
    @LauncherAPI
    public EnumSerializer(final Class<E> clazz) {
        this.map = new HashMap<Integer, E>(16);
        for (final E e : clazz.getEnumConstants()) {
            VerifyHelper.putIfAbsent((Map<Integer, Enum>)this.map, ((Itf)e).getNumber(), (Enum)e, "Duplicate number for enum constant " + ((java.lang.Enum)e).name());
        }
    }
    
    @LauncherAPI
    public E read(final HInput input) throws IOException {
        final int n = input.readVarInt();
        return VerifyHelper.getMapValue(this.map, n, "Unknown enum number: " + n);
    }
    
    @FunctionalInterface
    public interface Itf
    {
        @LauncherAPI
        int getNumber();
    }
}
