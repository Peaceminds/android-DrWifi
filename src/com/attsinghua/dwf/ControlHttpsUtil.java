package com.attsinghua.dwf;

/*
 * Https工具类 本例中是信任包括自签名证书在内的一切证书
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;   
import java.security.cert.CertificateException;   
import java.security.cert.X509Certificate;   
import java.util.List;

import javax.net.ssl.HostnameVerifier;   
import javax.net.ssl.HttpsURLConnection;   
import javax.net.ssl.SSLContext;   
import javax.net.ssl.SSLSession;   
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import android.os.Bundle;
import android.util.Log;

public class ControlHttpsUtil {
	
	private static final String TAG = "ControlHttps";
	
	/**
	 * ####################################################################
	 * 
	 * 01 - HttpGet类方法
	 * 02 - HttpPost类方法
	 * 03 - Http请求的具体实现(01、02的具体实现)
	 * 04 - readStream读取信息流
	 * 05 - MyHostnameVerifier 的实现（目前的05+06实现的是认证所有SSL证书）
	 * 06 - MyTrustManager 的实现
	 * 
	 * ####################################################################
	 */
	
	// 01
	public static Bundle HttpsGet(String url, List<BasicNameValuePair> params) {
		return HttpsRequest(url, "GET", params);
	}
		
	// 02
	public static Bundle HttpsPost(String url, List<BasicNameValuePair> params) {
		return HttpsRequest(url, "POST", params);
	}
	
	// 03
	private static Bundle HttpsRequest( String _url, String method, List<BasicNameValuePair> params ) {
		
		Bundle res = new Bundle();
		
		// encode url paramters
		String paramStr = "";
		if(params != null) {
			paramStr = URLEncodedUtils.format(params, "UTF-8");
			}
		
		// create url
		URL sUrl = null;
		try {
			if(params != null && method.equalsIgnoreCase("GET")) {
				_url += "?" + paramStr;
				}
			
			Log.d(TAG, "url" + _url);
			sUrl = new URL(_url);
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
			res.putString("res", "fail");
			res.putString("reason", "Wrong URL");
			return res;
			}
		
		// do the connection
		HttpsURLConnection conn = null;
		try {
			
			SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new MyTrustManager()}, new SecureRandom());
            
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new MyHostnameVerifier());
            
			conn = (HttpsURLConnection) sUrl.openConnection();
			conn.setConnectTimeout(8000);
			conn.setReadTimeout(8000);

			if (method.equalsIgnoreCase("POST") && params != null) {
				
				conn.setRequestMethod("POST"); 							// 默认不设置的话是if上面的GET
				conn.setDoOutput(true);
//				conn.setRequestProperty("Accept-Language", "zh-CN");
				conn.setRequestProperty("Accept-Language", "er-US");
				conn.setRequestProperty("Charset", "utf-8");
				BufferedOutputStream out = new BufferedOutputStream(conn.getOutputStream());
				DataOutputStream dout = new DataOutputStream(out);
				dout.writeBytes(paramStr);
				dout.close();
			}
			
			BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
			
			String data = readStream(in);
			Log.d(TAG, "HTTPS 响应信息: " + data);

			res.putString("res", "ok");
			res.putString("data", data);
			
		} catch (IOException e) {
			e.printStackTrace();
			res.putString("res", "fail");
			res.putString("reason", "HTTPS访问服务器失败");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
			
		} finally {
			conn.disconnect();
		}
		
		return res;	
	}
	
	// 04
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
	
	// 05
    private static class MyHostnameVerifier implements HostnameVerifier{
    	
    	@Override
    	public boolean verify(String hostname, SSLSession session) {
    		// TODO Auto-generated method stub
    		return true;
    		}
    }
    
    // 06
    private static class MyTrustManager implements X509TrustManager{
    	
    	@Override
    	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
    		// TODO Auto-generated method stub
    		}
    	
    	@Override
    	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
    		// TODO Auto-generated method stub
    		}
    	
    	@Override
    	public X509Certificate[] getAcceptedIssuers() {
    		// TODO Auto-generated method stub
    		return null;
    		}
    }
    
    
}
