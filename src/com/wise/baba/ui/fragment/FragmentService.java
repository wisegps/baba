package com.wise.baba.ui.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;


import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Constant;
import com.wise.baba.app.Msg;
import com.wise.baba.biz.GetSystem;
import com.wise.baba.biz.HttpServiceProvider;
import com.wise.baba.biz.ShowErWeiMa;
import com.wise.baba.ui.adapter.GridShopAdapter;
import com.wise.baba.ui.widget.CustomGridView;
import com.wise.baba.ui.widget.NavigationLayout;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

/**
 * 服务商模块
 *@author honesty
 **/
public class FragmentService extends Fragment implements OnItemClickListener, Callback{

	private AppApplication app;
	private View view;
	private CustomGridView gridShop;
	private GridShopAdapter adapter;
	private HttpServiceProvider  HttpService;
	private Handler handler;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		if (view == null) {
			view = inflater.inflate(R.layout.fragment_service, container,
					false);
		} else {
			ViewGroup parent = (ViewGroup) view.getParent();
			if (null != parent) {
				parent.removeView(view);
			}
		}

		gridShop = (CustomGridView) view.findViewById(R.id.gridShop);
		gridShop.setSelector(new ColorDrawable(Color.TRANSPARENT));// 取消GridView中Item选中时默认的背景色
		adapter = new GridShopAdapter(this.getActivity());
		gridShop.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		gridShop.setOnItemClickListener(this);
		app = (AppApplication) getActivity()
				.getApplication();
		return view;
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		app = (AppApplication) getActivity().getApplication();
		handler = new Handler(this);
		HttpService= new HttpServiceProvider(this.getActivity(), handler);
		HttpService.request();
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		NavigationLayout navigationLayout = (NavigationLayout) this.getActivity().findViewById(R.id.navigationLayout);
		navigationLayout.performTabClick(2);
	}
	/* (non-Javadoc)
	 * @see android.os.Handler.Callback#handleMessage(android.os.Message)
	 */
	@Override
	public boolean handleMessage(Message msg) {
		switch(msg.what){
		case Msg.Get_Customer_Total:
			int [] totals = (int[]) msg.obj;
			adapter.setValue(totals);
			adapter.notifyDataSetChanged();
			break;
		}
		return false;
	}
	
}