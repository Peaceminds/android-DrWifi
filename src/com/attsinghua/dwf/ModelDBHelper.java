package com.attsinghua.dwf;

/*
 * 本类重写了DBHelper工具类，以重写的方式定义了打开数据库时初始化表格的操作和数据结构
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ModelDBHelper extends SQLiteOpenHelper {

	/**
	 * ####################################################################
	 * 
	 * 数据库结构定义
	 * 
	 * ####################################################################
	 */
	private static final String TAG = "DBHelper";
	
	private static String createGoodAPInfoTable = 
			"create table goodap_tb(" +
			"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
			"vercode Long," +
			"ssid char(64)," +
			"bssid char(32))";
	private static String createBadAPInfoTable = 
			"create table badap_tb(" +
			"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
			"vercode Long," +
			"ssid char(64)," +
			"bssid char(32))";
	private static String createWifiFPInfoTable = 
			"create table wififp_tb(" +
			"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
			"fptime Long," +
			"lcerr01 boolean," +
			"lcerr02 boolean," +
			"lcerr03 boolean," +
			"lcwar01 boolean," +
			"lcwar02 boolean," +
			"lcwar03 boolean," +
			"lcwar04 boolean," +
			"lcwar05 boolean," +
			"lcwar06 boolean," +
			"lkwfstat int," +
			"lnntid int," +
			"devip int," +
			"devmac char(32)," +
			"lkssid char(32)," +
			"lkbssid char(32)," +
			"lkrssi int," +
			"lkspeed int," +
			"lkishidden boolean," +
			"lkstepinfo char(64)," +
			"lklogs char(64)," +
			"lkfreq int," +
			"devimei char(32)," +
			"devmodel char(32)," +
			"devosver char(32)," +
			"devfirm char(64)," +
			"scapnum int)";
	private static String createAPScanInfoTable = 
			"create table apscan_tb(" +
			"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
			"fkey INTEGER," +
			"sctime Long," +
			"fdevimei char(32)," +
			"devmac char(32)," +
			"lkbssid char(32)," +
			"scssid char(32)," +
			"scbssid char(32)," +
			"sccap char(128)," +
			"sclevel int," +
			"scfreq int)";
	private static String createPingInfoTable = 
			"create table pingd_tb(" +
			"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
			"pgtime Long," +
			"fdevimei char(32)," +
			"devmac char(32)," +
			"lkbssid char(32)," +
			"inpgbytes int," +
			"inpgip char(32)," +
			"inpgicmp int," +
			"inpgttl int," +
			"inpkgnum int," +
			"inpkgrcv int," +
			"inpkglossrt int," +
			"inpgtotaltime int," +
			"inrttmin float," +
			"inrttavg float," +
			"inrttmax float," +
			"inrttmdev float," +
			"otpgbytes int," +
			"otpgip char(32)," +
			"otpgicmp int," +
			"otpgttl int," +
			"otpkgnum int," +
			"otpkgrcv int," +
			"otpkglossrt int," +
			"otpgtotaltime int," +
			"otrttmin float," +
			"otrttavg float," +
			"otrttmax float," +
			"otrttmdev float)";
	private static String createRatingInfoTable = 
			"create table rating_tb(" +
			"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
			"rttime Long," +
			"fdevimei char(32)," +
			"devmac char(32)," +
			"lkbssid char(32)," +
			"rtstars int," +
			"rtreview varchar(1024))";
	private static String createAdditionInfoTable = 
			"create table addition_tb(" +
			"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
			"fkey INTEGER," +
			"sn char(32)," +
			"sa INTEGER," +
			"ss char(32))";

	/**
	 * ####################################################################
	 * 
	 * 重写SQLiteOpenHelper实例化的方法
	 * 
	 * 01 - 必须要有的构造函数
	 * 02 - 当第一次创建数据库的时候，调用该方法
	 * 03 - 当更新数据库的时候执行该方法
	 * 
	 * ####################################################################
	 */
	// 01
	public ModelDBHelper(Context c, String name, CursorFactory factory, int version) {
		super(c, name, factory, version);
	}

	// 02
	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i(TAG, "create GoodAPInfoTable------------->");
		db.execSQL(createGoodAPInfoTable);
		Log.i(TAG, "create BadAPInfoTable------------->");
		db.execSQL(createBadAPInfoTable);
		Log.i(TAG, "create WifiFPInfoTable------------->");
		db.execSQL(createWifiFPInfoTable);
		Log.i(TAG, "create PingInfoTable------------->");
		db.execSQL(createPingInfoTable);
		Log.i(TAG, "create APScanInfoTable------------->");
		db.execSQL(createAPScanInfoTable);
		Log.i(TAG, "create RatingInfoTable------------->");
		db.execSQL(createRatingInfoTable);
		Log.i(TAG, "create AdditionInfoTable------------->");
		db.execSQL(createAdditionInfoTable);
	}

	// 03
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i(TAG, "update Database------------->");
	}

}
