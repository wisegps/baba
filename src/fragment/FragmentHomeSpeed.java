package fragment;

import pubclas.Constant;
import pubclas.NetThread;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Const;
import com.wise.baba.biz.HttpGetData;

/**
 * 
 * @author c 首页速度信息
 * 
 */
public class FragmentHomeSpeed extends Fragment implements Callback {

	private Handler handler;
	private AppApplication app;
	private HttpGetData http;
	/**
	 * 1=电源，2=进气，节气门，3=怠速，4=冷却，水温，5=排放，
	 */
	int type = Const.TYPE_DY;
	
	private TextView textSpeed,textRotary,textVoltage,textTemperature,textLoad,textThrottle,textOil;
	private View view;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_home_speed, container, false);
		textSpeed = (TextView) view.findViewById(R.id.textSpeed);
		textRotary = (TextView) view.findViewById(R.id.textRotary);
		textVoltage = (TextView) view.findViewById(R.id.textVoltage);
		textTemperature = (TextView) view.findViewById(R.id.textTemperature);
		textLoad = (TextView) view.findViewById(R.id.textLoad);
		textThrottle = (TextView) view.findViewById(R.id.textThrottle);
		textOil = (TextView) view.findViewById(R.id.textOil);
		return view;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		app = (AppApplication) this.getActivity().getApplication();
		handler = new Handler(this);
		http = new HttpGetData(this.getActivity(), handler);
		http.request(type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.Handler.Callback#handleMessage(android.os.Message)
	 */
	@Override
	public boolean handleMessage(Message msg) {
		String result = (String) msg.obj;
		Log.i("FragmentHomeSpeed" , msg.what +" : "+result);
		switch (msg.what) {
		case Const.TYPE_DY://电源电压
			textVoltage.setText(result);
			http.request(Const.TYPE_SW);
			break;
		case Const.TYPE_SW://水温
			textTemperature.setText(result);
			http.request(Const.TYPE_DS);
			break;
		case Const.TYPE_DS://怠速，转速
			textRotary.setText(result);
			http.request(Const.TYPE_JQM);
			break;
		case Const.TYPE_JQM://节气门
			textThrottle.setText(result);
			//http.request(Const.TYPE_DS);
			break;
		case Const.TYPE_PF:
			textLoad.setText(result);
			break;
		}
		return false;
	}

}
