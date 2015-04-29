package fragment;

import java.util.ArrayList;
import java.util.HashMap;

import listener.OnCardMenuListener;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Const;
import com.wise.baba.db.SharePOI;
import com.wise.baba.ui.widget.CustomGridView;
import com.wise.car.AddressActivity;
import com.wise.car.CarLocationActivity;
import com.wise.car.SearchLocationActivity;
import com.wise.car.SearchMapActivity;


/**
 * 
 * @author c 首页周边信息
 * 
 */
public class FragmentHomePOI extends Fragment implements OnItemClickListener,android.view.View.OnClickListener{
	
	private ImageView iv_poi_menu;// 下拉箭头
	private AutoCompleteTextView autoTextSearch;// 搜索框
	private CustomGridView gridPOI = null;// 周边信息
	private View view;
	/**
	 * 
	 * @周边信息GridView适配器
	 */
	public SimpleAdapter getAdapter() {
		// 生成动态数组，并且转入数据
		ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
		int[] imgId = { R.drawable.ico_poi_home, R.drawable.ico_poi_company,
				R.drawable.ico_poi_gas, R.drawable.ico_poi_parking,
				R.drawable.ico_poi_food, R.drawable.ico_poi_hotel,
				R.drawable.ico_poi_movie, R.drawable.ico_poi_more };
		String[] text = getPOIName();
		HashMap<String, Object> map;
		for (int i = 0; i < imgId.length; i++) {
			map = new HashMap<String, Object>();
			map.put("imgItem", imgId[i]);// 添加图像资源的ID
			map.put("textItem", text[i]);// 文字描述
			listItem.add(map);
		}

		SimpleAdapter saImageItems = new SimpleAdapter(this.getActivity(),
				listItem, R.layout.item_home_poi, new String[] { "imgItem",
						"textItem" }, new int[] { R.id.imgItem, R.id.textItem });

		return saImageItems;
	}
	
	public String[] getPOIName(){
		String[] points = { "家", "公司", "加油站", "停车场", "美食", "酒店", "电影院", "更多" };
		return points;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if(view == null){
			view = inflater.inflate(R.layout.fragment_home_poi, container,
					false);
		}else{
			ViewGroup parent = (ViewGroup) view.getParent();
			if (null != parent) {
				parent.removeView(view);
			}
		}
		
		iv_poi_menu = (ImageView) view.findViewById(R.id.iv_poi_menu);
		autoTextSearch = (AutoCompleteTextView) view
				.findViewById(R.id.autoTextSearch);
		autoTextSearch.setOnClickListener(this);
		iv_poi_menu.setOnClickListener(this);
		gridPOI = (CustomGridView) view.findViewById(R.id.gridPOI);
		gridPOI.setSelector(new ColorDrawable(Color.TRANSPARENT));//取消GridView中Item选中时默认的背景色  
		SimpleAdapter simpleAdapter = getAdapter();
		gridPOI.setAdapter(simpleAdapter);
		simpleAdapter.notifyDataSetChanged();
		gridPOI.setOnItemClickListener(this);
		return view;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if(position ==0){//家
			SharePOI share = new SharePOI(this.getActivity());
			double[] location = share.getHomeLocation();
			
			if (location[0] == 0.0 && location[1] == 0.0) {
				Toast.makeText(this.getActivity(), "家的地址未设置",
						Toast.LENGTH_SHORT).show();
				startActivity(new Intent(this.getActivity(),
						AddressActivity.class));
			} else {
				Intent intent = new Intent(this.getActivity(), CarLocationActivity.class);
				intent.putExtra("POI_FLAG", "home");
				startActivity(intent);
			}
			
		}else if(position == 1){//公司
			
			SharePOI share = new SharePOI(this.getActivity());
			double[] location = share.getCompanyLocation();
			
			if (location[0] == 0.0 && location[1] == 0.0) {
				Toast.makeText(this.getActivity(), "公司的地址未设置",
						Toast.LENGTH_SHORT).show();
				startActivity(new Intent(this.getActivity(),
						AddressActivity.class));
			} else {
				Intent intent = new Intent(this.getActivity(), CarLocationActivity.class);
				intent.putExtra("POI_FLAG", "company");
				startActivity(intent);
			}
			
		}else if(position ==7){//更多
			Toast.makeText(this.getActivity(), "敬请期待", Toast.LENGTH_SHORT).show();
		}else{
			toSearchMap(getPOIName()[position]);
		}
		
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.autoTextSearch:
			Intent intent = new Intent(this.getActivity(),
					SearchLocationActivity.class);
			//intent.putExtra("POI_FLAG", "autoTextSearch");
			startActivity(intent);
			break;
		case R.id.iv_poi_menu:
			if(onCardMenuListener != null){
				onCardMenuListener.showCarMenu(Const.TAG_POI);
			}
			break;
		}
		
	}
	
	/**
	 * 根据类型跳转搜索
	 * 
	 * @param keyWord
	 */
	private void toSearchMap(String keyWord) {
		AppApplication app = (AppApplication) getActivity().getApplication();
		if (app.carDatas == null || app.carDatas.size() == 0) {
			return;
		}
		// 地图搜寻
		Intent intent = new Intent(FragmentHomePOI.this.getActivity(), SearchMapActivity.class);
		intent.putExtra("keyWord", keyWord);
		intent.putExtra("key", keyWord);
		startActivity(intent);
	}

	OnCardMenuListener onCardMenuListener;
	public void setOnCardMenuListener(OnCardMenuListener onCardMenuListener) {
		this.onCardMenuListener = onCardMenuListener;
		
	}



}
