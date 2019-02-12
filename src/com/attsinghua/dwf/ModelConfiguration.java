package com.attsinghua.dwf;

/*
 * 本类主要定义了一些App的基本配置 并实现了从服务器端抓取相关配置内容
 * 
 * legalUploadAPScanNum  1. 允许上传的最大扫描AP数量
 *       legalCheckTime  2. 允许的检测发起时间间隔
 *       legalAPSSIDSet  3. 允许的AP SSID 字符串集
 *       
 */

import java.util.ArrayList;
import java.util.List;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ModelConfiguration {
	
	private static final String TAG = "ModelConfiguration";
	private static int legalUploadAPScanNum;
	private static Long legalCheckTime;
	private static String legalAPSSIDSet;
	private static SharedPreferences mySharedPreferences;
	private static JSONObject queryConfJo;
	private static JSONObject receivedConfDataJo;
	private static boolean queryFlag;
	private static boolean refreshFlag;
	public static String PUSH_MSG_TYPE_CHECK_RESULT;
	public static String PUSH_MSG_TYPE_NEAR_WARNING;
	public static String PUSH_MSG_TYPE_AP_RECOMMEND;
	public static String PUSH_MSG_TYPE_INFO;
	
	/** 
	 * ####################################################################
	 * 
	 * M01 - 构造方法
	 * M02 - 默认配置数据生成
	 * M03 - 从服务器端get配置数据（失败时使用M02中的默认值）
	 * M04 - 刷新数据 将配置文件写入SP中
	 * M05 - 
	 * 
	 * ####################################################################
	 */
	
	// M01
	public ModelConfiguration (Context c) {
		mySharedPreferences = c.getSharedPreferences("local_conf", Activity.MODE_PRIVATE);
		initDefaultConfData();
		requestConf();
	}
	
	// M02
	private static void initDefaultConfData() {
		// 默认手动赋值
		legalUploadAPScanNum = 10;
		legalCheckTime = (long) 60000;
		legalAPSSIDSet = "Tsinghua,Tsinghua-5G,4over6,IVI,Pureland";
		PUSH_MSG_TYPE_CHECK_RESULT = "ap_validate";
		PUSH_MSG_TYPE_NEAR_WARNING = "ap_around_warning";
		PUSH_MSG_TYPE_AP_RECOMMEND = "ap_recommend";
		PUSH_MSG_TYPE_INFO = "thu_broadcast";
		// 以往SP取值（如SP中无值则使用默认手动值）
		legalUploadAPScanNum = mySharedPreferences.getInt("local_conf_legal_scan_num", legalUploadAPScanNum);
		legalCheckTime = mySharedPreferences.getLong("local_conf_legal_check_time", legalCheckTime);
		legalAPSSIDSet = mySharedPreferences.getString("local_conf_legal_ssid_set", legalAPSSIDSet);
		// 封装服务器请求用JSON
		try {
			queryConfJo = new JSONObject().put( "query_conf", "SERVER_CONF" );
		} catch (JSONException e) {
			e.printStackTrace();
		}	
	}
	
	// M03
	private static void requestConf() {
		new Thread() {
			@Override
			public void run() {
				try {
					List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
					params.add( new BasicNameValuePair( "get_conf", queryConfJo.toString()) );
					Bundle httpRes = ControlHttpsUtil.HttpsPost( "https://目标url", params );
					String httpResState = httpRes.getString("res");
					String httpResData =  httpRes.getString("data");
					if ( httpResState.equals("ok") ) {
						Log.i( TAG, "配置文件返回内容为：" + httpResData.toString() );
						setQueryFlag(true);
						receivedConfDataJo = new JSONObject(httpResData);
						// 成功获取服务器信息后的赋值
						setLegalUploadAPScanNum(receivedConfDataJo.getInt("local_conf_legal_scan_num"));
						legalAPSSIDSet = receivedConfDataJo.getString("local_conf_legal_ssid_set");
						setLegalCheckTime(receivedConfDataJo.getLong("local_conf_legal_check_time"));
						myHandler.sendEmptyMessage(0);
					} else {
						setQueryFlag(false);
						// 未成功获取时仍为原值 下同
						myHandler.sendEmptyMessage(1);
					}
				} catch (Exception e) {
					e.printStackTrace();
					setQueryFlag(false);
					myHandler.sendEmptyMessage(2);
				}
			}
		}.start();
	}
	
	// M04
	private static void refreshConf() {
		SharedPreferences.Editor editor = mySharedPreferences.edit();
		editor.putInt("local_conf_legal_scan_num", legalUploadAPScanNum);
		editor.putString("local_conf_legal_ssid_set", legalAPSSIDSet);
		editor.putLong("local_conf_legal_check_time", legalCheckTime);
		editor.commit();
		setRefreshFlag(true);
		Log.i(TAG, "配置文件写入完成！");
	}
	
	/** 
	 * ####################################################################
	 * 
	 * T00 - Handler 多线程获取后进行SP存储调度
	 * 
	 * ####################################################################
	 */
	// T00
	@SuppressLint("HandlerLeak")
	private static Handler myHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 0 :
					Log.i( TAG, "从服务器获取配置文件：成功！内容为 >>> " + receivedConfDataJo.toString() );
					refreshConf();
				break;
				case 1:
					Log.w(TAG, "从服务器获取配置文件：未得到正确返回！");
					refreshConf();
				break;
				case 2:
					Log.e(TAG, "从服务器获取配置文件：出错！");
				break;
			}
		}
	};
	
	/** 
	 * ####################################################################
	 * 
	 * getter & setter
	 * 
	 * ####################################################################
	 */
	public static boolean isQueryFlag() {
		return queryFlag;
	}

	public static void setQueryFlag(boolean queryFlag) {
		ModelConfiguration.queryFlag = queryFlag;
	}

	public static boolean isRefreshFlag() {
		return refreshFlag;
	}

	public static void setRefreshFlag(boolean refreshFlag) {
		ModelConfiguration.refreshFlag = refreshFlag;
	}

	public static int getLegalUploadAPScanNum() {
		return legalUploadAPScanNum;
	}

	public static void setLegalUploadAPScanNum(int legalUploadAPScanNum) {
		ModelConfiguration.legalUploadAPScanNum = legalUploadAPScanNum;
	}

	public static Long getLegalCheckTime() {
		return legalCheckTime;
	}

	public static void setLegalCheckTime(Long legalCheckTime) {
		ModelConfiguration.legalCheckTime = legalCheckTime;
	}
	
	/*
	 * 外部接口
	 */
	
	public static boolean getIsCurrentSSIDLegal(String currentSSID) {
		boolean tempBoolean = false;
		String tempStrArray[];
		String currentStrArray[];
		tempStrArray = legalAPSSIDSet.split(",");
		if (currentSSID.contains("\"")) {
			Log.w(TAG, "削去了比较SSID的引号");
			currentStrArray = currentSSID.split("\"");
			currentSSID = currentStrArray[1];
		} 
		for (int i = 0; i < tempStrArray.length; i++) {
			if ( tempStrArray[i].equals(currentSSID) ) {
				tempBoolean = true;
				Log.i(TAG, "是合法的SSID");
				break;
			} else {
				tempBoolean = false;
			}
		}
		return tempBoolean;
	}
	

}
