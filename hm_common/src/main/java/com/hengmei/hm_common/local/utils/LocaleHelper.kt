package com.hengmei.hm_common.local.utils
import android.annotation.TargetApi
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import android.util.Log
import com.hengmei.hm_common.R
import com.hengmei.hm_common.local.LocaleConstant
import com.hengmei.hm_common.local.LocaleDefaultSPHelper
import org.json.JSONObject
import java.util.*

class LocaleHelper(private val application: Application) {

    // 静态属性，修复识别系统语言为英语的问题
    private var currentSystemLocale = Locale.SIMPLIFIED_CHINESE

    /**
     * 获取已选择的语言设置
     */
    fun getSetLocale(): Locale {
        with(LocaleDefaultSPHelper.language) {
            return when (this) {
                "0" -> currentSystemLocale
                "1" -> Locale.ENGLISH
                "2" -> Locale.SIMPLIFIED_CHINESE
                "3" -> Locale.TRADITIONAL_CHINESE
                else -> return if (JSONObject(this).getString("language") == "auto") {
                    currentSystemLocale
                } else {
                    getLocaleFromJSON(this)
                }
            }
        }
    }

    /**
     * 获取已选择的语言对应的名称
     */
    fun getSelectLanguageString(context: Context): String {
        with(LocaleDefaultSPHelper.language) {
            return when (this) {
                "0" -> context.getString(R.string.plugin_locale_language_title_auto)
                "1" -> "English"
                "2" -> "简体中文"
                "3" -> "繁體中文"
                else -> return if (JSONObject(this).getString("language") == "auto") {
                    context.getString(R.string.plugin_locale_language_title_auto)
                } else {
                    getLocaleFromJSON(this).displayName
                }

            }
        }
    }

    /**
     * 更新 Context
     */
    fun updateContext(context: Context): Context {
        val setLocale = getSetLocale()
        return if (needUpdateLocale(context, setLocale)) {
            updateResources(context, setLocale)
        } else {
            context
        }
    }

