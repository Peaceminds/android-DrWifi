package com.attsinghua.dwf;

/*
 * 本类是"用户设置"的View
 */

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import com.thu.wlab.dwf.R;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ViewActivitySettings extends Activity implements OnClickListener{
	
	private static final String TAG = "ViewActivitySettings";
	private Context ctx;
	private Button cleanBtn;
	private Button manualBtn;
	private Button checkVerBtn;
	private Button pingSetBtn;
	// private Button tsukkomiBtn;																// 目前隐藏了吐槽按钮和功能
    private ProgressDialog myProgressDialog;
    private EditText destIP;
	private Long badAPDataVer;
	private JSONObject badAPDataJO;
	private ControlDBMana dbMana;
	
	/**
	 * ##########################################################################
	 * 
	 * 初始化
	 * 
	 * A01 - Activity初始化onCreate方法
	 * A02 - UI部件的关联
	 * A03 - onClick各按钮点击事件监听
	 * 	     A03-1 清空缓存 (AlertDialog 回调事件)
	 * 	     A03-2 查看用户手册 (Activity)
	 *       A03-3 检查版本号并提示更新 (AlertDialog 回调事件 多线程 Handler弹窗)
	 *       A03-4 设置Ping外网操作时外网网关 (AlertDialog 回调事件 赋值)
	 *       A03-5 发送吐槽邮件 (AlertDialog 回调事件)
	 * A04 - ActionBar返回按钮响应
	 * A05 - 连接数据库
	 * A06 - 用户输入的有效性验证
	 * 
	 * ##########################################################################
	 */
	
    // A01
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_settings);
		ctx = this;																				//Content设定 便于对话框等使用
		
		ActionBar actionBar = getActionBar();													//ActionBar设定
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setDisplayShowTitleEnabled(true);
		}
		
		dbMana = new ControlDBMana();
		initViews();																			//Button关联的事件
	}
	
	// A02
	private void initViews() {																	//关联按钮并设置监听
		cleanBtn = (Button) findViewById(R.id.settings_clean_db);
		manualBtn = (Button) findViewById(R.id.settings_manual);
		checkVerBtn = (Button) findViewById(R.id.settings_check_ver);
		pingSetBtn = (Button) findViewById(R.id.settings_outer_ping_set);
		//tsukkomiBtn = (Button) findViewById(R.id.settings_tsukkomi);
		cleanBtn.setOnClickListener(this);
		manualBtn.setOnClickListener(this);
		checkVerBtn.setOnClickListener(this);
		pingSetBtn.setOnClickListener(this);
		//tsukkomiBtn.setOnClickListener(this);
	}

	// A03
	@SuppressLint("InflateParams")
	@Override
	public void onClick(View v) {
		
		if(ModelPressInterval.isFastDoubleClick()){																					// 快速点击检测
			Toast.makeText(ctx, "不要着急嘛，休息一下再点了啦~", Toast.LENGTH_SHORT).show();
		} else {
			
			switch (v.getId()) {
			
			/*
			 * A03-1
			 */
			case R.id.settings_clean_db:
				Log.i(TAG, "缓存清空操作");
				AlertDialog.Builder builder02_1 = new AlertDialog.Builder(ctx);
				builder02_1.setTitle("清空所有缓存数据");																				// AlertDialog Builder定义
				builder02_1.setMessage("清空后可能会导致诊断误判，真要这么做嘛？（用户数据只占用少量存储资源）");
				builder02_1.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			    	public void onClick(DialogInterface dialog, int whichButton) {													// AlertDialog PositiveButton回调
			    		ControlDataCleaner.cleanDatabases(getApplicationContext());													// 删除数据库
						ControlDataCleaner.cleanSharedPreference(getApplicationContext()); 											// 删除用户数据
						Toast.makeText(ctx, "缓存已清空", Toast.LENGTH_SHORT).show();
			        }  
			    });  
				builder02_1.setNegativeButton("取消", null);  
				builder02_1.create().show();																						// 02-1 AlertDialog提示清理缓存
				break;
				
			/*
			 * A03-2
			 */
			case R.id.settings_manual:
				Log.i(TAG, "启动手册界面");
				Intent intent02_2 = new Intent();
				ComponentName cp02_2 = new ComponentName(ctx, ViewActivitySettingsManual.class);
				intent02_2.setComponent(cp02_2);
				startActivity(intent02_2);																							// 02-2 Activity跳转
				break;
				
			/*
			 * A03-3
			 * 发送0230-版本检测异常
			 * 发送0231-有新版本 需要更新
			 * 发送0232-已是最新 无需更新
			 */
			case R.id.settings_check_ver:
				Log.i(TAG, "开始检查更新");
				connectDB();
				myProgressDialog = ProgressDialog.show(ctx, "请稍等...", "检查更新中...", true); 										// ProgressDialog显示
				queryBadAPDataVer();
				
				break;
				
			/*
			 * A03-4
			 */
			case R.id.settings_outer_ping_set:
				Log.i(TAG, "开始设置外网Ping网关");
				LayoutInflater factory = LayoutInflater.from(ctx);																	// LayoutInflater容纳自定义填写IP的输入框View并关联视图
			    final View textEntryView = factory.inflate(R.layout.layout_settings_pingdest, null);
				destIP = (EditText) textEntryView.findViewById(R.id.settings_destIP);
				
				AlertDialog.Builder builder02_4 = new AlertDialog.Builder(ctx);														// AlertDialog提示用户更改
				builder02_4.setTitle("设定Ping外网网关");
				builder02_4.setView(textEntryView);
				builder02_4.setPositiveButton("确定",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								
								if (varifyIfGoodInput(destIP.getText().toString())) {
									Log.w(TAG, "当前设置的IP为："+ destIP.getText().toString() );
//								if (destIP != null && destIP.getText() != null && varifyIfGoodInput(destIP.getText().toString())) {	// Step00 用户输入的有效性验证 调用A06
									SharedPreferences mySharedPreferences = getSharedPreferences("my_sp_instance", 					// step01 实例化SharedPreferences对象
											Activity.MODE_PRIVATE);																	
									SharedPreferences.Editor editor = mySharedPreferences.edit();									// step02 实例化SharedPreferences.Editor对象
									editor.putString("sp_outer_ping_dest", destIP.getText().toString());							// step03 用putString的方法保存数据
									editor.commit();																				// step04 提交当前数据
									Toast.makeText(ctx, "外网Ping网关设置完毕", Toast.LENGTH_SHORT).show();
									Log.i(TAG, "外网网关设置：成功！网关为：" + destIP.getText().toString());
								} else {
									Toast.makeText(ctx, "请输入合法的IP或域名，空着或者填无效字符都是不可以的哦~", Toast.LENGTH_SHORT).show();
									Log.i(TAG, "外网网关设置：失败！");
								}
							}
						});
				builder02_4.setNegativeButton("取消", null);
				builder02_4.create().show();
				break;
				
			/*
			 * A03-5
			 */
