package com.iwdael.wifimanager;

public interface IWifi {

    String name();

    boolean isEncrypt();

    boolean isSaved();

    boolean isConnected();

    String encryption();

    int level();

    String description();

    String ip();

    String description2();

    void state(String state);

    @Deprecated
    String SSID();

    @Deprecated
    String capabilities();

    @Deprecated
    IWifi merge(IWifi merge);

    String state();

    /** 返回 WiFi 的 BSSID（MAC 地址），可选，不支持时返回空字符串 */
    default String bssid() {
        return "";
    }
}
