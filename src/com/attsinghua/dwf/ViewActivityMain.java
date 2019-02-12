package com.attsinghua.dwf;

/*
 * 程序主View
 * 提供最上部的三个Tab选项卡和菜单
 * 进行程序初始化(布局创建+表创建+表数据导入+默认Ping外网网关设定)
 * 
 */

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.json.JSONException;
import org.json.JSONObject;
import com.attsinghua.socketservice.SocketService;
import com.attsinghua.socketservice.connection.CommunicationProtocol;
import com.thu.wlab.dwf.R;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class ViewActivityMain extends Activity implements OnClickListener {

	private static final String TAG = "MainActivity";
	private static Context ctx;
	private static SharedPreferences mySharedPreferences;
	@SuppressWarnings("unused")
	private static ModelConfiguration cfg;
    private ViewFragmentNetList netListFragment;   
    private ViewFragmentInstantCheck instantCheckFragment;  
    private ViewFragmentResourceForecast resourceForecastFragment;
	private static ControlGlobalAPChangeMsg cReceiver = null;
	private static ControlAPChangeBroadcastReceiver cReceiver2 = null;
    private static ControlDBMana dbMana = new ControlDBMana();
    private static FragmentManager fragmentManager;
	private static WifiManager wifiMana;
	private static WifiInfo wInfo;
	private static SocketService mBoundSocketService = null;
    private static View netListLayout;
    private static View instantCheckLayout;  
    private static View resourceForecastLayout;
    private static TextView btnTextView01; 
    private static TextView btnTextView02; 
    private static TextView btnTextView03;
	private static int startTimes;
	private static int lastUploadFalseTimes;
	private static Handler mHandler;
	private static boolean mIsSocketServiceBound = false;
	private static String myTokenStr = null;
	private static JSONObject myTokenJo = new JSONObject();
	private static PushMsgBroadcastReceiver mReceiver = null;
	private static int isJustOpenApp;
	private static long exitTime = 0;
	private int ppcValue;
	
    /**
     * ####################################################################
     * 
     * 初始化
     * 
     * A01 - Activity onCreate()
     * 		 A01-0  配置文件初始化
     * 		 A01-1  程序的初始化  根据mySharedPreferences存储的sp_start_times判断程序是否初始化
	 *			   1.初始化时重设sp_start_times为1; 
	 *			   2.初始化时重设lasttime_upload_false为0次;
	 *			   3.新建Database; 
	 *			   4.导入goodap_table表数据 
	 * 		 A01-2  布局初始化
	 * 		 A01-3 FragmentManager及各个Fragment状态初始化
	 * 		 A01-4  绑定Socket服务与获取推送服务需要的设备唯一Token(注意：Token上传时先要使用UTF-8编码，否则服务器会因为特殊字符的原因导致推送失败)
	 * 
     * A02 - onStart
     * A03 - onStop
     * A04 - onDestroy unbind if you have bound
     * A05 - onCreate 调用的界面初始化方法 Fragment及View中各元素init
     * A06 - onCreateOptionsMenu ActionBar顶部菜单init - 调用A05
     * A07 - onOptionsItemSelected Activity全按钮点击定义(含三个Tab的显示/隐藏方法调用)
     * 	   - 监听到ActionBar的检测按钮被点击 - 调用T02
     * A08 - onClick事件监听 - 调用M01(含快速点击测试)
     * 
     * ####################################################################
     */
	
    // A01
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ctx = this;
		// A01-0
		cfg = new ModelConfiguration(ctx);
		
		// A01-1 
		mySharedPreferences = 																	// 启动时读取sharedPreferences
				getSharedPreferences("my_sp_instance", Activity.MODE_PRIVATE); 
		if (mySharedPreferences == null) {														// 为空则初始化sharedPreferences 并为 sp_start_times 启动次数赋值
			Log.i(TAG, "程序需要初始化");
			SharedPreferences.Editor editor = mySharedPreferences.edit();						// sp_start_times值的存储
			editor.putInt("sp_start_times", 1);
			editor.putInt("sp_last_upload_false", 0);
			editor.commit();
		} else {
			startTimes = mySharedPreferences.getInt("sp_start_times", 0);
			lastUploadFalseTimes = mySharedPreferences.getInt("sp_last_upload_false", -999);
			Log.i(TAG, "此次启动读到的startTimes：" + startTimes + " last_upload_false:" + lastUploadFalseTimes);
			
			if ( startTimes <= 1) {																// 初次启动时创建Database并动态插入记录
				dbMana.createUserDBbyHelper(this, "dwfdb");
		        Log.i(TAG, "本地数据库初始化完成！startTimes：" + startTimes);
			} else {
				Log.i(TAG, "App已初始化过，无需再次初始化。当前startTimes：" + startTimes);
			}
			startTimes++;																		// sp_start_times 自增后存储
			SharedPreferences.Editor editor = mySharedPreferences.edit();
			editor.putInt("sp_start_times", startTimes);
			editor.commit();
		}
		
		// A01-2 
		setContentView(R.layout.activity_dwf_main);												// 初始化整体布局
		ActionBar actionBar = getActionBar();													// 初始化菜单布局(调用自定义方法)
		if(actionBar != null){
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setDisplayShowTitleEnabled(true);
		}
        initViews();																			// 初始化各个Fragment布局(调用自定义方法)
        
		// A01-3 
        fragmentManager = getFragmentManager();													// 初始化Fragment管理器
        setTabSelection(0);																		// 启动App默认选中第0个Fragment
        
        // A01-4
        mHandler = new Handler();																// 拿Token的准备01 -- 与 SocketService 绑定进而获取
		startService(new Intent(getApplicationContext(), SocketService.class));					// 拿Token的过程01 -- 启动 Service (程序初始启动时就需要拿Token，也就这个方法必须放在onCreate()方法中)
		doBindSocketService();																	// 拿Token的过程02 -- 与 SocketService 绑定进而获取
		preventFromDoNotHaveMyToken();															// 拿Token的准备02 -- 调用 T03 为了防止没绑定就取Token导致报错，这里进行了一个等待
		
		setIsJustOpenApp(1);
	}
	
	// A02
	@Override
	protected void onStart() {																	// onStart的同时动态注册上BroadcastReceiver
		super.onStart();
		if(mReceiver == null) {
			mReceiver = new PushMsgBroadcastReceiver();
			IntentFilter pushMsgFilter = new IntentFilter(SocketService.ACTION_PUSH_MESSAGE_RECEIVED);
			pushMsgFilter.setPriority(10);
			registerReceiver(mReceiver, pushMsgFilter);
		}
		if (cReceiver == null) {
			cReceiver = new ControlGlobalAPChangeMsg();
			IntentFilter pushMsgFilter02 = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
			registerReceiver(cReceiver, pushMsgFilter02);
		}
		if (cReceiver2 == null) {
			cReceiver2 = new ControlAPChangeBroadcastReceiver();
			IntentFilter pushMsgFilter03 = new IntentFilter(WifiManager.RSSI_CHANGED_ACTION);
			pushMsgFilter03.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
			registerReceiver(cReceiver2, pushMsgFilter03);
		}
	}
	
	// A03
	@Override
	protected void onStop() {																	// onStop的同时注销掉BroadcastReceiver
		super.onStop();
		if(mReceiver != null) {
			unregisterReceiver(mReceiver);
			mReceiver = null;
		}
		setIsJustOpenApp(getIsJustOpenApp() + 1);
	}
	
	// A04
	@Override
	protected void onDestroy() {
		super.onDestroy();
		doUnbindSocketService();
		setIsJustOpenApp(1);
		if (cReceiver != null) {
			unregisterReceiver(cReceiver);
		}
		if (cReceiver2 != null) {
			unregisterReceiver(cReceiver2);
		}
		try {
			dbMana.finalize();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	// A05
	private void initViews() {
		netListLayout = findViewById(R.id.netlist_btnlayout);									// 顶部按钮部分布局
	    instantCheckLayout = findViewById(R.id.inscheck_btnlayout);
	    resourceForecastLayout = findViewById(R.id.forecast_btnlayout);
	    btnTextView01 = (TextView) findViewById(R.id.netlist_btntext);
	    btnTextView02 = (TextView) findViewById(R.id.inscheck_btntext);
	    btnTextView03 = (TextView) findViewById(R.id.forecast_btntext);
	    netListLayout.setOnClickListener(this);													// 设置顶部按钮监听器
	    instantCheckLayout.setOnClickListener(this);
	    resourceForecastLayout.setOnClickListener(this);
	}
	
	// A06
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.dwfmain, menu);
		return true;
	}
	
	// A07
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();
		switch (id) {
		
		case android.R.id.home:
			this.finish();
			System.exit(0);
		break;
		
		case R.id.actionbar_btn_settings:
			Log.i(TAG, "设置界面启动");
			Intent intent02 = new Intent();
			ComponentName cp02 = new ComponentName(this,
					ViewActivitySettings.class);
			intent02.setComponent(cp02);
			startActivity(intent02);
		break;
		
		case R.id.actionbar_btn_ppchecker:
			Log.i(TAG, "ppchecker界面启动");
			Intent intent03 = new Intent();
			ComponentName cp03 = new ComponentName(this, ViewActivityPPCounter.class);
			intent03.setComponent(cp03);
			startActivity(intent03);
		break;

		case R.id.actionbar_btn_rogue_ap_check:
			
			if ( !ModelConfiguration.getIsCurrentSSIDLegal(netListFragment.getWFPNow.getLinkSSID()) ) {
				new AlertDialog.Builder(ctx)
				.setTitle("无法联网验证AP真伪")
				.setMessage("只能为关联的校园网AP鉴定真伪喔")
				.setPositiveButton("好吧", null)
				.show();
			} else {
				Log.i(TAG, "询问用户发送检测指纹");
				new AlertDialog.Builder(ctx)
					.setTitle("联网验证AP真伪")
					.setMessage("确认后，将同校园网内的安全服务器联合进行真伪AP验证，结果会以推送的方式发送给您。")
					.setPositiveButton("走一个", new DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog, int whichButton) {
					if (ModelPressInterval.isFastRequest()) { 										// 防止用户快速点击  Toast + AlertDialog
						Toast.makeText(ctx, "每3秒内只能发起一次请求啦~", Toast.LENGTH_SHORT).show();
						new AlertDialog.Builder(ctx)
						.setTitle("温馨小提示")
						.setMessage("频繁发起检测会让其他同学也没法用网的TAT...稍稍等一下吧~")
						.setNegativeButton("好吧", null)
						.show();
					} else {
						// 调用Fragment中的方法检测
						netListFragment.checkSPAndUploadData();
						}
					}
				}).setNegativeButton("算了吧", null)
				.show();
			}
		break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	// A08
	@Override
	public void onClick(View v) {
		if (ModelPressInterval.isFastDoubleClick()) {											// 防止用户快速点击
			Toast.makeText(this, "点太快不是好孩子_(:з」∠)_呐,等1.5秒再点了啦~", Toast.LENGTH_SHORT).show();
			return;
		}
		switch (v.getId()) {
		
		case R.id.netlist_btnlayout:
			setTabSelection(0);
		break;
		
		case R.id.inscheck_btnlayout:
			setTabSelection(1);
		break;
		
		case R.id.forecast_btnlayout:
			setTabSelection(2);
		break;
		
		case R.id.netlist_refresh_btn:															// ListFragment中按钮的监听
			netListFragment.refreshNetList();
			Toast.makeText(ctx, "列表数据刷新啦", Toast.LENGTH_SHORT).show();
			Log.i(TAG, "列表数据刷新啦");
		break;
		
		default:
		break;
		
		}
	}

	/**
     * ####################################################################
     * 
     * 多线程方法
     * T01 - Handler 多线程没有获取到Token时进行一个设定
     * T02 - 为防止没绑定就取Token导致报错，这里进行了一个等待
     * 
     * ####################################################################
     */
	
	// T01
	@SuppressLint("HandlerLeak")
	private Handler myHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 404:
			break;		
			}
		}
	};
	
	// T02
	private void preventFromDoNotHaveMyToken() {
		new Thread() {
			@Override
			public void run() {
				super.run();
				for(int i = 0; i < 10; i++) {													// 拿Token的准备02 --为了防止没绑定就取Token导致报错，这里进行了一个等待
					if(mBoundSocketService != null) {
						mBoundSocketService.getPushServiceDeviceToken(mHandler, mGetTokenCallback);
						return;
					} else {
						try {
							sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}.start();
	}

	
	/**
     * ####################################################################
     * 
     * 主线程方法
     * 
     * M01 - 选项卡监听点击序号后：显示目标Tab + 执行M02 + M03
     * M02 - 选项卡切换时，隐藏当前选择Tab
     * M03 - 选项卡切换时，清除当前选择内容
     * M04 - 绑定 push socket service 来获取设备 Token 并存入 SP
     * M05 - socket service 绑定关系(绑定/解绑)
     * M06 - doBindSocketService() - M05 的辅助方法
     * M07 - doUnbindSocketService() - M05 的辅助方法
     * M08 - 动态 BroadcastReceiver01 推送广播接收器定义
     * M09 - 1 动态 BroadcastReceiver01 消息处理方法1 - 恶意AP告警/提示
     * M09 - 2 动态 BroadcastReceiver01 消息处理方法2 - 附近有恶意AP提示
     * M09 - 3 动态 BroadcastReceiver01 消息处理方法3 - AP推荐
     * M09 - 4 动态 BroadcastReceiver01 消息处理方法4 - 校园网通知
     * M10 - 动态 BroadcastReceiver02 AP切换广播接收器定义
     * M11 - 动态 BroadcastReceiver02 消息处理方法 - 打时间戳及刷新列表
     * M12 - 按返回/退出键的响应
     * 
     * ####################################################################
     */
	
	// M01
	private void setTabSelection(int i) {
        clearSelection();  																		// 每次选中之前先清楚掉上次的选中状态  
        FragmentTransaction transaction = fragmentManager.beginTransaction();					// 开启一个Fragment事务  
        hideFragments(transaction);																// 先隐藏掉所有的Fragment,以防止有多个Fragment显示在界面上的情况 
        switch (i) {																			// 判断点击的Tab
        case 0:
        	netListLayout.setBackgroundColor(getResources().getColor(R.color.lightgrey));		// 当点击了消息tab时,改变控件的图片和文字颜色//(R.drawable.top_btn_bar_btnbgd_pd);
        	btnTextView01.setTextColor(Color.WHITE);
            if (netListFragment == null) {														// 如果listFragment为空,则创建一个并添加到界面上
            	netListFragment = new ViewFragmentNetList();  
                transaction.add(R.id.content, netListFragment);  
            } else {																			// 如果MessageFragment不为空, 则直接将它显示出来. 注意, 如果不加hide的话,在Android 5.0 下面,会产生图层重叠覆盖的效果(对于没有指定Layout背景色的尤为严重)
                transaction.show(netListFragment);
            }
            break;
        case 1:
        	instantCheckLayout.setBackgroundColor(getResources().getColor(R.color.lightgrey)); 
            btnTextView02.setTextColor(Color.WHITE);
            if (instantCheckFragment == null) {   
            	instantCheckFragment = new ViewFragmentInstantCheck();
                transaction.add(R.id.content, instantCheckFragment); 
            } else {  
                transaction.show(instantCheckFragment);
            }
            break;
        case 2:  
            resourceForecastLayout.setBackgroundColor(getResources().getColor(R.color.lightgrey)); 
            btnTextView03.setTextColor(Color.WHITE);
            if (resourceForecastFragment == null) {    
            	resourceForecastFragment = new ViewFragmentResourceForecast();  
                transaction.add(R.id.content, resourceForecastFragment);  
            } else {    
                transaction.show(resourceForecastFragment);
            }  
            break;
        }
        transaction.commit(); 
	}

	// M02
	private void hideFragments(FragmentTransaction transaction) {
		if (netListFragment != null) {  
            transaction.hide(netListFragment);  
        }  
        if (instantCheckFragment != null) {  
            transaction.hide(instantCheckFragment);  
        }  
        if (resourceForecastFragment != null) {  
            transaction.hide(resourceForecastFragment);  
        } 
	}
	
	// M03
	private void clearSelection() {
		netListLayout.setBackgroundColor(getResources().getColor(R.color.white));
		instantCheckLayout.setBackgroundColor(getResources().getColor(R.color.white));  
		resourceForecastLayout.setBackgroundColor(getResources().getColor(R.color.white));
		btnTextView01.setTextColor(Color.parseColor("#6d6e6d"));
		btnTextView02.setTextColor(Color.parseColor("#6d6e6d"));
		btnTextView03.setTextColor(Color.parseColor("#6d6e6d"));
		}
	
	// M04
	private CommunicationProtocol.ResponseCallback mGetTokenCallback = new CommunicationProtocol.ResponseCallback() {
		@Override
		public void onSuccess(String msg) {
			
			mySharedPreferences = getSharedPreferences("my_sp_instance", Activity.MODE_PRIVATE);					
			try {
				JSONObject jo = new JSONObject(msg);
				myTokenStr = jo.getString("token");
				myTokenStr = URLEncoder.encode(myTokenStr, "UTF-8");
				Log.i(TAG, "获取到的Token字符串:" + myTokenStr);														
				
				myTokenJo.put("devToken", myTokenStr);											// Token获取成功后，准备好检查上传的Token(封装为JSON)
				
				SharedPreferences.Editor editor = mySharedPreferences.edit();					// 更新SP中的Token
				editor.putString("sp_device_token", myTokenStr);
				editor.commit();
				Log.i(TAG, "已更新SP Token");
				
			} catch (JSONException e) {
				e.printStackTrace();
				myHandler.sendEmptyMessage(404);
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
				myHandler.sendEmptyMessage(404);
			}
		}
		
		@Override
		public void onFail(String failCode, String failDesc) {
			
			netListFragment.mStatus[2][1] = "获取Token失败！暂无法验证AP真伪";
			myHandler.sendEmptyMessage(404);
			
			mySharedPreferences = getSharedPreferences("my_sp_instance", Activity.MODE_PRIVATE);
			String defValue = new String();
			myTokenStr = mySharedPreferences.getString("sp_device_token", defValue);			// Token获取失败后，尝试读取SP中的Token
			if (myTokenStr.isEmpty()) {
				Log.i(TAG, "之前Token记录为空！此次启动后将没有有效Token！");
				myTokenStr = "NO_TOKEN";
			} else {
				Log.i(TAG, "之前的Token记录为：" + myTokenStr);
			}
			
			SharedPreferences.Editor editor = mySharedPreferences.edit();
			editor.putString("sp_device_token", null);
			editor.commit();
		}
	};
		
	// M05
	private ServiceConnection mSocketServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBoundSocketService = null;
			Log.d(TAG, "SocketService unbound 解除绑定！");
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBoundSocketService = ((SocketService.LocalBinder) service).getService();
			Log.d(TAG, "SocketService bound 已经绑定！");
		}
	};
	
	// M06
	private void doBindSocketService() {
		bindService(new Intent(getApplicationContext(), SocketService.class), mSocketServiceConnection, Context.BIND_AUTO_CREATE);
		mIsSocketServiceBound = true;
	}
	
	// M07
	private void doUnbindSocketService() {
		if (mIsSocketServiceBound) {
			Log.d(TAG, "unbinding SocketService ...");
			unbindService(mSocketServiceConnection);
			mIsSocketServiceBound = false;
			}
	}
	
	// M08
	private class PushMsgBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				JSONObject jo = new JSONObject(intent.getStringExtra(SocketService.BROADCAST_INTENT_EXTRA_PUSH_MSG_CONTENT));
				
				Log.i(TAG, "MainActivity广播接收器收到的推送: ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~" + jo.toString());
				String msgType = jo.getString("type");
				// 调试时的推送
				// 目前接口推送格式 {"message":"{ap_validate:true}","author":"AtTsinghua","title":"{ap_validate:true}","type":"通知信息","uri":"YES"}
				if( msgType.equalsIgnoreCase("通知信息")) {
					handlePushMessage1(jo);
				}
				// 生产时的推送
//				if( msgType.equalsIgnoreCase(ModelConfiguration.PUSH_MSG_TYPE_CHECK_RESULT)) {							// 可根据推送类型调用同的弹窗方法
//					handlePushMessage1(jo);																				// 调用M09 弹Toast
//				}
//				if ( msgType.equalsIgnoreCase(ModelConfiguration.PUSH_MSG_TYPE_NEAR_WARNING) ) {
//					handlePushMessage2(jo);
//				}
//				if ( msgType.equalsIgnoreCase(ModelConfiguration.PUSH_MSG_TYPE_AP_RECOMMEND) ) {
//					handlePushMessage3(jo);
//				}
//				if ( msgType.equalsIgnoreCase(ModelConfiguration.PUSH_MSG_TYPE_INFO) ) {
//					handlePushMessage4(jo);
//				}
				abortBroadcast();																// 在主界面状态下，收到通知时会推出这个动态注册的 Activity 中的通知，不会在通知栏中出现(避免重复通知)
			} catch (JSONException e) {
				Log.e(TAG, "PushMsgBroadcastReceiver:onReceive(): parse push msg failed");
				e.printStackTrace();
			}
		}
	}
	
	// M09 - 1
	void handlePushMessage1(JSONObject jo_msg) throws JSONException {
		// 获取服务器接口返回的实时鉴定结果 json解析出来 默认为true -- 非Rouge AP
		boolean insAPCheckRst = false;
		JSONObject recevJo = new JSONObject(jo_msg.getString("message"));
		insAPCheckRst = recevJo.getBoolean("ap_validate1");	
		if (insAPCheckRst) {
			new AlertDialog.Builder(this)
				.setTitle("权威验证提示")
				.setMessage("经过联网权威验证，您链接的AP是合法的校园网AP~!")
				.setNegativeButton("赞~", null).show();
			netListFragment.mStatus[2][1] = "经服务器验证为合法校园网AP~";
			netListFragment.setAPChanged(false);
			netListFragment.isRogueAP = false;
		} else {
			new AlertDialog.Builder(this)
				.setTitle("权威验证警告")
				.setMessage("您当前可能连接了恶意AP，请不要此环境中进行账号密码操作。若您已输入，请到安全的环境中修改密码！")
				.setNegativeButton("好吧", null).show();
			netListFragment.mStatus[2][1] = "很可能是山寨的校园网AP，建议不要输入任何敏感信息！";
			netListFragment.setAPChanged(false);
			netListFragment.isRogueAP = true;
		}
		netListFragment.refreshNetList();
	}
	
	// M09 - 2
	void handlePushMessage2(JSONObject jo_msg) throws JSONException {
		boolean APWaring = true;
		JSONObject recevJo = new JSONObject(jo_msg.getString("message"));
//		APWaring = recevJo.getBoolean("ap_around_warning");
		APWaring = recevJo.getBoolean("ap_around_warning");
		if (APWaring) {
			new AlertDialog.Builder(this)
			.setTitle("小提示")
			.setMessage("当前关联的AP附近可能存在恶意AP，使用校园网时建议先检测喔~")
			.setNegativeButton("知道了", null).show();
			netListFragment.setAPChanged(false);
			netListFragment.isRogueAP = false;
		}
	}
	
	// M09 - 3
	void handlePushMessage3(JSONObject jo_msg) throws JSONException {
		JSONObject recevJo = new JSONObject(jo_msg.getString("message"));
		new AlertDialog.Builder(this)
		.setTitle("AP优选推荐")
//		.setMessage(recevJo.getString("ap_recommend"))
		.setMessage(recevJo.getString("在您附近存在更优的AP可供选择，若您需要改善无线网用网体验，建议到F2使用。"))
		.setNegativeButton("知道了", null).show();
	}
	
	// M09 - 4
	void handlePushMessage4(JSONObject jo_msg) throws JSONException {
		JSONObject recevJo = new JSONObject(jo_msg.getString("message"));
		new AlertDialog.Builder(this)
		.setTitle("校园网通知")
//		.setMessage(recevJo.getString("thu_broadcast"))
		.setMessage(recevJo.getString("图书馆F3的AP准备调试重启，请您稍后再连接，谢谢！"))
//		.setMessage(recevJo.getString("今日夜间24:00到次日1:30，图书馆AP将重启，届时无法使用无线网，给您带来不便敬请谅解。"))
		.setNegativeButton("知道了", null).show();
	}
	
	// M10
	public class ControlAPChangeBroadcastReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			try {	
				handleWifiChangeMessage(context, intent);
			} catch (Exception e) {
				Log.e(TAG, "ControlAPChangeBroadcastReceiver:onReceive(): parse push msg failed");
				e.printStackTrace();
			}		
		}
	}
		
	// M11 
	void handleWifiChangeMessage(Context context, Intent intent) {
			
		// 信号变化时刷新列表
		if (intent.getAction().equals(WifiManager.RSSI_CHANGED_ACTION)) {
			netListFragment.refreshNetList();
			Log.i(TAG, "信号强度发生改变" );
		}

		// Wifi变化时，判定BSSID的变化进而给出是否允许发起验证的提示
		// 并且评价图表也进行更新
		if ( intent.getAction().equalsIgnoreCase(WifiManager.NETWORK_STATE_CHANGED_ACTION) ){
//			Toast.makeText(context, intent.getAction(), Toast.LENGTH_LONG).show();
			Log.i(TAG, "666:" + WifiManager.EXTRA_BSSID.toString() );
			setPPCounter(1);
			netListFragment.mStatus[2][1] = "WiFi连接状态发生改变，若已关联AP建议手动验证其真伪~~~";
			netListFragment.isRogueAP = false;
			netListFragment.setAPChanged(true);
			netListFragment.refreshNetList();
			resourceForecastFragment.uploadKeyAPAndGetRatingData();
		}
		
	}
	
	// M12
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){   
	        if((System.currentTimeMillis()-exitTime) > 3000){  
	            Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();                                
	            exitTime = System.currentTimeMillis();   
	        } else {
	            finish();
	            System.exit(0);
	        }
	        return true;   
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	// M13 ******
	public void setPPCounter(int i) {
		SharedPreferences mySharedPreferencesF = getSharedPreferences("my_sp_instance", Activity.MODE_PRIVATE);																	
		SharedPreferences.Editor editor = mySharedPreferencesF.edit();
		if (i == 0) {
			ppcValue = 0;
			editor.putInt("sp_ppcounter", 0);
		} else if (i == 1) {
			int j = mySharedPreferencesF.getInt("sp_ppcounter", 0);
			ppcValue = j + 1;
			editor.putInt("sp_ppcounter", ppcValue);
		}
		editor.commit();
	}
	
	/**
     * ####################################################################
     * 
     * getter & setter
     * 
     * ####################################################################
     */
	public WifiManager getWifiMana() {
		return wifiMana;
	}

	public void setWifiMana(WifiManager wifiMana) {
		ViewActivityMain.wifiMana = wifiMana;
	}

	public WifiInfo getwInfo() {
		return wInfo;
	}

	public void setwInfo(WifiInfo wInfo) {
		ViewActivityMain.wInfo = wInfo;
	}

	public int getIsJustOpenApp() {
		return isJustOpenApp;
	}

	public void setIsJustOpenApp(int isJustOpenApp) {
		ViewActivityMain.isJustOpenApp = isJustOpenApp;
	}
	
}	
