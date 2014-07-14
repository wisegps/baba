package com.wise.state;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pubclas.NetThread;

import com.wise.baba.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
/**
 * 车况检测
 * @author honesty
 *
 */
public class FaultDetectionActivity extends Activity{
	private static final int getData = 1;
	
	TextView tv_guzhang ,tv_guzhang_icon , tv_dianyuan,tv_dianyuan_icon,
				tv_jinqi,tv_jinqi_icon, tv_daisu,tv_daisu_icon,
				tv_lengque,tv_lengque_icon,tv_paifang,tv_paifang_icon;
	
	private TasksCompletedView mTasksView;	
	private int mTotalProgress;
	private int mCurrentProgress;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_fault_detection);
		initVariable();
		initView();
		ImageView iv_back = (ImageView)findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		Intent intent = getIntent();
		System.out.println(intent.toString());
		getData();
		//new Thread(new ProgressRunable()).start();
	}
	
	OnClickListener onClickListener = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			case R.id.rl_guzhang:
				break;
			case R.id.rl_dianyuan:
				break;
			case R.id.rl_jinqi:
				break;
			case R.id.rl_daisu:
				break;
			case R.id.rl_lengque:
				break;
			case R.id.rl_paifang:
				break;
			}
		}
	};
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case getData:
				jsonHealth(msg.obj.toString());
				break;

			default:
				break;
			}
		}		
	};
	
	private void initVariable() {
		mTotalProgress = 100;
		mCurrentProgress = 0;
	}
	private void initView() {
		mTasksView = (TasksCompletedView) findViewById(R.id.tasks_view);
		tv_guzhang = (TextView)findViewById(R.id.tv_guzhang);
		tv_guzhang_icon = (TextView)findViewById(R.id.tv_guzhang_icon);
		tv_dianyuan = (TextView)findViewById(R.id.tv_dianyuan);
		tv_dianyuan_icon = (TextView)findViewById(R.id.tv_dianyuan_icon);
		tv_jinqi = (TextView)findViewById(R.id.tv_jinqi);
		tv_jinqi_icon = (TextView)findViewById(R.id.tv_jinqi_icon);
		tv_daisu = (TextView)findViewById(R.id.tv_daisu);
		tv_daisu_icon = (TextView)findViewById(R.id.tv_daisu_icon);
		tv_lengque = (TextView)findViewById(R.id.tv_lengque);
		tv_lengque_icon = (TextView)findViewById(R.id.tv_lengque_icon);
		tv_paifang = (TextView)findViewById(R.id.tv_paifang);
		tv_paifang_icon = (TextView)findViewById(R.id.tv_paifang_icon);
		RelativeLayout rl_guzhang = (RelativeLayout)findViewById(R.id.rl_guzhang);
		rl_guzhang.setOnClickListener(onClickListener);
		RelativeLayout rl_dianyuan = (RelativeLayout)findViewById(R.id.rl_dianyuan);
		rl_dianyuan.setOnClickListener(onClickListener);
		RelativeLayout rl_jinqi = (RelativeLayout)findViewById(R.id.rl_jinqi);
		rl_jinqi.setOnClickListener(onClickListener);
		RelativeLayout rl_daisu = (RelativeLayout)findViewById(R.id.rl_daisu);
		rl_daisu.setOnClickListener(onClickListener);
		RelativeLayout rl_lengque = (RelativeLayout)findViewById(R.id.rl_lengque);
		rl_lengque.setOnClickListener(onClickListener);
		RelativeLayout rl_paifang = (RelativeLayout)findViewById(R.id.rl_paifang);
		rl_paifang.setOnClickListener(onClickListener);
	}
	
	class ProgressRunable implements Runnable {
		@Override
		public void run() {
			while (mCurrentProgress < mTotalProgress) {
				mCurrentProgress += 1;
				mTasksView.setProgress(mCurrentProgress);
				try {
					Thread.sleep(100);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	private void getData(){
		String url = "http://baba.api.wisegps.cn/device/4/health_exam?auth_code=bba2204bcd4c1f87a19ef792f1f68404";
		new Thread(new NetThread.GetDataThread(handler, url, getData)).start();
	}
	String dpdy_content;
	private void jsonHealth(String str){
		try {
			JSONObject jsonObject = new JSONObject(str);
			//健康指数
			int health_score = jsonObject.getInt("health_score");
			System.out.println("health_score = " + health_score);
			//故障码
			JSONArray jsonErrArray = jsonObject.getJSONArray("active_obd_err");
			if(jsonErrArray.length() > 0){
				String error = "";
				for(int i = 0 ; i < jsonErrArray.length() ; i++){
					error += jsonErrArray.get(i).toString() + ",";
				}
				tv_guzhang.setText(error.substring(0, error.length() - 1));
				tv_guzhang.setTextColor(getResources().getColor(R.color.yellow));
				Drawable drawable= getResources().getDrawable(R.drawable.icon_guzhang_normal);
				drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
				tv_guzhang_icon.setCompoundDrawables(drawable,null,null,null);
			}else{
				tv_guzhang.setText("无故障码");
				tv_guzhang.setTextColor(getResources().getColor(R.color.blue));
				Drawable drawable= getResources().getDrawable(R.drawable.icon_guzhang_abnormal);
				drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
				tv_guzhang_icon.setCompoundDrawables(drawable,null,null,null);
			}
			//电源系统
			boolean if_dpdy_err = jsonObject.getBoolean("if_dpdy_err");			
			if(if_dpdy_err){
				tv_dianyuan.setText("蓄电池状态良好");
				tv_dianyuan.setTextColor(getResources().getColor(R.color.blue));
				Drawable drawable= getResources().getDrawable(R.drawable.icon_dianyuan_normal);
				drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
				tv_dianyuan_icon.setCompoundDrawables(drawable,null,null,null);
			}else{
				tv_dianyuan.setText("蓄电池状态异常");
				tv_dianyuan.setTextColor(getResources().getColor(R.color.yellow));
				Drawable drawable= getResources().getDrawable(R.drawable.icon_dianyuan_abnormal);
				drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
				tv_dianyuan_icon.setCompoundDrawables(drawable,null,null,null);
			}
			//进气系统
			boolean if_jqmkd_err = jsonObject.getBoolean("if_jqmkd_err");
			if(if_jqmkd_err){
				tv_jinqi.setText("蓄电池状态良好");
				tv_jinqi.setTextColor(getResources().getColor(R.color.blue));
				Drawable drawable= getResources().getDrawable(R.drawable.icon_jinqi_normal);
				drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
				tv_jinqi_icon.setCompoundDrawables(drawable,null,null,null);
			}else{
				tv_jinqi.setText("蓄电池状态异常");
				tv_jinqi.setTextColor(getResources().getColor(R.color.yellow));
				Drawable drawable= getResources().getDrawable(R.drawable.icon_jinqi_abnormal);
				drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
				tv_jinqi_icon.setCompoundDrawables(drawable,null,null,null);
			}
			//怠速控制系统
			boolean if_fdjzs_err = jsonObject.getBoolean("if_fdjzs_err");
			if(if_fdjzs_err){
				tv_daisu.setText("怠速稳定");
				tv_daisu.setTextColor(getResources().getColor(R.color.blue));
				Drawable drawable= getResources().getDrawable(R.drawable.icon_daisu_normal);
				drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
				tv_daisu_icon.setCompoundDrawables(drawable,null,null,null);
			}else{
				tv_daisu.setText("怠速异常");
				tv_daisu.setTextColor(getResources().getColor(R.color.yellow));
				Drawable drawable= getResources().getDrawable(R.drawable.icon_daisu_abnormal);
				drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
				tv_daisu_icon.setCompoundDrawables(drawable,null,null,null);
			}
			//冷却系统
			boolean if_sw_err = jsonObject.getBoolean("if_sw_err");
			//tv_lengque
			if(if_sw_err){
				tv_lengque.setText("水温正常");
				tv_lengque.setTextColor(getResources().getColor(R.color.blue));
				Drawable drawable= getResources().getDrawable(R.drawable.icon_lengque_normal);
				drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
				tv_lengque_icon.setCompoundDrawables(drawable,null,null,null);
			}else{
				tv_lengque.setText("水温异常");
				tv_lengque.setTextColor(getResources().getColor(R.color.yellow));
				Drawable drawable= getResources().getDrawable(R.drawable.icon_lengque_abnormal);
				drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
				tv_lengque_icon.setCompoundDrawables(drawable,null,null,null);
			}
			//排放系统
			boolean if_chqwd_err = jsonObject.getBoolean("if_chqwd_err");
			if(if_chqwd_err){
				tv_paifang.setText("三元催化剂状态良好");
				tv_paifang.setTextColor(getResources().getColor(R.color.blue));
				Drawable drawable= getResources().getDrawable(R.drawable.icon_paifang_normal);
				drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
				tv_paifang_icon.setCompoundDrawables(drawable,null,null,null);
			}else{
				tv_paifang.setText("三元催化剂状态异常");
				tv_paifang.setTextColor(getResources().getColor(R.color.yellow));
				Drawable drawable= getResources().getDrawable(R.drawable.icon_paifang_abnormal);
				drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
				tv_paifang_icon.setCompoundDrawables(drawable,null,null,null);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}