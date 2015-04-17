package fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import pubclas.Constant;
import pubclas.GetSystem;
import pubclas.ShowErWeiMa;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.wise.baba.AppApplication;
import com.wise.baba.R;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 服务商模块
 *@author honesty
 **/
public class FragmentService extends Fragment{

	List<ServiceData> serviceDatas = new ArrayList<ServiceData>();
	AppApplication app;
	ImageView iv_logo;
	TextView tv_name;
	RequestQueue mQueue;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_service, container, false);
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		app = (AppApplication) getActivity().getApplication();
		mQueue = Volley.newRequestQueue(getActivity());
		getData();
		iv_logo = (ImageView)getActivity().findViewById(R.id.iv_logo);
		tv_name = (TextView)getActivity().findViewById(R.id.tv_name);
		TextView tv_phone = (TextView)getActivity().findViewById(R.id.tv_phone);
		TextView tv_adress = (TextView)getActivity().findViewById(R.id.tv_adress);
		ImageView iv_eweima = (ImageView)getActivity().findViewById(R.id.iv_eweima);
		iv_eweima.setOnClickListener(onClickListener);
		GridView gridView = (GridView)getActivity().findViewById(R.id.gv_service);
		ServiceAdapter serviceAdapter = new ServiceAdapter();
		gridView.setAdapter(serviceAdapter);
		GetCustomer();
	}
	OnClickListener onClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_eweima:
				DisplayMetrics dm = new DisplayMetrics();
				getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
				int width = dm.widthPixels;
				int QR_WIDTH = width / 2;
				ShowErWeiMa showErWeiMa = new ShowErWeiMa(getActivity(), iv_logo, QR_WIDTH, app.cust_id);
				showErWeiMa.openErWeiMa();
				break;
			}
		}
	};
	private void GetCustomer() {
		SharedPreferences preferences = getActivity().getSharedPreferences(
				Constant.sharedPreferencesName, Context.MODE_PRIVATE);
		String customer = preferences.getString(Constant.sp_customer
				+ app.cust_id, "");
		if (customer.equals("")) {

		} else {
			jsonCustomer(customer);
		}

	}
	/** 获取个人信息 **/
	private void jsonCustomer(String str) {
		try {
			JSONObject jsonObject = new JSONObject(str);
			if (jsonObject.opt("status_code") == null) {
				tv_name.setText(jsonObject.getString("cust_name"));
				final String logo = jsonObject.getString("logo");
				Bitmap bimage = BitmapFactory.decodeFile(Constant.userIconPath
						+ GetSystem.getM5DEndo(logo) + ".png");
				if (bimage != null) {
					iv_logo.setImageBitmap(bimage);
				}
				if (logo == null || logo.equals("")) {

				} else {
					mQueue.add(new ImageRequest(
							logo,
							new Response.Listener<Bitmap>() {
								@Override
								public void onResponse(Bitmap response) {
									GetSystem.saveImageSD(
											response,
											Constant.userIconPath,
											GetSystem.getM5DEndo(logo) + ".png",
											100);
									iv_logo.setImageBitmap(response);
								}
							}, 0, 0, Config.RGB_565,
							new Response.ErrorListener() {
								@Override
								public void onErrorResponse(VolleyError error) {
									error.printStackTrace();
								}
							}));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private class ServiceAdapter extends BaseAdapter{
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		@Override
		public int getCount() {
			System.out.println("serviceDatas = " + serviceDatas.size());
			return serviceDatas.size();
		}
		@Override
		public Object getItem(int position) {
			return serviceDatas.get(position);
		}
		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			viewService holder = null;
			if(convertView == null){
				holder = new viewService();
				convertView = inflater.inflate(R.layout.item_service_card, null);
				holder.tv_number = (TextView)convertView.findViewById(R.id.tv_number);
				holder.tv_key = (TextView)convertView.findViewById(R.id.tv_key);
				convertView.setTag(holder);
			}else{
				holder = (viewService) convertView.getTag();
			}
			ServiceData serviceData = serviceDatas.get(position);
			holder.tv_key.setText(serviceData.key);
			holder.tv_number.setText(""+serviceData.value);
			return convertView;
		}
		private class viewService{
			TextView tv_number;
			TextView tv_key;
		}
	}
	
	private void getData(){
		for(int i = 0 ; i < 8 ; i++){
			ServiceData serviceData = new ServiceData();
			serviceData.key = "用户总数";
			serviceData.value = 5;
			serviceDatas.add(serviceData);
		}
	}
	class ServiceData{
		String key;
		int value;
	}
}