package com.attsinghua.dwf;

/*
 * 主要的View之三/三 
 * 供用户打分于评价
 * 上传无线网指纹+打分与评价+时间戳
 */

import java.util.ArrayList;
import java.util.List;
import org.achartengine.GraphicalView;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import com.thu.wlab.dwf.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Toast;

public class ViewFragmentResourceForecast extends Fragment implements OnClickListener, OnTouchListener, OnFocusChangeListener {
	
	/** 
	 * ####################################################################
	 * 
	 * 变量定义
	 * 
	 * 01 - 打分部分变量
	 * 02 - 图表部分变量
	 * 03 - 网络指纹获取部分
	 * 04 - 数据库部分
	 * 05 - SharedPreferences相关
	 * 
	 * ####################################################################
	 */
	private static final String TAG = "DWFViewFragmentResourceForecast";
	private RatingBar userRatingBar;
	private EditText userEditText;
	private int rts;
	private String rev;
	private Button submitBtn;
	private Button repaintBtn;
	private ModelRateData realRateData;
	protected long submitTime;
	private LinearLayout chartLayout;
	private ControlAChart aChart;
	private GraphicalView resChartView;
	private double[] avgRating = new double[24];
	private double[] avgAPLoad = new double[24];
	private ControlGetWifiFingerPrint getWFPNow;
	private ControlJsonMaker makeJsonObj;
	private JSONObject wfpJo;
	private JSONObject ratingJo;
	private ControlDBMana dbMana = new ControlDBMana();
	private static SharedPreferences mySharedPreferences;
	private int lastUploadFalseTimes;
	private boolean isSubmitYet1 = false;
	private boolean isSubmitYet2 = false;
	public static boolean legalSSID;
	
	/** 
	 * ####################################################################
	 * 
	 * Fragment本体定义
	 * 
	 * F01 - Fragment初始化整个View
	 * F02 - 按钮click监听响应事件
	 * F03 - onTouch监听响应事件 针对Layout点击 目的在于移除editText软键盘
	 * F04 - OnFocusChange监听响应事件 针对Tab切换等焦点变更的动作 目的在于移除editText软键盘
	 * 
	 * ####################################################################
	 */
	// F01
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View forecastLayout = inflater.inflate(R.layout.layout_forecast, container, false); 		// 设置Fragment的View
		userRatingBar = (RatingBar) forecastLayout.findViewById(R.id.forecast_ratingBar);			// 设置userRatingBar的View
		userEditText = (EditText) forecastLayout.findViewById(R.id.forecast_editText);				// 设置userEditText的View
		submitBtn = (Button) forecastLayout.findViewById(R.id.forecast_button);						// 设置submitBtn的View
		repaintBtn = (Button) forecastLayout.findViewById(R.id.forecast_button_see_what_they_say);
		
		submitBtn.setOnClickListener((OnClickListener) this);										// 设置点击 手势 焦点监听器
		repaintBtn.setOnClickListener((OnClickListener) this);
		forecastLayout.setOnTouchListener((OnTouchListener) this);
		userEditText.setOnFocusChangeListener((OnFocusChangeListener) this);
		
		// 获取网络指纹
		refreshWfpJo();
		
		// 上传关键AP数据，获取图表数据并更新图表视图
		uploadKeyAPAndGetRatingData();
		
		// 图表初始化
		chartLayout = (LinearLayout) forecastLayout.findViewById(R.id.forecast_chart);				// 渲染图表布局 // 这里不能用getActivity()，要用forecastLayout, 因为这样才是为每一个子Layout找到对应
		aChart = new ControlAChart();
		if (resChartView == null) {																	// 本例成员变量的AChart图表对象实例化
			resChartView = aChart.getMyGraphicalView(getActivity(), avgRating, avgAPLoad);
			chartLayout.addView(resChartView);
			} else {
				resChartView.repaint();
				}
		
