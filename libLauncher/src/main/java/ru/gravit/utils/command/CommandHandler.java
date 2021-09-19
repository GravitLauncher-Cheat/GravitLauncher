package ru.gravit.utils.command;

import java.util.Collections;
import java.util.Objects;
import ru.gravit.utils.helper.VerifyHelper;
import java.io.IOException;
import java.util.Arrays;
import ru.gravit.utils.helper.CommonHelper;
import ru.gravit.utils.helper.LogHelper;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public abstract class CommandHandler implements Runnable
{
    private final Map<String, Command> commands;
    
    public CommandHandler() {
        this.commands = new ConcurrentHashMap<String, Command>(32);
    }
    
    public void eval(final String line, final boolean bell) {
        LogHelper.info("Command '%s'", line);
        String[] args;
        try {
            args = CommonHelper.parseCommand(line);
        }
        catch (Exception e) {
            LogHelper.error(e);
            return;
        }
        this.eval(args, bell);
    }
    
    public void eval(final String[] args, final boolean bell) {
        if (args.length == 0) {
            return;
        }
        final long startTime = System.currentTimeMillis();
        try {
            this.lookup(args[0]).invoke((String[])Arrays.copyOfRange(args, 1, args.length));
        }
        catch (Exception e) {
            LogHelper.error(e);
        }
        final long endTime = System.currentTimeMillis();
        if (bell && endTime - startTime >= 5000L) {
            try {
                this.bell();
            }
            catch (IOException e2) {
                LogHelper.error(e2);
            }
        }
    }
    
    public Command lookup(final String name) throws CommandException {
        final Command command = this.commands.get(name);
        if (command == null) {
            throw new CommandException(String.format("Unknown command: '%s'", name));
        }
        return command;
    }
    
    public abstract String readLine() throws IOException;
    
    private void readLoop() throws IOException {
        for (String line = this.readLine(); line != null; line = this.readLine()) {
            this.eval(line, true);
        }
    }
    
    public void registerCommand(final String name, final Command command) {
        VerifyHelper.verifyIDName(name);
        VerifyHelper.putIfAbsent(this.commands, name, Objects.requireNonNull(command, "command"), String.format("Command has been already registered: '%s'", name));
    }
    
    @Override
    public void run() {
        try {
            this.readLoop();
        }
        catch (IOException e) {
            LogHelper.error(e);
        }
    }
    
    public abstract void bell() throws IOException;
    
    public abstract void clear() throws IOException;
    
    public Map<String, Command> commandsMap() {
        return Collections.unmodifiableMap((Map<? extends String, ? extends Command>)this.commands);
    }
}
