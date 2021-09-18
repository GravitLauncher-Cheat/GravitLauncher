package ru.gravit.launcher;

public class AutogenConfig
{
    public String projectname;
    public String address;
    public String nettyAddress;
    public int port;
    public int nettyPort;
    public int clientPort;
    private boolean isInitModules;
    public boolean isUsingWrapper;
    public boolean isDownloadJava;
    public boolean isNettyEnabled;
    public String secretKeyClient;
    public String guardLicenseName;
    public String guardLicenseKey;
    public String guardLicenseEncryptKey;
    public int env;
    public boolean isWarningMissArchJava;
    
    public AutogenConfig() {
        this.address = "mc.dreamfinity.org";
        this.port = 9274;
        this.clientPort = 32288;
        this.secretKeyClient = "a4fa3a868aa31b1c";
    }
    
    public void initModules() {
    }
}
