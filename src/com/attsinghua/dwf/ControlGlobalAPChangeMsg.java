package com.attsinghua.dwf;

/*
 * 本类实现了后台情况下AP切换的监听，此时为用户更新数据，以便发起检测。
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.util.Log;

public class ControlGlobalAPChangeMsg extends BroadcastReceiver {

	private static final String TAG = "ControlGlobalAPChangeMsg";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		Log.i(TAG, "切换到的AP BSSID为:" + intent.getStringExtra(WifiManager.EXTRA_BSSID) );
		
		SharedPreferences sp = context.getSharedPreferences("my_sp_instance", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();						// sp_start_times值的存储
		editor.putString("sp_ap_changed_bssid", WifiManager.EXTRA_BSSID);
		editor.putLong("sp_ap_changed_timestamp", System.currentTimeMillis());
		editor.commit();
		Log.i(TAG, "AP切换，SP打戳完成");
		
	}
	
}
