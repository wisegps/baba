package com.wise.baba;

import java.util.LinkedHashSet;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;
import cn.sharesdk.framework.ShareSDK;
import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.FeedbackAgent;
import com.umeng.update.UpdateConfig;
import com.wise.state.FaultActivity;
import customView.WaitLinearLayout;
import customView.WaitLinearLayout.OnFinishListener;
import pubclas.Constant;
import pubclas.GetSystem;
import pubclas.JsonData;
import pubclas.NetThread;
import pubclas.Variable;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;

public class WelcomeActivity extends Activity implements TagAliasCallback{
	private static final String TAG = "WelcomeActivity";
	private static final int login_account = 1;
	private static final int Wait = 2;
	private static final int get_data = 3;
	private static final int get_customer = 4;
    
    /**是否自动登录**/
    boolean isLogin = true;
    /**是否登录完毕**/
    boolean isLoging = false;
    /**登录发生异常**/
    boolean isException = false;
    
    boolean isWait = false;
    /**是否从通知栏里启动**/
    boolean isSpecify = false;
    Bundle bundle;
    
    WaitLinearLayout ll_wait;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		ShareSDK.initSDK(this);
		setContentView(R.layout.activity_welcome);
		clearData();
		GetSystem.myLog(TAG, "onCreate");
		Intent intent = getIntent();
		
		isSpecify = intent.getBooleanExtra("isSpecify", false);
		bundle = intent.getExtras();
		
		ll_wait = (WaitLinearLayout)findViewById(R.id.ll_wait);
		ll_wait.setOnFinishListener(onFinishListener);
		ll_wait.setWheelImage(R.drawable.wheel_white);
		ll_wait.setShadowImage(R.drawable.shadow_white);
		ll_wait.startWheel();
		
