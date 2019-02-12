package com.attsinghua.dwf;

/*
 * 本定义了WifiFingerPrint的数据结构
 */

import java.util.List;

import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;

public class ModelWifiFingerPrint {
	
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
	private int APCollisionNum;
	private int APAroundNum;
	private String devMAC;
	private String devIMEI;
	private String devModel;
	private String sftVersion;
	private String devIndustrial;
	private String devManufacture;
	private String devFirmware;
	
	private boolean ERR01_NO_WIFI_MANAGER = false;
	private boolean ERR02_NO_WIFI_STATE = false;
	private boolean ERR03_NO_DHCP_IP = false;
	private boolean WAR01_NO_WIFI_INFO = false;
	private boolean WAR02_NO_WIFI_SCAN = false;
	private boolean WAR03_PING_INNER = false;
	private boolean WAR04_PING_OUTER = false;
	private boolean WAR05_PHONE_INFO = false;
	private boolean WAR06_SERV_SEND = false;
	
	public int getWifiState() {
		return wifiState;
	}
	public void setWifiState(int wifiState) {
		this.wifiState = wifiState;
	}
	public String getWifiStateString() {
		return wifiStateString;
	}
	public void setWifiStateString(String wifiStateString) {
		this.wifiStateString = wifiStateString;
	}
	public DhcpInfo getWifiDhcpInfo() {
		return wifiDhcpInfo;
	}
	public void setWifiDhcpInfo(DhcpInfo wifiDhcpInfo) {
		this.wifiDhcpInfo = wifiDhcpInfo;
	}
	public WifiInfo getWifiInfo() {
		return wifiInfo;
	}
	public void setWifiInfo(WifiInfo wifiInfo) {
		this.wifiInfo = wifiInfo;
	}
	public String getLinkSSID() {
		return linkSSID;
	}
	public void setLinkSSID(String linkSSID) {
		this.linkSSID = linkSSID;
	}
	public String getLinkBSSID() {
		return linkBSSID;
	}
	public void setLinkBSSID(String linkBSSID) {
		this.linkBSSID = linkBSSID;
	}
	public String getLinkStepInfo() {
		return linkStepInfo;
	}
	public void setLinkStepInfo(String linkStepInfo) {
		this.linkStepInfo = linkStepInfo;
	}
	public String getLinkLogs() {
		return linkLogs;
	}
	public void setLinkLogs(String linkLogs) {
		this.linkLogs = linkLogs;
	}
	public int getLinkNetWorkID() {
		return linkNetWorkID;
	}
	public void setLinkNetWorkID(int linkNetWorkID) {
		this.linkNetWorkID = linkNetWorkID;
	}
	public int getLinkIPAdd() {
		return linkIPAdd;
	}
	public void setLinkIPAdd(int linkIPAdd) {
		this.linkIPAdd = linkIPAdd;
	}
	public int getLinkSpeed() {
		return linkSpeed;
	}
	public void setLinkSpeed(int linkSpeed) {
		this.linkSpeed = linkSpeed;
	}
	public int getLinkStrength() {
		return linkStrength;
	}
	public void setLinkStrength(int linkStrength) {
		this.linkStrength = linkStrength;
	}
	public int getLinkFrequency() {
		return linkFrequency;
	}
	public void setLinkFrequency(int linkFrequency) {
		this.linkFrequency = linkFrequency;
	}
	public boolean isLinkIsHidden() {
		return linkIsHidden;
	}
	public void setLinkIsHidden(boolean linkIsHidden) {
		this.linkIsHidden = linkIsHidden;
	}
	public List<ScanResult> getWifiScanInfoList() {
		return wifiScanInfoList;
	}
	public void setWifiScanInfoList(List<ScanResult> wifiScanInfoList) {
		this.wifiScanInfoList = wifiScanInfoList;
	}
	public int getAPCollisionNum() {
		return APCollisionNum;
	}
	public void setAPCollisionNum(int aPCollisionNum) {
		APCollisionNum = aPCollisionNum;
	}
	public int getAPAroundNum() {
		return APAroundNum;
	}
	public void setAPAroundNum(int aPAroundNum) {
		APAroundNum = aPAroundNum;
	}
	public String getDevMAC() {
		return devMAC;
	}
	public void setDevMAC(String devMAC) {
		this.devMAC = devMAC;
	}
	public String getDevIMEI() {
		return devIMEI;
	}
	public void setDevIMEI(String devIMEI) {
		this.devIMEI = devIMEI;
	}
	public String getDevModel() {
		return devModel;
	}
	public void setDevModel(String devModel) {
		this.devModel = devModel;
	}
	public String getSftVersion() {
		return sftVersion;
	}
	public void setSftVersion(String sftVersion) {
		this.sftVersion = sftVersion;
	}
	public String getDevIndustrial() {
		return devIndustrial;
	}
	public void setDevIndustrial(String devIndustrial) {
		this.devIndustrial = devIndustrial;
	}
	public String getDevManufacture() {
		return devManufacture;
	}
	public void setDevManufacture(String devManufacture) {
		this.devManufacture = devManufacture;
	}
	public String getDevFirmware() {
		return devFirmware;
	}
	public void setDevFirmware(String devFirmware) {
		this.devFirmware = devFirmware;
	}
	public boolean isERR01_NO_WIFI_MANAGER() {
		return ERR01_NO_WIFI_MANAGER;
	}
	public void setERR01_NO_WIFI_MANAGER(boolean eRR01_NO_WIFI_MANAGER) {
		ERR01_NO_WIFI_MANAGER = eRR01_NO_WIFI_MANAGER;
	}
	public boolean isERR02_NO_WIFI_STATE() {
		return ERR02_NO_WIFI_STATE;
	}
	public void setERR02_NO_WIFI_STATE(boolean eRR02_NO_WIFI_STATE) {
		ERR02_NO_WIFI_STATE = eRR02_NO_WIFI_STATE;
	}
	public boolean isERR03_NO_DHCP_IP() {
		return ERR03_NO_DHCP_IP;
	}
	public void setERR03_NO_DHCP_IP(boolean eRR03_NO_DHCP_IP) {
		ERR03_NO_DHCP_IP = eRR03_NO_DHCP_IP;
	}
	public boolean isWAR01_NO_WIFI_INFO() {
		return WAR01_NO_WIFI_INFO;
	}
	public void setWAR01_NO_WIFI_INFO(boolean wAR01_NO_WIFI_INFO) {
		WAR01_NO_WIFI_INFO = wAR01_NO_WIFI_INFO;
	}
	public boolean isWAR02_NO_WIFI_SCAN() {
		return WAR02_NO_WIFI_SCAN;
	}
	public void setWAR02_NO_WIFI_SCAN(boolean wAR02_NO_WIFI_SCAN) {
		WAR02_NO_WIFI_SCAN = wAR02_NO_WIFI_SCAN;
	}
	public boolean isWAR03_PING_INNER() {
		return WAR03_PING_INNER;
	}
	public void setWAR03_PING_INNER(boolean wAR03_PING_INNER) {
		WAR03_PING_INNER = wAR03_PING_INNER;
	}
	public boolean isWAR04_PING_OUTER() {
		return WAR04_PING_OUTER;
	}
	public void setWAR04_PING_OUTER(boolean wAR04_PING_OUTER) {
		WAR04_PING_OUTER = wAR04_PING_OUTER;
	}
	public boolean isWAR05_PHONE_INFO() {
		return WAR05_PHONE_INFO;
	}
	public void setWAR05_PHONE_INFO(boolean wAR05_PHONE_INFO) {
		WAR05_PHONE_INFO = wAR05_PHONE_INFO;
	}
	public boolean isWAR06_SERV_SEND() {
		return WAR06_SERV_SEND;
	}
	public void setWAR06_SERV_SEND(boolean wAR06_SERV_SEND) {
		WAR06_SERV_SEND = wAR06_SERV_SEND;
	}
	
}
