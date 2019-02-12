package com.attsinghua.dwf;

/*
 * Http工具类 具体实现了POST GET
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

public class ControlHttpUtil {
	
	private static final String TAG = "ControlHttpUtil";
	
	/**
	 * ####################################################################
	 * 
	 * 01 - 判断版本以确定http连接状态
	 * 02 - HttpGet类方法
	 * 03 - HttpPost类方法
	 * 04 - Http请求的具体实现(02、03的具体实现)
	 * 
	 * ####################################################################
	 */
	// 01
	static {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
		     System.setProperty("http.keepAlive", "false");
		}
	}
	
	// 02
	public static Bundle HttpGet(String url, List<BasicNameValuePair> params) {
		return HttpRequest(url, "GET", params);
	}
	
	// 03
	public static Bundle HttpPost(String url, List<BasicNameValuePair> params) {
		return HttpRequest(url, "POST", params);
	}
	
	// 04
	private static Bundle HttpRequest(String _url, String method, List<BasicNameValuePair> params) {
		
		Bundle res = new Bundle();
		
		// encode url paramters
		String paramStr = "";
		if(params != null) {
			paramStr = URLEncodedUtils.format(params, "UTF-8");
		}
		// Log.d(TAG, "paramStr:" + paramStr);
		
		// create url
		URL url = null;
		try {
			if(params != null && method.equalsIgnoreCase("GET")) {
				_url += "?" + paramStr;
			}
			Log.d(TAG, "url" + _url);
			
			url = new URL(_url);
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
			res.putString("res", "fail");
			res.putString("reason", "Wrong URL");
			return res;
		}
		
		// do the connection
		HttpURLConnection conn = null;
		try {
			
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(8000);
			conn.setReadTimeout(8000);
			
			if(method.equalsIgnoreCase("POST") && params != null) {
				conn.setDoOutput(true);
				BufferedOutputStream out = new BufferedOutputStream(conn.getOutputStream());
				
				DataOutputStream dout = new DataOutputStream(out);
				dout.writeBytes(paramStr);
				dout.close();
				Log.d(TAG, "当前的httppp的url为" + paramStr);
			}
			
			BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
			String data = readStream(in);
			Log.d(TAG, "HTTP Response: " + data);
			
			res.putString("res", "ok");
			res.putString("data", data);
		} catch (IOException e) {
			e.printStackTrace();
			res.putString("res", "fail");
			res.putString("reason", "访问服务器失败");
		} finally {
			conn.disconnect();
		}
		
		return res;	
	}
	
	private static String readStream(BufferedInputStream in) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		StringBuilder sb = new StringBuilder();
		
		String line = null;
		while((line = br.readLine()) != null) {
			sb.append(line);
			sb.append(System.getProperty("line.separator"));
		}
		return sb.toString();
	}
	
}