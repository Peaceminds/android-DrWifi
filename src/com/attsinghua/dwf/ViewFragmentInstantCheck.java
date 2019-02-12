package com.attsinghua.dwf;

/*
 * 主要的View之二/三 
 * 供用户评价无线网并进行诊断
 * 上传无线网指纹+错误码+Ping结果+时间戳
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import com.thu.wlab.dwf.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ViewFragmentInstantCheck extends Fragment implements OnClickListener {
	
	/** 
	 * #######################################################################
	 * 
	 * ***flag***
	 * 
	 * 01 - threadMediator
	 * 	    false为默认
	 * 	    true的话所有线程都将不能循环
	 * 02 - 开始按钮按下检测
	 * 	    false为默认
	 * 	    true的提示正在执行,不会重复调用线程
	 * 03 - Ping的flag
	 * 	    false为默认
	 * 	    true表示已执行完相应的多线程Ping操作
	 * 
	 * ***变量定义***
	 * 
	 * 01 - 界面UI元素相关
	 * 02 - WiFi指纹相关
	 * 03 - Ping信息相关
	 * 04 - 错误码相关
	 * 05 - 上传相关
	 * 06 - 数据库相关
	 * 07 - SharedPreferences相关
	 * 
	 * #######################################################################
	 */
	private static final String TAG = "InstantCheckActivity";
	private ControlGetWifiFingerPrint getWFPNow = new ControlGetWifiFingerPrint();
	private ControlDBMana dbMana = new ControlDBMana();
	private ControlJsonMaker makeJsonObj = new ControlJsonMaker();
	private ModelErrCodeData errorCodeEntity = new ModelErrCodeData();
	private ModelPingData innerPingRst = new ModelPingData();
	private ModelPingData outerPingRst = new ModelPingData();
	private TextView nowWhatTV;
	private TextView chkResultTV;
	private TextView logsTV;
	private ImageView chkResultIV;
	private Button chkButton;
	private Button stpButton;
//	private ProgressBar pgBar;															// 目前将进度条及其相关方法去掉了
	private String innerMediateOutput;
	private String outerMediateOutput;
	private String outerPingDest;
	private JSONObject wfpJo;
	private JSONObject ecdJo;
	private JSONObject PingJo;
	private static int lastUploadFalseTimes;
	private static SharedPreferences mySharedPreferences;
	
	public static boolean threadMediator = false;
	public static boolean startChecker = false;
	public static boolean innerPingisOK;
	public static boolean outerPingisOK;
	public static boolean allPingEnd = false;
	public static boolean legalSSID;
	
	/**
	 * #######################################################################
	 * 
	 * Fragment初始化
	 * 
	 * F01 - onCreateView基本的视图初始化方法
	 * F02 - onClick时间监听之 开始按钮
	 * 	  F02-01 War05 - 设备信息获取的判断
	 * 	  F02-02 Err01 - 如果拿不到WifiManager对象的报错
	 * 	  F02-03 Err02 - WiFi模块不正确的报错
	 * 	  F02-04 Err03 - DHCP的检测
	 * 	  F02-05 War01 - WifiInfo的检测
	 * 	  F02-06 War02 - WifiScan的检测
	 * 	  F02-07 War03 - Ping校园网网关 - 调用多线程T03
	 * 	  F02-08 War04 - Ping校外网指定地址
	 * 	  F02-09 War06 - 信息的上传与错误处理 - 调用多线程
	 * 	  F02-10 Report - 用户报告生成与上传 - 调用主线程M
	 * F03 - onClick时间监听之 终止按钮
	 * F04 - SharedPreferences初始化
	 * 
	 * #######################################################################
	 */
	
	// F01
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View icLayout = inflater.inflate(R.layout.layout_instantcheck, container, false);
		nowWhatTV = (TextView) icLayout.findViewById(R.id.instantcheck_tip);
		chkResultTV = (TextView) icLayout.findViewById(R.id.instantcheck_result);
		chkResultIV = (ImageView) icLayout.findViewById(R.id.instant_rst_pic);
		chkButton = (Button) icLayout.findViewById(R.id.instantcheck_btn);
		stpButton = (Button) icLayout.findViewById(R.id.instantcheck_stop);
