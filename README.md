# HM_COMMON

## 1.项目引用

### 1.1 项目build.gradle文件添加

```groovy
    mavenCentral()
    maven { url 'https://jitpack.io' }
```

### 1.2 app下build.gradle文件添加

```groovy
    implementation 'com.github.PeachFestival:HmCommon:version_code'//version_code为最新tag 当前最新版本1.1.2
```

---

## 2.初始化引用 [查看 MyApplication](./app/src/main/java/com/hengmei/testdemo/MyApplication.kt)

### 2.1 全局初始化,在APP中Application类中添加初始化信息。

```Java
    CommonLibInit().init(this);
```

### 2.2 添加MMKV KTX相关配置

```Java
    val dir = filesDir.absolutePath + "/mmkv_hengmei"
    MMKV.initialize(this, dir)
    MMKVOwner.default = MMKV.defaultMMKV()
```

### 2.3 在AndroidManifest.xml 中 <application/>标签下添加 

```html
    <provider  
     android:name="androidx.startup.InitializationProvider"  
     android:authorities="${applicationId}.androidx-startup"  
     android:exported="false"  
     tools:node="merge">  
         <meta-data android:name="com.hengmei.common_lib.mmkv.MMKVInitializer"  
         android:value="androidx.startup" />  
    </provider>
```

&emsp;注：如果发现找不到 InitializationProvider ，则需要在项目中添加依赖 

```java
    implementation "androidx.startup:startup-runtime:1.1.1"
```

---

## 3.功能使用

### 3.1 扩展函数

```java
    getAndroidId(context) //获取仪器设备号
    byteToInt(byte1,byte2)//无符号byte转int 
    log(str)//全局日志弹窗
```

### 3.2 全局通信,功能强大,童叟无欺

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

### 3.3 MMKV 可直接调用操作。

```java
    val testUser = DataRepository.testUser 
    DataRepository.testUser = "user"
```

### 3.4 国际化，加界面重新加载。操作会跳转你的首页Activity

```java
    Locale.SIMPLIFIED_CHINESE //中文
    Locale.ENGLISH //英文
    LocaleHelper.getInstance().language(local)
    
    val intent = baseContext.packageManager.getLaunchIntentForPackage(baseContext.packageName)
    intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    startActivity(intent)
```

### 3.5 应用防止二次连点

```java
    override fun onClick(v: View?) {
            super.onClick(v)
          if(AntiShake.check(v)){
                return
          }
    }
```

### 3.6 获取设备SD卡位置，也是U盘位置

```java
     var sdPath = FileUtils.getSDPath()
```

### 3.7 获取路径下所有的video文件

```java
    val imgPath: String = FileUrils.getSDPath() + "/Movies"
    val files: ArrayList<File> = FileUtils.getFileName(videoPath)
```

### 3.8 Logistic 拟合多次曲线，logisticAB ，logisticABC，logisticABCD。
### &emsp;分别对应计算曲线 getLogisticValueAB, getLogisticValueABC, getLogisticValueABCD 

```java
     //对应y = a+bx
     var map: Map<String, Double> = LogisticUtils.logisticAB(xList,yList)
     //取值map.get("valueA") //valueA valueB valueC valueD valueR
     
     getLogisticValueAB() //传入对应参数，对应计算公式
```

### 3.9 NumberUtils 对应 加减乘除操作

```java
    additionStr("1","1") //小数相加
    subtractionStr()//小数相减，参数为空当0处理
    multiplicationStr()//乘法保留小数位数，参数为空不运算
    divisionStr()//除法保留小数位数，参数为空不运算
    formatStr() //将double转换指定小数位数
```

### 3.10 仪器更新

```java
    /**
    - @param mUrl 请求地址
    - @param updateApk 更新包的标识
    - @param acCode 仪器编号
    - @param title 更新标题
    - @param context activity
    - @param callback 回调函数
      */
    UpdateUtils.getUpdateUrl("http://manage.hengmeierp.com/api/project/produceApkRela/getByApp","e4fec07c-8917-44ca-99f5-582daa869f02","eb477b2f48026fda","更新", activity!!, callback = { isUpdate, message ->
          if (isUpdate!!) {
              // 调用回调返回错误信息
              println("开始更新: $message")
          } else {
              // 调用回调返回响应数据
              println("更新失败: $message")
          }
      })
```

