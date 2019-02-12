package com.attsinghua.dwf;

/* 
 * 本类实现了缓存、数据库等数据的清除
 */

import java.io.File;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

public class ControlDataCleaner {
	
	private static final String TAG = "ControlDataCleaner";
	private static SharedPreferences mySharedPreferences;
	
	/** 
	 * ####################################################################
	 * 
	 * 01 - 清除本应用内部缓存(/data/data/com.xxx.xxx/cache)
	 * 02 - 清除本应用默认路径数据库(/data/data/com.xxx.xxx/databases)
	 * 03 - 清除本应用用户文件(/data/data/com.xxx.xxx/shared_prefs)
	 * 04 - 清除本应用指定名称的数据库(按名字 注意:名字需要外部传入)
	 * 05 - 清除/data/data/com.xxx.xxx/files下的内容
	 * 06 - 清除外部cache下的内容(/mnt/sdcard/android/data/com.xxx.xxx/cache)
	 * 07 - 清除自定义路径下的文件 使用需小心请不要误删 而且只支持目录下的文件删除
	 * 08 - 清除本应用所有的数据
	 * 09 - 删除方法(这里只会删除某个文件夹下的文件 如果传入的directory是个文件将不做处理)
	 * 
	 * ####################################################################
	 */
	
    // 01
    public static void cleanInternalCache(Context context) {
        try {
			deleteFilesByDirectory(context.getCacheDir());
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    // 02
    @SuppressLint("SdCardPath")
	public static void cleanDatabases(Context context) {
        try {
        	String pathOfDatabase = "/data/data/" + context.getPackageName() + "/databases";
        	Log.i(TAG, pathOfDatabase);
			deleteFilesByDirectory(new File(pathOfDatabase));
			Log.i(TAG, "本应用Databases已清空");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    // 03
	public static void cleanSharedPreference(Context context) {
        try {
        	mySharedPreferences = context.getSharedPreferences("my_sp_instance", Activity.MODE_PRIVATE);
        	mySharedPreferences.edit().clear().commit(); 
			Log.i(TAG, "SharedPreference已清空");
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    // 04
    public static void cleanDatabaseByName(Context context, String dbName) {
        context.deleteDatabase(dbName);
        Log.i(TAG, "名为：" + dbName + " 的Databases已清空");
    }
    
    // 05
    public static void cleanFiles(Context context) {
        deleteFilesByDirectory(context.getFilesDir());
    }
    
    // 06
    public static void cleanExternalCache(Context context) {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            deleteFilesByDirectory(context.getExternalCacheDir());
        }
    }
    
    // 07
    public static void cleanCustomCache(String filePath) {
        deleteFilesByDirectory(new File(filePath));
    }
    
    // 08
    public static void cleanApplicationData(Context context, String... filepath) {
        cleanInternalCache(context);
        cleanExternalCache(context);
        cleanDatabases(context);
        cleanSharedPreference(context);
        cleanFiles(context);
        for (String filePath : filepath) {
            cleanCustomCache(filePath);
        }
    }
    
    // 09
    private static void deleteFilesByDirectory(File directory) {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            for (File item : directory.listFiles()) {
                item.delete();
            }
        }
    }
}