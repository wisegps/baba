package com.wise.baba.ui.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.wise.baba.R;
import com.wise.baba.app.Constant;
import com.wise.baba.biz.GetSystem;
import com.wise.baba.db.dao.FriendData;
import com.wise.baba.ui.widget.CircleImageView;

public class FriendCareListAdapter	extends BaseAdapter {
	
	private List<FriendData> friendList = new ArrayList<FriendData>();
	private Context context;

	public FriendCareListAdapter(Context context) {
		super();
		this.context = context;
	}
	
	public void setFriendList(List<FriendData> list){
		this.friendList = list;
	}

	@Override
	public int getCount() {
		return friendList.size();
	}

	@Override
	public Object getItem(int position) {
		return friendList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(context);
			convertView = inflater.inflate(R.layout.item_friend, null);
			holder = new ViewHolder();
			holder.tv_name = (TextView) convertView
					.findViewById(R.id.tv_name);
			holder.iv_image = (CircleImageView) convertView
					.findViewById(R.id.iv_image);
			holder.tv_title = (TextView) convertView
					.findViewById(R.id.tv_item_group_title);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		FriendData friendData =friendList.get(position);
		// 判断是否是分组标题
		if (friendData.getGroup_letter() != null) {
			holder.tv_name.setVisibility(View.GONE);
			holder.iv_image.setVisibility(View.GONE);
			holder.tv_title.setVisibility(View.VISIBLE);
			holder.tv_title.setText(friendData.getGroup_letter());
			return convertView;
		} else {
			holder.tv_name.setVisibility(View.VISIBLE);
			holder.iv_image.setVisibility(View.VISIBLE);
			holder.tv_title.setVisibility(View.GONE);
		}

		holder.tv_name.setText(friendData.getFriend_name());
//		if (position == 0) {
//			// 第一项是新的朋友
//			holder.iv_image.setImageResource(R.drawable.ico_friend_new);
//		} else if (position == 1) {
//			// 第二项是服务商
//			holder.iv_image.setImageResource(R.drawable.ico_friend_service);
//		}
//		
//		else if (position == 2) {
//			// 第三项目是特别关心
//			holder.iv_image.setImageResource(R.drawable.ico_friend_care);
//		}
//		
//		else {
			if (new File(Constant.userIconPath
					+ GetSystem.getM5DEndo(friendData.getLogo()) + ".png")
					.exists()) {
				Bitmap image = BitmapFactory
						.decodeFile(Constant.userIconPath
								+ GetSystem.getM5DEndo(friendData.getLogo())
								+ ".png");
				holder.iv_image.setImageBitmap(image);
			} else {
				holder.iv_image.setImageResource(R.drawable.icon_people_no);
			}
		//}
		return convertView;
	}

	private class ViewHolder {
		TextView tv_name;
		CircleImageView iv_image;
		TextView tv_title;
	}
}
