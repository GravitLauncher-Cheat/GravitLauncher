package ru.gravit.launcher;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

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
        try {
            JSONParser parser = new JSONParser();
            JSONObject data = (JSONObject) parser.parse(new FileReader("./config.json"));
            this.projectname = data.get("projectname").toString();
            this.address = data.get("address").toString();
            this.port = ((Long) data.get("port")).intValue();
            this.clientPort = ((Long) data.get("clientPort")).intValue();
            this.secretKeyClient = data.get("secretKeyClient").toString();
            this.env = ((Long) data.get("env")).intValue();
            System.out.println("Конфиг прочитан!");
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            System.out.println("Создаю новый файл конфигурации...");
            FileWriter file = null;
            JSONObject obj = new JSONObject();
            obj.put("projectname", "Dreamfinity");
            obj.put("address", "mc.dreamfinity.org");
            obj.put("port", 7240);
            obj.put("clientPort", 32288);
            obj.put("secretKeyClient", "a4fa3a868aa31b1c");
            obj.put("env", 3);
            try {
                file = new FileWriter("./config.json");
                file.write(obj.toJSONString());
                System.out.println("Файл конфигурации создан!");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            } finally {
                try {
                    file.flush();
                    file.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }

        }
    }
    
    public void initModules() {
    }
}
