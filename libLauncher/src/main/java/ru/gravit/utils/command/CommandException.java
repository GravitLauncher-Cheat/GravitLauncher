package ru.gravit.utils.command;

public final class CommandException extends Exception
{
    private static final long serialVersionUID = -6588814993972117772L;
    
    public CommandException(final String message) {
        super(message);
    }
    
    public CommandException(final Throwable exc) {
        super(exc);
    }
    
    @Override
    public String toString() {
        return this.getMessage();
    }
}