//			case R.id.settings_tsukkomi:
//				Log.i(TAG, "开始吐槽");
//				Intent it = new Intent(Intent.ACTION_SEND) ;
//				it.setType("这里是使用的右键类型，比如plain/text纯文本") ;
//				String add[] = new String[]{"这是完整的收件人地址"} ;
//				String sub = "这是邮件的主题" ;
//				String con = "这是邮件的内容" ;
//				it.putExtra(Intent.EXTRA_EMAIL, add) ;
//				it.putExtra(Intent.EXTRA_SUBJECT, sub) ;
//				it.putExtra(Intent.EXTRA_TEXT, con);
//				startActivity(it);
//				
//				//CODE HERE......																									// ******02-5 AlertDialog提示发邮件
//				Toast.makeText(ctx, "吐槽邮件发送完毕", Toast.LENGTH_SHORT).show();
//				break;
				
			}
		}
	}
	
	// A04
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
		case android.R.id.home:
			this.onDestroy();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	// A05
	private void connectDB() {
		try {
			dbMana.createUserDBbyHelper(ctx, "dwfdb");
			Log.i(TAG, "数据库链接建立完毕");
		} catch (Exception e) {
			Log.i(TAG, "数据库链接建立失败或无法执行goodap_table版本查询");
			e.printStackTrace();
		}
	}
	
	// A06
	private boolean varifyIfGoodInput(String str) {
		Pattern pattern01 = Pattern.compile("^((\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])\\.){3}(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])$");
		Pattern pattern02 = Pattern.compile("(http://|ftp://|https://|www){0,1}[^\u4e00-\u9fa5\\s]*?\\.(com|net|cn|me|tw|fr)[^\u4e00-\u9fa5\\s]*");
		Matcher matcher01 = pattern01.matcher(str);
		Matcher matcher02 = pattern02.matcher(str);
		boolean found01 = matcher01.find();
		boolean found02 = matcher02.find();
		if (found01 || found02) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * ##########################################################################
	 * 
	 * 多线程部分
	 * 
	 * T01 - 多线程Handler
	 * T02 - 多线程获取AP表版本号
	 * T03 - 多线程获取AP表信息
	 * T04 - 多线程刷新AP表信息
	 * 
	 * ##########################################################################
	 */
	
	// T01
	@SuppressLint("HandlerLeak")
	private Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			AlertDialog.Builder builder02_3 = new AlertDialog.Builder(ctx);
			switch (msg.what) {
			
			case 0:
				builder02_3.setTitle("检查更新出错啦");
				builder02_3.setMessage("请稍后再检查吧@_@");
				builder02_3.setNegativeButton("好吧", null);
				break;
				
			case 1:
				builder02_3.setTitle("有新版本可用");
				builder02_3.setMessage("点击“我要更新”来更新");
				builder02_3.setNegativeButton("先算了吧", null);
				builder02_3.setPositiveButton("我要更新", new DialogInterface.OnClickListener() {
			    	public void onClick(DialogInterface dialog, int whichButton) {				// AlertDialog PositiveButton回调-执行清理
			    		requestBadAPData();														// 回调T03
			        }  
			    });
				break;
				
			case 111:
				refreshBadAPData();																// 数据传输成功 调用T04
				break;
			
			case 110:																			// 120-122 T04回传
				Toast.makeText(ctx, "AP库更新：请求数据时出错！", Toast.LENGTH_SHORT).show();
				break;
				
			case 121:
				Toast.makeText(ctx, "AP库更新：成功！", Toast.LENGTH_SHORT).show();
				break;
				
			case 120:
				Toast.makeText(ctx, "AP库更新：存储数据时出错！", Toast.LENGTH_SHORT).show();
				break;

			case 2:
				builder02_3.setTitle("无需更新");
				builder02_3.setMessage("已经是最新版本啦^_^");
				builder02_3.setNegativeButton("太好啦~", null);
				break;
			}
			
			myProgressDialog.dismiss(); 														// ProgressDialog终止
			builder02_3.create().show();														// AlertDialog的最终显示(根据不同情况弹出不同提示)
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
					Bundle httpRes = ControlHttpsUtil.HttpsPost("https://目标url", params);
					String httpResState = httpRes.getString("res");
					String httpResData =  httpRes.getString("data");
					if (httpResState.equals("ok")) {
						JSONObject badAPVersionJO = new JSONObject(httpResData);
						badAPDataVer = badAPVersionJO.getLong("RAP_UPDATE_TIME");

						int isBadAPDBVersionOK = dbMana.queryIfBadAPTableVersionOK(badAPDataVer);	// query数据库版本号匹配情况
						switch (isBadAPDBVersionOK) {
						case 0: 																		// 回传为0（无匹配项）则需要更新
							myHandler.sendEmptyMessage(1);
							Log.i(TAG, "AP版本号获取：成功，有新版本可用！");
							break;
						case 1:
							myHandler.sendEmptyMessage(2);
							Log.i(TAG, "AP版本号获取：成功，无需更新");
							break;
						}
					} else {
						Log.i(TAG, "AP版本号获取：失败！");
						myHandler.sendEmptyMessage(0);
					}
				} catch (Exception e) {
					e.printStackTrace();
					myHandler.sendEmptyMessage(0);
					Log.i(TAG, "AP版本号获取：未知错误！");
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
					Bundle httpRes = ControlHttpsUtil.HttpsPost("https://目标url", params);
					String httpResState = httpRes.getString("res");
					String httpResData =  httpRes.getString("data");
					if (httpResState.equals("ok")) {
						badAPDataJO = new JSONObject(httpResData);
						Log.i(TAG, "黑名单AP版库信息获取成功，内容为：" + badAPDataJO);
						myHandler.sendEmptyMessage(111);
					} else {
						myHandler.sendEmptyMessage(110);
						Log.i(TAG, "黑名单AP版库信息获取失败！内容为空");
					}
				} catch (Exception e) {
					e.printStackTrace();
					myHandler.sendEmptyMessage(110);
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
					Log.i(TAG, "黑名单数据：" + badAPDataJO.toString());
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
					dbMana.insertNewBadAPDataToDB(badAPDatas);
					myHandler.sendEmptyMessage(121);
					
				} catch (Exception e) {
					e.printStackTrace();
					myHandler.sendEmptyMessage(120);
				}
			}
		}.start();
	}
	
	
}
