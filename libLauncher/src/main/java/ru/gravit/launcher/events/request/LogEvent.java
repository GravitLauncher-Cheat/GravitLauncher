package ru.gravit.launcher.events.request;

import ru.gravit.launcher.request.ResultInterface;

public class LogEvent implements ResultInterface
{
    public String string;
    
    @Override
    public String getType() {
        return "log";
    }
    
    public LogEvent(final String string) {
        this.string = string;
    }
}
