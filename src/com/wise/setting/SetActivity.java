package com.wise.setting;

import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import pubclas.Constant;
import pubclas.GetSystem;
import pubclas.Judge;
import pubclas.NetThread;
import pubclas.Variable;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qzone.QZone;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.FeedbackAgent;
import com.wise.baba.AboutActivity;
import com.wise.baba.R;
import com.wise.baba.SelectCityActivity;
import com.wise.car.CarActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
/**
 * 设置界面
 * @author Administrator
 *
 */
public class SetActivity extends Activity implements TagAliasCallback{
	
	private static final String TAG = "SetActivity";
	private static final int get_customer = 2;
	private static final int get_pic = 3;
	
	TextView tv_login,tv_city;
	ImageView iv_logo;
	Button bt_login_out;
	RequestQueue mQueue;
	Platform platformQQ;
    Platform platformSina;
    Platform platformWhat;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_set);
        JPushInterface.init(getApplicationContext());
		mQueue = Volley.newRequestQueue(this);
		ShareSDK.initSDK(this);
        platformQQ = ShareSDK.getPlatform(SetActivity.this, QZone.NAME);
        platformSina = ShareSDK.getPlatform(SetActivity.this, SinaWeibo.NAME);
        RelativeLayout rl_login = (RelativeLayout)findViewById(R.id.rl_login);
        rl_login.setOnClickListener(onClickListener);
        RelativeLayout rl_city = (RelativeLayout)findViewById(R.id.rl_city);
        rl_city.setOnClickListener(onClickListener);
		tv_login = (TextView)findViewById(R.id.tv_login);
		tv_city = (TextView)findViewById(R.id.tv_city);
		iv_logo = (ImageView)findViewById(R.id.iv_logo);
		ImageView iv_back = (ImageView)findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		TextView tv_car = (TextView)findViewById(R.id.tv_car);
		tv_car.setOnClickListener(onClickListener);
		TextView tv_feedback = (TextView)findViewById(R.id.tv_feedback);
		tv_feedback.setOnClickListener(onClickListener);
		TextView tv_about = (TextView)findViewById(R.id.tv_about);
		tv_about.setOnClickListener(onClickListener);
		bt_login_out = (Button)findViewById(R.id.bt_login_out);
		bt_login_out.setOnClickListener(onClickListener);
		getSpData();
		
		String url = Constant.BaseUrl + "customer/" + Variable.cust_id
				+ "?auth_code=" + Variable.auth_code;
		new Thread(new NetThread.GetDataThread(handler, url, get_customer))
				.start();
	}
	OnClickListener onClickListener = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			case R.id.rl_login:
				if(!Judge.isLogin()){
					startActivityForResult(new Intent(SetActivity.this, LoginActivity.class), 1);
				}else{
					startActivity(new Intent(SetActivity.this, AccountActivity.class));
				}
				break;
			case R.id.tv_car:
				if(!Judge.isLogin()){
					startActivity(new Intent(SetActivity.this, LoginActivity.class));
				}else{
					startActivity(new Intent(SetActivity.this, CarActivity.class));
				}
				break;
			case R.id.bt_login_out:
				SharedPreferences preferences = getSharedPreferences(Constant.sharedPreferencesName, Context.MODE_PRIVATE);
		        Editor editor = preferences.edit();
		        editor.putString(Constant.sp_pwd, "");
			    editor.putString(Constant.sp_account, "");
		        editor.commit();
		        Variable.cust_id = null ;
		        Variable.auth_code = null;
		        Variable.carDatas.clear();
		        Intent intent = new Intent(Constant.A_LoginOut);
	            sendBroadcast(intent);
		        bt_login_out.setVisibility(View.GONE);
		        tv_login.setText("登录/注册");
		        iv_logo.setImageResource(R.drawable.icon_add);
		        platformQQ.removeAccount();
		        platformSina.removeAccount();
		        JPushInterface.stopPush(getApplicationContext());
				break;
			case R.id.tv_feedback:
				//startActivity(new Intent(SetActivity.this, FeedBackActivity.class));
				FeedbackAgent agent = new FeedbackAgent(SetActivity.this);
			    agent.startFeedbackActivity();
				break;
			case R.id.tv_about:
				startActivity(new Intent(SetActivity.this, AboutActivity.class));
				break;
			case R.id.rl_city:
				startActivityForResult(new Intent(SetActivity.this, SelectCityActivity.class), 2);
				break;
			}
		}
	};
	
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case get_customer:
				jsonCustomer(msg.obj.toString(),true);
				break;
			case get_pic:
				iv_logo.setImageBitmap(bitmap);
				break;
			}
		}		
	};
	
	private void getSpData(){
		tv_city.setText(Variable.City);
	}
	
	private void GetCustomer() {
		SharedPreferences preferences = getSharedPreferences(
				Constant.sharedPreferencesName, Context.MODE_PRIVATE);
		String customer = preferences.getString(Constant.sp_customer + Variable.cust_id, "");
		if(customer.equals("")){
			
		}else{
			jsonCustomer(customer,false);
		}
		
	}
	/**获取个人信息**/
	private void jsonCustomer(String str,boolean isSave) {
		if(isSave){
			SharedPreferences preferences1 = getSharedPreferences(Constant.sharedPreferencesName, Context.MODE_PRIVATE);
	        Editor editor1 = preferences1.edit();
	        editor1.putString(Constant.sp_customer + Variable.cust_id, str);
	        editor1.commit();
		}		
		try {
			JSONObject jsonObject = new JSONObject(str);			
			if(jsonObject.opt("status_code") == null){
				bt_login_out.setVisibility(View.VISIBLE);
				String mobile = jsonObject.getString("mobile");
				String email = jsonObject.getString("email");
				String password = jsonObject.getString("password");
				Variable.cust_name = jsonObject.getString("cust_name");
				SharedPreferences preferences = getSharedPreferences(Constant.sharedPreferencesName, Context.MODE_PRIVATE);
		        Editor editor = preferences.edit();
		        editor.putString(Constant.sp_pwd, password);			
				if(mobile.equals("")){
			        editor.putString(Constant.sp_account, email);
				}else{
					editor.putString(Constant.sp_account, mobile);
				}
		        editor.commit();
		        
		        Bitmap bimage = BitmapFactory.decodeFile(Constant.userIconPath + Variable.cust_id + ".png");
		        if(bimage != null){
		        	iv_logo.setImageBitmap(bimage);
		        }
		        
		        tv_login.setText(jsonObject.getString("cust_name"));
		        String logo = jsonObject.getString("logo");
		        mQueue.add(new ImageRequest(logo, new Response.Listener<Bitmap>() {
					@Override
					public void onResponse(Bitmap response) {
						GetSystem.saveImageSD(response, Constant.userIconPath, Variable.cust_id + ".png",100);
						iv_logo.setImageBitmap(response);
					}
				}, 0, 0, Config.RGB_565, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						error.printStackTrace();
					}
				}));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	Bitmap bitmap;
	class imageThread extends Thread{
		String url;
		public imageThread(String Url){
			url = Url;
		}
		@Override
		public void run() {
			super.run();
			bitmap = GetSystem.getBitmapFromURL(url);
			Message message = new Message();
			message.what = get_pic;
			handler.sendMessage(message);
		}
	}
		
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);		
		GetSystem.myLog(TAG, "requestCode = " + ",resultCode= " + resultCode);
		if(resultCode == 1){
			String url = Constant.BaseUrl + "customer/" + Variable.cust_id
					+ "?auth_code=" + Variable.auth_code;
			new Thread(new NetThread.GetDataThread(handler, url, get_customer))
					.start();
		}else if(resultCode == 2){
			tv_city.setText(Variable.City);
		}
	}
	@Override
	public void gotResult(int arg0, String arg1, Set<String> arg2) {}
	@Override
	protected void onResume() {
		super.onResume();
		GetCustomer();
		MobclickAgent.onResume(this);
	}
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
}