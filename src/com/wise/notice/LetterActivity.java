package com.wise.notice;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pubclas.Blur;
import pubclas.Constant;
import pubclas.GetSystem;
import pubclas.NetThread;
import pubclas.Variable;
import xlist.XListView;
import xlist.XListView.IXListViewListener;
import com.aliyun.android.oss.model.OSSObject;
import com.aliyun.android.oss.task.GetObjectTask;
import com.aliyun.android.oss.task.PutObjectTask;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.wise.baba.R;
import com.wise.show.ImageDetailsActivity;
import customView.CircleImageView;
import customView.WaitLinearLayout.OnFinishListener;
import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;

/** 私信 **/
@SuppressLint("NewApi")
public class LetterActivity extends Activity implements IXListViewListener {
	private static final String TAG = "LetterActivity";

	private static final int send_letter = 1;
	private static final int get_data = 2;
	private static final int refresh_data = 3;
	private static final int get_friend_info = 4;
	/** 上传文件到阿里云 **/
	private static final int putOss = 5;
	private static final int getPersionImage = 6;
	private static final int getOssSound = 7;
	
	TextView tv_friend, btn_rcd,tv_send;
	XListView lv_letter;
	List<LetterData> letterDatas = new ArrayList<LetterData>();
	LetterAdapter letterAdapter;
	EditText et_content;
	ImageView ivPopUp, volume,ivNowPlay;
	RelativeLayout btn_bottom;
	LinearLayout voice_rcd_hint_loading, voice_rcd_hint_rcding,
			voice_rcd_hint_tooshort,ll_menu;
	View rcChat_popup;
	ImageView img1, sc_img1,iv_expand;
	LinearLayout del_re;
	RequestQueue mQueue;
	Bitmap imageFriend = null;
	Bitmap imageMe = null;
	String friend_id;
	String cust_name;
	String logo;
	MyBroadCastReceiver myBroadCastReceiver;
	private MediaPlayer mMediaPlayer = new MediaPlayer();
	// 复制内容
	String letterCopy;
	boolean btn_vocie = false;
	boolean isShosrt = false;
	long startVoiceT, endVoiceT;
	String voiceName;
	SoundMeter mSensor;
	int flag = 1;
	/**文字，语音，图片**/
	int type_text = 0;
	int type_pic = 1;
	int type_sound = 2;
	enum type {friend,me};
	type noSoundPlay;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_letter);	
		mQueue = Volley.newRequestQueue(this);
		ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		iv_expand = (ImageView)findViewById(R.id.iv_expand);
		iv_expand.setOnClickListener(onClickListener);
		ll_menu = (LinearLayout)findViewById(R.id.ll_menu);
		ImageView iv_gallery = (ImageView)findViewById(R.id.iv_gallery);
		iv_gallery.setOnClickListener(onClickListener);
		ImageView iv_camera = (ImageView)findViewById(R.id.iv_camera);
		iv_camera.setOnClickListener(onClickListener);
		ImageView iv_location = (ImageView)findViewById(R.id.iv_location);
		iv_location.setOnClickListener(onClickListener);
		volume = (ImageView) findViewById(R.id.volume);
		ivPopUp = (ImageView) findViewById(R.id.ivPopUp);
		ivPopUp.setOnClickListener(onClickListener);
		btn_rcd = (TextView) findViewById(R.id.btn_rcd);
		btn_bottom = (RelativeLayout) findViewById(R.id.btn_bottom);
		del_re = (LinearLayout) findViewById(R.id.del_re);
		voice_rcd_hint_rcding = (LinearLayout) findViewById(R.id.voice_rcd_hint_rcding);
		voice_rcd_hint_loading = (LinearLayout) findViewById(R.id.voice_rcd_hint_loading);
		voice_rcd_hint_tooshort = (LinearLayout) findViewById(R.id.voice_rcd_hint_tooshort);
		img1 = (ImageView) findViewById(R.id.img1);
		sc_img1 = (ImageView) findViewById(R.id.sc_img1);
		rcChat_popup = findViewById(R.id.rcChat_popup);
		mSensor = new SoundMeter();
		lv_letter = (XListView) findViewById(R.id.lv_letter);
		lv_letter.setPullLoadEnable(false);
		lv_letter.setPullRefreshEnable(true);
		lv_letter.setXListViewListener(this);
		letterAdapter = new LetterAdapter();
		lv_letter.setAdapter(letterAdapter);
		lv_letter.setOnFinishListener(onFinishListener);
		lv_letter.setOnScrollListener(onScrollListener);
		lv_letter.setOnTouchListener(new OnTouchListener() {			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(ll_menu.getVisibility() == View.VISIBLE){
					ll_menu.setVisibility(View.GONE);
				}
				return false;
			}
		});
		et_content = (EditText) findViewById(R.id.et_content);
		et_content.addTextChangedListener(textWatcher);
		et_content.setOnClickListener(onClickListener);
		tv_friend = (TextView) findViewById(R.id.tv_friend);
		tv_send = (TextView) findViewById(R.id.tv_send);
		tv_send.setOnClickListener(onClickListener);
		friend_id = getIntent().getStringExtra("cust_id");
		cust_name = getIntent().getStringExtra("cust_name");
		logo = getIntent().getStringExtra("logo");
		tv_friend.setText(cust_name);
		// 读取朋友对应的图片
		if (new File(Constant.userIconPath + friend_id + ".png").exists()) {
			imageFriend = BitmapFactory.decodeFile(Constant.userIconPath
					+ friend_id + ".png");
		}
		// 读取自己对应的图片
		if (new File(Constant.userIconPath + Variable.cust_id + ".png")
				.exists()) {
			imageMe = BitmapFactory.decodeFile(Constant.userIconPath
					+ Variable.cust_id + ".png");
		}
		getFristData();
		getLogo();// 判断是否有需要从网上读取的图片
		myBroadCastReceiver = new MyBroadCastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Constant.A_ReceiverLetter);
		registerReceiver(myBroadCastReceiver, intentFilter);
		getFriendInfo();
		btn_rcd.setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				//按下语音录制按钮时返回false执行父类OnTouch
				return false;
			}
		});
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_camera:
				ll_menu.setVisibility(View.GONE);
				File file = new File(Constant.VehiclePath);
				if (!file.exists()) {
					file.mkdirs();// 创建文件夹
				}
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra(
						MediaStore.EXTRA_OUTPUT,
						Uri.fromFile(new File(Constant.VehiclePath
								+ Constant.TemporaryImage)));
				startActivityForResult(intent, 1);
				break;
			case R.id.iv_gallery:
				ll_menu.setVisibility(View.GONE);
				Intent i = new Intent(
						Intent.ACTION_PICK,
						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(i, 2);
				break;
			case R.id.iv_location:
				ll_menu.setVisibility(View.GONE);
				break;
			case R.id.et_content:
				if(ll_menu.getVisibility() == View.VISIBLE){
					ll_menu.setVisibility(View.GONE);
				}
				break;
			case R.id.iv_expand:
				ll_menu.setVisibility(View.VISIBLE);
				break;
			case R.id.tv_send:
				String content = et_content.getText().toString().trim();
				if (content.equals("")) {
					Toast.makeText(LetterActivity.this, "发送内容不能为空",
							Toast.LENGTH_SHORT).show();
					return;
				}
				send(content, "", "0");
				break;
			case R.id.iv_back:
				back();
				break;
			case R.id.ivPopUp:
				if (btn_vocie) {
					btn_rcd.setVisibility(View.GONE);
					btn_bottom.setVisibility(View.VISIBLE);
					btn_vocie = false;
					ivPopUp.setImageResource(R.drawable.chatto_voice_friend);

				} else {
					btn_rcd.setVisibility(View.VISIBLE);
					btn_bottom.setVisibility(View.GONE);
					ivPopUp.setImageResource(R.drawable.icon_qiehuan_shuru);
					btn_vocie = true;
				}
				break;
			}
		}
	};
	TextWatcher textWatcher = new TextWatcher() {		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if(s.length() != 0){
				iv_expand.setVisibility(View.INVISIBLE);
				tv_send.setVisibility(View.VISIBLE);
			}else{
				iv_expand.setVisibility(View.VISIBLE);
				tv_send.setVisibility(View.INVISIBLE);
			}
		}		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {}		
		@Override
		public void afterTextChanged(Editable s) {
			System.out.println();
		}
	};

	// 复制到剪切板
	/**
	 * 实现文本复制功能 add by wangqianzhou
	 * 
	 * @param content
	 */
	@SuppressLint("NewApi")
	public static void copyContent(String content, Context context) {
		// 得到剪贴板管理器
		ClipboardManager cmb = (ClipboardManager) context
				.getSystemService(Context.CLIPBOARD_SERVICE);
		cmb.setText(content.trim());
	}

	/**
	 * 实现粘贴功能 add by wangqianzhou
	 * 
	 * @param context
	 * @return
	 */
	public static String pasteContent(Context context) {
		// 得到剪贴板管理器
		ClipboardManager cmb = (ClipboardManager) context
				.getSystemService(Context.CLIPBOARD_SERVICE);
		return cmb.getText().toString().trim();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(myBroadCastReceiver);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	/** 如果没有好友信息则获取 **/
	private void getFriendInfo() {
		if (cust_name == null || cust_name.equals("") || logo == null
				|| logo.equals("")) {
			String url = Constant.BaseUrl + "customer/" + friend_id
					+ "?auth_code=" + Variable.auth_code;
			new NetThread.GetDataThread(handler, url, get_friend_info).start();
		}
	}

	/** 解析好友信息 **/
	private void jsonFriendInfo(String result) {
		try {
			JSONObject jsonObject = new JSONObject(result);
			cust_name = jsonObject.getString("cust_name");
			tv_friend.setText(cust_name);
			getLogo();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	String refresh = "";
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case send_letter:
				jsonSendLetter(msg.obj.toString());
				break;

			case get_data:
				List<LetterData> lDatas = jsonData(msg.obj.toString());
				letterDatas.addAll(lDatas);
				letterAdapter.notifyDataSetChanged();
				lv_letter.setSelection(lv_letter.getBottom());
				// 读取图片
				handler.postDelayed(new Runnable() {					
					@Override
					public void run() {
						getPersionImage();
					}
				}, 100);
				break;
			case refresh_data:
				refresh = msg.obj.toString();
				lv_letter.runFast(1);
				break;
			case get_friend_info:
				jsonFriendInfo(msg.obj.toString());
				break;
			case putOss:
				/** 在阿里云上对应的图片url **/
				String big_pic_url = Constant.oss_url + big_pic;
				//上传
				send("0", big_pic_url, "1");
				break;
			case getPersionImage:
				letterAdapter.notifyDataSetChanged();
				break;
			case getOssSound:
				String sound_url = Constant.oss_url + soundName;
				send("0", sound_url, "2");
				break;
			}
		}
	};
	OnFinishListener onFinishListener = new OnFinishListener() {
		@Override
		public void OnFinish(int index) {
			List<LetterData> lDatas = jsonData(refresh);
			letterDatas.addAll(0, lDatas);
			letterAdapter.notifyDataSetChanged();
			lv_letter.setSelection(lDatas.size());
			onLoadOver();
		}
	};

	/**
	 * @param content
	 *            内容
	 * @param url
	 *            url,
	 * @param type
	 *            类型,0:文本 1:图片 2:语音
	 */
	private void send(String content, String oss_url, String type) {
		GetSystem.myLog(TAG, "content = " + content + " , oss_url = " + oss_url
				+ " , type = " + type);
		String url = Constant.BaseUrl + "customer/" + Variable.cust_id
				+ "/send_chat?auth_code=" + Variable.auth_code;
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("cust_name", Variable.cust_name));
		pairs.add(new BasicNameValuePair("friend_id", friend_id));
		pairs.add(new BasicNameValuePair("type", type));
		pairs.add(new BasicNameValuePair("url", oss_url));
		pairs.add(new BasicNameValuePair("content", content));
		new NetThread.postDataThread(handler, url, pairs, send_letter).start();
		et_content.setText("");
		// 添加显示
		LetterData letterData = new LetterData();
		letterData.setContent(content);
		letterData.setType(1);
		letterData.setContent_type(Integer.valueOf(type));
		letterData.setUrl(oss_url);
		letterData.setSend_time(GetSystem.GetNowTime());
		letterDatas.add(letterData);
		letterAdapter.notifyDataSetChanged();
		lv_letter.setSelection(lv_letter.getBottom());
	}

	/** 判断发送状态 **/
	private void jsonSendLetter(String result) {
		Log.d(TAG, "result = " + result);
		try {
			JSONObject jsonObject = new JSONObject(result);
			if (jsonObject.getInt("status_code") == 0) {

			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void getFristData() {
		String url = Constant.BaseUrl + "customer/" + Variable.cust_id
				+ "/get_chats?auth_code=" + Variable.auth_code + "&friend_id="
				+ friend_id;
		new NetThread.GetDataThread(handler, url, get_data).start();
	}

	private List<LetterData> jsonData(String result) {
		List<LetterData> lDatas = new ArrayList<LetterData>();
		try {
			JSONArray jsonArray = new JSONArray(result);
			for (int i = (jsonArray.length() - 1); i >= 0; i--) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				LetterData letterData = new LetterData();
				letterData.setChat_id(jsonObject.getInt("chat_id"));
				letterData.setContent(jsonObject.getString("content"));
				String sender_id = jsonObject.getString("sender_id");
				String send_time = GetSystem.ChangeTimeZone(jsonObject
						.getString("send_time").substring(0, 19)
						.replace("T", " "));
				letterData.setSend_time(send_time);
				if (sender_id.equals(friend_id)) {// 好友
					letterData.setType(0);
				} else {
					letterData.setType(1);
				}
				letterData.setContent_type(jsonObject.getInt("type"));
				letterData.setUrl(jsonObject.getString("url"));
				lDatas.add(letterData);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return lDatas;
	}

	/** 获取头像 **/
	private void getLogo() {
		if (imageFriend == null) {
			if (logo == null || logo.equals("")) {

			} else {
				// 获取用户头像
				mQueue.add(new ImageRequest(logo,
						new Response.Listener<Bitmap>() {
							@Override
							public void onResponse(Bitmap response) {
								GetSystem.saveImageSD(response,
										Constant.userIconPath, friend_id
												+ ".png", 100);
								imageFriend = response;
								letterAdapter.notifyDataSetChanged();
							}
						}, 0, 0, Config.RGB_565, null));
			}
		}
		if (imageMe == null) {
			String meLogo = "";
			SharedPreferences preferences = getSharedPreferences(
					Constant.sharedPreferencesName, Context.MODE_PRIVATE);
			String customer = preferences.getString(Constant.sp_customer
					+ Variable.cust_id, "");
			try {
				JSONObject jsonObject = new JSONObject(customer);
				meLogo = jsonObject.getString("logo");
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (!meLogo.equals("")) {
				// 获取自己头像
				mQueue.add(new ImageRequest(meLogo,
						new Response.Listener<Bitmap>() {
							@Override
							public void onResponse(Bitmap response) {
								GetSystem.saveImageSD(response,
										Constant.userIconPath, Variable.cust_id
												+ ".png", 100);
								imageMe = response;
								letterAdapter.notifyDataSetChanged();
							}
						}, 0, 0, Config.RGB_565, null));
			}
		}
	}
	
	class LetterAdapter extends BaseAdapter {
		LayoutInflater inflater = LayoutInflater.from(LetterActivity.this);

		@Override
		public int getCount() {
			return letterDatas.size();
		}

		@Override
		public Object getItem(int position) {
			return letterDatas.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			int Type = getItemViewType(position);
			ViewFriend viewFriend = null;
			ViewMe viewMe = null;

			if (convertView == null) {
				if (Type == 0) {// 朋友
					convertView = inflater.inflate(R.layout.item_letter_friend,
							null);
					viewFriend = new ViewFriend();
					viewFriend.iv_friend = (CircleImageView) convertView
							.findViewById(R.id.iv_friend);
					viewFriend.tv_friend_content = (TextView) convertView
							.findViewById(R.id.tv_friend_content);
					viewFriend.tv_time = (TextView) convertView
							.findViewById(R.id.tv_time);
					viewFriend.iv_friend_pic = (ImageView) convertView
							.findViewById(R.id.iv_friend_pic);
					viewFriend.iv_friend_sound = (ImageView) convertView
							.findViewById(R.id.iv_friend_sound);
					convertView.setTag(viewFriend);
				} else {
					convertView = inflater.inflate(R.layout.item_letter_me,
							null);
					viewMe = new ViewMe();
					viewMe.iv_me = (CircleImageView) convertView
							.findViewById(R.id.iv_me);
					viewMe.tv_me_content = (TextView) convertView
							.findViewById(R.id.tv_me_content);
					viewMe.tv_time = (TextView) convertView
							.findViewById(R.id.tv_time);
					viewMe.tv_sound_lenght = (TextView) convertView
							.findViewById(R.id.tv_sound_lenght);
					viewMe.iv_me_pic = (ImageView) convertView
							.findViewById(R.id.iv_me_pic);
					viewMe.iv_me_sound = (ImageView) convertView
							.findViewById(R.id.iv_me_sound);
					convertView.setTag(viewMe);
				}
			} else {
				if (Type == 0) {
					viewFriend = (ViewFriend) convertView.getTag();
				} else {
					viewMe = (ViewMe) convertView.getTag();
				}
			}
			final LetterData letterData = letterDatas.get(position);
			if ((position + 1) >= letterDatas.size()) {
				// 最后一条
				String last_time = letterData.getSend_time();
				// 得到间隔分钟
				int min = GetSystem.spacingNowTime(last_time);
				if (min >= 5) {
					if (Type == 0) {// 朋友
						viewFriend.tv_time.setVisibility(View.VISIBLE);
						viewFriend.tv_time.setText(last_time.substring(5, 16));
					} else {
						viewMe.tv_time.setVisibility(View.VISIBLE);
						viewMe.tv_time.setText(last_time.substring(5, 16));
					}
				} else {
					if (Type == 0) {// 朋友
						viewFriend.tv_time.setVisibility(View.GONE);
					} else {
						viewMe.tv_time.setVisibility(View.GONE);
					}
				}
			} else {
				String last_time = letterData.getSend_time();
				String next_time = letterDatas.get(position + 1).getSend_time();
				// 得到间隔分钟
				int min = GetSystem.spacingTime(last_time, next_time) / 60;
				if (min >= 5) {
					if (Type == 0) {// 朋友
						viewFriend.tv_time.setVisibility(View.VISIBLE);
						viewFriend.tv_time.setText(last_time.substring(5, 16));
					} else {
						viewMe.tv_time.setVisibility(View.VISIBLE);
						viewMe.tv_time.setText(last_time.substring(5, 16));
					}
				} else {
					if (Type == 0) {// 朋友
						viewFriend.tv_time.setVisibility(View.GONE);
					} else {
						viewMe.tv_time.setVisibility(View.GONE);
					}
				}
			}
			if (Type == 0) {// 朋友
				// 长按监听，弹出（复制，分享等）功能
				viewFriend.tv_friend_content
						.setOnLongClickListener(new OnLongClickListener() {
							@Override
							public boolean onLongClick(View v) {
								initPopWindow(v);
								letterCopy = ((TextView) v).getText()
										.toString();
								return true;
							}
						});
				// 读取朋友对应的图片
				if (imageFriend != null) {
					viewFriend.iv_friend.setImageBitmap(imageFriend);
				} else {
					viewFriend.iv_friend
							.setImageResource(R.drawable.icon_people_no);
				}
				if (letterData.getContent_type() == type_text) {
					viewFriend.iv_friend_pic.setVisibility(View.GONE);
					viewFriend.iv_friend_sound.setVisibility(View.GONE);
					viewFriend.tv_friend_content.setVisibility(View.VISIBLE);
					viewFriend.tv_friend_content
					.setCompoundDrawablesWithIntrinsicBounds(0, 0,0, 0);
					viewFriend.tv_friend_content.setText(letterData
							.getContent());
				} else if (letterData.getContent_type() == type_pic) {
					viewFriend.iv_friend_pic.setVisibility(View.VISIBLE);
					viewFriend.tv_friend_content.setVisibility(View.GONE);
					viewFriend.iv_friend_sound.setVisibility(View.GONE);
					//显示
					String imageUrl = letterData.getUrl();
					int lastSlashIndex = imageUrl.lastIndexOf("/");
					final String imageName = imageUrl.substring(lastSlashIndex + 1);
					if (new File(getImagePath(imageUrl)).exists()) {
						Bitmap image = BitmapFactory
								.decodeFile(Constant.VehiclePath + imageName);
						image = Blur.scaleImage(image, 100);
						
						viewFriend.iv_friend_pic.setImageBitmap(Blur.toRoundCorner(image, 5));
						viewFriend.iv_friend_pic.setOnClickListener(new OnClickListener() {							
							@Override
							public void onClick(View v) {
								Intent intent = new Intent(LetterActivity.this, ImageDetailsActivity.class);
								intent.putExtra("image_path", Constant.VehiclePath + imageName);
								startActivity(intent);
							}
						});
					}else{
						viewFriend.iv_friend_pic.setImageBitmap(null);
					}
				} else if (letterData.getContent_type() == type_sound) {
					viewFriend.iv_friend_pic.setVisibility(View.GONE);
					viewFriend.tv_friend_content.setVisibility(View.GONE);
					viewFriend.iv_friend_sound.setVisibility(View.VISIBLE);
					viewFriend.tv_friend_content.setText("");
					//TODO iv_friend_sound
					viewFriend.iv_friend_sound.setOnClickListener(new OnClickListener() {						
						@Override
						public void onClick(View v) {
							playMusic(getImagePath(letterData.getUrl())) ;
							noSoundPlay = type.friend;
							ivNowPlay = (ImageView)v;
							((ImageView)v).setImageResource(R.drawable.sound_friend);  
							AnimationDrawable animationDrawable = (AnimationDrawable) ((ImageView)v).getDrawable();  
			                animationDrawable.start();  
						}
					});
				}
			} else {
				viewMe.tv_me_content.setText(letterData.getContent());
				// 长按监听，弹出（复制，分享等）功能
				viewMe.tv_me_content
						.setOnLongClickListener(new OnLongClickListener() {
							@Override
							public boolean onLongClick(View v) {
								initPopWindow(v);
								letterCopy = ((TextView) v).getText()
										.toString();
								return true;
							}
						});

				// 读取自己对应的图片
				if (imageMe != null) {
					viewMe.iv_me.setImageBitmap(imageMe);
				} else {
					viewMe.iv_me.setImageResource(R.drawable.icon_people_no);
				}
				if (letterData.getContent_type() == type_text) {
					viewMe.tv_sound_lenght.setVisibility(View.GONE);
					viewMe.iv_me_sound.setVisibility(View.GONE);
					viewMe.iv_me_pic.setVisibility(View.GONE);
					viewMe.tv_me_content.setVisibility(View.VISIBLE);
					viewMe.tv_me_content.setText(letterData.getContent());
					viewMe.tv_me_content
					.setCompoundDrawablesWithIntrinsicBounds(0, 0,0, 0);
				} else if (letterData.getContent_type() == type_pic) {
					viewMe.iv_me_pic.setVisibility(View.VISIBLE);
					viewMe.tv_me_content.setVisibility(View.GONE);
					viewMe.tv_sound_lenght.setVisibility(View.GONE);
					viewMe.iv_me_sound.setVisibility(View.GONE);
					//显示
					String imageUrl = letterData.getUrl();
					int lastSlashIndex = imageUrl.lastIndexOf("/");
					final String imageName = imageUrl.substring(lastSlashIndex + 1);
					if (new File(getImagePath(imageUrl)).exists()) {
						Bitmap image = BitmapFactory.decodeFile(Constant.VehiclePath + imageName);
						image = Blur.scaleImage(image, 100);
						viewMe.iv_me_pic.setImageBitmap(Blur.toRoundCorner(image, 5));
						viewMe.iv_me_pic.setOnClickListener(new OnClickListener() {							
							@Override
							public void onClick(View v) {
								Intent intent = new Intent(LetterActivity.this, ImageDetailsActivity.class);
								intent.putExtra("image_path", Constant.VehiclePath + imageName);
								startActivity(intent);
							}
						});
					}else{
						viewMe.iv_me_pic.setImageBitmap(null);
					}
				}else if (letterData.getContent_type() == type_sound) {
					viewMe.iv_me_pic.setVisibility(View.GONE);
					viewMe.tv_me_content.setVisibility(View.GONE);
					viewMe.iv_me_sound.setVisibility(View.VISIBLE);
					viewMe.tv_me_content.setText("");
					viewMe.iv_me_sound.setOnClickListener(new OnClickListener() {						
						@Override
						public void onClick(View v) {
							playMusic(getImagePath(letterData.getUrl())) ;
							noSoundPlay = type.me;
							ivNowPlay = (ImageView)v;
							((ImageView)v).setImageResource(R.drawable.sound_me);  
							AnimationDrawable animationDrawable = (AnimationDrawable) ((ImageView)v).getDrawable();  
			                animationDrawable.start();  
						}
					});
					try {
						//long time = GetSystem.getAmrDuration(new File(getImagePath(letterData.getUrl())));
						//viewMe.tv_sound_lenght.setText(""+abdddd(getImagePath(letterData.getUrl())));
						viewMe.tv_sound_lenght.setVisibility(View.GONE);
						//TODO 刷新
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			return convertView;
		}

		private PopupWindow popupWindow;

		// 弹出框显示复制分享等功能
		private void initPopWindow(View v) {
			View letterView = inflater
					.inflate(R.layout.letter_popupwidow, null);
			popupWindow = new PopupWindow(letterView,
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			popupWindow.setBackgroundDrawable(new BitmapDrawable());
			popupWindow.setFocusable(true);
			popupWindow.setOutsideTouchable(true);
			popupWindow.showAsDropDown(v, 0, -104);

			popupWindow.getContentView().findViewById(R.id.letter_copy)
					.setOnClickListener(click);
			popupWindow.getContentView().findViewById(R.id.letter_collection)
					.setOnClickListener(click);
			popupWindow.getContentView().findViewById(R.id.letter_share)
					.setOnClickListener(click);

			popupWindow.update();
		}

		OnClickListener click = new OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.letter_copy:// 复制
					LetterActivity.copyContent(letterCopy, LetterActivity.this);
					Toast.makeText(LetterActivity.this, "复制成功",
							Toast.LENGTH_SHORT).show();
					break;
				case R.id.letter_collection:// 收藏

					break;
				case R.id.letter_share:// 分享

					break;
				}
				if (popupWindow.isShowing()) {
					popupWindow.dismiss();
				}
			}
		};

		@Override
		public int getItemViewType(int position) {
			LetterData letterData = letterDatas.get(position);
			return letterData.getType();
		}

		@Override
		public int getViewTypeCount() {
			return 2;
		}

		class ViewFriend {
			CircleImageView iv_friend;
			TextView tv_friend_content;
			TextView tv_time;
			ImageView iv_friend_pic;
			ImageView iv_friend_sound;
		}

		class ViewMe {
			CircleImageView iv_me;
			TextView tv_me_content;
			TextView tv_time;
			TextView tv_sound_lenght;
			ImageView iv_me_pic;
			ImageView iv_me_sound;
		}
	}

	class LetterData {
		int type;
		String content;
		int friend_id;
		String friend_name;
		String logo;
		String send_time;
		int relat_id;
		int chat_id;
		String url;
		int content_type;

		public int getContent_type() {
			return content_type;
		}

		public void setContent_type(int content_type) {
			this.content_type = content_type;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getSend_time() {
			return send_time;
		}

		public void setSend_time(String send_time) {
			this.send_time = send_time;
		}

		public int getChat_id() {
			return chat_id;
		}

		public void setChat_id(int chat_id) {
			this.chat_id = chat_id;
		}

		public int getType() {
			return type;
		}

		public void setType(int type) {
			this.type = type;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}

		public int getFriend_id() {
			return friend_id;
		}

		public void setFriend_id(int friend_id) {
			this.friend_id = friend_id;
		}

		public String getFriend_name() {
			return friend_name;
		}

		public void setFriend_name(String friend_name) {
			this.friend_name = friend_name;
		}

		public String getLogo() {
			return logo;
		}

		public void setLogo(String logo) {
			this.logo = logo;
		}

		public int getRelat_id() {
			return relat_id;
		}

		public void setRelat_id(int relat_id) {
			this.relat_id = relat_id;
		}

		@Override
		public String toString() {
			return "LetterData [type=" + type + ", content=" + content
					+ ", friend_id=" + friend_id + ", friend_name="
					+ friend_name + ", logo=" + logo + ", send_time="
					+ send_time + ", relat_id=" + relat_id + ", chat_id="
					+ chat_id + ", url=" + url + "]";
		}
	}

	class MyBroadCastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Constant.A_ReceiverLetter)) {
				String extras = intent.getStringExtra("extras");
				try {
					JSONObject jsonObject = new JSONObject(extras);
					if (friend_id.equals(jsonObject.getString("friend_id"))) {
						String content = jsonObject.getString("msg");
						// 如果是当前朋友发来的私信则显示
						LetterData letterData = new LetterData();
						letterData.setContent(content);
						letterData.setType(0);
						letterData.setSend_time(GetSystem.GetNowTime());
						letterData.setContent_type(jsonObject.getInt("msg_type"));
						letterData.setUrl(jsonObject.getString("url"));
						letterDatas.add(letterData);						
						letterAdapter.notifyDataSetChanged();
						lv_letter.setSelection(lv_letter.getBottom());
						handler.postDelayed(new Runnable() {					
							@Override
							public void run() {
								getPersionImage();
							}
						}, 100);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void onLoadOver() {
		lv_letter.refreshHeaderView();
		lv_letter.refreshBottomView();
		lv_letter.stopRefresh();
		lv_letter.stopLoadMore();
		lv_letter.setRefreshTime(GetSystem.GetNowTime());
	}

	@Override
	public void onRefresh() {
		refresh = "";
		int Chat_id = letterDatas.get(0).getChat_id();
		String url = Constant.BaseUrl + "customer/" + Variable.cust_id
				+ "/get_chats?auth_code=" + Variable.auth_code + "&friend_id="
				+ friend_id + "&max_id=" + Chat_id;
		new NetThread.GetDataThread(handler, url, refresh_data).start();
		lv_letter.startHeaderWheel();
	}

	@Override
	public void onLoadMore() {
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		System.out.println("requestCode = " + requestCode + " , resultCode = " + resultCode);
		if(resultCode == Activity.RESULT_CANCELED){
			ll_menu.setVisibility(View.VISIBLE);
			return;
		}else if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
			// 拍照返回
			saveImage(Constant.VehiclePath + Constant.TemporaryImage);
			return;
		}else if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
			// 图库返回
			if (data != null) {
				// 获取图片路径
				Uri uri = data.getData();
				saveImage(getPath(uri));
			}
			return;
		}
	}

	String big_pic = "";

	private void saveImage(final String path) {
		// 给图片命名
		// 上传图片
		// 存到服务器
		new Thread(new Runnable() {
			@Override
			public void run() {
				// 判断文件夹是否为空
				File filePath = new File(Constant.VehiclePath);
				if (!filePath.exists()) {
					filePath.mkdirs();
				}

				// 获取手机分辨率,选出最小的
				DisplayMetrics metrics = new DisplayMetrics();
				getWindowManager().getDefaultDisplay().getMetrics(metrics);
				int widthPixels = metrics.widthPixels;
				int heightPixels = metrics.heightPixels;
				int newWidth = widthPixels > heightPixels ? heightPixels
						: widthPixels;
				// 设置大图形和小图像的名称
				big_pic = Variable.cust_id + System.currentTimeMillis()
						+ ".png";
				Bitmap bitmap = Blur.decodeSampledBitmapFromPath(path,
						newWidth, newWidth);
				//存大图像
				bitmap = Blur.scaleImage(bitmap, newWidth);

				FileOutputStream bigOutputStream = null;
				final String bigFile = Constant.VehiclePath + big_pic;
				try {
					bigOutputStream = new FileOutputStream(bigFile);
					bitmap.compress(Bitmap.CompressFormat.JPEG, 90,
							bigOutputStream);// 把数据写入文件
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} finally {
					try {
						bigOutputStream.flush();
						bigOutputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				// 上传大图图片到阿里云
				PutObjectTask bigTask = new PutObjectTask(Constant.oss_path,
						big_pic, "image/jpg", bigFile, Constant.oss_accessId,
						Constant.oss_accessKey);
				bigTask.getResult();

				Message message = new Message();
				message.what = putOss;
				handler.sendMessage(message);
			}
		}).start();
	}

	String soundName = "";

	private void saveSound(String name) {
		soundName = name;
		new Thread(new Runnable() {
			@Override
			public void run() {
				PutObjectTask bigTask = new PutObjectTask(Constant.oss_path,
						soundName, "audio/amr", Constant.VehiclePath
								+ soundName, Constant.oss_accessId,
						Constant.oss_accessKey);
				bigTask.getResult();
				Message message = new Message();
				message.what = getOssSound;
				handler.sendMessage(message);
			}
		}).start();
	}

	/** 把uri 转换成 SD卡路径 **/
	public String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}
	public int abdddd(String Path){
		System.out.println("Path = " + Path);
		int durationIndex = 0;
		String[] projection = { MediaStore.Audio.Media.DATA};
		ContentResolver mResolver = getContentResolver();
		Cursor cursor = mResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI , null, MediaStore.Audio.Media.DATA + "=?", new String[]{Path}, null);
		if(cursor != null){
			//TODO 获取分数
			cursor.moveToFirst();
			durationIndex = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
			System.out.println("-------------------------------------------------------");
			System.out.println("durationIndex = " + durationIndex);
			System.out.println("TITLE_KEY = " + cursor.getColumnIndex(MediaStore.Audio.Media.TITLE_KEY));
			System.out.println("DEFAULT_SORT_ORDER = " + cursor.getColumnIndex(MediaStore.Audio.Media.DEFAULT_SORT_ORDER));
			System.out.println("_COUNT = " + cursor.getColumnIndex(MediaStore.Audio.Media._COUNT));
			System.out.println("_ID = " + cursor.getColumnIndex(MediaStore.Audio.Media._ID));
			System.out.println("ALBUM = " + cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
			System.out.println("ALBUM_ID = " + cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
			System.out.println("ALBUM_KEY = " + cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_KEY));
			System.out.println("ARTIST = " + cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));			
			System.out.println("ARTIST_ID = " + cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID));
			System.out.println("ARTIST_KEY = " + cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_KEY));
			System.out.println("BOOKMARK = " + cursor.getColumnIndex(MediaStore.Audio.Media.BOOKMARK));
			System.out.println("COMPOSER = " + cursor.getColumnIndex(MediaStore.Audio.Media.COMPOSER));
			System.out.println("DATA = " + cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
			System.out.println("DATE_ADDED = " + cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED));
			System.out.println("DATE_MODIFIED = " + cursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED));
			System.out.println("IS_MUSIC = " + cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
			System.out.println("SIZE = " + cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
			System.out.println("TITLE = " + cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
		}else{
			System.out.println("cursor 为空");
		}
		cursor.close();
		return durationIndex;
	}

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
		int start = lv_letter.getFirstVisiblePosition();
		int stop = lv_letter.getLastVisiblePosition();
		for (int i = start; i < stop; i++) {
			if (start == 0 || stop == (letterDatas.size() - 1)) {

			} else {
				// 判断图片是否存在
				if (new File(getImagePath(letterDatas.get(i - 1).getUrl()))
						.exists()) {

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

	class ImageThread extends Thread {
		int position;

		public ImageThread(int position) {
			this.position = position;
		}

		@Override
		public void run() {
			super.run();
			try {
				String imageUrl = letterDatas.get(position).getUrl();
				int lastSlashIndex = imageUrl.lastIndexOf("/");
				String imageName = imageUrl.substring(lastSlashIndex + 1);

				GetObjectTask task = new GetObjectTask(Constant.oss_path,
						imageName, Constant.oss_accessId,
						Constant.oss_accessKey);
				OSSObject obj = task.getResult();
				File imageFile = null;
				imageFile = new File(getImagePath(imageUrl));
				FileOutputStream fileOutputStream = new FileOutputStream(
						imageFile);
				fileOutputStream.write(obj.getData());
				fileOutputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			for (int i = 0; i < photoThreadId.size(); i++) {
				if (photoThreadId.get(i) == position) {
					photoThreadId.remove(i);
					break;
				}
			}
			Message message = new Message();
			message.what = getPersionImage;
			handler.sendMessage(message);
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		if (!Environment.getExternalStorageDirectory().exists()) {
			Toast.makeText(this, "No SDCard", Toast.LENGTH_LONG).show();
			return false;
		}
		if (btn_vocie) {
			int[] location = new int[2];
			btn_rcd.getLocationInWindow(location); // 获取在当前窗口内的绝对坐标
			int btn_rc_Y = location[1];
			int btn_rc_X = location[0];
			int[] del_location = new int[2];
			del_re.getLocationInWindow(del_location);
			int del_Y = del_location[1];
			int del_x = del_location[0];
			if (event.getAction() == MotionEvent.ACTION_DOWN && flag == 1) {
				if (!Environment.getExternalStorageDirectory().exists()) {
					Toast.makeText(this, "No SDCard", Toast.LENGTH_LONG).show();
					return false;
				}
				if (event.getY() > btn_rc_Y && event.getX() > btn_rc_X) {// 判断手势按下的位置是否是语音录制按钮的范围内
					btn_rcd.setBackgroundResource(R.drawable.bg_letter_white_press);
					rcChat_popup.setVisibility(View.VISIBLE);
					voice_rcd_hint_loading.setVisibility(View.VISIBLE);
					voice_rcd_hint_rcding.setVisibility(View.GONE);
					voice_rcd_hint_tooshort.setVisibility(View.GONE);
					handler.postDelayed(new Runnable() {
						public void run() {
							if (!isShosrt) {
								voice_rcd_hint_loading.setVisibility(View.GONE);
								voice_rcd_hint_rcding
										.setVisibility(View.VISIBLE);
							}
						}
					}, 300);
					img1.setVisibility(View.VISIBLE);
					del_re.setVisibility(View.GONE);
					startVoiceT = System.currentTimeMillis();
					voiceName = Variable.cust_id + System.currentTimeMillis()
							+ ".amr";
					start(voiceName);
					flag = 2;
				}
			} else if (event.getAction() == MotionEvent.ACTION_UP && flag == 2) {// 松开手势时执行录制完成
				btn_rcd.setBackgroundResource(R.drawable.bg_letter_white);
				if (event.getY() >= del_Y
						&& event.getY() <= del_Y + del_re.getHeight()
						&& event.getX() >= del_x
						&& event.getX() <= del_x + del_re.getWidth()) {
					// 取消发送
					System.out.println("取消发送");
					rcChat_popup.setVisibility(View.GONE);
					img1.setVisibility(View.VISIBLE);
					del_re.setVisibility(View.GONE);
					stop();
					flag = 1;
					File file = new File(Constant.VehiclePath + voiceName);
					if (file.exists()) {
						file.delete();
					}
				} else {
					System.out.println("发送");
					voice_rcd_hint_rcding.setVisibility(View.GONE);
					stop();
					endVoiceT = System.currentTimeMillis();
					flag = 1;
					int time = (int) ((endVoiceT - startVoiceT) / 1);
					if (time < 1) {
						isShosrt = true;
						voice_rcd_hint_loading.setVisibility(View.GONE);
						voice_rcd_hint_rcding.setVisibility(View.GONE);
						voice_rcd_hint_tooshort.setVisibility(View.VISIBLE);
						handler.postDelayed(new Runnable() {
							public void run() {
								voice_rcd_hint_tooshort
										.setVisibility(View.GONE);
								rcChat_popup.setVisibility(View.GONE);
								isShosrt = false;
							}
						}, 500);
						return false;
					}
					// 录制完毕
					saveSound(voiceName);
					rcChat_popup.setVisibility(View.GONE);

				}
			}
			if (event.getY() < btn_rc_Y) {// 手势按下的位置不在语音录制按钮的范围内
				Animation mLitteAnimation = AnimationUtils.loadAnimation(this,
						R.anim.cancel_rc);
				Animation mBigAnimation = AnimationUtils.loadAnimation(this,
						R.anim.cancel_rc2);
				img1.setVisibility(View.GONE);
				del_re.setVisibility(View.VISIBLE);
				del_re.setBackgroundResource(R.drawable.voice_rcd_cancel_bg);
				if (event.getY() >= del_Y
						&& event.getY() <= del_Y + del_re.getHeight()
						&& event.getX() >= del_x
						&& event.getX() <= del_x + del_re.getWidth()) {
					del_re.setBackgroundResource(R.drawable.voice_rcd_cancel_bg_focused);
					sc_img1.startAnimation(mLitteAnimation);
					sc_img1.startAnimation(mBigAnimation);
				}
			} else {

				img1.setVisibility(View.VISIBLE);
				del_re.setVisibility(View.GONE);
				del_re.setBackgroundResource(0);
			}
		}
		return super.onTouchEvent(event);
	}

	private static final int POLL_INTERVAL = 300;

	private Runnable mSleepTask = new Runnable() {
		public void run() {
			stop();
		}
	};
	private Runnable mPollTask = new Runnable() {
		public void run() {
			double amp = mSensor.getAmplitude();
			updateDisplay(amp);
			handler.postDelayed(mPollTask, POLL_INTERVAL);

		}
	};

	private void start(String name) {
		mSensor.start(name);
		handler.postDelayed(mPollTask, POLL_INTERVAL);
	}

	private void stop() {
		handler.removeCallbacks(mSleepTask);
		handler.removeCallbacks(mPollTask);
		mSensor.stop();
		volume.setImageResource(R.drawable.amp1);
	}

	private void updateDisplay(double signalEMA) {

		switch ((int) signalEMA) {
		case 0:
		case 1:
			volume.setImageResource(R.drawable.amp1);
			break;
		case 2:
		case 3:
			volume.setImageResource(R.drawable.amp2);

			break;
		case 4:
		case 5:
			volume.setImageResource(R.drawable.amp3);
			break;
		case 6:
		case 7:
			volume.setImageResource(R.drawable.amp4);
			break;
		case 8:
		case 9:
			volume.setImageResource(R.drawable.amp5);
			break;
		case 10:
		case 11:
			volume.setImageResource(R.drawable.amp6);
			break;
		default:
			volume.setImageResource(R.drawable.amp7);
			break;
		}
	}
	private void playMusic(String name) {
		try {
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.stop();
				AnimationDrawable animationDrawable = (AnimationDrawable) ivNowPlay.getDrawable();  
                animationDrawable.stop(); 
				if(noSoundPlay == type.me){
	                ivNowPlay.setImageResource(R.drawable.sound_me_2);
				}else{
	                ivNowPlay.setImageResource(R.drawable.sound_friend_2);
				}
			}
			System.out.println("Duration = " + mMediaPlayer.getDuration());
			mMediaPlayer.reset();
			mMediaPlayer.setDataSource(name);
			mMediaPlayer.prepare();
			mMediaPlayer.start();
			mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
				public void onCompletion(MediaPlayer mp) {
					System.out.println("播放完毕");
					AnimationDrawable animationDrawable = (AnimationDrawable) ivNowPlay.getDrawable();  
	                animationDrawable.stop(); 
					if(noSoundPlay == type.me){
		                ivNowPlay.setImageResource(R.drawable.sound_me_2);
					}else{
		                ivNowPlay.setImageResource(R.drawable.sound_friend_2);
					}
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	private void back(){
		if(ll_menu.getVisibility() == View.VISIBLE){
			ll_menu.setVisibility(View.GONE);
		}else{
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.stop();
			}
			finish();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			back();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}	
}