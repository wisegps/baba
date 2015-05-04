package com.wise.setting;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.umeng.analytics.MobclickAgent;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Constant;
import com.wise.baba.biz.GetSystem;
import com.wise.baba.net.NetThread;
import com.wise.baba.ui.adapter.OpenDateDialog;
import com.wise.baba.ui.adapter.OpenDateDialogListener;
import com.wise.baba.ui.widget.PopView;
import com.wise.baba.ui.widget.PopView.OnItemClickListener;
import com.wise.baba.util.Blur;
import com.wise.baba.util.UploadUtil;
import com.wise.baba.util.UploadUtil.OnUploadProcessListener;


/**
 * 个人信息
 * 
 * @author honesty
 * 
 */
public class AccountActivity extends Activity implements OnUploadProcessListener {
	private static final int get_customer = 1;
	private static final int set_sex = 2;
	private static final int set_birth = 3;
	private static final int update_pic = 4;
	TextView tv_phone, tv_name, tv_email, tv_sex, tv_birth;
	ImageView iv_pic;
	RequestQueue mQueue;
	String birth;
	AppApplication app;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		app = (AppApplication) getApplication();
		setContentView(R.layout.activity_account);
		mQueue = Volley.newRequestQueue(this);
		ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		iv_pic = (ImageView) findViewById(R.id.iv_pic);
		iv_pic.setOnClickListener(onClickListener);
		tv_phone = (TextView) findViewById(R.id.tv_phone);
		tv_phone.setOnClickListener(onClickListener);
		tv_name = (TextView) findViewById(R.id.tv_name);
		tv_name.setOnClickListener(onClickListener);
		tv_email = (TextView) findViewById(R.id.tv_email);
		tv_email.setOnClickListener(onClickListener);
		tv_sex = (TextView) findViewById(R.id.tv_sex);
		tv_sex.setOnClickListener(onClickListener);
		tv_birth = (TextView) findViewById(R.id.tv_birth);
		tv_birth.setOnClickListener(onClickListener);
		TextView tv_update_pwd = (TextView) findViewById(R.id.tv_update_pwd);
		tv_update_pwd.setOnClickListener(onClickListener);
		jsonCustomer();
		OpenDateDialog.SetCustomDateListener(new OpenDateDialogListener() {
			@Override
			public void OnDateChange(String Date, int index) {
				switch (index) {
				case R.id.tv_birth:
					String url = Constant.BaseUrl + "customer/" + app.cust_id + "/field?auth_code=" + app.auth_code;
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("field_name", "birth"));
					params.add(new BasicNameValuePair("field_type", "Date"));
					params.add(new BasicNameValuePair("field_value", Date));
					new Thread(new NetThread.putDataThread(handler, url, params, set_birth)).start();
					birth = Date;
					tv_birth.setText(Date);
					break;
				}
			}
		});
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			case R.id.tv_name:
				if (app.isTest) {
					Toast.makeText(AccountActivity.this, "演示账号不支持该功能", Toast.LENGTH_SHORT).show();
					return;
				}
				String name = tv_name.getText().toString().trim();
				Intent intent2 = new Intent(AccountActivity.this, NameActivity.class);
				intent2.putExtra("name", name);
				startActivityForResult(intent2, 1);
				break;
			case R.id.tv_sex:
				if (app.isTest) {
					Toast.makeText(AccountActivity.this, "演示账号不支持该功能", Toast.LENGTH_SHORT).show();
					return;
				}
				setSex();
				break;
			case R.id.tv_update_pwd:
				if (app.isTest) {
					Toast.makeText(AccountActivity.this, "演示账号不支持该功能", Toast.LENGTH_SHORT).show();
					return;
				}
				startActivity(new Intent(AccountActivity.this, UpdatePwdActivity.class));
				break;
			case R.id.tv_phone:
				if (app.isTest) {
					Toast.makeText(AccountActivity.this, "演示账号不支持该功能", Toast.LENGTH_SHORT).show();
					return;
				}
				Intent intent = new Intent(AccountActivity.this, RegisterActivity.class);
				intent.putExtra("mark", 3);
				intent.putExtra("phone", tv_phone.getText().toString().trim());
				startActivityForResult(intent, 1);
				break;
			case R.id.tv_email:
				if (app.isTest) {
					Toast.makeText(AccountActivity.this, "演示账号不支持该功能", Toast.LENGTH_SHORT).show();
					return;
				}
				Intent intent1 = new Intent(AccountActivity.this, RegisterActivity.class);
				intent1.putExtra("mark", 4);
				intent1.putExtra("email", tv_email.getText().toString().trim());
				startActivityForResult(intent1, 1);
				break;
			case R.id.iv_pic:
				if (app.isTest) {
					Toast.makeText(AccountActivity.this, "演示账号不支持该功能", Toast.LENGTH_SHORT).show();
					return;
				}
				picPop();
				break;
			case R.id.tv_birth:
				if (app.isTest) {
					Toast.makeText(AccountActivity.this, "演示账号不支持该功能", Toast.LENGTH_SHORT).show();
					return;
				}
				OpenDateDialog.ShowDate(AccountActivity.this, R.id.tv_birth, birth);
				break;
			}
		}
	};

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case set_sex:
				// 修改性别刷新
				GetCustomer();
				break;
			case get_customer:
				jsonCustomer(msg.obj.toString());
				break;
			case set_birth:
				// 修改生日后刷新
				GetCustomer();
				break;
			case update_pic:
				GetCustomer();
				break;
			}
		}
	};

	private void jsonCustomer() {
		try {
			SharedPreferences preferences = getSharedPreferences(Constant.sharedPreferencesName, Context.MODE_PRIVATE);
			String customer = preferences.getString(Constant.sp_customer + app.cust_id, "");
			JSONObject jsonObject = new JSONObject(customer);
			tv_phone.setText(jsonObject.getString("mobile"));
			tv_name.setText(jsonObject.getString("cust_name"));
			tv_email.setText(jsonObject.getString("email"));
			if (jsonObject.getString("sex").equals("0")) {
				tv_sex.setText("男");
			} else {
				tv_sex.setText("女");
			}
			birth = jsonObject.getString("birth").substring(0, 10);
			tv_birth.setText(birth);
			String logo = jsonObject.getString("logo");
			if (logo == null || logo.equals("")) {

			} else {
				mQueue.add(new ImageRequest(logo, new Response.Listener<Bitmap>() {
					@Override
					public void onResponse(Bitmap response) {
						iv_pic.setImageBitmap(response);
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

	String[] Sexs = { "男", "女" };

	private void setSex() {
		new AlertDialog.Builder(AccountActivity.this).setTitle("请选择性别").setItems(Sexs, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String url = Constant.BaseUrl + "customer/" + app.cust_id + "/field?auth_code=" + app.auth_code;
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("field_name", "sex"));
				params.add(new BasicNameValuePair("field_type", "Number"));
				params.add(new BasicNameValuePair("field_value", String.valueOf(which)));
				new Thread(new NetThread.putDataThread(handler, url, params, set_sex)).start();
				switch (which) {
				case 0:
					tv_sex.setText("男");
					break;
				case 1:
					tv_sex.setText("女");
					break;
				}
			}
		}).setNegativeButton("取消", null).show();
	}

	private void picPop() {
		List<String> items = new ArrayList<String>();
		items.add("拍照");
		items.add("从手机相册中选取");
		final PopView popView = new PopView(this);
		popView.initView(findViewById(R.id.iv_pic));
		popView.setData(items);
		popView.SetOnItemClickListener(new OnItemClickListener() {
			@Override
			public void OnItemClick(int index) {
				switch (index) {
				case 0:
					Intent intent1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					startActivityForResult(intent1, 1);
					popView.dismiss();
					break;

				case 1:
					Intent intent = new Intent();
					/* 开启Pictures画面Type设定为image */
					intent.setType("image/*");
					/* 使用Intent.ACTION_GET_CONTENT这个Action */
					intent.setAction(Intent.ACTION_GET_CONTENT);
					/* 取得相片后返回本画面 */
					startActivityForResult(intent, 9);
					popView.dismiss();
					break;
				}
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 9 && resultCode == Activity.RESULT_OK) {
			Uri uri = data.getData();
			Log.e("uri", uri.toString());
			ContentResolver cr = this.getContentResolver();
			try {
				Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
				UpdateBitmap(bitmap);
			} catch (FileNotFoundException e) {
				Log.e("Exception", e.getMessage(), e);
			}
			return;
		} else if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
			String sdStatus = Environment.getExternalStorageState();
			if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用
				Log.v("TestFile", "SD card is not avaiable/writeable right now.");
				return;
			}
			Bundle bundle = data.getExtras();
			Bitmap bitmap = (Bitmap) bundle.get("data");// 获取相机返回的数据，并转换为Bitmap图片格式
			UpdateBitmap(bitmap);
		}
		switch (resultCode) {
		case 1:
			tv_name.setText(data.getStringExtra("name"));
			break;

		case 2:
			boolean isPhone = data.getBooleanExtra("isPhone", true);
			String account = data.getStringExtra("account");
			if (isPhone) {
				tv_phone.setText(account);
			} else {
				tv_email.setText(account);
			}

			GetCustomer();
			break;
		}
	}

	/** 修改完手机或邮箱后刷新本地数据 **/
	private void GetCustomer() {
		String url = Constant.BaseUrl + "customer/" + app.cust_id + "?auth_code=" + app.auth_code;
		new Thread(new NetThread.GetDataThread(handler, url, get_customer)).start();
	}

	private void jsonCustomer(String str) {
		try {
			JSONObject jsonObject = new JSONObject(str);
			String mobile = jsonObject.getString("mobile");
			String email = jsonObject.getString("email");
			String password = jsonObject.getString("password");
			app.cust_name = jsonObject.getString("cust_name");
			SharedPreferences preferences = getSharedPreferences(Constant.sharedPreferencesName, Context.MODE_PRIVATE);
			Editor editor = preferences.edit();
			editor.putString(Constant.sp_customer + app.cust_id, str);
			editor.putString(Constant.sp_pwd, password);
			if (mobile.equals("")) {
				editor.putString(Constant.sp_account, email);
			} else {
				editor.putString(Constant.sp_account, mobile);
			}
			editor.commit();
		} catch (JSONException e) {
			e.printStackTrace();
			finish();
		}
	}

	String fileName;

	private void UpdateBitmap(Bitmap bitmap) {
		File filePath = new File(Constant.userIconPath);
		if (!filePath.exists()) {
			filePath.mkdirs();
		}
		bitmap = Blur.scaleImage(bitmap, 150);
		bitmap = Blur.getSquareBitmap(bitmap);
		FileOutputStream b = null;
		fileName = Constant.userIconPath + app.cust_id + ".png";
		try {
			b = new FileOutputStream(fileName);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, b);// 把数据写入文件
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				b.flush();
				b.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		iv_pic.setImageBitmap(bitmap);

		String url = Constant.BaseUrl + "upload_image?auth_code=" + app.auth_code;
		UploadUtil.getInstance().setOnUploadProcessListener(AccountActivity.this);
		UploadUtil.getInstance().uploadFile(fileName, "image", url, new HashMap<String, String>());
	}

	private void jsonUpdatePic(String str) {
		try {
			JSONObject jsonObject = new JSONObject(str);
			if (jsonObject.getString("status_code").equals("0")) {
				String ImageUrl = jsonObject.getString("image_file_url");
				File file = new File(fileName);
				file.renameTo(new File(Constant.userIconPath + GetSystem.getM5DEndo(ImageUrl) + ".png"));
				String url = Constant.BaseUrl + "customer/" + app.cust_id + "/field?auth_code=" + app.auth_code;
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("field_name", "logo"));
				params.add(new BasicNameValuePair("field_type", "String"));
				params.add(new BasicNameValuePair("field_value", ImageUrl));
				new Thread(new NetThread.putDataThread(handler, url, params, update_pic)).start();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onUploadDone(int responseCode, String message) {
		if (responseCode == 1) {
			jsonUpdatePic(message);
		} else if (responseCode == 2) {
			Toast.makeText(AccountActivity.this, "文件不存在", Toast.LENGTH_SHORT).show();
		} else if (responseCode == 3) {
			Toast.makeText(AccountActivity.this, "服务器接受失败", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onUploadProcess(int uploadSize) {
	}

	@Override
	public void initUpload(int fileSize) {
	}

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
}