package com.attsinghua.dwf;

/*
 * 本类定义Ping数据结构，并提供了Ping结果信息截取与取值方法(返回值)
 */

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.util.Log;

public class ModelPingData {
	
	private static final String TAG = "ModelPingData";
	private int bytes;
	private String destIPContent;
	private String destIP;
	private int ICMP_SEQ;
	private int ttl;
	private int packageNum;
	private int packageReceived;
	private int packageLossRate;
	private int pingTotalTime;
	private float rttMin;
	private float rttAvg;
	private float rttMax;
	private float rttMdev;
	
	/**
	 * ####################################################################
	 * 
	 * 字符串截取
	 * 
	 * 01 - Ping得到标准格式信息的判断
	 * 	  	true说明第一行返回数字时说明已Ping通
	 * 	  	false则是说明命令已执行但没有Ping通
	 * 02 - 上述判断调用的方法
	 * 03 - 通过字符串截取的方式获取所有Ping输出的信息
	 * 	         并给出各个成员变量赋值用于JSON和SQL
	 * 04 - Ping不通时的成员变量赋值
	 * 	         避免JSON和SQL存空信息出错
	 * 
	 * ####################################################################
	 */
	// 01
	public boolean isPingOk(String sourceString) {
		String asbLine00[] = sourceString.split("\n");
		int index = asbLine00.length;
		String asbLine01[] = asbLine00[index-1].split(" ");
		Log.i(TAG, "Ping全部信息: " + sourceString);
		if (isAllDigit(asbLine01[0])) {
			Log.i(TAG, "Ping结果格式不正确或链路不通！");
			return false;
		} else {
			Log.i(TAG, "Ping结果正确喔~");
			return true;
		}
	}
	
	// 02
	public static boolean isAllDigit(String aString){
		for (int i = 0; i < aString.length(); i++) {
			if (!Character.isDigit(aString.charAt(i)))
				return false;
		}
		return true;
	}
	
