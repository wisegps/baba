package com.wise.violation;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import xlist.XListView;
import xlist.XListView.IXListViewListener;
import com.umeng.analytics.MobclickAgent;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Constant;
import com.wise.baba.biz.GetSystem;
import com.wise.baba.net.NetThread;
import com.wise.baba.ui.widget.WaitLinearLayout;
import com.wise.baba.ui.widget.WaitLinearLayout.OnFinishListener;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ComplainActivity extends Activity implements IXListViewListener{
	private static final int getData = 1;
	private static final int sendData = 2;
	private static final int LoadData = 3;
	private static final int refreshData = 4;
	
	TextView tv_total_score,tv_total_complain;
	EditText et_complain;
	String Location;
	
	WaitLinearLayout ll_wait;
	XListView lv_complain;
	List<ComplainData> complainDatas = new ArrayList<ComplainData>();
	ComplainAdapter complainAdapter;
	
	boolean isChange = false;
	int index = 0;
	int total_complain = 0;
	AppApplication app;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_complain);
		app = (AppApplication)getApplication();
		ll_wait = (WaitLinearLayout)findViewById(R.id.ll_wait);
		ll_wait.setOnFinishListener(onFinishListener);
		lv_complain = (XListView)findViewById(R.id.lv_complain);
		lv_complain.setPullLoadEnable(true);
		lv_complain.setPullRefreshEnable(true);
		lv_complain.setXListViewListener(this);
		lv_complain.setOnFinishListener(onFinishListener);
		lv_complain.setBottomFinishListener(onFinishListener);
		complainAdapter = new ComplainAdapter();
		lv_complain.setAdapter(complainAdapter);
		et_complain = (EditText)findViewById(R.id.et_complain);
		tv_total_score = (TextView)findViewById(R.id.tv_total_score);
		tv_total_complain = (TextView)findViewById(R.id.tv_total_complain);
		TextView tv_adress = (TextView)findViewById(R.id.tv_adress);
		TextView tv_send = (TextView)findViewById(R.id.tv_send);
		tv_send.setOnClickListener(onClickListener);
		ImageView iv_back = (ImageView)findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		index = getIntent().getIntExtra("index", 0);
		Location = getIntent().getStringExtra("Location");
		int total_vio = getIntent().getIntExtra("total_vio", 0);
		total_complain = getIntent().getIntExtra("total_complain", 0);
		tv_total_score.setText(String.valueOf(total_vio));
		tv_total_complain.setText(String.valueOf(total_complain));
		tv_adress.setText(Location);
		getData();
	}
	OnClickListener onClickListener = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				Back();
				break;
			case R.id.tv_send:
				send();
				break;
			}
		}
	};
	String refresh = "";
	String load = "";
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case getData:
				ll_wait.runFast(0);
				complainDatas.addAll(0, jsonData(msg.obj.toString()));
				complainAdapter.notifyDataSetChanged();
				break;
			case sendData:
				jsonSend(msg.obj.toString());
				break;
			case LoadData:
				lv_complain.runBottomFast(2);
            	load = msg.obj.toString();
				break;
			case refreshData:
				lv_complain.runFast(1);
            	refresh = msg.obj.toString();
				break;
			}
		}		
	};
	OnFinishListener onFinishListener = new OnFinishListener() {		
		@Override
		public void OnFinish(int index) {
			if(index == 0){
				ll_wait.setVisibility(View.GONE);
				lv_complain.setVisibility(View.VISIBLE);
			}else if(index == 2){
				complainDatas.addAll(jsonData(load));
				complainAdapter.notifyDataSetChanged();
				onLoadOver();
			}else if(index == 1){
				complainDatas.addAll(0,jsonData(refresh));
				complainAdapter.notifyDataSetChanged();
				onLoadOver();
			}
		}
	};
	private void jsonSend(String str){
		try {
			JSONObject jsonObject = new JSONObject(str);
			if(jsonObject.getString("status_code").equals("0")){
				isChange = true;
				//TODO 刷新
				String complain_id = jsonObject.getString("complain_id");
				ComplainData complainData = new ComplainData();
				complainData.setComplain_id(complain_id);
				complainData.setContent(content);
				complainData.setCust_name(app.cust_name);
				complainData.setCreate_time(GetSystem.GetNowTime());
				complainDatas.add(0, complainData);
				complainAdapter.notifyDataSetChanged();
				lv_complain.setSelection(0);
				total_complain = total_complain + 1;
				tv_total_complain.setText(String.valueOf(total_complain));
				
				content = "";
				et_complain.setText("");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}		
	}
	String content = "";
	private void send(){
		content = et_complain.getText().toString().trim();
		if(content.equals("")){
			Toast.makeText(ComplainActivity.this, "内容不能为空", Toast.LENGTH_SHORT).show();
			return;
		}
		String url = Constant.BaseUrl + "violation_complain?auth_code=" + app.auth_code;
		List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("location", Location));
        params.add(new BasicNameValuePair("cust_name", app.cust_name));
        params.add(new BasicNameValuePair("content", content));
        new NetThread.postDataThread(handler, url, params, sendData).start();
	}
	private List<ComplainData> jsonData(String str){
		List<ComplainData> complainDatas = new ArrayList<ComplainData>();
		try {
			JSONObject jsonObject = new JSONObject(str);
			JSONArray jsonArray = jsonObject.getJSONArray("data");
			for(int i = 0 ; i < jsonArray.length() ; i++){
				JSONObject jsonData = jsonArray.getJSONObject(i);
				ComplainData complainData = new ComplainData();
				complainData.setCreate_time(GetSystem.ChangeTimeZone(jsonData.getString("create_time").substring(0, 19).replace("T", " ")));
				complainData.setContent(jsonData.getString("content"));
				complainData.setCust_name(jsonData.getString("cust_name"));
				complainData.setComplain_id(jsonData.getString("complain_id"));
				complainDatas.add(complainData);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return complainDatas;
	}
	private void getData(){
		try {
			ll_wait.startWheel();
			String url = Constant.BaseUrl + "violation_complain/" + URLEncoder.encode(Location, "UTF-8") + 
					"?auth_code=" + app.auth_code;
			new Thread(new NetThread.GetDataThread(handler, url, getData)).start();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	class ComplainAdapter extends BaseAdapter{
		LayoutInflater mInflater = LayoutInflater.from(ComplainActivity.this);
		@Override
		public int getCount() {
			return complainDatas.size();
		}
		@Override
		public Object getItem(int arg0) {
			return complainDatas.get(arg0);
		}
		@Override
		public long getItemId(int arg0) {
			return arg0;
		}
		@Override
		public View getView(int arg0, View convertView, ViewGroup arg2) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.item_complain, null);
				holder = new ViewHolder();
				holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
				holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);
				holder.tv_content = (TextView) convertView.findViewById(R.id.tv_content);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			if(complainDatas.get(arg0).getCust_name().equals("")){
				holder.tv_name.setText("匿名");
			}else{
				holder.tv_name.setText(complainDatas.get(arg0).getCust_name());
			}
			holder.tv_time.setText(complainDatas.get(arg0).getCreate_time());
			holder.tv_content.setText(complainDatas.get(arg0).getContent());
			return convertView;
		}
		private class ViewHolder {
			TextView tv_name,tv_time,tv_content;
		}
	}
	class ComplainData{
		String create_time;
		String content;
		String cust_name;
		String complain_id;
		public String getCreate_time() {
			return create_time;
		}
		public void setCreate_time(String create_time) {
			this.create_time = create_time;
		}
		public String getContent() {
			return content;
		}
		public void setContent(String content) {
			this.content = content;
		}
		public String getCust_name() {
			return cust_name;
		}
		public void setCust_name(String cust_name) {
			this.cust_name = cust_name;
		}
		public String getComplain_id() {
			return complain_id;
		}
		public void setComplain_id(String complain_id) {
			this.complain_id = complain_id;
		}
		@Override
		public String toString() {
			return "ComplainData [create_time=" + create_time + ", content="
					+ content + ", cust_name=" + cust_name + ", complain_id="
					+ complain_id + "]";
		}		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Back();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	private void Back(){
		if(isChange){
			Intent intent = new Intent();
			intent.putExtra("index", index);
			intent.putExtra("size", total_complain);
			setResult(3, intent);
		}
		finish();
	}
	@Override
	public void onRefresh() {
		if(complainDatas.size() != 0){
			refresh = "";
			try {
				String id = complainDatas.get(0).getComplain_id();
				String url = Constant.BaseUrl + "violation_complain/" + URLEncoder.encode(Location, "UTF-8") + 
						"?auth_code=" + app.auth_code + "&max_id=" + id;
				new Thread(new NetThread.GetDataThread(handler, url, refreshData)).start();
				lv_complain.startHeaderWheel();
			} catch (Exception e) {
				e.printStackTrace();
			}			
		}
	}
	@Override
	public void onLoadMore() {
		if(complainDatas.size() != 0){
            String id = complainDatas.get(complainDatas.size() - 1).getComplain_id();
            try {
            	load = "";
    			String url = Constant.BaseUrl + "violation_complain/" + URLEncoder.encode(Location, "UTF-8") + 
    					"?auth_code=" + app.auth_code + "&min_id=" + id;
    			new Thread(new NetThread.GetDataThread(handler, url, LoadData)).start();
    			lv_complain.startBottomWheel();
    		} catch (UnsupportedEncodingException e) {
    			e.printStackTrace();
    		}
        }
	}
	private void onLoadOver() {
		lv_complain.refreshHeaderView();
		lv_complain.refreshBottomView();
		lv_complain.stopRefresh();
		lv_complain.stopLoadMore();
		lv_complain.setRefreshTime(GetSystem.GetNowTime());
	}
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
}