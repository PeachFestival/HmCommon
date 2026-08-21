package com.iwdael.wifimanager;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class WifiManager extends BaseWifiManager {

    private static final String TAG = "WifiManager";

    /** 连接超时任务（15s） */
    private Runnable connectTimeoutRunnable;

    private WifiManager(Context context) {
        super(context);
    }

    public static IWifiManager create(Context context) {
        return new WifiManager(context);
    }

    @Override
    public boolean isOpened() {
        if (manager == null) return false;
        return manager.isWifiEnabled();
    }

    @Override
    public void openWifi() {
        if (manager != null && !manager.isWifiEnabled())
            manager.setWifiEnabled(true);
    }

    @Override
    public void closeWifi() {
        if (manager != null && manager.isWifiEnabled())
            manager.setWifiEnabled(false);
    }

    @Override
    public void scanWifi() {
        if (manager != null)
            manager.startScan();
    }

    @Override
    public boolean disConnectWifi() {
        if (manager == null) return false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && connectivityManager != null) {
            try { connectivityManager.bindProcessToNetwork(null); } catch (Exception ignore) {}
        }
        return manager.disconnect();
    }

    @Override
    public boolean connectEncryptWifi(IWifi wifi, String password) {
        if (manager == null || wifi == null) return false;
        // 已连接同一网络则直接返回成功
        if (manager.getConnectionInfo() != null
                && wifi.SSID().equals(manager.getConnectionInfo().getSSID())) {
            return true;
        }

        modifyWifi(wifi.SSID(), "开始连接...");

        // 1. 优先尝试系统级 WifiConfiguration 直连
        // （系统应用 android.uid.system 拥有直接配置网络的最高权限，秒连且无弹窗）
        boolean legacySuccess = connectLegacy(wifi, password);
        if (legacySuccess) {
            startConnectTimeout(wifi.SSID());
            return true;
        }

        // 2. 若系统直连失败（普通非系统权限），在 Android 10+ 上 fallback 到 Suggestion
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return connectWithSuggestion(wifi, password);
        }

        modifyWifi(wifi.SSID(), "连接配置失败");
        return false;
    }

    /**
     * 系统级 WifiConfiguration 直连（适用于系统签名应用及 Android 9 及以下）
     */
    private boolean connectLegacy(IWifi wifi, String password) {
        try {
            int networkId = WifiHelper.configOrCreateWifi(manager, wifi, password);
            Log.d(TAG, "connectLegacy networkId=" + networkId + " ssid=" + wifi.name());
            if (networkId == -1) {
                return false;
            }
            try { manager.disconnect(); } catch (Exception ignore) {}
            boolean enabled = manager.enableNetwork(networkId, true);
            if (enabled) {
                manager.reconnect();
            }
            Log.d(TAG, "connectLegacy enableNetwork=" + enabled);
            return enabled;
        } catch (Exception e) {
            Log.e(TAG, "connectLegacy exception: " + e.getMessage());
            return false;
        }
    }

    /**
     * Android 10+ 普通应用静默连接方案：WifiNetworkSuggestion
     */
    @android.annotation.TargetApi(Build.VERSION_CODES.Q)
    private boolean connectWithSuggestion(IWifi wifi, String password) {
        try {
            // 1. 先移除同 SSID 的旧 Suggestion
            try {
                android.net.wifi.WifiNetworkSuggestion old =
                        new android.net.wifi.WifiNetworkSuggestion.Builder()
                                .setSsid(wifi.name()).build();
                List<android.net.wifi.WifiNetworkSuggestion> rmList = new ArrayList<>();
                rmList.add(old);
                manager.removeNetworkSuggestions(rmList);
            } catch (Exception ignore) {}

            // 2. 构建新 Suggestion
            android.net.wifi.WifiNetworkSuggestion.Builder builder =
                    new android.net.wifi.WifiNetworkSuggestion.Builder()
                            .setSsid(wifi.name())
                            .setIsAppInteractionRequired(false);

            boolean hasPassword = password != null && !password.isEmpty();
            if (hasPassword) {
                String caps = wifi.capabilities();
                if (caps != null && caps.toUpperCase().contains("WPA3")) {
                    builder.setWpa3Passphrase(password);
                } else {
                    builder.setWpa2Passphrase(password);
                }
            }

            List<android.net.wifi.WifiNetworkSuggestion> suggestions = new ArrayList<>();
            suggestions.add(builder.build());

            // 3. 提交 Suggestion
            int status = manager.addNetworkSuggestions(suggestions);
            Log.d(TAG, "addNetworkSuggestions status=" + status + " ssid=" + wifi.name());

            if (status != android.net.wifi.WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS
                    && status != android.net.wifi.WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_ADD_DUPLICATE) {
                Log.e(TAG, "addNetworkSuggestions failed, status=" + status);
                modifyWifi(wifi.SSID(), "连接失败");
                if (handler != null) handler.sendEmptyMessage(WIFI_STATE_UNCONNECTED);
                return false;
            }

            // 4. 注册 NetworkCallback 监听连接
            registerSuggestionCallback(wifi.name(), wifi.SSID());

            // 5. 触发扫描加速系统连接
            manager.startScan();

            // 6. 延迟断开让系统切换到 Suggestion
            handler.postDelayed(() -> {
                if (isDestroyed || manager == null) return;
                try {
                    android.net.wifi.WifiInfo curInfo = manager.getConnectionInfo();
                    if (curInfo != null && !wifi.SSID().equals(curInfo.getSSID())) {
                        manager.disconnect();
                        Log.d(TAG, "disconnect triggered to force suggestion evaluation");
                    }
                } catch (Exception e) {
                    Log.w(TAG, "delayed disconnect: " + e.getMessage());
                }
            }, 1500L);

            // 7. 启动 15s 超时
            startConnectTimeout(wifi.SSID());
            return true;

        } catch (Exception e) {
            Log.e(TAG, "connectWithSuggestion exception: " + e.getMessage());
            modifyWifi(wifi.SSID(), "连接失败");
            if (handler != null) handler.sendEmptyMessage(WIFI_STATE_UNCONNECTED);
            return false;
        }
    }

    /**
     * 注册 NetworkCallback 检测是否成功连接目标网络
     */
    @android.annotation.TargetApi(Build.VERSION_CODES.Q)
    private void registerSuggestionCallback(String targetName, String targetSsid) {
        if (currentNetworkCallback != null && connectivityManager != null) {
            try { connectivityManager.unregisterNetworkCallback(currentNetworkCallback); } catch (Exception ignore) {}
            currentNetworkCallback = null;
        }
        if (connectivityManager == null) return;

        NetworkRequest request = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build();

        currentNetworkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onCapabilitiesChanged(Network network, NetworkCapabilities caps) {
                if (isDestroyed || caps == null) return;
                try {
                    Object info = caps.getTransportInfo();
                    if (info instanceof android.net.wifi.WifiInfo) {
                        String ssid = ((android.net.wifi.WifiInfo) info).getSSID();
                        boolean matched = targetSsid.equals(ssid)
                                || ("\"" + targetName + "\"").equals(ssid)
                                || targetName.equals(ssid);
                        if (matched) {
                            Log.d(TAG, "NetworkCallback: connected to target: " + ssid);
                            cancelConnectTimeout();
                            modifyWifi(targetSsid, "已连接");
                            if (handler != null) handler.sendEmptyMessage(WIFI_STATE_CONNECTED);
                            releaseCallback();
                        }
                    }
                } catch (Exception e) {
                    Log.w(TAG, "onCapabilitiesChanged: " + e.getMessage());
                }
            }

            private void releaseCallback() {
                if (connectivityManager != null) {
                    try { connectivityManager.unregisterNetworkCallback(this); } catch (Exception ignore) {}
                }
                if (currentNetworkCallback == this) currentNetworkCallback = null;
            }
        };

        try {
            connectivityManager.registerNetworkCallback(request, currentNetworkCallback);
            Log.d(TAG, "registerSuggestionCallback for: " + targetName);
        } catch (Exception e) {
            Log.w(TAG, "registerNetworkCallback failed: " + e.getMessage());
            currentNetworkCallback = null;
        }
    }

    /** 启动 15s 超时 */
    private void startConnectTimeout(String ssid) {
        cancelConnectTimeout();
        connectTimeoutRunnable = () -> {
            Log.w(TAG, "connect timeout for: " + ssid);
            modifyWifi(ssid, "连接超时");
            if (handler != null) handler.sendEmptyMessage(WIFI_STATE_CONNECT_TIMEOUT);
        };
        handler.postDelayed(connectTimeoutRunnable, 15_000L);
    }

    /** 取消超时任务 */
    void cancelConnectTimeout() {
        if (connectTimeoutRunnable != null) {
            handler.removeCallbacks(connectTimeoutRunnable);
            connectTimeoutRunnable = null;
        }
    }

    @Override
    protected void onWifiConnectedEvent() {
        cancelConnectTimeout();
    }

    @Override
    public boolean connectSavedWifi(IWifi wifi) {
        if (manager == null || wifi == null) return false;
        modifyWifi(wifi.SSID(), "开始连接...");

        // 1. 优先尝试系统级直连
        boolean legacySuccess = connectLegacy(wifi, null);
        if (legacySuccess) {
            startConnectTimeout(wifi.SSID());
            return true;
        }

        // 2. fallback 到 Suggestion
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return connectWithSuggestion(wifi, null);
        }

        return false;
    }

    @Override
    public boolean connectOpenWifi(IWifi wifi) {
        if (manager == null || wifi == null) return false;
        return connectEncryptWifi(wifi, null);
    }

    @Override
    public boolean removeWifi(IWifi wifi) {
        if (manager == null || wifi == null) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                android.net.wifi.WifiNetworkSuggestion suggestion =
                        new android.net.wifi.WifiNetworkSuggestion.Builder()
                                .setSsid(wifi.name()).build();
                List<android.net.wifi.WifiNetworkSuggestion> list = new ArrayList<>();
                list.add(suggestion);
                manager.removeNetworkSuggestions(list);
            } catch (Exception e) {
                Log.w(TAG, "removeNetworkSuggestions: " + e.getMessage());
            }
        }

        try {
            WifiHelper.deleteWifiConfiguration(manager, wifi);
        } catch (Exception e) {
            Log.w(TAG, "deleteWifiConfiguration: " + e.getMessage());
        }

        modifyWifi();
        return true;
    }

    @Override
    public List<IWifi> getWifi() {
        return wifis;
    }

    @Override
    public void destroy() {
        cancelConnectTimeout();
        super.destroy();
    }
}
