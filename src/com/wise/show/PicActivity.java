package com.wise.show;

import com.wise.baba.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

public class PicActivity extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pic);
		ImageView iv_pic = (ImageView)findViewById(R.id.iv_pic);
		Intent intent = getIntent();
		String imageUrl = intent.getStringExtra("imageUrl");
		ImageLoader imageLoader = ImageLoader.getInstance();
		iv_pic.setImageBitmap(imageLoader.getBitmapFromMemoryCache(imageUrl));
	}
}