		return forecastLayout;     							  										// 返回整个Fragment的视图
	}
	
	// F02
	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		
		case R.id.forecast_button:
			realRateData = new ModelRateData();
			realRateData.setRateStars(userRatingBar.getRating());
			rev = userEditText.getText().toString();
			rts = Math.round(userRatingBar.getRating());
			if (!ModelRateData.isValidStars(rts)) {
				Toast.makeText(getActivity(), "亲，请给打个分吧~", Toast.LENGTH_SHORT).show();
				return;
			} else {
				if (!ModelRateData.isValidReview(rev)) { 											// Static 直接用类名就可以了
					Log.d("检测输入内容", "非法输入");
					Toast.makeText(getActivity(), "输入有误，只允许输入简体汉字、英文、数字和基本符号", Toast.LENGTH_SHORT).show();
				} else {
					if (!ModelPressInterval.cleanSubmitRec()) {
						isSubmitYet1 = false;
					}
					if (!isSubmitYet1) {
						realRateData.setRateReview(rev);
						setRatingJo(makeJsonObj.rateDataToJson(rts, rev));
						insertRatingData(); 														// 调用T04 本地数据库插入操作
						if ( ModelConfiguration.getIsCurrentSSIDLegal(getWFPNow.getLinkSSID()) ) {	
							upLoadRatingToServer(); 												// 调用T02 上传服务器与判断
							isSubmitYet1 = true;
						} else {
							new AlertDialog.Builder(getActivity())
							.setTitle("提示")
							.setMessage("只能在连接校园网AP情况下对AP发起评价和吐槽哦~")
							.setNegativeButton("好吧", null).show();
						}
					} else {
						Toast.makeText(getActivity(), "30秒内只需提交一次，谢谢您！", Toast.LENGTH_SHORT).show();
					}
				}
			}
			break;

		case R.id.forecast_button_see_what_they_say:
			if (ModelPressInterval.isFastRequest()) {
				Toast.makeText(getActivity(), "点太快不是好孩子_(:з」∠)_呐，等几秒再点了啦~", Toast.LENGTH_SHORT).show();
				return;
			} else {
				if (!ModelPressInterval.cleanSubmitRec()) {
					isSubmitYet2 = false;
				}
				if (!isSubmitYet2) {
					uploadKeyAPAndGetRatingData();
				} else {
					Toast.makeText(getActivity(), "请不要刷新太频繁哦~", Toast.LENGTH_SHORT).show();
				}
			}
			break;
		}
	}

	// F03
	public boolean onTouch(View v, MotionEvent event) {
	    switch (event.getAction()) {
	    case MotionEvent.ACTION_DOWN:
	    	InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(userEditText.getWindowToken(), 0);
	        break;
	    case MotionEvent.ACTION_UP:
	        v.performClick();
	        break;
	    default:
	        break;
	    }
	    return true;
	}
	
	// F04
	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if(!hasFocus){
			InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(userEditText.getWindowToken(), 0);
		}
	}
	
	/** 
	 *  ####################################################################
	 *  
	 *  多线程
	 *  
	 *  T01 - Handler
	 *  T02 - wfp与rating上传服务器
	 *  T03 - wfp与rating上传失败时SP置位
	 *  T04 - wfp与rating入库
	 *  T05 - 接收Server端Rating数据
	 *  T06 - 上传关联AP与附近最强3个AP信息
	 *  
	 *  ####################################################################
	 */
	// T01
	@SuppressLint("HandlerLeak")
	private Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			
			case 20:																				// 接收T02，0为上传成功；1 2为错误 此时调用T03将SP中上传失败置位
				Toast.makeText(getActivity(), "评价上传成功~", Toast.LENGTH_SHORT).show();
				uploadKeyAPAndGetRatingData();
				break;
			case 21:
				Toast.makeText(getActivity(), "评价上传失败...请稍后再试", Toast.LENGTH_SHORT).show();
				uploadFailSetFunc();
				break;
			case 22:
				Toast.makeText(getActivity(), "评价上传失败，出错啦@_@", Toast.LENGTH_SHORT).show();
				uploadFailSetFunc();
				break;
				
			case 60:																				// 接收T06回传，0为接收评价信息成功，给待显示数据赋值
				refreshChart();
				Toast.makeText(getActivity(), "综合评价信息获取完成 O(∩_∩)O", Toast.LENGTH_SHORT).show();
				break;			
			case 61:
				Toast.makeText(getActivity(), "综合评价信息获取失败，请稍后再试！", Toast.LENGTH_SHORT).show();
				break;
			case 62:
				Toast.makeText(getActivity(), "综合评价信息获取出错！请稍后再试！", Toast.LENGTH_SHORT).show();
				break;
				
			}
		};
	};
	
	// T02
	private void upLoadRatingToServer() {
		new Thread() {
			public void run() {
				try {
					List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
					String fpDataStr = wfpJo.toString();											// wifiFingerPrint封装好的JSON转为字符串
					String rtDataStr = ratingJo.toString();											// userRating封装好的JSON转为字符串
					String sysTime = makeJsonObj.timestampToJson().toString();						// 准备好上传时间
					params.add(new BasicNameValuePair("msgType", "{\"msgType\":2}"));
					params.add(new BasicNameValuePair("sysTime", sysTime));							// JSON额外的封装，用于给服务器标明字符串内容
					params.add(new BasicNameValuePair("rtData", rtDataStr));
					params.add(new BasicNameValuePair("fpData", fpDataStr));
					Bundle httpRes = ControlHttpsUtil.HttpsPost("https://59.66.25.139/hadoop/forming.pl", params);
					Log.w(TAG+"Type=2的fp上传", params.toString());
					String httpResState = httpRes.getString("res");
					if (httpResState.equals("ok")) {
						myHandler.sendEmptyMessage(20);
						Log.i(TAG, "评价信息上传成功！");
					} else {
						myHandler.sendEmptyMessage(21);
						Log.i(TAG, "评价信息上传失败！");
					}
				} catch (Exception e) {
					e.printStackTrace();
					myHandler.sendEmptyMessage(22);
					Log.i(TAG, "评价信息上传失败：未知错误！");
				}
			};
		}.start();
	}
	
	// T03
	private void uploadFailSetFunc() {
		mySharedPreferences = getActivity().getSharedPreferences("my_sp_instance", Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = mySharedPreferences.edit();
		editor.putInt("sp_last_upload_false", lastUploadFalseTimes + 1);
		editor.commit();
	}
			
	// T04
	private void insertRatingData() {
		new Thread() {
			@Override
			public void run() {
				try {
					dbMana.createUserDBbyHelper(getActivity(), "dwfdb");
					dbMana.insertWifiFingerPrintToDB(null, getWFPNow);
					dbMana.insertAPScanInfoToDB(getWFPNow);
					dbMana.insertRatingToDB(getWFPNow, rts, rev);
					Log.i(TAG, "评价信息入库完成！");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	// T06
	private void requsetRatingData() {
		new Thread() {
			@Override
			public void run() {
				try {
					refreshWfpJo();																// 刷新WfpJo
					List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
					String fpDataStr = wfpJo.toString();
					String sysTime = makeJsonObj.timestampToJson().toString();					// 准备好上传时间
					params.add(new BasicNameValuePair("msgType", "{\"msgType\":9}"));			// 9代表请求关键AP评价
					params.add(new BasicNameValuePair("sysTime", sysTime));						// JSON额外的封装，用于给服务器标明字符串内容
					params.add(new BasicNameValuePair("fpData", fpDataStr));
					Log.w(TAG, "Type=9的上传内容为： >>> " + params.toString());
					Bundle httpRes = ControlHttpsUtil.HttpsPost("https://59.66.25.139/hadoop/forming.pl", params);
					String httpResState = httpRes.getString("res");
					String httpResData = httpRes.getString("data");
					if (httpResState.equals("ok")) {
						JSONObject serverRatingJO = new JSONObject(httpResData);
						JSONArray serverRatingArray01 = serverRatingJO.getJSONArray("network_load");
						JSONArray serverRatingArray02 = serverRatingJO.getJSONArray("user_eval");
						for (int i = 0; i < avgAPLoad.length; i++) {
							avgAPLoad[i] = serverRatingArray01.getDouble(i);
							avgRating[i] = serverRatingArray02.getDouble(i);
						}
						Log.i(TAG, "服务器端Rating获取：成功，内容为：" + avgAPLoad.toString() + "  " + avgRating.toString());
						myHandler.sendEmptyMessage(60);
					} else {
						Log.i(TAG, "服务器端Rating获取：失败！");
						myHandler.sendEmptyMessage(61);
					}
				} catch (Exception e) {
					e.printStackTrace();
					Log.i(TAG, "服务器端Rating获取：未知错误！");
					myHandler.sendEmptyMessage(62);
				}
			}
		}.start();
	}
	
	/** 
	 *  ####################################################################
	 *  
	 *  主线程方法
	 *  
	 *  M01 - 刷新以获取动态更新的Wfp
	 *  M02 - 上传关键AP数据，获取图表数据并调用M03
	 *  M03 - 更新图表视图
	 *  M04 - setRatingJo
	 *  M05 - setWfpJo
	 *  
	 *  ####################################################################
	 */
	// M01
	public void refreshWfpJo(){
		getWFPNow = new ControlGetWifiFingerPrint();
		makeJsonObj = new ControlJsonMaker();
		try {
			getWFPNow.setThreadMediatorInStructure(false);
			getWFPNow.getWifiMana(getActivity());
			getWFPNow.getWifiInfo();
			getWFPNow.getWifiDhcpInfo();
			getWFPNow.getWifiScanInfoList();
			getWFPNow.getDeviceInfo(getActivity());
			setWfpJo(makeJsonObj.fingerPrintToJSON(getWFPNow));  									// 利用JSON封装结构创建一个JSONObject以供后续操作
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// M02
	public void uploadKeyAPAndGetRatingData() {
		refreshWfpJo();
		if (!getWFPNow.getLinkBSSID().isEmpty()) {
			if ( ModelConfiguration.getIsCurrentSSIDLegal(getWFPNow.getLinkSSID()) ) {	
				requsetRatingData();
			} else {
				Toast.makeText(getActivity(), "只能获取校园网AP评价数据喔", Toast.LENGTH_SHORT).show();
				setDefaultRatingPoints();
				try {
					refreshChart();
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		} else {
			Toast.makeText(getActivity(), "AP连接已断开", Toast.LENGTH_SHORT).show();
			setDefaultRatingPoints();
			refreshChart();
		}
	}
	
	// M03
	public void refreshChart() {
		refreshWfpJo();
		aChart = new ControlAChart();
		resChartView = aChart.getMyGraphicalView(getActivity(), avgRating, avgAPLoad);
		chartLayout.removeAllViews();
		chartLayout.addView(resChartView);
		resChartView.repaint();
	}
	
	// M04
	private void setDefaultRatingPoints() {
		for (int i = 0; i < avgAPLoad.length; i++) {
			avgAPLoad[i] = 0;
			avgRating[i] = 0;
		}
	}
	
	// M05
	public void setRatingJo(JSONObject jo) {
		this.ratingJo = jo;
	}
	
	// M06
	public void setWfpJo(JSONObject jo){
		this.wfpJo = jo;
	}
	
}
