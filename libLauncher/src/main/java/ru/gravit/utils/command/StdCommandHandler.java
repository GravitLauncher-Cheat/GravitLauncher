package ru.gravit.utils.command;

import java.io.IOException;
import ru.gravit.utils.helper.IOHelper;
import java.io.BufferedReader;

public class StdCommandHandler extends CommandHandler
{
    private final BufferedReader reader;
    
    public StdCommandHandler(final boolean readCommands) {
        this.reader = (readCommands ? IOHelper.newReader(System.in) : null);
    }
    
    @Override
    public void bell() {
    }
    
    @Override
    public void clear() {
        throw new UnsupportedOperationException("clear terminal");
    }
    
    @Override
    public String readLine() throws IOException {
        return (this.reader == null) ? null : this.reader.readLine();
    }
}
