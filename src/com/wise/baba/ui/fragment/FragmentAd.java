package com.wise.baba.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.wise.baba.R;
import com.wise.baba.app.Constant;
import com.wise.baba.net.NetThread;
import com.wise.baba.ui.widget.HScrollLayout;
import com.wise.baba.ui.widget.OnViewChangeListener;


/**
 * @author honesty
 **/
public class FragmentAd extends Fragment {
	/** 获取广告 **/
	private static final int get_ad = 13;
	HScrollLayout hs_photo;

	LinearLayout ll_image;
	TextView tv_content;

	int image_position = 0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_ad, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mQueue = Volley.newRequestQueue(getActivity());
		getAD();
		hs_photo = (HScrollLayout) getActivity().findViewById(R.id.hs_photo);
		hs_photo.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getAdHeight()));

		tv_content = (TextView) getActivity().findViewById(R.id.tv_content);
		ll_image = (LinearLayout) getActivity().findViewById(R.id.ll_image);
		hs_photo.setOnViewChangeListener(new OnViewChangeListener() {
			@Override
			public void OnViewChange(int view, int duration) {
				image_position = view;
				tv_content.setText(adDatas.get(view).getContent());
				changeImage(view);
			}
		});
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_pic:
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(adDatas.get(image_position).getUrl()));
				startActivity(intent);
				break;
			}
		}
	};
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case get_ad:
				setImageView(msg.obj.toString());
				getImage();
				if (adDatas.size() > 0) {
					changeImage(0);
					tv_content.setText(adDatas.get(0).getContent());
				}
				break;
			}
		}

	};

	private void setImageView(String result) {
		try {
			JSONArray jsonArray = new JSONArray(result);
			for (int i = 0; i < jsonArray.length(); i++) {
				View view_image = LayoutInflater.from(getActivity()).inflate(R.layout.item_nocar_image, null);
				hs_photo.addView(view_image);
				ImageView iv_pic = (ImageView) view_image.findViewById(R.id.iv_pic);
				iv_pic.setOnClickListener(onClickListener);
				ADView aView = new ADView();
				aView.setImageView(iv_pic);
				adViews.add(aView);
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				AData aData = new AData();
				aData.setImage(jsonObject.getString("image"));
				aData.setContent(jsonObject.getString("content"));
				aData.setUrl(jsonObject.getString("url"));
				adDatas.add(aData);

				ImageView imageView = new ImageView(getActivity());
				imageView.setImageResource(R.drawable.round_press);
				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(15, 15);
				lp.setMargins(5, 0, 5, 0);
				imageView.setLayoutParams(lp);
				ll_image.addView(imageView);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void changeImage(int index) {
		for (int i = 0; i < ll_image.getChildCount(); i++) {
			ImageView imageView = (ImageView) ll_image.getChildAt(i);
			if (index == i) {
				imageView.setImageResource(R.drawable.round);
			} else {
				imageView.setImageResource(R.drawable.round_press);
			}
		}
	}

	/** 获取控件的高度 **/
	public int getAdHeight() {
		// 690*512宽高
		int imageWidth = 690;
		int imageHeight = 512;
		DisplayMetrics metrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int widthPixels = metrics.widthPixels;
		double ratio = imageWidth / (widthPixels * 1.0);
		return (int) (imageHeight / ratio);
	}

	List<ADView> adViews = new ArrayList<ADView>();
	List<AData> adDatas = new ArrayList<AData>();

	private class ADView {
		ImageView imageView;

		public ImageView getImageView() {
			return imageView;
		}

		public void setImageView(ImageView imageView) {
			this.imageView = imageView;
		}
	}

	private class AData {
		private String image;
		private String content;
		private String url;

		public String getImage() {
			return image;
		}

		public void setImage(String image) {
			this.image = image;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		@Override
		public String toString() {
			return "AData [image=" + image + ", content=" + content + ", url=" + url + "]";
		}
	}

	private void getAD() {
		String url = Constant.BaseUrl + "base/AD";
		new NetThread.GetDataThread(handler, url, get_ad).start();
	}

	RequestQueue mQueue;

	private void getImage() {
		for (final AData aData : adDatas) {
			mQueue.add(new ImageRequest(aData.getImage(), new Response.Listener<Bitmap>() {
				@Override
				public void onResponse(Bitmap response) {
					for (int i = 0; i < adDatas.size(); i++) {
						if (adDatas.get(i).getImage().equals(aData.getImage())) {
							setImageWidthHeight(adViews.get(i).getImageView(), response);
							adViews.get(i).getImageView().setImageBitmap(response);
						}
					}
				}
			}, 0, 0, Config.RGB_565, new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					error.printStackTrace();
				}
			}));
		}
	}

	/** 计算设置图片的宽高 **/
	private void setImageWidthHeight(ImageView iv_pic, Bitmap bitmap) {
		DisplayMetrics metrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int widthPixels = metrics.widthPixels;

		double ratio = bitmap.getWidth() / (widthPixels * 1.0);
		int scaledHeight = (int) (bitmap.getHeight() / ratio);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(widthPixels, scaledHeight);
		iv_pic.setLayoutParams(params);
	}
}
