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

import xlist.XListView;
import xlist.XListView.IXListViewListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
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
import android.text.SpannableString;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aliyun.android.oss.model.OSSObject;
import com.aliyun.android.oss.task.GetObjectTask;
import com.aliyun.android.oss.task.PutObjectTask;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Constant;
import com.wise.baba.biz.GetSystem;
import com.wise.baba.biz.MyLruCache;
import com.wise.baba.net.NetThread;
import com.wise.baba.ui.widget.CircleImageView;
import com.wise.baba.ui.widget.TimTextView;
import com.wise.baba.ui.widget.WaitLinearLayout.OnFinishListener;
import com.wise.baba.util.Blur;
import com.wise.baba.util.FaceConversionUtil;
import com.wise.baba.util.Uri2Path;
import com.wise.show.ImageDetailsActivity;


/**
 * 私信 1：布局优化，解决图片上传阴影问题 2：语音优化，语言的时间越长显示越长 3：图片加上发送中状态 4：发送失败提示，可以从发。
 * 
 * @author honesty
 * 
 */
@SuppressLint("NewApi")
public class LetterActivity extends Activity implements IXListViewListener {
	private static final String TAG = "LetterActivity";

	private static final int FriendText = 0;
	private static final int FriendImage = 2;
	private static final int FriendSound = 4;
	private static final int FriendFile = 6;
	private static final int FriendMap = 8;
	private static final int MeText = 1;
	private static final int MeImage = 3;
	private static final int MeSound = 5;
	private static final int MeFile = 7;
	private static final int MeMap = 9;

	private static final int send_letter = 1;
	private static final int get_data = 2;
	private static final int refresh_data = 3;
	private static final int get_friend_info = 4;
	/** 上传文件到阿里云 **/
	private static final int putOssImage = 5;
	private static final int getPersionImage = 6;
	private static final int getOssSound = 7;

	TextView tv_friend, btn_rcd, tv_send;
	XListView lv_letter;
	List<LetterData> letterDatas = new ArrayList<LetterData>();
	LetterAdapter letterAdapter;
	EditText et_content;
	ImageView ivPopUp, volume, ivNowPlay;
	RelativeLayout btn_bottom, ll_facechoose;
	LinearLayout voice_rcd_hint_loading, voice_rcd_hint_rcding, voice_rcd_hint_tooshort, ll_menu;
	View rcChat_popup;
	ImageView img1, sc_img1;
	ImageView iv_expand, iv_emj;
	LinearLayout del_re;
	RequestQueue mQueue;
	Bitmap imageFriend = null;
	Bitmap imageMe = null;
	String friend_id;
	String cust_name;
	String logo;
	MyBroadCastReceiver myBroadCastReceiver;
	private final MediaPlayer mMediaPlayer = new MediaPlayer();
	// 复制内容
	String letterCopy;
	boolean btn_vocie = false;
	boolean isShosrt = false;
	long startVoiceT, endVoiceT;
	String voiceName;
	SoundMeter mSensor;
	int flag = 1;
	/** 当前播放谁的语言，播放完毕后需要使用它来改变不同的图片 **/
	type noSoundPlay;

	enum type {
		friend, me
	};

