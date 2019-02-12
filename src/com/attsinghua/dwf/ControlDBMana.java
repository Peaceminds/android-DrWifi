package com.attsinghua.dwf;

/*
 * 本类实现了针对DBHelper数据库的增删改查操作
 */

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.ScanResult;
import android.util.Log;

public class ControlDBMana {

	private static final String TAG = "ControlDBMana";
	private static ModelDBHelper dbHelper = null;
	private static SQLiteDatabase userDBInstance = null;
	private static SQLiteDatabase userDBInstance2 = null;
	private Long insertedWFPID;
	private int historyIndex;
	
	/**
	 * ####################################################################
	 * 
	 * 初始化
	 * 
	 * 01 - 创建DBHelper实例(创建数据库的实例方法在主界面中完成)
	 * 02 - 创建NewGoodAPTable
	 * 03 - 创建NewBadAPTable
	 * 
	 * ####################################################################
	 */
	// 01
	public ModelDBHelper createUserDBbyHelper(Context c, String dbName) {
		if (dbHelper == null)
			dbHelper = new ModelDBHelper(c, dbName, null, 1);
		userDBInstance = dbHelper.getReadableDatabase();
		userDBInstance2 = dbHelper.getReadableDatabase();
		Log.i(TAG, "已完成数据库实例化创建/连接，当前数据库名为：" + dbName);
		return dbHelper;
	}
	
	// 02
	public void createNewGoodAPDataToDB() {
		userDBInstance.execSQL(
				"create table goodap_tb(" +
				"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
				"vercode Long," +
				"ssid char(64)," +
				"bssid char(32))");
		Log.i(TAG, "创建了新版本的GoodAPInfoTable===>>>");
	}
	
	// 03
	public void createNewBadAPDataToDB() {
		userDBInstance.execSQL(
				"create table badap_tb(" +
				"_id INTEGER PRIMARY KEY AUTOINCREMENT," +
				"vercode Long," +
				"ssid char(64)," +
				"bssid char(32))");
		Log.i(TAG, "创建了新版本的BadAPInfoTable===>>>");
	}
	
	/**
	 * ####################################################################
	 * 
	 * 增
	 * 
	 * 01 - 初始化时默认写apk携带的数据库(调试方法, 目前只作为初始化数据库时最基本数据导入用)
	 * 021 - 插入NewGoodAPTable数据
	 * 022 - 插入NewBadAPTable数据
	 * 03 - 插入WifiFingerPrint数据
	 * 04 - 插入APScan数据
	 * 05 - 插入Ping数据
	 * 06 - 插入Rating数据
	 * 
	 * ####################################################################
	 */
	// 01
	public void initInsertGoodAPDataToDB() {
		ContentValues cValue = new ContentValues();
		userDBInstance.beginTransaction();
		cValue.put("vercode", 7);
		cValue.put("ssid", "Tsinghua");
		cValue.put("bssid", "44:e4:d9:41:25:70");
		cValue.put("bssid", "88:1f:a1:3a:9a:a6");
		cValue.put("bssid", "10:6f:3f:e7:3b:3e");
		userDBInstance.insert("goodap_tb", null, cValue);
		Log.i(TAG, "程序初始化，GoodAPInfoTable 样板数据创建===>>>");
	}
	
	// 021
	public boolean insertNewGoodAPDataToDB(List<ModelGoodBadAPData> goodAPDatas) {
		ContentValues cValue = new ContentValues();
		userDBInstance.beginTransaction();
		for (int i = 0; i < goodAPDatas.size(); i++) {
			ModelGoodBadAPData goodAPData = goodAPDatas.get(i);
			cValue.put("vercode", goodAPData.getTimeStampVersion());
			cValue.put("bssid", goodAPData.getGdBSSID());
			userDBInstance.insert("goodap_tb", null, cValue);
		}
		userDBInstance.setTransactionSuccessful();
		userDBInstance.endTransaction();
		Log.i(TAG, "GoodAP 信息插入完毕!!!");
		return true;
	}
	
