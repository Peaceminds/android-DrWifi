package com.attsinghua.dwf;

/*
 * 本类定义了Wifi指纹信息的数据格式，并实现了数据的获取
 */

import java.util.ArrayList;
import java.util.List;
import android.annotation.SuppressLint;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

@SuppressLint("HandlerLeak")
public class ControlGetWifiFingerPrint {	
	
	private WifiManager wifiMana;
	private int wifiState;
	private String wifiStateString;
	private DhcpInfo wifiDhcpInfo;
	private WifiInfo wifiInfo;
	private String linkSSID;
	private String linkBSSID;
	private String linkStepInfo;
	private String linkLogs;
	private int linkNetWorkID;
	private int linkIPAdd;
	private int linkSpeed;
	private int linkStrength;
	private int linkFrequency;
	private boolean linkIsHidden;
	private List<ScanResult>wifiScanInfoList;
	private List<ScanResult>wifiScanInfoListShort;
	private int APCollisionNum;
	private int APAroundNum;
	private String devMAC;
	private String devIMEI;
	private String devModel;
	private String sftVersion;
	private String devIndustrial;
	private String devManufacture;
	private String devFirmware;
	private boolean haveWifiMana;
	private boolean haveWifiState;
	private boolean haveDHCPInfo;
	private boolean haveWifiInfo;
	private boolean haveScanInfo;
	private boolean haveDeviceInfo;
	private boolean threadMediatorInStructure;
	public static int maxScanResult;
	public final static int THREAD_01_COMPLETE = 1;
	public final static int THREAD_02_COMPLETE = 2;
	public final static int THREAD_03_COMPLETE = 3;
	
	/** 
	 * ####################################################################
	 * 
	 * 全局flag接口
	 * @param false 是可以进行; true 是不能继续
	 * 
	 * ####################################################################
	 */
	public void setThreadMediatorInStructure(boolean threadMediatorInStructure) {
		this.threadMediatorInStructure = threadMediatorInStructure;
	}

	/** 
	 * ####################################################################
	 * 
	 * 主线程
	 * 
	 * M01 - 获取WifiManager 
	 * M02 - 获取WifiState
	 * M03 - 获取WifiState中的字符串信息(DHCP状态字符串)
	 * M04 - 获取设备信息DeviceInfo
	 * 
	 * ####################################################################
	 */
	
	// M01
	public WifiManager getWifiMana(Context c) {
		wifiMana = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
		if (wifiMana == null) {
			haveWifiMana = false;
			threadMediatorInStructure = true;
		} else {
				haveWifiMana = true;
				}
		return wifiMana;
	}
	
	// M02
	public int getWifiState() {
		wifiState = wifiMana.getWifiState();
		return wifiState;
	}
	
	// M03
	public String getWifiStateString() {
		switch (wifiState) {
		case 3:  										// WIFI_STATE_ENABLED
			wifiStateString = "WiFi硬件模块......正常";
			haveWifiState = true;
			break;
		case 1:  										// WIFI_STATE_DISABLED
			wifiStateString = "WiFi硬件模块......关闭";
			haveWifiState = false;
			threadMediatorInStructure = true;
			break;
		case 0:  										// WIFI_STATE_DISABLING
			wifiStateString = "WiFi硬件模块....正关闭";
			haveWifiState = false;
			threadMediatorInStructure = true;
			break;
		case 2:  										// WIFI_STATE_ENABLING
			wifiStateString = "WiFi硬件模块....正开启";
			haveWifiState = false;
			threadMediatorInStructure = true;
			break;
		default:
			wifiStateString = "WiFi硬件模块......未知";
			haveWifiState = false;
			threadMediatorInStructure = true;
			break;
		}
		return wifiStateString;
	}
	
	// M04
	@SuppressWarnings("static-access")
	public void getDeviceInfo(Context c) {
		TelephonyManager tm = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
		Build bd = new Build();
		devIMEI = tm.getDeviceId();
		devIndustrial = bd.DEVICE;
		devManufacture = bd.BRAND;
		devFirmware = bd.HARDWARE;
		devModel = bd.MODEL;
		sftVersion = android.os.Build.VERSION.RELEASE;
		if (devIMEI == null && devModel == null && sftVersion == null) {
			haveDeviceInfo = false;
		} else {
			haveDeviceInfo = true;
		}
	}
	
	// M05
//	public static void setMaxScanResultNum (int num) {
//		maxScanResult = num;
//	}
	
	/** 
	 * ####################################################################
	 * 
	 * 多线程
	 * 
	 * T01 - 本类实例创建后必须执行的方法
	 * *** 调用以下多线程来checkWifiFingerPrint
	 * T02 - DHCP信息
	 * T03 - WifiInfo及其子项参数
	 * T04 - WifiScan结果
	 * 
	 * ####################################################################
	 */
	// T01
	public void checkWifiFingerPrint() {
		if (threadMediatorInStructure == false) {									//  注意，线程必须在这里new，如果作为全局变量new的话，会因为重复调用而报错
			wifiCheckThread1 wifiThread01 = new wifiCheckThread1();
			wifiCheckThread2 wifiThread02 = new wifiCheckThread2();
			wifiCheckThread3 wifiThread03 = new wifiCheckThread3();
			wifiThread01.setDaemon(true);
			wifiThread02.setDaemon(true);
			wifiThread03.setDaemon(true);
			wifiThread01.start();
			wifiThread02.start();
			wifiThread03.start();
		}
	}
	
