HM_COMMON

1.项目build.gradle文件添加

```groovy
mavenCentral()
maven { url 'https://jitpack.io' }
```

2.app下build.gradle文件添加

```groovy
implementation 'com.github.PeachFestival:HmCommon:version_code'
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

扩展函数

```java
getAndroidId(context) //获取仪器设备号
byteToInt(byte1,byte2)//无符号byte转int 
log(str)//全局日志弹窗

```

全局通信,功能强大,童叟无欺

```java
postValue(EventMessage(100, value))//发送，任意类型value
 /**
     接收
 */
observeEvent {
   if (it.key == 100) {
      log("接收内容 "+it.message)
   }
}
```

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

分别对应计算曲线 getLogisticValueAB, getLogisticValueABC, getLogisticValueABCD 

```java
 //对应y = a+bx
 var map: Map<String, Double> = LogisticUtils.logisticAB(xList,yList)
 //取值map.get("valueA") //valueA valueB valueC valueD valueR
 
 getLogisticValueAB() //传入对应参数，对应计算公式
 
```

NumberUtils 对应 加减乘除操作

```java
additionStr("1","1") //小数相加
subtractionStr()//小数相减，参数为空当0处理
multiplicationStr()//乘法保留小数位数，参数为空不运算
divisionStr()//除法保留小数位数，参数为空不运算
formatStr() //将double转换指定小数位数

@param num: Int 为要保留的小数位数
```

仪器更新

```java
/**

- @param mUrl 请求地址
- @param updateApk 更新包的标识
- @param acCode 仪器编号
- @param title 更新标题
- @param context activity
- @param callback 回调函数
  */
- UpdateUtils.getUpdateUrl("http://manage.hengmeierp.com/api/project/produceApkRela/getByApp","e4fec07c-8917-44ca-99f5-582daa869f02","eb477b2f48026fda","更新", activity!!, callback = { isUpdate, message ->
  if (isUpdate!!) {
      // 调用回调返回错误信息
      println("开始更新: $message")
  } else {
      // 调用回调返回响应数据
      println("更新失败: $message")
  }
  })
```


仪器 WIFI 连接操作

1、加入对应权限

```java
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
```

2、由于权限直接请求无法请求成功，需要主动让用户从设置授予权限，以下是跳转设置代码，建议放到 setting界面 onStart() 或者 onResume() 方法体中。因为要调用startMaskService()来隐藏顶部悬浮窗，也可以手动调用 WifiSettingUtils.startMaskService（）来关闭顶部悬浮窗

```java
// 检查应用是否有允许在上层权限
override fun onResume() {
        super.onResume()
        WifiSettingUtils.checkUpPermission(Activity)
}
```

3、权限获取成功后，需要在清单文件配置service。

```java
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

4、@xml/accessibilityservice 文件代码为以下 common_hint 是对应无障碍服务里面提示文字，随便输入即可,用户不可见。

```java
<?xml version="1.0" encoding="utf-8"?>
<accessibility-service
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:accessibilityFeedbackType="feedbackGeneric"
    android:description="@string/common_hint"/>
```

5、最后调用执行 ,如果是第一次安装或者 覆盖安装，会跳转设置，要求用户开启无障碍选项

```java
WifiSettingUtils.setWifi(context,backStr,confirmStr)
```
