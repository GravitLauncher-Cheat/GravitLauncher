package ru.gravit.utils.helper;

import java.util.Map;
import java.util.Locale;

public final class EnvHelper
{
    public static final String[] toTest;
    
    public static void addEnv(final ProcessBuilder builder) {
        final Map<String, String> map = builder.environment();
        for (final String env : EnvHelper.toTest) {
            if (map.containsKey(env)) {
                map.put(env, "");
            }
            final String lower_env = env.toLowerCase(Locale.US);
            if (map.containsKey(lower_env)) {
                map.put(lower_env, "");
            }
        }
    }
    
    public static void checkDangerousParams() {
        for (final String t : EnvHelper.toTest) {
            String env = System.getenv(t);
            if (env != null) {
                env = env.toLowerCase(Locale.US);
                if (env.contains("-cp") || env.contains("-classpath") || env.contains("-javaagent") || env.contains("-agentpath") || env.contains("-agentlib")) {
                    throw new SecurityException("JavaAgent in global options not allow");
                }
            }
        }
    }
    
    static {
        toTest = new String[] { "_JAVA_OPTIONS", "_JAVA_OPTS", "JAVA_OPTS", "JAVA_OPTIONS" };
    }
}
