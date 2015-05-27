package com.wise.remind;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.json.JSONObject;
import com.umeng.analytics.MobclickAgent;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Constant;
import com.wise.baba.biz.GetSystem;
import com.wise.baba.entity.CarData;
import com.wise.baba.entity.RemindData;
import com.wise.baba.net.NetThread;
import com.wise.car.CarActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 提醒明细
 * 
 * @author honesty
 */
public class RemindActivity extends Activity {
	private static final int delete = 1;
	private static final int get_data = 2;
	int resultCode = 0;
	RemindData remindData;
	TextView tv_content;
	RelativeLayout rl_Note, rl_body;
	LinearLayout ll_content;
	AppApplication app;
	List<CarData> carDatas ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_remind);
		app = (AppApplication) getApplication();
		ll_content = (LinearLayout) findViewById(R.id.ll_content);
		rl_Note = (RelativeLayout) findViewById(R.id.rl_Note);
		rl_body = (RelativeLayout) findViewById(R.id.rl_body);
		ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		Button bt_edit = (Button) findViewById(R.id.bt_edit);
		bt_edit.setOnClickListener(onClickListener);
		Button bt_delete = (Button) findViewById(R.id.bt_delete);
		bt_delete.setOnClickListener(onClickListener);
		Button bt_share = (Button) findViewById(R.id.bt_share);
		bt_share.setOnClickListener(onClickListener);
		Button bt_place = (Button) findViewById(R.id.bt_place);
		bt_place.setOnClickListener(onClickListener);

		Intent intent = getIntent();
		boolean isNeedGetData = intent.getBooleanExtra("isNeedGetData", true);
		carDatas = (List<CarData>) intent.getSerializableExtra("carDatas");
		if(carDatas == null){
			carDatas = app.carDatas;
		}
		if (isNeedGetData) {
			// 从服务器读取数据
			int reminder_id = intent.getIntExtra("reminder_id", 0);
			String url = Constant.BaseUrl + "reminder/" + reminder_id
					+ "?auth_code=" + app.auth_code;
			new NetThread.GetDataThread(handler, url, get_data).start();
		} else {
			// 接收传过来的数据
			remindData = (RemindData) intent.getSerializableExtra("remindData");
			rl_body.setVisibility(View.VISIBLE);
			rl_Note.setVisibility(View.GONE);
			setView();
		}
	}

	private void setView() {
		try {
			TextView tv_name = (TextView) findViewById(R.id.tv_name);
			TextView tv_date = (TextView) findViewById(R.id.tv_date);
			// TODO 有null指针异常
			String title = "距离" + getCarName(remindData.getObj_id())
					+ Constant.items_note_type[remindData.getRemind_type()];
			tv_name.setText(title);

			int count_time = GetSystem.isTimeOut(remindData.getRemind_time());
			if (count_time > 0) {
				tv_date.setText(GetSystem.jsTime(count_time));
			} else {
				tv_date.setText("0");
			}
			tv_content = (TextView) findViewById(R.id.tv_content);
			tv_content.setText(remindData.getContent());
			setUrl();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** 得到车辆对应的位置 **/
	private String getCarName(int Obj_id) {
		for (int i = 0; i < carDatas.size(); i++) {
			if (carDatas.get(i).getObj_id() == Obj_id) {
				return carDatas.get(i).getNick_name();
			}
		}
		return "";
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				setResult(resultCode);
				finish();
				break;
			case R.id.bt_edit:
				if (app.isTest) {
					Toast.makeText(RemindActivity.this, "演示账号不支持该功能",
							Toast.LENGTH_SHORT).show();
					return;
				}
				Log.i("RemindActivity", "编辑");
				Intent intent = new Intent(RemindActivity.this,
						CarRemindUpdateActivity.class);
				intent.putExtra("remindData", remindData);
				intent.putExtra("carDatas", (Serializable)carDatas);
				startActivityForResult(intent, 2);
				break;
			case R.id.bt_delete:
				if (app.isTest) {
					Toast.makeText(RemindActivity.this, "演示账号不支持该功能",
							Toast.LENGTH_SHORT).show();
					return;
				}
				new AlertDialog.Builder(RemindActivity.this)
						.setTitle("提示")
						.setMessage("你确定删除这条提醒吗？")
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										Delete();
									}
								}).setNegativeButton("取消", null).show();
				break;
			case R.id.bt_place:
				ToDealAdress();
				break;
			case R.id.bt_share:
				Bitmap bitmap = GetSystem.getViewBitmap(ll_content);
				savePic(bitmap, "/mnt/sdcard/Picture.png");
				GetSystem.share(getApplicationContext(), "",
						"/mnt/sdcard/Picture.png", 0, 0, "提醒", "");
				break;
			}
		}
	};

	private static void savePic(Bitmap b, String strFileName) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(strFileName);
			if (null != fos) {
				b.compress(Bitmap.CompressFormat.JPEG, 60, fos);
				fos.flush();
				fos.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case delete:
				setResult(2);
				finish();
				break;

			case get_data:
				// TODO 返回""说明该车务提醒以被删除，做处理
				if (msg.obj.toString().equals("")) {
					rl_body.setVisibility(View.GONE);
					rl_Note.setVisibility(View.VISIBLE);
				} else {
					rl_body.setVisibility(View.VISIBLE);
					rl_Note.setVisibility(View.GONE);
					jsonRemindData(msg.obj.toString());
					setView();
				}
				break;
			}
		}
	};

	/** 解析提醒数据 **/
	private void jsonRemindData(String str) {
		try {
			JSONObject jsonObject = new JSONObject(str);
			String remind_time = jsonObject.getString("remind_time").substring(
					0, 10);
			remindData = new RemindData();
			remindData.setCreate_time(jsonObject.getString("create_time"));
			remindData.setRemind_time(remind_time);
			remindData.setContent(jsonObject.getString("content"));
			remindData.setRepeat_type(jsonObject.getInt("repeat_type"));
			remindData.setRemind_way(jsonObject.getInt("remind_way"));

			if (jsonObject.opt("mileage") == null) {
				remindData.setMileages(0);
			} else {
				int mileage = jsonObject.getInt("mileage");
				remindData.setMileages(mileage);
			}
			if (jsonObject.opt("cur_mileage") == null) {
				remindData.setCur_mileage(0);
			} else {
				int cur_mileage = jsonObject.getInt("cur_mileage");
				remindData.setCur_mileage(cur_mileage);
			}

			remindData.setObj_id(jsonObject.getInt("obj_id"));
			remindData.setRemind_type(jsonObject.getInt("remind_type"));
			remindData.setReminder_id(jsonObject.getString("reminder_id"));
			remindData.setUrl(jsonObject.getString("url"));
			int count_time = GetSystem.isTimeOut(remind_time);
			if (count_time > 0) {
				remindData.setCount_time(GetSystem.ProcessTime(count_time));
			} else {
				remindData.setCount_time("");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void ToDealAdress() {
		Intent intent = new Intent(RemindActivity.this,
				DealAddressActivity.class);
		if (remindData.getRemind_type() == 1) {
			intent.putExtra("Title", getString(R.string.inspection_title));
			intent.putExtra("Type", 1);
		} else if (remindData.getRemind_type() == 0) {
			intent.putExtra("Title", "驾照换证");
			intent.putExtra("Type", 2);
		} else if (remindData.getRemind_type() == 5) {
			return;
		}
		startActivity(intent);
	}

	private void Delete() {
		String url = Constant.BaseUrl + "reminder/"
				+ remindData.getReminder_id() + "?auth_code=" + app.auth_code;
		new Thread(new NetThread.DeleteThread(handler, url, delete)).start();
	}

	private void setUrl() {
		WebView shareView = (WebView) findViewById(R.id.share_web);
		shareView.requestFocus(); // 设置可获取焦点
		shareView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY); // 取消滚动条
		switch (remindData.getRemind_type()) {
		case 0:
			tv_content.setVisibility(View.GONE);
			shareView.loadUrl(remindData.getUrl());
			break;
		case 1:
			tv_content.setVisibility(View.GONE);
			shareView.loadUrl(remindData.getUrl());
			break;
		case 2:
			tv_content.setVisibility(View.GONE);
			shareView.loadUrl(remindData.getUrl());
			break;
		case 3:
			tv_content.setVisibility(View.GONE);
			shareView.loadUrl(remindData.getUrl());
			break;
		case 4:
			tv_content.setVisibility(View.GONE);
			shareView.loadUrl(remindData.getUrl());
			break;
		case 5:
			tv_content.setVisibility(View.VISIBLE);
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == 2) {
			resultCode = 2;
			setResult(resultCode);
			finish();
		}
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
