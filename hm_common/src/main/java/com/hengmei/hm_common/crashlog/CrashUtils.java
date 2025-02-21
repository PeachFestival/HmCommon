package com.hengmei.hm_common.crashlog;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * @ClassName CrashUtils
 * @Description TODO
 * @Author thy
 * @Date 2024/4/29
 */
public class CrashUtils {
    //默认为北京时间对应的东八区
    private static final TimeZone GMT = TimeZone.getTimeZone("GMT+8");
    //SD卡的最小剩余容量大小1MB
    private final static long DEFAULT_LIMIT_SIZE = 1;
    private static String imei;

    /**
     * 获取应用版本号
     */
    public static int getAppVersionCode(Context mContext) {
        try {
            PackageManager manager = mContext.getPackageManager();
            PackageInfo info = manager.getPackageInfo(mContext.getPackageName(), 0);
            return info.versionCode;
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * 获取手机型号
     */
    public static String getPhoneModel(Context mContext) {
        // 取得 android 版本
        String manufacturer = "";
        String model = "";
        try {

            Class<Build> build_class = Build.class;
            // 取得牌子
            Field manu_field = build_class.getField("MANUFACTURER");
            //noinspection InstantiationOfUtilityClass
            manufacturer = (String) manu_field.get(new Build());
            // 取得型號
            Field field2 = build_class.getField("MODEL");
            //noinspection InstantiationOfUtilityClass
            model = (String) field2.get(new Build());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return manufacturer + " " + model;
    }

    /**
     * 获取android当前可用内存大小
     */
    public static String getAvailMemory(Context mContext) {// 获取android当前可用内存大小

        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        //mi.availMem; 当前系统的可用内存

        return Formatter.formatFileSize(mContext, mi.availMem);// 将获取的内存大小规格化
    }

    /**
     * 获得系统总内存
     */
    public static String getTotalMemory(Context mContext) {
        String str1 = "/proc/meminfo";// 系统内存信息文件
        String str2;
        String[] arrayOfString;
        long initial_memory = 0;

        try {
            FileReader localFileReader = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(
                    localFileReader, 8192);
            str2 = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大小

            arrayOfString = str2.split("\\s+");
            for (String num : arrayOfString) {
                Log.i(str2, num + "\t");
            }

            initial_memory = Integer.parseInt(arrayOfString[1]) * 1024L;// 获得系统总内存，单位是KB，乘以1024转换为Byte
            localBufferedReader.close();

        } catch (IOException ignored) {
        }
        return Formatter.formatFileSize(mContext, initial_memory);// Byte转换为KB或者MB，内存大小规格化
    }


    public static String formatDate(Date date, String pattern) {
        if (date == null) {
            throw new IllegalArgumentException("date is null");
        }
        if (pattern == null) {
            throw new IllegalArgumentException("pattern is null");
        }
        SimpleDateFormat formatter = formatFor(pattern);
        return formatter.format(date);
    }

    private static final ThreadLocal<SoftReference<Map<String, SimpleDateFormat>>> THREAD_LOCAL_FORMATS = new ThreadLocal<SoftReference<Map<String, SimpleDateFormat>>>() {
        @Override
        protected SoftReference<Map<String, SimpleDateFormat>> initialValue() {
            return new SoftReference<Map<String, SimpleDateFormat>>(
                    new HashMap<String, SimpleDateFormat>());
        }
    };

    private static SimpleDateFormat formatFor(String pattern) {
        SoftReference<Map<String, SimpleDateFormat>> ref = THREAD_LOCAL_FORMATS.get();
        assert ref != null;
        Map<String, SimpleDateFormat> formats = ref.get();
        if (formats == null) {
            formats = new HashMap<String, SimpleDateFormat>();
            THREAD_LOCAL_FORMATS.set(new SoftReference<Map<String, SimpleDateFormat>>(formats));
        }

        SimpleDateFormat format = formats.get(pattern);
        if (format == null) {
            format = new SimpleDateFormat(pattern, Locale.CHINA);
            format.setTimeZone(GMT);
            formats.put(pattern, format);
        }
        return format;
    }

    public static void writeToFile(String dir, String fileName, String content, String encoder) {
        File file = new File(dir, fileName);
        File parentFile = file.getParentFile();
        OutputStreamWriter osw = null;
        BufferedWriter bw = null;
        try {
            assert parentFile != null;
            if (!parentFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                parentFile.mkdirs();
            }
            if (!file.exists()) {
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
            }
            osw = new OutputStreamWriter(new FileOutputStream(file, true), encoder);
            bw = new BufferedWriter(osw);
            bw.append(content);
            bw.append("\r\n");
            bw.flush();
        } catch (IOException ignored) {

        } finally {
            closeSilently(bw);
            closeSilently(osw);
        }
    }

    /**
     * 关闭流操作
     */
    public static void closeSilently(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) {
            }
        }
    }

    public static boolean isSDCardAvailable(Context context) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // ToastUtil.showToast(context, "SD卡容量不足，请检查");
            return getSDFreeSize() > DEFAULT_LIMIT_SIZE;
        } else {
            // ToastUtil.showToast(context, "SD卡不存在或者不可用");
            return false;
        }
    }

    /**
     * 获取SDCard的剩余大小
     *
     * @return 多少MB
     */
    @SuppressWarnings("deprecation")
    public static long getSDFreeSize() {
        // 取得SD卡文件路径
        File path = Environment.getExternalStorageDirectory();
        StatFs sf = new StatFs(path.getPath());
        // 获取单个数据块的大小(Byte)
        long blockSize = sf.getBlockSize();
        // 空闲的数据块的数量
        long freeBlocks = sf.getAvailableBlocks();
        // 返回SD卡空闲大小
        return (freeBlocks * blockSize) / 1024 / 1024; // 单位MB
    }

}
