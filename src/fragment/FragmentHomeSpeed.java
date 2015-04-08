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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Const;
import com.wise.baba.app.Msg;
import com.wise.baba.biz.HttpGetObdData;
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
	/**
	 * 0，速度 1，转速 2，电源电压 3，水温 4 ，负荷 5，节气门  ，6，剩余油量
	 */
	private View view;
	private int textId[] = { R.id.textSpeed, R.id.textRotary, R.id.textVoltage,
			R.id.textTemperature, R.id.textLoad, R.id.textThrottle,
			R.id.textOil };
	private int llytId[] = { R.id.llytSpeed, R.id.llytRotary, R.id.llytVoltage,
			R.id.llytTemperature, R.id.llytLoad, R.id.llytThrottle,
			R.id.llytOil };
	private int value[] = new int[7];
	private TextView textScore;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_home_speed, container, false);
		dialSpeed = (DialView) view.findViewById(R.id.dialSpeed);

		for (int i = 0; i < 7; i++) {
			view.findViewById(llytId[i]).setOnClickListener(this);
		}

		textScore = (TextView) view.findViewById(R.id.tv_score);
		return view;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		app = (AppApplication) this.getActivity().getApplication();
		handler = new Handler(this);
		http = new HttpGetObdData(this.getActivity(), handler);
		http.request();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.Handler.Callback#handleMessage(android.os.Message)
	 */
	@Override
	public boolean handleMessage(Message msg) {
		Log.i("FragmentHomeSpeed", "handleMessage");
		if (msg.what == Msg.Get_OBD_Data) {
			Bundle bundle = msg.getData();
			value[0] = bundle.getInt("ss");
			value[1] = bundle.getInt("fdjfz");
			value[2] = bundle.getInt("dpdy");
			value[3] = bundle.getInt("sw");
			value[4] = 0;
			value[5] = bundle.getInt("jqmkd");
			value[6] = bundle.getInt("syyl");

			for (int i = 0; i < 7; i++) {
				((TextView) view.findViewById(textId[i]))
						.setText(value[i] + "");
			}
			textScore.setText(value[0] + "");
			dialSpeed.initValue(value[0]);
		}else if(msg.what == Msg.Dial_Refresh_Value){
			int value = msg.arg1;
			textScore.setText(value + "");
			//dialSpeed.startCheckAnimation(value, handler);
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {

		int id = v.getId();
		for (int i = 0; i < 7; i++) {
			if (id == llytId[i]) {
				Log.i("FragmentHomeSpeed", "clicke" + i);
				dialSpeed.startAnimation(value[i], handler);
				break;
			}
		}
	}

}
