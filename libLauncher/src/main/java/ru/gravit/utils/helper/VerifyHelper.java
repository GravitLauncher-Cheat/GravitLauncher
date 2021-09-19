package ru.gravit.utils.helper;

import java.util.function.DoublePredicate;
import java.util.Objects;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.function.Predicate;
import java.util.function.LongPredicate;
import ru.gravit.launcher.LauncherAPI;
import java.util.function.IntPredicate;

public final class VerifyHelper
{
    @LauncherAPI
    public static final IntPredicate POSITIVE;
    @LauncherAPI
    public static final IntPredicate NOT_NEGATIVE;
    @LauncherAPI
    public static final LongPredicate L_POSITIVE;
    @LauncherAPI
    public static final LongPredicate L_NOT_NEGATIVE;
    @LauncherAPI
    public static final Predicate<String> NOT_EMPTY;
    @LauncherAPI
    public static final Pattern USERNAME_PATTERN;
    private static final Pattern SERVERID_PATTERN;
    
    @LauncherAPI
    public static <K, V> V getMapValue(final Map<K, V> map, final K key, final String error) {
        return verify(map.get(key), Objects::nonNull, error);
    }
    
    @LauncherAPI
    public static boolean isValidIDName(final String name) {
        return !name.isEmpty() && name.length() <= 255 && name.chars().allMatch(VerifyHelper::isValidIDNameChar);
    }
    
    @LauncherAPI
    public static boolean isValidIDNameChar(final int ch) {
        return (ch >= 97 && ch <= 122) || (ch >= 65 && ch <= 90) || (ch >= 48 && ch <= 57) || ch == 45 || ch == 95;
    }
    
    @LauncherAPI
    public static boolean isValidServerID(final CharSequence serverID) {
        return VerifyHelper.SERVERID_PATTERN.matcher(serverID).matches();
    }
    
    @LauncherAPI
    public static boolean isValidUsername(final CharSequence username) {
        return VerifyHelper.USERNAME_PATTERN.matcher(username).matches();
    }
    
    @LauncherAPI
    public static <K, V> void putIfAbsent(final Map<K, V> map, final K key, final V value, final String error) {
        verify(map.putIfAbsent(key, value), Objects::isNull, error);
    }
    
    @LauncherAPI
    public static IntPredicate range(final int min, final int max) {
        return i -> i >= min && i <= max;
    }
    
    @LauncherAPI
    public static <T> T verify(final T object, final Predicate<T> predicate, final String error) {
        if (predicate.test(object)) {
            return object;
        }
        throw new IllegalArgumentException(error);
    }
    
    @LauncherAPI
    public static double verifyDouble(final double d, final DoublePredicate predicate, final String error) {
        if (predicate.test(d)) {
            return d;
        }
        throw new IllegalArgumentException(error);
    }
    
    @LauncherAPI
    public static String verifyIDName(final String name) {
        return verify(name, VerifyHelper::isValidIDName, String.format("Invalid name: '%s'", name));
    }
    
    @LauncherAPI
    public static int verifyInt(final int i, final IntPredicate predicate, final String error) {
        if (predicate.test(i)) {
            return i;
        }
        throw new IllegalArgumentException(error);
    }
    
    @LauncherAPI
    public static long verifyLong(final long l, final LongPredicate predicate, final String error) {
        if (predicate.test(l)) {
            return l;
        }
        throw new IllegalArgumentException(error);
    }
    
    @LauncherAPI
    public static String verifyServerID(final String serverID) {
        return verify(serverID, VerifyHelper::isValidServerID, String.format("Invalid server ID: '%s'", serverID));
    }
    
    @LauncherAPI
    public static String verifyUsername(final String username) {
        return verify(username, VerifyHelper::isValidUsername, String.format("Invalid username: '%s'", username));
    }
    
    private VerifyHelper() {
    }
    
    static {
        POSITIVE = (i -> i > 0);
        NOT_NEGATIVE = (i -> i >= 0);
        L_POSITIVE = (l -> l > 0L);
        L_NOT_NEGATIVE = (l -> l >= 0L);
        NOT_EMPTY = (s -> !s.isEmpty());
        USERNAME_PATTERN = Pattern.compile(Boolean.parseBoolean(System.getProperty("username.russian", "true")) ? "[a-zA-Z\u0430-\u044f\u0410-\u042f0-9_.\\-]{1,16}" : "[a-zA-Z0-9-_\\\\.]{1,16}");
        SERVERID_PATTERN = Pattern.compile("-?[0-9a-f]{1,40}");
    }
}
