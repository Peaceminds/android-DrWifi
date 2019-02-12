package com.attsinghua.dwf;

/*
 * 本类实现了全局BroadcastReceiver的定义，主要用于全局状态下（即各种情况，包括处于后台的情况）接收系统广播，从而实现后台推送（前台下的推送在MainActivity中以动态注册方式实现）
 */

import org.json.JSONException;
import org.json.JSONObject;
import com.attsinghua.socketservice.SocketService;
import com.thu.wlab.dwf.R;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class ControlGlobalPushMsgBroadcastReceiver extends BroadcastReceiver {

	private static final String TAG = ControlGlobalPushMsgBroadcastReceiver.class.getName();
	public static int PUSH_MESSAGE_RECEIVED_NOTIFICATION_ID = 1;

	/**
	 * ####################################################################
	 * 
	 * M01 - BroadcastReceiver 收到推送时的处理（BroadcastReceiver的基本方法）
	 * 
	 * @param context 针对显示不同 Activity 需求，用于传入 Activity 的 Context
	 * @param intent 针对静态注册在 Manifest 中的 Intent Filter
	 * 
	 * ####################################################################
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		String targetAction = SocketService.ACTION_PUSH_MESSAGE_RECEIVED;

		if (!intent.getAction().equals(targetAction))
			return;
		Log.d(TAG, "Push message received by global receive.");
		
		JSONObject jo;
		try {
			
			jo = new JSONObject(intent.getStringExtra(SocketService.BROADCAST_INTENT_EXTRA_PUSH_MSG_CONTENT)); 	// 按需取出推送的数据
			Log.i(TAG, "静态接收器（推往通知中心）接收到的推送数据为：" + jo.toString());
			
			String noifyTitle = "";
			String notifyMsg = "";
			PendingIntent pi = null;																			// 根据推送类型的不同，点击通知栏图标可跳转至不同的Activity（当前是没有分类的，所以无需跳转）
			
			/*
			 * 调试用推送 1-3
			 */
//			if (jo.getString("type").contains("通知信息")) {
//				noifyTitle = "校园网AP通知";
//				notifyMsg = "当前关联的AP附近可能存在恶意AP，使用校园网时建议先检测喔~";
//			}
//			if (jo.getString("type").contains("通知信息")) {
//				noifyTitle = "校园网AP通知";
//				notifyMsg = "人文社科图书馆F3 AP准备调试重启，请您稍后再连接，谢谢！";
//			}
			if (jo.getString("type").contains("通知信息")) {
				noifyTitle = "AP优选推荐";
				notifyMsg = "在您附近存在更优的AP可供选择，若您需要改善无线网用网体验，建议到F2使用。";
			}
			
			/*
			 * 生产环境推送
			 */
//			if (jo.getString("type").contains("ap_validate")) {
//				boolean APCheckRst = true;
//				JSONObject recevJo = new JSONObject(jo.getString("message"));
//				APCheckRst = recevJo.getBoolean("ap_validate");
//				if (APCheckRst) {
//					noifyTitle = "AP检测结果";
//					notifyMsg = "恭喜~关联的校园网AP合法，通过认证即可上网。也可点击此通知检查网络状况";
//				} else {
//					noifyTitle = "山寨AP风险警告";
//					notifyMsg = "警告！关联的AP可能是山寨的！个人信息有泄漏的风险！点击此通知可检查网络状况";
//				}
//			}
//
//			if (jo.getString("type").contains("ap_around_warning")) {
//				boolean APWaring = true;
//				JSONObject recevJo = new JSONObject(jo.getString("message"));
//				APWaring = recevJo.getBoolean("ap_around_warning");
//				if (APWaring) {
//					noifyTitle = "小提示";
//					notifyMsg = "当前关联的AP附近可能存在恶意AP，使用校园网时建议先检测喔~";
//				} else {
//					return;
//				}
//			}
//			
//			if (jo.getString("type").contains("ap_recommend")) {
//				JSONObject recevJo = new JSONObject(jo.getString("message"));
//				String APRecommend = recevJo.getString("ap_recommend");
//				noifyTitle = "AP优选推荐";
//				notifyMsg = APRecommend;
//			}
//			
//			if (jo.getString("type").contains("thu_broadcast")) {
//				JSONObject recevJo = new JSONObject(jo.getString("message"));
//				String tHUBroadcast = recevJo.getString("thu_broadcast");
//				noifyTitle = "校园网通知";
//				notifyMsg = tHUBroadcast;
//			}
			
			// 调用下面的方法实现推送的显示
			pi = PendingIntent.getActivity(context, 0, new Intent(context, ViewActivityMain.class), PendingIntent.FLAG_UPDATE_CURRENT);
			showNotification(context, noifyTitle, notifyMsg, R.drawable.dr_wifi_logo, null, PUSH_MESSAGE_RECEIVED_NOTIFICATION_ID++, pi);  // 原来的ID是 PUSH_MESSAGE_RECEIVED_NOTIFICATION_ID++	
		
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ####################################################################
	 * 
	 * M02 - showNotification 在通知中心里显示信息的方法
	 * 
	 * @param context 启动某个制定Activity时，传入其上下文
	 * @param title 通知标题
	 * @param msg 通知标题
	 * @param smallIconId 显示在通知栏的小Icon
	 * @param largeIconBitmap 下拉通知栏的大Icon
	 * @param id 通知ID
	 * @param intent 启动Activity时的Intent实例
	 * 
	 * ####################################################################
	 */
	@SuppressLint("NewApi")
	public static void showNotification(Context context, String title, String msg, int smallIconId, Bitmap largeIconBitmap, int id, PendingIntent intent) {

		NotificationCompat.Builder nb = new NotificationCompat
				.Builder(context)
				.setContentTitle(title)
				.setContentText(msg)
				.setTicker(msg)
				.setSmallIcon(smallIconId)
				.setLargeIcon(largeIconBitmap)
				.setContentIntent(intent)
				.setOnlyAlertOnce(true);
		
		nb.setAutoCancel(true);
		nb.setDefaults(Notification.DEFAULT_ALL);

		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		if (nm != null) {
			nm.notify(id, nb.build());
		}

	}

	
}