//		pgBar = (ProgressBar) icLayout.findViewById(R.id.instantcheck_progressBar);
		logsTV = (TextView) icLayout.findViewById(R.id.instantcheck_Logs);
		chkButton.setOnClickListener(this);
		stpButton.setOnClickListener(this);
		logsTV.setMovementMethod(ScrollingMovementMethod.getInstance());
		chkResultTV.setMovementMethod(ScrollingMovementMethod.getInstance());
		
		return icLayout;
	}

	// F02
	@Override
	public void onClick(View v) {
		if (ModelPressInterval.isFastDoubleClick()) {															// 暴力点击检测
			Toast.makeText(getActivity(), "点太快不是好孩子_(:з」∠)_呐，等2秒再点了啦~", Toast.LENGTH_SHORT).show();
			return;
		} else {
			switch (v.getId()) {
			
			case R.id.instantcheck_btn:																			// 点击开始按钮 ================================================			
				if (startChecker == false) {																	// flag检测--判断是否已经点击过开始
					
					threadMediator = false;																		// flag重置--为准备就绪状态
					allPingEnd = false;
					startChecker = true;
					getWFPNow.setThreadMediatorInStructure(threadMediator);
					nowWhatTV.setText("执行WiFi基本状态检查");
					chkResultTV.setText("");
					logsTV.setText("\n" + "开始检查~" + "\n");
//					pgBar.setProgress(0);
					chkResultIV.setBackgroundResource(R.drawable.instant_check_welcome);
					
					// 获取Wifi相关的信息
					getWFPNow.setThreadMediatorInStructure(threadMediator);										// 获取判断所需的Wifi信息（内部以多线程实现）
					getWFPNow.getWifiMana(getActivity());
					getWFPNow.getWifiState();
					getWFPNow.getWifiDhcpInfo();
					getWFPNow.checkWifiFingerPrint();
					getWFPNow.getDeviceInfo(getActivity());														// 获取设备硬件信息

					/*
					 * F02-1
					 */
					if (getWFPNow.isHaveDeviceInfo() == false) {
						errorCodeEntity.setWAR05_PHONE_INFO(true);												// WAR05警告码与时间戳
						nowWhatTV.setText("设备硬件信息读取失败");
						chkResultTV.setText("读不到手机型号啦！外星设备是不行的~");
						appendLogSec("\n" + "无法获悉本机型号" + "\n");
					} else {
						nowWhatTV.setText("设备硬件信息读取中");
						chkResultTV.setText("设备硬件信息......正常");
						appendLogSec("\n" + "OEM厂商 = "
								+ getWFPNow.getDevManufacture() + "\n"
								+ "设备型号 = " + getWFPNow.getDevModel() + "\n"
								+ "系统版本 = " + getWFPNow.getSftVersion() + "\n"
								+ "固件版本 = " + getWFPNow.getDevFirmware() + "\n");
					}
//					pgBar.incrementProgressBy(5);

					/*
					 * F02-2
					 */
					if (getWFPNow.isHaveWifiMana() == false) {
						errorCodeEntity.setERR01_NO_WIFI_MANAGER(true);											// Err01错误码与时间戳
						threadMediator = true;																	// 捕捉到错误码则flag变化
						startChecker = false;
						getWFPNow.setThreadMediatorInStructure(threadMediator);
						nowWhatTV.setText("WiFi错误，您的系统或硬件可能存在问题");
						chkResultTV.setText("软件无法获取任何WiFi信息");
						appendLogSec("\n" + "本机系统或硬件可能存在故障" + "\n");
//						pgBar.incrementProgressBy(100);
						Log.i("test", "test");
						chkResultIV.setBackgroundResource(R.drawable.ic_launcher);
					} else {
						nowWhatTV.setText("WiFi基础检测完成");
						chkResultTV.setText("WiFi逻辑模块......正常");
						appendLogSec("\n" + "WiFi逻辑模块正常" + "\n");
//						pgBar.incrementProgressBy(5);
					}

					/*
					 * F02-3
					 */
					if (threadMediator == false) {
						int tmpIntforWifiState = getWFPNow.getWifiState();
						String tmpStrforWifiState = getWFPNow.getWifiStateString();
						switch (tmpIntforWifiState) {
						case 3: 																				// WIFI_STATE_ENABLED
							nowWhatTV.setText("WiFi已打开");
							appendChkTextViewSec(tmpStrforWifiState);
							appendLogSec("\n" + "WiFi硬件模块正常" + "\n");
//							pgBar.incrementProgressBy(5);
							break;
						case 1: 																				// WIFI_STATE_DISABLED
							errorCodeEntity.setERR02_NO_WIFI_STATE(true);
							threadMediator = true;
							startChecker = false;
							getWFPNow.setThreadMediatorInStructure(threadMediator);
							appendLogSec("\n" + "WiFi模块DISABLED" + "\n");
							nowWhatTV.setText("WiFi模块错误");
							appendChkTextViewSec(tmpStrforWifiState);
//							pgBar.incrementProgressBy(100);
							chkResultIV.setBackgroundResource(R.drawable.instant_check_bad);
							break;
						case 0: 																				// WIFI_STATE_DISABLING
							errorCodeEntity.setERR02_NO_WIFI_STATE(true);
							threadMediator = true;
							startChecker = false;
							getWFPNow.setThreadMediatorInStructure(threadMediator);
							appendLogSec("\n" + "WiFi模块DISABLING" + "\n");
							nowWhatTV.setText("WiFi模块错误");
							appendChkTextViewSec(tmpStrforWifiState);
//							pgBar.incrementProgressBy(100);
							chkResultIV.setBackgroundResource(R.drawable.instant_check_bad);
							break;
						case 2: 																				// WIFI_STATE_ENABLING
							errorCodeEntity.setERR02_NO_WIFI_STATE(true);
							threadMediator = true;
							startChecker = false;
							getWFPNow.setThreadMediatorInStructure(threadMediator);
							appendLogSec("\n" + "WiFi模块ENABLING" + "\n");
							nowWhatTV.setText("WiFi模块错误");
							appendChkTextViewSec(tmpStrforWifiState);
//							pgBar.incrementProgressBy(100);
							chkResultIV.setBackgroundResource(R.drawable.instant_check_bad);
							break;
						default: 																				// WIFI_STATE_UNKNOW
							errorCodeEntity.setERR02_NO_WIFI_STATE(true);
							threadMediator = true;
							startChecker = false;
							getWFPNow.setThreadMediatorInStructure(threadMediator);
							appendLogSec("\n" + "WiFi模块UNKNOW" + "\n");
							nowWhatTV.setText("WiFi模块发生了未知错误！");
							appendChkTextViewSec(tmpStrforWifiState);
//							pgBar.incrementProgressBy(100);
							chkResultIV.setBackgroundResource(R.drawable.instant_check_bad);
							break;
						}
					}

					/*
					 * F02-4
					 */
					if (threadMediator == false) {
						if (getWFPNow.isHaveDHCPInfo() == false) {
							threadMediator = true;
							startChecker = false;
							getWFPNow.setThreadMediatorInStructure(threadMediator);
							errorCodeEntity.setERR03_NO_DHCP_IP(true);
							nowWhatTV.setText("无法获取DHCP");
							appendChkTextViewSec("DCHP信息为空，您当前可能无法上网");
							appendLogSec("\n" + "DHCP信息 = Null" + "\n");
//							pgBar.incrementProgressBy(100);
							chkResultIV.setBackgroundResource(R.drawable.instant_check_bad);
						} else {
							nowWhatTV.setText("DHCP检测完成");
							appendChkTextViewSec("DHCP信息读取...正常");
							appendLogSec("\n" + "DHCP信息 = " + getWFPNow.getWifiDhcpInfo().toString() + "\n");
//							pgBar.incrementProgressBy(5);
						}
					}

					/*
					 * F02-5
					 */
					if (threadMediator == false) {
						if (getWFPNow.isHaveWifiInfo() == false) {
							nowWhatTV.setText("无法获取WifiInfo");
							chkResultTV.setText("无法读取您的WiFi连接详细信息，您的访问可能受限");
							appendLogSec("\n" + "WifiInfo = Null" + "\n");
							chkResultIV.setBackgroundResource(R.drawable.instant_check_warn);
							errorCodeEntity.setWAR01_NO_WIFI_INFO(true);
						} else {
							nowWhatTV.setText("WiFi信息读取完成");
							appendChkTextViewSec("WiFi信息读取.......正常");
							appendLogSec("\n" + "连接信息 = " + getWFPNow.getWifiInfo().toString() + "\n");
						}
					}
//					pgBar.incrementProgressBy(5);

					/*
					 * F02-6
					 */
					if (threadMediator == false) {
						if (getWFPNow.isHaveScanInfo() == false) {
							nowWhatTV.setText("无法扫描周围AP");
							chkResultTV.setText("您当前扫描不到其他任何AP信息");
							appendLogSec("\n" + "WifiScanResult = Null" + "\n");
							chkResultIV.setBackgroundResource(R.drawable.instant_check_warn);
							errorCodeEntity.setWAR02_NO_WIFI_SCAN(true);
						} else {
							nowWhatTV.setText("周围AP扫描完成");
							appendChkTextViewSec("AP扫描.................正常");
							appendLogSec("\n" + "周围AP数量  = " + getWFPNow.getAPAroundNum() + "\n");
							appendLogSec("\n" + "AP扫描结果  = " + getWFPNow.getWifiScanInfoList().toString() + "\n");
						}
					}
//					pgBar.incrementProgressBy(5);

					/*
					 * F02-7 -- F02-10
					 */
					innerNetPingThead innerPingThead = new innerNetPingThead("166.111.8.28"); 					//（或8.29）
					innerPingThead.setDaemon(true);
					if (threadMediator == false) {
						innerPingThead.start();
					}
					
				} else {																						// 在已经点击且没执行完的情况下，又点击开始检测按钮
					
					Toast.makeText(getActivity(), "检查执行中，请稍后~或者可以点击停止来终止操作", Toast.LENGTH_SHORT).show();
				}
				break;

			//F03
			case R.id.instantcheck_stop:
				threadMediator = true;
				startChecker = false;
				getWFPNow = new ControlGetWifiFingerPrint();
				getWFPNow.setThreadMediatorInStructure(threadMediator);
				nowWhatTV.setText("体检中断，点开始可重来~");
				chkResultTV.setText("( ¯▽¯；)");
				logsTV.setText("\n" + " ");																		// 默认状态下Log显示的字											
//				pgBar.setProgress(0);
				chkResultIV.setBackgroundResource(R.drawable.instant_check_welcome);
				break;
			default:
				break;
			}
		}
		return;
	}

	//F04
	private SharedPreferences getSharedPreferences(String spName, int mode_PRIVATE) {
		return getActivity().getSharedPreferences(spName, mode_PRIVATE);
	}
	
	/**
	 * ####################################################################
	 * 
	 * 多线程方法
	 * 
	 * T01 - Handler定义
	 * T02 - Handler调用T05 Ping多线程
	 * T03 - 内网Ping多线程
	 * T04 - 外网Ping多线程
	 * T05 - 上传wfp信息
	 * T06 - 上传wfp失败时置位
	 * 
	 * ####################################################################
	 */
	
	// T01
	@SuppressLint("HandlerLeak")
	private Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			
			// 10 - 内网Ping命令执行失败
			case 10:
				innerPingisOK = false;
				appendChkTextViewSec("Ping内网失败");
				appendLogSec("\n" + "无法执行Ping内网的指令" + "\n");
				errorCodeEntity.setWAR03_PING_INNER(true);				// WAR内网Ping失败错误码										
				String toPing10 = readSPOuterPingDest();				// 调用M05（读SP中存的外网IP）
				startToOuterPing(toPing10);								// 调用T02
				break;
			
			// 11 - 内网Ping命令执行成功
			case 11:													
				if (innerPingRst.isPingOk(innerMediateOutput)) {		// 内网Ping得通的赋值
					innerPingRst.getPingInfoStringDetails(innerMediateOutput);
					} else {											// 内网Ping不通的赋值
						innerPingRst.getPingTimeOutInfo();
					}
				innerPingisOK = true;
				appendChkTextViewSec("Ping内网过程......完成");
				appendLogSec("\n" + "Ping内网指令执行完毕" + "\n");
				String toPing11 = readSPOuterPingDest();				// 调用M05（读SP中存的外网IP）
				startToOuterPing(toPing11);								// 调用T02
				break;
				
			// 20 - 外网Ping命令执行失败
			case 20:													
				outerPingisOK = false;
				appendChkTextViewSec("Ping外网失败");
				appendLogSec("\n" + "无法执行Ping外网的指令" + "\n");
				errorCodeEntity.setWAR04_PING_OUTER(true);				// WAR外网Ping失败错误码				
				cheerAfterPing();										// 调用M03
				break;
				
			// 21 - 外网Ping命令执行成功
			case 21:													
				if (outerPingRst.isPingOk(outerMediateOutput)) {		// 外网Ping得通的赋值
					outerPingRst.getPingInfoStringDetails(outerMediateOutput);
				} else {												// 外网Ping不通的赋值
					outerPingRst.getPingTimeOutInfo();
					}
				outerPingisOK = true;
				appendChkTextViewSec("Ping外网过程......完成");
				appendLogSec("\n" + "Ping外网指令执行完毕" + "\n");				
				cheerAfterPing();										// 调用M03
				break;
			
			// 60 - 上传失败的SP赋值
			case 60:
				uploadFailSetFunc();
				break;
			}
		};
	};
	
	// T02 - Ping外网网关多线程的调用
	private void startToOuterPing(String toPing) {
		if (threadMediator == false) { 									
			nowWhatTV.setText("再稍等一下下哈");
			appendLogSec("\n" + "开始Ping外网咯" + "\n");
//			pgBar.incrementProgressBy(5);
			outerNetPingThead outerPingThead = new outerNetPingThead(toPing);
			outerPingThead.setDaemon(true);
			if (threadMediator == false) {
				outerPingThead.start();
			}
		}
	}

	
	// T03 - Ping校园网网关
	private class innerNetPingThead extends Thread {
		private String mHost;
		public innerNetPingThead(String host) {
			mHost = host;
		}

		@Override
		public void run() {
			try {
				Log.d(TAG, "开始Ping啦~ '" + mHost + "'...");
				Process process = new ProcessBuilder()
						.command("/system/bin/ping", "-c 4", mHost)
						.redirectErrorStream(true).start();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(process.getInputStream()));
				int i;
				char[] buffer = new char[4096];
				StringBuffer output = new StringBuffer();
				while ((i = reader.read(buffer)) > 0 && threadMediator == false) {
					innerMediateOutput = new String(Arrays.copyOfRange(buffer, 0, i));
					Log.d(TAG, "Ping内网结果: " + innerMediateOutput);
					output.append(buffer, 0, i);
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							nowWhatTV.setText("Ping校内网阶段");
							appendChkTextViewSec("Ping内网中...");
							String Step04 = logsTV.getText().toString();
							logsTV.setText(Step04);
							appendLogSec(innerMediateOutput);
						}
					});
				}
				reader.close();
				myHandler.sendEmptyMessage(11);
			} catch (final Exception e) {
				e.printStackTrace();
				myHandler.sendEmptyMessage(10);
			}
		}
	};

	// T04 - Ping外网线程
	private class outerNetPingThead extends Thread {
		private String mHost;
		public outerNetPingThead(String host) {
			mHost = host;
		}
		
		@Override
		public void run() {
			if (threadMediator == false) {
				try {
					Log.d(TAG, "start to ping '" + mHost + "'...");
					Process process = new ProcessBuilder()
							.command("/system/bin/ping", "-c 4", mHost)
							.redirectErrorStream(true).start();
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(process.getInputStream()));
					int i;
					char[] buffer = new char[4096];
					StringBuffer output = new StringBuffer();
					while ((i = reader.read(buffer)) > 0
							&& threadMediator == false) {
						outerMediateOutput = new String(Arrays.copyOfRange(
								buffer, 0, i));
						Log.d(TAG, "Ping外网结果: " + outerMediateOutput);
						output.append(buffer, 0, i);
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								nowWhatTV.setText("Ping校外网阶段");
								appendChkTextViewSec("努力Ping外网中~");
								String finStep = logsTV.getText().toString();
								logsTV.setText(finStep);
								appendLogSec(outerMediateOutput);
							}
						});
					}
					reader.close();
					myHandler.sendEmptyMessage(21);
				} catch (final Exception e) {
					e.printStackTrace();
					myHandler.sendEmptyMessage(20);
				}
			}
		}
	};
	
	// T05 - 上传检测信息
	private void upLoadInsCheckRstToServer() {

		getWFPNow.setThreadMediatorInStructure(false);
		getWFPNow.getWifiMana(getActivity());
		getWFPNow.getWifiInfo();
		getWFPNow.getWifiDhcpInfo();
		getWFPNow.getWifiScanInfoList();
		getWFPNow.getDeviceInfo(getActivity());
		wfpJo = makeJsonObj.fingerPrintToJSON(getWFPNow); 							// 三个实例对象的JSON封装
		ecdJo = makeJsonObj.errCodeToJson(errorCodeEntity);
		PingJo = makeJsonObj.pingDataToJson(innerPingRst, outerPingRst);

		new Thread() {
			public void run() {
				try {
					List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
					String fpDataStr = wfpJo.toString(); 							// 三个JSON转为串 作为上传的最后准备
					String ecdDataStr = ecdJo.toString();
					String PingDataStr = PingJo.toString();
					String sysTime = makeJsonObj.timestampToJson().toString(); 		// 准备好上传时间
					params.add(new BasicNameValuePair("msgType", "{\"msgType\":1}"));
					params.add(new BasicNameValuePair("sysTime", sysTime)); 		// JSON额外的封装，用于给服务器标明字符串内容
					params.add(new BasicNameValuePair("ecdData", ecdDataStr));
					params.add(new BasicNameValuePair("PingDataStr",PingDataStr));
					params.add(new BasicNameValuePair("fpData", fpDataStr));
					
					Bundle httpRes = ControlHttpsUtil.HttpsPost(
							"https://目标API", params);
					
					//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
					Log.w(TAG+"Type=1的Insfp上传", params.toString());
					
					
					String httpResState = httpRes.getString("res");
					if (httpResState.equals("ok")) {
						Log.i(TAG, "INSTANTCHECK信息上传成功！");
					} else {
						myHandler.sendEmptyMessage(60);
						Log.i(TAG, "INSTANTCHECK信息上传失败！");
					}
				} catch (Exception e) {
					e.printStackTrace();
					myHandler.sendEmptyMessage(60);
					Log.i(TAG, "INSTANTCHECK信息上传失败：未知错误！");
				}
			};
		}.start();
	}

	// T06 - 检测信息入库
	private void insertInsCheckDataToDB() {
		new Thread() {
			@Override
			public void run() {
				try {
					dbMana.createUserDBbyHelper(getActivity(), "dwfdb"); 			// 数据库存储
					dbMana.insertWifiFingerPrintToDB(errorCodeEntity, getWFPNow);
					dbMana.insertAPScanInfoToDB(getWFPNow);
					Log.i(TAG, "外部Ping结果：" + outerPingRst.toString());
					dbMana.insertPingDetailToDB(getWFPNow, innerPingRst, outerPingRst);
					// dbMana.queryWifiFingerPrintData(); 				 			//***数据库读取(调试用)
					// dbMana.queryAPScanData();
					// dbMana.queryPingData();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
		
		
	/**
	 * ####################################################################
	 * 
	 * 主线程方法
	 * 
	 * M01 - 向UI中“检查结果”文字添加内容
	 * M02 - 向Log中添加内容 
	 * M03 - Ping结束后的收尾方法
	 * M04 - 根据所有检测数据执行判断并生成报告
	 * 	  M04 - 01 DHCP阶段判断
	 * 	  M04 - 02 信号及链路速率判断
	 * 	  M04 - 03 内部Ping判断
	 * 	  M04 - 04 外部Ping判断
	 * M05 - SharedPreferences读取Ping目标外网网关
	 * M06 - SharedPreferences设定（上传失败时错误计数+1）
	 * 
	 * ####################################################################
	 */
	// M01
	private void appendChkTextViewSec(String chkRst) {
		String originString = chkResultTV.getText().toString();
		chkResultTV.setText(chkRst + "\n" + originString);
	}
	
	// M02
	private void appendLogSec(String log) {
		String origin = logsTV.getText().toString();
		logsTV.setText(log + origin);
	}
	
	// M03 - Ping结束后的收尾方法
	private void cheerAfterPing() {
		startChecker = false; 											// 更改开始按钮和全部线程完成的flag
		allPingEnd = true;
		if (threadMediator == false) {
			nowWhatTV.setText("检测完成啦~");
			judgment();													// 执行判断方法
			appendChkTextViewSec("--------------------------"); 		// UI中添加分割线
			appendChkTextViewSec("您可以滑动查看报告，也可重开检测");
//			pgBar.incrementProgressBy(5);
			if ( ModelConfiguration.getIsCurrentSSIDLegal(getWFPNow.getLinkSSID()) ) {
				upLoadInsCheckRstToServer();
			}							
			insertInsCheckDataToDB();									// 执行将所有数据插入数据库方法
		}
	}
	
	// M04
	private void judgment() {
		appendChkTextViewSec("--------------------------" + "\n" + "检测报告" + "\n" + "--------------------------");
		
		/*
		 * M04-01
		 */
		int ipAdd = getWFPNow.getWifiDhcpInfo().ipAddress;
		DetailedState dhcpStep;
		SupplicantState sstate = getWFPNow.getWifiInfo().getSupplicantState();
		
		if (sstate != null) {											// 获取四次握手的状态信息，连接成功的话一般会停留在 OBTAINING_IPADDR 状态
			dhcpStep = WifiInfo.getDetailedStateOf(sstate);
			if (dhcpStep == NetworkInfo.DetailedState.OBTAINING_IPADDR && ipAdd != 0) {
				appendChkTextViewSec("IP获取 => 正常");
			} else {
				appendChkTextViewSec("IP获取 => 异常");
				chkResultIV.setBackgroundResource(R.drawable.instant_check_warn);
			}
		} else {
			appendChkTextViewSec("DHCP => 状态异常，可能无法上网");
			chkResultIV.setBackgroundResource(R.drawable.instant_check_bad);
		}
//		pgBar.incrementProgressBy(10);
		
		getWFPNow.getWifiScanInfoList();
		int aAPN = getWFPNow.getAPAroundNum();
		if (aAPN < 10) {
			appendChkTextViewSec("附近AP => " + aAPN + "个");
			appendLogSec("\n" + "附近AP数量 < 10" + aAPN + "\n");
		}
		if (aAPN > 10 && aAPN < 20) {
			appendChkTextViewSec("附近AP数 => 较多");
			appendLogSec("\n" + "附近AP数量 " + aAPN + "\n");
		}
		if (aAPN > 20) {
			appendChkTextViewSec("附近AP数 => 很多");
			appendLogSec("\n" + "附近AP数量： " + aAPN + "（很多，可能对您上网造成影响）" + "\n");
		}
		
		/*
		 * M04-02
		 */
		int linkSpeed = getWFPNow.getLinkSpeed();
		int linkStrength = getWFPNow.getLinkStrength();
		if (linkStrength <= -80) {
			appendChkTextViewSec("WiFi信号 => 差");
			appendLogSec("\n" + "信号 <-80dBm" + "\n");
		}
		if (linkStrength <= -70 && linkStrength > -80) {
			appendChkTextViewSec("WiFi信号 => 中");
			appendLogSec("\n" + "-80dBm < 信号 <= -70dBm" + "\n");
		}
		if (linkStrength > -60) {
			appendChkTextViewSec("WiFi信号 => 好");
			appendLogSec("\n" + "信号 >= -70dBm" + "\n");
		}
		if (linkSpeed <= 20) {
			appendChkTextViewSec("链路速率 => 差");
			appendLogSec("\n" + "链路速率 <20Mbps" + "\n");
		}
		if (linkSpeed < 30 && linkSpeed > 20) {
			appendChkTextViewSec("链路速率 => 中");
			appendLogSec("\n" + "链路速率 <30Mbps" + "\n");
		}
		if (linkSpeed > 30) {
			appendChkTextViewSec("链路速率 => 好");
			appendLogSec("\n" + "链路速率 > 30Mbps" + "\n");
		}
		if (linkStrength > -50 && linkSpeed < 20) {
			appendChkTextViewSec("AP负载 => 重");
			appendLogSec("\n" + "链路较差 信号良好" + "\n");
		}
		if (linkStrength > -50 && linkSpeed > 20) {
			appendChkTextViewSec("WiFi信号 => 好");
			appendChkTextViewSec("连接速率 => 高");
			appendLogSec("\n" + "链路良好 信号良好" + "\n");
		}
//		pgBar.incrementProgressBy(10);

		/*
		 * M04-03
		 */
		if (innerPingisOK) {
			if (innerPingRst.getPackageReceived() == -1) {
				appendChkTextViewSec("校内访问 => 受限");
				appendLogSec("\n" + "Ping不通校内网关" + "\n");
				chkResultIV.setBackgroundResource(R.drawable.instant_check_bad);
			} else {
				
				if (innerPingRst.getPackageLossRate() >= 50) {										//存在丢包的分支
					appendChkTextViewSec("\n" + "校内访问 => 丢包较大" + "\n");
					chkResultIV.setBackgroundResource(R.drawable.instant_check_warn);
				}
				if (innerPingRst.getPackageLossRate() < 50 && innerPingRst.getPackageLossRate() > 0) {
					appendChkTextViewSec("\n" + "校内访问 => 干扰有||有线网不稳定" + "\n");
				}
				
				if (innerPingRst.getPackageLossRate() == 0 && innerPingRst.getRttAvg() <= 100) {	//不存在丢包的分支
					appendChkTextViewSec("校内访问 => 正常");
				}
				if (innerPingRst.getPackageLossRate() == 0 && innerPingRst.getRttAvg() > 100) {
					appendChkTextViewSec("校内访问 => 干扰大||有线网不稳定");
				}
			}
		} else {
			appendLogSec("\n" + "Ping校内网命令执行出错" + "\n");
		}
//		pgBar.incrementProgressBy(10);

		/*
		 * M04-05
		 */
		if (outerPingisOK) {
			if (outerPingRst.getPackageReceived() == -1) {
				appendChkTextViewSec("外网访问 => 受限");
				appendLogSec("\n" + "Ping不通校外网关：" + outerPingDest + "\n");
//				pgBar.incrementProgressBy(10);
				chkResultIV.setBackgroundResource(R.drawable.instant_check_warn);
			} else {
				// 存在丢包的分支
				if (outerPingRst.getPackageLossRate() >= 50) {										
					appendChkTextViewSec("外网访问 => 丢包较大");
					appendChkTextViewSec("注：认证后方可使用校园网");
				}
				if (outerPingRst.getPackageLossRate() < 50 && outerPingRst.getPackageLossRate() > 0) {
					appendChkTextViewSec("外网访问 => 丢包有");
					appendChkTextViewSec("注：认证后方可使用校园网");
				}
				// 不存在丢包的分支
				if (outerPingRst.getPackageLossRate() == 0 && outerPingRst.getRttAvg() <= 100) {	
					appendChkTextViewSec("外网访问 => 正常");
					appendChkTextViewSec("注：认证后方可使用校园网");
				}
				if (outerPingRst.getPackageLossRate() == 0 && outerPingRst.getRttAvg() > 100) {
					appendChkTextViewSec("外网访问 => 压力大");
					appendChkTextViewSec("注：认证后方可使用校园网");
				}
			}
		} else {
			appendLogSec("\n" + "Ping校外网命令执行出错" + "\n");
		}
//		pgBar.incrementProgressBy(10);
	}
	
	// M05
	private String readSPOuterPingDest() {															//SharedPreferences读取Ping目标外网网关
		SharedPreferences sharedPreferences = getSharedPreferences("my_sp_instance", Activity.MODE_PRIVATE);
		if (sharedPreferences != null) {
			outerPingDest = sharedPreferences.getString("sp_outer_ping_dest", "");
			if (outerPingDest == "") {
				outerPingDest = "www.baidu.com";
			}
		} else {
			outerPingDest = "www.baidu.com";
		}
		Log.i(TAG, "当前读到的外网网关IP为——————————" + outerPingDest);
		return outerPingDest;
	}
	
	// M06
	private void uploadFailSetFunc() {
		mySharedPreferences = getActivity().getSharedPreferences("my_sp_instance", Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = mySharedPreferences.edit();
		editor.putInt("sp_last_upload_false", lastUploadFalseTimes+1);
		editor.commit();
	}

}
