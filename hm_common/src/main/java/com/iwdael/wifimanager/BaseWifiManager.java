package com.iwdael.wifimanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiNetworkSpecifier;
import android.net.wifi.WifiNetworkSuggestion;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public abstract class BaseWifiManager implements IWifiManager {

    static final int WIFI_STATE_DISABLED = 1;
    static final int WIFI_STATE_DISABLING = 2;
    static final int WIFI_STATE_ENABLING = 3;
    static final int WIFI_STATE_ENABLED = 4;
    static final int WIFI_STATE_UNKNOWN = 5;
    static final int WIFI_STATE_MODIFY = 6;
    static final int WIFI_STATE_CONNECTED = 7;
    static final int WIFI_STATE_UNCONNECTED = 8;
    static final int WIFI_STATE_CONNECT_TIMEOUT = 9;

    WifiManager manager;
    ConnectivityManager connectivityManager;
    ConnectivityManager.NetworkCallback currentNetworkCallback;
    List<IWifi> wifis;
    OnWifiChangeListener onWifiChangeListener;
    OnWifiConnectListener onWifiConnectListener;
    OnWifiStateChangeListener onWifiStateChangeListener;
    WifiReceiver wifiReceiver;
    Context context;
    boolean isDestroyed = false;

    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (isDestroyed) return;
            switch (msg.what) {
                case WIFI_STATE_DISABLED:
                    if (onWifiStateChangeListener != null)
                        onWifiStateChangeListener.onStateChanged(State.DISABLED);
                    break;
                case WIFI_STATE_DISABLING:
                    if (onWifiStateChangeListener != null)
                        onWifiStateChangeListener.onStateChanged(State.DISABLING);
                    break;
                case WIFI_STATE_ENABLING:
                    if (onWifiStateChangeListener != null)
                        onWifiStateChangeListener.onStateChanged(State.ENABLING);
                    break;
                case WIFI_STATE_ENABLED:
                    if (onWifiStateChangeListener != null)
                        onWifiStateChangeListener.onStateChanged(State.ENABLED);
                    break;
                case WIFI_STATE_UNKNOWN:
                    if (onWifiStateChangeListener != null)
                        onWifiStateChangeListener.onStateChanged(State.UNKNOWN);
                    break;
                case WIFI_STATE_MODIFY:
                    if (onWifiChangeListener != null && wifis != null)
                        onWifiChangeListener.onWifiChanged(new ArrayList<>(wifis));
                    break;
                case WIFI_STATE_CONNECTED:
                    onWifiConnectedEvent();   // 子类可 override（如取消超时）
                    if (onWifiConnectListener != null)
                        onWifiConnectListener.onConnectChanged(true);
                    break;
                case WIFI_STATE_UNCONNECTED:
                    onWifiDisconnectedEvent(); // 子类可 override
                    if (onWifiConnectListener != null)
                        onWifiConnectListener.onConnectChanged(false);
                    break;
                case WIFI_STATE_CONNECT_TIMEOUT:
                    if (onWifiConnectListener != null)
                        onWifiConnectListener.onConnectTimeout();
                    break;
            }
        }
    };

    /** 子类 override：WiFi 连接成功时回调（如取消超时） */
    protected void onWifiConnectedEvent() {}

    /** 子类 override：WiFi 断开时回调 */
    protected void onWifiDisconnectedEvent() {}

    BaseWifiManager(Context context) {
        this.context = context.getApplicationContext();
        manager = (WifiManager) this.context.getSystemService(Context.WIFI_SERVICE);
        connectivityManager = (ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
        wifis = new ArrayList<>();
        wifiReceiver = new WifiReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        // Android 10+: 监听 WifiNetworkSuggestion 连接成功事件
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            filter.addAction(WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION);
        }
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                this.context.registerReceiver(wifiReceiver, filter, Context.RECEIVER_EXPORTED);
            } else {
                this.context.registerReceiver(wifiReceiver, filter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        modifyWifi();
    }

    @Override
    public void destroy() {
        isDestroyed = true;
        try {
            if (connectivityManager != null && currentNetworkCallback != null) {
                connectivityManager.unregisterNetworkCallback(currentNetworkCallback);
                connectivityManager.bindProcessToNetwork(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        currentNetworkCallback = null;
        try {
            if (context != null && wifiReceiver != null) {
                context.unregisterReceiver(wifiReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        handler.removeCallbacksAndMessages(null);
        onWifiChangeListener = null;
        onWifiConnectListener = null;
        onWifiStateChangeListener = null;
        wifiReceiver = null;
        manager = null;
        connectivityManager = null;
        wifis = null;
        context = null;
    }

    @Override
    public void setOnWifiChangeListener(OnWifiChangeListener onWifiChangeListener) {
        this.onWifiChangeListener = onWifiChangeListener;
        if (wifis != null && onWifiChangeListener != null) {
            onWifiChangeListener.onWifiChanged(new ArrayList<>(wifis));
        }
    }

    @Override
    public void setOnWifiConnectListener(OnWifiConnectListener onWifiConnectListener) {
        this.onWifiConnectListener = onWifiConnectListener;
    }

    @Override
    public void setOnWifiStateChangeListener(OnWifiStateChangeListener onWifiStateChangeListener) {
        this.onWifiStateChangeListener = onWifiStateChangeListener;
    }

    public class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isDestroyed || intent == null) return;
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) return;
            if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
                int what = 0;
                switch (state) {
                    case WifiManager.WIFI_STATE_DISABLED:
                        what = WIFI_STATE_DISABLED;
                        break;
                    case WifiManager.WIFI_STATE_DISABLING:
                        what = WIFI_STATE_DISABLING;
                        break;
                    case WifiManager.WIFI_STATE_ENABLING:
                        what = WIFI_STATE_ENABLING;
                        break;
                    case WifiManager.WIFI_STATE_ENABLED:
                        scanWifi();
                        what = WIFI_STATE_ENABLED;
                        break;
                    case WifiManager.WIFI_STATE_UNKNOWN:
                        what = WIFI_STATE_UNKNOWN;
                        break;
                }
                handler.sendEmptyMessage(what);
            } else if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                modifyWifi();
            } else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info == null) return;
                NetworkInfo.DetailedState state = info.getDetailedState();
                if (state == null) return;
                String SSID = info.getExtraInfo();
                if (state == NetworkInfo.DetailedState.AUTHENTICATING) {
                    if (!TextUtils.isEmpty(SSID)) modifyWifi(SSID, "身份验证中...");
                } else if (state == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
                    if (!TextUtils.isEmpty(SSID)) modifyWifi(SSID, "获取IP地址中...");
                } else if (state == NetworkInfo.DetailedState.CONNECTED) {
                    modifyWifi();
                    handler.sendEmptyMessage(WIFI_STATE_CONNECTED);
                } else if (state == NetworkInfo.DetailedState.SUSPENDED) {
                    if (!TextUtils.isEmpty(SSID)) modifyWifi(SSID, "连接中断");
                } else if (state == NetworkInfo.DetailedState.DISCONNECTING) {
                    if (!TextUtils.isEmpty(SSID)) modifyWifi(SSID, "断开中...");
                } else if (state == NetworkInfo.DetailedState.DISCONNECTED) {
                    modifyWifi();
                    handler.sendEmptyMessage(WIFI_STATE_UNCONNECTED);
                } else if (state == NetworkInfo.DetailedState.FAILED) {
                    if (!TextUtils.isEmpty(SSID)) modifyWifi(SSID, "连接失败");
                } else if (state == NetworkInfo.DetailedState.BLOCKED) {
                    if (!TextUtils.isEmpty(SSID)) modifyWifi(SSID, "WiFi无效");
                } else if (state == NetworkInfo.DetailedState.VERIFYING_POOR_LINK) {
                    if (!TextUtils.isEmpty(SSID)) modifyWifi(SSID, "信号差");
                } else if (state == NetworkInfo.DetailedState.CAPTIVE_PORTAL_CHECK) {
                    if (!TextUtils.isEmpty(SSID)) modifyWifi(SSID, "强制登录门户");
                }
            } else if (action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {
                int code = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
                if (code == WifiManager.ERROR_AUTHENTICATING) {
                    modifyWifi(null, "密码错误");
                }
            } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q
                    && action.equals(WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION)) {
                // WifiNetworkSuggestion 连接成功确认广播
                // NETWORK_STATE_CHANGED_ACTION 通常也会触发，此处作为补充确保可靠性
                modifyWifi();
                handler.sendEmptyMessage(WIFI_STATE_CONNECTED);
            }
        }
    }

    public void modifyWifi() {
        if (manager == null || wifis == null) return;
        synchronized (wifis) {
            try {
                List<ScanResult> results = manager.getScanResults();
                if (results == null) results = new ArrayList<>();
                List<IWifi> wifiList = new LinkedList<>();
                List<IWifi> mergeList = new ArrayList<>();
                List<WifiConfiguration> configurations = manager.getConfiguredNetworks();
                WifiInfo connectionInfo = manager.getConnectionInfo();
                String connectedSSID = connectionInfo != null ? connectionInfo.getSSID() : "";
                int ipAddress = connectionInfo != null ? connectionInfo.getIpAddress() : 0;
                for (ScanResult result : results) {
                    IWifi mergeObj = Wifi.create(result, configurations, connectedSSID, ipAddress);
                    if (mergeObj == null) continue;
                    mergeList.add(mergeObj);
                }
                mergeList = WifiHelper.removeDuplicate(mergeList);
                for (IWifi merge : mergeList) {
                    boolean isMerge = false;
                    for (IWifi wifi : wifis) {
                        if (wifi.equals(merge)) {
                            wifiList.add(wifi.merge(merge));
                            isMerge = true;
                            break;
                        }
                    }
                    if (!isMerge)
                        wifiList.add(merge);
                }
                wifis.clear();
                wifis.addAll(wifiList);
                handler.sendEmptyMessage(WIFI_STATE_MODIFY);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void modifyWifi(String SSID, String state) {
        if (wifis == null) return;
        synchronized (wifis) {
            List<IWifi> wifiList = new ArrayList<>();
            for (IWifi wifi : wifis) {
                if (SSID != null && (SSID.equals(wifi.SSID()) || SSID.equals(wifi.name()) || SSID.equals("\"" + wifi.name() + "\""))) {
                    wifi.state(state);
                    wifiList.add(0, wifi);
                } else if (SSID == null && wifi.isConnected()) {
                    wifi.state(state);
                    wifiList.add(0, wifi);
                } else {
                    wifi.state(null);
                    wifiList.add(wifi);
                }
            }
            wifis.clear();
            wifis.addAll(wifiList);
            handler.sendEmptyMessage(WIFI_STATE_MODIFY);
        }
    }
}
