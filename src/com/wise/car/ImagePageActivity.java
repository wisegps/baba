package com.wise.car;

import java.util.ArrayList;
import java.util.List;


import com.wise.baba.R;
import com.wise.baba.app.Constant;
import com.wise.show.ZoomImageView;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

/**
 * @author honesty
 **/
public class ImagePageActivity extends Activity {
	String[] images1 = { Constant.VehiclePath + "2301419401249743big.png",
			Constant.VehiclePath + "1781412066739954.png",
			Constant.VehiclePath + "2301415844218066.png" };
	List<String> pathList;
	List<ZoomImageView> zViews = new ArrayList<ZoomImageView>();
	ViewPager vp_image;
	TextView tv_index;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_image_page);
		tv_index = (TextView)findViewById(R.id.tv_index);		
		pathList = getIntent().getStringArrayListExtra("pathList");
		setViews();
		tv_index.setText("1/" + pathList.size());
		vp_image = (ViewPager) findViewById(R.id.vp_image);
		vp_image.setAdapter(new ImagePageAdapter());
		vp_image.setOnPageChangeListener(new OnPageChangeListener() {			
			@Override
			public void onPageSelected(int arg0) {
				tv_index.setText((arg0 + 1) + "/" + pathList.size());
			}			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {}			
			@Override
			public void onPageScrollStateChanged(int arg0) {}
		});
	}
	private void setViews(){
		for(int i = 0 ; i < pathList.size() ; i++){
			ZoomImageView imageView = new ZoomImageView(getApplicationContext());
			Bitmap bitmap = BitmapFactory.decodeFile(pathList.get(i));  
			imageView.setImageBitmap(bitmap);  
			zViews.add(imageView);
		}
	}
	class ImagePageAdapter extends PagerAdapter {
		@Override
		public int getCount() {
			return zViews.size();
		}
		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}
		@Override
		public int getItemPosition(Object object) {
			return super.getItemPosition(object);
		}
		@Override
		public void destroyItem(View container, int position, Object object) {
			((ViewPager) container).removeView(zViews.get(position));
		}
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			((ViewPager) container).addView(zViews.get(position));
			return zViews.get(position);
		}
	}
}