	// T02
	private class wifiCheckThread1 extends Thread {
		@Override
		public void run() {
			if (threadMediatorInStructure == false) {
				try {
					getWifiDhcpInfo();
					if (wifiDhcpInfo == null) {
						haveDHCPInfo = false;
						threadMediatorInStructure = true;
					} else {
						haveDHCPInfo = true;
					}
				} catch (Exception e) {
					e.printStackTrace();
					haveDHCPInfo = false;
				}
			}
		}
	}
	
	// T03
	private class wifiCheckThread2 extends Thread {
		@Override
		public void run() {
			if (threadMediatorInStructure == false) {
				try {
					getWifiInfo();
					if (wifiInfo == null) {
						haveWifiInfo = false;
					} else {
						haveWifiInfo = true;
					}
					getDevMAC();
					getLinkSSID();
					getLinkIPAdd();
					getLinkBSSID();
					getLinkNetWorkID();
					getLinkStrength();
					getLinkSpeed();
				} catch (Exception e) {
					e.printStackTrace();
					haveWifiInfo = false;
				}
			}
		}
	}
	
	// T04
	private class wifiCheckThread3 extends Thread {
		@Override
		public void run() {
			if (threadMediatorInStructure == false) {
				try {
					getWifiScanInfoList();
					getAPAroundNum();
					// getLinkCollisionAP();														// Android5.0之后可以扫描信道Frequency进而获知冲突AP数量
				} catch (Exception e) {
					e.printStackTrace();
					haveScanInfo = false;
				}
			}
		}
	}
			
	/** 
	 * ####################################################################
	 * 
	 * getters
	 * 
	 * 01 - getWifiDhcpInfo
	 * 02 - getWifiInfo
	 * 03 - getLinkSpeed
	 * 04 - getLinkStrength
	 * 05 - getDevMAC
	 * 06 - getLinkSSID
	 * 07 - getLinkBSSID
	 * 08 - getLinkIPAdd(含有int到IP地址格式转换)
	 * 09 - getLinkNetWorkID
	 * 10 - isLinkIsHidden
	 * 11 - getLinkStepInfo
	 * 12 - getLinkLogs
	 * 13 - getLinkFrequency
	 * 14 - List<ScanResult> getWifiScanInfoList()(注意返回的是List,使用内部对象需要用ScanResult对象遍历)
	 * 15 - getAPAroundNum (遍历上述List以数清数目)
	 * 16 - getAPCollisionNum() (Android5.0以上才能有效果)
	 * 17 - getDevIMEI
	 * 18 - getDevModel
	 * 19 - getSftVersion
	 * 20 - getDevIndustrial
	 * 21 - getDevManufacture
	 * 22 - getDevFirmware
	 * 
	 * ####################################################################
	 */
	// 01
	public DhcpInfo getWifiDhcpInfo() {
		wifiDhcpInfo = wifiMana.getDhcpInfo();
		return wifiDhcpInfo;
	}
	
	// 02
	public WifiInfo getWifiInfo() {
		wifiInfo = wifiMana.getConnectionInfo();
		if (wifiInfo == null) {
			haveWifiInfo = false;
			threadMediatorInStructure = true;
		} else {
			haveWifiInfo = true;
			}
		return wifiInfo;
	}
	
	// 03
	public int getLinkSpeed() {
		linkSpeed = wifiInfo.getLinkSpeed();
		return linkSpeed;
	}
	
	// 04
	public int getLinkStrength() {
		linkStrength = wifiInfo.getRssi();
		return linkStrength;
	}
	
	// 05
	public String getDevMAC(){
		devMAC = wifiInfo.getMacAddress();
		return devMAC;
	}
	
	// 06
	public String getLinkSSID() {
		linkSSID = wifiInfo.getSSID();
		return linkSSID;
	}
	
	// 07
	public String getLinkBSSID() {
		linkBSSID = wifiInfo.getBSSID();
		return linkBSSID;
	}
	
	// 08
	public String getLinkIPAdd() {
		linkIPAdd = wifiInfo.getIpAddress();							// int到String的转换
		StringBuffer sb = new StringBuffer("");							// 直接右移24位  
		sb.append(String.valueOf((linkIPAdd & 0x000000FF)));
		sb.append(".");
		sb.append(String.valueOf((linkIPAdd & 0x0000FFFF) >>> 8));		// 将高8位置0，然后右移16位
		sb.append(".");
		sb.append(String.valueOf((linkIPAdd & 0x00FFFFFF) >>> 16));		// 将高16位置0，然后右移8位
		sb.append(".");
        sb.append(String.valueOf((linkIPAdd >>> 24)));					// 将高24位置0
		return sb.toString();
	}
	
