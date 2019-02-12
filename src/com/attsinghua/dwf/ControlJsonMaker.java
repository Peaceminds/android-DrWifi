package com.attsinghua.dwf;

/*
 * 本类实现Json格式的封装，供InstantCheck等功能模块调用，提高Json封装代码复用性
 */

import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.net.wifi.ScanResult;
import android.util.Log;

public class ControlJsonMaker {
	
	private static final String TAG = "DWFModelJSON";
	
	/**
	 * ####################################################################
	 * 
	 * 错误码封装部分
	 * @param ec 传入本模型类的错误码
	 * 
	 * ####################################################################
	 */
	public JSONObject errCodeToJson(ModelErrCodeData ec) {
		JSONObject ecParam = new JSONObject();
		try {
			Long sampleTime = System.currentTimeMillis();
			ecParam.put("sampleTime", sampleTime);
			ecParam.put("errCode01", ec.isERR01_NO_WIFI_MANAGER());
			ecParam.put("errCode02", ec.isERR02_NO_WIFI_STATE());
			ecParam.put("errCode03", ec.isERR03_NO_DHCP_IP());
			ecParam.put("warCode01", ec.isWAR01_NO_WIFI_INFO());
			ecParam.put("warCode02", ec.isWAR02_NO_WIFI_SCAN());
			ecParam.put("warCode03", ec.isWAR03_PING_INNER());
			ecParam.put("warCode04", ec.isWAR04_PING_OUTER());
			ecParam.put("warCode05", ec.isWAR05_PHONE_INFO());
			ecParam.put("warCode06", ec.isWAR06_SERV_SEND());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ecParam;
	}
	
	/**
	 * ####################################################################
	 * 
	 * WiFi指纹封装部分
	 * 
	 * 01 - 当前AP信息
	 * 02 - 周边AP扫描信息
	 * 03 - 01与02的整体封装
	 * 
	 * ####################################################################
	 */
	public JSONObject fingerPrintToJSON(ControlGetWifiFingerPrint fp) {
		
		// 01
		JSONObject fpBaseParam = new JSONObject();
		try {
			fpBaseParam.put("wifiState", fp.getWifiState());
			fpBaseParam.put("linkNetID", fp.getLinkNetWorkID());
			fpBaseParam.put("phoneIP", fp.getLinkIPAdd());
			fpBaseParam.put("phoneMac", fp.getWifiInfo().getMacAddress());
			fpBaseParam.put("linkSSID", fp.getLinkSSID());
			fpBaseParam.put("linkBSSID", fp.getLinkBSSID());
			fpBaseParam.put("linkRSSI", fp.getLinkStrength());
			fpBaseParam.put("linkSpeed", fp.getLinkSpeed());
			fpBaseParam.put("isHidden", fp.isLinkIsHidden());
			fpBaseParam.put("linkStepInfo", fp.getLinkStepInfo());
			fpBaseParam.put("linkLogs", fp.getLinkLogs());
			fpBaseParam.put("linkFrequency", fp.getLinkFrequency());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		// 02
		JSONArray fpScanArray = new JSONArray();
		List<ScanResult> targetList = fp.getWifiScanInfoList();
		JSONObject fpScanListElement = null;
		for (int i = 0; i < targetList.size(); i++) {
			fpScanListElement = new JSONObject();
			try {
				fpScanListElement.put("scSSID", targetList.get(i).SSID);
				fpScanListElement.put("scBSSID", targetList.get(i).BSSID);
				fpScanListElement.put("scCapabilities", targetList.get(i).capabilities);
				fpScanListElement.put("scLevel", targetList.get(i).level);
				fpScanListElement.put("scFrequency", targetList.get(i).frequency);
				fpScanListElement.put("scDescribe", targetList.get(i).describeContents());
				fpScanArray.put(i, fpScanListElement);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		// 03
		JSONObject fpJSON = new JSONObject();
		try {
			Long sampleTime = System.currentTimeMillis();
			// Date date = new Date(sampleTime);							// 格式化日期打开此注释
			// SimpleDateFormat format = new SimpleDateFormat("");
			fpJSON.put("aroundAP", fpScanArray);
			fpJSON.put("linkAP", fpBaseParam);
			fpJSON.put("sampleTime", sampleTime);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fpJSON;
	}
	
	
	/**
	 * ####################################################################
	 * 
	 * Ping数据封装部分
	 * @param innerPD 传入本模型类的内部ping数据
	 * @param innerPD 传入本模型类的外部ping数据
	 * 
	 * ####################################################################
	 */
	public JSONObject pingDataToJson(ModelPingData innerPD, ModelPingData outerPD) {
		JSONObject pdParam = new JSONObject();
		JSONObject innerPingParam = new JSONObject();
		JSONObject outerPingParam = new JSONObject();
		try {
			Long sampleTime = System.currentTimeMillis();
			innerPingParam.put("innerPingBytes", innerPD.getBytes());
			innerPingParam.put("innerPingDest", innerPD.getDestIP());
			innerPingParam.put("innerIcmpSeq", innerPD.getICMP_SEQ());
			innerPingParam.put("innerPingTtl", innerPD.getTtl());
			innerPingParam.put("innerPingPkgNum", innerPD.getPackageNum());
			innerPingParam.put("innerPingPkgRcv", innerPD.getPackageReceived());
			innerPingParam.put("innerPingPkgLossRt", innerPD.getPackageLossRate());
			innerPingParam.put("innerPingTotalTime", innerPD.getPingTotalTime());
			innerPingParam.put("innerPingRttMin", innerPD.getRttMin());
			innerPingParam.put("innerPingRttMax", innerPD.getRttMax());
			innerPingParam.put("innerPingRttAvg", innerPD.getRttAvg());
			innerPingParam.put("innerPingRttMdev", innerPD.getRttMdev());
			pdParam.put("innerPingAllInfo", innerPingParam);
			outerPingParam.put("outerPingBytes", outerPD.getBytes());
			outerPingParam.put("outerPingDest", outerPD.getDestIP());
			outerPingParam.put("outerIcmpSeq", outerPD.getICMP_SEQ());
			outerPingParam.put("outerPingTtl", outerPD.getTtl());
			outerPingParam.put("outerPingPkgNum", outerPD.getPackageNum());
			outerPingParam.put("outerPingPkgRcv", outerPD.getPackageReceived());
			outerPingParam.put("outerPingPkgLossRt", outerPD.getPackageLossRate());
			outerPingParam.put("outerPingTotalTime", outerPD.getPingTotalTime());
			outerPingParam.put("outerPingRttMin", outerPD.getRttMin());
			outerPingParam.put("outerPingRttMax", outerPD.getRttMax());
			outerPingParam.put("outerPingRttAvg", outerPD.getRttAvg());
			outerPingParam.put("outerPingRttMdev", outerPD.getRttMdev());
			pdParam.put("outerPingAllInfo", outerPingParam);
			pdParam.put("sampleTime", sampleTime);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return pdParam;
	}
	
	
	/**
	 * ####################################################################
	 * 
	 * 评价数据封装部分
	 * @param rs 评价分数
	 * @param rd 评价内容
	 * 
	 * ####################################################################
	 */
	public JSONObject rateDataToJson(float rs, String rd) {
		JSONObject userRateParam = new JSONObject();
		try {
			Long sampleTime = System.currentTimeMillis();
			userRateParam.put("sampleTime", sampleTime);
			userRateParam.put("rateStars", rs);
			userRateParam.put("rateText", rd);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return userRateParam;
	}
	
	/**
	 * ####################################################################
	 * 
	 * 上传时间戳封装部分
	 * 
	 * ####################################################################
	 */
	public JSONObject timestampToJson() {
		JSONObject timestampJo = new JSONObject();
		Long sysTime = System.currentTimeMillis();
		try {
			timestampJo.put("sysTime", sysTime);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return timestampJo;
	}
	
	/**
	 * ####################################################################
	 * 
	 * 关键AP JSON生成部分
	 * @param scRstArray 周围扫描到的关键AP信息，每个都是ScanResult类型
	 * 
	 * ####################################################################
	 */
	public JSONObject keyAPScanToContent(ScanResult[] scRstArray) {
		
		JSONObject keyAroundAPJo = new JSONObject(); 
		JSONArray keyAPScanJoArray = new JSONArray();
		JSONObject keyAPScanJo = new JSONObject();
		try {
			for (int i = 0; i < scRstArray.length; i++) {
				ScanResult scRst = scRstArray[i];
				keyAPScanJo.put("scSSID", scRst.SSID);
				keyAPScanJo.put("scBSSID", scRst.BSSID);
				keyAPScanJo.put("scCapabilities", scRst.capabilities);
				keyAPScanJo.put("scLevel", scRst.level);
				keyAPScanJo.put("scFrequency", scRst.frequency);
				keyAPScanJo.put("scDescribe", scRst.describeContents());
				keyAPScanJoArray.put(i, keyAPScanJo);
			}
			keyAroundAPJo.put("keyAroundAP", keyAPScanJoArray);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return keyAroundAPJo;
	}
	
	/**
	 * ####################################################################
	 * 
	 * 默认开机上传FP的封装
	 * @param fp 初始化上传的数据 最全的fp
	 * 
	 * ####################################################################
	 */
	/*
	 * 此处需要将Type=0的数据按下面的封装样式封装起来，注意：
	 * 1.相对Type=0来讲改变了一些格式
	 * 2.不要使用DB的方法
	 * 3.要封装设备信息（外部传参进来）
		
		JSONObject linkJo = new JSONObject();
		JSONObject devInfoJo = new JSONObject();
		JSONObject scanJo = new JSONObject();
		JSONArray scanArray = new JSONArray();
		JSONObject allJo = new JSONObject();
		JSONObject bigAllJo = new JSONObject();
		
		devInfoJo.put("fptime", linkCursor.getString(1));
		devInfoJo.put("devImei", linkCursor.getString(23));
		devInfoJo.put("devmodel", linkCursor.getString(24));
		devInfoJo.put("devOsVer", linkCursor.getString(25));
		devInfoJo.put("devFirm", linkCursor.getString(26));
		devInfoJo.put("scAPNum", linkCursor.getString(27));
				
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

		scanJo = new JSONObject();
		scanJo.put("scSSID", scanCursor.getString(6));
		scanJo.put("scBSSID", scanCursor.getString(7));
		scanJo.put("scCapabilities", scanCursor.getString(8));
		scanJo.put("scLevel", scanCursor.getString(9));
		scanJo.put("scFrequency", scanCursor.getString(10));
		scanJo.put("scDescribe", 0);
		scanArray.put(countInt, scanJo);


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
	 */
	public JSONObject initFingerPrintDataToJson(ControlGetWifiFingerPrint fp){
		
		JSONObject linkJo = new JSONObject();
		JSONObject devInfoJo = new JSONObject();
		JSONObject fpJo = new JSONObject();
		JSONObject bigJo = new JSONObject();
		Long sampleTime = System.currentTimeMillis();
		
		try {
			devInfoJo.put("fptime", sampleTime);
			devInfoJo.put("devImei", fp.getDevIMEI());
			devInfoJo.put("devmodel", fp.getDevModel());
			devInfoJo.put("devOsVer", fp.getSftVersion());
			devInfoJo.put("devFirm", fp.getDevFirmware());
			devInfoJo.put("scAPNum", fp.getAPAroundNum());
			
			linkJo.put("wifiState", fp.getWifiState());
			linkJo.put("linkNetID", fp.getLinkNetWorkID());
			linkJo.put("phoneIP", fp.getLinkIPAdd());
			linkJo.put("phoneMac", fp.getDevMAC());
			linkJo.put("linkSSID", fp.getLinkSSID());
			linkJo.put("linkBSSID", fp.getLinkBSSID());
			linkJo.put("linkRSSI", fp.getLinkStrength());
			linkJo.put("linkSpeed", fp.getLinkSpeed());
			linkJo.put("isHidden", fp.isLinkIsHidden());
			linkJo.put("linkStepInfo", fp.getLinkStepInfo());
			linkJo.put("linkLogs", fp.getLinkLogs());
			linkJo.put("linkFrequency", fp.getLinkFrequency());
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		JSONArray fpScanArray = new JSONArray();
		List<ScanResult> targetList = fp.getWifiScanInfoList();
		JSONObject fpScanListElement = null;
		for (int i = 0; i < targetList.size(); i++) {
			fpScanListElement = new JSONObject();
			try {
				fpScanListElement.put("scSSID", targetList.get(i).SSID);
				fpScanListElement.put("scBSSID", targetList.get(i).BSSID);
				fpScanListElement.put("scCapabilities", targetList.get(i).capabilities);
				fpScanListElement.put("scLevel", targetList.get(i).level);
				fpScanListElement.put("scFrequency", targetList.get(i).frequency);
				fpScanListElement.put("scDescribe", targetList.get(i).describeContents());
				fpScanArray.put(i, fpScanListElement);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		try {
			fpJo.put("aroundAP", fpScanArray);
			fpJo.put("linkAP", linkJo);
			fpJo.put("sampleTime", sampleTime);
			bigJo.put("fpBig01", devInfoJo);
			bigJo.put("fpBig02", fpJo);
			Log.i(TAG, "初始化fp封装：成功");
		} catch (Exception e) {
			e.printStackTrace();
			Log.i(TAG, "初始化fp封装：未知错误！");
		}
		
		return bigJo;
	}
}
