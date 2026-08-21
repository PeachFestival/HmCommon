# HM_COMMON

## 1. 项目引用

### 1.1 项目 build.gradle 文件添加

```groovy
    mavenCentral()
    maven { url 'https://jitpack.io' }
```

### 1.2 app 下 build.gradle 文件添加

```groovy
    implementation 'com.github.PeachFestival:HmCommon:2.3' // 当前最新版本 2.3
```

---

## 2. 初始化引用 [查看 MyApplication](./app/src/main/java/com/hengmei/testdemo/MyApplication.kt)

### 2.1 全局初始化，在 APP 的 Application 类中添加初始化代码

```kotlin
    CommonLibInit().init(this)
```

### 2.2 添加 MMKV KTX 相关配置

```kotlin
    val dir = filesDir.absolutePath + "/mmkv_hengmei"
    MMKV.initialize(this, dir)
    MMKVOwner.default = MMKV.defaultMMKV()
```

### 2.3 在 AndroidManifest.xml 中 `<application/>` 标签下添加

```xml
    <provider  
     android:name="androidx.startup.InitializationProvider"  
     android:authorities="${applicationId}.androidx-startup"  
     android:exported="false"  
     tools:node="merge">  
         <meta-data android:name="com.hengmei.common_lib.mmkv.MMKVInitializer"  
         android:value="androidx.startup" />  
    </provider>
```

&emsp;注：如果发现找不到 InitializationProvider ，则需要在项目中添加依赖：

```groovy
    implementation "androidx.startup:startup-runtime:1.1.1"
```

---

## 3. 功能使用

### 3.1 扩展函数

```kotlin
    getAndroidId() // 获取仪器设备号
    byteToInt(byte1, byte2) // 无符号 byte 转 int 
    log(str) // 全局日志弹窗
```

### 3.2 全局通信 (FlowBus)

```kotlin
    postValue(EventMessage(100, value)) // 发送任意类型 value

    /**
     * 接收
     */
    observeEvent {
       if (it.key == 100) {
          log("接收内容 " + it.message)
       }
    }
```

### 3.3 MMKV 键值对存储

```kotlin
    val testUser = DataRepository.testUser 
    DataRepository.testUser = "user"
```

### 3.4 国际化多语言切换

本库支持中文、英文、繁体中文等多语言动态切换及持久化：

```kotlin
    // 切换语言
    LocaleHelper.getInstance().language(Locale.SIMPLIFIED_CHINESE).apply(this@MainActivity) // 简体中文
    LocaleHelper.getInstance().language(Locale.ENGLISH).apply(this@MainActivity) // 英文
    LocaleHelper.getInstance().language(Locale.TRADITIONAL_CHINESE).apply(this@MainActivity) // 繁体中文
```

在 Activity 中重写 `attachBaseContext` 以确保资源配置生效：
```kotlin
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.getInstance().updateContext(newBase))
    }
```

### 3.5 应用防止二次连点

```kotlin
    override fun onClick(v: View?) {
        super.onClick(v)
        if (AntiShake.check(v)) {
            return
        }
    }
```

### 3.6 获取设备 SD 卡 / U 盘路径

```kotlin
    val sdPath = FileUtils.getSDPath()
```

### 3.7 获取指定路径下所有的视频文件

```kotlin
    val videoPath: String = FileUtils.getSDPath() + "/Movies"
    val files: ArrayList<File> = FileUtils.getFileName(videoPath)
```

### 3.8 Logistic 拟合多次曲线

```kotlin
    // 对应 y = a + bx
    val map: Map<String, Double> = LogisticUtils.logisticAB(xList, yList)
    // 取值: map["valueA"], map["valueB"], map["valueC"], map["valueD"], map["valueR"]
    
    getLogisticValueAB() // 传入对应参数，对应计算公式
```

### 3.9 NumberUtils 高精度计算

```kotlin
    additionStr("1", "1") // 小数相加
    subtractionStr() // 小数相减，参数为空当 0 处理
    multiplicationStr() // 乘法保留小数位数，参数为空不运算
    divisionStr() // 除法保留小数位数，参数为空不运算
    formatStr() // 将 double 转换指定小数位数
```

### 3.10 仪器 OTA / 应用更新

```kotlin
    /**
     * @param mUrl 请求地址
     * @param updateApk 更新包标识
     * @param acCode 仪器编号
     * @param title 更新标题
     * @param context Activity 上下文
     * @param callback 回调函数
     */
    UpdateUtils.getUpdateUrl(
        "http://manage.hengmeierp.com/api/project/produceApkRela/getByApp",
        "e4fec07c-8917-44ca-99f5-582daa869f02",
        getAndroidId(),
        "更新",
        this@MainActivity,
        callback = { isUpdate, message ->
            if (isUpdate == true) {
                println("开始更新: $message")
            } else {
                println("更新失败: $message")
            }
        }
    )
```

