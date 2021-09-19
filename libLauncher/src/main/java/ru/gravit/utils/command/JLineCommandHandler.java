package ru.gravit.utils.command;

import java.io.IOException;
import ru.gravit.utils.helper.LogHelper;
import jline.console.ConsoleReader;

public class JLineCommandHandler extends CommandHandler
{
    private final ConsoleReader reader;
    
    public JLineCommandHandler() throws IOException {
        (this.reader = new ConsoleReader()).setExpandEvents(false);
        LogHelper.removeStdOutput();
        LogHelper.addOutput(new JLineOutput(), LogHelper.OutputTypes.JANSI);
    }
    
    @Override
    public void bell() throws IOException {
        this.reader.beep();
    }
    
    @Override
    public void clear() throws IOException {
        this.reader.clearScreen();
    }
    
    @Override
    public String readLine() throws IOException {
        return this.reader.readLine();
    }
    
    private final class JLineOutput implements LogHelper.Output
    {
        @Override
        public void println(final String message) {
            try {
                JLineCommandHandler.this.reader.println((CharSequence)('\r' + message));
                JLineCommandHandler.this.reader.drawLine();
                JLineCommandHandler.this.reader.flush();
            }
            catch (IOException ex) {}
        }
    }
}
