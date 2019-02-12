package com.attsinghua.dwf;

/*
 * 本类定义防止暴力点击的参数设置
 */

public class ModelPressInterval {
	
	private static long lastClickTime;
	private static long lastSubmitTime;
	private static long lastSubmitTime2;
	
	/**
	 * ####################################################################
	 * 
	 * 01-防止按钮暴力点击
	 * 02-防止重复提交
	 * 03-防止快速AP检测
	 * 
	 * ####################################################################
	 */
	// 01
    public static boolean isFastDoubleClick() {  // 间隔小于0.5秒的点击无效化 
        long time = System.currentTimeMillis();  
        long timeD = time - lastClickTime;  
        if ( 0 < timeD && timeD < 500) { 
            return true;     
        }     
        lastClickTime = time;     
        return false;     
    }
    
    // 02
	public static boolean cleanSubmitRec() {
		long time = System.currentTimeMillis();
		long timeD = time - lastSubmitTime2;
		if (0 < timeD && timeD < 5000) {		 // 间隔小于30秒的评价无效化
			return true;
		}
		lastSubmitTime2 = time;
		return false;
	}
	
	// 03
	public static boolean isFastRequest() {
		long time = System.currentTimeMillis();
		long timeD = time - lastSubmitTime;
		if (0 < timeD && timeD < 5000) {		 // 间隔小于5秒的评价无效化
			return true;
		}
		lastSubmitTime = time;
		return false;
	}
    
}