### 3.11 仪器 WIFI 连接操作

#### 3.11.1 加入对应权限

```html
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
```

#### 3.11.2 由于权限直接请求无法请求成功，需要主动让用户从设置授予权限，以下是跳转设置代码，建议放到 setting界面 onStart() 或者 onResume() 方法体中。因为要调用startMaskService()来隐藏顶部悬浮窗，也可以手动调用 WifiSettingUtils.startMaskService（）来关闭顶部悬浮窗

```java
    // 检查应用是否有允许在上层权限
    override fun onResume() {
            super.onResume()
            WifiSettingUtils.checkUpPermission(Activity)
    }
```

#### 3.11.3 权限获取成功后，需要在清单文件配置service。

```html
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

#### 3.11.4 @xml/accessibilityservice 文件代码为以下 common_hint 是对应无障碍服务里面提示文字，随便输入即可,用户不可见。

```html
    <?xml version="1.0" encoding="utf-8"?>
    <accessibility-service
        xmlns:android="http://schemas.android.com/apk/res/android"
        android:accessibilityFeedbackType="feedbackGeneric"
        android:description="@string/common_hint"/>
```

#### 3.11.5 最后调用执行 ,如果是第一次安装或者 覆盖安装，会跳转设置，要求用户开启无障碍选项

```java
    WifiSettingUtils.setWifi(context,backStr,confirmStr)
```

### 3.12 多层级展开recyclerview
```java
        //0级列表
        val levelZero = mutableListOf<ExtendListData>()
        levelZero.add(ExtendListData(level = 0,name = "零级0"))
        levelZero.add(ExtendListData(level = 0,name = "零级1"))

        //1级列表
        val level0One = mutableListOf<ExtendListData>()
        level0One.add(ExtendListData(level = 1,name = "一级0"))
        level0One.add(ExtendListData(level = 1,name = "一级1"))
        val level1One = mutableListOf<ExtendListData>()
        level1One.add(ExtendListData(level = 1,name = "一级0"))
        level1One.add(ExtendListData(level = 1,name = "一级1"))

        //2级别列表
        val level00Two = mutableListOf<ExtendListData>()
        level00Two.add(ExtendListData(level = 2,name = "二级0"))
        level00Two.add(ExtendListData(level = 2,name = "二级1"))
        val level01Two = mutableListOf<ExtendListData>()
        level01Two.add(ExtendListData(level = 2,name = "二级0"))
        level01Two.add(ExtendListData(level = 2,name = "二级1"))
        val level10Two = mutableListOf<ExtendListData>()
        level10Two.add(ExtendListData(level = 2,name = "二级0"))
        level10Two.add(ExtendListData(level = 2,name = "二级1"))
        val level11Two = mutableListOf<ExtendListData>()
        level11Two.add(ExtendListData(level = 2,name = "二级0", data = MyData()))
        level11Two.add(ExtendListData(level = 2,name = "二级1", data = MyData()))

        binding.rvView.layoutManager = LinearLayoutManager(this)
        binding.rvView.adapter =
            ExtendListAdapter(this)
                //0级
                .addLevel0Data(levelZero)
                //1级
                .addLevelOtherData(levelZero[0], level0One)
                .addLevelOtherData(levelZero[1], level1One)
                //2级
                .addLevelOtherData(level0One[0], level00Two)
                .addLevelOtherData(level0One[1], level01Two)
                .addLevelOtherData(level1One[0], level10Two)
                //自定义UI和点击事件
                .addLevelOtherData(level1One[1], level11Two, object : OnBindExtendView(R.layout.item_extend_view) {
                    override fun onViewBind(view: View, extendListData: ExtendListData) {//UI绑定
                        val tvText = view.findViewById<TextView>(R.id.tv_name)
                        tvText.text = extendListData.name
                        tvText.setOnClickListener{
                            //do nothing
                        }
                    }

                    override fun onViewClick(view: View, extendListData: ExtendListData) {//UI点击
                        val myData : MyData = extendListData.data as MyData
                        if (extendListData.isExtend) {//展开状态
                            //do nothing
                        } else {//收起状态
                            //do nothing
                        }
                    }
                })
                .build()
```
