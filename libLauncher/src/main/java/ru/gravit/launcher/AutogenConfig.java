package ru.gravit.launcher;

public class AutogenConfig {
    public String projectname;
    public String address;
    public String nettyAddress;
    public int port;
    public int nettyPort;
    public int clientPort;
    @SuppressWarnings("unused")
    private boolean isInitModules;
    public boolean isUsingWrapper;
    public boolean isDownloadJava; //Выставление этого флага требует модификации runtime части
    public boolean isNettyEnabled;
    public String secretKeyClient;
    public String guardLicenseName;
    public String guardLicenseKey;
    public String guardLicenseEncryptKey;
    public int env;
    public boolean isWarningMissArchJava;
    // 0 - Dev (дебаг включен по умолчанию, все сообщения)
    // 1 - Debug (дебаг включен по умолчанию, основные сообщения)
    // 2 - Std (дебаг выключен по умолчанию, основные сообщения)
    // 3 - Production (дебаг выключен, минимальный объем сообщений, stacktrace не выводится)

    AutogenConfig() {
        this.address = "mc.dreamfinity.org";
        this.port = 7240;
        this.clientPort = 32288;
        this.secretKeyClient = "04e51b63006856bb";
    }

    public void initModules() {
    }
}