	// 022
	public boolean insertNewBadAPDataToDB(List<ModelGoodBadAPData> badAPDatas) {
		ContentValues cValue = new ContentValues();
		userDBInstance2.beginTransaction();
		for (int i = 0; i < badAPDatas.size(); i++) {
			ModelGoodBadAPData badAPData = badAPDatas.get(i);
			cValue.put("vercode", badAPData.getTimeStampVersion());
			cValue.put("bssid", badAPData.getGdBSSID());
			userDBInstance2.insert("badap_tb", null, cValue);
		}
		userDBInstance2.setTransactionSuccessful();
		userDBInstance2.endTransaction();
		Log.i(TAG, "BadAP 信息插入完毕!!!");
		return true;
	}

	// 03
	public void insertWifiFingerPrintToDB(final ModelErrCodeData ecd, final ControlGetWifiFingerPrint wfp) {
		userDBInstance = dbHelper.getReadableDatabase();
		ContentValues cValue = new ContentValues();
		long tp = System.currentTimeMillis();
		cValue.put("fptime", tp);
		
		if (!ecd.equals(null)) {
			cValue.put("lcerr01", ecd.isERR01_NO_WIFI_MANAGER());
			cValue.put("lcerr02", ecd.isERR02_NO_WIFI_STATE());
			cValue.put("lcerr03", ecd.isERR03_NO_DHCP_IP());
			cValue.put("lcwar01", ecd.isWAR01_NO_WIFI_INFO());
			cValue.put("lcwar02", ecd.isWAR02_NO_WIFI_SCAN());
			cValue.put("lcwar03", ecd.isWAR03_PING_INNER());
			cValue.put("lcwar04", ecd.isWAR04_PING_OUTER());
			cValue.put("lcwar05", ecd.isWAR05_PHONE_INFO());
			cValue.put("lcwar06", ecd.isWAR06_SERV_SEND());
		}
		
		cValue.put("lkwfstat", wfp.getWifiState());
		cValue.put("lnntid", wfp.getLinkNetWorkID());
		cValue.put("devip", wfp.getLinkIPAdd());
		cValue.put("devmac", wfp.getDevMAC());
		cValue.put("lkssid", wfp.getLinkSSID());
		cValue.put("lkbssid", wfp.getLinkBSSID());
		cValue.put("lkrssi", wfp.getLinkStrength());
		cValue.put("lkspeed", wfp.getLinkSpeed());
		cValue.put("lkishidden", wfp.isLinkIsHidden());
		cValue.put("lkstepinfo", wfp.isLinkIsHidden());
		cValue.put("lklogs", wfp.isLinkIsHidden());
		cValue.put("lkfreq", "-1");												// Android 5.0 才能使用的方法
		cValue.put("devimei", wfp.getDevIMEI());
		cValue.put("devmodel", wfp.getDevModel());
		cValue.put("devosver", wfp.getSftVersion());
		cValue.put("devfirm", wfp.getDevFirmware());
		cValue.put("scapnum", wfp.getAPAroundNum());
		cValue.put("lkfreq", wfp.getLinkFrequency());
		insertedWFPID = userDBInstance.insert("wififp_tb", null, cValue);
		Log.i(TAG, "指纹插入完毕!!!,已插入第" + insertedWFPID + "行");
	}

	// 04
	public void insertAPScanInfoToDB(final ControlGetWifiFingerPrint wfp) {		// 这里的插入是紧接着FP信息之后的, 所以外键直接用FP插入的行ID就行(已定义为全局变量)
		ContentValues cValue = new ContentValues();
		long tp = System.currentTimeMillis();									// 插入系统的时间戳
		List<ScanResult> scrList = wfp.getWifiScanInfoList();
		for (int i = 0; i < scrList.size(); i++) {
			cValue.put("fkey", insertedWFPID);
			cValue.put("sctime", tp);
			cValue.put("fdevimei", wfp.getDevIMEI());
			cValue.put("lkbssid", wfp.getLinkSSID());
			cValue.put("scssid", scrList.get(i).SSID);
			cValue.put("scbssid", scrList.get(i).BSSID);
			cValue.put("sccap", scrList.get(i).capabilities);
			cValue.put("sclevel", scrList.get(i).level);
			cValue.put("scfreq", scrList.get(i).frequency);
			userDBInstance.insert("apscan_tb", null, cValue);
		}
		Log.i(TAG, "扫描AP信息插入完毕!!!");
	}

