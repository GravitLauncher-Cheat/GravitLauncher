package ru.gravit.utils.command;

import java.util.UUID;
import ru.gravit.utils.helper.VerifyHelper;

public abstract class Command
{
    protected static String parseUsername(final String username) throws CommandException {
        try {
            return VerifyHelper.verifyUsername(username);
        }
        catch (IllegalArgumentException e) {
            throw new CommandException(e.getMessage());
        }
    }
    
    protected static UUID parseUUID(final String s) throws CommandException {
        try {
            return UUID.fromString(s);
        }
        catch (IllegalArgumentException ignored) {
            throw new CommandException(String.format("Invalid UUID: '%s'", s));
        }
    }
    
    public abstract String getArgsDescription();
    
    public abstract String getUsageDescription();
    
    public abstract void invoke(final String... p0) throws Exception;
    
    protected final void verifyArgs(final String[] args, final int min) throws CommandException {
        if (args.length < min) {
            throw new CommandException("Command usage: " + this.getArgsDescription());
        }
    }
}
