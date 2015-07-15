package com.wise.baba.ui.fragment;

import java.util.Timer;
import java.util.TimerTask;


import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Const;
import com.wise.baba.app.Msg;
import com.wise.baba.biz.HttpGetObdData;
import com.wise.baba.ui.adapter.OnCardMenuListener;
import com.wise.baba.ui.widget.DialView;

/**
 * 
 * @author c 首页速度信息
 * 
 */
public class FragmentHomeSpeed extends Fragment implements Callback,
		OnClickListener {

	private Handler handler;
	private AppApplication app;
	private HttpGetObdData http;
	private DialView dialSpeed;
	private int index = 0;
	private Timer timer;
	
	private Boolean isStart = false;
	
	/**
	 * 0，速度 1，转速 2，电源电压 3，水温 4 ，负荷 5，节气门 ，6，剩余油量
	 */
	private View view;
	//值文字显示
	private int textId[] = { R.id.textSpeed, R.id.textRotary, R.id.textVoltage,
			R.id.textTemperature, R.id.textLoad, R.id.textThrottle,
			R.id.textOil };
	//各图片
	private int icon[] = { R.drawable.ico_speed,
			R.drawable.ico_home_speed__rotary,
			R.drawable.ico_home_speed_voltage,
			R.drawable.ico_home_speed__temperature,
			R.drawable.ico_home_speed__load,
			R.drawable.ico_home_speed_throttle, R.drawable.ico_home_speed_oil };
	//各项布局
	private int llytId[] = { R.id.llytSpeed, R.id.llytRotary, R.id.llytVoltage,
			R.id.llytTemperature, R.id.llytLoad, R.id.llytThrottle,
			R.id.llytOil };
	
	//各项标题
	private String[] title = {"速度","转速","电源电压","水温","负荷","节气门","剩余油量"};
	//各项单位
	private String[] unit = {"km","rpm","v","℃","%","%", "L"};
	
	private String value[] = new String[7];
	private int maxValue[] = {120,1000,15,112,100,100,70};
	private TextView tvCardTitle, textScore, textUnit;
	private ImageView ivCardIcon;
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_home_speed, container, false);
		dialSpeed = (DialView) view.findViewById(R.id.dialSpeed);
		
		view.findViewById(R.id.iv_speed_menu).setOnClickListener(this);
		for (int i = 0; i < 7; i++) {
			view.findViewById(llytId[i]).setOnClickListener(this);
		}
		tvCardTitle = (TextView) view.findViewById(R.id.tv_card_title);
		textScore = (TextView) view.findViewById(R.id.tv_score);
		textUnit = (TextView) view.findViewById(R.id.tv_unit);
		ivCardIcon = (ImageView) view.findViewById(R.id.iv_card_icon);
		
		return view;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		app = (AppApplication) this.getActivity().getApplication();
		handler = new Handler(this);
		
	}

	@Override
	public void onResume() {
		super.onResume();
		http = new HttpGetObdData(FragmentHomeSpeed.this.getActivity(), handler);
		timer = new Timer();
		timer.schedule(new TimerTask(){
			@Override
			public void run() {
				http.request();
				Log.i("FragmentHomeSpeed", "timer");
			}
		}, 1, 3000);
	}

	@Override
	public void onStop() {
		super.onStop();
		if(timer!=null){
			timer.cancel();
			timer.purge();
			timer = null;
		}
	}

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.Handler.Callback#handleMessage(android.os.Message)
	 */
	@Override
	public boolean handleMessage(Message msg) {
		
		if (msg.what == Msg.Get_OBD_Data) {
			
			
			
			Log.i("FragmentHomeSpeed", "Get_OBD_Data");
			Bundle bundle = msg.getData();
			
			isStart = bundle.getBoolean("isStart");
			
			
			
			if(isStart){
				
				value[0] = bundle.getString("ss");
				value[1] = bundle.getString("fdjzs");
				value[2] = bundle.getString("dpdy");
				value[3] = bundle.getString("sw");
				value[4] = bundle.getString("fdjfz");
				value[5] = bundle.getString("jqmkd");
				value[6] = bundle.getString("syyl");
				
			}else{
				value = new String[]{"--","--",bundle.getString("dpdy"),"--","--","--","--"};
			}
			

			for (int i = 0; i < 7; i++) {
				((TextView) view.findViewById(textId[i]))
						.setText(value[i]);
			}
			textScore.setText(toNumber(value[index]));
			dialSpeed.initValue(caclPercent(index), handler);
		} else if (msg.what == Msg.Dial_Refresh_Value) {
//			/Log.i("FragmentHomeSpeed", "Dial_Refresh_Value");
			//int value = msg.arg1;
			
			// dialSpeed.startCheckAnimation(value, handler);
		}

		return false;
	}
	
	public String toNumber(String value){
		if(value.equals("--")){
			return "0";
		}
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {

		int id = v.getId();
		
		if(id == R.id.iv_speed_menu){
			if (onCardMenuListener != null) {
				onCardMenuListener.showCarMenu(Const.TAG_SPEED);
			}
			return;
		}
		
		if(isStart == false && id != llytId[2]){
			Toast toast = Toast.makeText(FragmentHomeSpeed.this.getActivity(), "车辆未启动", Toast.LENGTH_SHORT);
			toast.setDuration(50);
			toast.show();
			return;
		}
		
		
		for(int i =0;i<llytId.length;i++){
			if(id == llytId[i]){
				
				
				
				this.index = i;
				//圆环刻度设置
				dialSpeed.initValue(caclPercent(i), handler);
				//切换标题
				tvCardTitle.setText(title[i]);
				//切换图片
				ivCardIcon.setImageResource(icon[i]);
				
				//中间单位设置
				textUnit.setText(unit[i]);
				
				//中间分值设置
				textScore.setText(toNumber(value[i]));
			
				break;
			}
		}
		
	}
	
	OnCardMenuListener onCardMenuListener;

	public void setOnCardMenuListener(OnCardMenuListener onCardMenuListener) {
		this.onCardMenuListener = onCardMenuListener;

	}
	
	/**
	 * 计算当前数值 相对于 最大值 的百分比 
	 * @param index
	 * @return
	 */
	public int caclPercent(int index){
		
		//把值转变为数值
		String strValue = value[index];
		float v = 0;
		if(strValue.equals("--")){
			v = 0;
		}else{
			v = Float.parseFloat(strValue);
		}
		
		int percent = 100;
		//数值过大，比值设为100
		float mv = maxValue[index];
		Log.i("FragmentHomeSpeed", "value "+value[index]);
		Log.i("FragmentHomeSpeed", "maxValue "+maxValue[index]);
		if(v>=mv){
			percent = 100;
		}else{
			percent = (int) (v/mv * 100f);
		}
		Log.i("FragmentHomeSpeed", "percent "+percent);
		return percent;
	}
}
