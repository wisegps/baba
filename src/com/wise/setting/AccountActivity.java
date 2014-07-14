package com.wise.setting;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import nadapter.OpenDateDialog;
import nadapter.OpenDateDialogListener;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import pubclas.BlurImage;
import pubclas.Constant;
import pubclas.NetThread;
import pubclas.UploadUtil;
import pubclas.UploadUtil.OnUploadProcessListener;
import pubclas.Variable;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.wise.baba.R;
import customView.PopView;
import customView.PopView.OnItemClickListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class AccountActivity extends Activity implements OnUploadProcessListener{

	private static final int get_customer = 1;

	TextView tv_phone,tv_name, tv_email, tv_sex,tv_birth;
	ImageView iv_pic;
	RequestQueue mQueue;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_account);
		mQueue = Volley.newRequestQueue(this);
		ImageView iv_back = (ImageView)findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		iv_pic = (ImageView)findViewById(R.id.iv_pic);
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
		TextView tv_update_pwd = (TextView)findViewById(R.id.tv_update_pwd);
		tv_update_pwd.setOnClickListener(onClickListener);
		GetCustomer();
		OpenDateDialog.SetCustomDateListener(new OpenDateDialogListener() {			
			@Override
			public void OnDateChange(String Date, int index) {
				switch (index) {
				case R.id.tv_birth:
					String url = Constant.BaseUrl + "customer/" + Variable.cust_id + "/field?auth_code=" + Variable.auth_code;
					List<NameValuePair> params = new ArrayList<NameValuePair>();
			        params.add(new BasicNameValuePair("field_name", "birth"));
			        params.add(new BasicNameValuePair("field_type", "Date"));
			        params.add(new BasicNameValuePair("field_value", Date));
			        new Thread(new NetThread.putDataThread(handler, url, params, 0)).start();
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
				startActivityForResult(new Intent(AccountActivity.this,NameActivity.class), 1);
				break;
			case R.id.tv_sex:
				setSex();
				break;
			case R.id.tv_update_pwd:
				startActivity(new Intent(AccountActivity.this, UpdatePwdActivity.class));
				break;
			case R.id.tv_phone:
				Intent intent = new Intent(AccountActivity.this, RegisterActivity.class);
				intent.putExtra("mark", 3);
				startActivityForResult(intent, 1);
				break;
			case R.id.tv_email:
				Intent intent1 = new Intent(AccountActivity.this, RegisterActivity.class);
				intent1.putExtra("mark", 4);
				startActivityForResult(intent1, 1);
				break;
			case R.id.iv_pic:
				picPop();
				break;
			case R.id.tv_birth:
				OpenDateDialog.ShowDate(AccountActivity.this, R.id.tv_birth);
				break;
			}
		}
	};

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case get_customer:
				jsonCustomer(msg.obj.toString());
				break;

			case 0:
				System.out.println(msg.obj.toString());
				break;
			}
		}
	};

	private void GetCustomer() {
		String url = Constant.BaseUrl + "customer/" + Variable.cust_id
				+ "?auth_code=" + Variable.auth_code;
		new Thread(new NetThread.GetDataThread(handler, url, get_customer))
				.start();
	}
	String birth;
	private void jsonCustomer(String str) {
		try {
			JSONObject jsonObject = new JSONObject(str);
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
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	String[] Sexs = { "男", "女" };

	private void setSex() {
		new AlertDialog.Builder(AccountActivity.this).setTitle("请选择性别").setItems(Sexs,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String url = Constant.BaseUrl + "customer/" + Variable.cust_id + "/field?auth_code=" + Variable.auth_code;
						List<NameValuePair> params = new ArrayList<NameValuePair>();
				        params.add(new BasicNameValuePair("field_name", "sex"));
				        params.add(new BasicNameValuePair("field_type", "Number"));
				        params.add(new BasicNameValuePair("field_value", String.valueOf(which)));
				        new Thread(new NetThread.putDataThread(handler, url, params, 0)).start();
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
	
	private void picPop(){
		List<String> items = new ArrayList<String>();
		items.add("拍照");
		items.add("从手机相册中选取");
		PopView popView = new PopView(this);
		popView.initView(findViewById(R.id.iv_pic));
		popView.setData(items);
		popView.SetOnItemClickListener(new OnItemClickListener() {			
			@Override
			public void OnItemClick(int index) {
				switch (index) {
				case 0:
					Intent intent1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					startActivityForResult(intent1, 1);
					break;

				case 1:
					Intent intent = new Intent(); 
	                /* 开启Pictures画面Type设定为image */ 
	                intent.setType("image/*"); 
	                /* 使用Intent.ACTION_GET_CONTENT这个Action */ 
	                intent.setAction(Intent.ACTION_GET_CONTENT);  
	                /* 取得相片后返回本画面 */ 
	                startActivityForResult(intent, 9); 
					break;
				}
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		System.out.println("requestCode = " + requestCode + ",resultCode = " + resultCode);
		if(requestCode == 9){
			Uri uri = data.getData(); 
            Log.e("uri", uri.toString()); 
            ContentResolver cr = this.getContentResolver(); 
            try { 
                Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri)); 
                UpdateBitmap(bitmap);
            } catch (FileNotFoundException e) { 
                Log.e("Exception", e.getMessage(),e); 
            }
			return;
		}
		switch (resultCode) {
		case 1:
			tv_name.setText(data.getStringExtra("name"));
			break;

		case 2:
			boolean isPhone = data.getBooleanExtra("isPhone", true);
			String account = data.getStringExtra("account");
			if(isPhone){
				tv_phone.setText(account);
			}else{
				tv_email.setText(account);
			}
			break;
		case Activity.RESULT_OK:
			String sdStatus = Environment.getExternalStorageState();
            if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用
                Log.v("TestFile","SD card is not avaiable/writeable right now.");
                return;
            }
            Bundle bundle = data.getExtras();
            Bitmap bitmap = (Bitmap) bundle.get("data");// 获取相机返回的数据，并转换为Bitmap图片格式
            UpdateBitmap(bitmap);
			break;
		}
	}
	private void UpdateBitmap(Bitmap bitmap){
		bitmap = BlurImage.getSquareBitmap(bitmap);
        FileOutputStream b = null;
        File file = new File("/sdcard/myImage/");
        file.mkdirs();// 创建文件夹
        String fileName = "/sdcard/myImage/111.jpg";
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
        String url = Constant.BaseUrl + "upload_image?auth_code=" + Variable.auth_code;
        UploadUtil.getInstance().setOnUploadProcessListener(AccountActivity.this);
        UploadUtil.getInstance().uploadFile(fileName, "image", url, new HashMap<String, String>());
	}
	private void jsonUpdatePic(String str){
		try {
			JSONObject jsonObject = new JSONObject(str);
			if(jsonObject.getString("status_code").equals("0")){
				String ImageUrl = jsonObject.getString("image_file_url");
				String url = Constant.BaseUrl + "customer/" + Variable.cust_id + "/field?auth_code=" + Variable.auth_code;
				List<NameValuePair> params = new ArrayList<NameValuePair>();
		        params.add(new BasicNameValuePair("field_name", "logo"));
		        params.add(new BasicNameValuePair("field_type", "String"));
		        params.add(new BasicNameValuePair("field_value", ImageUrl));
		        new Thread(new NetThread.putDataThread(handler, url, params, 0)).start();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onUploadDone(int responseCode, String message) {
		System.out.println("message = " + message);
		jsonUpdatePic(message);
	}
	@Override
	public void onUploadProcess(int uploadSize) {}
	@Override
	public void initUpload(int fileSize) {}
}