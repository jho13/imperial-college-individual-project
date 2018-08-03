package richard.oh.android.app;

/**
 * Created by Richard on 25/07/2017.
 */

public class App {

    private String mName;
    private String mPackageName;
    private long mFTimeUsed;
    private long mBTimeUsed;
    private double mFBatteryUsed;
    private double mBBatteryUsed;

    private double mFCPUUsage;
    private double mBCPUUsage;
    private long mFMemoryUsage;
    private long mBMemoryUsage;
    private int mFAppearances;
    private int mBAppearances;

    private int mRestartCycle;
    private long mFUpload;
    private long mBUpload;
    private long mFCurrentUpload;
    private long mBCurrentUpload;
    private long mFDownload;
    private long mBDownload;
    private long mBCurrentDownload;
    private long mFCurrentDownload;


    public String getName() {
        return mName;
    }
    public void setName(String name) {
        mName = name;
    }
    public String getPackageName() {
        return mPackageName;
    }
    public void setPackageName(String packageName) {
        mPackageName = packageName;
    }
    public long getFTimeUsed() {
        return mFTimeUsed;
    }
    public void setFTimeUsed(long FTimeUsed) {
        mFTimeUsed = FTimeUsed;
    }
    public long getBTimeUsed() {
        return mBTimeUsed;
    }
    public void setBTimeUsed(long BTimeUsed) {
        mBTimeUsed = BTimeUsed;
    }
    public double getFBatteryUsed() {
        return mFBatteryUsed;
    }
    public void setFBatteryUsed(double FBatteryUsed) {
        mFBatteryUsed = FBatteryUsed;
    }
    public double getBBatteryUsed() {
        return mBBatteryUsed;
    }
    public void setBBatteryUsed(double BBatteryUsed) {
        mBBatteryUsed = BBatteryUsed;
    }

    public double getFCPUUsage() {
        return mFCPUUsage;
    }
    public void setFCPUUsage(double FCPUUsage) {
        mFCPUUsage = FCPUUsage;
    }
    public double getBCPUUsage() {
        return mBCPUUsage;
    }
    public void setBCPUUsage(double BCPUUsage) {
        mBCPUUsage = BCPUUsage;
    }
    public long getFMemoryUsage() {
        return mFMemoryUsage;
    }
    public void setFMemoryUsage(long FMemoryUsage) {
        mFMemoryUsage = FMemoryUsage;
    }
    public long getBMemoryUsage() {
        return mBMemoryUsage;
    }
    public void setBMemoryUsage(long BMemoryUsage) {
        mBMemoryUsage = BMemoryUsage;
    }
    public int getFAppearances() {
        return mFAppearances;
    }
    public void setFAppearances(int appearances) {
        mFAppearances = appearances;
    }
    public int getBAppearances() {
        return mBAppearances;
    }
    public void setBAppearances(int BAppearances) {
        mBAppearances = BAppearances;
    }

    public int getRestartCycle() {
        return mRestartCycle;
    }
    public void setRestartCycle(int restartCycle) {
        mRestartCycle = restartCycle;
    }
    public long getFUpload() {
        return mFUpload;
    }
    public void setFUpload(long FUpload) {
        mFUpload = FUpload;
    }
    public long getBUpload() {
        return mBUpload;
    }
    public void setBUpload(long BUpload) {
        mBUpload = BUpload;
    }
    public long getFCurrentUpload() {
        return mFCurrentUpload;
    }
    public void setFCurrentUpload(long FCurrentUpload) {
        mFCurrentUpload = FCurrentUpload;
    }
    public long getBCurrentUpload() {
        return mBCurrentUpload;
    }
    public void setBCurrentUpload(long BCurrentUpload) {
        mBCurrentUpload = BCurrentUpload;
    }
    public long getFDownload() {
        return mFDownload;
    }
    public void setFDownload(long FDownload) {
        mFDownload = FDownload;
    }
    public long getBDownload() {
        return mBDownload;
    }
    public void setBDownload(long BDownload) {
        mBDownload = BDownload;
    }
    public long getFCurrentDownload() {
        return mFCurrentDownload;
    }
    public void setFCurrentDownload(long FCurrentDownload) {
        mFCurrentDownload = FCurrentDownload;
    }
    public long getBCurrentDownload() {
        return mBCurrentDownload;
    }
    public void setBCurrentDownload(long BCurrentDownload) {
        mBCurrentDownload = BCurrentDownload;
    }
}