	// 09
	public int getLinkNetWorkID() {
		linkNetWorkID = wifiInfo.getNetworkId();
		return linkNetWorkID;
	}
	
	// 10
	public boolean isLinkIsHidden() {
		linkIsHidden = wifiInfo.getHiddenSSID();
		return linkIsHidden;
	}
	
	// 11
	public String getLinkStepInfo() {
		linkStepInfo = wifiInfo.getSupplicantState().name();
		return linkStepInfo;
	}
	
	// 12
	public final String getLinkLogs() {
		SupplicantState sstate = wifiInfo.getSupplicantState();
		linkLogs = WifiInfo.getDetailedStateOf(sstate).toString() ;
		return linkLogs;
	}
	
	// 13
	public int getLinkFrequency() {
		
		for (ScanResult result : wifiScanInfoList) {
			if (result.BSSID.equalsIgnoreCase(wifiInfo.getBSSID())
					&& result.SSID.equalsIgnoreCase(wifiInfo.getSSID()
							.substring(1, wifiInfo.getSSID().length() - 1))) {
				return (result.frequency);
			}
		}
		// linkFrequency = wifiInfo.getFrequency();
		// return linkFrequency;
		
		return -1;
	}
	
	// 14
	public List<ScanResult> getWifiScanInfoList() {
		wifiScanInfoList = wifiMana.getScanResults();
		if (wifiScanInfoList == null) {
			haveScanInfo = false;
			Log.e("ControlGetWifiFingerPrint", "没有APScan信息！！！");
		} else {
			haveScanInfo = true;
			Log.i("ControlGetWifiFingerPrint", "有APScan信息！");
			for (int i = 0; i < wifiScanInfoList.size(); i++) {
				for (int j = i+1; j < wifiScanInfoList.size(); j++) {
					if( wifiScanInfoList.get(i).level < wifiScanInfoList.get(j).level ) {		// ScanResult的list排序 
	                    ScanResult temp = null;  
	                    temp = wifiScanInfoList.get(i);  
	                    wifiScanInfoList.set(i, wifiScanInfoList.get(j));   
	                    wifiScanInfoList.set(j, temp);
	                }  
				}
			}
			// 根据配置文件中限定的上传扫描数量给扫描AP信息赋值
			int realBound = wifiScanInfoList.size();
			if ( realBound <= ModelConfiguration.getLegalUploadAPScanNum() ) {
				maxScanResult = realBound;
			} else {
				maxScanResult = ModelConfiguration.getLegalUploadAPScanNum();
			}
			wifiScanInfoListShort = new ArrayList<ScanResult>();
			for (int i = 0; i < maxScanResult; i++) {											// 限定ScanResult的list数量
				wifiScanInfoListShort.add(i, wifiScanInfoList.get(i));
			}
		}
		Log.i("ControlGetWifiFingerPrint", "当前的wifiscan扫描完成！");
		return wifiScanInfoListShort;
	}
	
	// 15
	public int getAPAroundNum() {
		APAroundNum = 0;
		for (int i = 0; i < wifiScanInfoList.size(); i++) {
			APAroundNum++;
		}
		return APAroundNum;
	}
	
	// 16
	public int getAPCollisionNum() {
		for (int i = 0; i < wifiScanInfoList.size(); i ++) {
			if (wifiScanInfoList.get(i).frequency == linkFrequency) {
				APCollisionNum++;
			}
		}
		return APCollisionNum;
	}
	
	// 17-22
	public String getDevIMEI() {
		return devIMEI;
	}
	public String getDevModel() {
		return devModel;
	}
	public String getSftVersion() {
		return sftVersion;
	}
	public String getDevIndustrial() {
		return devIndustrial;
	}
	public String getDevManufacture() {
		return devManufacture;
	}
	public String getDevFirmware() {
		return devFirmware;
	}
	
	/** 
	 * ##################################
	 * 
	 * is 上述变量获得与否的flag
	 * 
	 * 01 - wifiManager对象
	 * 02 - wifiState对象
	 * 03 - dhcpInfo对象
	 * 04 - wifiInfo对象
	 * 05 - scanResult对象
	 * 06 - deviceInfo对象
	 * 
	 * ##################################
	 */
	// 01
	public boolean isHaveWifiMana() {
		return haveWifiMana;
	}
	
	// 02
	public boolean isHaveWifiState() {
		return haveWifiState;
	}
	
	// 03
	public boolean isHaveDHCPInfo() {
		return haveDHCPInfo;
	}
	
	// 04
	public boolean isHaveWifiInfo() {
		return haveWifiInfo;
	}
	
	// 05
	public boolean isHaveScanInfo() {
		return haveScanInfo;
	}
	
	// 06
	public boolean isHaveDeviceInfo() {
		return haveDeviceInfo;
	}


}