	// 03
	/*
	 *  Ping www.baidu.com（域名）输出是 
	 *  64 bytes from www.baidu.com (119.75.217.56): icmp_seq=1 ttl=53 time=22.2 ms
	 *  
	 *  Ping 192.168.0.1（IP）输出是
	 *  64 bytes from 166.111.8.28: icmp_seq=1 ttl=47 time=33.6 ms
	 *  
	 *  所以下面的 ICMP_SEQ TTL的截取就在数组中依次顺延了一位，其他行则无需关心
	 */
	public void getPingInfoStringDetails(String sourceString) {
		try {
			String asbLine00[] = sourceString.split("\n");						// 按每次正常Ping的输出回行进行截取,只关心结果各！个！行！
			String asbLine10[] = asbLine00[0].split(" ");						
			
			// 调试用，有些时候Console输出的信息不一致，对此进行了长短适配
//			System.out.println("看一下Ping后的字符串 >>> ");
//			System.out.println(asbLine00[0]);
//			System.out.println(asbLine00[1]);
//			System.out.println(asbLine10[0]);
//			System.out.println(asbLine10[1]);
			
			if (asbLine00[0].contains("-")) {
				Log.d(TAG, "此次Ping采用了短格式");
				destIP = asbLine10[1];
				
				System.out.println("此次截取到的/域名 >>> ");
				System.out.println(destIP);
				
				String asbLine11[] = asbLine00[1].split(" ");
				packageNum = Integer.valueOf(asbLine11[0]).intValue();
				packageReceived = Integer.valueOf(asbLine11[3]).intValue();
				packageLossRate = Integer.valueOf((asbLine11[5].split("%"))[0]).intValue();
				pingTotalTime = Integer.valueOf((asbLine11[9].split("ms"))[0]).intValue();
				// (3)rtt时间（最小/平均/最大/方差）
				String asbLine12[] = asbLine00[2].split(" ");
				String asbLine123[] = asbLine12[3].split("/");
				rttMin = Float.parseFloat(asbLine123[0]);
				rttAvg = Float.parseFloat(asbLine123[1]);
				rttMax = Float.parseFloat(asbLine123[2]);
				rttMdev = Float.parseFloat(asbLine123[3]);
				
			} else {
				Log.d(TAG, "此次Ping采用了长格式");
				
				destIPContent = asbLine10[3];
				destIP = destIPContent.split(":")[0];								// 默认的Console输出会在目的IP后跟一个冒号，在此截取
				
				System.out.println("此次截取到的/域名 >>> ");
				System.out.println(destIP);
				
				Pattern pattern = Pattern.compile("[a-zA-z]");
				Matcher matcher = pattern.matcher(destIP);
				boolean found = matcher.find();
				if (found) {
					Log.d(TAG, "目标IP是域名，采用字符串截取方法（一）");
					ICMP_SEQ = Integer.valueOf(((asbLine10[5]).split("="))[1]).intValue();
					ttl = Integer.valueOf(((asbLine10[6]).split("="))[1]).intValue();
				} else {
					Log.d(TAG, "目标IP是地址，采用字符串截取方法（二）");
					ICMP_SEQ = Integer.valueOf(((asbLine10[4]).split("="))[1]).intValue();
					ttl = Integer.valueOf(((asbLine10[5]).split("="))[1]).intValue();
				}
				// (2)包总数、收到包、损失百分比、Ping耗时
				String asbLine13[] = asbLine00[3].split(" ");
				packageNum = Integer.valueOf(asbLine13[0]).intValue();
				packageReceived = Integer.valueOf(asbLine13[3]).intValue();
				packageLossRate = Integer.valueOf((asbLine13[5].split("%"))[0]).intValue();
				pingTotalTime = Integer.valueOf((asbLine13[9].split("ms"))[0]).intValue();
				// (3)rtt时间（最小/平均/最大/方差）
				String asbLine14[] = asbLine00[4].split(" ");
				String asbLine143[] = asbLine14[3].split("/");
				rttMin = Float.parseFloat(asbLine143[0]);
				rttAvg = Float.parseFloat(asbLine143[1]);
				rttMax = Float.parseFloat(asbLine143[2]);
				rttMdev = Float.parseFloat(asbLine143[3]);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// 04
	public void getPingTimeOutInfo() {
		try {
			// 目的IP、字节数、ICMP_SEQ、TTL
			bytes = -1;
			destIP = "0.0.0.0";
			ICMP_SEQ = -1;
			ttl = -1;
			// 包总数、收到包、损失百分比、Ping耗时
			packageNum = -1;
			packageReceived = -1;
			packageLossRate = -1;
			pingTotalTime = -1;
			// rtt时间（最小/平均/最大/方差）
			rttMin = -1;
			rttAvg = -1;
			rttMax = -1;
			rttMdev = -1;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ####################################################################
	 * 
	 * 对外暴露的数据接口
	 * 执行完上述的截取与判断之后外部类可用下面的接口获取成员变量的值
	 *  
	 * ####################################################################
	 */
	public int getBytes() {
		return bytes;
	}
	public String getDestIP() {
		return destIP;
	}
	public int getICMP_SEQ() {
		return ICMP_SEQ;
	}
	public int getTtl() {
		return ttl;
	}
	public int getPackageNum() {
		return packageNum;
	}
	public int getPackageReceived() {
		return packageReceived;
	}
	public int getPackageLossRate() {
		return packageLossRate;
	}
	public int getPingTotalTime() {
		return pingTotalTime;
	}
	public float getRttMin() {
		return rttMin;
	}
	public float getRttAvg() {
		return rttAvg;
	}
	public float getRttMax() {
		return rttMax;
	}
	public float getRttMdev() {
		return rttMdev;
	}
	
	@Override
	public String toString() {
		return "DWFStructurePingInfo [bytes=" + bytes + ", destIP=" + destIP
				+ ", ICMP_SEQ=" + ICMP_SEQ + ", ttl=" + ttl + ", packageNum="
				+ packageNum + ", packageReceived=" + packageReceived
				+ ", packageLossRate=" + packageLossRate + ", pingTotalTime="
				+ pingTotalTime + ", rttMin=" + rttMin + ", rttAvg=" + rttAvg
				+ ", rttMax=" + rttMax + ", rttMdev=" + rttMdev + "]";
	}
	
}
