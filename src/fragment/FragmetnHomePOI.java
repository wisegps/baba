package fragment;

import java.util.ArrayList;
import java.util.HashMap;

import com.wise.baba.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;

/**
 * 
 * @author c 首页周边信息
 * 
 */
public class FragmetnHomePOI extends Fragment {
	private ImageView imgDown;// 下拉箭头
	private AutoCompleteTextView autoTextSearch;// 搜索框
	private GridView gridPOI = null;// 周边信息
	private View view;

	
	public  void set(){
		ArrayList<HashMap<String, String >> listItem = new ArrayList<HashMap<String, String>>();
		HashMap map = new HashMap();
		map.put("1", "111");
		listItem.add(map);
		
		map = new HashMap();
		map.put("1", "2222");
		listItem.add(map);
		map = new HashMap();
		map.put("1", "333");
		listItem.add(map);
		map = new HashMap();
		map.put("1", "444");
		listItem.add(map);
		map = new HashMap();
		map.put("1", "555");
		listItem.add(map);
		map = new HashMap();
		map.put("1", "666");
		listItem.add(map);
		
		// 生成适配器的ImageItem <====> 动态数组的元素，两者一一对应
				SimpleAdapter saImageItems = new SimpleAdapter(this.getActivity(), // 没什么解释
						listItem,// 数据来源
						android.R.layout.simple_list_item_1,// item的XML实现
						// 动态数组与Item对应的子项
						new String[] { "1"},
						// ImageItem的XML文件里面的一个ImageView,两个TextView ID
						new int[] { android.R.id.text1 });
				// 添加并且显示
				gridPOI.setAdapter(saImageItems);
				saImageItems.notifyDataSetChanged();
	}
	
	
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		set();
	}



	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_home_poi, container, false);
		imgDown = (ImageView) view.findViewById(R.id.imgDown);
		autoTextSearch = (AutoCompleteTextView) view
				.findViewById(R.id.autoTextSearch);
		gridPOI = (GridView) view.findViewById(R.id.gridPOI);
		
		set();
		
		
		
//		// 生成动态数组，并且转入数据
//		ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
//		int[] imgId = { 
//				R.drawable.ico_nav_home, 
//				R.drawable.ico_nav_company,
//				R.drawable.ico_nav_gas, 
//				R.drawable.ico_nav_parking,
//				R.drawable.ico_nav_food, 
//				R.drawable.ico_nav_hotel,
//				R.drawable.ico_nav_movie, 
//				R.drawable.ico_nav_more };
//		String[] text = { "家", "公司", "加油站", "停车场", "美食", "酒店", "电影院", "更多" };
//
//		HashMap<String, Object> map;
//		for (int i = 0; i < imgId.length; i++) {
//			map = new HashMap<String, Object>();
//			map.put("imgItem", imgId[i]);// 添加图像资源的ID
//			map.put("textItem", text[i]);// 文字描述
//			listItem.add(map);
//		}
//
//		// 生成适配器的ImageItem <====> 动态数组的元素，两者一一对应
//		SimpleAdapter saImageItems = new SimpleAdapter(this.getActivity(), // 没什么解释
//				listItem,// 数据来源
//				R.layout.item_home_poi,// item的XML实现
//				// 动态数组与Item对应的子项
//				new String[] { "imgItem", "textItem" },
//				// ImageItem的XML文件里面的一个ImageView,两个TextView ID
//				new int[] { R.id.imgItem, R.id.textItem });
//		// 添加并且显示
//		gridPOI.setAdapter(saImageItems);
//		saImageItems.notifyDataSetChanged();
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	
	}

}
