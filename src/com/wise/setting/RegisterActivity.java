package com.wise.setting;

import java.util.regex.Pattern;
import org.json.JSONException;
import org.json.JSONObject;
import pubclas.Constant;
import pubclas.NetThread;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 注册界面
 * 
 * @author Administrator
 * 
 */
public class RegisterActivity extends Activity {
	private static final int exists = 1;
	
	TextView tv_title,tv_note;
	EditText et_account;
	boolean isPhone = true;
	String account;
	/**
	 * 0 注册 ， 1 重置 ，2第三方注册,3修改手机,4修改邮箱
	 */
	int mark = 0;
	String platform = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		AppApplication.getActivityInstance().addActivity(this);
		setContentView(R.layout.activity_register);
		ImageView iv_back = (ImageView)findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		tv_title = (TextView)findViewById(R.id.tv_title);
		tv_note = (TextView)findViewById(R.id.tv_note);
		TextView tv_note = (TextView)findViewById(R.id.tv_note);
		Button bt_register = (Button) findViewById(R.id.bt_register);
		bt_register.setOnClickListener(onClickListener);
		et_account = (EditText) findViewById(R.id.et_account);
		Intent intent = getIntent();
		mark = intent.getIntExtra("mark", 0);
		platform = intent.getStringExtra("platform");
		if(mark == 0 || mark == 2){
			tv_title.setText("注册");
			bt_register.setText("注册");
			tv_note.setVisibility(View.VISIBLE);
		}else if(mark == 1){
			tv_title.setText("忘记密码");
			bt_register.setText("忘记密码");
			tv_note.setVisibility(View.GONE);
		}else if(mark == 3){
			tv_title.setText("修改手机");
			bt_register.setText("下一步");
			tv_note.setVisibility(View.GONE);
			et_account.setHint("请输入要修改的手机号码");
		}else if(mark == 4){
			tv_title.setText("修改邮箱");
			bt_register.setText("下一步");
			tv_note.setVisibility(View.GONE);
			et_account.setHint("请输入要修改的邮箱");
		}
		setNote();
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.bt_register:
				if(mark == 3){
					RegisterPhone();
				}else if(mark == 4){
					RegisterEmail();
				}else{
					Register();	
				}			
				break;
			case R.id.iv_back:
				finish();
				break;
			}
		}
	};
	
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case exists:
				jsonExists(msg.obj.toString());
				break;

			default:
				break;
			}
		}		
	};
	
	private void RegisterPhone(){
		account = et_account.getText().toString().trim();
		String url = Constant.BaseUrl + "exists?query_type=6&value=" + account;
		if (account.equals("")) {
			Toast.makeText(RegisterActivity.this, "请填写手机号码",Toast.LENGTH_SHORT).show();
		} else if (account.length() == 11 && isNumeric(account)) {
			isPhone = true;
			new Thread(new NetThread.GetDataThread(handler, url, exists)).start();
		}else{
			Toast.makeText(RegisterActivity.this, "您手机号码格式不正确",
					Toast.LENGTH_SHORT).show();
		}
	}
	private void RegisterEmail(){
		account = et_account.getText().toString().trim();
		String url = Constant.BaseUrl + "exists?query_type=6&value=" + account;
		if (account.equals("")) {
			Toast.makeText(RegisterActivity.this, "请填写邮箱",
					Toast.LENGTH_SHORT).show();
		} else if(isEmail(account)){
			isPhone = false;
			new Thread(new NetThread.GetDataThread(handler, url, exists)).start();
		}else{
			Toast.makeText(RegisterActivity.this, "您输入的邮箱格式不正确",
					Toast.LENGTH_SHORT).show();
		}
	}
	
	private void Register() {
		account = et_account.getText().toString().trim();
		String url = Constant.BaseUrl + "exists?query_type=6&value=" + account;
		if (account.equals("")) {
			Toast.makeText(RegisterActivity.this, "请填写手机号码或邮箱",
					Toast.LENGTH_SHORT).show();
		} else if (account.length() == 11 && isNumeric(account)) {
			isPhone = true;
			new Thread(new NetThread.GetDataThread(handler, url, exists)).start();
		}else if(isEmail(account)){
			isPhone = false;
			new Thread(new NetThread.GetDataThread(handler, url, exists)).start();
		}else{
			Toast.makeText(RegisterActivity.this, "您输入的账号不正确",
					Toast.LENGTH_SHORT).show();
		}
	}
	private void jsonExists(String result){
		try {
			JSONObject jsonObject = new JSONObject(result);
			boolean isExist = jsonObject.getBoolean("exist");
			if(isExist){//true ,账号已存在
				if(mark == 0){
					Toast.makeText(RegisterActivity.this, "该账号已注册，请登录", Toast.LENGTH_SHORT).show();
				}else if(mark == 1){//重置密码
					new AlertDialog.Builder(RegisterActivity.this)    
					.setTitle("确认")  
					.setMessage("我们将发送验证码到以上账号\n" + account)  
					.setPositiveButton("好", new DialogInterface.OnClickListener() {						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent intent = new Intent(RegisterActivity.this,CaptchaActivity.class);
							intent.putExtra("account", account);
							intent.putExtra("isPhone", isPhone);
							intent.putExtra("mark", mark);	
							startActivityForResult(intent, 2);
						}
					}).setNegativeButton("取消", null)
					.show(); 
				}else if(mark == 2){
					Toast.makeText(RegisterActivity.this, "该账号已注册，不能重复注册", Toast.LENGTH_SHORT).show();
				}else if(mark == 3){
					Toast.makeText(RegisterActivity.this, "该手机号码已注册，请重新输入", Toast.LENGTH_SHORT).show();
				}else if(mark == 4){
					Toast.makeText(RegisterActivity.this, "该邮箱已注册，请重新输入", Toast.LENGTH_SHORT).show();
				}
			}else{//false,可以注册
				if(mark == 0 || mark == 2){
					new AlertDialog.Builder(RegisterActivity.this)    
					.setTitle("确认手机账号")  
					.setMessage("我们将发送验证码到以上账号\n" + account)  
					.setPositiveButton("好", new DialogInterface.OnClickListener() {						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent intent = new Intent(RegisterActivity.this,CaptchaActivity.class);
							intent.putExtra("account", account);
							intent.putExtra("isPhone", isPhone);
							intent.putExtra("mark", mark);							
							intent.putExtra("platform", platform);							
							startActivityForResult(intent, 2);
						}
					}).setNegativeButton("取消", null)
					.show(); 
				}else if(mark == 1){//没有账号
					Toast.makeText(RegisterActivity.this, "该账号不存在，请重新输入", Toast.LENGTH_SHORT).show();
				}else if(mark == 3 ||mark == 4){
					Intent intent = new Intent(RegisterActivity.this,CaptchaActivity.class);
					intent.putExtra("account", account);
					intent.putExtra("isPhone", isPhone);
					intent.putExtra("mark", mark);	
					startActivityForResult(intent, 1);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	public static boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		return pattern.matcher(str).matches();
	}
	public static boolean isEmail(String str){
		Pattern pattern = Pattern.compile("^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$");
		return pattern.matcher(str).matches();
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		System.out.println("requestCode = " + requestCode + " , resultCode = " + resultCode);
		switch (resultCode) {
		case 2:
			setResult(2,data);
			finish();
			break;

		default:
			break;
		}
	}
	
	private void setNote(){
		SpannableString sp = new SpannableString("点击上面的注册按钮，即表示同意《叭叭软件许可及服务条款》");
		sp.setSpan(new URLSpan("http://www.baidu.com"), 16, 27,
		Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		tv_note.setText(sp);
		tv_note.setMovementMethod(LinkMovementMethod.getInstance());
	}
}