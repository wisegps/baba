package com.wise.state;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.umeng.analytics.MobclickAgent;
import com.wise.baba.R;
import android.app.Activity;
import android.os.Bundle;
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

/**
 * 故障明细
 * @author honesty
 *
 */
public class FaultDetailActivity extends Activity{
	RelativeLayout rl_no_fault;
	ListView lv_fault;
	TextView tv_fault;
	String fault_content;
	List<FaultData> faultDatas = new ArrayList<FaultData>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_fault_detail);
		rl_no_fault = (RelativeLayout)findViewById(R.id.rl_no_fault);
		lv_fault = (ListView)findViewById(R.id.lv_fault);
		tv_fault = (TextView)findViewById(R.id.tv_fault);
		ImageView iv_back = (ImageView)findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);
		fault_content = getIntent().getStringExtra("fault_content");
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
			default:
				break;
			}
		}
	};
	private void jsonData(){
		try {
			JSONArray jsonArray = new JSONArray(fault_content);
			tv_fault.setText(""+jsonArray.length());
			for(int i = 0 ; i < jsonArray.length() ; i++){
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				FaultData faultData = new FaultData();
				faultData.setC_define(jsonObject.getString("c_define"));
				faultData.setContent(jsonObject.getString("content"));
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
	            convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			FaultData faultData = faultDatas.get(position);
			holder.tv_define.setText(faultData.getC_define());
			holder.tv_content.setText(faultData.getContent());
			return convertView;
		}
		private class ViewHolder {
	        TextView tv_define,tv_content;
	    }
	}
	class FaultData{
		private String content;
		private String c_define;
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