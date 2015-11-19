package com.wise.baba.biz;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import com.wise.baba.AppApplication;
import com.wise.baba.app.Constant;
import com.wise.baba.app.Msg;
import com.wise.baba.entity.AQIEntity;
import com.wise.baba.entity.Air;
import com.wise.baba.entity.CarData;
import com.wise.baba.net.NetThread;
import com.wise.car.CarActivity;
import com.wise.car.CarUpdateActivity;
import com.wise.car.DevicesAddActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
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

public class CarManage {

	private Context context;
	private AppApplication app;

	private int unbindIndex;
	private int deleteIndex;
	private int updateIndex;
	
	private Handler uiHandler;
	private Handler workHandler;
	private HandlerThread handlerThread = null;

	public CarManage(Context context, AppApplication app, Handler uiHandler) {
		super();
		this.context = context;
		this.app = app;
		this.uiHandler = uiHandler;
		
		handlerThread = new HandlerThread("HttpAir");
		handlerThread.start();

		Looper looper = handlerThread.getLooper();
		workHandler = new Handler(looper, handleCallBack);
	}
	
	/**
	 * 工作子线程回调函数：
	 * 主线程把网络请求数据发送到该工作子线程，子线程解析完毕，发送通知到ui主线程跟新界面
	 */
	public Handler.Callback handleCallBack = new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case Msg.Delete_Car:
				jsonDelete(msg.obj.toString());
				break;
			case Msg.Unbind:
				clearData();
				break;
			case Msg.Unbind_Clear_Data:
				jsonClearData(msg.obj.toString(), msg.arg1);
				break;
			}
			return false;
		}

	};


	/**
	 * 解除绑定
	 */
	public void unbind(int index) {
		Log.i("CarManage", "解除绑定" + index);
		this.unbindIndex = index;
		CarData carData = app.carDatas.get(unbindIndex);
		Log.i("CarManage", "解除绑定" + carData.getDevice_id());
		String url_sim = Constant.BaseUrl + "device/" + carData.getDevice_id()
				+ "/customer?auth_code=" + app.auth_code;
		List<NameValuePair> paramSim = new ArrayList<NameValuePair>();
		paramSim.add(new BasicNameValuePair("cust_id", "0"));
		Log.i("CarManage", "解除绑定1");
		new NetThread.putDataThread(workHandler, url_sim, paramSim, Msg.Unbind)
				.start();
		Log.i("CarManage", "解除绑定2");
	}

	/**
	 * 清除终端的所有数据
	 */
	public void clearData() {
		Log.i("CarManage", "清除终端的所有数据");
		CarData carData = app.carDatas.get(unbindIndex);
		final String url = Constant.BaseUrl + "vehicle/" + carData.getObj_id()
				+ "/device?auth_code=" + app.auth_code;
		final List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("device_id", "0"));
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("提示")
				.setMessage("是否在解除绑定的同时清除终端的所有数据？")
				.setPositiveButton("是", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						params.add(new BasicNameValuePair("deal_data", "1"));
						new Thread(
								new NetThread.putDataThread(workHandler, url,
										params, Msg.Unbind_Clear_Data,
										unbindIndex)).start();
					}
				})
				.setNegativeButton("否", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						new Thread(
								new NetThread.putDataThread(workHandler, url,
										params, Msg.Unbind_Clear_Data,
										unbindIndex)).start();
					}
				}).show();

	}

	/**
	 * 修改终端
	 */
	public void updateDevice(int index) {
		this.updateIndex = index;
		CarData carData = app.carDatas.get(index);
		Intent intent = new Intent(context, DevicesAddActivity.class);
		intent.putExtra("car_id", carData.getObj_id());
		intent.putExtra("isBind", false);
		intent.putExtra("car_series_id", carData.getCar_series_id());
		intent.putExtra("car_series", carData.getCar_series());
		// 传以前终端的值
		intent.putExtra("old_device_id", carData.getDevice_id());
		((Activity) context).startActivityForResult(intent, CarUpdateActivity.Request_Device_Update);
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

				new Thread(new NetThread.DeleteThread(workHandler, url,
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

	private void jsonClearData(String str, int index) {

		Log.i("CarManage", "jsonClearData");
		try {
			JSONObject jsonObject = new JSONObject(str);
			if (jsonObject.getString("status_code").equals("0")) {
				// 刷新
				Toast.makeText(context, "解除绑定成功", Toast.LENGTH_SHORT).show();
				app.carDatas.get(index).setDevice_id("");
				// 发广播
				Intent intent = new Intent(Constant.A_RefreshHomeCar);
				context.sendBroadcast(intent);
				((Activity) context).finish();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
