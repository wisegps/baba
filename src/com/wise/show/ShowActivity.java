package com.wise.show;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import pubclas.Constant;
import pubclas.Judge;
import pubclas.NetThread;
import pubclas.Variable;
import com.wise.baba.R;
import com.wise.setting.LoginActivity;
import com.wise.show.MyScrollView.OnFlowClickListener;
import com.wise.show.RefreshableView.RefreshListener;
import customView.PopView;
import customView.PopView.OnItemClickListener;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**车秀大厅**/
public class ShowActivity extends Activity{
	private static final String TAG = "ShowActivity";
	
	private static final int getFristImage = 1;
	private static final int getNextImage = 2;
	private static final int praise = 3;
	private static final int getRefreshImage = 4;

	List<ImageData> imageDatas = new ArrayList<ImageData>();
	TextView tv_time;
	MyScrollView my_scroll_view;
	RefreshableView ll_refresh;
	/**个人头像路径**/
	String logo = "";
	int page_count = 20;
	/**是否正在加载图片**/
	boolean isLoading = false;
	boolean is_beauty = false;
	String beauty = "&if_beauty=true";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_show);
		ImageView iv_back = (ImageView)findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		ImageView iv_beauty = (ImageView)findViewById(R.id.iv_beauty);
		iv_beauty.setOnClickListener(onClickListener);
		ImageView iv_show_car = (ImageView)findViewById(R.id.iv_show_car);
		iv_show_car.setOnClickListener(onClickListener);
		ll_refresh = (RefreshableView)findViewById(R.id.ll_refresh);
		ll_refresh.setRefreshListener(refreshListener);
		my_scroll_view = (MyScrollView)findViewById(R.id.my_scroll_view);
		my_scroll_view.setOnFlowClickListener(onFlowClickListener);
		tv_time = (TextView)findViewById(R.id.tv_time);
		Button bt_show_car = (Button)findViewById(R.id.bt_show_car);
		bt_show_car.setOnClickListener(onClickListener);
		getFristImages();
		getLogo();
	}
	OnClickListener onClickListener = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			case R.id.iv_show_car:
				picPop();
				break;

			case R.id.iv_beauty:
				is_beauty = true;
				getFristImages();
				break;
			}
		}
	};
	String refresh = "";
	RefreshListener refreshListener = new RefreshListener() {		
		@Override
		public void onRefresh() {
			//下拉刷新
			refresh = "";
			int Photo_id = imageDatas.get(0).getPhoto_id();
			String url = Constant.BaseUrl + "photo?auth_code=" + Variable.auth_code + "&cust_id=" + 
						Variable.cust_id + "&max_id=" + Photo_id + getBeauty();
			new NetThread.GetDataThread(handler, url, getRefreshImage).start();
		}
		@Override
		public void onRefreshOver() {
			System.out.println("刷新完毕");
			List<ImageData> iDatas = jsonImages(refresh);
			imageDatas.addAll(0,iDatas);
			my_scroll_view.addHeadImages(iDatas);
		}
	};
	
	OnFlowClickListener onFlowClickListener = new OnFlowClickListener() {		
		@Override
		public void OnPraise(int position) {//点赞
			if(Judge.isLogin()){
				ImageData imageData = imageDatas.get(position);
				if(!imageData.isCust_praise()){
					int Photo_id = imageData.getPhoto_id();
					String url = Constant.BaseUrl + "photo/" + Photo_id + "/praise?auth_code=" + Variable.auth_code;
					List<NameValuePair> pairs = new ArrayList<NameValuePair>();
					pairs.add(new BasicNameValuePair("cust_id", Variable.cust_id));
					pairs.add(new BasicNameValuePair("cust_name", Variable.cust_name));
					pairs.add(new BasicNameValuePair("icon", logo));
					new NetThread.putDataThread(handler, url, pairs, praise,position).start();
				}
			}else{
				//没有登录则跳转到登录
				startActivityForResult(new Intent(ShowActivity.this, LoginActivity.class),1);
			}			
		}		
		@Override
		public void OnClick(int position) {//点击图片
			if(Judge.isLogin()){
				Intent intent = new Intent(ShowActivity.this, PhotoActivity.class);
				intent.putExtra("imageData", imageDatas.get(position));
				intent.putExtra("position", position);
				startActivityForResult(intent, 1);
			}else{
				//没有登录则跳转到登录
				startActivityForResult(new Intent(ShowActivity.this, LoginActivity.class),1);
			}			
		}
		@Override
		public void OnLoad() {//图片加载完毕
			//获取更多图片链接
			//判断还有图片,通过返回数目计算，如果是 page_count 的整数倍则读取数据，否则说明服务器数据读取完毕
			if(isLoading){
				return;
			}
			if(imageDatas != null && imageDatas.size() != 0){
				int i = imageDatas.size() % page_count; //取余
				if(i == 0){
					isLoading = true;
					int Photo_id = imageDatas.get(imageDatas.size() - 1).getPhoto_id();
					String url = Constant.BaseUrl + "photo?auth_code=" + Variable.auth_code + "&cust_id=" + 
								Variable.cust_id + "&min_id=" + Photo_id + getBeauty();
					new NetThread.GetDataThread(handler, url, getNextImage).start();
				}
			}
		}
		@Override
		public void OnScrollPosition(String Time) {
			//TODO 滑动显示时间
			tv_time.setVisibility(View.VISIBLE);
			tv_time.setText(Time.substring(0, 16).replace("T", " "));
			handler.removeCallbacks(hideTime);
		}
		@Override
		public void OnScrollFinish() {
			handler.postDelayed(hideTime, 1500);
		}
	};
	
	Runnable hideTime = new Runnable() {		
		@Override
		public void run() {
			Animation operatingAnim = AnimationUtils.loadAnimation(
                    ShowActivity.this, R.anim.car_show_time_gone);
			tv_time.setAnimation(operatingAnim);
			tv_time.setVisibility(View.GONE);
		}
	};
	
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case getFristImage:
				List<ImageData> iDatas = jsonImages(msg.obj.toString());
				imageDatas.addAll(iDatas);
				my_scroll_view.resetImages(iDatas);
				break;
			case getNextImage:
				isLoading = false;
				List<ImageData> Datas = jsonImages(msg.obj.toString());
				imageDatas.addAll(Datas);
				System.out.println("imageDatas.size() = " + imageDatas.size());
				my_scroll_view.addFootImages(Datas);
				break;
			case praise:
				jsonPraise(msg.obj.toString(),msg.arg1);
				break;
			case getRefreshImage:
				refresh = msg.obj.toString();
				ll_refresh.runFast();
				break;
			}
		}		
	};
	/**判断点赞**/
	private void jsonPraise(String result,int position){
		System.out.println(result);
		try {
			JSONObject jsonObject = new JSONObject(result);
			if(jsonObject.getInt("status_code") == 0){
				//点赞成功
				//考虑在连续点2次的情况
				if(!imageDatas.get(position).isCust_praise()){
					imageDatas.get(position).setCust_praise(true);
					//修改图片点赞状态
					my_scroll_view.setPraise(position);
					//点赞次数+1;
					int Praise_count = imageDatas.get(position).getPraise_count() + 1;
					imageDatas.get(position).setPraise_count(Praise_count);
					my_scroll_view.setPraiseCount(position, Praise_count);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**获取图片列表**/
	private void getFristImages(){
		imageDatas.clear();
		String url = Constant.BaseUrl + "photo?auth_code=" + Variable.auth_code + "&cust_id=" + Variable.cust_id + getBeauty();
		new NetThread.GetDataThread(handler, url, getFristImage).start();
	}
	/**获取**/
	private List<ImageData> jsonImages(String result){
		List<ImageData> Datas = new ArrayList<ImageData>();
		try {
			JSONArray jsonArray = new JSONArray(result);
			for(int i = 0 ; i < jsonArray.length() ; i++){
				ImageData imageData = new ImageData();
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				imageData.setCust_praise(jsonObject.getBoolean("cust_praise"));
				imageData.setCar_series(jsonObject.getString("car_series"));
				imageData.setCreate_time(jsonObject.getString("create_time"));
				imageData.setPhoto_id(jsonObject.getInt("photo_id"));
				imageData.setPraise_count(jsonObject.getInt("praise_count"));
				imageData.setSmall_pic_url(jsonObject.getString("small_pic_url"));
				Datas.add(imageData);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Datas;
	}
	private void getLogo(){
		SharedPreferences preferences = getSharedPreferences(
				Constant.sharedPreferencesName, Context.MODE_PRIVATE);
		String customer = preferences.getString(Constant.sp_customer + Variable.cust_id, "");
		try {
			JSONObject jsonObject = new JSONObject(customer);
			if(jsonObject.opt("status_code") == null){
				logo = jsonObject.getString("logo");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private void picPop(){
		List<String> items = new ArrayList<String>();
		items.add("拍照");
		items.add("从手机相册中选取");
		final PopView popView = new PopView(this);
		popView.initView(findViewById(R.id.ll_bottom));
		popView.setData(items);
		popView.SetOnItemClickListener(new OnItemClickListener() {			
			@Override
			public void OnItemClick(int index) {
				switch (index) {
				case 0:
					File file = new File(Constant.VehiclePath);
	                if (!file.exists()) {
	                    file.mkdirs();// 创建文件夹
	                }
	                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(Constant.VehiclePath + Constant.TemporaryImage)));
	                startActivityForResult(intent, 1);
					popView.dismiss();
					break;

				case 1:
					Intent intent1 = new Intent(); 
	                /* 开启Pictures画面Type设定为image */ 
	                intent1.setType("image/*"); 
	                /* 使用Intent.ACTION_GET_CONTENT这个Action */ 
	                intent1.setAction(Intent.ACTION_GET_CONTENT);  
	                /* 取得相片后返回本画面 */ 
	                startActivityForResult(intent1, 9);
	                popView.dismiss();
					break;
				}
			}
		});
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 9){
			Uri uri = data.getData();
            Intent intent = new Intent(ShowActivity.this, ShowCarAcitivity.class);
			intent.putExtra("image", getPath(uri));
	        startActivity(intent);
	        return;
		}
		if(resultCode == 1){
			//TODO 登录返回,刷新数据
			imageDatas.clear();
			getFristImages();
			getLogo();
			return;
		}
		if(resultCode == Activity.RESULT_OK){
			Intent intent = new Intent(ShowActivity.this, ShowCarAcitivity.class);
			intent.putExtra("image", Constant.VehiclePath + Constant.TemporaryImage);
	        startActivity(intent);
	        return;
		}
		if(resultCode == 1){
			//TODO 相片详细界面点赞返回
			int position = data.getIntExtra("position", 0);
			int Praise_count = data.getIntExtra("Praise_count", 0);
			imageDatas.get(position).setCust_praise(data.getBooleanExtra("isCust_praise", true));
			imageDatas.get(position).setPraise_count(Praise_count);			
			//修改图片点赞状态
			my_scroll_view.setPraise(position);
			my_scroll_view.setPraiseCount(position, Praise_count);
		}
	}
	/**把uri 转换成 SD卡路径**/
	public String getPath(Uri uri){    
       String[] projection = {MediaStore.Images.Media.DATA };    
       Cursor cursor = managedQuery(uri, projection, null, null, null);    
       int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);    
       cursor.moveToFirst();    
       return cursor.getString(column_index);    
    }
	/**如果是车宝贝返回**/
	private String getBeauty(){
		if(is_beauty){
			return beauty;
		}else{
			return "";
		}
	}
}