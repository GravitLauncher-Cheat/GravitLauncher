package ru.gravit.launcher;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import ru.gravit.utils.helper.IOHelper;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class AutogenConfig {
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
        try {
            final String pathLauncher = IOHelper.getCodeSource(Launcher.class).toString().replace(new File(AutogenConfig.class.getProtectionDomain().getCodeSource().getLocation().toString()).getName(), "");
            JSONParser parser = new JSONParser();
            JSONObject data = (JSONObject) parser.parse(new FileReader(pathLauncher+"config.json"));
            this.address = data.get("address").toString();
            this.port = ((Long) data.get("port")).intValue();
            this.env = ((Long) data.get("env")).intValue();
            System.out.println("Конфиг прочитан!");
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        this.clientPort = 32288;
        this.secretKeyClient = "sosy1tvoi2hui345";
    }
    
    public void initModules() {
    }
}
