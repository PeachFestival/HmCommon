package com.iwdael.wifimanager;

public interface OnWifiConnectListener {
    void onConnectChanged(boolean status);

    /** 连接超时回调（30s 内未收到 onAvailable 或 onUnavailable），默认空实现 */
    default void onConnectTimeout() {}
}
