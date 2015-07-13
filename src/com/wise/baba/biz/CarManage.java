package com.wise.baba.biz;

import org.json.JSONObject;

import com.wise.baba.AppApplication;
import com.wise.baba.app.Constant;
import com.wise.baba.app.Msg;
import com.wise.baba.entity.CarData;
import com.wise.baba.net.NetThread;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

/**
 * 
 * 我的爱车
 * 
 * @author c
 * 
 */

public class CarManage implements Callback {

	private Context context;
	private AppApplication app;
	private Handler thisHandler;
	private Handler parentHandler;

	private int deleteIndex;

	public CarManage(Context context, AppApplication app, Handler handler) {
		super();
		this.context = context;
		this.app = app;
		this.thisHandler = new Handler(this);
		this.parentHandler = handler;
	}

	/**
	 * 解除绑定
	 */
	public void unbind() {

	}

	/**
	 * 修改终端
	 */
	public void updateDevice() {

	}

	/**
	 * 删除
	 */
	public void delete(int index) {
		Log.i("CarManage", "删除");
		this.deleteIndex = index;
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("提示");
		builder.setMessage("确定删除该车辆？");
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String url = Constant.BaseUrl + "vehicle/"
						+ app.carDatas.get(deleteIndex).getObj_id()
						+ "?auth_code=" + app.auth_code;

				new Thread(new NetThread.DeleteThread(thisHandler, url,
						Msg.Delete_Car)).start();
			}
		}).setNegativeButton("否", null);
		builder.setNegativeButton("取消", null);
		builder.show();
	}

	/**
	 * 解析删除返回信息
	 * 
	 * @param str
	 */
	private void jsonDelete(String str) {
		try {
			JSONObject jsonObject = new JSONObject(str);
			if (jsonObject.getString("status_code").equals("0")) {
				Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show();
			}
			app.carDatas.remove(deleteIndex);
			// 发广播
			Intent intent = new Intent(Constant.A_RefreshHomeCar);
			context.sendBroadcast(intent);
			((Activity) context).finish();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean handleMessage(Message msg) {

		switch (msg.what) {
		case Msg.Delete_Car:
			jsonDelete(msg.obj.toString());
			break;
		}
		return false;
	}

}
