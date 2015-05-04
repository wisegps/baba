package com.wise.baba.ui.widget;

import java.util.List;
import com.wise.baba.R;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;

/**弹出List PopView**/
public class PopView {
	
	private Context mContext;
	ListView lv_pop;
	OnItemClickListener onItemClickListener;
	PopupWindow mPopupWindow;
	
	public PopView(Context context){
		mContext = context;
	}
	public void initView(View v){
		LayoutInflater mLayoutInflater = LayoutInflater.from(mContext);
        View popunwindwow = mLayoutInflater.inflate(R.layout.pop,null);
        mPopupWindow = new PopupWindow(popunwindwow, LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT);
        //mPopupWindow.setAnimationStyle(R.style.PopupAnimation);
        mPopupWindow.setAnimationStyle(R.style.AnimHead);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mPopupWindow.setFocusable(true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.showAtLocation(v, Gravity.BOTTOM, 0, 0);
        lv_pop = (ListView)popunwindwow.findViewById(R.id.lv_pop);
	}
	public void dismiss(){
        mPopupWindow.dismiss();
	}
	public void setData(List<String> items){
		lv_pop.setAdapter(new ItemAdapter(items));
	}
	
	public interface OnItemClickListener{
		public abstract void OnItemClick(int index);
	}
	
	public void SetOnItemClickListener(OnItemClickListener onItemClickListener){
		this.onItemClickListener = onItemClickListener;
	}
	
	class ItemAdapter extends BaseAdapter{
		private LayoutInflater layoutInflater;
		List<String> datas;
		public ItemAdapter(List<String> items){
			layoutInflater = LayoutInflater.from(mContext);
			datas = items;
		}
		@Override
		public int getCount() {
			return datas.size();
		}
		@Override
		public Object getItem(int position) {
			return datas.get(position);
		}
		@Override
		public long getItemId(int position) {
			return position;
		}
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if(convertView == null){
				convertView = layoutInflater.inflate(R.layout.item_pop, null);
	            holder = new ViewHolder();
	            holder.bt_item_pop = (Button) convertView.findViewById(R.id.bt_item_pop);
	            convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			holder.bt_item_pop.setText(datas.get(position));
			holder.bt_item_pop.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {
					if(onItemClickListener != null){
						onItemClickListener.OnItemClick(position);
					}
				}
			});
			return convertView;
		}
		private class ViewHolder {
	        Button bt_item_pop;
	    }
	}
}