	// 05
	public void insertPingDetailToDB(final ControlGetWifiFingerPrint wfp, final ModelPingData inpd, final ModelPingData otpd) {
		userDBInstance = dbHelper.getReadableDatabase();
		ContentValues cValue = new ContentValues();
		long tp = System.currentTimeMillis();
		cValue.put("pgtime", tp);
		cValue.put("fdevimei", wfp.getDevIMEI());
		cValue.put("lkbssid", wfp.getLinkSSID());
		cValue.put("inpgbytes", inpd.getBytes());
		cValue.put("inpgip", inpd.getDestIP());
		cValue.put("inpgicmp", inpd.getICMP_SEQ());
		cValue.put("inpgttl", inpd.getTtl());
		cValue.put("inpkgnum", inpd.getPackageNum());
		cValue.put("inpkgrcv", inpd.getPackageReceived());
		cValue.put("inpkglossrt", inpd.getPackageLossRate());
		cValue.put("inpgtotaltime", inpd.getPingTotalTime());
		cValue.put("inrttmin", inpd.getRttMin());
		cValue.put("inrttavg", inpd.getRttAvg());
		cValue.put("inrttmax", inpd.getRttMax());
		cValue.put("inrttmdev", inpd.getRttMdev());
		cValue.put("otpgbytes", otpd.getBytes());
		cValue.put("otpgip", otpd.getDestIP());
		cValue.put("otpgicmp", otpd.getICMP_SEQ());
		cValue.put("otpgttl", otpd.getTtl());
		cValue.put("otpkgnum", otpd.getPackageNum());
		cValue.put("otpkgrcv", otpd.getPackageReceived());
		cValue.put("otpkglossrt", otpd.getPackageLossRate());
		cValue.put("otpgtotaltime", otpd.getPingTotalTime());
		cValue.put("otrttmin", otpd.getRttMin());
		cValue.put("otrttavg", otpd.getRttAvg());
		cValue.put("otrttmax", otpd.getRttMax());
		cValue.put("otrttmdev", otpd.getRttMdev());
		userDBInstance.insert("pingd_tb", null, cValue);
		Log.i(TAG, "Ping信息插入完毕!!!");
	}

	// 06
	public void insertRatingToDB(ControlGetWifiFingerPrint fp, int ratingStars, String ratingContent) {
		userDBInstance = dbHelper.getReadableDatabase();
		ContentValues cValue = new ContentValues();
		long tp = System.currentTimeMillis();
		cValue.put("rttime", tp);
		cValue.put("fdevimei", fp.getDevIMEI());
		cValue.put("lkbssid", fp.getLinkBSSID());
		cValue.put("rtstars", ratingStars);
		cValue.put("rtreview", ratingContent);
		userDBInstance.insert("rating_tb", null, cValue);
		Log.i(TAG, "Rating信息插入完毕!!!");
	}

	/**
	 * ####################################################################
	 * 
	 * 查 
	 * 
	 * 11 - 查询GoodAP表版本
	 * 12 - 查询BadAP表版本
	 * 21 - 查询比对GoodAP表记录
	 * 22 - 查询比对BadAP表记录
	 * 03 - 查询WifiFingerPrint数据
	 * 04 - 查询APScan数据
	 * 05 - 查询Ping数据
	 * 06 - 查询Rating数据
	 * 
	 * ####################################################################
	 */
	// 11
	public int queryIfGoodAPTableVersionOK(Long serverDBVer) {
		Log.i(TAG, "查询GoodAP表版本信息===>>>");
		int whatsTheVer;
		Cursor cursor = userDBInstance.rawQuery("Select * from goodap_tb where vercode=?", new String[] { String.valueOf(serverDBVer) });
		if (cursor.moveToLast()) {
			whatsTheVer = 1;														// goodap_tb版本OK
			Log.i(TAG, "GoodAP表已是最新版本！");
		} else {
			whatsTheVer = 0;														// goodap_tb需要更新
			Log.i(TAG, "GoodAP表需要更新啦！");
		}
		cursor.close();
		return whatsTheVer;
	}
	
