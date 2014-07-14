package com.wise.remind;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.wise.baba.R;
import data.CarData;
import data.RemindData;
import pubclas.Constant;
import pubclas.NetThread;
import pubclas.Variable;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 车务提醒
 * @author honesty
 */
public class RemindListActivity extends Activity {
    private static final String TAG = "RemindActivity";
    private static final int get_remind = 1;
    
    List<RemindData> remindDatas = new ArrayList<RemindData>();
    RemindAdapter remindAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_car_remind);
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
    	String url = Constant.BaseUrl + "customer/" + Variable.cust_id + "/reminder?auth_code=" + Variable.auth_code;
    	new Thread(new NetThread.GetDataThread(handler, url, get_remind)).start();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case get_remind:
            	System.out.println(msg.obj.toString());
            	jsonRemind(msg.obj.toString());
            	remindAdapter.notifyDataSetChanged();
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
            	startActivity(new Intent(RemindListActivity.this, RemindAddActivity.class));
            	break;
            }
        }
    };
    OnItemClickListener onItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			Intent intent = new Intent(RemindListActivity.this, RemindActivity.class);
			intent.putExtra("remindData", remindDatas.get(arg2));
			startActivityForResult(intent, 2);
		}
	};
    private void jsonRemind(String str){
    	try {
    		remindDatas.clear();
			JSONArray jsonArray = new JSONArray(str);
			for(int i = 0 ; i < jsonArray.length() ; i++){
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				RemindData remindData = new RemindData();
				remindData.setCreate_time(jsonObject.getString("create_time"));
				remindData.setRemind_time(jsonObject.getString("remind_time").substring(0, 10));
				remindData.setContent(jsonObject.getString("content"));
				remindData.setRepeat_type(jsonObject.getInt("repeat_type"));
				remindData.setRemind_way(jsonObject.getInt("remind_way"));
				remindData.setMileage(jsonObject.getInt("mileage"));
				remindData.setObj_id(jsonObject.getInt("obj_id"));
				remindData.setRemind_type(jsonObject.getInt("remind_type"));
				remindData.setReminder_id(jsonObject.getString("reminder_id"));
				System.out.println(remindData.toString());
				remindDatas.add(remindData);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
    }
    
    private String getObj_name(int Obj_id){
    	for(CarData carData : Variable.carDatas){
    		if(carData.getObj_id() == Obj_id){
    			return carData.getObj_name();
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
	            holder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);
	            convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}			
			holder.tv_remind_type.setText(Constant.items_note_type[remindDatas.get(arg0).getRemind_type()]);
			holder.tv_obj_name.setText(getObj_name(remindDatas.get(arg0).getObj_id()));
			holder.tv_remind_time.setText(remindDatas.get(arg0).getRemind_time());
			holder.iv_icon.setBackgroundResource(Constant.items_note_type_image[remindDatas.get(arg0).getRemind_type()]);
			return convertView;
		}
		private class ViewHolder {
	        TextView tv_remind_type,tv_obj_name,tv_remind_time;
	        ImageView iv_icon;
	    }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	if(resultCode == 2){
    		System.out.println("刷新");
    		getData();
    	}
    }
}