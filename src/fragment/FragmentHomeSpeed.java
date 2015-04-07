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
import com.wise.baba.biz.HttpGetData;
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
	private HttpGetData http;
	private DialView dialSpeed;
	/**
	 * 1=电源，2=进气，节气门，3=怠速，4=冷却，水温，5=排放，
	 */

	// private TextView
	// textSpeed,textRotary,textVoltage,textTemperature,textLoad,textThrottle,textOil,textScore;
	private View view;
	private LinearLayout llytSpeed, llytRotary, llytVoltage, llytTemperature,
			llytLoad, llytThrottle, llytOil;

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

		// textSpeed = (TextView) view.findViewById(R.id.textSpeed);
		// textRotary = (TextView) view.findViewById(R.id.textRotary);
		// textVoltage = (TextView) view.findViewById(R.id.textVoltage);
		// textTemperature = (TextView) view.findViewById(R.id.textTemperature);
		// textLoad = (TextView) view.findViewById(R.id.textLoad);
		// textThrottle = (TextView) view.findViewById(R.id.textThrottle);
		// textOil = (TextView) view.findViewById(R.id.textOil);

		// llytSpeed = (LinearLayout) view.findViewById(R.id.llytSpeed);
		// llytRotary = (LinearLayout) view.findViewById(R.id.llytRotary);
		// llytVoltage = (LinearLayout) view.findViewById(R.id.llytVoltage);
		// llytTemperature = (LinearLayout)
		// view.findViewById(R.id.llytTemperature);
		// llytLoad = (LinearLayout) view.findViewById(R.id.llytLoad);
		// llytThrottle = (LinearLayout) view.findViewById(R.id.llytThrottle);
		// llytOil = (LinearLayout) view.findViewById(R.id.llytOil);
		//
		// llytSpeed.setOnClickListener(this);
		// llytRotary.setOnClickListener(this);
		// llytVoltage.setOnClickListener(this);
		// llytTemperature.setOnClickListener(this);
		// llytLoad.setOnClickListener(this);
		// llytThrottle.setOnClickListener(this);
		// llytOil.setOnClickListener(this);

		// textSpeed = (TextView) view.findViewById(R.id.textSpeed);
		// textRotary = (TextView) view.findViewById(R.id.textRotary);
		// textVoltage = (TextView) view.findViewById(R.id.textVoltage);
		// textTemperature = (TextView) view.findViewById(R.id.textTemperature);
		// textLoad = (TextView) view.findViewById(R.id.textLoad);
		// textThrottle = (TextView) view.findViewById(R.id.textThrottle);
		// textOil = (TextView) view.findViewById(R.id.textOil);

		textScore = (TextView) view.findViewById(R.id.tv_score);
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
		Log.i("FragmentHomeSpeed", "handleMessage");
		if (msg.what == 1) {
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
		Log.i("FragmentHomeSpeed", "clicke");
		int id = v.getId();
		for (int i = 0; i < 7; i++) {
			final int index = i;
			if (id == llytId[i]) {
				Log.i("FragmentHomeSpeed","clicke"+index);
				dialSpeed.startCheckAnimation(value[index], handler);
				textScore.setText(value[index]);
				return;
			}
		}
		// dialSpeed.startCheckAnimation(textSpeed.get, handler)
	}

}
