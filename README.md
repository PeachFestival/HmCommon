HM_COMMON

引用
方式一：使用本地mavenLocal仓库  需要clone项目到本地并执行publish脚本


1.项目build.gradle文件添加
```groovy
repositories {
    mavenLocal()
}
```

2.app下build.gradle文件添加
```groovy
implementation "com.hm.lib:CommonLib:lastviersion"
```

3.需要将lib库中的第三方引用全部添加到app下build.gradle
```groovy
api 'androidx.core:core-ktx:1.10.1'
api 'androidx.appcompat:appcompat:1.6.1'
api 'com.google.android.material:material:1.9.0'
api 'androidx.constraintlayout:constraintlayout:2.1.4'
api 'androidx.lifecycle:lifecycle-livedata-ktx:2.6.1'
api 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1'
api 'androidx.navigation:navigation-fragment-ktx:2.7.1'
api 'androidx.navigation:navigation-ui-ktx:2.7.1'
api 'androidx.lifecycle:lifecycle-common:2.6.1'

testImplementation 'junit:junit:4.13.2'
androidTestImplementation 'androidx.test.ext:junit:1.1.5'
androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
// 网络请求
api 'com.squareup.retrofit2:retrofit:2.11.0'
api 'com.squareup.retrofit2:converter-gson:2.11.0'
api 'com.squareup.okhttp3:logging-interceptor:4.11.0'

// JSON 解析
api 'com.google.code.gson:gson:2.10.1'

api 'com.orhanobut:logger:2.2.0'

//  port 串口 接口处理
api 'io.github.xmaihh:serialport:2.1.1'
//Image Loading
api("io.coil-kt:coil:2.5.0")
api("io.coil-kt:coil-video:2.5.0")

//    MMKV 相关KTX
api "com.tencent:mmkv-static:1.2.14"
api "androidx.startup:startup-runtime:1.1.1"
```

方式二：使用远程github仓库


1.项目build.gradle文件添加
```groovy
mavenCentral()
maven { url 'https://jitpack.io' }
```

2.app下build.gradle文件添加
```groovy
implementation 'com.github.PeachFestival:HmCommon:Tag'
```

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
