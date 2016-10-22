package com.inlook.android_clean_bluetooth.data.net.metadata;

/**
 * ReqHeader
 *
 * @author or
 * @since 2016/10/21.
 */

public class ReqHeader {

    private String business;
    private int squence;
    private String version;
    private String clientVersion;
    private int platform;
    private long timestamp;
    private String nonce;
    private String signature;


    public int getPlatform() {
        return platform;
    }

    public void setPlatform(int platform) {
        this.platform = platform;
    }

    public String getBusiness() {
        return business;
    }

    public void setBusiness(String business) {
        this.business = business;
    }

    public int getSquence() {
        return squence;
    }

    public void setSquence(int squence) {
        this.squence = squence;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

}