### 3.11 仪器 WIFI 连接操作

#### &emsp;3.11.1 【推荐】新版 WiFi 设置弹窗组件（支持国际化多语言与状态监听）

`WifiSettingUtils.showWifiDialog` 提供了内置的 WiFi 设置弹窗组件，包含 WiFi 列表扫描、状态展示、密码连接、忘记网络、断开连接等完整功能。弹窗已全量适配横屏工业仪器分辨率，并支持简体中文、英文、繁体中文等国际化多语言。

##### 1. 动态权限申请（在 Activity 创建时调用）
```kotlin
    // 启动时自动检查并申请 WiFi 及定位相关权限
    WifiSettingUtils.checkAndRequestWifiPermissions(this@MainActivity)
```

##### 2. 基础调用（一行业务代码）
```kotlin
    // 打开 WiFi 设置弹窗
    WifiSettingUtils.showWifiDialog(this@MainActivity)
```

##### 3. 带连接状态监听回调（Kotlin 推荐）
```kotlin
    WifiSettingUtils.showWifiDialog(
        activity = this@MainActivity,
        onConnected = { wifi ->
            // 连接成功！wifi 包含 SSID、IP、MAC 等详细信息
            val name = wifi?.name() ?: ""
            val ip = wifi?.ip() ?: ""
            println("WiFi 连接成功: $name, 分配 IP: $ip")
        },
        onDisconnected = {
            // 网络已断开
            println("WiFi 已断开连接")
        },
        onTimeout = {
            // 连接超时（如密码错误或信号差）
            println("WiFi 连接超时")
        }
    )
```

##### 4. 接口监听方式（Java 调用友好）
```java
    WifiSettingUtils.showWifiDialogWithListener(MainActivity.this, new WifiSettingUtils.OnWifiDialogListener() {
        @Override
        public void onConnected(@Nullable IWifi wifi) {
            if (wifi != null) {
                System.out.println("WiFi 连接成功: " + wifi.name() + ", IP: " + wifi.ip());
            }
        }

        @Override
        public void onDisconnected() {
            System.out.println("WiFi 已断开");
        }

        @Override
        public void onConnectTimeout() {
            System.out.println("WiFi 连接超时");
        }
    });
```

##### 5. 国际化多语言支持说明
弹窗内部文字全部引用 `@string/hm_wifi_...` 资源，随系统语言或 `LocaleHelper` 动态切换：
- 默认/简体中文：`values/strings.xml`
- 英文 (English)：`values-en/strings.xml`
- 繁体中文 (繁體中文)：`values-zh-rTW/strings.xml`

---

#### &emsp;3.11.2 【传统方式】跳转系统 WiFi 设置界面（通过无障碍服务悬浮窗遮挡）

##### 1. 加入对应权限
```xml
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
```

##### 2. 检查并引导开启悬浮窗权限
```kotlin
    override fun onResume() {
        super.onResume()
        WifiSettingUtils.checkUpPermission(this)
    }
```

##### 3. 在 AndroidManifest.xml 中配置无障碍服务 Service
```xml
    <service 
        android:name="com.hengmei.hm_common.window.FloatBallService"
        android:exported="true"
        android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE">
        <intent-filter>
            <action android:name="android.accessibilityservice.AccessibilityService" />
        </intent-filter>
        <meta-data
            android:name="android.accessibilityservice"
            android:resource="@xml/accessibilityservice" />
    </service>
```

##### 4. 调用系统设置跳转方法
```kotlin
    WifiSettingUtils.setWifi(this, "返回", "确认")
```

---

### 3.12 多层级展开 RecyclerView（支持无限层级）

```kotlin
    val levelZero = mutableListOf<ExtendListData>()
    levelZero.add(ExtendListData(level = 0, name = "零级0"))
    levelZero.add(ExtendListData(level = 0, name = "零级1"))

    binding.rvView.layoutManager = LinearLayoutManager(this)
    binding.rvView.adapter = ExtendListAdapter(this)
        .addLevel0Data(levelZero)
        .build()
```

### 3.13 闪退日志保存到本地

```kotlin
    // 在 Application 中初始化
    CrashHandler.getInstance(applicationContext).setCrashLogDir(getCrashLogDir())
```

### 3.14 获取后台动态密码

```kotlin
    BackagePasswordUtils.getBackagePassword(this, callback = { isSuccess, message ->
        if (isSuccess) {
            longToast("获取到的后台密码为：$message")
        } else {
            longToast(message)
        }
    })
```