	AppApplication app;
	ProgressDialog myDialog = null;
	int mapWidth = 300;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_letter);
		app = (AppApplication) getApplication();
		mQueue = Volley.newRequestQueue(this);
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;
		mapWidth = (int) (width * 0.6);
		ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		iv_emj = (ImageView) findViewById(R.id.iv_emj);
		iv_emj.setOnClickListener(onClickListener);
		ll_facechoose = (RelativeLayout) findViewById(R.id.ll_facechoose);
		iv_expand = (ImageView) findViewById(R.id.iv_expand);
		iv_expand.setOnClickListener(onClickListener);
		ll_menu = (LinearLayout) findViewById(R.id.ll_menu);
		ImageView iv_gallery = (ImageView) findViewById(R.id.iv_gallery);
		iv_gallery.setOnClickListener(onClickListener);
		ImageView iv_camera = (ImageView) findViewById(R.id.iv_camera);
		iv_camera.setOnClickListener(onClickListener);
		ImageView iv_location = (ImageView) findViewById(R.id.iv_location);
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
				if (ll_menu.getVisibility() == View.VISIBLE || ll_facechoose.getVisibility() == View.VISIBLE) {
					ll_menu.setVisibility(View.GONE);
					ll_facechoose.setVisibility(View.GONE);
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
		if (logo != null) {
			if (new File(Constant.userIconPath + GetSystem.getM5DEndo(logo) + ".png").exists()) {
				imageFriend = BitmapFactory.decodeFile(Constant.userIconPath + GetSystem.getM5DEndo(logo) + ".png");
			}
		}
		getLogo();// 判断是否有需要从网上读取的图片
		getFristData();
		myBroadCastReceiver = new MyBroadCastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Constant.A_ReceiverLetter);
		intentFilter.addAction(Constant.A_City);
		registerReceiver(myBroadCastReceiver, intentFilter);
		getFriendInfo();
		btn_rcd.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// 按下语音录制按钮时返回false执行父类OnTouch
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
				intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(Constant.VehiclePath + Constant.TemporaryImage)));
				startActivityForResult(intent, 1);
				break;
			case R.id.iv_gallery:
				ll_menu.setVisibility(View.GONE);
				Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(i, 2);
				break;
			case R.id.iv_location:
				ll_menu.setVisibility(View.GONE);
				startActivityForResult(new Intent(LetterActivity.this, LetterSendMapActivity.class), 3);
				break;
			case R.id.et_content:
				if (ll_menu.getVisibility() == View.VISIBLE) {
					ll_menu.setVisibility(View.GONE);
				}
				break;
			case R.id.iv_expand:
				ll_menu.setVisibility(View.VISIBLE);
				ll_facechoose.setVisibility(View.GONE);
				break;
			case R.id.iv_emj:
				ll_facechoose.setVisibility(View.VISIBLE);
				ll_menu.setVisibility(View.GONE);
				break;
			case R.id.tv_send:
				String content = et_content.getText().toString().trim();
				if (content.equals("")) {
					Toast.makeText(LetterActivity.this, "发送内容不能为空", Toast.LENGTH_SHORT).show();
					return;
				}
				send(content, "", "0", 0.0, 0.0);
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
			case R.id.letter_copy:
				LetterActivity.copyContent(letterCopy, LetterActivity.this);
				Toast.makeText(LetterActivity.this, "复制成功", Toast.LENGTH_SHORT).show();
				if (popupWindow.isShowing()) {
					popupWindow.dismiss();
				}
				break;
			}
		}
	};
	TextWatcher textWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if (s.length() != 0) {
				iv_expand.setVisibility(View.INVISIBLE);
				tv_send.setVisibility(View.VISIBLE);
			} else {
				iv_expand.setVisibility(View.VISIBLE);
				tv_send.setVisibility(View.INVISIBLE);
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {

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
		ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
		cmb.setText(content.trim());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(myBroadCastReceiver);
		if (imageFriend != null) {
			imageFriend.recycle();
			imageFriend = null;
		}
		if (imageMe != null) {
			imageMe.recycle();
			imageMe = null;
		}
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
		if (cust_name == null || cust_name.equals("") || logo == null || logo.equals("")) {
			String url = Constant.BaseUrl + "customer/" + friend_id + "?auth_code=" + app.auth_code;
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
			case putOssImage:
				letterAdapter.notifyDataSetChanged();
				break;
			case getPersionImage:
				removeThreadMark(msg.arg1);
				letterAdapter.notifyDataSetChanged();
				break;
			case getOssSound:
				String sound_url = Constant.oss_url + soundName;
				send("0", sound_url, "2", 0.0, 0.0);
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
			System.out.println("lDatas.size() = " + letterDatas.size());
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
	 *            类型,0:文本 1:图片 2:语音, 3:文件 4:位置
	 */
	private void send(String content, String oss_url, String type, double lat, double lon) {
		String url = Constant.BaseUrl + "customer/" + app.cust_id + "/send_chat?auth_code=" + app.auth_code;
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("cust_name", app.cust_name));
		pairs.add(new BasicNameValuePair("friend_id", friend_id));
		pairs.add(new BasicNameValuePair("type", type));
		pairs.add(new BasicNameValuePair("url", oss_url));
		pairs.add(new BasicNameValuePair("content", content));
		pairs.add(new BasicNameValuePair("voice_len", String.valueOf(voice_len)));
		pairs.add(new BasicNameValuePair("lat", String.valueOf(lat)));
		pairs.add(new BasicNameValuePair("lon", String.valueOf(lon)));
		pairs.add(new BasicNameValuePair("address", ""));
		new NetThread.postDataThread(handler, url, pairs, send_letter).start();
		et_content.setText("");
		// 添加显示
		LetterData letterData = new LetterData();
		letterData.setContent(content);
		letterData.setChatType(revisionType(false, Integer.valueOf(type) * 2 + 1));
		letterData.setUrl(oss_url);
		letterData.setSend_time(GetSystem.GetNowTime());
		letterData.setVoice_len(voice_len);
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
		String url = Constant.BaseUrl + "customer/" + app.cust_id + "/get_chats?auth_code=" + app.auth_code + "&friend_id=" + friend_id;
		new NetThread.GetDataThread(handler, url, get_data).start();
	}

	private List<LetterData> jsonData(String result) {
		List<LetterData> lDatas = new ArrayList<LetterData>();
		try {
			JSONArray jsonArray = new JSONArray(result);
			for (int i = (jsonArray.length() - 1); i >= 0; i--) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				LetterData letterData = new LetterData();
				if (jsonObject.opt("voice_len") == null) {
					letterData.setVoice_len(0);
				} else {
					letterData.setVoice_len(jsonObject.getInt("voice_len"));
				}
				if (jsonObject.opt("lat") == null) {
					letterData.setLat(0);
				} else {
					letterData.setLat(jsonObject.getDouble("lat"));
				}
				if (jsonObject.opt("lon") == null) {
					letterData.setLon(0);
				} else {
					letterData.setLon(jsonObject.getDouble("lon"));
				}
				if (jsonObject.opt("address") == null) {
					letterData.setAdress("");
				} else {
					letterData.setAdress(jsonObject.getString("address"));
				}
				letterData.setChat_id(jsonObject.getInt("chat_id"));
				letterData.setContent(jsonObject.getString("content"));
				String sender_id = jsonObject.getString("sender_id");
				String send_time = GetSystem.ChangeTimeZone(jsonObject.getString("send_time").substring(0, 19).replace("T", " "));
				letterData.setSend_time(send_time);
				int type = jsonObject.getInt("type");
				if (sender_id.equals(friend_id)) {// 好友,//0文本，1图片，2音乐
					letterData.setChatType(revisionType(true, type * 2));
				} else {
					letterData.setChatType(revisionType(false, type * 2 + 1));
				}
				letterData.setUrl(jsonObject.getString("url"));
				letterData.setSendIn(false);
				lDatas.add(letterData);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return lDatas;
	}

	/** 获取头像 **/
	private void getLogo() {
		try {
			// 获取好友头像
			if (imageFriend == null) {
				if (logo == null || logo.equals("")) {

				} else {
					// 获取用户头像
					mQueue.add(new ImageRequest(logo, new Response.Listener<Bitmap>() {
						@Override
						public void onResponse(Bitmap response) {
							GetSystem.saveImageSD(response, Constant.userIconPath, friend_id + ".png", 100);
							imageFriend = response;
							letterAdapter.notifyDataSetChanged();
						}
					}, 0, 0, Config.RGB_565, null));
				}
			}
			// 获取自己信息
			SharedPreferences preferences = getSharedPreferences(Constant.sharedPreferencesName, Context.MODE_PRIVATE);
			String customer = preferences.getString(Constant.sp_customer + app.cust_id, "");
			JSONObject jsonObject = new JSONObject(customer);
			final String meLogo = jsonObject.getString("logo");
			// 读取自己对应的图片
			if (new File(Constant.userIconPath + GetSystem.getM5DEndo(meLogo) + ".png").exists()) {
				imageMe = BitmapFactory.decodeFile(Constant.userIconPath + GetSystem.getM5DEndo(meLogo) + ".png");
			} else {
				if (!meLogo.equals("")) {
					// 获取自己头像
					mQueue.add(new ImageRequest(meLogo, new Response.Listener<Bitmap>() {
						@Override
						public void onResponse(Bitmap response) {
							GetSystem.saveImageSD(response, Constant.userIconPath, GetSystem.getM5DEndo(meLogo) + ".png", 100);
							imageMe = response;
							letterAdapter.notifyDataSetChanged();
						}
					}, 0, 0, Config.RGB_565, null));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private PopupWindow popupWindow;

	// 弹出框显示复制分享等功能
	private void initPopWindow(View v) {
		View letterView = LayoutInflater.from(LetterActivity.this).inflate(R.layout.letter_popupwidow, null);
		popupWindow = new PopupWindow(letterView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		popupWindow.setFocusable(true);
		popupWindow.setOutsideTouchable(true);
		popupWindow.showAsDropDown(v, 0, -104);

		popupWindow.getContentView().findViewById(R.id.letter_copy).setOnClickListener(onClickListener);
		popupWindow.update();
	}

	OnLongClickListener onLongClickListener = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View v) {
			initPopWindow(v);
			letterCopy = ((TextView) v).getText().toString();
			return false;
		}
	};
	
	OnClickListener friendLogoClickListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			Intent intent = new Intent(LetterActivity.this,FriendInfoActivity.class);
			intent.putExtra("FriendId", friend_id);
			startActivity(intent);
			
		}
	};

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

			ViewFriendText viewFriendText = null;
			ViewFriendImage viewFriendImage = null;
			ViewFriendSound viewFriendSound = null;
			ViewFriendFile viewFriendFile = null;
			ViewFriendMap viewFriendMap = null;
			ViewMeText viewMeText = null;
			ViewMeImage viewMeImage = null;
			ViewMeSound viewMeSound = null;
			ViewMeFile viewMeFile = null;
			ViewMeMap viewMeMap = null;

			if (convertView == null) {
				switch (Type) {
				case FriendText:
					convertView = inflater.inflate(R.layout.item_letter_friend_text, null);
					viewFriendText = new ViewFriendText();
					viewFriendText.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
					viewFriendText.iv_friend = (CircleImageView) convertView.findViewById(R.id.iv_friend);
					viewFriendText.tv_friend_content = (TextView) convertView.findViewById(R.id.tv_friend_content);
					convertView.setTag(viewFriendText);
					
					break;
				case FriendImage:
					convertView = inflater.inflate(R.layout.item_letter_friend_image, null);
					viewFriendImage = new ViewFriendImage();
					viewFriendImage.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
					viewFriendImage.iv_friend = (CircleImageView) convertView.findViewById(R.id.iv_friend);
					viewFriendImage.iv_friend_pic = (ImageView) convertView.findViewById(R.id.iv_friend_pic);
					convertView.setTag(viewFriendImage);
					break;
				case FriendSound:
					convertView = inflater.inflate(R.layout.item_letter_friend_sound, null);
					viewFriendSound = new ViewFriendSound();
					viewFriendSound.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
					viewFriendSound.iv_friend = (CircleImageView) convertView.findViewById(R.id.iv_friend);
					viewFriendSound.tv_sound_lenght = (TextView) convertView.findViewById(R.id.tv_sound_lenght);
					viewFriendSound.iv_friend_sound = (ImageView) convertView.findViewById(R.id.iv_friend_sound);
					convertView.setTag(viewFriendSound);
					break;
				case FriendFile:
					convertView = inflater.inflate(R.layout.item_letter_friend_file, null);
					viewFriendFile = new ViewFriendFile();
					viewFriendFile.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
					viewFriendFile.iv_friend = (CircleImageView) convertView.findViewById(R.id.iv_friend);
					viewFriendFile.tv_friend_content = (TextView) convertView.findViewById(R.id.tv_friend_content);
					convertView.setTag(viewFriendFile);
					break;
				case FriendMap:
					convertView = inflater.inflate(R.layout.item_letter_friend_map, null);
					viewFriendMap = new ViewFriendMap();
					viewFriendMap.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
					viewFriendMap.tv_adress = (TextView) convertView.findViewById(R.id.tv_adress);
					viewFriendMap.iv_friend = (CircleImageView) convertView.findViewById(R.id.iv_friend);
					viewFriendMap.iv_friend_map = (ImageView) convertView.findViewById(R.id.iv_friend_map);
					convertView.setTag(viewFriendMap);
					break;
				case MeText:
					convertView = inflater.inflate(R.layout.item_letter_me_text, null);
					viewMeText = new ViewMeText();
					viewMeText.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
					viewMeText.iv_me = (CircleImageView) convertView.findViewById(R.id.iv_me);
					viewMeText.tv_me_content = (TextView) convertView.findViewById(R.id.tv_me_content);
					convertView.setTag(viewMeText);
					break;
				case MeImage:
					convertView = inflater.inflate(R.layout.item_letter_me_image, null);
					viewMeImage = new ViewMeImage();
					viewMeImage.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
					viewMeImage.tv_send_in = (TimTextView) convertView.findViewById(R.id.tv_send_in);
					viewMeImage.iv_me = (CircleImageView) convertView.findViewById(R.id.iv_me);
					viewMeImage.iv_me_pic = (ImageView) convertView.findViewById(R.id.iv_me_pic);
					convertView.setTag(viewMeImage);
					break;
				case MeSound:
					convertView = inflater.inflate(R.layout.item_letter_me_sound, null);
					viewMeSound = new ViewMeSound();
					viewMeSound.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
					viewMeSound.iv_me = (CircleImageView) convertView.findViewById(R.id.iv_me);
					viewMeSound.iv_me_sound = (ImageView) convertView.findViewById(R.id.iv_me_sound);
					viewMeSound.tv_sound_lenght = (TextView) convertView.findViewById(R.id.tv_sound_lenght);
					convertView.setTag(viewMeSound);
					break;
				case MeFile:
					convertView = inflater.inflate(R.layout.item_letter_me_file, null);
					viewMeFile = new ViewMeFile();
					viewMeFile.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
					viewMeFile.iv_me = (CircleImageView) convertView.findViewById(R.id.iv_me);
					viewMeFile.tv_me_content = (TextView) convertView.findViewById(R.id.tv_me_content);
					convertView.setTag(viewMeFile);
					break;
				case MeMap:
					convertView = inflater.inflate(R.layout.item_letter_me_map, null);
					viewMeMap = new ViewMeMap();
					viewMeMap.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
					viewMeMap.iv_me = (CircleImageView) convertView.findViewById(R.id.iv_me);
					viewMeMap.iv_me_map = (ImageView) convertView.findViewById(R.id.iv_me_map);
					viewMeMap.tv_adress = (TextView) convertView.findViewById(R.id.tv_adress);
					convertView.setTag(viewMeMap);
					break;
				}
			} else {
				switch (Type) {
				case FriendText:
					viewFriendText = (ViewFriendText) convertView.getTag();
					break;
				case FriendImage:
					viewFriendImage = (ViewFriendImage) convertView.getTag();
					break;
				case FriendSound:
					viewFriendSound = (ViewFriendSound) convertView.getTag();
					break;
				case FriendFile:
					viewFriendFile = (ViewFriendFile) convertView.getTag();
					break;
				case FriendMap:
					viewFriendMap = (ViewFriendMap) convertView.getTag();
					break;
				case MeText:
					viewMeText = (ViewMeText) convertView.getTag();
					break;
				case MeImage:
					viewMeImage = (ViewMeImage) convertView.getTag();
					break;
				case MeSound:
					viewMeSound = (ViewMeSound) convertView.getTag();
					break;
				case MeFile:
					viewMeFile = (ViewMeFile) convertView.getTag();
					break;
				case MeMap:
					viewMeMap = (ViewMeMap) convertView.getTag();
					break;
				}
			}
			
			
			
			
			boolean isTimeShow = false;
			final LetterData letterData = letterDatas.get(position);
			if (position == 0) { // 第一条特殊考虑
				// 最后一条
				String now_time = letterData.getSend_time();
				// 得到间隔分钟
				int min = GetSystem.spacingNowTime(now_time);
				if (min >= 5) {
					isTimeShow = true;
				} else {
					isTimeShow = false;
				}
			} else {
				String now_time = letterData.getSend_time();
				String last_time = letterDatas.get(position - 1).getSend_time();
				// 得到间隔分钟
				int min = GetSystem.spacingTime(last_time, now_time) / 60;
				if (min >= 5) {
					isTimeShow = true;
				} else {
					isTimeShow = false;
				}
			}
			switch (Type) {
			case FriendText:
				if (isTimeShow) {
					viewFriendText.tv_time.setVisibility(View.VISIBLE);
					viewFriendText.tv_time.setText(letterData.getSend_time().substring(5, 16));
				} else {
					viewFriendText.tv_time.setVisibility(View.GONE);
				}
				if (imageFriend != null) {
					viewFriendText.iv_friend.setImageBitmap(imageFriend);
				} else {
					viewFriendText.iv_friend.setImageResource(R.drawable.icon_people_no);
				}
				viewFriendText.tv_friend_content.setText(getFaceImage(letterData.getContent()));
				viewFriendText.tv_friend_content.setOnLongClickListener(onLongClickListener);
				break;
			case FriendImage:
				if (isTimeShow) {
					viewFriendImage.tv_time.setVisibility(View.VISIBLE);
					viewFriendImage.tv_time.setText(letterData.getSend_time().substring(5, 16));
				} else {
					viewFriendImage.tv_time.setVisibility(View.GONE);
				}
				if (imageFriend != null) {
					viewFriendImage.iv_friend.setImageBitmap(imageFriend);
				} else {
					viewFriendImage.iv_friend.setImageResource(R.drawable.icon_people_no);
				}
				// 显示
				String imageUrl = letterData.getUrl();
				int lastSlashIndex = imageUrl.lastIndexOf("/");
				final String imageName = imageUrl.substring(lastSlashIndex + 1);
				Bitmap bitmap = MyLruCache.getInstance().getLruBitmap(imageName);
				if (bitmap != null) {
					viewFriendImage.iv_friend_pic.setImageBitmap(Blur.toRoundCorner(bitmap, 5));
					viewFriendImage.iv_friend_pic.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent intent = new Intent(LetterActivity.this, ImageDetailsActivity.class);
							intent.putExtra("image_path", Constant.VehiclePath + imageName);
							startActivity(intent);
						}
					});
				} else {
					if (new File(getImagePath(imageUrl)).exists()) {
						Bitmap image = BitmapFactory.decodeFile(Constant.VehiclePath + imageName);
						image = Blur.scaleImage(image, 100);
						MyLruCache.getInstance().putLruBitmap(imageName, image);
						viewFriendImage.iv_friend_pic.setImageBitmap(Blur.toRoundCorner(image, 5));
						viewFriendImage.iv_friend_pic.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								Intent intent = new Intent(LetterActivity.this, ImageDetailsActivity.class);
								intent.putExtra("image_path", Constant.VehiclePath + imageName);
								startActivity(intent);
							}
						});
					} else {
						viewFriendImage.iv_friend_pic.setImageBitmap(null);
					}
				}
				break;
			case FriendSound:
				if (isTimeShow) {
					viewFriendSound.tv_time.setVisibility(View.VISIBLE);
					viewFriendSound.tv_time.setText(letterData.getSend_time().substring(5, 16));
				} else {
					viewFriendSound.tv_time.setVisibility(View.GONE);
				}
				if (imageFriend != null) {
					viewFriendSound.iv_friend.setImageBitmap(imageFriend);
				} else {
					viewFriendSound.iv_friend.setImageResource(R.drawable.icon_people_no);
				}
				viewFriendSound.tv_sound_lenght.setText(letterData.getVoice_len() + "\"");

				viewFriendSound.iv_friend_sound.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						playMusic(getImagePath(letterData.getUrl()));
						noSoundPlay = type.friend;
						ivNowPlay = (ImageView) v;
						((ImageView) v).setImageResource(R.drawable.sound_friend);
						AnimationDrawable animationDrawable = (AnimationDrawable) ((ImageView) v).getDrawable();
						animationDrawable.start();
					}
				});
				break;
			case FriendMap:
				if (isTimeShow) {
					viewFriendMap.tv_time.setVisibility(View.VISIBLE);
					viewFriendMap.tv_time.setText(letterData.getSend_time().substring(5, 16));
				} else {
					viewFriendMap.tv_time.setVisibility(View.GONE);
				}
				if (imageFriend != null) {
					viewFriendMap.iv_friend.setImageBitmap(imageFriend);
				} else {
					viewFriendMap.iv_friend.setImageResource(R.drawable.icon_people_no);
				}
				viewFriendMap.tv_adress.setLayoutParams(new LinearLayout.LayoutParams(mapWidth, LinearLayout.LayoutParams.WRAP_CONTENT));
				viewFriendMap.tv_adress.setText(letterData.getAdress());
				String imageUrl3 = letterData.getUrl();
				int lastSlashIndex3 = imageUrl3.lastIndexOf("/");
				final String imageName3 = imageUrl3.substring(lastSlashIndex3 + 1);
				Bitmap bitmapFriend = MyLruCache.getInstance().getLruBitmap(imageName3);
				if (bitmapFriend != null) {
					viewFriendMap.iv_friend_map.setImageBitmap(Blur.toRoundCorner(bitmapFriend, 5));
					viewFriendMap.iv_friend_map.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent intent = new Intent(LetterActivity.this, LetterMapActivity.class);
							intent.putExtra("adress", letterData.getAdress());
							intent.putExtra("latitude", letterData.getLat());
							intent.putExtra("longitude", letterData.getLon());
							startActivity(intent);
						}
					});
				} else {
					if (new File(getImagePath(imageUrl3)).exists()) {
						Bitmap image = BitmapFactory.decodeFile(Constant.VehiclePath + imageName3);
						image = Blur.scaleWidthImage(image, mapWidth);
						MyLruCache.getInstance().putLruBitmap(imageName3, image);
						viewFriendMap.iv_friend_map.setImageBitmap(Blur.toRoundCorner(image, 5));
						// TODO 地址大小
						viewFriendMap.iv_friend_map.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								Intent intent = new Intent(LetterActivity.this, LetterMapActivity.class);
								intent.putExtra("adress", letterData.getAdress());
								intent.putExtra("latitude", letterData.getLat());
								intent.putExtra("longitude", letterData.getLon());
								startActivity(intent);
							}
						});
					} else {
						viewFriendMap.iv_friend_map.setImageBitmap(null);
					}
				}
				break;
			case MeText:
				if (isTimeShow) {
					viewMeText.tv_time.setVisibility(View.VISIBLE);
					viewMeText.tv_time.setText(letterData.getSend_time().substring(5, 16));
				} else {
					viewMeText.tv_time.setVisibility(View.GONE);
				}
				if (imageMe != null) {
					viewMeText.iv_me.setImageBitmap(imageMe);
				} else {
					viewMeText.iv_me.setImageResource(R.drawable.icon_people_no);
				}
				viewMeText.tv_me_content.setText(getFaceImage(letterData.getContent()));
				viewMeText.tv_me_content.setOnLongClickListener(onLongClickListener);
				break;
			case MeImage:
				if (isTimeShow) {
					viewMeImage.tv_time.setVisibility(View.VISIBLE);
					viewMeImage.tv_time.setText(letterData.getSend_time().substring(5, 16));
				} else {
					viewMeImage.tv_time.setVisibility(View.GONE);
				}
				if (imageMe != null) {
					viewMeImage.iv_me.setImageBitmap(imageMe);
				} else {
					viewMeImage.iv_me.setImageResource(R.drawable.icon_people_no);
				}
				// 显示
				String imageUrl1 = letterData.getUrl();
				int lastSlashIndex1 = imageUrl1.lastIndexOf("/");
				final String imageName1 = imageUrl1.substring(lastSlashIndex1 + 1);
				Bitmap bitmapImageMe = MyLruCache.getInstance().getLruBitmap(imageName1);
				if (bitmapImageMe != null) {
					viewMeImage.iv_me_pic.setImageBitmap(Blur.toRoundCorner(bitmapImageMe, 5));
					viewMeImage.iv_me_pic.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent intent = new Intent(LetterActivity.this, ImageDetailsActivity.class);
							intent.putExtra("image_path", Constant.VehiclePath + imageName1);
							startActivity(intent);
						}
					});
				} else {
					if (new File(getImagePath(imageUrl1)).exists()) {
						Bitmap image = BitmapFactory.decodeFile(Constant.VehiclePath + imageName1);
						image = Blur.scaleImage(image, 100);
						MyLruCache.getInstance().putLruBitmap(imageName1, image);
						viewMeImage.iv_me_pic.setImageBitmap(Blur.toRoundCorner(image, 5));
						viewMeImage.iv_me_pic.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								Intent intent = new Intent(LetterActivity.this, ImageDetailsActivity.class);
								intent.putExtra("image_path", Constant.VehiclePath + imageName1);
								startActivity(intent);
							}
						});
						if (letterData.isSendIn) {
							float Scale = Blur.calculateScale(image.getHeight(), image.getWidth(), 100);
							viewMeImage.tv_send_in.setVisibility(View.VISIBLE);
							viewMeImage.tv_send_in.setLayoutParams(new RelativeLayout.LayoutParams((int) (image.getWidth() * Scale),
									(int) (image.getHeight() * Scale)));
							viewMeImage.tv_send_in.startTim();
						} else {
							viewMeImage.tv_send_in.setVisibility(View.GONE);
							viewMeImage.tv_send_in.setStop(true);
						}

					} else {
						viewMeImage.iv_me_pic.setImageBitmap(null);
					}
				}
				break;
			case MeSound:
				if (isTimeShow) {
					viewMeSound.tv_time.setVisibility(View.VISIBLE);
					viewMeSound.tv_time.setText(letterData.getSend_time().substring(5, 16));
				} else {
					viewMeSound.tv_time.setVisibility(View.GONE);
				}
				if (imageMe != null) {
					viewMeSound.iv_me.setImageBitmap(imageMe);
				} else {
					viewMeSound.iv_me.setImageResource(R.drawable.icon_people_no);
				}
				viewMeSound.tv_sound_lenght.setText(letterData.getVoice_len() + "\"");
				// TODO viewMeSound.iv_me_sound.setLayoutParams(new
				// RelativeLayout.LayoutParams(500, LayoutParams.WRAP_CONTENT));

				viewMeSound.iv_me_sound.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						playMusic(getImagePath(letterData.getUrl()));
						noSoundPlay = type.me;
						ivNowPlay = (ImageView) v;
						((ImageView) v).setImageResource(R.drawable.sound_me);
						AnimationDrawable animationDrawable = (AnimationDrawable) ((ImageView) v).getDrawable();
						animationDrawable.start();
					}
				});
				break;
			case MeMap:
				if (isTimeShow) {
					viewMeMap.tv_time.setVisibility(View.VISIBLE);
					viewMeMap.tv_time.setText(letterData.getSend_time().substring(5, 16));
				} else {
					viewMeMap.tv_time.setVisibility(View.GONE);
				}
				if (imageMe != null) {
					viewMeMap.iv_me.setImageBitmap(imageMe);
				} else {
					viewMeMap.iv_me.setImageResource(R.drawable.icon_people_no);
				}
				// 显示
				viewMeMap.tv_adress.setLayoutParams(new LinearLayout.LayoutParams(mapWidth, LinearLayout.LayoutParams.WRAP_CONTENT));

				viewMeMap.tv_adress.setText(letterData.getAdress());
				System.out.println("地图地址 ：" + letterData.getUrl());
				String imageUrl2 = letterData.getUrl();
				int lastSlashIndex2 = imageUrl2.lastIndexOf("/");
				final String imageName2 = imageUrl2.substring(lastSlashIndex2 + 1);
				Bitmap bitmapMe = MyLruCache.getInstance().getLruBitmap(imageName2);
				if (bitmapMe != null) {// 显示图片
					viewMeMap.iv_me_map.setImageBitmap(Blur.toRoundCorner(bitmapMe, 5));
					viewMeMap.iv_me_map.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent intent = new Intent(LetterActivity.this, LetterMapActivity.class);
							intent.putExtra("adress", letterData.getAdress());
							intent.putExtra("latitude", letterData.getLat());
							intent.putExtra("longitude", letterData.getLon());
							startActivity(intent);
						}
					});
				} else {
					if (new File(getImagePath(imageUrl2)).exists()) {
						Bitmap image = BitmapFactory.decodeFile(Constant.VehiclePath + imageName2);
						image = Blur.scaleWidthImage(image, mapWidth);
						MyLruCache.getInstance().putLruBitmap(imageName2, image);
						viewMeMap.iv_me_map.setImageBitmap(Blur.toRoundCorner(image, 5));
						viewMeMap.iv_me_map.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								Intent intent = new Intent(LetterActivity.this, LetterMapActivity.class);
								intent.putExtra("adress", letterData.getAdress());
								intent.putExtra("latitude", letterData.getLat());
								intent.putExtra("longitude", letterData.getLon());
								startActivity(intent);
							}
						});
					} else {
						viewMeMap.iv_me_map.setImageBitmap(null);
					}
				}
				break;
			}
			
			View logo = convertView.findViewById(R.id.iv_friend);
			if(logo!=null){
				logo.setOnClickListener(friendLogoClickListener);
			}
			
			
			
			
			return convertView;
		}

		@Override
		public int getItemViewType(int position) {
			LetterData letterData = letterDatas.get(position);
			return letterData.getChatType();
		}

		@Override
		public int getViewTypeCount() {
			return 10;
		}

		class ViewFriendText {
			TextView tv_time;
			CircleImageView iv_friend;
			TextView tv_friend_content;
		}

		class ViewFriendImage {
			TextView tv_time;
			CircleImageView iv_friend;
			ImageView iv_friend_pic;
		}

		class ViewFriendSound {
			TextView tv_time;
			CircleImageView iv_friend;
			TextView tv_sound_lenght;
			ImageView iv_friend_sound;
		}

		class ViewFriendFile {
			TextView tv_time;
			CircleImageView iv_friend;
			TextView tv_friend_content;
		}

		class ViewFriendMap {
			TextView tv_time;
			CircleImageView iv_friend;
			TextView tv_adress;
			ImageView iv_friend_map;
		}

		class ViewMeText {
			TextView tv_time;
			CircleImageView iv_me;
			TextView tv_me_content;
		}

		class ViewMeImage {
			TextView tv_time;
			CircleImageView iv_me;
			ImageView iv_me_pic;
			TimTextView tv_send_in;
		}

		class ViewMeSound {
			TextView tv_time;
			CircleImageView iv_me;
			TextView tv_sound_lenght;
			ImageView iv_me_sound;
		}

		class ViewMeFile {
			TextView tv_time;
			CircleImageView iv_me;
			TextView tv_me_content;
		}

		class ViewMeMap {
			TextView tv_time;
			CircleImageView iv_me;
			TextView tv_adress;
			ImageView iv_me_map;
		}
	}

	public SpannableString getFaceImage(String faceContent) {
		return FaceConversionUtil.getInstace().getExpressionString(LetterActivity.this, faceContent);
	}

	class LetterData {
		int chatType;
		String content;
		int friend_id;
		String friend_name;
		String logo;
		String send_time;
		int relat_id;
		int chat_id;
		String url;
		int voice_len;
		double lat;
		double lon;
		String adress;
		boolean isSendIn;

		public String getAdress() {
			return adress;
		}

		public void setAdress(String adress) {
			this.adress = adress;
		}

		public double getLat() {
			return lat;
		}

		public void setLat(double lat) {
			this.lat = lat;
		}

		public double getLon() {
			return lon;
		}

		public void setLon(double lon) {
			this.lon = lon;
		}

		public boolean isSendIn() {
			return isSendIn;
		}

		public void setSendIn(boolean isSendIn) {
			this.isSendIn = isSendIn;
		}

		public int getVoice_len() {
			return voice_len;
		}

		public void setVoice_len(int voice_len) {
			this.voice_len = voice_len;
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

		public int getChatType() {
			return chatType;
		}

		public void setChatType(int chatType) {
			this.chatType = chatType;
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
			return "LetterData [chatType=" + chatType + ", content=" + content + ", friend_id=" + friend_id + ", friend_name=" + friend_name + ", logo=" + logo
					+ ", send_time=" + send_time + ", relat_id=" + relat_id + ", chat_id=" + chat_id + ", url=" + url + ", voice_len=" + voice_len + ", lat="
					+ lat + ", lon=" + lon + ", adress=" + adress + ", isSendIn=" + isSendIn + "]";
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
						letterData.setChatType(revisionType(true, jsonObject.getInt("msg_type") * 2));
						letterData.setSend_time(GetSystem.GetNowTime());
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
			} else if (action.equals(Constant.A_City)) {
				if (myDialog != null) {
					myDialog.dismiss();
				}
				et_content.setText(intent.getStringExtra("AddrStr"));
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
		String url = Constant.BaseUrl + "customer/" + app.cust_id + "/get_chats?auth_code=" + app.auth_code + "&friend_id=" + friend_id + "&max_id=" + Chat_id;
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
		if (resultCode == Activity.RESULT_CANCELED) {
			ll_menu.setVisibility(View.VISIBLE);
			return;
		} else if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
			// 拍照返回
			saveImageSD((Constant.VehiclePath + Constant.TemporaryImage), 1, 0.0, 0.0, "");
			return;
		} else if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
			// 图库返回
			if (data != null) {
				// 获取图片路径
				Uri uri = data.getData();
				saveImageSD(Uri2Path.getPath(LetterActivity.this, uri), 1, 0.0, 0.0, "");
			}
			return;
		} else if (requestCode == 3 && resultCode == 3) {
			// 选着位置后返回
			String adress = data.getStringExtra("adress");
			String mapPath = data.getStringExtra("mapPath");
			double latitude = data.getDoubleExtra("latitude", 0);
			double longitude = data.getDoubleExtra("longitude", 0);
			saveImageSD(mapPath, 4, latitude, longitude, adress);
			return;
		}
	}

	/**
	 * 图库和拍照的图片需要压缩处理在发送
	 * 
	 * @param path
	 */
	private void saveImageSD(final String path, final int Type, final double lat, final double lon, final String address) {
		// 设置图像的名称和地址
		final String big_pic = app.cust_id + System.currentTimeMillis() + ".png";
		final String oss_url = Constant.oss_url + big_pic;
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
		int newWidth = widthPixels > heightPixels ? heightPixels : widthPixels;

		Bitmap bitmap = Blur.decodeSampledBitmapFromPath(path, newWidth, newWidth);
		// 存大图像
		bitmap = Blur.scaleImage(bitmap, newWidth);

		FileOutputStream bigOutputStream = null;
		final String bigFile = Constant.VehiclePath + big_pic;
		try {
			bigOutputStream = new FileOutputStream(bigFile);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 60, bigOutputStream);// 把数据写入文件
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
		// 先显示
		LetterData letterData = new LetterData();
		letterData.setContent("");
		letterData.setChatType(Type * 2 + 1);
		letterData.setUrl(oss_url);
		letterData.setSend_time(GetSystem.GetNowTime());
		letterData.setVoice_len(0);
		letterData.setSendIn(true);
		letterData.setLat(lat);
		letterData.setLon(lon);

		Log.i("LetterActivity", lat+"");
		
		Log.i("LetterActivity", lon+"");
		
		letterData.setAdress(address);
		letterDatas.add(letterData);
		letterAdapter.notifyDataSetChanged();
		lv_letter.setSelection(lv_letter.getBottom());
		// 给图片命名
		// 上传图片
		// 存到服务器
		new Thread(new Runnable() {
			@Override
			public void run() {
				// 上传大图图片到阿里云
				PutObjectTask bigTask = new PutObjectTask(Constant.oss_path, big_pic, "image/jpg", bigFile, Constant.oss_accessId, Constant.oss_accessKey);
				bigTask.getResult();
				String url = Constant.BaseUrl + "customer/" + app.cust_id + "/send_chat?auth_code=" + app.auth_code;
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				pairs.add(new BasicNameValuePair("cust_name", app.cust_name));
				pairs.add(new BasicNameValuePair("friend_id", friend_id));
				pairs.add(new BasicNameValuePair("type", "" + Type));
				pairs.add(new BasicNameValuePair("url", oss_url));
				pairs.add(new BasicNameValuePair("content", ""));
				pairs.add(new BasicNameValuePair("voice_len", "0"));
				pairs.add(new BasicNameValuePair("lat", String.valueOf(lat)));
				pairs.add(new BasicNameValuePair("lon", String.valueOf(lon)));
				pairs.add(new BasicNameValuePair("address", address));
				String result = NetThread.postData(url, pairs);
				GetSystem.myLog(TAG, result);
				for (LetterData letterData : letterDatas) {
					if (letterData.getUrl().equals(oss_url)) {
						letterData.setSendIn(false);
						Message message = new Message();
						message.what = putOssImage;
						handler.sendMessage(message);
						break;
					}
				}
			}
		}).start();
	}

	String soundName = "";

	private void saveSound(String name) {
		soundName = name;
		new Thread(new Runnable() {
			@Override
			public void run() {
				PutObjectTask bigTask = new PutObjectTask(Constant.oss_path, soundName, "audio/amr", Constant.VehiclePath + soundName, Constant.oss_accessId,
						Constant.oss_accessKey);
				bigTask.getResult();
				Message message = new Message();
				message.what = getOssSound;
				handler.sendMessage(message);
			}
		}).start();
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
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		}
	};

	/** 获取显示区域的图片 **/
	private void getPersionImage() {
		int start = lv_letter.getFirstVisiblePosition();
		int stop = lv_letter.getLastVisiblePosition();
		// 循环读取图片
		for (int i = start; i < stop; i++) {
			if (i >= letterDatas.size()) {
				break;
			}
			if (letterDatas.get(i).getUrl() == null || letterDatas.get(i).getUrl().equals("")) {

			} else {
				// 判断图片是否存在
				if (new File(getImagePath(letterDatas.get(i).getUrl())).exists()) {

				} else {
					if (isThreadRun(i)) {
						// 如果图片正在读取则跳过
					} else {
						photoThreadId.add(i);
						new ImageThread(i).start();
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
	 * 
	 * @param position
	 */
	private void removeThreadMark(int position) {
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
			try {
				String imageUrl = letterDatas.get(position).getUrl();
				int lastSlashIndex = imageUrl.lastIndexOf("/");
				String imageName = imageUrl.substring(lastSlashIndex + 1);

				GetObjectTask task = new GetObjectTask(Constant.oss_path, imageName, Constant.oss_accessId, Constant.oss_accessKey);
				OSSObject obj = task.getResult();
				File imageFile = null;
				imageFile = new File(getImagePath(imageUrl));
				FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
				fileOutputStream.write(obj.getData());
				fileOutputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			Message message = new Message();
			message.what = getPersionImage;
			message.arg1 = position;
			handler.sendMessage(message);
		}
	}

	int voice_len = 0;

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
						@Override
						public void run() {
							if (!isShosrt) {
								voice_rcd_hint_loading.setVisibility(View.GONE);
								voice_rcd_hint_rcding.setVisibility(View.VISIBLE);
							}
						}
					}, 300);
					img1.setVisibility(View.VISIBLE);
					del_re.setVisibility(View.GONE);
					startVoiceT = System.currentTimeMillis();
					voiceName = app.cust_id + System.currentTimeMillis() + ".amr";
					start(voiceName);
					flag = 2;
				}
			} else if (event.getAction() == MotionEvent.ACTION_UP && flag == 2) {// 松开手势时执行录制完成
				btn_rcd.setBackgroundResource(R.drawable.bg_letter_white);
				if (event.getY() >= del_Y && event.getY() <= del_Y + del_re.getHeight() && event.getX() >= del_x && event.getX() <= del_x + del_re.getWidth()) {
					// 取消发送
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
					voice_rcd_hint_rcding.setVisibility(View.GONE);
					stop();
					endVoiceT = System.currentTimeMillis();
					flag = 1;

					int time = (int) ((endVoiceT - startVoiceT) / 1000);
					voice_len = time;
					GetSystem.myLog(TAG, "time = " + time);
					if (time < 1) {
						isShosrt = true;
						voice_rcd_hint_loading.setVisibility(View.GONE);
						voice_rcd_hint_rcding.setVisibility(View.GONE);
						voice_rcd_hint_tooshort.setVisibility(View.VISIBLE);
						handler.postDelayed(new Runnable() {
							@Override
							public void run() {
								voice_rcd_hint_tooshort.setVisibility(View.GONE);
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
				Animation mLitteAnimation = AnimationUtils.loadAnimation(this, R.anim.cancel_rc);
				Animation mBigAnimation = AnimationUtils.loadAnimation(this, R.anim.cancel_rc2);
				img1.setVisibility(View.GONE);
				del_re.setVisibility(View.VISIBLE);
				del_re.setBackgroundResource(R.drawable.voice_rcd_cancel_bg);
				if (event.getY() >= del_Y && event.getY() <= del_Y + del_re.getHeight() && event.getX() >= del_x && event.getX() <= del_x + del_re.getWidth()) {
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

	private final Runnable mSleepTask = new Runnable() {
		@Override
		public void run() {
			stop();
		}
	};
	private final Runnable mPollTask = new Runnable() {
		@Override
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
				if (noSoundPlay == type.me) {
					ivNowPlay.setImageResource(R.drawable.sound_me_2);
				} else {
					ivNowPlay.setImageResource(R.drawable.sound_friend_2);
				}
			}
			mMediaPlayer.reset();
			mMediaPlayer.setDataSource(name);
			mMediaPlayer.prepare();
			mMediaPlayer.start();
			mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					AnimationDrawable animationDrawable = (AnimationDrawable) ivNowPlay.getDrawable();
					animationDrawable.stop();
					if (noSoundPlay == type.me) {
						ivNowPlay.setImageResource(R.drawable.sound_me_2);
					} else {
						ivNowPlay.setImageResource(R.drawable.sound_friend_2);
					}
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void back() {
		if (ll_menu.getVisibility() == View.VISIBLE || ll_facechoose.getVisibility() == View.VISIBLE) {
			ll_menu.setVisibility(View.GONE);
			ll_facechoose.setVisibility(View.GONE);
		} else {
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

	/** 防止超过0-9之间的数据 **/
	private int revisionType(boolean isFriend, int Type) {
		if (Type >= 0 && Type <= 9) {
			return Type;
		} else {
			if (isFriend) {
				return 0;
			} else {
				return 1;
			}
		}
	}
}
