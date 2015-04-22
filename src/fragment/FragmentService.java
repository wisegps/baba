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
import com.wise.baba.ui.adapter.GridShopAdapter;
import com.wise.baba.ui.widget.CustomGridView;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
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
public class FragmentService extends Fragment implements OnItemClickListener{

	private AppApplication app;
	private View view;
	private CustomGridView gridShop;
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
		GridShopAdapter adapter = new GridShopAdapter(this.getActivity());
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
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		
	}
	
}