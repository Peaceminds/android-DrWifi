package com.attsinghua.dwf;

import com.thu.wlab.dwf.R;
import android.app.Activity;
import android.app.ActionBar;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ViewActivityPPCounter extends Activity implements OnClickListener {

	private static final String TAG = "ViewActivityPPCounter";
	private Context ctx;
	private int cValue;
	private TextView ppcTv;
	private Button displayBtn;
	private Button resetBtn;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_ppcounter);
		ctx = this;
		
		ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setDisplayShowTitleEnabled(true);
		}
		
		ppcTv = (TextView)findViewById(R.id.ppcounter_textView1);
		displayBtn = (Button)findViewById(R.id.ppcounter_btn_display);
		resetBtn = (Button)findViewById(R.id.ppcounter_btn_reset);
		displayBtn.setOnClickListener(this);
		resetBtn.setOnClickListener(this);
		
		loadPPCounter();
		
	}

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

	@Override
	public void onClick(View v) {
		if(ModelPressInterval.isFastDoubleClick()){																					// 快速点击检测
			Toast.makeText(ctx, "不要着急嘛，休息一下再点了啦~", Toast.LENGTH_SHORT).show();
		} else {
			switch (v.getId()) {
				
				case R.id.ppcounter_btn_display:
					Log.w(TAG, "点击刷新按钮");
					loadPPCounter();
					refreshTV();
				break;
				
				case R.id.ppcounter_btn_reset:
					setPPCounter(0);
					refreshTV();
				break;
			}
		}
		
	}
	
	private void loadPPCounter() {
		SharedPreferences mySharedPreferencesD = getSharedPreferences("my_sp_instance", Activity.MODE_PRIVATE);
		if (mySharedPreferencesD == null) { 
			cValue = 0;
		} else {
			cValue = mySharedPreferencesD.getInt("sp_ppcounter", -1);
		}
	}
	
	public void setPPCounter(int i) {
		SharedPreferences mySharedPreferencesF = getSharedPreferences("my_sp_instance", Activity.MODE_PRIVATE);																	
		SharedPreferences.Editor editor = mySharedPreferencesF.edit();
		if (i == 0) {
			cValue = -1;
			editor.putInt("sp_ppcounter", -1);
		} else if (i == 1) {
			int j = mySharedPreferencesF.getInt("sp_ppcounter", 0);
			cValue = j + 1;
			editor.putInt("sp_ppcounter", cValue);
		}
		editor.commit();
	}
	
	private void refreshTV() {
		Log.w(TAG, "ppCounter准备刷新~");
		String tv = String.valueOf(cValue);
		ppcTv.setText(tv);
		Toast.makeText(ctx, "数据已刷新！", Toast.LENGTH_SHORT).show();
		Log.i(TAG, "ppCounter已刷新~");
	}
	
}
