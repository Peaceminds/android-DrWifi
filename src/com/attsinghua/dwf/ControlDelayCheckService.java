package com.attsinghua.dwf;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class ControlDelayCheckService extends Service {

	private static final String TAG = "ControlDelayCheckService"; 

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	 @Override  
	    public void onCreate() {  
	        Log.v(TAG, "onCreate 延迟推送的Service");  
	        Toast.makeText(this, "show media player", Toast.LENGTH_SHORT).show();  

	    }  
	  
	    @Override  
	    public void onDestroy() {  
	        Log.v(TAG, "onDestroy");  

	    }  
	  
	    @Override  
	    public void onStart(Intent intent, int startId) {  
	        Log.v(TAG, "onStart");  

	    }  

}