	// 12
	/**
	 * @param serverDBVer
	 * @return 0找到匹配的 1没找到匹配的  2取值错误   默认是1不匹配
	 */
	public int queryIfBadAPTableVersionOK(Long serverDBVer) {
		Log.i(TAG, "查询BadAP表版本信息===>>>");
		int whatsTheVer;
		Cursor cursor = userDBInstance.rawQuery("Select * from badap_tb where vercode=?", new String[] { String.valueOf(serverDBVer) });
		if (cursor.moveToLast()) {
			whatsTheVer = 1;														// badap_tb版本OK
			Log.i(TAG, "BadAP表已是最新版本！");
		} else {
			whatsTheVer = 0;														// badap_tb需要更新
			Log.i(TAG, "BadAP表需要更新啦！");
		}
		cursor.close();
		return whatsTheVer;
	}
	
	// 21
	/**
	 * @param wfp
	 * @return 0找到匹配的 1没找到匹配的  2取值错误   默认是1不匹配
	 */
	public int queryIfGoodAP(ControlGetWifiFingerPrint wfp) {
		Log.i(TAG, "正在用本地白名单验证AP真伪===>>>");
		int whatsTheAP = 3;						
		// String linkSSID = wfp.getLinkSSID().replaceAll("\"", "");				// 如果需要比较SSID的时候 可以把这个打开去掉本机取MAC的引号
		String linkBSSID = wfp.getLinkBSSID();
		ArrayList<String> bList = new ArrayList<String>();
		
		Cursor cursor = userDBInstance.rawQuery("Select bssid from goodap_tb", null);
		while (cursor.moveToNext()) {
			bList.add(cursor.getString(0));
		}
		cursor.close();
		
		if (bList.isEmpty()) {
			whatsTheAP = 2;
			Log.i(TAG, "取good_ap库时出错！");
		} else {
			String cmpString = linkBSSID.substring(0, linkBSSID.lastIndexOf(":"));	// 字符串处理 思科AP的MAC地址前5组都一致 只有最后一组不同，因此可以只比对BSSID前11位！
			for (String bssidString : bList) {										// compareToIgnoreCase 正确状态返回0
				if ( !((bssidString.substring(0, bssidString.lastIndexOf(":"))).compareToIgnoreCase(cmpString) == 0) ) {
					whatsTheAP = 1;
				}
				else {
					System.out.println("Origin:" + cmpString + ", " + "CMP:" + bssidString);
					Log.i(TAG, "匹配白名单库内记录！");
					whatsTheAP = 0;
					break;
				}
			}
		}
		return whatsTheAP;
	}
	
	// 22
	/**
	 * @param wfp
	 * @return 0找到匹配的 1没找到匹配的  2取值错误   默认是1不匹配
	 */
	public int queryIfBadAP(ControlGetWifiFingerPrint wfp) {
		Log.i(TAG, "正在用本地黑名单验证AP真伪===>>>");
		
		int whatsTheAP = 1;						
		// String linkSSID = wfp.getLinkSSID().replaceAll("\"", "");				// 如果需要比较SSID的时候 可以把这个打开去掉本机取MAC的引号
		String linkBSSID = wfp.getLinkBSSID();
		ArrayList<String> bList = new ArrayList<String>();
		
		Cursor cursor = userDBInstance.rawQuery("Select bssid from badap_tb", null);
		while (cursor.moveToNext()) {
			bList.add(cursor.getString(0));
		}
		cursor.close();
		
		if (bList.isEmpty()) {
			whatsTheAP = 2;
			Log.i(TAG, "取bad_ap库时出错！");
		} else {
			Log.i(TAG, "取bad_ap库正常，正在比较");
			String cmpString = linkBSSID.substring(0, linkBSSID.lastIndexOf(":"));	// 字符串处理 思科AP的MAC地址前5组都一致 只有最后一组不同，因此可以只比对BSSID前11位！
			for (String bssidString : bList) {										// compareToIgnoreCase 正确状态返回0
				if ((bssidString.substring(0, bssidString.lastIndexOf(":"))).compareToIgnoreCase(cmpString) == 0) {
					System.out.println("当前MAC:" + cmpString + ", " + "比较库中的MAC:" + bssidString);
					Log.i(TAG, "匹配库内记录！");
					whatsTheAP = 0;
					break;
				} else {
					whatsTheAP = 1;	
				}
			}
		}
		return whatsTheAP;
	}

