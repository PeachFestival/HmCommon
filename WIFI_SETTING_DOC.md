# WiFi 设置弹窗组件（WifiSettingDialog）调用与国际化接入文档

`WifiSettingUtils` 是 `hm_common` 库中封装的 WiFi 设置弹窗组件。本组件已完全适配多语言国际化，并支持丰富的外部调用方式与事件监听回调。

---

## 目录
1. [快速上手与调用方式](#1-快速上手与调用方式)
2. [多语言国际化说明与配置](#2-多语言国际化说明与配置)
3. [多语言动态切换联动](#3-多语言动态切换联动)
4. [支持的多语言资源键名表](#4-支持的多语言资源键名表)
5. [权限检查与申请](#5-权限检查与申请)

---

## 1. 快速上手与调用方式

### 方式一：最简调用（一行业务代码）
```kotlin
// Kotlin
WifiSettingUtils.showWifiDialog(this@MainActivity)
```
```java
// Java
WifiSettingUtils.showWifiDialog(MainActivity.this);
```

---

### 方式二：带连接回调调用（Kotlin 推荐）
当外部需要感知 WiFi 是否连接成功、断开连接或连接超时时，可以直接传入 Lambda 回调：

```kotlin
WifiSettingUtils.showWifiDialog(
    activity = this@MainActivity,
    onConnected = { wifi ->
        // 连接成功！wifi 为当前已连接的 IWifi 对象（包含 SSID、IP、MAC 等信息）
        val name = wifi?.name() ?: ""
        val ip = wifi?.ip() ?: ""
        Log.d("WiFi", "已成功连接到: $name, 分配IP: $ip")
    },
    onDisconnected = {
        // 网络断开连接
        Log.d("WiFi", "WiFi 网络已断开")
    },
    onTimeout = {
        // 连接超时（用户可能输错了密码）
        Log.w("WiFi", "WiFi 连接超时")
    }
)
```

---

### 方式三：接口监听调用（Java 友好）
```java
WifiSettingUtils.showWifiDialogWithListener(MainActivity.this, new WifiSettingUtils.OnWifiDialogListener() {
    @Override
    public void onConnected(@Nullable IWifi wifi) {
        if (wifi != null) {
            System.out.println("连接成功: " + wifi.name() + ", IP: " + wifi.ip());
        }
    }

    @Override
    public void onDisconnected() {
        System.out.println("网络已断开");
    }

    @Override
    public void onConnectTimeout() {
        System.out.println("连接超时");
    }
});
```

---

## 2. 多语言国际化说明与配置

组件已内置标准 Android 多语言资源体系，所有界面文本、提示框、Toast、网络状态描述均通过 `@string` 资源引用，**零硬编码文本**。

### 内置支持的语言目录：
- 默认/简体中文：`values/strings.xml`
- 英文 (English)：`values-en/strings.xml`
- 繁体中文 (繁體中文)：`values-zh-rTW/strings.xml`

### 如何扩展添加新的语言（例如日语、西班牙语等）：
在宿主项目或 `hm_common` 的 `res` 目录下新建对应语言的 `values-xxx/strings.xml` 即可覆盖或新增：
例如 **日语 (`res/values-ja/strings.xml`)**：
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="hm_wifi_title">Wi-Fi 設定</string>
    <string name="hm_wifi_refresh">更新</string>
    <string name="hm_wifi_close">閉じる</string>
    <string name="hm_wifi_empty_disabled">Wi-Fi がオフになっています</string>
    <string name="hm_wifi_empty_searching">利用可能なネットワークをスキャン中...</string>
    <string name="hm_wifi_empty_none">利用可能なネットワークが見つかりません</string>
    <string name="hm_wifi_connected_tip">現在このネットワークに接続されています</string>
    <string name="hm_wifi_saved_tip">保存済みのネットワーク</string>
    <string name="hm_wifi_disconnect">切断</string>
    <string name="hm_wifi_forget">ネットワークを削除</string>
    <string name="hm_wifi_cancel">キャンセル</string>
    <string name="hm_wifi_connect">接続</string>
    <string name="hm_wifi_pwd_dialog_title">Wi-Fi に接続</string>
    <string name="hm_wifi_pwd_ssid_prefix">Wi-Fi名: %1$s</string>
    <string name="hm_wifi_pwd_hint">パスワードを入力してください</string>
    <string name="hm_wifi_pwd_show">パスワードを表示</string>
    <string name="hm_wifi_pwd_length_error">パスワードは8文字以上である必要があります</string>
    <string name="hm_wifi_connecting">接続中...</string>
    <string name="hm_wifi_disconnecting">切断中...</string>
    <string name="hm_wifi_forget_success">設定が削除されました</string>
    <string name="hm_wifi_timeout_tip">接続がタイムアウトしました。パスワードを確認してください</string>
    <string name="hm_wifi_status_connected">接続済み</string>
    <string name="hm_wifi_status_connecting">接続中...</string>
    <string name="hm_wifi_status_authenticating">認証中...</string>
    <string name="hm_wifi_status_obtaining_ip">IPアドレスを取得中...</string>
    <string name="hm_wifi_status_pwd_error">パスワードが違います</string>
    <string name="hm_wifi_status_saved">保存済み</string>
    <string name="hm_wifi_status_fail">接続失敗</string>
</resources>
```

---

## 3. 多语言动态切换联动

本库已内置 `LocaleHelper`，如果你的 App 内部有“语言切换”功能：
```kotlin
// 切换为英文并生效
LocaleHelper.getInstance().language(Locale.ENGLISH).apply(this@MainActivity)

// 切换为简体中文
LocaleHelper.getInstance().language(Locale.SIMPLIFIED_CHINESE).apply(this@MainActivity)

// 切换为繁体中文
LocaleHelper.getInstance().language(Locale.TRADITIONAL_CHINESE).apply(this@MainActivity)
```
切换语言后，WiFi 弹窗中的所有标题、按钮、密码提示、状态描述均会自动跟随切换。

---

## 4. 支持的多语言资源键名表

| 资源 ID (String Name) | 默认/中文 (values) | 英文 (values-en) | 繁体中文 (values-zh-rTW) |
|---|---|---|---|
| `hm_wifi_title` | WLAN 设置 | Wi-Fi Settings | WLAN 設定 |
| `hm_wifi_refresh` | 刷新 | Refresh | 重新整理 |
| `hm_wifi_close` | 关闭 | Close | 關閉 |
| `hm_wifi_empty_disabled` | WLAN 已关闭，请开启以搜索可用网络 | Wi-Fi is turned off. Turn it on to scan available networks | WLAN 已關閉，請開啟以搜尋可用網路 |
| `hm_wifi_empty_searching` | 正在扫描附近的可用网络... | Scanning for available networks... | 正在搜尋附近的可用網路... |
| `hm_wifi_empty_none` | 未找到附近的可用网络 | No available networks found | 未找到附近的可用網路 |
| `hm_wifi_connected_tip` | 当前已连接到此网络 | Currently connected to this network | 目前已連線至此網路 |
| `hm_wifi_saved_tip` | 已保存的网络 | Saved network | 已儲存的網路 |
| `hm_wifi_disconnect` | 断开连接 | Disconnect | 中斷連線 |
| `hm_wifi_forget` | 忘记网络 | Forget Network | 清除網路 |
| `hm_wifi_cancel` | 取消 | Cancel | 取消 |
| `hm_wifi_connect` | 连接 | Connect | 連線 |
| `hm_wifi_pwd_dialog_title` | 连接到 WLAN | Connect to Wi-Fi | 連線至 WLAN |
| `hm_wifi_pwd_ssid_prefix` | WiFi 名称: %1$s | Wi-Fi: %1$s | WiFi 名稱: %1$s |
| `hm_wifi_pwd_hint` | 请输入密码 | Enter password | 請輸入密碼 |
| `hm_wifi_pwd_show` | 显示密码 | Show password | 顯示密碼 |
| `hm_wifi_pwd_length_error` | 密码长度不能少于8位 | Password must be at least 8 characters | 密碼長度不能少於8位 |
| `hm_wifi_connecting` | 正在连接... | Connecting... | 正在連線... |
| `hm_wifi_disconnecting` | 正在断开... | Disconnecting... | 正在中斷連線... |
| `hm_wifi_forget_success` | 已清除该网络配置 | Network configuration removed | 已清除該網路配置 |
| `hm_wifi_timeout_tip` | 连接超时，请检查密码后重试 | Connection timed out, please check the password and try again | 連線逾時，請檢查密碼後重試 |
| `hm_wifi_status_connected` | 已连接 | Connected | 已連線 |
| `hm_wifi_status_connecting` | 开始连接... | Connecting... | 開始連線... |
| `hm_wifi_status_authenticating`| 身份验证中... | Authenticating... | 身份驗證中... |
| `hm_wifi_status_obtaining_ip` | 获取IP地址中... | Obtaining IP address... | 取得IP位址中... |
| `hm_wifi_status_pwd_error` | 密码错误 | Incorrect password | 密碼錯誤 |
| `hm_wifi_status_saved` | 已保存 | Saved | 已儲存 |
| `hm_wifi_status_fail` | 连接失败 | Connection failed | 連線失敗 |

---

## 5. 权限检查与申请

在调用 `showWifiDialog` 前，建议在宿主 Activity 初始化时申请必要权限：
```kotlin
// 启动时一键检查并申请所需权限
WifiSettingUtils.checkAndRequestWifiPermissions(this@MainActivity)
```
函数内部会自动根据当前 Android 系统版本（如 Android 13+ 的 `NEARBY_WIFI_DEVICES` 或 Android 6+ 的定位权限）动态匹配申请。
