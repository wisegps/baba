package com.wise.car;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import com.aliyun.android.oss.task.PutObjectTask;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Constant;
import com.wise.baba.net.NetThread;
import com.wise.baba.ui.widget.PopView;
import com.wise.baba.ui.widget.PopView.OnItemClickListener;
import com.wise.baba.util.Blur;
import com.wise.baba.util.Uri2Path;


public class PictureChoose extends Activity {
	AppApplication app;
	ImageView pic_near, pic_far, pic_near_add, pic_far_add;
	int type = 0;
	String car_series_id = "";
	public static final int PIC_NEAR = 1;
	public static final int PIC_FAR = 2;
	public static final int PIC_ALL = 3;

	public static final int Pictrue = 5;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_picture_choose);
		app = (AppApplication) getApplication();
		car_series_id = getIntent().getStringExtra("car_series_id");

		pic_near = (ImageView) findViewById(R.id.pic_near);
		pic_near.setOnClickListener(onClickListener);
		pic_far = (ImageView) findViewById(R.id.pic_far);
		pic_far.setOnClickListener(onClickListener);
		pic_near_add = (ImageView) findViewById(R.id.pic_near_add);
		pic_near_add.setOnClickListener(onClickListener);
		pic_far_add = (ImageView) findViewById(R.id.pic_far_add);
		pic_far_add.setOnClickListener(onClickListener);
		findViewById(R.id.iv_back).setOnClickListener(onClickListener);
		findViewById(R.id.iv_add).setOnClickListener(onClickListener);
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			case R.id.pic_near_add:
				picPop(PIC_NEAR, R.id.pic_near);
				break;
			case R.id.pic_far_add:
				picPop(PIC_FAR, R.id.pic_far);
				break;
			case R.id.pic_near:
				picPop(PIC_NEAR, R.id.pic_near);
				break;
			case R.id.pic_far:
				picPop(PIC_FAR, R.id.pic_far);
				break;
			case R.id.iv_add:
				String naerUrl = Constant.BaseUrl + "base/car_series/"
						+ car_series_id + "/near_pic?auth_code="
						+ app.auth_code;

				String farUrl = Constant.BaseUrl + "base/car_series/"
						+ car_series_id + "/far_pic?auth_code=" + app.auth_code;
				if (near_small != null && !near_small.equals("")
						&& far_small != null && !far_small.equals("")) {
					type = PIC_ALL;
					upLoadPic(naerUrl, near_small, near_big);
					upLoadPic(farUrl, far_small, far_big);
				} else if (near_small != null && !near_small.equals("")) {
					type = PIC_NEAR;
					upLoadPic(naerUrl, near_small, near_big);
				} else if (far_small != null && !far_small.equals("")) {
					type = PIC_FAR;
					upLoadPic(farUrl, far_small, far_big);
				}
				break;
			}
		}

	};

	private static final int request = 6;

	/**
	 * 图片上传
	 * 
	 * @param url
	 */
	private void upLoadPic(String url, final String small, final String big) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// 上传图片到阿里云
				PutObjectTask smallTask = new PutObjectTask(Constant.oss_path,
						small, "image/jpg", Constant.VehiclePath + small,
						Constant.oss_accessId, Constant.oss_accessKey);
				smallTask.getResult();

				PutObjectTask bigTask = new PutObjectTask(Constant.oss_path,
						big, "image/jpg", Constant.VehiclePath + big,
						Constant.oss_accessId, Constant.oss_accessKey);
				bigTask.getResult();
			}
		}).start();

		List<NameValuePair> params2 = new ArrayList<NameValuePair>();
		params2.add(new BasicNameValuePair("big_pic_url",
				(Constant.oss_url + big)));
		params2.add(new BasicNameValuePair("small_pic_url",
				(Constant.oss_url + small)));
		params2.add(new BasicNameValuePair("author", app.cust_name));
		new NetThread.postDataThread(handler, url, params2, request).start();
	}

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case request:
				Toast.makeText(PictureChoose.this, "图片分享成功", Toast.LENGTH_SHORT)
						.show();
				Intent data = new Intent();
				data.putExtra("type", type);
				if (near_small != null && !near_small.equals("")
						&& far_small != null && !far_small.equals("")) {
					data.putExtra("near_small", near_small);
					data.putExtra("far_small", far_small);
				} else if (near_small != null && !near_small.equals("")) {
					data.putExtra("near_small", near_small);
				} else if (far_small != null && !far_small.equals("")) {
					data.putExtra("far_small", far_small);
				}
				setResult(Pictrue, data);
				finish();
				break;
			}
		}

	};
	boolean flag = false;

	private void picPop(final int type, int id) {
		List<String> items = new ArrayList<String>();
		items.add("拍照");
		items.add("从手机相册中选取");
		final PopView popView = new PopView(this);
		popView.initView(findViewById(id));
		popView.setData(items);
		popView.SetOnItemClickListener(new OnItemClickListener() {
			@Override
			public void OnItemClick(int index) {
				switch (index) {
				case 0:
					flag = true;
					File file = new File(Constant.VehiclePath);
					if (!file.exists()) {
						file.mkdirs();// 创建文件夹
					}
					Intent intent1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					intent1.putExtra(
							MediaStore.EXTRA_OUTPUT,
							Uri.fromFile(new File(Constant.VehiclePath
									+ Constant.TemporaryImage)));
					startActivityForResult(intent1, type);
					popView.dismiss();
					break;
				case 1:
					Intent intent = new Intent();
					/* 开启Pictures画面Type设定为image */
					intent.setType("image/*");
					/* 使用Intent.ACTION_GET_CONTENT这个Action */
					intent.setAction(Intent.ACTION_GET_CONTENT);
					/* 取得相片后返回本画面 */
					startActivityForResult(intent, type);
					popView.dismiss();
					break;
				}
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == PIC_NEAR && resultCode == Activity.RESULT_OK) {
			if (flag) {
				saveImageSD((Constant.VehiclePath + Constant.TemporaryImage),
						pic_near, PIC_NEAR);
				flag = false;
			} else {
				if (data != null) {
					Uri uri = data.getData();
					saveImageSD(Uri2Path.getPath(PictureChoose.this, uri),
							pic_near, PIC_NEAR);
				}
			}
		} else if (requestCode == PIC_FAR && resultCode == Activity.RESULT_OK) {
			if (flag) {
				saveImageSD((Constant.VehiclePath + Constant.TemporaryImage),
						pic_far, PIC_FAR);
				flag = false;
			} else {
				if (data != null) {
					Uri uri = data.getData();
					saveImageSD(Uri2Path.getPath(PictureChoose.this, uri),
							pic_far, PIC_FAR);
				}
			}
		}
	}

	String small_pic = "";
	String big_pic = "";

	String near_small = "";
	String near_big = "";

	String far_small = "";
	String far_big = "";

	private void saveImageSD(String path, ImageView showView, int type) {
		if (path == null || path.equals("")) {
			Toast.makeText(PictureChoose.this, "请选择图片或者拍照上传",
					Toast.LENGTH_SHORT).show();
			return;
		}

		// 设置图像的名称和地址
		small_pic = app.cust_id + System.currentTimeMillis() + "small.png";
		big_pic = app.cust_id + System.currentTimeMillis() + "big.png";

		if (type == PIC_NEAR) {
			// 记录近景图片地址
			near_small = small_pic;
			near_big = big_pic;
			pic_near_add.setVisibility(View.GONE);
		} else if (type == PIC_FAR) {
			// 记录远景图片地址
			far_small = small_pic;
			far_big = big_pic;
			pic_far_add.setVisibility(View.GONE);
		}

		// 判断文件夹是否为空
		File filePath = new File(Constant.VehiclePath);
		if (!filePath.exists()) {
			filePath.mkdirs();
		}
		// 获取手机分辨率,选出最小的
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int widthPixels = metrics.widthPixels;
		int heightPixels = metrics.heightPixels;
		int newWidth = widthPixels > heightPixels ? heightPixels : widthPixels;

		Bitmap bitmap = Blur.decodeSampledBitmapFromPath(path, newWidth,
				newWidth);
		// 存大图像
		bitmap = Blur.scaleImage(bitmap, newWidth);
		FileOutputStream bigOutputStream = null;
		try {
			bigOutputStream = new FileOutputStream(Constant.VehiclePath
					+ big_pic);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bigOutputStream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				bigOutputStream.flush();
				bigOutputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// 存小图像
		bitmap = Blur.scaleImage(bitmap, newWidth / 3);
		FileOutputStream smallOutputStream = null;
		try {
			smallOutputStream = new FileOutputStream(Constant.VehiclePath
					+ small_pic);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 80, smallOutputStream);// 把数据写入文件
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				smallOutputStream.flush();
				smallOutputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// 图片显示
		showView.setVisibility(View.VISIBLE);
		showView.setImageBitmap(Blur.getSquareBitmap(bitmap));
	}
}
