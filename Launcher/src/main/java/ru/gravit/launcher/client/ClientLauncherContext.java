package ru.gravit.launcher.client;

import java.util.LinkedList;
import ru.gravit.launcher.profiles.PlayerProfile;
import ru.gravit.launcher.profiles.ClientProfile;
import java.util.List;
import java.nio.file.Path;

public class ClientLauncherContext
{
    public Path javaBin;
    public List<String> args;
    public String pathLauncher;
    public ProcessBuilder builder;
    public ClientProfile clientProfile;
    public PlayerProfile playerProfile;
    
    public ClientLauncherContext() {
        this.args = new LinkedList<String>();
    }
}
