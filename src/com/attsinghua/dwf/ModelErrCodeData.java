package com.attsinghua.dwf;

/*
 * 本类定义错误码、警告码的数据结构
 */

public class ModelErrCodeData {
	
	/**
	 * ####################################################################
	 * 
	 * 错误码与警告码定义（注意:错误码之间是互斥事件,所以只需要一个时间戳）
	 * 
	 * 01 - ErrorCode错误码
	 * 02 - WarningCode警告码
	 * 
	 * ####################################################################
	 */
	//01
	private boolean ERR01_NO_WIFI_MANAGER = false;
	private boolean ERR02_NO_WIFI_STATE = false;
	private boolean ERR03_NO_DHCP_IP = false;
	//02
	private boolean WAR01_NO_WIFI_INFO = false;
	private boolean WAR02_NO_WIFI_SCAN = false;
	private boolean WAR03_PING_INNER = false;
	private boolean WAR04_PING_OUTER = false;
	private boolean WAR05_PHONE_INFO = false;
	private boolean WAR06_SERV_SEND = false;
	
	
	/**
	 * ####################################################################
	 * 
	 * 错误码与警告码的getter与setter
	 * 
	 * ####################################################################
	 */
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
