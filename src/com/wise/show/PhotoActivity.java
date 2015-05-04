package com.wise.show;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import com.aliyun.android.oss.model.OSSObject;
import com.aliyun.android.oss.task.GetObjectTask;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Constant;
import com.wise.baba.biz.GetSystem;
import com.wise.baba.net.NetThread;
import com.wise.baba.ui.widget.CircleImageView;
import com.wise.notice.LetterActivity;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

/** 相片信息，评论界面 **/
public class PhotoActivity extends Activity {
	private static final String TAG = "PicActivity";
	private static final int getPhoto = 1;
	private static final int getBigImage = 2;
	private static final int setComments = 3;
	private static final int getPersionImage = 4;
	/** 点赞 **/
	private static final int praise = 5;

	RequestQueue mQueue;
	ListView lv_comments;
	TextView tv_time, tv_persion, tv_content, tv_adress, tv_praise, tv_see,
			tv_people;
	ImageView iv_back, iv_car_logo, iv_sex, iv_pic;
	CircleImageView iv_persion_icon;
	EditText et_comments;
	ImageData imageData;
	int position = 0;
	List<PhotoData> photoDatas = new ArrayList<PhotoData>();
	PhotoAdapter photoAdapter;
	ProgressDialog progressDialog;
	/** 个人头像路径 **/
	String logo = "";
	/** 是否修改了点赞状态 **/
	boolean isPraises = false;
	int textHeight = 45;
	AppApplication app;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_photo);
		app = (AppApplication)getApplication();
		mQueue = Volley.newRequestQueue(this);
		ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		ImageView iv_more = (ImageView) findViewById(R.id.iv_more);
		iv_more.setOnClickListener(onClickListener);
		TextView tv_send = (TextView) findViewById(R.id.tv_send);
		tv_send.setOnClickListener(onClickListener);
		et_comments = (EditText) findViewById(R.id.et_comments);
		tv_people = (TextView) findViewById(R.id.tv_people);

		imageData = (ImageData) getIntent().getSerializableExtra("imageData");
		position = getIntent().getIntExtra("position", 0);

		lv_comments = (ListView) findViewById(R.id.lv_comments);
		View v = LayoutInflater.from(this).inflate(R.layout.view_photo_header,
				null);
		lv_comments.addHeaderView(v, null, false);
		photoAdapter = new PhotoAdapter();
		lv_comments.setAdapter(photoAdapter);
		lv_comments.setOnScrollListener(onScrollListener);
		lv_comments.setOnItemLongClickListener(onItemLongClickListener);

		TextView tv_share = (TextView) v.findViewById(R.id.tv_share);
		tv_share.setOnClickListener(onClickListener);
		tv_time = (TextView) v.findViewById(R.id.tv_time);
		String Time = GetSystem.ChangeTimeZone(imageData.getCreate_time()
				.replace("T", " ").substring(0, 19));
		tv_time.setText(Time);
		tv_persion = (TextView) v.findViewById(R.id.tv_persion);
		tv_content = (TextView) v.findViewById(R.id.tv_content);
		tv_adress = (TextView) v.findViewById(R.id.tv_adress);
		tv_praise = (TextView) v.findViewById(R.id.tv_praise);
		tv_praise.setText("" + imageData.getPraise_count());
		tv_praise.setOnClickListener(onClickListener);
		if (imageData.isCust_praise()) {
			Drawable drawable = getResources().getDrawable(
					R.drawable.icon_zan_reply);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(),
					drawable.getMinimumHeight());
			tv_praise.setCompoundDrawables(drawable, null, null, null);
		} else {
			Drawable drawable = getResources().getDrawable(
					R.drawable.icon_zan_nor);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(),
					drawable.getMinimumHeight());
			tv_praise.setCompoundDrawables(drawable, null, null, null);
		}
		tv_see = (TextView) v.findViewById(R.id.tv_see);
		iv_persion_icon = (CircleImageView) v
				.findViewById(R.id.iv_persion_icon);
		iv_car_logo = (ImageView) v.findViewById(R.id.iv_car_logo);
		iv_sex = (ImageView) v.findViewById(R.id.iv_sex);
		iv_pic = (ImageView) v.findViewById(R.id.iv_pic);
		String imageUrl = imageData.getSmall_pic_url();
		image_path = imageUrl;
		ImageLoader imageLoader = ImageLoader.getInstance();
		Bitmap bitmap = imageLoader.getBitmapFromMemoryCache(imageUrl);
		setImageWidthHeight(bitmap);
		iv_pic.setImageBitmap(bitmap);
		iv_pic.setOnClickListener(onClickListener);
		/** 获取大图片 **/
		String url = Constant.BaseUrl + "photo/" + imageData.getPhoto_id()
				+ "?auth_code=" + app.auth_code;
		new NetThread.GetDataThread(handler, url, getPhoto).start();
		getLogo();
		textHeight = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 40, getResources()
						.getDisplayMetrics());
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				updatePhotoInfo();
				break;
			case R.id.iv_pic:
				turnImageDetails();
				break;
			case R.id.iv_more:
				if (cust_id == null || cust_id.equals("0")) {
					// 没读取到数据
				} else if (cust_id.equals(app.cust_id)) {
					// 自己不能给自己私信
				} else {
					showMenu();
				}
				break;
			case R.id.tv_send:// 发表评论
				sendComments();
				break;
			case R.id.tv_praise:// 点赞
				praises();
				break;
			case R.id.tv_letter:// 私信
				Intent intent = new Intent(PhotoActivity.this,
						LetterActivity.class);
				intent.putExtra("cust_id", cust_id);
				intent.putExtra("cust_name", cust_name);
				startActivity(intent);
				mPopupWindow.dismiss();
				break;
			case R.id.tv_Comments:
				//回复
				reply = "";
				tv_people.setText("");
				mPopupWindow.dismiss();
				break;
			}
		}
	};
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case getPhoto:
				jsonPhoto(msg.obj.toString());
				break;
			case getBigImage:
				// 显示图片
				Bitmap image = BitmapFactory.decodeFile(Constant.VehiclePath + imageName);
				iv_pic.setImageBitmap(image);
				image_path = imageUrl;
				break;
			case setComments:
				jsonComments(msg.obj.toString());
				break;
			case getPersionImage:
				removeThreadMark(msg.arg1);
				photoAdapter.notifyDataSetChanged();
				break;
			case praise:// 点赞
				jsonPraise(msg.obj.toString());
				break;
			}
		}
	};

	//长按监听，弹出提示框
	OnItemLongClickListener onItemLongClickListener = new OnItemLongClickListener() {
		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
				final int arg2, long arg3) {
			replyPopWin(arg1, arg2, true);
			return true;
		}
	};

	//弹出回复提示框
	private void replyPopWin(View v, int position, boolean b) {
		final PhotoData photoData;
		if (b) {
			photoData = photoDatas.get(position - 1);// 去掉头部
		} else {
			photoData = photoDatas.get(position);
		}
		if (photoData.getCust_id().equals(app.cust_id)) {
			//Toast.makeText(PhotoActivity.this, "不能回复自己", Toast.LENGTH_SHORT).show();
		} else {
			LayoutInflater mLayoutInflater = LayoutInflater
					.from(PhotoActivity.this);
			View popunwindwow = mLayoutInflater.inflate(
					R.layout.item_menu_horizontal, null);
			TextView tv_item_letter = (TextView) popunwindwow
					.findViewById(R.id.tv_item_letter);
			TextView tv_item_reply = (TextView) popunwindwow
					.findViewById(R.id.tv_item_reply);

			tv_item_reply.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					reply = photoData.getCust_name();
					tv_people.setText("回复" + photoData.getCust_name() + ":");
					mPopupWindow.dismiss();
				}
			});

			tv_item_letter.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(PhotoActivity.this,
							LetterActivity.class);
					intent.putExtra("cust_id", photoData.getCust_id());
					intent.putExtra("cust_name", photoData.getCust_name());
					startActivity(intent);
					mPopupWindow.dismiss();
				}
			});
			mPopupWindow = new PopupWindow(popunwindwow,
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			mPopupWindow.setAnimationStyle(R.style.PopupAnimation);
			mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
			mPopupWindow.setFocusable(true);
			mPopupWindow.setOutsideTouchable(true);
			mPopupWindow.showAsDropDown(v, widthPixels / 3,
					-(textHeight + v.getHeight()) / 2);
		}

	}

	OnScrollListener onScrollListener = new OnScrollListener() {
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			switch (scrollState) {
			case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:// 触摸状态
				break;
			case OnScrollListener.SCROLL_STATE_FLING:// 滑动状态
				break;
			case OnScrollListener.SCROLL_STATE_IDLE:// 停止
				// 读取图片
				getPersionImage();
				break;
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {

		}
	};

	/** 获取显示区域的图片 **/
	private void getPersionImage() {
		int start = lv_comments.getFirstVisiblePosition();
		int stop = lv_comments.getLastVisiblePosition();
		//TODO 显示图片需要调整
		for (int i = start; i <= stop; i++) {
			if (start == 0) {

			} else {
				// 判断图片是否存在
				if (new File(Constant.userIconPath
						+ GetSystem.getM5DEndo(photoDatas.get(i - 1).getIcon()) + ".png").exists()) {

				} else {
					if (isThreadRun(i - 1)) {
						// 如果图片正在读取则跳过
					} else {
						photoThreadId.add(i - 1);
						new ImageThread(i - 1).start();
					}
				}
			}
		}
	}

	List<Integer> photoThreadId = new ArrayList<Integer>();

	/** 判断图片是否开启了线程正在读图 **/
	private boolean isThreadRun(int positon) {
		for (int i = 0; i < photoThreadId.size(); i++) {
			if (positon == photoThreadId.get(i)) {
				return true;
			}
		}
		return false;
	}
	/**
	 * 删除列表里正在下载的线程标识
	 * @param position
	 */
	private void removeThreadMark(int position){
		for (int i = 0; i < photoThreadId.size(); i++) {
			if (photoThreadId.get(i) == position) {
				photoThreadId.remove(i);
				break;
			}
		}
	}

	class ImageThread extends Thread {
		int position;

		public ImageThread(int position) {
			this.position = position;
		}

		@Override
		public void run() {
			super.run();
			Bitmap bitmap = GetSystem.getBitmapFromURL(photoDatas.get(position)
					.getIcon());
			if (bitmap != null) {
				GetSystem.saveImageSD(bitmap, Constant.userIconPath, GetSystem.getM5DEndo(photoDatas
						.get(position).getIcon()) + ".png", 100);
			}
			Message message = new Message();
			message.what = getPersionImage;
			message.arg1 = position;
			handler.sendMessage(message);
		}
	}

	/** 点赞 **/
	private void praises() {
		if (imageData.isCust_praise()) {
			// 已经点过攒
		} else {
			int Photo_id = imageData.getPhoto_id();
			String url = Constant.BaseUrl + "photo/" + Photo_id
					+ "/praise?auth_code=" + app.auth_code;
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("cust_id", app.cust_id));
			pairs.add(new BasicNameValuePair("cust_name", app.cust_name));
			pairs.add(new BasicNameValuePair("icon", logo));
			new NetThread.putDataThread(handler, url, pairs, praise).start();
		}
	}

	/** 判断点赞 **/
	private void jsonPraise(String result) {
		try {
			JSONObject jsonObject = new JSONObject(result);
			if (jsonObject.getInt("status_code") == 0) {
				// 点赞成功
				// 考虑在连续点2次的情况
				if (!imageData.isCust_praise()) {
					isPraises = true;
					imageData.setCust_praise(true);
					// 修改图片点赞状态
					Drawable drawable = getResources().getDrawable(
							R.drawable.icon_zan_reply);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(),
							drawable.getMinimumHeight());
					tv_praise.setCompoundDrawables(drawable, null, null, null);
					// 点赞次数+1;
					int Praise_count = imageData.getPraise_count() + 1;
					imageData.setPraise_count(Praise_count);
					// 刷新点赞数目
					tv_praise.setText("" + imageData.getPraise_count());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** 解析发表评论 **/
	private void jsonComments(String result) {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
		try {
			JSONObject jsonObject = new JSONObject(result);
			if (jsonObject.getInt("status_code") == 0) {
				// 发表成功
				PhotoData photoData = new PhotoData();
				photoData.setContent(comments);
				photoData.setCust_name(app.cust_name);
				photoData.setCreate_time(GetSystem.GetNowTime());
				photoData.setCust_id(app.cust_id);
				photoData.setReply(reply);
				photoDatas.add(photoData);
				photoAdapter.notifyDataSetChanged();

				reply = "";
				comments = "";
				et_comments.setText("");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	String comments = "";
	String reply = "";

	private void sendComments() {
		comments = et_comments.getText().toString().trim();
		if (comments.equals("")) {
			Toast.makeText(PhotoActivity.this, "评论不能为空", Toast.LENGTH_SHORT)
					.show();
			return;
		} else {
			progressDialog = ProgressDialog.show(PhotoActivity.this, "提示",
					"评论发送中");
			progressDialog.setCancelable(true);
			String url = Constant.BaseUrl + "photo/" + imageData.getPhoto_id()
					+ "/comment?auth_code=" + app.auth_code;
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("cust_id", app.cust_id));
			pairs.add(new BasicNameValuePair("cust_name", app.cust_name));
			pairs.add(new BasicNameValuePair("icon", logo));
			pairs.add(new BasicNameValuePair("content", comments));
			pairs.add(new BasicNameValuePair("reply", reply));
			new NetThread.putDataThread(handler, url, pairs, setComments)
					.start();
		}
	}

	private void updatePhotoInfo() {
		if (isPraises) {
			// 如果修改了点赞的状态，需要传回点赞的数目和自己是否点赞
			Intent intent = new Intent();
			intent.putExtra("position", position);
			intent.putExtra("Praise_count", imageData.getPraise_count());
			intent.putExtra("isCust_praise", imageData.isCust_praise());
			setResult(2, intent);
		} else {
			finish();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			updatePhotoInfo();
		}
		return super.onKeyDown(keyCode, event);
	}

	class PhotoAdapter extends BaseAdapter {
		private LayoutInflater inflater = LayoutInflater
				.from(PhotoActivity.this);

		@Override
		public int getCount() {
			return photoDatas.size();
		}

		@Override
		public Object getItem(int arg0) {
			return photoDatas.get(arg0);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.item_photo_comments,
						null);
				holder = new ViewHolder();
				holder.tv_name = (TextView) convertView
						.findViewById(R.id.tv_name);
				holder.tv_time = (TextView) convertView
						.findViewById(R.id.tv_time);
				holder.tv_content = (TextView) convertView
						.findViewById(R.id.tv_content);
				holder.tv_span = (TextView) convertView
						.findViewById(R.id.tv_span);
				holder.tv_to_name = (TextView) convertView
						.findViewById(R.id.tv_to_name);
				holder.iv_comments = (ImageView) convertView
						.findViewById(R.id.iv_comments);
				holder.iv_logo = (CircleImageView) convertView
						.findViewById(R.id.iv_logo);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			final PhotoData photoData = photoDatas.get(position);
			holder.tv_content.setText(photoData.getContent());
			if (photoData.getReply() == null || photoData.getReply().equals("")) {
				holder.tv_name.setText(photoData.getCust_name());
				holder.tv_span.setVisibility(View.GONE);
				holder.tv_to_name.setVisibility(View.GONE);

			} else {
				holder.tv_name.setText(photoData.getCust_name());
				holder.tv_span.setVisibility(View.VISIBLE);
				holder.tv_to_name.setVisibility(View.VISIBLE);
				holder.tv_to_name.setText(photoData.getReply());
			}
			// 间隔时间
			int spacingData = GetSystem.spacingNowTime(photoData
					.getCreate_time());
			holder.tv_time.setText(GetSystem.showData(spacingData,
					photoData.getCreate_time()));
			// 读取用户对应的图片
			if (new File(Constant.userIconPath + GetSystem.getM5DEndo(photoData.getIcon()
					+ ".png")).exists()) {
				Bitmap image = BitmapFactory.decodeFile(Constant.userIconPath
						+ GetSystem.getM5DEndo(photoData.getIcon()) + ".png");
				holder.iv_logo.setImageBitmap(image);
			} else {
				holder.iv_logo.setImageResource(R.drawable.icon_add);
			}
			//回复点击监听
			holder.iv_comments.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					replyPopWin((View) (v.getParent().getParent().getParent()),
							position, false);
				}
			});
			return convertView;
		}

		private class ViewHolder {
			TextView tv_name;
			TextView tv_time;
			TextView tv_content;
			TextView tv_span;
			TextView tv_to_name;
			ImageView iv_comments;
			CircleImageView iv_logo;
		}
	}

	private class PhotoData {
		private String cust_id;
		private String cust_name;
		private String icon;
		private String content;
		private String create_time;
		private String reply;

		public String getCust_id() {
			return cust_id;
		}

		public void setCust_id(String cust_id) {
			this.cust_id = cust_id;
		}

		public String getCust_name() {
			return cust_name;
		}

		public void setCust_name(String cust_name) {
			this.cust_name = cust_name;
		}

		public String getIcon() {
			return icon;
		}

		public void setIcon(String icon) {
			this.icon = icon;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}

		public String getCreate_time() {
			return create_time;
		}

		public void setCreate_time(String create_time) {
			this.create_time = create_time;
		}

		public String getReply() {
			return reply;
		}

		public void setReply(String reply) {
			this.reply = reply;
		}

		@Override
		public String toString() {
			return "PhotoData [cust_id=" + cust_id + ", cust_name=" + cust_name
					+ ", icon=" + icon + ", content=" + content
					+ ", create_time=" + create_time + "]";
		}
	}

	String imageName = "";
	String cust_id = "";
	String cust_name = "";
	String imageUrl;
	private void jsonPhoto(String result) {
		try {
			JSONObject jsonObject = new JSONObject(result);
			// 获取用户id
			cust_id = jsonObject.getString("cust_id");
			final String icon = jsonObject.getString("icon");
			// 读取用户对应的图片
			if (new File(Constant.userIconPath + GetSystem.getM5DEndo(icon) + ".png").exists()) {
				Bitmap image = BitmapFactory.decodeFile(Constant.userIconPath
						+ GetSystem.getM5DEndo(icon) + ".png");
				iv_persion_icon.setImageBitmap(image);
			} else {
				if (!icon.equals("")) {
					// 获取用户头像
					mQueue.add(new ImageRequest(icon,
							new Response.Listener<Bitmap>() {
								@Override
								public void onResponse(Bitmap response) {
									GetSystem.saveImageSD(response,
											Constant.userIconPath, GetSystem.getM5DEndo(icon)
													+ ".png", 100);
									iv_persion_icon.setImageBitmap(response);
								}
							}, 0, 0, Config.RGB_565, null));
				}
			}

			// 车辆品牌
			final int car_brand_id = jsonObject.getInt("car_brand_id");
			// 读取对应的图片
			if (new File(Constant.VehicleLogoPath + car_brand_id + ".png")
					.exists()) {
				Bitmap image = BitmapFactory
						.decodeFile(Constant.VehicleLogoPath + car_brand_id
								+ ".png");
				iv_car_logo.setImageBitmap(image);
			} else {
				String brand_logo_url = jsonObject.getString("brand_logo_url");
				mQueue.add(new ImageRequest(brand_logo_url,
						new Response.Listener<Bitmap>() {
							@Override
							public void onResponse(Bitmap response) {
								GetSystem.saveImageSD(response,
										Constant.VehicleLogoPath, car_brand_id
												+ ".png", 100);
								iv_car_logo.setImageBitmap(response);
							}
						}, 0, 0, Config.RGB_565, null));
			}
			tv_adress.setText(jsonObject.getString("city"));
			cust_name = jsonObject.getString("cust_name");
			tv_persion.setText(cust_name);
			tv_content.setText(jsonObject.getString("content"));
			tv_see.setText(jsonObject.getString("saw_count"));
			int sex = jsonObject.getInt("sex");
			if (sex == 0) {
				iv_sex.setImageResource(R.drawable.icon_man);
			} else {
				iv_sex.setImageResource(R.drawable.icon_woman);
			}
			//TODO 读取大图片
			imageUrl = jsonObject.getString("big_pic_url");
			int lastSlashIndex = imageUrl.lastIndexOf("/");
			imageName = imageUrl.substring(lastSlashIndex + 1);
			if (new File(getImagePath(imageUrl)).exists()) {
				Bitmap image = BitmapFactory.decodeFile(Constant.VehiclePath + imageName);
				iv_pic.setImageBitmap(image);
				image_path = imageUrl;
			} else {
				new Thread(new Runnable() {
					@Override
					public void run() {						
						try {
							GetObjectTask task = new GetObjectTask(
									Constant.oss_path, imageName,
									Constant.oss_accessId, Constant.oss_accessKey);
							OSSObject obj = task.getResult();
							File imageFile = null;
							imageFile = new File(getImagePath(imageUrl));
							FileOutputStream fileOutputStream = new FileOutputStream(
									imageFile);
							fileOutputStream.write(obj.getData());
							fileOutputStream.close();
							Message message = new Message();
							message.what = getBigImage;
							handler.sendMessage(message);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).start();
			}

			JSONArray jsonArray = jsonObject.getJSONArray("comments");
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject2 = jsonArray.getJSONObject(i);
				PhotoData photoData = new PhotoData();
				photoData.setCust_id(jsonObject2.getString("cust_id"));
				photoData.setCust_name(jsonObject2.getString("cust_name"));
				photoData.setIcon(jsonObject2.getString("icon"));
				photoData.setContent(jsonObject2.getString("content"));
				// 实际时间
				String data = GetSystem.ChangeTimeZone(jsonObject2
						.getString("create_time").replace("T", " ")
						.substring(0, 19));
				photoData.setCreate_time(data);
				photoData.setReply(jsonObject2.getString("reply"));
				photoDatas.add(photoData);
			}
			photoAdapter.notifyDataSetChanged();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**跳转到图片显示界面**/
	private void turnImageDetails(){
		Intent intent = new Intent(PhotoActivity.this, ImageDetailsActivity.class);
		intent.putExtra("image_path", getImagePath(image_path));
		startActivity(intent);
	}
	String image_path = "";

	/** 读取图片位置 **/
	private String getImagePath(String imageUrl) {
		int lastSlashIndex = imageUrl.lastIndexOf("/");
		String imageName = imageUrl.substring(lastSlashIndex + 1);
		String imageDir = Constant.VehiclePath;
		File file = new File(imageDir);
		if (!file.exists()) {
			file.mkdirs();
		}
		String imagePath = imageDir + imageName;
		return imagePath;
	}

	// 获取头像地址和名称
	private void getLogo() {
		SharedPreferences preferences = getSharedPreferences(
				Constant.sharedPreferencesName, Context.MODE_PRIVATE);
		String customer = preferences.getString(Constant.sp_customer
				+ app.cust_id, "");
		try {
			JSONObject jsonObject = new JSONObject(customer);
			logo = jsonObject.getString("logo");
			app.cust_name = jsonObject.getString("cust_name");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	int widthPixels;

	/** 计算设置图片的宽高 **/
	private void setImageWidthHeight(Bitmap bitmap) {
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		widthPixels = metrics.widthPixels;

		double ratio = bitmap.getWidth() / (widthPixels * 1.0);
		int scaledHeight = (int) (bitmap.getHeight() / ratio);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				widthPixels, scaledHeight);
		iv_pic.setLayoutParams(params);
	}

	PopupWindow mPopupWindow;

	private void showMenu() {
		LayoutInflater mLayoutInflater = LayoutInflater
				.from(PhotoActivity.this);
		View popunwindwow = mLayoutInflater.inflate(
				R.layout.item_menu_vertical, null);
		TextView tv_letter = (TextView) popunwindwow
				.findViewById(R.id.tv_letter);
		tv_letter.setOnClickListener(onClickListener);
		TextView tv_Comments = (TextView) popunwindwow
				.findViewById(R.id.tv_Comments);
		tv_Comments.setOnClickListener(onClickListener);
		mPopupWindow = new PopupWindow(popunwindwow, LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		mPopupWindow.setAnimationStyle(R.style.PopupAnimation);
		mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		mPopupWindow.setFocusable(true);
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.showAsDropDown(findViewById(R.id.iv_more), 0, 0);
	}
}