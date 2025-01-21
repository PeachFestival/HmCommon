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

MMKV 可直接调用操作。

```java
val testUser = DataRepository.testUser 
DataRepository.testUser = “user”
```

国际化，加界面重新加载。操作会跳转你的首页Activity

```java
Locale.SIMPLIFIED_CHINESE //中文
Locale.ENGLISH //英文
LocaleHelper.getInstance().language(local)

val intent = baseContext.packageManager.getLaunchIntentForPackage(baseContext.packageName)
intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
startActivity(intent)
```

应用防止二次连点

```java
override fun onClick(v: View?) {
        super.onClick(v)
      if(AntiShake.check(v)){
            return
      }
}
```

获取设备SD卡位置，也是U盘位置

```java
 var sdPath = FileUtils.getSDPath()
```

获取路径下所有的video文件

```java
        val imgPath: String = FileUrils.getSDPath() + "/Movies"
        val files: ArrayList<File> = FileUtils.getFileName(videoPath)
```

Logistic 拟合多次曲线，logisticAB ，logisticABC，logisticABCD。

```java
 //对应y = a+bx
 var map: Map<String, Double> = LogisticUtils.logisticAB(xList,yList)
 //取值map.get("valueA") //valueA valueB valueC valueD valueR
```

NumberUtils 对应 加减乘除操作
