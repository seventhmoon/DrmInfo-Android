package com.androidfung.drminfo.safetynet;

/**
 * Created by fung on 12/3/2016.
 */

//{"nonce":"jzkuUJBHfbqD9cYmx7Wfiw==",
// "timestampMs":1480743757338,
// "apkPackageName":"com.androidfung.drminfo",
// "apkDigestSha256":"aGwoVA/T3lNVZCFeANkLstBWO5Isj4YOn8DeemQF1TQ=",
// "ctsProfileMatch":true,
// "extension":"CQd2u3JeloRK",
// "apkCertificateDigestSha256":["120qcDtm0B2cqN5Ttz9wRme4fsmmq8zS1fzpyt4i8Zg="],
// "basicIntegrity":true}
public class CompatibilityCheckResponse {
    private String nonce;
    private long timestampMs;
    private String apkPackageName;
    private String apkDigestSha256;
    private boolean ctsProfileMatch;
    private String extension;
    private boolean basicIntegrity;

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public long getTimestampMs() {
        return timestampMs;
    }

    public void setTimestampMs(long timestampMs) {
        this.timestampMs = timestampMs;
    }

    public String getApkPackageName() {
        return apkPackageName;
    }

    public void setApkPackageName(String apkPackageName) {
        this.apkPackageName = apkPackageName;
    }

    public String getApkDigestSha256() {
        return apkDigestSha256;
    }

    public void setApkDigestSha256(String apkDigestSha256) {
        this.apkDigestSha256 = apkDigestSha256;
    }

    public boolean isCtsProfileMatch() {
        return ctsProfileMatch;
    }

    public void setCtsProfileMatch(boolean ctsProfileMatch) {
        this.ctsProfileMatch = ctsProfileMatch;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public boolean isBasicIntegrity() {
        return basicIntegrity;
    }

    public void setBasicIntegrity(boolean basicIntegrity) {
        this.basicIntegrity = basicIntegrity;
    }
}
