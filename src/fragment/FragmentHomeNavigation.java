package fragment;

import java.util.ArrayList;
import java.util.HashMap;

import listener.OnCardMenuListener;

import com.wise.baba.R;
import com.wise.baba.app.Const;

import customView.CustomGridView;

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
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

/**
 * 
 * @author c 首页导航信息
 * 
 */
public class FragmentHomeNavigation extends Fragment implements OnItemClickListener,android.view.View.OnClickListener{
	
	private ImageView imgDown;// 下拉箭头
	private CustomGridView gridNav = null;//导航信息
	private View view;
	/**
	 * 
	 * 导航信息GridView适配器
	 */
	public SimpleAdapter getAdapter() {
		// 生成动态数组，并且转入数据
		ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
		int[] imgId = { R.drawable.ico_nav_location, R.drawable.ico_nav_trip,
				R.drawable.ico_nav_health, R.drawable.ico_nav_drive,
				R.drawable.ico_nav_oil, R.drawable.ico_nav_expenses,
				R.drawable.ico_nav_detail, R.drawable.ico_nav_notice,R.drawable.ico_nav_query};
		String[] text = getNavName();
		HashMap<String, Object> map;
		for (int i = 0; i < imgId.length; i++) {
			map = new HashMap<String, Object>();
			map.put("imgItem", imgId[i]);// 添加图像资源的ID
			map.put("textItem", text[i]);// 文字描述
			listItem.add(map);
		}

		SimpleAdapter saImageItems = new SimpleAdapter(this.getActivity(),
				listItem, R.layout.item_home_nav, new String[] { "imgItem",
						"textItem" }, new int[] { R.id.imgItem, R.id.textItem });

		return saImageItems;
	}
	
	public String[] getNavName(){
		String[] navigations = { "位置监控", "车辆行程", "车辆体检", "驾驶得分", "油耗分析", "费用分析", "费用明细", "车务提醒", "违章查询" };
		return navigations;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if(view == null){
			view = inflater.inflate(R.layout.fragment_home_nav, container,
					false);
		}else{
			ViewGroup parent = (ViewGroup) view.getParent();
			if (null != parent) {
				parent.removeView(view);
			}
		}
		
		imgDown = (ImageView) view.findViewById(R.id.imgDown);
		imgDown.setOnClickListener(this);
		gridNav = (CustomGridView) view.findViewById(R.id.gridNav);
		gridNav.setSelector(new ColorDrawable(Color.TRANSPARENT));//取消GridView中Item选中时默认的背景色  
		SimpleAdapter simpleAdapter = getAdapter();
		gridNav.setAdapter(simpleAdapter);
		simpleAdapter.notifyDataSetChanged();
		gridNav.setOnItemClickListener(this);
		
		
		
		return view;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.imgDown:
			if(onCardMenuListener != null){
				onCardMenuListener.showCarMenu(Const.TAG_NAV);
			}
			break;
		}
		
	}
	

	OnCardMenuListener onCardMenuListener;
	public void setOnCardMenuListener(OnCardMenuListener onCardMenuListener) {
		this.onCardMenuListener = onCardMenuListener;
		
	}



}
