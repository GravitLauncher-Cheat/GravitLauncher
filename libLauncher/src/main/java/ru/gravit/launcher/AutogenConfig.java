package ru.gravit.launcher;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import ru.gravit.utils.helper.IOHelper;
import ru.gravit.utils.helper.LogHelper;

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
    public int env;
    public boolean liteloader;
    public boolean isWarningMissArchJava;
    public String configMode;
    public String runtimeMode;
    
    public AutogenConfig() {
        try {
            JSONParser parser = new JSONParser();
            JSONObject data = (JSONObject) parser.parse(new FileReader(IOHelper.getCodeSource(Launcher.class).getParent().resolve("config.json").toString()));
            this.address = data.get("address").toString();
            this.port = ((Long) data.get("port")).intValue();
            this.env = ((Long) data.get("env")).intValue();
            this.liteloader = Boolean.parseBoolean(data.get("liteloader").toString());
            this.configMode = (String) data.get("configMode");
            this.runtimeMode = (String) data.get("runtimeMode");
            LogHelper.debug("Конфиг прочитан!");
        } catch (IOException | ParseException e) {
            if (LogHelper.isStacktraceEnabled()) {
                LogHelper.error("При чтении конфига произошла ошибка: "+e);
            } else {
                LogHelper.error("При чтении конфига произошла ошибка:");
                e.printStackTrace();
            }
        }
        this.clientPort = 32288;
        this.secretKeyClient = "13371pizdahui228";
    }
    
    public void initModules() {
    }
}
