package ru.gravit.utils.helper;

import java.util.Collection;
import ru.gravit.utils.command.CommandException;
import java.util.LinkedList;
import javax.script.ScriptEngine;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Locale;
import java.util.Iterator;
import javax.script.ScriptEngineFactory;
import ru.gravit.launcher.LauncherAPI;
import javax.script.ScriptEngineManager;

public final class CommonHelper
{
    @LauncherAPI
    public static final ScriptEngineManager scriptManager;
    @LauncherAPI
    public static final ScriptEngineFactory nashornFactory;
    
    private static ScriptEngineFactory getEngineFactories(final ScriptEngineManager manager) {
        for (final ScriptEngineFactory fact : manager.getEngineFactories()) {
            if (fact.getNames().contains("nashorn") || fact.getNames().contains("Nashorn")) {
                return fact;
            }
        }
        return null;
    }
    
    @LauncherAPI
    public static String low(final String s) {
        return s.toLowerCase(Locale.US);
    }
    
    @LauncherAPI
    public static boolean multiMatches(final Pattern[] pattern, final String from) {
        for (final Pattern p : pattern) {
            if (p.matcher(from).matches()) {
                return true;
            }
        }
        return false;
    }
    
    @LauncherAPI
    public static String multiReplace(final Pattern[] pattern, final String from, final String replace) {
        String tmp = null;
        for (final Pattern p : pattern) {
            final Matcher m = p.matcher(from);
            if (m.matches()) {
                tmp = m.replaceAll(replace);
            }
        }
        return (tmp != null) ? tmp : from;
    }
    
    @LauncherAPI
    public static ScriptEngine newScriptEngine() {
        return CommonHelper.nashornFactory.getScriptEngine();
    }
    
    @LauncherAPI
    public static Thread newThread(final String name, final boolean daemon, final Runnable runnable) {
        final Thread thread = new Thread(runnable);
        thread.setDaemon(daemon);
        if (name != null) {
            thread.setName(name);
        }
        return thread;
    }
    
    @LauncherAPI
    public static String replace(String source, final String... params) {
        for (int i = 0; i < params.length; i += 2) {
            source = source.replace('%' + params[i] + '%', params[i + 1]);
        }
        return source;
    }
    
    private CommonHelper() {
    }
    
    public static String[] parseCommand(final CharSequence line) throws CommandException {
        boolean quoted = false;
        boolean wasQuoted = false;
        final Collection<String> result = new LinkedList<String>();
        final StringBuilder builder = new StringBuilder(100);
        for (int i = 0; i <= line.length(); ++i) {
            final boolean end = i >= line.length();
            final char ch = end ? '\0' : line.charAt(i);
            if (end || (!quoted && Character.isWhitespace(ch))) {
                if (end && quoted) {
                    throw new CommandException("Quotes wasn't closed");
                }
                if (wasQuoted || builder.length() > 0) {
                    result.add(builder.toString());
                }
                wasQuoted = false;
                builder.setLength(0);
            }
            else {
                switch (ch) {
                    case '\"': {
                        quoted = !quoted;
                        wasQuoted = true;
                        break;
                    }
                    case '\\': {
                        if (i + 1 >= line.length()) {
                            throw new CommandException("Escape character is not specified");
                        }
                        final char next = line.charAt(i + 1);
                        builder.append(next);
                        ++i;
                        break;
                    }
                    default: {
                        builder.append(ch);
                        break;
                    }
                }
            }
        }
        return result.toArray(new String[0]);
    }
    
    static {
        scriptManager = new ScriptEngineManager();
        nashornFactory = getEngineFactories(CommonHelper.scriptManager);
    }
}
