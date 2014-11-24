package com.wise.setting;

import java.util.Hashtable;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import pubclas.Constant;
import pubclas.GetSystem;
import pubclas.Judge;
import pubclas.NetThread;
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
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.FeedbackAgent;
import com.wise.baba.AboutActivity;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.SelectCityActivity;
import com.wise.car.CarActivity;
import com.wise.car.OfflineActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
/**
 * 设置界面
 * @author Administrator
 *
 */


public class SetActivity extends Activity implements TagAliasCallback {

	private static final String abc = "adafsd";
	private static final String TAG = "SetActivity";
	private static final int get_customer = 2;
	private static final int get_pic = 3;
	
	TextView tv_login,tv_city;
	ImageView iv_logo,iv_sex,iv_service,iv_eweima;
	Button bt_login_out;
	RequestQueue mQueue;
	Platform platformQQ;
    Platform platformSina;
    Platform platformWhat;
    AppApplication app;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_set);
		app = (AppApplication)getApplication();
        JPushInterface.init(getApplicationContext());
		mQueue = Volley.newRequestQueue(this);
		ShareSDK.initSDK(this);
        platformQQ = ShareSDK.getPlatform(SetActivity.this, QZone.NAME);
        platformSina = ShareSDK.getPlatform(SetActivity.this, SinaWeibo.NAME);
        RelativeLayout rl_login = (RelativeLayout)findViewById(R.id.rl_login);
        rl_login.setOnClickListener(onClickListener);
        RelativeLayout rl_city = (RelativeLayout)findViewById(R.id.rl_city);
        rl_city.setOnClickListener(onClickListener);
        iv_eweima = (ImageView)findViewById(R.id.iv_eweima);
        iv_eweima.setOnClickListener(onClickListener);
		tv_login = (TextView)findViewById(R.id.tv_login);
		tv_city = (TextView)findViewById(R.id.tv_city);
		iv_logo = (ImageView)findViewById(R.id.iv_logo);
		iv_sex = (ImageView)findViewById(R.id.iv_sex);
		iv_service = (ImageView)findViewById(R.id.iv_service);
		ImageView iv_back = (ImageView)findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		TextView tv_car = (TextView)findViewById(R.id.tv_car);
		tv_car.setOnClickListener(onClickListener);
		TextView tv_offline_map = (TextView)findViewById(R.id.tv_offline_map);
		tv_offline_map.setOnClickListener(onClickListener);
		TextView tv_feedback = (TextView) findViewById(R.id.tv_feedback);
		tv_feedback.setOnClickListener(onClickListener);
		TextView tv_about = (TextView)findViewById(R.id.tv_about);
		tv_about.setOnClickListener(onClickListener);
		bt_login_out = (Button)findViewById(R.id.bt_login_out);
		bt_login_out.setOnClickListener(onClickListener);
		getSpData();
		
		String url = Constant.BaseUrl + "customer/" + app.cust_id
				+ "?auth_code=" + app.auth_code;
		new NetThread.GetDataThread(handler, url, get_customer).start();
	}
	OnClickListener onClickListener = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			case R.id.rl_login:
				if(!Judge.isLogin(app)){
					startActivityForResult(new Intent(SetActivity.this, LoginActivity.class), 1);
				}else{
					startActivity(new Intent(SetActivity.this, AccountActivity.class));
				}
				break;
			case R.id.tv_car:
				if(!Judge.isLogin(app)){
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
		        app.cust_id = null ;
		        app.auth_code = null;
		        app.carDatas.clear();
		        Intent intent = new Intent(Constant.A_LoginOut);
	            sendBroadcast(intent);
	            iv_eweima.setVisibility(View.GONE);
		        bt_login_out.setVisibility(View.GONE);
		        tv_login.setText("登录/注册");
		        iv_sex.setVisibility(View.GONE);
		        iv_service.setVisibility(View.GONE);
		        iv_logo.setImageResource(R.drawable.icon_add);
		        platformQQ.removeAccount();
		        platformSina.removeAccount();
		        JPushInterface.stopPush(getApplicationContext());
				break;
			case R.id.tv_offline_map:
				startActivity(new Intent(SetActivity.this, OfflineActivity.class));
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
			case R.id.iv_eweima:
				//TODO 打开二维码
				openErWeiMa();
				startActivityForResult(new Intent(SetActivity.this,
						SelectCityActivity.class), 2);
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
		tv_city.setText(app.City);
	}
	
	private void GetCustomer() {
		SharedPreferences preferences = getSharedPreferences(
				Constant.sharedPreferencesName, Context.MODE_PRIVATE);
		String customer = preferences.getString(Constant.sp_customer + app.cust_id, "");
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
	        editor1.putString(Constant.sp_customer + app.cust_id, str);
	        editor1.commit();
		}
		try {
			JSONObject jsonObject = new JSONObject(str);			
			if(jsonObject.opt("status_code") == null){
				iv_eweima.setVisibility(View.VISIBLE);
				bt_login_out.setVisibility(View.VISIBLE);
				app.cust_name = jsonObject.getString("cust_name");
		        tv_login.setText(jsonObject.getString("cust_name"));
		        final String logo = jsonObject.getString("logo");
				Bitmap bimage = BitmapFactory.decodeFile(Constant.userIconPath
						+ GetSystem.getM5DEndo(logo) + ".png");
				if (bimage != null) {
					iv_logo.setImageBitmap(bimage);
				}
				iv_sex.setVisibility(View.VISIBLE);
				String sex = jsonObject.getString("sex");
				if (sex.equals("0")) {
					iv_sex.setImageResource(R.drawable.icon_man);
				} else {
					iv_sex.setImageResource(R.drawable.icon_woman);
				}
				int cust_type = jsonObject.getInt("cust_type");
				// 如果是服务商显示标志
				if (cust_type == 2) {
					iv_service.setVisibility(View.VISIBLE);
				} else {
					iv_service.setVisibility(View.GONE);
				}
				if (logo == null || logo.equals("")) {

				} else {
					mQueue.add(new ImageRequest(logo,
							new Response.Listener<Bitmap>() {
								@Override
								public void onResponse(Bitmap response) {
									GetSystem.saveImageSD(response,
											Constant.userIconPath, GetSystem.getM5DEndo(logo)
													+ ".png", 100);
									iv_logo.setImageBitmap(response);
								}
							}, 0, 0, Config.RGB_565,
							new Response.ErrorListener() {
								@Override
								public void onErrorResponse(VolleyError error) {
									error.printStackTrace();
								}
							}));
				}
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
	/**弹出二维码界面**/
	private void openErWeiMa(){
		//得到二维码
		Bitmap bitmap = createImage();
		if(bitmap == null){
			Toast.makeText(getApplicationContext(), "生成二维码失败", Toast.LENGTH_SHORT).show();
			return ;
		}
		LayoutInflater mLayoutInflater = LayoutInflater
				.from(SetActivity.this);
		View popunwindwow = mLayoutInflater.inflate(
				R.layout.pop_erweima, null);
		ImageView iv_erweima = (ImageView) popunwindwow
				.findViewById(R.id.iv_erweima);
		iv_erweima.setImageBitmap(bitmap);
		PopupWindow mPopupWindow = new PopupWindow(popunwindwow, LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		mPopupWindow.setAnimationStyle(R.style.PopupAnimation);
		mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		mPopupWindow.setFocusable(true);
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.showAtLocation(iv_logo, Gravity.CENTER, 0, 0);
	}
	private Bitmap createImage() {
        try {
        	DisplayMetrics dm = new DisplayMetrics();
    		getWindowManager().getDefaultDisplay().getMetrics(dm);
    		int width = dm.widthPixels;
        	int QR_WIDTH = width/2;
        	int QR_HEIGHT = width/2;
            // 需要引入core包
            QRCodeWriter writer = new QRCodeWriter();
            String text = app.cust_id;
            Log.i(TAG, "生成的文本：" + text);
            if (text == null || "".equals(text) || text.length() < 1) {
                return null;
            }
            // 把输入的文本转为二维码
            BitMatrix martix = writer.encode(text, BarcodeFormat.QR_CODE,
                    QR_WIDTH, QR_HEIGHT);

            System.out.println("w:" + martix.getWidth() + "h:"
                    + martix.getHeight());

            Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            BitMatrix bitMatrix = new QRCodeWriter().encode(text,
                    BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);
            int[] pixels = new int[QR_WIDTH * QR_HEIGHT];
            for (int y = 0; y < QR_HEIGHT; y++) {
                for (int x = 0; x < QR_WIDTH; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * QR_WIDTH + x] = 0xff000000;
                    } else {
                        pixels[y * QR_WIDTH + x] = 0xffffffff;
                    }

                }
            }

            Bitmap bitmap = Bitmap.createBitmap(QR_WIDTH, QR_HEIGHT,
                    Bitmap.Config.ARGB_8888);

            bitmap.setPixels(pixels, 0, QR_WIDTH, 0, 0, QR_WIDTH, QR_HEIGHT);
            return bitmap;

        } catch (WriterException e) {
            e.printStackTrace();
        }
		return bitmap;
    }
		
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);		
		GetSystem.myLog(TAG, "requestCode = " + ",resultCode= " + resultCode);
		if(requestCode == 1 && resultCode == 1){
			GetSystem.myLog(TAG, "登录返回");
			GetCustomer();
			setResult(1);
			finish();
		}else if(resultCode == 2){
			tv_city.setText(app.City);
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
