package widget.adapters;

import java.util.List;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.wise.baba.R;
import com.wise.baba.app.Constant;
import com.wise.baba.biz.GetSystem;
import com.wise.baba.entity.FriendSearch;


/**
 * 
 * 搜索好友，查询列表适配器
 * 
 * @author ccc
 * 
 */
public class FriendListAdapter extends BaseAdapter {

	private LayoutInflater inflater = null;
	private Context context = null;
	private List friends;
	private RequestQueue mQueue;

	public FriendListAdapter(Context context, List friends) {
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		this.friends = friends;
		mQueue = Volley.newRequestQueue(context);
	}

	@Override
	public int getCount() {
		return friends.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_friend, null);
		}

		final ImageView imgLogo = (ImageView) convertView
				.findViewById(R.id.iv_image);
		TextView textName = (TextView) convertView.findViewById(R.id.tv_name);
		FriendSearch friend = (FriendSearch) friends.get(position);
		textName.setText(friend.getCust_name());
		final String logoUrl = friend.getLogo();
		final int friendId = friend.getCust_id();
		if(logoUrl == null || friendId == 0){
			return convertView;
		}
		//先查看本地有没有图片
		Bitmap bitmap = BitmapFactory.decodeFile(Constant.userIconPath
				+ friendId + ".png");
		if (bitmap != null) {
			imgLogo.setImageBitmap(bitmap);
			return convertView;
		}
		
		
		//查看网络读取路径
		if (!logoUrl.startsWith("http://")) {
			imgLogo.setImageResource(R.drawable.icon_people_no);
			return convertView;
		}
		
		
		//从网络读取
		mQueue.add(new ImageRequest(logoUrl, new Response.Listener<Bitmap>() {
			@Override
			public void onResponse(Bitmap response) {
				GetSystem.saveImageSD(response, Constant.userIconPath,friendId 
						+ ".png", 100);
				imgLogo.setImageBitmap(response);
			}
		}, 0, 0, Config.RGB_565, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				imgLogo.setImageResource(R.drawable.icon_people_no);
				error.printStackTrace();
			}
		}));
		return convertView;
	}
}