		getLogin();
		MobclickAgent.setDebugMode(true);
		//打印更新日志，正式发布去掉
		//UpdateConfig.setDebug(true);
		FeedbackAgent agent = new FeedbackAgent(this);
		agent.sync();
	}
	String strData = "";
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case Wait:
				isWait = true;
				TurnActivity();
				break;
			case login_account:
				jsonLogin(msg.obj.toString());
				break;
			case get_data:
				isLoging = true;
				strData = msg.obj.toString();
				TurnActivity();
				break;
			case get_customer:
				jsonCustomer(msg.obj.toString());
				break;
			}
		}
	};
	
	private void getLogin() {
		SharedPreferences preferences = getSharedPreferences(
				Constant.sharedPreferencesName, Context.MODE_PRIVATE);
		String sp_account = preferences.getString(Constant.sp_account, "");
		String sp_pwd = preferences.getString(Constant.sp_pwd, "");
		new WaitThread().start();
		if (sp_account.equals("")) {
			isLogin = false;
		} else {// 登录
			String url = Constant.BaseUrl + "user_login?account=" + sp_account + "&password=" + sp_pwd;
			new NetThread.GetDataThread(handler, url, login_account).start();
		}
	}
	//解析登录
	private void jsonLogin(String str){
		if(str.equals("")){
			GetSystem.myLog(TAG, "网络连接异常");
			isException = true;
			TurnActivity();
		}else{
			try {
				JSONObject jsonObject = new JSONObject(str);
				if(jsonObject.getString("status_code").equals("0")){
					Variable.cust_id = jsonObject.getString("cust_id");
					Variable.auth_code = jsonObject.getString("auth_code");
					setJpush();
					GetCustomer();
			        getData();		        
				}else{
					isLogin = false;
					TurnActivity();
				}
			} catch (JSONException e) {
				e.printStackTrace();
				isException = true;
				TurnActivity();
			}
		}		
	}
	private void GetCustomer() {
		String url = Constant.BaseUrl + "customer/" + Variable.cust_id
				+ "?auth_code=" + Variable.auth_code;
		new NetThread.GetDataThread(handler, url, get_customer).start();
	}
	private void jsonCustomer(String str) {
		SharedPreferences preferences1 = getSharedPreferences(Constant.sharedPreferencesName, Context.MODE_PRIVATE);
        Editor editor1 = preferences1.edit();
        editor1.putString(Constant.sp_customer + Variable.cust_id, str);
        editor1.commit();
//		try {
//			JSONObject jsonObject = new JSONObject(str);
//			String mobile = jsonObject.getString("mobile");
//			String email = jsonObject.getString("email");
//			String password = jsonObject.getString("password");
//			Variable.cust_name = jsonObject.getString("cust_name");
//			SharedPreferences preferences = getSharedPreferences(Constant.sharedPreferencesName, Context.MODE_PRIVATE);
//	        Editor editor = preferences.edit();
//	        editor.putString(Constant.sp_pwd, password);			
//			if(mobile.equals("")){
//		        editor.putString(Constant.sp_account, email);
//			}else{
//				editor.putString(Constant.sp_account, mobile);
//			}
//	        editor.commit();
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
	}
	
	private void getData(){
		String url = Constant.BaseUrl + "customer/" + Variable.cust_id + "/vehicle?auth_code=" + Variable.auth_code;
		new NetThread.GetDataThread(handler, url, get_data).start();
	}

	class WaitThread extends Thread {
		@Override
		public void run() {
			super.run();
			try {
				Thread.sleep(3000);
				Message message = new Message();
				message.what = Wait;
				handler.sendMessage(message);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void TurnActivity() {
        if (isWait) {//城市读取完毕，延时        	
        	if(!isLogin){
        		//未登录跳转
            	ll_wait.runFast();
        	}else{
        		if(isException){//程序异常
        			GetSystem.myLog(TAG, "isException runFast");
        			ll_wait.runFast();
        		}else if(isLoging){//登录流程走完
        			GetSystem.myLog(TAG, "isLoging runFast");
                	ll_wait.runFast();
            	}
        	}
        }
    }
	@Override
	public void gotResult(int arg0, String arg1, Set<String> arg2) {
		GetSystem.myLog(TAG, "arg0 = " + arg0 + " , arg1 = " + arg1);
	}
	private void setJpush(){
		JPushInterface.resumePush(getApplicationContext());
		GetSystem.myLog(TAG, "setJpush");
        Set<String> tagSet = new LinkedHashSet<String>();
        tagSet.add(Variable.cust_id);
        //调用JPush API设置Tag
        JPushInterface.setAliasAndTags(getApplicationContext(), null, tagSet, this);
	}	
	
	OnFinishListener onFinishListener = new OnFinishListener() {
		@Override
		public void OnFinish(int index) {
			GetSystem.myLog(TAG, "runFast OnFinish");
    		//未登录跳转
    		SharedPreferences preferences = getSharedPreferences(
    				Constant.sharedPreferencesName, Context.MODE_PRIVATE);
    		Variable.City = preferences.getString(Constant.sp_city, "");
			if(!isLogin){
        		//是否需要选择城市
        		if(Variable.City.equals("")){
        			Intent intent = new Intent(WelcomeActivity.this, SelectCityActivity.class);
    				intent.putExtra("Welcome", true);
    				startActivity(intent);
    				finish();
        		}else{
        			Intent intent = new Intent(WelcomeActivity.this, FaultActivity.class);
    				startActivity(intent);
    				finish();
        		}        		
        	}else{
        		if(isException){//程序异常
        			GetSystem.myLog(TAG, "runFast isException");
        			Intent intent = new Intent(WelcomeActivity.this, FaultActivity.class);
    				startActivity(intent);
    				finish();
        		}else if(isLoging){//登录流程走完
        			GetSystem.myLog(TAG, "runFast isLoging");
        			Variable.carDatas.clear();
        			Variable.carDatas.addAll(JsonData.jsonCarInfo(strData));
            		Intent intent = new Intent(WelcomeActivity.this, FaultActivity.class);
    				if(isSpecify){
    	    			intent.putExtra("isSpecify", isSpecify);
    	    			intent.putExtras(bundle);
    				}
    				startActivity(intent);
    				finish();
            	}
        	}
		}
	};
	
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
	/**清空静态数据**/
	private void clearData(){
		Variable.auth_code = null;
		Variable.cust_id = null;
		Variable.cust_name = "";
		Variable.carDatas.clear();
	}
}