    /**
     * 更新 Resources
     */
    private fun updateResources(context: Context, locale: Locale): Context {
        // 设置默认语言
        Locale.setDefault(locale)

        // 获取 Resources 和 Configuration
        val resources = context.resources
        val configuration = resources.configuration

        // 更新 Configuration
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Android 7.0+ 使用 createConfigurationContext
            setAppLocale(configuration, locale)
            context.createConfigurationContext(configuration)
        } else {
            // Android 7.0 以下使用 updateConfiguration
            setAppLocaleLegacy(configuration, locale)
            resources.updateConfiguration(configuration, resources.displayMetrics)
            context
        }
    }

    /**
     * 设置 App 的语言（传统方式）
     */
    @Suppress("DEPRECATION")
    private fun setAppLocaleLegacy(config: Configuration, locale: Locale) {
        config.locale = locale
    }

    /**
     * 设置 App 的语言（Android N+）
     */
    @TargetApi(Build.VERSION_CODES.N)
    private fun setAppLocale(config: Configuration, locale: Locale) {
        config.setLocale(locale)
        val localeList = LocaleList(locale)
        LocaleList.setDefault(localeList)
        config.setLocales(localeList)
    }

    /**
     * 更新 ApplictionContext
     */
    fun updateApplicationContext(context: Context): Context {
        return updateContext(context.applicationContext)
    }

    /**
     * 获取系统的语言（传统方式）
     */
    private fun getSystemLocaleLegacy(): Locale {
        return Locale.getDefault()
    }

    /**
     * 获取系统的语言（Android N+）
     */
    @TargetApi(Build.VERSION_CODES.N)
    private fun getSystemLocaleN(): Locale {
        return LocaleList.getDefault().get(0)
    }

    /**
     * 设置系统的语言
     */
    fun getSystemLocale(): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            getSystemLocaleN()
        } else {
            getSystemLocaleLegacy()
        }
    }

    // 只有第一次存下来的 Locale 才是正确的系统语言，后面 LocaleList 会随着切换语言变动导致 getSystemLocale() 获取的不再是系统语言
    fun cacheSystemLocale() {
        currentSystemLocale = getSystemLocale()
    }

    // 因此若要获取系统语言只能拿已缓存下来的 Locale
    fun getCurrentSystemLocale(): Locale {
        return currentSystemLocale
    }

    /**
     * 获取 App 的语言（传统方式）
     */
    private fun getAppLocaleLegacy(context: Context) : Locale {
        return context.resources.configuration.locale
    }

    /**
     * 获取 App 的语言（Android N+）
     */
    @TargetApi(Build.VERSION_CODES.N)
    private fun getAppLocaleN(context: Context): Locale {
        return context.resources.configuration.locales.get(0)
    }

    /**
     * 获取 App 的当前语言
     */
    fun getCurrentAppLocale(context: Context): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { //7.0有多语言设置获取顶部的语言
            getAppLocaleN(context)
        } else {
            getAppLocaleLegacy(context)
        }
    }

    /**
     * 判断需不需要更新
     *
     * @param context Context
     * @param locale  New User Locale
     * @return true / false
     */
    fun needUpdateLocale(context: Context, locale: Locale): Boolean {
        return getCurrentAppLocale(context).isO3Country != locale.isO3Country
    }

    /**
     * 系统设置更改后要做的操作
     */
    fun onConfigurationChanged(context: Context) {
        cacheSystemLocale()
        updateContext(context)
        updateApplicationContext(context)
    }

    private fun getLocaleJSON(locale: Locale): String {
        val jsonObject = JSONObject()
        if (locale.language == "") {
            jsonObject.put("language", "auto")
        } else {
            jsonObject.put("language", locale.language)
        }
        return jsonObject
            .put("country", locale.country)
            .put("variant", locale.variant)
            .toString()
    }

    private fun getLocaleFromJSON(jsonString: String): Locale {
        val jsonObject = JSONObject(jsonString)
        return Locale(
            jsonObject.getString("language"),
            jsonObject.getString("country"),
            jsonObject.getString("variant")
        )
    }

    /**
     * 保存选择的语言
     */
    fun language(selectLocale: Locale): LocaleHelper {
        // 需要先保存选择的语言，否则更新 application 的语言配置时，拿到的还是上次配置的语言
        LocaleDefaultSPHelper.language = getLocaleJSON(selectLocale)
        // recreate() 后只在 BaseActivity#attachBaseContext() 更新 Context，不更新 ApplicationContext，因此要手动更新
        getInstance().updateApplicationContext(getInstance().application)
        return instance
    }

    /**
     * 应用选择的语言
     */
    fun apply(context: Context) {
        // 使用 EventBus 可以实现不重启到 LauncherActivity 只需 recreate() 即可刷新 Context 的 Resources
//        EventBus.getDefault().post(Constant.EVENT_RECREATE_ACTIVITY)
        // 使用广播也可以实现不重启到 LauncherActivity 只需 recreate() 即可刷新 Context 的 Resources
        ActivityHelper.getInstance().recreateActivity(context)
    }

    /**
     * 应用选择的语言
     */
    fun apply(context: Context, intent: Intent?) {
        // 重启到 LauncherActivity 刷新 Context 的 Resources
        ActivityHelper.getInstance().openWithClearTask(context, intent)
    }

    fun apply(context: Context, intent: Intent? = null, activityUtil: ActivityHelper? = null) {
        with(LocaleConstant) {
            when (ActivityHelper.getInstance().getInterfaceUpdateWay()) {
                RESTART_TO_LAUNCHER_ACTIVITY -> {
                    apply(context, intent!!)
                }
                RECREATE_CURRENT_ACTIVITY -> {
                    apply(context)
                }
                CUSTOM_WAY_TO_UPDATE_INTERFACE -> {
                    intent?.let {
                        activityUtil?.updateInterface(context, it)
                    }
                }
                else -> apply(context)
            }
        }
    }

    /**
     * 输出当前context使用的语言，仅调试用
     */
    @Suppress("DEPRECATION")
    @TargetApi(Build.VERSION_CODES.N)
    fun printContextLocale(context: Context, tag: String) {
        val resources = context.resources
        val config = resources.configuration
        Log.d("Locale-$tag", config.locale.language)
        Log.d("Locale-$tag", config.locales.toLanguageTags())
    }

    companion object {
        private const val TAG = "LocaleHelper"
        private lateinit var instance: LocaleHelper

        fun getInstance(): LocaleHelper {
            check(Companion::instance.isInitialized) { "LocaleHelper should be initialized first, please check you are already LocalePlugin.init(...) in application" }
            return instance
        }

        // internal 控制只能被 LocalePlugin 初始化
        internal fun init(application: Application): LocaleHelper {
            check(!Companion::instance.isInitialized) { "LocaleHelper is already initialized" }
            instance = LocaleHelper(application)
            return instance
        }
    }
}