	// 03
	public void queryWifiFingerPrintData() {
		Log.i(TAG, "正在输出WifiFingerPrint表内信息===>>>");
		
		userDBInstance = dbHelper.getReadableDatabase();
		Cursor cursor = userDBInstance.rawQuery("Select * from wififp_tb", null);
		while (cursor.moveToNext()) {
			System.out.println("wffp " + cursor.getString(0));
			System.out.println("wffp " + cursor.getString(1));
			System.out.println("wffp " + cursor.getString(2));
			System.out.println("wffp " + cursor.getString(3));
			System.out.println("wffp " + cursor.getString(4));
			System.out.println("wffp " + cursor.getString(5));
			System.out.println("wffp " + cursor.getString(6));
			System.out.println("wffp " + cursor.getString(7));
			System.out.println("wffp " + cursor.getString(8));
			System.out.println("wffp " + cursor.getString(9));
			System.out.println("wffp " + cursor.getString(10));
			System.out.println("wffp " + cursor.getString(11));
			System.out.println("wffp " + cursor.getString(12));
			System.out.println("wffp " + cursor.getString(13));
			System.out.println("wffp " + cursor.getString(14));
			System.out.println("wffp " + cursor.getString(15));
			System.out.println("wffp " + cursor.getString(16));
			System.out.println("wffp " + cursor.getString(17));
			System.out.println("wffp " + cursor.getString(18));
			System.out.println("wffp " + cursor.getString(19));
			System.out.println("wffp " + cursor.getString(20));
			System.out.println("wffp " + cursor.getString(21));
			System.out.println("wffp " + cursor.getString(22));
			System.out.println("wffp " + cursor.getString(23));
			System.out.println("wffp " + cursor.getString(24));
			System.out.println("wffp " + cursor.getString(25));
			System.out.println("wffp " + cursor.getString(26));
			System.out.println("wffp " + cursor.getString(27));
		}
		cursor.close();
	}
	
