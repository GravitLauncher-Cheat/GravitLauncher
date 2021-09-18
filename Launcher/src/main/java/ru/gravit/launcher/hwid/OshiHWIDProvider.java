package ru.gravit.launcher.hwid;

import ru.gravit.utils.helper.IOHelper;
import ru.gravit.launcher.Launcher;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.FileReader;
import com.google.gson.Gson;
import java.io.IOException;
import java.nio.file.OpenOption;
import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import ru.gravit.launcher.OshiHWID;
import ru.gravit.launcher.HWID;
import oshi.hardware.CentralProcessor;
import java.net.NetworkInterface;
import oshi.hardware.UsbDevice;
import oshi.hardware.ComputerSystem;
import oshi.hardware.NetworkIF;
import oshi.hardware.SoundCard;
import oshi.hardware.HWDiskStore;
import ru.gravit.utils.helper.LogHelper;
import java.nio.file.Path;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.SystemInfo;
import ru.gravit.launcher.LauncherHWIDInterface;

public class OshiHWIDProvider implements LauncherHWIDInterface
{
    public SystemInfo systemInfo;
    public HardwareAbstractionLayer hardware;
    public boolean noHWID;
    public static final Path HWID_PATH;
    
    public OshiHWIDProvider() {
        try {
            this.systemInfo = new SystemInfo();
            this.noHWID = false;
        }
        catch (Throwable var2) {
            LogHelper.error(var2);
            this.noHWID = true;
        }
    }
    
    public String getSerial() {
        try {
            if (this.hardware == null) {
                this.systemInfo.getHardware();
            }
            return this.hardware.getComputerSystem().getSerialNumber();
        }
        catch (Exception var2) {
            LogHelper.error(var2);
            return "";
        }
    }
    
    public String getProcessorID() {
        try {
            if (this.hardware == null) {
                 this.systemInfo.getHardware();
            }
            return this.hardware.getProcessor().getProcessorID();
        }
        catch (Exception var2) {
            LogHelper.error(var2);
            return "";
        }
    }
    
    public String getHWDisk() {
        try {
            if (this.hardware == null) {
                 this.systemInfo.getHardware();
            }
            HWDiskStore store = null;
            long size = 0L;
            for (final HWDiskStore s : this.hardware.getDiskStores()) {
                if (size < s.getSize()) {
                    store = s;
                    size = s.getSize();
                }
            }
            return (store == null) ? "" : store.getSerial();
        }
        catch (Exception var7) {
            LogHelper.error(var7);
            return "";
        }
    }
    
    public String getSoundCardInfo() {
        final SoundCard[] var1 = this.hardware.getSoundCards();
        final int var2 = var1.length;
        final byte var3 = 0;
        if (var3 < var2) {
            final SoundCard soundcard = var1[var3];
            return soundcard.getName();
        }
        return "";
    }
    
    public String getMacAddr() {
        for (final NetworkIF networkIF : this.hardware.getNetworkIFs()) {
            for (final String ipv4 : networkIF.getIPv4addr()) {
                if (!ipv4.startsWith("127.") && !ipv4.startsWith("10.")) {
                    return networkIF.getMacaddr();
                }
            }
        }
        return "";
    }
    
    public long getTotalMemory() {
        if (this.noHWID) {
            return -1L;
        }
        if (this.hardware == null) {
            this.hardware = this.systemInfo.getHardware();
        }
        return this.hardware.getMemory().getTotal();
    }
    
    public long getAvailableMemory() {
        if (this.noHWID) {
            return -1L;
        }
        if (this.hardware == null) {
            this.hardware = this.systemInfo.getHardware();
        }
        return this.hardware.getMemory().getAvailable();
    }
    
    public void printHardwareInformation() {
        try {
            if (this.hardware == null) {
                this.hardware = this.systemInfo.getHardware();
            }
            final ComputerSystem computerSystem = this.hardware.getComputerSystem();
            LogHelper.debug("ComputerSystem Model: %s Serial: %s", computerSystem.getModel(), computerSystem.getSerialNumber());
            for (final HWDiskStore s : this.hardware.getDiskStores()) {
                LogHelper.debug("HWDiskStore Serial: %s Model: %s Size: %d", s.getSerial(), s.getModel(), s.getSize());
            }
            for (final UsbDevice s2 : this.hardware.getUsbDevices(true)) {
                LogHelper.debug("USBDevice Serial: %s Name: %s", s2.getSerialNumber(), s2.getName());
            }
            for (final NetworkIF networkIF : this.hardware.getNetworkIFs()) {
                LogHelper.debug("Network Interface: %s mac: %s", networkIF.getName(), networkIF.getMacaddr());
                final NetworkInterface network = networkIF.getNetworkInterface();
                if (!network.isLoopback() && !network.isVirtual()) {
                    LogHelper.debug("Network Interface display: %s name: %s", network.getDisplayName(), network.getName());
                    for (final String ipv4 : networkIF.getIPv4addr()) {
                        if (!ipv4.startsWith("127.") && !ipv4.startsWith("10.")) {
                            LogHelper.subDebug("IPv4: %s", ipv4);
                        }
                    }
                }
            }
            for (final SoundCard soundcard : this.hardware.getSoundCards()) {
                LogHelper.debug("SoundCard %s", soundcard.getName());
            }
            final CentralProcessor processor = this.hardware.getProcessor();
            LogHelper.debug("Processor Model: %s ID: %s", processor.getModel(), processor.getProcessorID());
        }
        catch (Throwable var11) {
            LogHelper.error(var11);
        }
    }
    
    @Override
    public HWID getHWID() {
        OshiHWID hwid = new OshiHWID();
        hwid.serialNumber = this.getSerial();
        hwid.totalMemory = this.getTotalMemory();
        hwid.HWDiskSerial = this.getHWDisk();
        hwid.processorID = this.getProcessorID();
        hwid.macAddr = this.getMacAddr();
        if (!Files.exists(OshiHWIDProvider.HWID_PATH, new LinkOption[0])) {
            final Gson gson = new GsonBuilder().create();
            final String data = gson.toJson(hwid);
            try {
                Files.write(OshiHWIDProvider.HWID_PATH, data.getBytes(), new OpenOption[0]);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            final Gson gson = new Gson();
            final String path = OshiHWIDProvider.HWID_PATH.toString();
            try {
                final BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
                hwid = gson.fromJson(bufferedReader, OshiHWID.class);
            }
            catch (FileNotFoundException e2) {
                e2.printStackTrace();
            }
        }
        this.printHardwareInformation();
        return hwid;
    }
    
    static {
        HWID_PATH = IOHelper.getCodeSource(Launcher.class).getParent().resolve("Launcher_HWID.txt");
    }
}
