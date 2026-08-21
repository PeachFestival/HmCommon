package com.iwdael.wifimanager;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class WifiHelper {
    public static final String WEP = "WEP";
    public static final String PSK = "PSK";
    public static final String EAP = "EAP";
    public static final String WPA = "WPA";

    public static int configOrCreateWifi(WifiManager manager, IWifi wifi, String password) {
        if (manager == null || wifi == null) return -1;
        try {
            List<WifiConfiguration> configurations = manager.getConfiguredNetworks();
            if (configurations != null) {
                for (WifiConfiguration configuration : configurations) {
                    if (configuration.SSID != null && configuration.SSID.equals(wifi.SSID())) {
                        if (password != null && !password.isEmpty()) {
                            // 传入了新密码，移除旧配置以应用新密码
                            manager.removeNetwork(configuration.networkId);
                            manager.saveConfiguration();
                            break;
                        } else {
                            // 未传新密码（已保存网络重连），直接返回已有 networkId
                            return configuration.networkId;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        WifiConfiguration configuration = createWifiConfiguration(wifi, password);
        return saveWifiConfiguration(manager, configuration);
    }

    public static boolean deleteWifiConfiguration(WifiManager manager, IWifi wifi) {
        try {
            List<WifiConfiguration> configurations = manager.getConfiguredNetworks();
            if (configurations != null) {
                for (WifiConfiguration configuration : configurations) {
                    if (configuration.SSID != null && configuration.SSID.equals(wifi.SSID())) {
                        boolean ret = manager.removeNetwork(configuration.networkId);
                        ret = ret & manager.saveConfiguration();
                        return ret;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static WifiConfiguration createWifiConfiguration(IWifi wifi, String password) {
        WifiConfiguration configuration = new WifiConfiguration();
        configuration.SSID = wifi.SSID();
        configuration.hiddenSSID = false;
        configuration.status = WifiConfiguration.Status.ENABLED;

        String caps = wifi.capabilities() != null ? wifi.capabilities().toUpperCase() : "";

        if (password == null || password.isEmpty()) {
            if (caps.contains(WEP)) {
                configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                configuration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                configuration.wepTxKeyIndex = 0;
                configuration.wepKeys[0] = "";
            } else if (caps.contains(PSK) || caps.contains(WPA)) {
                configuration.preSharedKey = "";
            } else if (caps.contains(EAP)) {
                configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
                configuration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                configuration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                configuration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                configuration.preSharedKey = "";
            } else {
                // 开放无密码网络
                configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                configuration.preSharedKey = null;
            }
        } else {
            configuration.allowedAuthAlgorithms.clear();
            configuration.allowedGroupCiphers.clear();
            configuration.allowedKeyManagement.clear();
            configuration.allowedPairwiseCiphers.clear();
            configuration.allowedProtocols.clear();

            if (caps.contains(WEP)) {
                configuration.preSharedKey = "\"" + password + "\"";
                configuration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                configuration.wepTxKeyIndex = 0;
            } else if (caps.contains("WPA3") || caps.contains("SAE")) {
                // WPA3-SAE 支持
                configuration.preSharedKey = "\"" + password + "\"";
                try {
                    // API 29+ KeyMgmt.SAE
                    configuration.allowedKeyManagement.set(8); // WifiConfiguration.KeyMgmt.SAE = 8
                } catch (Exception ignore) {
                    configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                }
                configuration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                configuration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            } else {
                // WPA / WPA2-PSK
                configuration.preSharedKey = "\"" + password + "\"";
                configuration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                configuration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                configuration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                configuration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                configuration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            }
        }

        return configuration;
    }

    private static int saveWifiConfiguration(WifiManager manager, WifiConfiguration configuration) {
        int networkId = manager.addNetwork(configuration);
        if (networkId != -1) {
            manager.saveConfiguration();
        }
        return networkId;
    }

    public static List<IWifi> removeDuplicate(List<IWifi> list) {
        if (list == null) return new ArrayList<>();
        Collections.sort(list, new Comparator<IWifi>() {
            @Override
            public int compare(IWifi l, IWifi r) {
                return r.level() - l.level();
            }
        });
        List<IWifi> set = new ArrayList<>();
        for (IWifi wifi : list) {
            if (!set.contains(wifi)) {
                if (wifi.isConnected())
                    set.add(0, wifi);
                else
                    set.add(wifi);
            }
        }
        return set;
    }
}