	// 03-2
	public JSONObject queryHistoryWifiFingerPrintData(int queryTimes) {
		Log.i(TAG, "正在查询历史中的WifiFingerPrint表内信息===>>>");
		
		JSONObject linkJo = new JSONObject();
		JSONObject ecdDataJo = new JSONObject();
		JSONObject devInfoJo = new JSONObject();
		JSONObject scanJo = new JSONObject();
		JSONArray scanArray = new JSONArray();
		JSONObject allJo = new JSONObject();
		JSONObject bigAllJo = new JSONObject();
		
		userDBInstance = dbHelper.getReadableDatabase();							// 连接AP数据读取
		String sql = "select * from wififp_tb order by _id desc limit " + queryTimes;
		Cursor linkCursor = userDBInstance.rawQuery(sql, null);
		if (linkCursor.moveToLast()) {
			try {
				historyIndex = linkCursor.getInt(0);
				// 历史数据相比即时数据多了两部分增量，分别为设备信息和错误码
				devInfoJo.put("fptime", linkCursor.getString(1));
				devInfoJo.put("devImei", linkCursor.getString(23));
				devInfoJo.put("devmodel", linkCursor.getString(24));
				devInfoJo.put("devOsVer", linkCursor.getString(25));
				devInfoJo.put("devFirm", linkCursor.getString(26));
				devInfoJo.put("scAPNum", linkCursor.getString(27));
				
				ecdDataJo.put("errCode01", Boolean.getBoolean(linkCursor.getString(2)));
				ecdDataJo.put("errCode02", Boolean.getBoolean(linkCursor.getString(3)));
				ecdDataJo.put("errCode03", Boolean.getBoolean(linkCursor.getString(4)));
				ecdDataJo.put("warCode01", Boolean.getBoolean(linkCursor.getString(5)));
				ecdDataJo.put("warCode02", Boolean.getBoolean(linkCursor.getString(6)));
				ecdDataJo.put("warCode03", Boolean.getBoolean(linkCursor.getString(7)));
				ecdDataJo.put("warCode04", Boolean.getBoolean(linkCursor.getString(8)));
				ecdDataJo.put("warCode05", Boolean.getBoolean(linkCursor.getString(9)));
				ecdDataJo.put("warCode06", Boolean.getBoolean(linkCursor.getString(10)));
				ecdDataJo.put("sampleTime", linkCursor.getString(1));
				
				linkJo.put("wifiState", linkCursor.getString(11));
				linkJo.put("linkNetID", linkCursor.getString(12));
				linkJo.put("phoneIP", linkCursor.getString(13));
				linkJo.put("phoneMac", linkCursor.getString(14));
				linkJo.put("linkSSID", linkCursor.getString(15));
				linkJo.put("linkBSSID", linkCursor.getString(16));
				linkJo.put("linkRSSI", linkCursor.getString(17));
				linkJo.put("linkSpeed", linkCursor.getString(18));
				linkJo.put("isHidden", linkCursor.getString(19));
				linkJo.put("linkStepInfo", linkCursor.getString(20));
				linkJo.put("linkLogs", linkCursor.getString(21));
				linkJo.put("linkFrequency", linkCursor.getString(22));
			} catch (JSONException e) {
				e.printStackTrace();
				Log.i(TAG, "历史link数据读取：未知错误");
			}
		} else {
			Log.i(TAG, "历史link数据读取：为空！");
		}
		linkCursor.close();
		// 根据连接AP数据ID记录，关联fkey读取scan记录
		if (historyIndex > 0) {
			Cursor scanCursor = userDBInstance.rawQuery("select * from apscan_tb where fkey = " + historyIndex, null);
			int countInt = 0;
			while (scanCursor.moveToNext()) {
				try {
					scanJo = new JSONObject();									// 注意这里如果不new的话，会导致每次的数据全都一样
					scanJo.put("scSSID", scanCursor.getString(6));
					scanJo.put("scBSSID", scanCursor.getString(7));
					scanJo.put("scCapabilities", scanCursor.getString(8));
					scanJo.put("scLevel", scanCursor.getString(9));
					scanJo.put("scFrequency", scanCursor.getString(10));
					scanJo.put("scDescribe", 0);
					scanArray.put(countInt, scanJo);
				} catch (JSONException e) {
					Log.i(TAG, "历史scan数据读取：未知错误");
					e.printStackTrace();
				}
				countInt++;
			}
			scanCursor.close();
		} else {
			Log.i(TAG, "历史scan数据读取：行号不正确！");
		}
		
		// 三部分数据封装
		try {
			allJo.put("aroundAP", scanArray);
			allJo.put("linkAP", linkJo);
			allJo.put("sampleTime", ecdDataJo.getString("sampleTime"));
			bigAllJo.put("fpBig01", devInfoJo);
			bigAllJo.put("fpBig02", ecdDataJo);
			bigAllJo.put("fpBig03", allJo);
			Log.i(TAG, "历史link + scan封装：成功，内容为：" + allJo.toString());
		} catch (Exception e) {
			e.printStackTrace();
			Log.i(TAG, "历史link + scan封装：未知错误！");
		}
		
		return bigAllJo;
	}

