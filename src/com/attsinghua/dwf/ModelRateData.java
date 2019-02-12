package com.attsinghua.dwf;

/*
 * 本类定义评分、评价信息的定义，提供与输入检测的方法
 */

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModelRateData {
	
	private float rateStars;
	private String rateReview;
	
	/**
	 * ####################################################################
	 * 
	 * 对外接口（数据输入）
	 * 
	 * 外部类的UI操作据此接口对本类成员变量赋值来判断
	 * 使用本类判断前先要使用本接口赋值
	 * 
	 * ####################################################################
	 */
	public void setRateStars(float rateStars) {
		this.rateStars = rateStars;
	}
	
	public void setRateReview(String rateReview) {
		this.rateReview = rateReview;
	}
	
	
	/**
	 * ####################################################################
	 * 
	 * 输入检测
	 * 
	 * 01 - 评星/分检测
	 * 	  true	不为0的评分
	 * 	  false 为0的评分
	 * 02 - 评价内容检测
	 * 	  true	仅为汉字英文数字的输入 字符无效 
	 * 	  false 空输入或无效的输入
	 * 
	 * ####################################################################
	 */
	// 01
	public static boolean isValidStars(float rtS) {
		if (rtS == 0)
			return false;
		return true;
	}
	
	// 02 限制用户输入1-70字符只能是中英数、空格、全/半角逗号句号、@
	public static boolean isValidReview(String rtR) {
		Pattern pattern = Pattern.compile("^[A-Za-z0-9\\，\\。\\！\\!\\？\\?\\@\\s,\\.\n\uff0c\u3002\u4e00-\u9fa5]{0,70}$");  // 半角空格直接空就行 也可以用\s 但是要加双斜杠转义
		Matcher matcher = pattern.matcher(rtR);
		return matcher.find();
	}

	
	/**
	 * ####################################################################
	 * 
	 * 对外接口（数据输出）
	 * 
	 * 执行完上述的获取与判断之后
	 * 外部类可用下面的接口获取成员变量的值
	 * 
	 * ####################################################################
	 */
	public float getRateStars() {
		return rateStars;
	}

	public String getRateReview() {
		return rateReview;
	}

	
}
