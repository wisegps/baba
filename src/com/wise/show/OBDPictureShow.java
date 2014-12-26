package com.wise.show;

import java.util.ArrayList;
import java.util.List;

import pubclas.Blur;

import com.wise.baba.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;

public class OBDPictureShow extends Activity {
	private List<String> picSmall = null;
	private List<String> picBig = null;
	int index = -1;
	GridAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_picture_show);
		picSmall = getIntent().getStringArrayListExtra("picSmall");
		picBig = getIntent().getStringArrayListExtra("picBig");

		GridView gridView = (GridView) findViewById(R.id.pic_grid);
		adapter = new GridAdapter(picSmall);
		gridView.setAdapter(adapter);

		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Intent intent = new Intent(OBDPictureShow.this,
						ImageDetailsActivity.class);
				intent.putExtra("index", arg2);
				intent.putStringArrayListExtra("pathList",
						(ArrayList<String>) picBig);
				startActivity(intent);
			}
		});

		gridView.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				index = arg2;
				adapter.notifyDataSetChanged();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});
		findViewById(R.id.iv_back).setOnClickListener(onClickListener);
		findViewById(R.id.iv_add).setOnClickListener(onClickListener);
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			case R.id.iv_add:

				break;
			}
		}
	};

	class GridAdapter extends BaseAdapter {
		LayoutInflater mInflater = LayoutInflater.from(OBDPictureShow.this);
		private List<String> list = null;

		public GridAdapter(List<String> list) {
			this.list = list;
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
		public View getView(int position, View convertView, ViewGroup parent) {
			Holder mHolder;
			if (convertView == null) {
				mHolder = new Holder();
				convertView = mInflater.inflate(R.layout.item_pic_choose, null);
				mHolder.picShow = (ImageView) convertView
						.findViewById(R.id.image_pic_choose);
				mHolder.picCheck = (CheckBox) findViewById(R.id.pic_check);
				convertView.setTag(mHolder);
			} else {
				mHolder = (Holder) convertView.getTag();
			}
			Bitmap bitmap = BitmapFactory.decodeFile(list.get(position));
			mHolder.picShow.setImageBitmap(Blur.getSquareBitmap(bitmap));
			if (index == position) {
				mHolder.picCheck.setChecked(true);
			} else {
				mHolder.picCheck.setChecked(false);
			}
			return convertView;
		}

		class Holder {
			ImageView picShow;
			CheckBox picCheck;
		}
	}
}