	// 04
	public void queryAPScanData() {
		Log.i(TAG, "APScan表内信息===>>>");
		
		Cursor cursor = userDBInstance.rawQuery("Select * from apscan_tb", null);
		int rows_num = cursor.getCount();											// 取得資料表列數
		if (rows_num != 0) {
			cursor.moveToFirst();													// 將指標移至第一筆資料
			for (int i = 0; i < rows_num; i++) {
				System.out.println("apsc " + cursor.getString(0));
				System.out.println("apsc " + cursor.getString(1));
				System.out.println("apsc " + cursor.getString(2));
				System.out.println("apsc " + cursor.getString(3));
				System.out.println("apsc " + cursor.getString(4));
				System.out.println("apsc " + cursor.getString(5));
				System.out.println("apsc " + cursor.getString(6));
				System.out.println("apsc " + cursor.getString(7));
				System.out.println("apsc " + cursor.getString(8));
				System.out.println("apsc " + cursor.getString(9));
				cursor.moveToNext();												// 將指標移至下一筆資料
			}
		}
		cursor.close();
	}

	// 05
	public void queryPingData() {
		Log.i(TAG, "PingData表内信息===>>>");
		
		Cursor cursor = userDBInstance.rawQuery("Select * from pingd_tb", null);
		while (cursor.moveToNext()) {
			System.out.println("pgif " + cursor.getString(0));
			System.out.println("pgif " + cursor.getString(1));
			System.out.println("pgif " + cursor.getString(2));
			System.out.println("pgif " + cursor.getString(3));
			System.out.println("pgif " + cursor.getString(4));
			System.out.println("pgif " + cursor.getString(5));
			System.out.println("pgif " + cursor.getString(6));
			System.out.println("pgif " + cursor.getString(7));
			System.out.println("pgif " + cursor.getString(8));
			System.out.println("pgif " + cursor.getString(9));
			System.out.println("pgif " + cursor.getString(10));
			System.out.println("pgif " + cursor.getString(11));
			System.out.println("pgif " + cursor.getString(12));
			System.out.println("pgif " + cursor.getString(13));
			System.out.println("pgif " + cursor.getString(14));
			System.out.println("pgif " + cursor.getString(15));
			System.out.println("pgif " + cursor.getString(16));
			System.out.println("pgif " + cursor.getString(17));
			System.out.println("pgif " + cursor.getString(18));
			System.out.println("pgif " + cursor.getString(19));
			System.out.println("pgif " + cursor.getString(20));
			System.out.println("pgif " + cursor.getString(21));
			System.out.println("pgif " + cursor.getString(22));
			System.out.println("pgif " + cursor.getString(23));
			System.out.println("pgif " + cursor.getString(24));
			System.out.println("pgif " + cursor.getString(25));
			System.out.println("pgif " + cursor.getString(26));
			System.out.println("pgif " + cursor.getString(27));
			System.out.println("pgif " + cursor.getString(28));
		}
		cursor.close();
	}

	// 06
	public void queryRatingData() {
		Log.i(TAG, "RatingData表内信息===>>>");
		Cursor cursor = userDBInstance.rawQuery("Select * from rating_tb", null);
		while (cursor.moveToNext()) {
			System.out.println("rtif " + cursor.getString(0));
			System.out.println("rtif " + cursor.getString(1));
			System.out.println("rtif " + cursor.getString(2));
			System.out.println("rtif " + cursor.getString(3));
			System.out.println("rtif " + cursor.getString(4));
			System.out.println("rtif " + cursor.getString(5));
		}
		cursor.close();
	}

	/**
	 * ####################################################################
	 * 
	 * 删 
	 * 
	 * 01-删除GoodAPTable
	 * 02-删除BadAPTable
	 * 03-删除所有用户表再新建数据库(查出记录扫描大于15000条或用户执行清理)
	 * 
	 * ####################################################################
	 */
	// 01
	public void deleteOldGoodAPTable() {
		userDBInstance.execSQL("DROP TABLE goodap_tb");
		Log.i(TAG, "已删除了旧版本的GoodAPInfoTable===>>>");
	}
	
	// 02
	public void deleteOldBadAPTable() {
		userDBInstance.execSQL("DROP TABLE badap_tb");
		Log.i(TAG, "已删除了旧版本的BadAPInfoTable===>>>");
	}

	// 03
	public void deleteUserRec(String tableName, String whereClause, String[] whereArgs) {
		userDBInstance = dbHelper.getReadableDatabase();
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		userDBInstance.close();
		userDBInstance2.close();
	}
	
	
	
}

