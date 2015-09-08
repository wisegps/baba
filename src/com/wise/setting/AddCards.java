package com.wise.setting;

import java.util.ArrayList;
import java.util.List;

import xlist.DragListView;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Const;
import com.wise.baba.app.Constant;
import com.wise.baba.db.ShareCards;
import com.wise.baba.entity.CardsData;
import com.wise.baba.ui.fragment.FragmentHome;


public class AddCards extends Activity {
	DragListView infoListView;
	List<CardsData> list = new ArrayList<CardsData>();
	InforAdapter adapter;
	private String[] cards = { Const.TAG_POI,Const.TAG_CAR,Const.TAG_SPEED,
			 Const.TAG_NEWS ,Const.TAG_WEATHER,Const.TAG_SERVICE,Const.TAG_NAV,Const.TAG_AIR};
	private ShareCards cardsSharePreferences;
	private boolean isResume = true;
	private int custType = 1;//是服务商2  个人1
	
	private View parent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_info);
		parent = findViewById(R.id.llytParent);
		AppApplication app = (AppApplication) getApplication();
		custType = app.cust_type;
		cardsSharePreferences = new ShareCards(this);
		setCardsDataList();
		infoListView = (DragListView) findViewById(R.id.info_add);
		TextView tvAdd = (TextView) findViewById(R.id.tv_info_add);
		adapter = new InforAdapter();
		infoListView.setAdapter(adapter);
		infoListView.setDragListener(mDrapListener);
		findViewById(R.id.tv_info_add).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						// 添加卡片跳转到添加页面
						Intent intent = new Intent(AddCards.this,
								ChooseCard.class);
						startActivityForResult(intent, 6);
					}
				});

		findViewById(R.id.iv_back).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				putJson();
				list.clear();
				Intent intent = new Intent(Constant.A_ChangeCards);
				sendBroadcast(intent);
				finish();
			}
		});
		
		
		
	}
	
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		showTip();
	}


	/**
	 * 显示提示
	 */
	public void showTip(){
		
		if(isResume == false){
			
			return;
		}
	
		
		new Handler().postDelayed(new Runnable(){
			@Override
			public void run() {
				int childCount = 0;
				childCount = infoListView.getChildCount();
				if(childCount<1){
					return;
				}
				View view = infoListView.getChildAt(childCount-1);
				TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 0,-view.getMeasuredHeight());
				translateAnimation.setDuration(700);
				translateAnimation.setRepeatMode(Animation.REVERSE);
				view.setAnimation(translateAnimation); 
				if(childCount<2){
					return;
				}
				View view1 = infoListView.getChildAt(childCount-2);
				TranslateAnimation translateAnimation1 = new TranslateAnimation(0, 0, 0,view1.getMeasuredHeight());
				translateAnimation1.setDuration(700);
				translateAnimation1.setRepeatMode(Animation.REVERSE);
				view1.setAnimation(translateAnimation1); 
				translateAnimation.start();
				translateAnimation1.start();
				isResume = false;
				
				
				Toast.makeText(AddCards.this, "上下拖动可以调整首页卡片顺序哦!", Toast.LENGTH_SHORT).show();
			}
			
		}, 200);
		
	}

	public void setCardsDataList() {
		list.clear();
		String[] sharedCards = cardsSharePreferences.get();
		if (sharedCards != null) {
			for (int i = 0; i < sharedCards.length; i++) {
				String cardName = sharedCards[i];
				if(cardName.equals(Const.TAG_SERVICE) && custType ==1){
					//个人
					continue;
				}
				
				for (int j = 0; j < cards.length; j++) {
					if (cardName.equals(cards[j])) {
						CardsData cardsData = new CardsData();
						cardsData.setIcon(Constant.picture[j]);
						cardsData.setTitle(Constant.title[j]);
						cardsData.setContent(Constant.content[j]);
						cardsData.setCardName(cardName);
						list.add(cardsData);
						break;
					}
				}
			}
		}
	}

	// 交换listview的数据
	private DragListView.DragListener mDrapListener = new DragListView.DragListener() {
		@Override
		public void drag(int from, int to, int i) {
			
			adapter.index = i;
			CardsData item = new CardsData();
			item = list.get(from);
			list.set(from, list.get(to));
			list.set(to, item);
			adapter.notifyDataSetChanged();
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			putJson();
			list.clear();
			Intent intent = new Intent(Constant.A_ChangeCards);
			sendBroadcast(intent);
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onStart() {
		super.onStart();
		setCardsDataList();
		adapter.notifyDataSetChanged();
	}

	// 保存数据
	private void putJson() {

		String[] cardNames = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			cardNames[i] = list.get(i).getCardName();
		}
		cardsSharePreferences.put(cardNames);
		FragmentHome.isChange = true;
	}
	
//	public void popupWindow(final View v){
//		new Handler().postDelayed(new Runnable(){
//			@Override
//			public void run() {
//				   // 一个自定义的布局，作为显示的内容
//		        View contentView = LayoutInflater.from(AddCards.this).inflate(
//		        		R.layout.item_card_delete_popup, null);
//				  PopupWindow popupWindow = new PopupWindow(contentView,
//			                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//				  popupWindow.setBackgroundDrawable(new BitmapDrawable());
//				  popupWindow.setOutsideTouchable(true);
//				  
//				  final int[] location = new int[2];   
//				  v.getLocationOnScreen(location);  
//				  popupWindow.showAtLocation(parent, Gravity.TOP, location[0], location[1]-15);
//				  infoListView.setEnabled(true);
//			}
//		}, 100);
//		
//		
//	}

	class InforAdapter extends BaseAdapter {

		private int index = -1;

		public int getIndex() {
			return index;
		}

		public void setIndex(int index) {
			this.index = index;
		}

		@Override
		public int getCount() {
			return list == null ? 0 : list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup arg2) {
			final Holder mHolder;
			// if (convertView == null) {
			mHolder = new Holder();
			convertView = (LayoutInflater.from(AddCards.this)).inflate(
					R.layout.item_info_delete, null);
			mHolder.info_icon = (ImageView) convertView
					.findViewById(R.id.info_icon);
			mHolder.tv_info_title = (TextView) convertView
					.findViewById(R.id.tv_info_title);
			mHolder.tv_info_content = (TextView) convertView
					.findViewById(R.id.tv_info_content);
			mHolder.iv_manage = (ImageView) convertView
					.findViewById(R.id.iv_manage);
			mHolder.iv_delete = (ImageView) convertView
					.findViewById(R.id.iv_delete);
			
			
			LinearLayout llytLeft = (LinearLayout) convertView.findViewById(R.id.llytLeft);
			mHolder.item_add = (TextView) convertView.findViewById(R.id.item_add);
			
			int llytLeftWidth = View.MeasureSpec.makeMeasureSpec(0,
	                View.MeasureSpec.UNSPECIFIED);
	        int llytLeftHeight = View.MeasureSpec.makeMeasureSpec(0,
	                View.MeasureSpec.UNSPECIFIED);
	        llytLeft.measure(llytLeftWidth, llytLeftHeight);
	        
	        int mLlytLeftHeight = llytLeft.getMeasuredHeight();
	        
	        mHolder.item_add.setHeight(mLlytLeftHeight);
			convertView.setTag(mHolder);


			
//			mHolder.iv_delete.setOnTouchListener(new OnTouchListener() {
//				
//				@Override
//				public boolean onTouch(View v, MotionEvent event) {
//					int e = event.getAction();
//					
//					switch(e){
//					case MotionEvent.ACTION_UP:
//						if(mHolder.item_add.getVisibility() == View.GONE){
//							mHolder.item_add.setVisibility(View.VISIBLE);
//						}else{
//							mHolder.item_add.setVisibility(View.GONE);
//						}
//						
//						break;
//					
//					}
//					
//					return true;
//				}
//			});
			
			mHolder.iv_manage.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					int visble = mHolder.iv_delete.getVisibility();
					if(visble == View.VISIBLE){
						mHolder.iv_delete.setVisibility(View.GONE);
					}else{
						mHolder.iv_delete.setVisibility(View.VISIBLE);
					}
				}
			});
			
			mHolder.iv_delete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					infoListView.stopDrag();
					list.remove(position);
					adapter.notifyDataSetChanged();
					putJson();
				}
			});
			
			mHolder.info_icon.setImageResource(list.get(position).getIcon());
			mHolder.tv_info_title.setText(list.get(position).getTitle());
			mHolder.tv_info_content.setText(list.get(position).getContent());
			mHolder.item_add.setText("删除");
			mHolder.item_add.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					infoListView.stopDrag();
					list.remove(position);
					adapter.notifyDataSetChanged();
					putJson();
				}
			});
			if (index == position) {
				convertView.setVisibility(View.INVISIBLE);
			} else {
				convertView.setVisibility(View.VISIBLE);
			}
			return convertView;
		}

		class Holder {
			ImageView info_icon,iv_delete,iv_manage;
			TextView tv_info_title, tv_info_content;
			TextView item_add;
			
		}
	}
}
