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
import pubclas.Constant;
import pubclas.NetThread;
import pubclas.Variable;
import com.wise.baba.R;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ComplainActivity extends Activity {
	private static final int getData = 1;
	private static final int sendData = 2;
	EditText et_complain;
	String Location;
	List<ComplainData> complainDatas = new ArrayList<ComplainData>();
	ComplainAdapter complainAdapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_complain);
		ListView lv_complain = (ListView)findViewById(R.id.lv_complain);
		complainAdapter = new ComplainAdapter();
		lv_complain.setAdapter(complainAdapter);
		et_complain = (EditText)findViewById(R.id.et_complain);
		Button bt_send = (Button)findViewById(R.id.bt_send);
		bt_send.setOnClickListener(onClickListener);
		ImageView iv_back = (ImageView)findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		Location = getIntent().getStringExtra("Location");
		getData();
	}
	OnClickListener onClickListener = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			case R.id.bt_send:
				send();
				break;
			}
		}
	};
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case getData:
				jsonData(msg.obj.toString());
				break;
			case sendData:
				jsonSend(msg.obj.toString());
				break;
			}
		}		
	};
	private void jsonSend(String str){
		try {
			JSONObject jsonObject = new JSONObject(str);
			if(jsonObject.getString("status_code").equals("0")){
				et_complain.setText("");
				getData();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}		
	}
	private void send(){
		String content = et_complain.getText().toString().trim();
		if(content.equals("")){
			Toast.makeText(ComplainActivity.this, "内容不能为空", Toast.LENGTH_SHORT).show();
			return;
		}
		String url = Constant.BaseUrl + "violation_complain?auth_code=" + Variable.auth_code;
		List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("location", Location));
        params.add(new BasicNameValuePair("cust_name", Variable.cust_name));
        params.add(new BasicNameValuePair("content", content));
        new Thread(new NetThread.postDataThread(handler, url, params, sendData)).start();
	}
	private void jsonData(String str){
		try {
			complainDatas.clear();
			JSONObject jsonObject = new JSONObject(str);
			JSONArray jsonArray = jsonObject.getJSONArray("data");
			for(int i = 0 ; i < jsonArray.length() ; i++){
				JSONObject jsonData = jsonArray.getJSONObject(i);
				ComplainData complainData = new ComplainData();
				complainData.setCreate_time(jsonData.getString("create_time"));
				complainData.setContent(jsonData.getString("content"));
				complainData.setCust_name(jsonData.getString("cust_name"));
				complainData.setComplain_id(jsonData.getString("complain_id"));
				complainDatas.add(complainData);
			}
			complainAdapter.notifyDataSetChanged();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	private void getData(){
		try {
			String url = Constant.BaseUrl + "violation_complain/" + URLEncoder.encode(Location, "UTF-8") + 
					"?auth_code=" + Variable.auth_code;
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
			holder.tv_name.setText(complainDatas.get(arg0).getCust_name());
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
}