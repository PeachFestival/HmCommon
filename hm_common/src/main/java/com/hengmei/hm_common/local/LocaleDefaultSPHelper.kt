package com.hengmei.hm_common.local

import android.content.Context

import org.json.JSONObject

/**
 * 默认 Preference
 * @author Bakumon https://bakumon.me
 */
class LocaleDefaultSPHelper(private val context: Context) {

    fun getContext(): Context {
        return context
    }

    /**
     * Returns the name used for storing default shared preferences.
     *
     * @see .getDefaultSharedPreferences
     */
    private fun getDefaultSharedPreferencesName(): String? {
        return context.packageName + "_preferences"
    }

    private fun getDefaultSharedPreferencesMode(): Int {
        return Context.MODE_PRIVATE
    }

    companion object {
        private lateinit var instance: LocaleDefaultSPHelper

        private fun getInstance(): LocaleDefaultSPHelper {
            check(Companion::instance.isInitialized) { "LocaleDefaultSPHelper should be initialized first, please check you are already LocalePlugin.init(...) in application" }
            return instance
        }

        /**
         * 语言
         */
        var language: String
            get() = getInstance().getContext().getSharedPreferences(
                getInstance().getDefaultSharedPreferencesName(),
                getInstance().getDefaultSharedPreferencesMode()
            ).getString(
                LocaleConstant.LANGUAGE,
                JSONObject().put("language", "auto").toString()
            ) ?: JSONObject().put("language", "auto").toString()
            set(value) = getInstance().getContext().getSharedPreferences(
                getInstance().getDefaultSharedPreferencesName(),
                getInstance().getDefaultSharedPreferencesMode()
            ).edit().putString(
                LocaleConstant.LANGUAGE,
                value
            ).apply()

        fun init(context: Context): LocaleDefaultSPHelper {
            check(!Companion::instance.isInitialized) { "LocaleDefaultSPHelper is already initialized" }
            instance = LocaleDefaultSPHelper(context)
            return instance
        }
    }
}