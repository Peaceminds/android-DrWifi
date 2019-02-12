package com.attsinghua.dwf;

/*
 * 主要的View之一/三 
 * 供用户简要查看当前无线网连接状态 后台进行Rogue AP检测
 * 上传无线网指纹+时间戳
 * 2019-02-12更新：为了保护服务器IP，将https的url统一改为了“目标API”
 */

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.thu.wlab.dwf.R;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ViewFragmentNetList extends Fragment {

	private static final String TAG = "ViewFragmentNetList";
	private static SharedPreferences mySharedPreferences;
	private static int startTimes;
	private static int dbInitialedTimes;
	private ModelErrCodeData errorCodeEntity = new ModelErrCodeData();
	private ControlJsonMaker makeJsonObj = new ControlJsonMaker();
	private ControlDBMana dbMana;
	private ImageButton refreshBtn;
	private ImageView netStatusFeelView;
	private ListView myfstListView;
	private RelativeLayout netListLayoutBackground;
	private ListAdapter netInfoAdapter = new ListAdapter();
	private JSONObject wfpJo;
	private JSONObject goodAPDataJO;
	private JSONObject badAPDataJO;
	private Long goodAPDataVer;
	private Long badAPDataVer;
	private Long lastTimestamp;
	private Long newestTimeStamp;
	private int lastUploadFalseTimes;
	private int successHistoryUpload = 0;
	private String myTokenString;
	private String atTsinghuaTokenString;
	public ControlGetWifiFingerPrint getWFPNow = new ControlGetWifiFingerPrint();
	public int ENTRY_NUM = 11;
	public String[][] mStatus = new String[ENTRY_NUM][2];
	// F
	public boolean isAPChanged = false;
	// F
	public boolean isRogueAP = false;
	// F1
	public boolean isInitialed;
	// F2
	public boolean isDBInitialReady;
	// F2-1
	public boolean isDBInitialReadyAndBadAPOK;
	// F2-2
	public boolean isDBInitialReadyAndGoodAPOK;
	
	/** 
	 * ####################################################################
	 * 
	 * Fragment初始化
	 * 
	 * F01 - Fragment的View初始化
	 * F02 - Fragment创建时添加到Activity的对象(重写方法)
	 * 
	 * ####################################################################
	 */
	// F01
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View listLayout = inflater.inflate(R.layout.layout_netlist, container, false);			// 绑定UI控件
		netListLayoutBackground = (RelativeLayout) listLayout.findViewById(R.id.netlist_layout_background);
		netStatusFeelView = (ImageView) listLayout.findViewById(R.id.netlist_statusimage);
		myfstListView = (ListView) listLayout.findViewById(R.id.netlist_linklist);
		refreshBtn = (ImageButton) listLayout.findViewById(R.id.netlist_refresh_btn);
		dbMana = new ControlDBMana();
		
		getWifiStatus();																		// 调用M01 获取网络参数的方法
		setFeelView();																			// 调用M02 设定网络状态颜色图片
		myfstListView.setAdapter(netInfoAdapter);												// 调用M03 设定表格数据源Adapter
		connectDB();																			// 调用M05 创建数据库并进行App启动时的数据库操作
		
		if ( (getWFPNow.getWifiMana(getActivity()).getConnectionInfo() == null) || 				// MA1
				(getWFPNow.getWifiMana(getActivity()).getConnectionInfo().getBSSID() == null) ) {
			netStatusFeelView.setImageResource(R.drawable.main_status_down);
			netListLayoutBackground.setBackgroundColor(Color.parseColor("#E21052"));
			mStatus[2][1] = "未关联校园网AP或Wifi连接已中断";
		} else {
			if ( ModelConfiguration.getIsCurrentSSIDLegal(getWFPNow.getLinkSSID()) ) {			// MA2 连接AP过滤 SSID至少包括合法AP名关键词时才上传
				// F1获取
				mySharedPreferences = getActivity().getSharedPreferences("my_sp_instance", Context.MODE_PRIVATE);
				startTimes = mySharedPreferences.getInt("sp_start_times", 0);
				dbInitialedTimes = mySharedPreferences.getInt("sp_db_initial_ok", 99);
				// F1判断
				if (startTimes == 0) {
					isDBInitialReady = false;
					isInitialed = false;
					Log.i(TAG, "F1检测为初次安装App");
				} else {
					if (dbInitialedTimes == 99) {												// 没取到表初始化的情况 或 之前初始化失败了
						isDBInitialReady = false;
						isInitialed = false;
						Log.i(TAG, "非初次安装，但数据库未成功初始化过");
					} else if (dbInitialedTimes == 1) {											// 初始化成功的情况
						Log.i(TAG, "已初始化过，正常执行检测流程");
						isInitialed = true;
					}
				}
				// F1分支选择
				if (isInitialed) {																// Flow01
					// T04
					Log.i(TAG, "开始进入Flow01");
					mStatus[2][1] = "正在为您验证连接AP的真伪";
					localBadCheckIfRogueAP();
				} else {																		// Flow02
					// T30 T02 T40 T12
					Log.i(TAG, "开始进入Flow02");
					uploadInitialFingerPrintToServer();
					requestGoodAPData();
					requestBadAPData();
				}
				
			} else {
				mStatus[2][1] = "当前连接的不是校园网AP，无法为您检测是否恶意AP";
				refreshNetList();
			}
		}
		
		return listLayout;
	}
	
	// F02
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		refreshBtn = (ImageButton) getActivity().findViewById(R.id.netlist_refresh_btn);
		refreshBtn.setOnClickListener((OnClickListener) getActivity());						// 调用M04 监听事件绑定
	}
	
	
	/** 
	 * ####################################################################
	 * 
	 * UI线程方法
	 * 
	 * M01 - 获取网络参数并于列表显示
	 * M02 - 设置网络状态图片(绿 黄 红三色代表不同)
	 * M03 - 设置ListView数据源
	 * M04 - 刷新按钮响应(xml方式注册的刷新按钮可直接关联此，如此刷新网络数据)
	 * M05 - 初始化数据库并执行操作
	 * M06 - getMyToken 在ListFragment下获取Token
	 * M07 - 接口 MainActivity获取到Token后 才能从 SP 中读出Token
	 * M08 - 本次wfp获取
	 * M09 - F02流程中的F2判断与黑名单检测的执行
	 * 
	 * ####################################################################
	 */
	// M01
	private void getWifiStatus() {
		getWFPNow.getWifiMana(getActivity());
		getWFPNow.getWifiInfo();
		getWFPNow.getDeviceInfo(getActivity());
		getWFPNow.checkWifiFingerPrint();
		if (getWFPNow.getWifiMana(getActivity()).getConnectionInfo() == null) {
			Toast.makeText(getActivity().getApplicationContext(),"无法获取当前链接信息哎~", Toast.LENGTH_LONG).show();
		} else {
			mStatus[0][0] = "SSID";
			mStatus[0][1] = getWFPNow.getLinkSSID();
			mStatus[1][0] = "BSSID";
			mStatus[1][1] = getWFPNow.getLinkBSSID();
			mStatus[2][0] = "AP真伪鉴定";
			mStatus[3][0] = "本机IP地址";
			int ip = getWFPNow.getWifiMana(getActivity()).getConnectionInfo().getIpAddress();
			ByteBuffer buffer = ByteBuffer.allocate(4);
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			buffer.putInt(ip);
			try {
				mStatus[3][1] = InetAddress.getByAddress(buffer.array()).getHostAddress();
			} catch (UnknownHostException e) {
				mStatus[3][1] = ip + "";
				e.printStackTrace();
			}
			mStatus[4][0] = "本机MAC地址";
			mStatus[4][1] = getWFPNow.getDevMAC();
			mStatus[5][0] = "AP信号强度";
			mStatus[5][1] = getWFPNow.getLinkStrength() + "";
			mStatus[6][0] = "AP连接状态";
			mStatus[6][1] = getWFPNow.getWifiMana(getActivity()).getConnectionInfo().getSupplicantState() + "";
			mStatus[7][0] = "AP是否匿名";
			mStatus[7][1] = getWFPNow.isLinkIsHidden() + "";
			mStatus[8][0] = "当前AP连接速率";
			mStatus[8][1] = getWFPNow.getLinkSpeed() + "Mbps";
			mStatus[9][0] = "当前网络名";
			mStatus[9][1] = getWFPNow.getLinkSSID();
			mStatus[10][0] = "连接过程最后Log";
			SupplicantState sstate = getWFPNow.getWifiMana(getActivity()).getConnectionInfo().getSupplicantState();
			if (sstate != null) {
				mStatus[10][1] = WifiInfo.getDetailedStateOf(sstate) + "";
			} else {
				mStatus[10][1] = "null";
			}
			return;
		}
	}
	
	// M02
	private void setFeelView() {
		int spd = getWFPNow.getLinkSpeed();
		int rssi = getWFPNow.getLinkStrength();
		// 1 中断情况
		if (getWFPNow.getWifiMana(getActivity()).getConnectionInfo() == null || spd <= 0) {
			netStatusFeelView.setImageResource(R.drawable.main_status_down);
			netListLayoutBackground.setBackgroundColor(Color.parseColor("#E21052"));
		} else {
			// 2 非校园网情况
			if ( !ModelConfiguration.getIsCurrentSSIDLegal(getWFPNow.getLinkSSID()) ) {
				netStatusFeelView.setImageResource(R.drawable.main_status_neednot);
				netListLayoutBackground.setBackgroundColor(Color.parseColor("#c19665"));
				} else {
					// 3 AP初始化或变化情况
					if (isAPChanged) {
						netStatusFeelView.setImageResource(R.drawable.main_status_change);
						netListLayoutBackground.setBackgroundColor(Color.parseColor("#95d600"));
					} else {
						// 4 恶意AP情况
						if (isRogueAP) {
							netStatusFeelView.setImageResource(R.drawable.main_status_rogue);
							netListLayoutBackground.setBackgroundColor(Color.parseColor("#e60012"));
						} else {
							// 5.1  信号不好情况
							if ((0 < spd && spd < 25) || (rssi <= -85)) {
								netStatusFeelView.setImageResource(R.drawable.main_status_soso);
								netListLayoutBackground.setBackgroundColor(Color.parseColor("#FED86F"));
							// 5.2  健康AP情况
							} else {
								netStatusFeelView.setImageResource(R.drawable.main_status_good);
								netListLayoutBackground.setBackgroundColor(Color.parseColor("#1DB966"));
							}
						}
					}
				}
		}
	}
	
	// M03
	private class ListAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return ENTRY_NUM;
		}
		@Override
		public Object getItem(int arg0) {
			return null;
		}
		@Override
		public long getItemId(int arg0) {
			return 0;
		}
		@Override
		public View getView(int position, View view, ViewGroup arg2) {
			if (view == null) {
				view = getActivity().getLayoutInflater().inflate(R.layout.layout_netlist_cell, arg2, false);
			}
			TextView titleTv = (TextView) view.findViewById(R.id.netlist_listtitle);
			titleTv.setText(mStatus[position][0]);
			TextView contentTv = (TextView) view.findViewById(R.id.netlist_listcontent);
			contentTv.setText(mStatus[position][1]);
			return view;
		}
	}
	
	// M04
	public void refreshNetList() {
		getWifiStatus();
		setFeelView();
		netInfoAdapter.notifyDataSetChanged();
	}
	
	// M05
	private void connectDB() {
		try {
			dbMana.createUserDBbyHelper(getActivity(), "dwfdb");
			Log.i(TAG, "数据库链接建立完毕");
		} catch (Exception e) {
			Log.i(TAG, "数据库链接建立失败或无法执行goodap_table版本查询");
			e.printStackTrace();
		}
	}
	
	// M06
	private void getMyToken() {
		String tempStr;
		JSONObject jo = new JSONObject();
		mySharedPreferences = getActivity().getSharedPreferences("my_sp_instance", Context.MODE_PRIVATE);
		String defValue = new String();
		tempStr = mySharedPreferences.getString("sp_device_token", defValue);				// Token获取失败后，尝试读取SP中的Token
		try {
			jo.put("devToken", tempStr);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		myTokenString = jo.toString();
		Log.i(TAG, "当前从SP中拿到的Token为：" + myTokenString);
	}
	
	// M07
	public void setAPChanged(boolean isAPChanged) {
		this.isAPChanged = isAPChanged;
	}
	
	// M08
	private void getTheWfpThisTime(){
		getWFPNow.setThreadMediatorInStructure(false);
		getWFPNow.getWifiMana(getActivity());
		getWFPNow.getWifiInfo();
		getWFPNow.getWifiDhcpInfo();
		getWFPNow.getWifiScanInfoList();
		getWFPNow.getDeviceInfo(getActivity());
		wfpJo = makeJsonObj.fingerPrintToJSON(getWFPNow);										// 本次wfp封装为JSON
	}
	
	// M09
	private void judgeTheInitialState () {
		if (isDBInitialReadyAndBadAPOK && isDBInitialReadyAndGoodAPOK) {
			isDBInitialReady = true;
			dbInitialedTimes = 1;
			isInitialed = true;
			SharedPreferences.Editor editor2 = mySharedPreferences.edit();
			editor2.putInt("sp_db_initial_ok", dbInitialedTimes);
			Log.i(TAG, "初始化过程成功，F1值已置为1");
			editor2.commit();
		}
		// F02
		if (isDBInitialReady) {
			localBadCheckIfRogueAP();
		}
	}
	
	// Other 10 AtTsinghua集成专用，获取Token并在Type=4
//	private void getAtTsinghuaTokenFromSP () {
//		SharedPreferences sharedPrefs = mContext.getSharedPreferences(
//				Constants.SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
//		String token=sharedPrefs.getString(Constants.TOKEN, "");
//		if(token.equals(""))
//			return;
//		try {
//			token = URLEncoder.encode(token, "UTF-8");
//		} catch (Exception e) {
//			// TODO: handle exception
//			e.printStackTrace();
//		}
//
//	}
	
	/** 
	 * ####################################################################
	 * 
	 * 多线程及相关方法
	 * 
	 * T00 - Handler (主线程调用黑/白名单库版本检测方法T01 T11, 其余多线程都由Handler调度 以下逻辑皆在此处完成)
	 * 
	 * T01 - 黑名单库版本检测(需要更新返回10 调用T02)
	 * T02 - 黑名单库数据请求(获取成功返回20 调用T03)
	 * T03 - 黑名单库数据更新(JSON解析 + DB删 + DB建 + DB增 + 成功与否都调用T04)
	 * T04 - 黑名单验证AP(有记录返回40为恶意AP 弹窗提示; 无记录返回41 List显示 + 调用T05; 出错返回42)
	 * 
	 * T11 - 白名单库版本检测(需要更新返回110  调用T12)
	 * T12 - 白名单库数据请求(获取成功返回120 调用T13)
	 * T13 - 白名单库数据更新(JSON解析 + DB删 + DB建 + DB增)
	 * T14 - 白名单验证AP(有记录返回140为合法AP List显示; 无记录返回141 List显示可疑; 出错返回142)
	 * 
	 * T05 - LastUploadFalseTimes检查 
	 * 	   - 有历史记录 返回50: 调用T06 + 调用T07
	 *	   - 无历史记录 返回51: 调用T06
	 * T06 - 本次wfp上传 (即Rogue AP信息远程检测请求 type=0 调用到了T20)
	 * 	   - 成功60: 
	 * 	   - 失败61: 调用T14白名单验证(本地离线验证) + 调用T08历史记录累加
	 * T07 - 历史wfp读取 + 本次wfp上传 (即Rogue AP信息远程检测请求 type=0 调用到了T20)
	 * 	   - 成功70: 调用T09历史记录清零
	 *	   - 失败71: 调用T08历史记录累加
	 * T08 - lastUploadFalseTimes自加(本次/历史上传失败)
	 * T09 - lastUploadFalseTimes清零(历史上传成功)
	 * T10 - 检测AP切换时的时间戳，若时间戳小于规定的数据刷新时间则提示用户当前无法发起检测
	 * T11 - 检测信息入库
	 * T20 - Type = 4 埋点信息上传
	 * T30 - 等待用多线程
	 * 
	 * ####################################################################
	 */
	
	// T00
		@SuppressLint("HandlerLeak")
		private Handler myHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				
				// 黑名单线程调度逻辑
				case 10:
					Log.i(TAG, "黑名单需要更新");
					requestBadAPData();							// 调用T02 请求黑名单数据
				break;
				case 11:
					Log.i(TAG, "黑名单无需更新");
				break;
				case 20:
					Log.i(TAG, "黑名单数据接收完成，正在更新黑名单");
					refreshBadAPData();							// 调用T03 更新黑名单数据
				break;
				case 21:
					Log.i(TAG, "黑名单数据接收失败！无法更新黑名单");
				break;
				case 30:
					Log.i(TAG, "黑名单数据更新完成！");
				break;
				case 300:
					Log.i(TAG, "初始化黑名单数据更新完成！");
					judgeTheInitialState();
				break;
				case 31:
					Log.i(TAG, "黑名单数据更新失败！！！");
				break;
				case 310:
					Log.i(TAG, "初始化黑名单数据更新失败！！！");
					judgeTheInitialState();
				break;
				case 40:
					// 提示
					mStatus[2][1] = "黑名单AP！非法AP！";
					netInfoAdapter.notifyDataSetChanged();
					// 弹窗
					new AlertDialog.Builder(getActivity())
						.setTitle("警告")
						.setMessage("您当连接了恶意AP，请不要此环境中进行账号密码操作，若您已输入，请到安全的环境中修改密码！")
						.setNegativeButton("好吧", null).show();
					// 换图
					isRogueAP = true;
					setFeelView();
				break;
				case 41:
					// 提示
					mStatus[2][1] = "非黑名单AP，正为您进一步验证AP合法性";
					netInfoAdapter.notifyDataSetChanged();
					uploadInitialFingerPrintToServer();			// 发起Type = 4埋点检测
					queryBadAPDataVer();
					queryGoodAPDataVer();						// 顺序带有白名单检测
				break;
				case 411:										// 非黑名单AP
					// 提示
					mStatus[2][1] = "初次检测非黑名单AP，正为您进一步验证AP合法性";
					netInfoAdapter.notifyDataSetChanged();
					// 初始化步骤之 顺序检测白名单
					if (isDBInitialReady) {
						Log.i(TAG, "初次验证白名单中 >>> ");
						localGoodCheckIfRogueAP();
					} else {
						Log.i(TAG, "白名单更新状态未完成，本次未验证白名单 >>> ");
					}
				break;
				
				// 白名单线程调度逻辑
				case 110:
					Log.i(TAG, "白名单需要更新");
					requestGoodAPData();						// 调用T12 请求白名单数据
				break;
				case 111:
					Log.i(TAG, "白名单无需更新");
					localGoodCheckIfRogueAP();					// 调用T14 本地查询白名单
				break;
				case 120:
					Log.i(TAG, "白名单数据接收完成，正在更新白名单");
					refreshGoodAPData();						// 调用T13 更新黑名单数据
				break;
				case 130:
					Log.i(TAG, "白名单数据更新完成！");
					localGoodCheckIfRogueAP();
				break;
				case 1300:
					Log.i(TAG, "初始化白名单数据更新完成！");
					judgeTheInitialState();
				break;
				case 131:
					Log.i(TAG, "白名单数据更新失败！！！");	
					localGoodCheckIfRogueAP();
				break;
				case 1310:
					Log.i(TAG, "初始化白名单数据更新失败！！！");
					judgeTheInitialState();
				break;
				case 140:										// 是白名单AP
					mStatus[2][1] = "是白名单AP，1分钟后您可手动发起权威检测";
					Log.w(TAG, "核实为白名单AP");	
					netInfoAdapter.notifyDataSetChanged();
					// 发起Type=3协同检测检测
					// ****************************** 可否试验延时发送？？？
//					checkSPAndUploadData();
				break;
				case 141:										// 非白名单AP
					mStatus[2][1] = "非白名单AP，1分钟后您可手动发起权威检测";
					Log.w(TAG, "非白名单AP");
					netInfoAdapter.notifyDataSetChanged();
					// 发起Type=3协同检测检测
					// ****************************** 可否试验延时发送？？？
//					checkSPAndUploadData();
				break;
				case 142:										// 非白名单AP
					mStatus[2][1] = "白名单检测出错啦~~~您可尝试手动发起测试";
					Log.i(TAG, "白名单检测出错！");
					netInfoAdapter.notifyDataSetChanged();
					// 发起Type=3协同检测检测
					// ****************************** 可否试验延时发送？？？
				break;
					
				// 历史记录查询 + 本次/历史上传分支
				case 50:														// 无历史记录 -- 本次分支
					getTheWfpThisTime();										// 调用T20获取本次指纹
					getMyToken();												// “净空”检测环节(历史干净 获取Token并上传本次指纹)
					upLoadInstantFingerPrintToServer();
				break;
				case 51:														// 有历史记录	-- 历史分支
					getTheWfpThisTime();										// 调用T20获取本次指纹
					Log.i(TAG, "开始上传历史FP >>>");
					uploadHistoryFingerPrintToServer();							// 开始上传历史指纹数据
				break;
					
				// 本次分支上传结果
				case 60:														// 本次wfp上传成功 -- 此时默认会启动AP协同检测
					Log.w(TAG, "协同检测模式开启！");
					mStatus[2][1] = "协同检测模式开启！";
				break;
				case 61:														// 本次wfp上传失败
					Log.e(TAG, "协同检测模式开启失败！！！");
					mStatus[2][1] = "协同检测模式失败，建议您稍后手动验证！";
					uploadFailSetFunc();										// 调用T08 历史计数器累加
					insertInsCheckDataToDB();
				break;
				
				// 历史分支上传结果
				case 70:														// 历史wfp上传成功后，历史记录清零，转入“净空”状态获取Token
					uploadFailResetFunc();										// 调用T09 历史计数器清零
					getMyToken();
					upLoadInstantFingerPrintToServer();
				break;
				case 71:														// 历史wfp上传失败，本次也不会通
					sendEmptyMessage(312);
				break;
				
				// 检查时间戳之后的上传调度
				case 1000:
					mStatus[2][1] = "为您发起查询，请查看推送结果（无结果请再发起一次）";
					netInfoAdapter.notifyDataSetChanged();
					checkLastUploadFalseTimes();
				break;
				case 1001:
					// 弹窗
					new AlertDialog.Builder(getActivity())
					.setTitle("提示")
					.setMessage("连接的AP发生改变，请您于 " + (60 - (newestTimeStamp - lastTimestamp)/1000) + " 秒后发起查询")
					.setNegativeButton("好吧", null).show();
					// 提示
					mStatus[2][1] = "请您稍后发起查询，将得到权威检测结果喔！";
					netInfoAdapter.notifyDataSetChanged();
				break;
					
				// Type = 3回传结果（List文字修正）
				case 3000:
					Log.w(TAG, "合法AP");
					isAPChanged = false;
					isRogueAP = false;
					refreshNetList();
					setFeelView();
					new AlertDialog.Builder(getActivity())
					.setTitle("权威验证提示")
					.setMessage("经过联网权威验证，您链接的AP是合法的校园网AP~!")
					.setNegativeButton("赞~", null).show();
					mStatus[2][1] = "经服务器验证为合法校园网AP~";
					netInfoAdapter.notifyDataSetChanged();
				break;
				case 3001:
					Log.w(TAG, "非法AP");
					isAPChanged = false;
					isRogueAP = true;
					setFeelView();
					new AlertDialog.Builder(getActivity())
					.setTitle("权威验证警告")
					.setMessage("您当前可能连接了恶意AP，请不要此环境中进行账号密码操作。若您已输入，请到安全的环境中修改密码！")
					.setNegativeButton("好吧", null).show();
					mStatus[2][1] = "您关联的AP是非法AP，请谨慎使用！若您输入过敏感信息，建议您在安全的网络环境下修改！";
					netInfoAdapter.notifyDataSetChanged();
				break;
				case 3002:
					Log.w(TAG, "AP检测时发生了错误");
					mStatus[2][1] = "检查出错，请稍后再试吧@_@";
					netInfoAdapter.notifyDataSetChanged();
					isAPChanged = false;
					isRogueAP = false;
					setFeelView();
					uploadFailSetFunc();
					insertInsCheckDataToDB();
				break;
				
				// Type =4实时回传结果（List文字修正）
				case 4000:
					Log.w(TAG, "Type=4合法AP");
					isAPChanged = false;
					isRogueAP = false;
					setFeelView();
					new AlertDialog.Builder(getActivity())
					.setTitle("提示")
					.setMessage("初步判断，您链接的AP是合法的校园网AP~")
					.setNegativeButton("赞~", null).show();
					mStatus[2][1] = "合法校园网AP~";
					netInfoAdapter.notifyDataSetChanged();
				break;
				case 4001:
					Log.w(TAG, "Type=4非法AP");
					isRogueAP = true;
					setFeelView();
					mStatus[2][1] = "请您1分钟后手动查询，将得到准确结果！";
					netInfoAdapter.notifyDataSetChanged();
				break;
				case 4002:
					Log.w(TAG, "Type=4可疑AP");
					isAPChanged = false;
					isRogueAP = false;
					setFeelView();
					new AlertDialog.Builder(getActivity())
					.setTitle("风险提示")
					.setMessage("您当前连接的是合法AP，但附近存在恶意AP，请务必注意~")
					.setNegativeButton("赞~", null).show();
					mStatus[2][1] = "您当前连接的是合法AP，但附近存在恶意AP，请务必注意~";
					netInfoAdapter.notifyDataSetChanged();
				break;
				case 4003:
					Log.w(TAG, "AP检测时发生了错误");
					mStatus[2][1] = "检查出错，请稍后手动尝试验证吧@_@";
					netInfoAdapter.notifyDataSetChanged();
					setFeelView();
					uploadFailSetFunc();
					insertInsCheckDataToDB();
				break;

				}
			}
		};

	// T01
	public void queryBadAPDataVer() {
		new Thread() {
			@Override
			public void run() {
				try {
					List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
					params.add(new BasicNameValuePair("jsondata", "{\"query_type\":\"RAP_UPDATE_TIME\"}"));
					Bundle httpRes = ControlHttpsUtil.HttpsPost("https://目标API", params);
					String httpResState = httpRes.getString("res");
					String httpResData =  httpRes.getString("data");
					if (httpResState.equals("ok")) {
						JSONObject badAPVersionJO = new JSONObject(httpResData);
						badAPDataVer = badAPVersionJO.getLong("RAP_UPDATE_TIME");
						Log.i(TAG, "黑名单AP库版本号获取：成功，版本号为：" + badAPDataVer);
						int isGoodAPDBVersionOK = dbMana.queryIfBadAPTableVersionOK(badAPDataVer);	
						switch (isGoodAPDBVersionOK) {						// query数据库版本号匹配情况
						case 0: 											// 调用T02获取数据
							myHandler.sendEmptyMessage(10);
							break;
						case 1: 											// 从表中找到了记录，不需更新 
							myHandler.sendEmptyMessage(11);
							break;
						}
					} else {
						Log.i(TAG, "黑名单AP库版本号获取：失败！网络传输出错！");
						myHandler.sendEmptyMessage(12);
					}
				} catch (Exception e) {
					e.printStackTrace();
					Log.i(TAG, "黑名单AP库版本号获取：未知错误！");
					myHandler.sendEmptyMessage(13);
				}
			}
		}.start();
	}
	
	// T02
	private void requestBadAPData() {
		new Thread() {
			@Override
			public void run() {
				try {
					List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
					params.add(new BasicNameValuePair("jsondata", "{\"query_type\":\"RAP_TABLE\"}"));
					Bundle httpRes = ControlHttpsUtil.HttpsPost("https://目标API", params);
					String httpResState = httpRes.getString("res");
					String httpResData =  httpRes.getString("data");
					if (httpResState.equals("ok")) {
						badAPDataJO = new JSONObject(httpResData);
						Log.i(TAG, "黑名单AP版库信息获取成功");
						myHandler.sendEmptyMessage(20);
					} else {
						myHandler.sendEmptyMessage(21);
						Log.i(TAG, "黑名单AP版库信息获取失败！内容为空");
					}
				} catch (Exception e) {
					e.printStackTrace();
					myHandler.sendEmptyMessage(21);
					Log.i(TAG, "黑名单AP版库信息获取失败！内容为：未知");
				}
			}
		}.start();
	}
	
	// T03
	private void refreshBadAPData() {
		new Thread() {
			@Override
			public void run() {
				try {
					// 解析回传JSON格式字符串
					List<ModelGoodBadAPData> badAPDatas = new ArrayList<ModelGoodBadAPData>();
					JSONArray jsonArray = new JSONArray(badAPDataJO.getString("RAP_TABLE"));

					for(int i = 0; i < jsonArray.length(); i++) {
					    JSONObject jsonObject = jsonArray.getJSONObject(i);
					    String gap_bssid = jsonObject.getString("rap_bssid");
					    Long gap_vercode = badAPDataVer;
					    ModelGoodBadAPData badAPData = new ModelGoodBadAPData(gap_vercode, gap_bssid); 																
					    badAPDatas.add(badAPData);
					}
					
					// Database删建增
					dbMana.deleteOldBadAPTable();
					dbMana.createNewBadAPDataToDB();
					boolean insertOK = dbMana.insertNewBadAPDataToDB(badAPDatas);
					
					// 更新后
					if ( isInitialed ) {							// F01
						if ( insertOK ) {
							myHandler.sendEmptyMessage(30);
						} else {
							myHandler.sendEmptyMessage(31);
						}
					} else {
						if ( insertOK ) {							// F02
							isDBInitialReadyAndBadAPOK = true;
							judgeTheInitialState();
							myHandler.sendEmptyMessage(300);
						} else {
							isDBInitialReadyAndBadAPOK = false;
							judgeTheInitialState();
							myHandler.sendEmptyMessage(310);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					if ( isInitialed ) {
						myHandler.sendEmptyMessage(31);
					} else {
						myHandler.sendEmptyMessage(310);
					}
				}
				
			}
		}.start();
	}
	
	// T04
	public void localBadCheckIfRogueAP() {
		new Thread() {
			@Override
			public void run() {
				try {
					int whatsTheAP = dbMana.queryIfBadAP(getWFPNow);
					
					switch (whatsTheAP) {
					case 0:	
						// Flow01 + Flow02
						// 注意！这里是找到了匹配记录，由于是黑名单，即是Rogue AP
						myHandler.sendEmptyMessage(40);
						break;
					case 1:
						Log.w(TAG, "不是黑名单AP");
						if (isDBInitialReady) {					// isDBInitialReady 仅在 F02中才会被置为true
							// Flow02
							myHandler.sendEmptyMessage(411);
						} else {
							// Flow01
							myHandler.sendEmptyMessage(41);
						}
						break;		
					case 2:
						myHandler.sendEmptyMessage(42);
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
					Log.i(TAG, "黑名单AP本地检测：未知错误！");
					myHandler.sendEmptyMessage(42);
				}
			}
		}.start();
	}
	
	// T11
	public void queryGoodAPDataVer() {
		new Thread() {
			@Override
			public void run() {
				try {
					List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
					params.add(new BasicNameValuePair("jsondata", "{\"query_type\":\"UPDATE_TIME\"}"));
					Bundle httpRes = ControlHttpsUtil.HttpsPost("https://目标API", params);
					String httpResState = httpRes.getString("res");
					String httpResData =  httpRes.getString("data");
					if (httpResState.equals("ok")) {
						JSONObject goodAPVersionJO = new JSONObject(httpResData);
						goodAPDataVer = goodAPVersionJO.getLong("UPDATE_TIME");
						Log.i(TAG, "白名单AP库版本号获取：成功，版本号为：" + goodAPDataVer);
						int isGoodAPDBVersionOK = dbMana.queryIfGoodAPTableVersionOK(goodAPDataVer);
						switch (isGoodAPDBVersionOK) {											// query数据库版本号匹配情况
						case 0:
							myHandler.sendEmptyMessage(110);
							break;
						case 1:
							myHandler.sendEmptyMessage(111);
							break;
						}
					} else {
						Log.i(TAG, "白名单AP库版本号获取失败！服务器未返回有效数据");
					}
				} catch (Exception e) {
					e.printStackTrace();
					Log.i(TAG, "白名单AP库版本号获取：未知错误！");
				}
			}
		}.start();
	}
	
	// T12
	private void requestGoodAPData() {
		new Thread() {
			@Override
			public void run() {
				try {
					List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
					params.add(new BasicNameValuePair("jsondata", "{\"query_type\":\"AP_TABLE\"}"));
					Bundle httpRes = ControlHttpsUtil.HttpsPost("https://目标API", params);
					String httpResState = httpRes.getString("res");
					String httpResData =  httpRes.getString("data");
					if (httpResState.equals("ok")) {
						goodAPDataJO = new JSONObject(httpResData);
						Log.i(TAG, "白名单AP版库信息获取成功!");
						myHandler.sendEmptyMessage(120);
					} else {
						myHandler.sendEmptyMessage(121);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	// T13
	private void refreshGoodAPData() {
		new Thread() {
			@Override
			public void run() {
				try {
					// 解析回传JSON格式字符串
					List<ModelGoodBadAPData> goodAPDatas = new ArrayList<ModelGoodBadAPData>();	// List专用于收纳解析出的各个以 ModelGoodAPData 为结构; goodAPData 为名义的记录
					JSONArray jsonArray = new JSONArray(goodAPDataJO.getString("AP_TABLE"));	// goodAPDataJO 转为jsonArray来遍历解析
					for(int i = 0; i < jsonArray.length(); i++) {
					    JSONObject jsonObject = jsonArray.getJSONObject(i);						// 为解析提供临时容器
					    String gap_bssid = jsonObject.getString("bssid");
					    Long gap_vercode = goodAPDataVer;
					    ModelGoodBadAPData goodAPData = new ModelGoodBadAPData(gap_vercode, gap_bssid); 																
					    goodAPDatas.add(goodAPData);											// 此次goodAPData 添加至List
					}													
					// Database删建增
					dbMana.deleteOldGoodAPTable();												// DB01 - 删除旧goodap_tb表
					dbMana.createNewGoodAPDataToDB();											// DB02 - 建立新goodap_tb表
					boolean insertOK = dbMana.insertNewGoodAPDataToDB(goodAPDatas);
					
					// 更新后
					if ( isInitialed ) {							// F01
						if ( insertOK ) {
							myHandler.sendEmptyMessage(130);
						} else {
							myHandler.sendEmptyMessage(131);
						}
					} else {
						if ( insertOK ) {							// F02
							isDBInitialReadyAndGoodAPOK = true;
							myHandler.sendEmptyMessage(1300);
						} else {
							isDBInitialReadyAndGoodAPOK = false;
							myHandler.sendEmptyMessage(1310);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					if ( isInitialed ) {
						myHandler.sendEmptyMessage(131);
					} else {
						myHandler.sendEmptyMessage(1310);
					}
				}
					
			}
		}.start();
	}
	
	// T14
	public void localGoodCheckIfRogueAP() {
		new Thread() {
			@Override
			public void run() {
				try {
					int whatsTheAP = dbMana.queryIfGoodAP(getWFPNow);
					Log.i(TAG, "验证是否为白名单AP中");
					switch (whatsTheAP) {
					case 0:
						myHandler.sendEmptyMessage(140);								// 白名单库内有记录
						break;
					case 1:
						myHandler.sendEmptyMessage(141);
						break;
					case 2:
						myHandler.sendEmptyMessage(142);
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
					Log.i(TAG, "白名单AP本地检测：未知错误！");
					myHandler.sendEmptyMessage(142);
				}
			}
		}.start();
	}
	
	// T05
	public void checkLastUploadFalseTimes() {	
		new Thread() {
			@Override
			public void run() {
				try {
					int tempInt01 = -1;
					mySharedPreferences = getActivity().getSharedPreferences("my_sp_instance", Context.MODE_PRIVATE);
					if (mySharedPreferences == null) { 
						lastUploadFalseTimes = 0;
						myHandler.sendEmptyMessage(50);
					} else {
						lastUploadFalseTimes = mySharedPreferences.getInt("sp_last_upload_false", tempInt01);
						if (lastUploadFalseTimes <= 0) {
							myHandler.sendEmptyMessage(50);
						} else {
							myHandler.sendEmptyMessage(51);
						}
					}
					Log.i(TAG, "历史上传失败检测：完成，历史记录条数 = " + lastUploadFalseTimes);
				} catch (Exception e) {
					e.printStackTrace();
					Log.i(TAG, "历史上传失败检测：失败！");
				}
			};
		}.start();
	}
	
	// T06
	public void upLoadInstantFingerPrintToServer() {
		mStatus[2][1] = "正在为您联网验证AP真伪！O(∩_∩)O";
		new Thread() {
			@Override
			public void run() {
				Log.i(TAG, "List FP开始上传");
				try {
					myHandler.sendEmptyMessage(60);
					
					List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
					String fpDataStr = wfpJo.toString(); 										// 准备好上传JSON(转为串)
					String sysTime = makeJsonObj.timestampToJson().toString(); 					// 准备好上传时间
					params.add(new BasicNameValuePair("msgType","{\"msgType\":3}"));			// *0,1,2分别代表3个Fragment(手动封装为JSON格式)
					params.add(new BasicNameValuePair("devToken", myTokenString));				// 封装Token
					params.add(new BasicNameValuePair("sysTime", sysTime));
					params.add(new BasicNameValuePair("fpData", fpDataStr));
					
					Log.w(TAG, "Type=3的上传数据为：>>> " + params.toString());
					
					Bundle httpRes = ControlHttpsUtil.HttpsPost("https://目标API", params);
					String httpResState = httpRes.getString("res");								// 获取回传内容的信息
					String httpResData = httpRes.getString("data");

					if (httpResState.equals("ok")) {
						// 获取服务器接口返回的实时鉴定结果（非推送）
						boolean insAPCheckRst;
						System.out.println("List FP上传请求：成功，Type=3回传数据 >>>" + httpResData.toString() );
						JSONObject recevJo = new JSONObject(httpResData);
						insAPCheckRst = recevJo.getBoolean("ap_validate");
						if (insAPCheckRst) {
							myHandler.sendEmptyMessage(3000);									
						} else {
							myHandler.sendEmptyMessage(3001);
						}
					} else {
						myHandler.sendEmptyMessage(3002);
					}
				} catch (Exception e) {
					e.printStackTrace();
					myHandler.sendEmptyMessage(3002);
				}
			}
		}.start();
	}
	
	// T07
	private void uploadHistoryFingerPrintToServer() {
		mStatus[2][1] = "正在为您联网验证AP真伪！";
		new Thread() {
			@Override
			public void run() {
				try {
					boolean historyFlag = false;
					int loopTimer = lastUploadFalseTimes;
					
					while (loopTimer > 0) {														// 循环取值并上传
						JSONObject historyJson = dbMana.queryHistoryWifiFingerPrintData(loopTimer);
						List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
						
						String fpDataStr01 = historyJson.getString("fpBig01");
						String fpDataStr02 = historyJson.getString("fpBig02");
						String fpDataStr03 = historyJson.getString("fpBig03");
						String sysTime = makeJsonObj.timestampToJson().toString();
						params.add(new BasicNameValuePair("msgType","{\"msgType\":0}"));
						params.add(new BasicNameValuePair("sysTime", sysTime));
						params.add(new BasicNameValuePair("devInfo", fpDataStr01));
						params.add(new BasicNameValuePair("ecdData", fpDataStr02));
						params.add(new BasicNameValuePair("fpData", fpDataStr03));
						Log.w(TAG, "Type=0的上传数据为： >>> " + params.toString());
						
						Bundle httpRes = ControlHttpsUtil.HttpsPost("https://目标API", params);
						String httpResState = httpRes.getString("res");
						String httpResData = httpRes.getString("data");
						Log.i(TAG, "此次forming接口返回内容：" + httpResData.toString());
						
						if (httpResState.equals("ok")) {
							Log.i(TAG, "历史倒数第"+ loopTimer + "次上传请求：成功");					// 历史倒数第一为“上次最后一条”
							successHistoryUpload++;
							historyFlag = true;
						} else {
							Log.i(TAG, "历史倒数第"+ loopTimer + "次上传请求：失败！");
							historyFlag = false;
							break;
						}
						loopTimer--;
					}
					
					if (historyFlag) {															// 符号位均无误视为全部上传了，且联网也正常
						myHandler.sendEmptyMessage(70);
					} else {
						myHandler.sendEmptyMessage(71);
					}
					
				} catch (Exception e) {
					e.printStackTrace();
					Log.i(TAG, "本次+历史FP上传请求：未知错误！");
					myHandler.sendEmptyMessage(71);
				}
			};
		}.start();
	}
	
	// T08
	private void uploadFailSetFunc() {
		mySharedPreferences = getActivity().getSharedPreferences("my_sp_instance", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = mySharedPreferences.edit();
		editor.putInt("sp_last_upload_false", lastUploadFalseTimes + 1);
		editor.commit();
	}
	
	// T09
	private void uploadFailResetFunc() {
		mySharedPreferences = getActivity().getSharedPreferences("my_sp_instance", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = mySharedPreferences.edit();
		int tempInt = -99;
		int lastFalseTimes = mySharedPreferences.getInt("sp_last_upload_false", tempInt);
		if (lastFalseTimes >= 0 && lastUploadFalseTimes >= successHistoryUpload) {
			editor.putInt("sp_last_upload_false", lastUploadFalseTimes - successHistoryUpload);	// 历史上传出错数 - 本次成功上传的次数
			editor.commit();
		} else {
			Log.i(TAG, "重置lastUploadFalseTimes时出现错误！");
		}
	}
	
	// T10
	public void checkSPAndUploadData() {
		mySharedPreferences = getActivity().getSharedPreferences("my_sp_instance", Context.MODE_PRIVATE); 
		if (mySharedPreferences == null || (mySharedPreferences.getLong("sp_ap_changed_timestamp", 99) == 99 )) {														// 为空则初始化sharedPreferences 并为 sp_start_times 启动次数赋值
			SharedPreferences.Editor editor = mySharedPreferences.edit();
			editor.putLong( "sp_ap_changed_timestamp", System.currentTimeMillis() );
			editor.commit();
			Log.i(TAG, "未读取到AP切换时间戳，以本次时间戳为准");
		} else {
			lastTimestamp = mySharedPreferences.getLong("sp_ap_changed_timestamp", 99);
			newestTimeStamp = System.currentTimeMillis();
			if ( (newestTimeStamp - lastTimestamp) >= ModelConfiguration.getLegalCheckTime() ) {
				Log.i(TAG, "准许发起查询");
				myHandler.sendEmptyMessage(1000);
			} else {
				Log.i(TAG, "不允许发起查询");
				myHandler.sendEmptyMessage(1001);
			}
		}
	}
	
	// T11
	private void insertInsCheckDataToDB() {
		new Thread() {
			@Override
			public void run() {
				try {
					dbMana.createUserDBbyHelper(getActivity(), "dwfdb"); 			// 数据库存储
					dbMana.insertWifiFingerPrintToDB(errorCodeEntity, getWFPNow);
					dbMana.insertAPScanInfoToDB(getWFPNow);
					Log.i(TAG, "本次保存了上传失败的FP");
				} catch (Exception e) {
					e.printStackTrace();
					Log.i(TAG, "本保存上传FP出错！");
				}
			}
		}.start();
	}

	// T20
	private void uploadInitialFingerPrintToServer() {
		new Thread() {
			@Override
			public void run() {
				try {
					JSONObject initialJson = makeJsonObj.initFingerPrintDataToJson(getWFPNow);
					List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
					JSONObject atTsinghuaTokenJo = new JSONObject();
					atTsinghuaTokenJo.put("devTokenJo", atTsinghuaTokenString);
					String fpDataStr01 = initialJson.getString("fpBig01");
					String fpDataStr02 = initialJson.getString("fpBig02");
					String sysTime = makeJsonObj.timestampToJson().toString();
					params.add(new BasicNameValuePair("msgType","{\"msgType\":4}"));
					params.add(new BasicNameValuePair("devToken", atTsinghuaTokenJo.toString()));
					params.add(new BasicNameValuePair("sysTime", sysTime));
					params.add(new BasicNameValuePair("devInfo", fpDataStr01));
					params.add(new BasicNameValuePair("fpData", fpDataStr02));
					Log.w(TAG, "Type=4的上传数据为： >>> " + params.toString());
					Bundle httpRes = ControlHttpsUtil.HttpsPost("https://目标API", params);
					String httpResState = httpRes.getString("res");
					String httpResData = httpRes.getString("data");
					Log.i(TAG, "Type=4的接口返回内容：" + httpResData.toString());
					if (httpResState.equals("ok")) {
						int insAPCheckRst;
						JSONObject recevJo = new JSONObject(httpResData);
						insAPCheckRst = recevJo.getInt("ap_validation");
						if (insAPCheckRst == 0) {
							myHandler.sendEmptyMessage(4000);									
						}
						if (insAPCheckRst == 1) {
							myHandler.sendEmptyMessage(4001);									
						}
						if (insAPCheckRst == 2) {
							myHandler.sendEmptyMessage(4002);									
						}
					} else {
						myHandler.sendEmptyMessage(4003);
						}
					
				} catch (Exception e) {
					e.printStackTrace();
					Log.e(TAG, "Type=4上传出错！！！");
					myHandler.sendEmptyMessage(212);
					}
				};
		}.start();
	}
	
	// T30 
//	private void waitingForGood() {
//		new Thread() {
//			@Override
//			public void run() {
//				super.run();
//				try {
//					sleep(5000);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//		}.start();
//	}
	
}

