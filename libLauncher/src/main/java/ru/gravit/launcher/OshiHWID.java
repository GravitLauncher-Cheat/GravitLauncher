package ru.gravit.launcher;

import com.google.gson.Gson;

public class OshiHWID implements HWID
{
    public static Gson gson;
    @LauncherAPI
    public long totalMemory;
    @LauncherAPI
    public String serialNumber;
    @LauncherAPI
    public String HWDiskSerial;
    @LauncherAPI
    public String processorID;
    @LauncherAPI
    public String macAddr;
    
    public OshiHWID() {
        this.totalMemory = 0L;
    }
    
    @Override
    public String getSerializeString() {
        return OshiHWID.gson.toJson(this);
    }
    
    @Override
    public int getLevel() {
        int result = 0;
        if (this.totalMemory != 0L) {
            result += 8;
        }
        if (this.serialNumber != null && !this.serialNumber.equals("unknown")) {
            result += 12;
        }
        if (this.HWDiskSerial != null && !this.HWDiskSerial.equals("unknown")) {
            result += 30;
        }
        if (this.processorID != null && !this.processorID.equals("unknown")) {
            result += 10;
        }
        if (this.macAddr != null && !this.macAddr.equals("00:00:00:00:00:00")) {
            result += 15;
        }
        return result;
    }
    
    @Override
    public int compare(final HWID hwid) {
        if (hwid instanceof OshiHWID) {
            int rate = 0;
            final OshiHWID oshi = (OshiHWID)hwid;
            if (Math.abs(oshi.totalMemory - this.totalMemory) < 1048576L) {
                rate += 5;
            }
            if (oshi.totalMemory == this.totalMemory) {
                rate += 15;
            }
            if (oshi.HWDiskSerial.equals(this.HWDiskSerial)) {
                rate += 45;
            }
            if (oshi.processorID.equals(this.processorID)) {
                rate += 18;
            }
            if (oshi.serialNumber.equals(this.serialNumber)) {
                rate += 15;
            }
            if (!oshi.macAddr.isEmpty() && oshi.macAddr.equals(this.macAddr)) {
                rate += 19;
            }
            return rate;
        }
        return 0;
    }
    
    @Override
    public boolean isNull() {
        return this.getLevel() < 15;
    }
    
    static {
        OshiHWID.gson = new Gson();
    }
}
