package com.wise.state;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import pubclas.Blur;
import pubclas.Constant;
import com.aliyun.android.oss.OSSClient;
import com.aliyun.android.oss.task.Task;
import com.wise.baba.R;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

public class TestActivity extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
//		Button bt_phone = (Button) findViewById(R.id.bt_phone);
//		bt_phone.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Intent intent1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//				startActivityForResult(intent1, 1);
//			}
//		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		String sdStatus = Environment.getExternalStorageState();
		if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用
			Log.v("TestFile", "SD card is not avaiable/writeable right now.");
			return;
		}
		Bundle bundle = data.getExtras();
		Bitmap bitmap = (Bitmap) bundle.get("data");// 获取相机返回的数据，并转换为Bitmap图片格式
		UpdateBitmap(bitmap);
	}

	private void UpdateBitmap(Bitmap bitmap) {
		File filePath = new File(Constant.userIconPath);
		if (!filePath.exists()) {
			filePath.mkdirs();
		}
		bitmap = Blur.scaleImage(bitmap, 150);
		bitmap = Blur.getSquareBitmap(bitmap);
		FileOutputStream b = null;
		String fileName = Constant.userIconPath + "100.png";
		try {
			b = new FileOutputStream(fileName);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 80, b);// 把数据写入文件
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
		//String url = "http://baba-img.oss-cn-hangzhou.aliyuncs.com";
		String url = "baba-img";
//		OssUploadUtil.getInstance().setOnUploadProcessListener(
//				TestActivity.this);
//		OssUploadUtil.getInstance().uploadFile(fileName, "image", url,
//				new HashMap<String, String>());
//		initOss();
		new myThread(url, fileName).start();
		
	}
	
	class myThread extends Thread{
		String url;
		String fileName;
		
		public myThread(String url , String fileName){
			this.url = url;
			this.fileName = fileName;
		}
		@Override
		public void run() {
			super.run();
			OSSClient client = new OSSClient();
			Task.OSS_END_POINT = "http://baba-img.oss-cn-hangzhou.aliyuncs.com";
			Task.OSS_HOST = "baba-img.oss-cn-hangzhou.aliyuncs.com";
			client.setAccessId("eJ3GLV07j9DD4LY5");
			client.setAccessKey("iAxAHoAuG1ZYwjIRLfyYKK2oF9WcCe");
			client.uploadObject(url, "photo", fileName);
		}
	}
	
	private void initOss(){
		
		
	}

}