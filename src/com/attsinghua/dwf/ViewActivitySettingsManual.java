package com.attsinghua.dwf;

/*
 * 本类是用户说明View，提供滚动浏览功能
 */

import com.thu.wlab.dwf.R;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class ViewActivitySettingsManual extends Activity {

	private static final String TAG = "ViewActivitySettings";
	private LinearLayout sContainer;  
    private ScrollView sView;
	private TextView tv01;
    private TextView tv02;
    private TextView tv03;
    private TextView tv04;
    private TextView tv05;
    private TextView tv06;
    private TextView tv07;
    private TextView tv08;
    private TextView tv09;
    private TextView tv10;
    private TextView tv11;
    private TextView tv12;
    private TextView tv13;
	
	/**
	 * ##########################################################################
	 * 
	 * 01 - 初始化方法 
	 * 02 - 顶部菜单返回
	 * 03 - getter and setters
	 * 
	 * ##########################################################################
	 */
	// 01
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_settings_manual);
		
		ActionBar actionBar = getActionBar();													//ActionBar设定
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setDisplayShowTitleEnabled(true);
		}
		
		Log.i(TAG, "开始加载说明");
        sView = (ScrollView) findViewById(R.id.manual_scroll_view);								//创建一个ScrollView对象
		sContainer = (LinearLayout) findViewById(R.id.manual_scroll_view_container);			//创建一个线性布局
		tv01 = (TextView) findViewById(R.id.manual_h1);
		tv02 = (TextView) findViewById(R.id.manual_h2);
		tv03 = (TextView) findViewById(R.id.manual_h3);
		tv04 = (TextView) findViewById(R.id.manual_h4);
		tv05 = (TextView) findViewById(R.id.manual_h5);
		tv06 = (TextView) findViewById(R.id.manual_c1);
		tv07 = (TextView) findViewById(R.id.manual_c21);
		tv08 = (TextView) findViewById(R.id.manual_c22);
		tv09 = (TextView) findViewById(R.id.manual_c31);
		tv10 = (TextView) findViewById(R.id.manual_c41);
		tv11 = (TextView) findViewById(R.id.manual_c42);
		tv12 = (TextView) findViewById(R.id.manual_c51);
		tv12 = (TextView) findViewById(R.id.manual_c52);

	}
	
	// 02
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
		case android.R.id.home:
			this.finish();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	// 03
	 public LinearLayout getsContainer() {
			return sContainer;
		}

		public void setsContainer(LinearLayout sContainer) {
			this.sContainer = sContainer;
		}

		public ScrollView getsView() {
			return sView;
		}

		public void setsView(ScrollView sView) {
			this.sView = sView;
		}

		public TextView getTv01() {
			return tv01;
		}

		public void setTv01(TextView tv01) {
			this.tv01 = tv01;
		}

		public TextView getTv02() {
			return tv02;
		}

		public void setTv02(TextView tv02) {
			this.tv02 = tv02;
		}

		public TextView getTv03() {
			return tv03;
		}

		public void setTv03(TextView tv03) {
			this.tv03 = tv03;
		}

		public TextView getTv04() {
			return tv04;
		}

		public void setTv04(TextView tv04) {
			this.tv04 = tv04;
		}

		public TextView getTv05() {
			return tv05;
		}

		public void setTv05(TextView tv05) {
			this.tv05 = tv05;
		}

		public TextView getTv06() {
			return tv06;
		}

		public void setTv06(TextView tv06) {
			this.tv06 = tv06;
		}

		public TextView getTv07() {
			return tv07;
		}

		public void setTv07(TextView tv07) {
			this.tv07 = tv07;
		}

		public TextView getTv08() {
			return tv08;
		}

		public void setTv08(TextView tv08) {
			this.tv08 = tv08;
		}

		public TextView getTv09() {
			return tv09;
		}

		public void setTv09(TextView tv09) {
			this.tv09 = tv09;
		}

		public TextView getTv10() {
			return tv10;
		}

		public void setTv10(TextView tv10) {
			this.tv10 = tv10;
		}

		public TextView getTv11() {
			return tv11;
		}

		public void setTv11(TextView tv11) {
			this.tv11 = tv11;
		}

		public TextView getTv12() {
			return tv12;
		}

		public void setTv12(TextView tv12) {
			this.tv12 = tv12;
		}

		public TextView getTv13() {
			return tv13;
		}

		public void setTv13(TextView tv13) {
			this.tv13 = tv13;
		}
		
}
