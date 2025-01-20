HM_COMMON

使用

1、全局初始化,在APP中Application类中添加初始化信息。

```Java
CommonLibInit().init(this);
```

添加MMKV KTX相关配置

```Java
val dir = filesDir.absolutePath + "/mmkv_hengmei"
MMKV.initialize(this, dir)
MMKVOwner.default = MMKV.defaultMMKV()
```

在AndroidManifest.xml 中 <application/>标签下添加 

```java
<provider  
 android:name="androidx.startup.InitializationProvider"  
 android:authorities="${applicationId}.androidx-startup"  
 android:exported="false"  
 tools:node="merge">  
 <meta-data android:name="com.hengmei.common_lib.mmkv.MMKVInitializer"  
 android:value="androidx.startup" />  
</provider>
```

如果发现找不到 InitializationProvider ，则需要在项目中添加依赖 

```java
    implementation "androidx.startup:startup-runtime:1.1.1"
```





2、使用

MMKV 可直接调用 

```java
val testUser = DataRepository.testUser 
DataRepository.testUser = “user”
```

国际化

```java

```
