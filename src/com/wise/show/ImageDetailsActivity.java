package com.wise.show;

import java.util.List;

import com.wise.baba.R;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Window;

public class ImageDetailsActivity extends Activity {
	private ZoomImageView zoomImageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_image_details);
		zoomImageView = (ZoomImageView) findViewById(R.id.zoom_image_view);
		String imagePath = getIntent().getStringExtra("image_path");
		if (imagePath == null || imagePath.equals("")) {

		} else {
			Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
			zoomImageView.setImageBitmap(bitmap);
		}
		int index = getIntent().getIntExtra("index", -1);
		List<String> pathList = getIntent().getStringArrayListExtra("pathList");
		if (pathList != null && pathList.size() != 0) {
			Bitmap bitmap = BitmapFactory.decodeFile(pathList.get(index));
			zoomImageView.setImageBitmap(bitmap);
			zoomImageView.setIndex(index);
			zoomImageView.setPathList(pathList);
		}
	}
}