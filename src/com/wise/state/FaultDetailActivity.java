package com.wise.state;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.umeng.analytics.MobclickAgent;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Constant;
import com.wise.baba.net.NetThread;
import com.wise.car.SearchMapActivity;
import com.wise.notice.LetterActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 故障明细列表
 * @author honesty
 *
 */
public class FaultDetailActivity extends Activity{
	
	private static final int clear_obd = 1;
	private String COMMAND_CLEAR_ODBERR = "16448";
	
	RelativeLayout rl_no_fault;
	ListView lv_fault;
	TextView tv_fault;
	String fault_content;
	List<FaultData> faultDatas = new ArrayList<FaultData>();
	int[][] colors = {{83,181,220},{106,195,149},{133,208,66},{245,149,91},{248,127,96},{255,105,105}};
	
	AppApplication app;
	String device_id;
	ImageView iv_clear_obd;
	ProgressDialog myDialog = null;
	int index;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_fault_detail);
		app = (AppApplication) getApplication();
		rl_no_fault = (RelativeLayout)findViewById(R.id.rl_no_fault);
		lv_fault = (ListView)findViewById(R.id.lv_fault);
		tv_fault = (TextView)findViewById(R.id.tv_fault);
		findViewById(R.id.rescue).setOnClickListener(onClickListener);
		findViewById(R.id.risk).setOnClickListener(onClickListener);
		findViewById(R.id.ask).setOnClickListener(onClickListener);
		findViewById(R.id.mechanics).setOnClickListener(onClickListener);
		findViewById(R.id.tv_ask_expert).setOnClickListener(onClickListener);
		ImageView iv_back = (ImageView)findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		iv_clear_obd = (ImageView)findViewById(R.id.iv_clear_obd);
		iv_clear_obd.setOnClickListener(onClickListener);
		fault_content = getIntent().getStringExtra("fault_content");
		device_id = getIntent().getStringExtra("device_id");
		index = getIntent().getIntExtra("index", 0);
		jsonData();
		if(faultDatas.size() == 0){
			rl_no_fault.setVisibility(View.VISIBLE);
		}else{
			rl_no_fault.setVisibility(View.GONE);
			FaultAdapter faultAdapter = new FaultAdapter();
			lv_fault.setAdapter(faultAdapter);
		}
	}
	OnClickListener onClickListener = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				finish();
				break;
			case R.id.iv_clear_obd:
				AlertDialog.Builder dialog = new AlertDialog.Builder(FaultDetailActivity.this);   
				dialog.setTitle("提示");  
				dialog.setMessage("请先启动车辆，再进行故障码清除。"); 
				dialog.setPositiveButton("车辆已启动", new DialogInterface.OnClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						clearObd();
					}
				}).setNegativeButton("取消", null)
				.show(); 
				break;
				// 救援
			case R.id.risk:
				try{//平板没有电话模块异常
					String phone = app.carDatas.get(index)
							.getInsurance_tel();
					Intent in_1 = new Intent(
							Intent.ACTION_DIAL,
							Uri.parse("tel:" + (phone != null ? phone : "")));
					startActivity(in_1);
				}catch (Exception e) {
					e.printStackTrace();
				}				
				break;
			// 报险
			case R.id.rescue:
				try{//平板没有电话模块异常
					String tel = app.carDatas.get(index)
							.getMaintain_tel();
					Intent in_2 = new Intent(Intent.ACTION_DIAL,
							Uri.parse("tel:" + (tel != null ? tel : "")));
					startActivity(in_2);
				}catch (Exception e) {
					e.printStackTrace();
				}
				break;
			// 问一下
			case R.id.ask:
				Toast.makeText(FaultDetailActivity.this, "更新中.....",
						Toast.LENGTH_SHORT).show();
				break;
			// 找气修
			case R.id.mechanics:
				Intent in = new Intent(FaultDetailActivity.this,
						SearchMapActivity.class);
				in.putExtra("index", index);
				in.putExtra("keyWord", "维修店");
				in.putExtra("key", "汽车维修");
				in.putExtra("latitude", 0);
				in.putExtra("longitude", 0);
				startActivity(in);
				break;
			case R.id.tv_ask_expert:
				Intent intent = new Intent(FaultDetailActivity.this, LetterActivity.class);
				intent.putExtra("cust_id", "12");
				intent.putExtra("cust_name", "专家");
				startActivity(intent);
				break;
			}
		}
	};
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case clear_obd:
				jsonObd(msg.obj.toString());
				break;

			default:
				break;
			}
		}		
	};
	private void clearObd(){
		myDialog = ProgressDialog.show(FaultDetailActivity.this,"提示", "正在清除故障码…");
        myDialog.setCancelable(true);
		String url = Constant.BaseUrl + "command?auth_code=" + app.auth_code;
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("device_id", device_id));
		params.add(new BasicNameValuePair("cmd_type", COMMAND_CLEAR_ODBERR));
		new NetThread.postDataThread(handler, url, params, clear_obd).start();
		System.out.println("device_id = " + device_id);
		System.out.println("COMMAND_CLEAR_ODBERR = " + COMMAND_CLEAR_ODBERR);
	}
	private void jsonObd(String result){
		System.out.println(result);
		if(myDialog != null){
			myDialog.dismiss();
		}
		try {
			JSONObject jsonObject = new JSONObject(result);
			if(jsonObject.getInt("status_code") == 0){
				Toast.makeText(getApplicationContext(), "清除故障码成功", Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(getApplicationContext(), "清除故障码失败", Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), "清除故障码失败", Toast.LENGTH_SHORT).show();
		}
	}
	private void jsonData(){
		try {
			if(fault_content == null || fault_content.equals("")){
				return;
			}
			JSONObject jObject = new JSONObject(fault_content);
			int max_level = jObject.getInt("max_level");
			String advice = jObject.getString("advice");
			JSONArray jsonArray = jObject.getJSONArray("data");
			if(jsonArray.length() == 0){
				iv_clear_obd.setVisibility(View.GONE);
			}else{
				iv_clear_obd.setVisibility(View.VISIBLE);
			}
			if(max_level < 0){
				max_level = 0;
			};
			if(max_level >= colors.length){
				max_level = colors.length - 1;
			};
			int color[] = colors[max_level];
			tv_fault.setTextColor(Color.rgb(color[0], color[1], color[2]));
			tv_fault.setText(advice);
			for(int i = 0 ; i < jsonArray.length() ; i++){
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				FaultData faultData = new FaultData();
				faultData.setLevel(jsonObject.getInt("level"));
				faultData.setC_define(jsonObject.getString("code") + "  " +jsonObject.getString("c_define"));
				faultData.setContent(jsonObject.getString("content"));
				faultData.setCategory(jsonObject.getString("category"));
				faultDatas.add(faultData);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	class FaultAdapter extends BaseAdapter{
		private LayoutInflater layoutInflater = LayoutInflater.from(FaultDetailActivity.this);
		@Override
		public int getCount() {
			return faultDatas.size();
		}
		@Override
		public Object getItem(int position) {
			return faultDatas.get(position);
		}
		@Override
		public long getItemId(int position) {
			return position;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if(convertView == null){
				convertView = layoutInflater.inflate(R.layout.item_fault_detail, null);
	            holder = new ViewHolder();
	            holder.tv_define = (TextView) convertView.findViewById(R.id.tv_define);
	            holder.tv_content = (TextView) convertView.findViewById(R.id.tv_content);
	            holder.tv_category = (TextView) convertView.findViewById(R.id.tv_category);
	            convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			FaultData faultData = faultDatas.get(position);
			holder.tv_define.setText(faultData.getC_define());
			holder.tv_content.setText(faultData.getContent());			
			holder.tv_category.setText(faultData.getCategory());
			try {
				int level = faultData.getLevel();
				if(level < 0){
					level = 0;
				};
				if(level >= colors.length){
					level = colors.length - 1;
				};
				int color[] = colors[level];
				holder.tv_define.setTextColor(Color.rgb(color[0], color[1], color[2]));
			} catch (Exception e) {
				e.printStackTrace();
			}
			return convertView;
		}
		private class ViewHolder {
	        TextView tv_define,tv_content,tv_category;
	    }
	}
	class FaultData{
		private int level;
		private String content;
		private String c_define;
		private String category;
		
		public int getLevel() {
			return level;
		}
		public void setLevel(int level) {
			this.level = level;
		}
		public String getContent() {
			return content;
		}
		public void setContent(String content) {
			this.content = content;
		}
		public String getC_define() {
			return c_define;
		}
		public void setC_define(String c_define) {
			this.c_define = c_define;
		}
		public String getCategory() {
			return category;
		}
		public void setCategory(String category) {
			this.category = category;
		}		
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