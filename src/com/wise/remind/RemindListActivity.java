package com.wise.remind;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.umeng.analytics.MobclickAgent;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Constant;
import com.wise.baba.biz.GetSystem;
import com.wise.baba.entity.CarData;
import com.wise.baba.entity.RemindData;
import com.wise.baba.net.NetThread;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 车务提醒列表
 * @author honesty
 */
public class RemindListActivity extends Activity {
    private static final String TAG = "RemindActivity";
    private static final int get_remind = 1;
    
    TextView tv_name,tv_date,tv_date_last,tv_date_next;
    LinearLayout ll_frist;
    RelativeLayout rl_Note;
    List<RemindData> remindDatas = new ArrayList<RemindData>();
    RemindAdapter remindAdapter;
    AppApplication app;
    String cust_id;
    List<CarData> carDatas ;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_car_remind);
        app = (AppApplication)getApplication();
        cust_id = this.getIntent().getStringExtra("cust_id");
        carDatas = (List<CarData>) this.getIntent().getSerializableExtra("carDatas");
        if(cust_id == null){
        	cust_id= app.cust_id;
        }
        if(carDatas == null){
        	carDatas = app.carDatas;
        }
        
        rl_Note = (RelativeLayout)findViewById(R.id.rl_Note);
        ll_frist = (LinearLayout)findViewById(R.id.ll_frist);
        ll_frist.setOnClickListener(onClickListener);
        tv_date_last = (TextView)findViewById(R.id.tv_date_last);
        tv_date_next = (TextView)findViewById(R.id.tv_date_next);
        tv_name = (TextView)findViewById(R.id.tv_name);
		tv_date = (TextView)findViewById(R.id.tv_date);
        ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
        iv_back.setOnClickListener(onClickListener);
        ImageView iv_add = (ImageView) findViewById(R.id.iv_add);
        iv_add.setOnClickListener(onClickListener);
        getData();
        ListView lv_remind = (ListView)findViewById(R.id.lv_remind);
        remindAdapter = new RemindAdapter();
        lv_remind.setAdapter(remindAdapter);
        lv_remind.setOnItemClickListener(onItemClickListener);
    }
    private void getData(){
    	String url = Constant.BaseUrl + "customer/" + cust_id + "/reminder?auth_code=" + app.auth_code;
    	new Thread(new NetThread.GetDataThread(handler, url, get_remind)).start();
    	Log.i("RemindListActivity", url);
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case get_remind:
            	jsonRemind(msg.obj.toString());
            	remindAdapter.notifyDataSetChanged();
            	if(remindDatas.size() > 0){
            		rl_Note.setVisibility(View.GONE);
            		RemindData remindData = remindDatas.get(0);
            		ll_frist.setVisibility(View.VISIBLE);
            		String title = "距离" + getCarName(remindData.getObj_id()) + Constant.items_note_type[remindData.getRemind_type()];
            		tv_name.setText(title);            		
            		int count_time = GetSystem.isTimeOut(remindData.getRemind_time());
            		if(count_time > 0){
            			tv_date.setText(GetSystem.jsTime(count_time));
            			tv_date.setTextColor(getResources().getColor(R.color.blue_press));
            			tv_date_last.setVisibility(View.VISIBLE);
            			tv_date_next.setVisibility(View.VISIBLE);
            		}else{
            			tv_date.setText("已过期");
            			tv_date.setTextColor(getResources().getColor(R.color.pink));
            			tv_date_last.setVisibility(View.GONE);
            			tv_date_next.setVisibility(View.GONE);
            		}
            	}else{
            		ll_frist.setVisibility(View.GONE);
            		rl_Note.setVisibility(View.VISIBLE);
            	}
            	break;
            }
        }
    };
    OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.iv_add:
            	Intent intent1 = new Intent(RemindListActivity.this, RemindAddActivity.class);
            	intent1.putExtra("cust_id", cust_id);
            	intent1.putExtra("carDatas", (Serializable)carDatas);
            	startActivityForResult(intent1,2);
            	break;
            case R.id.ll_frist:
            	Intent intent = new Intent(RemindListActivity.this, RemindActivity.class);
    			intent.putExtra("isNeedGetData", false);
    			intent.putExtra("remindData", remindDatas.get(0));
    			intent.putExtra("carDatas", (Serializable)carDatas);
    			startActivityForResult(intent, 2);
            	break;
            }
        }
    };
    OnItemClickListener onItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			Intent intent = new Intent(RemindListActivity.this, RemindActivity.class);
			intent.putExtra("isNeedGetData", false);
			intent.putExtra("remindData", remindDatas.get(arg2));
			intent.putExtra("carDatas", (Serializable)carDatas);
			startActivityForResult(intent, 2);
		}
	};
    private void jsonRemind(String str){
    	try {
    		
    		Log.i("RemindListActivity", str);
    		remindDatas.clear();
			JSONArray jsonArray = new JSONArray(str);
			for(int i = 0 ; i < jsonArray.length() ; i++){
				
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String remind_time = jsonObject.getString("remind_time").substring(0, 10);
				RemindData remindData = new RemindData();
				remindData.setCreate_time(jsonObject.getString("create_time"));
				remindData.setRemind_time(remind_time);
				remindData.setContent(jsonObject.getString("content"));
				remindData.setRepeat_type(jsonObject.getInt("repeat_type"));
				remindData.setRemind_way(jsonObject.getInt("remind_way"));
				
				if(jsonObject.opt("mileage") == null){
					remindData.setMileages(0);
				}else{
					int mileage = jsonObject.getInt("mileage");
					remindData.setMileages(mileage);
				}
				if(jsonObject.opt("cur_mileage") == null){
					remindData.setCur_mileage(0);
				}else{
					int cur_mileage = jsonObject.getInt("cur_mileage");
					remindData.setCur_mileage(cur_mileage);
				}
				
				remindData.setObj_id(jsonObject.getInt("obj_id"));
				remindData.setRemind_type(jsonObject.getInt("remind_type"));
				remindData.setReminder_id(jsonObject.getString("reminder_id"));
				remindData.setUrl(jsonObject.getString("url"));
				int count_time = GetSystem.isTimeOut(remind_time);
				if(count_time > 0){
					remindData.setCount_time(GetSystem.jsTime(count_time));
				}else{
					remindData.setCount_time("");
				}
				remindDatas.add(remindData);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
    }
    
    private String getObj_name(int Obj_id){
    	for(CarData carData : carDatas){
    		if(carData.getObj_id() == Obj_id){
    			return carData.getNick_name();
    		}
    	}
    	return "";
    }
        
    class RemindAdapter extends BaseAdapter{
    	private LayoutInflater layoutInflater = LayoutInflater.from(RemindListActivity.this);
		@Override
		public int getCount() {
			return remindDatas.size();
		}
		@Override
		public Object getItem(int arg0) {
			return remindDatas.get(arg0);
		}
		@Override
		public long getItemId(int arg0) {
			return arg0;
		}
		@Override
		public View getView(int arg0, View convertView, ViewGroup arg2) {
			ViewHolder holder = null;
			if(convertView == null){
				convertView = layoutInflater.inflate(R.layout.item_remind, null);
	            holder = new ViewHolder();
	            holder.tv_remind_type = (TextView) convertView.findViewById(R.id.tv_remind_type);
	            holder.tv_obj_name = (TextView) convertView.findViewById(R.id.tv_obj_name);
	            holder.tv_remind_time = (TextView) convertView.findViewById(R.id.tv_remind_time);
	            holder.tv_date = (TextView) convertView.findViewById(R.id.tv_date);
	            holder.tv_day = (TextView) convertView.findViewById(R.id.tv_day);
	            holder.tv_remind_mileage = (TextView) convertView.findViewById(R.id.tv_remind_mileage);
	            holder.tv_mileage = (TextView) convertView.findViewById(R.id.tv_mileage);
	            holder.tv_mileage_unit = (TextView) convertView.findViewById(R.id.tv_mileage_unit);
	            holder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);
	            convertView.setTag(holder);				
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			RemindData remindData = remindDatas.get(arg0);
			holder.tv_remind_type.setText(Constant.items_note_type[remindData.getRemind_type()]);
			holder.tv_obj_name.setText(getObj_name(remindData.getObj_id()));
			holder.tv_remind_time.setText(remindData.getRemind_time());
			String Count_time = remindData.getCount_time();
			if(Count_time.equals("")){
				holder.tv_date.setText("");
				holder.tv_day.setText("已过期");
				holder.tv_day.setTextColor(getResources().getColor(R.color.pink));
				holder.tv_mileage.setVisibility(View.GONE);
				holder.tv_mileage_unit.setVisibility(View.GONE);
				holder.tv_remind_mileage.setVisibility(View.GONE);
			}else{
				holder.tv_date.setText(remindDatas.get(arg0).getCount_time());
				holder.tv_day.setText("天");
				holder.tv_day.setTextColor(getResources().getColor(R.color.navy));
				if(remindDatas.get(arg0).getRemind_type() == 2){
					//保养里程提醒
					holder.tv_remind_mileage.setText(remindDatas.get(arg0).getCur_mileage() + "KM");
					holder.tv_remind_mileage.setVisibility(View.VISIBLE);
					holder.tv_mileage.setVisibility(View.VISIBLE);
					holder.tv_mileage_unit.setVisibility(View.VISIBLE);
					if((remindDatas.get(arg0).getMileages() - remindDatas.get(arg0).getCur_mileage()) > 0){
						holder.tv_mileage.setText(String.valueOf(remindDatas.get(arg0).getMileages() - remindDatas.get(arg0).getCur_mileage()));
					}else{
						holder.tv_mileage.setText("0");
					}
				}else{
					holder.tv_mileage.setVisibility(View.GONE);
					holder.tv_mileage_unit.setVisibility(View.GONE);
					holder.tv_remind_mileage.setVisibility(View.GONE);
				}
			}
			holder.iv_icon.setBackgroundResource(Constant.items_note_type_image[remindDatas.get(arg0).getRemind_type()]);
			return convertView;
		}
		private class ViewHolder {
	        TextView tv_remind_type,tv_obj_name,tv_remind_time,tv_date,tv_day,tv_remind_mileage,
	        			tv_mileage,tv_mileage_unit;
	        ImageView iv_icon;
	    }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	if(resultCode == 2){
    		getData();
    	}if(resultCode == 3){
    		getData();
    	}
    }
    /**得到车辆对应的位置**/
	private String getCarName(int Obj_id){
		for(int i = 0 ; i < carDatas.size() ; i++){
			if(carDatas.get(i).getObj_id() == Obj_id){
				return carDatas.get(i).getNick_name();
			}
		}
		return "";
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