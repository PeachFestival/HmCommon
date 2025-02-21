package com.hengmei.hm_common.crashlog;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;

/**
 * @ClassName CrashHandler
 * @Description TODO
 * @Author thy
 * @Date 2024/4/29
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {
    //每个小时的日志都是记录在同一个文件
    private final static String LOG_FILE_CREATE_TIME_FORMAT = "yyyy-MM-dd_HH";
    private final static String LOG_FILE_SUFFIX = "_crash.log";
    //日志记入的时间
    private final static String LOG_RECORD_TIME_FORMAT ="yyyy-MM-dd HH:mm:ss";

    private UncaughtExceptionHandlerListener mHandlerListener;
    @SuppressLint("StaticFieldLeak")
    private static volatile CrashHandler sInstance;
    private final Context mContext;

    //设置日志所在文件夹路径
    private String mLogDir;

    private StringBuffer stringBuffer;

    private final Thread.UncaughtExceptionHandler mDefaultHandler;

    public static CrashHandler getInstance(Context context) {
        if (sInstance == null) {
            synchronized (CrashHandler.class) {
                if (sInstance == null) {
                    sInstance = new CrashHandler(context);
                }
            }
        }
        return sInstance;
    }

    private CrashHandler(Context context) {
        mContext = context;
        // 获取默认异常处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
        Log.e("CrashLocalInFile", "uncaughtException error");
        handleException(thread, throwable);
        if (mHandlerListener != null) {
            mHandlerListener.handlerUncaughtException(stringBuffer);
        }
    }

    /**
     * 保存崩溃日志的操作
     */
    private void handleException(@NonNull Thread thread, @NonNull Throwable ex) {
        Log.e("CrashLocalInFile", "handleException error");
        if (!TextUtils.isEmpty(mLogDir)) {
            saveCrashInfoToFile(ex);
            restartApp();
            return;
        }
        // 如果不处理,则调用系统默认处理异常,弹出系统强制关闭的对话框
        if (mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(thread, ex);
        }

    }

    /**
     * 1s后让APP重启
     */
    private void restartApp() {
        Intent intent = mContext.getPackageManager()
                .getLaunchIntentForPackage(mContext.getPackageName());
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent restartIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
        AlarmManager mgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        // 1秒钟后重启应用
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent);
        System.exit(0);
    }

    /**
     * 保存日志到本地文件
     */
    private void saveCrashInfoToFile(Throwable ex) {
        Writer info = new StringWriter();
        PrintWriter printWriter = new PrintWriter(info);
        ex.printStackTrace(printWriter);

        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        String content = info.toString();
        printWriter.close();
        StringBuffer sb = new StringBuffer();
        long time = System.currentTimeMillis();
        sb.append(">>>>>>>>>>>>>>时间");
        sb.append(CrashUtils.formatDate(new Date(time), LOG_RECORD_TIME_FORMAT));
        sb.append(">>>>>>>>>>>>>> ");
        sb.append("\r\n");
        sb.append(">>>>>>>>>>>>>>手机型号 ");
        sb.append(CrashUtils.getPhoneModel(mContext));
        sb.append(">>>>>>>>>>>>>> ");
        sb.append("\r\n");
        sb.append(">>>>>>>>>>>>>>应用版本号 ");
        sb.append(CrashUtils.getAppVersionCode(mContext));
        sb.append(">>>>>>>>>>>>>> ");
        sb.append("\r\n");
        sb.append(">>>>>>>>>>>>>>可用/总内存 ");
        sb.append(CrashUtils.getAvailMemory(mContext)).append("/").append(CrashUtils.getTotalMemory(mContext));
        sb.append(">>>>>>>>>>>>>> ");
        sb.append("\r\n");
        sb.append(content);
        sb.append("\r\n");
        sb.append("\r\n");
        stringBuffer =sb;
        CrashUtils.writeToFile(mLogDir,generateLogFileName(time), sb.toString(), "utf-8");
    }

    /**
     * 生成日志文件名
     */
    private String generateLogFileName(long time){
        return "error" +
                "_" +
                CrashUtils.formatDate(new Date(time), LOG_FILE_CREATE_TIME_FORMAT) +
                LOG_FILE_SUFFIX;
    }

    public void setCrashLogDir(String logDirPath) {
        mLogDir = logDirPath;
    }

    public void setHandlerListener(UncaughtExceptionHandlerListener mHandlerListener) {
        this.mHandlerListener = mHandlerListener;
    }

    /**
     * 未捕获异常的处理监听器
     */
    public interface UncaughtExceptionHandlerListener {
        /**
         * 当获取未捕获异常时的处理
         * 一般用于关闭界面和数据库、网络连接等等
         */
        void handlerUncaughtException(StringBuffer sb);
    }

}
