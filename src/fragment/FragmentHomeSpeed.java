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
		http.request();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.Handler.Callback#handleMessage(android.os.Message)
	 */
	@Override
	public boolean handleMessage(Message msg) {
		
		Bundle bundle = msg.getData();
		
		String ss = bundle.getInt("ss")+"";
		textSpeed.setText(ss);
		
		String fdjfz = bundle.getInt("fdjfz")+"";
		textRotary.setText(fdjfz);
		
		
		String dpdy = bundle.getInt("dpdy")+"";
		textVoltage.setText(dpdy);
		
		String sw = bundle.getInt("sw")+"";
		textTemperature.setText(sw);
		
		String syyl = bundle.getInt("syyl")+"";
		textOil.setText(syyl);
		
	
		return false;
	}

}
