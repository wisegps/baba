package com.wise.notice;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import com.umeng.analytics.MobclickAgent;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Constant;
import com.wise.baba.biz.GetSystem;
import com.wise.baba.net.NetThread;
import com.wise.baba.ui.widget.HScrollLayout;
import com.wise.baba.ui.widget.OnViewChangeListener;
import com.wise.baba.ui.widget.WaitLinearLayout;
import com.wise.baba.ui.widget.WaitLinearLayout.OnFinishListener;
import com.wise.remind.RemindActivity;
import com.wise.violation.TrafficActivity;
import xlist.XListView;
import xlist.XListView.IXListViewListener;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
/**
 * 未读消息页面
 * @author honesty
 *
 */
public class SmsActivity extends Activity implements IXListViewListener{
	
	private static final String TAG = "SmsActivity";
	private final int frist_sms = 1;
	private final int GET_SMS = 2;
	private final int GET_NEXT_SMS = 3;
	
	TextView tv_0,tv_1,tv_2,tv_3,tv_4;
	HScrollLayout hsl_sms;    
    int index_view = 0;
    AppApplication app;

	List<SmsView> smsViews;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_sms);
		app = (AppApplication)getApplication();
		tv_0 = (TextView)findViewById(R.id.tv_0);
		tv_0.setOnClickListener(onClickListener);
		tv_1 = (TextView)findViewById(R.id.tv_1);
		tv_1.setOnClickListener(onClickListener);
		tv_2 = (TextView)findViewById(R.id.tv_2);
		tv_2.setOnClickListener(onClickListener);
		tv_3 = (TextView)findViewById(R.id.tv_3);
		tv_3.setOnClickListener(onClickListener);
		tv_4 = (TextView)findViewById(R.id.tv_4);
		tv_4.setOnClickListener(onClickListener);
		
		ImageView iv_back = (ImageView)findViewById(R.id.iv_back);
		iv_back.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
			    finish();
			}
		});
		int type = getIntent().getIntExtra("type", 0);
		if(type == 0){
			index_view = 4;
		}else{
			index_view = type - 1;
		}
		hsl_sms = (HScrollLayout) findViewById(R.id.hsl_sms);
		hsl_sms.setOnViewChangeListener(new OnViewChangeListener() {			
			@Override
			public void OnViewChange(int view, int duration) {
				index_view = view;
				getSms();
				setCheck();
			}			
		});
		showSms();
		handler.postDelayed(new Runnable() {			
			@Override
			public void run() {
				hsl_sms.snapFastToScreen(index_view);
			}
		}, 50);
	}
	
	OnItemClickListener onItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
			List<SmsData> smsDatas = smsViews.get(index_view).getSmsDatas();
			System.out.println("arg2 = " + arg2);
			if(smsDatas != null && arg2 !=0 && arg2 != (smsDatas.size()+1)){
				System.out.println(smsDatas.get(arg2 -1).toString());
			    String Type = smsDatas.get(arg2 -1).getMsg_type();
	            if(Type.equals("0")){
	                
	            }else if(Type.equals("1")){
	                Intent intent = new Intent(SmsActivity.this, RemindActivity.class);	                
                    int reminder_id = smsDatas.get(arg2 -1).getReminder_id();
                    if(reminder_id != 0){
                    	intent.putExtra("Obj_id", smsDatas.get(arg2 -1).getObj_id());
	                    intent.putExtra("isNeedGetData", true);
	                    intent.putExtra("reminder_id", reminder_id);
                        startActivity(intent);
                    }	
	            }else if (Type.equals("2")) {
	                //holder.tv_new_Regnum.setText("车辆故障");
	                //startActivity(new Intent(SmsActivity.this, CarRemindActivity.class));
	            }else if (Type.equals("3")){
	                //holder.tv_new_Regnum.setText("车辆报警");
	                //startActivity(new Intent(SmsActivity.this, CarRemindActivity.class));
	            }else if (Type.equals("4")){
	            	Intent intent = new Intent(SmsActivity.this, TrafficActivity.class);
	            	intent.putExtra("isService", false);
	                startActivity(intent);
	            }
			}
		}
	};
	
	OnFinishListener onFristFinishListener = new OnFinishListener() {		
		@Override
		public void OnFinish(int index) {			
			SmsView smsView = smsViews.get(index);
			smsView.setNewAdapter(new NewAdapter(smsView.getSmsDatas()));
			smsView.getLv_sms().setAdapter(new NewAdapter(smsView.getSmsDatas()));
			smsView.getLl_wait_show().setVisibility(View.GONE);
			if (smsView.getSmsDatas().size() == 0) {
				smsView.getRl_Note().setVisibility(View.VISIBLE);
				smsView.getLv_sms().setVisibility(View.GONE);
			} else {
				smsView.getRl_Note().setVisibility(View.GONE);
				smsView.getLv_sms().setVisibility(View.VISIBLE);
			}
		}
	};
	
	OnFinishListener onRefreshFinishListener = new OnFinishListener() {		
		@Override
		public void OnFinish(int index) {
			if(index < 5){
				SmsView smsView = smsViews.get(index);
				smsView.getSmsDatas().addAll(0, jsonData(smsView.getRefresh()));
				smsView.getNewAdapter().notifyDataSetChanged();
				//smsView.getLv_sms().setAdapter(new NewAdapter(smsView.getSmsDatas()));
				if (smsView.getSmsDatas().size() == 0) {
					smsView.getRl_Note().setVisibility(View.VISIBLE);
					smsView.getLv_sms().setVisibility(View.GONE);
				} else {
					smsView.getRl_Note().setVisibility(View.GONE);
					smsView.getLv_sms().setVisibility(View.VISIBLE);
				}
			}else{//加载
				index = index - 5;
				SmsView smsView = smsViews.get(index);
				smsView.getSmsDatas().addAll(jsonData(smsView.getLoad()));
				smsView.getNewAdapter().notifyDataSetChanged();
				//smsView.getLv_sms().setAdapter(new NewAdapter(smsView.getSmsDatas()));
				smsView.getLl_wait_show().setVisibility(View.GONE);
				if (smsView.getSmsDatas().size() == 0) {
					smsView.getRl_Note().setVisibility(View.VISIBLE);
					smsView.getLv_sms().setVisibility(View.GONE);
				} else {
					smsView.getRl_Note().setVisibility(View.GONE);
					smsView.getLv_sms().setVisibility(View.VISIBLE);
				}
			}
		    onLoad(index);
		}
	};
	
	private void showSms() {
		smsViews = new ArrayList<SmsView>();
		hsl_sms.removeAllViews();
		for (int i = 0; i < 5; i++) {
			SmsView smsView = new SmsView();
			View v = LayoutInflater.from(this).inflate(R.layout.item_sms_type,null);
			hsl_sms.addView(v);
			XListView lv_sms = (XListView) v.findViewById(R.id.lv_sms);
			lv_sms.setPullLoadEnable(true);
			lv_sms.setPullRefreshEnable(true);
			lv_sms.setXListViewListener(this);
			lv_sms.setOnFinishListener(onRefreshFinishListener);
			lv_sms.setBottomFinishListener(onRefreshFinishListener);
			lv_sms.setOnItemClickListener(onItemClickListener);

			RelativeLayout rl_Note = (RelativeLayout) v.findViewById(R.id.rl_Note);
			LinearLayout ll_wait_show = (LinearLayout) v.findViewById(R.id.ll_wait_show);
			WaitLinearLayout ll_wait = (WaitLinearLayout) v.findViewById(R.id.ll_wait);
			ll_wait.setOnFinishListener(onFristFinishListener);
			ll_wait.setWheelImage(R.drawable.wheel_blue);

			smsView.setRl_Note(rl_Note);
			smsView.setLl_wait(ll_wait);
			smsView.setLl_wait_show(ll_wait_show);
			smsView.setLv_sms(lv_sms);
			smsViews.add(smsView);
			// 读取当前城市的数据
			if (i == index_view) {
				getSms();
			}
		}
	}
	
	private void getSms(){
		SmsView smsView = smsViews.get(index_view);
		if (smsView.getSmsDatas() == null) {
			try {
				int index = 0;
				if(index_view == 4){
					index = 0;
				}else{
					index = index_view + 1;
				}
				String url = Constant.BaseUrl +  "customer/" + app.cust_id + "/notification?auth_code=" + app.auth_code + 
						"&msg_type=" + index;
			    new Thread(new NetThread.GetDataThread(handler, url, frist_sms,index_view)).start();
				smsView.getLl_wait().startWheel(index_view);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	OnClickListener onClickListener = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.tv_0:
				setBg();
				tv_0.setBackgroundResource(R.drawable.bg_border_right_press);
				tv_0.setTextColor(getResources().getColor(R.color.white));
				hsl_sms.snapFastToScreen(4);
				break;
			case R.id.tv_1:
				setBg();
				tv_1.setBackgroundResource(R.drawable.bg_border_left_press);
				tv_1.setTextColor(getResources().getColor(R.color.white));
				hsl_sms.snapFastToScreen(0);
				break;
			case R.id.tv_2:
				setBg();
				tv_2.setBackgroundResource(R.drawable.bg_border_center_press);
				tv_2.setTextColor(getResources().getColor(R.color.white));
				hsl_sms.snapFastToScreen(1);
				break;
			case R.id.tv_3:
				setBg();
				tv_3.setBackgroundResource(R.drawable.bg_border_center_press);
				tv_3.setTextColor(getResources().getColor(R.color.white));
				hsl_sms.snapFastToScreen(2);
				break;
			case R.id.tv_4:
				setBg();
				tv_4.setBackgroundResource(R.drawable.bg_border_center_press);
				tv_4.setTextColor(getResources().getColor(R.color.white));
				hsl_sms.snapFastToScreen(3);
				break;
			}
		}
	};
	
	private void setBg(){
		tv_0.setTextColor(getResources().getColor(R.color.Green));
		tv_0.setBackgroundResource(R.drawable.bg_border_right);
		tv_1.setTextColor(getResources().getColor(R.color.Green));
		tv_1.setBackgroundResource(R.drawable.bg_border_left);
		tv_2.setTextColor(getResources().getColor(R.color.Green));
		tv_2.setBackgroundResource(R.drawable.bg_border_center);
		tv_3.setTextColor(getResources().getColor(R.color.Green));
		tv_3.setBackgroundResource(R.drawable.bg_border_center);
		tv_4.setTextColor(getResources().getColor(R.color.Green));
		tv_4.setBackgroundResource(R.drawable.bg_border_center);
	}
	private void setCheck(){
		setBg();
		switch (index_view) {
		case 0:
			tv_1.setBackgroundResource(R.drawable.bg_border_left_press);
			tv_1.setTextColor(getResources().getColor(R.color.white));
			break;
		case 1:
			tv_2.setBackgroundResource(R.drawable.bg_border_center_press);
			tv_2.setTextColor(getResources().getColor(R.color.white));
			break;
		case 2:
			tv_3.setBackgroundResource(R.drawable.bg_border_center_press);
			tv_3.setTextColor(getResources().getColor(R.color.white));
			break;
		case 3:
			tv_4.setBackgroundResource(R.drawable.bg_border_center_press);
			tv_4.setTextColor(getResources().getColor(R.color.white));
			break;
		case 4:
			tv_0.setBackgroundResource(R.drawable.bg_border_right_press);
			tv_0.setTextColor(getResources().getColor(R.color.white));
			break;
		}
	}
	
	Handler handler = new Handler(){
		@Override
		public void dispatchMessage(Message msg) {
			super.dispatchMessage(msg);
			switch (msg.what) {
			case frist_sms:
				smsViews.get(msg.arg1).getLl_wait().runFast();
				smsViews.get(msg.arg1).setSmsDatas(jsonData(msg.obj.toString()));
				break;
			case GET_SMS:
				smsViews.get(msg.arg1).getLv_sms().runFast(msg.arg1);
				smsViews.get(msg.arg1).setRefresh(msg.obj.toString());	
				break;
			case GET_NEXT_SMS:
				smsViews.get(msg.arg1 -5).getLv_sms().runBottomFast(msg.arg1);
				smsViews.get(msg.arg1 -5).setLoad(msg.obj.toString());
				break;
			}
		}		
	};
		
	private void onLoad(int index) {
		XListView lv_sms = smsViews.get(index).getLv_sms();
		lv_sms.stopRefresh();
		lv_sms.refreshHeaderView();
		lv_sms.refreshBottomView();
		lv_sms.stopLoadMore();
		lv_sms.setRefreshTime(GetSystem.GetNowTime());
	}
	public void onRefresh() {
		SmsView smsView = smsViews.get(index_view);
		try {			
			if(smsView.getSmsDatas().size() != 0){
				int index = 0;
				if(index_view == 4){
					index = 0;
				}else{
					index = index_view + 1;
				}
				smsView.setRefresh("");
	            int id = smsView.getSmsDatas().get(0).getNoti_id();
	            String url = Constant.BaseUrl +  "customer/" + app.cust_id + "/notification?auth_code=" 
	                    + app.auth_code + "&max_id=" + id + "&msg_type=" + index;
	            new Thread(new NetThread.GetDataThread(handler, url, GET_SMS,index_view)).start();
	            smsView.getLv_sms().startHeaderWheel();
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void onLoadMore() {
		SmsView smsView = smsViews.get(index_view);
		try {			
			if(smsView.getSmsDatas().size() != 0){
				int index = 0;
				if(index_view == 4){
					index = 0;
				}else{
					index = index_view + 1;
				}
				smsView.setLoad("");
	            int id = smsView.getSmsDatas().get(smsView.getSmsDatas().size() - 1).getNoti_id();
	            String url = Constant.BaseUrl +  "customer/" + app.cust_id + "/notification?auth_code=" 
	                    + app.auth_code + "&min_id=" + id + "&msg_type=" + index;
	            /**区分头尾**/
	            new Thread(new NetThread.GetDataThread(handler, url, GET_NEXT_SMS,(index_view + 5))).start();
	    		smsView.getLv_sms().startBottomWheel();
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
	
	private List<SmsData> jsonData(String result){
        List<SmsData> Datas = new ArrayList<SmsData>();
	    try {
            JSONArray jsonArray = new JSONArray(result);
            for(int i = 0 ; i < jsonArray.length() ; i++){
                SmsData smsData = new SmsData();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                smsData.setContent(jsonObject.getString("content"));
                smsData.setLat(jsonObject.getString("lat"));
                smsData.setLon(jsonObject.getString("lon"));
                smsData.setMsg_type(jsonObject.getString("msg_type"));
                smsData.setNoti_id(jsonObject.getInt("noti_id"));
                
                if(jsonObject.opt("reminder_id") == null){
                	smsData.setReminder_id(0);
                }else{
                    smsData.setReminder_id(jsonObject.getInt("reminder_id"));
                }
                
                String Rcv_time = GetSystem.ChangeTimeZone(jsonObject.getString("rcv_time").replace("T", " ").substring(0, 19));
                smsData.setRcv_time(Rcv_time.substring(0, 19));
                String status = "";
                if(jsonObject.opt("status") == null){
                    
                }else{
                    status = jsonObject.getString("status");
                }
                String obj_id = "";
                if(jsonObject.opt("obj_id") == null){
                    
                }else{
                    obj_id = jsonObject.getString("obj_id");
                }
                smsData.setObj_id(obj_id);
                smsData.setStatus(status);
                Datas.add(smsData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Datas;
	}
    
	
	class NewAdapter extends BaseAdapter{
	    private LayoutInflater mInflater;
	    List<SmsData> smsDatas;
	    public NewAdapter(List<SmsData> smsDatas){
	        mInflater = LayoutInflater.from(SmsActivity.this);
	        this.smsDatas = smsDatas;
	    }

	    public int getCount() {
	        return smsDatas.size();
	    }

	    public Object getItem(int position) {
	        return smsDatas.get(position);
	    }

	    public long getItemId(int position) {
	        return position;
	    }

	    public View getView(final int position, View convertView, ViewGroup parent) {
	        ViewHolder holder;
	        if (convertView == null) {
	            convertView = mInflater.inflate(R.layout.item_sms, null);
	            holder = new ViewHolder();
	            holder.tv_new_content = (TextView) convertView.findViewById(R.id.tv_new_content);
	            holder.tv_new_time = (TextView)convertView.findViewById(R.id.tv_new_time);
	            holder.tv_new_Regnum = (TextView)convertView.findViewById(R.id.tv_new_Regnum);
	            holder.v_line = (View)convertView.findViewById(R.id.v_line);
	            convertView.setTag(holder);
	        } else {
	            holder = (ViewHolder) convertView.getTag();
	        }
	        holder.tv_new_content.setText(smsDatas.get(position).getContent());
	        holder.tv_new_time.setText(smsDatas.get(position).getRcv_time());
	        
	        TextPaint tp = holder.tv_new_content.getPaint();
	        tp.setStrokeWidth(0);
	        tp.setFakeBoldText(false);
	        
	        TextPaint tt = holder.tv_new_time.getPaint();
	        tt.setStrokeWidth(0);
	        tt.setFakeBoldText(false);
	        
	        TextPaint tr = holder.tv_new_Regnum.getPaint();
	        tr.setStrokeWidth(0);
	        tr.setFakeBoldText(false);
	        
	        String Type = smsDatas.get(position).getMsg_type();
	        if(Type.equals("0")){
                holder.tv_new_Regnum.setText("系统消息");
            }else if(Type.equals("1")){
	            holder.tv_new_Regnum.setText("车务提醒");
	        }else if (Type.equals("2")) {
	            holder.tv_new_Regnum.setText("车辆故障");
	        }else if (Type.equals("3")){
	            holder.tv_new_Regnum.setText("车辆报警");
	        }else if (Type.equals("4")){
                holder.tv_new_Regnum.setText("违章提醒");
            }	        
	        if(position == (smsDatas.size() - 1)){
	        	holder.v_line.setVisibility(View.VISIBLE);
	        }else{
	        	holder.v_line.setVisibility(View.GONE);
	        }
	        return convertView;
	    }
	    private class ViewHolder {
	        TextView tv_new_Regnum,tv_new_content,tv_new_time;
	        View v_line;
	    }
	}
	
	class SmsData{
	    public String lat;
	    public String lon;
	    public String rcv_time;
	    public String msg_type;
	    public String content;
	    public int noti_id;
	    public String status;
	    public String obj_id;
	    public int reminder_id;
	    
	    public String getLat() {
	        return lat;
	    }
	    public void setLat(String lat) {
	        this.lat = lat;
	    }
	    public String getLon() {
	        return lon;
	    }
	    public void setLon(String lon) {
	        this.lon = lon;
	    }
	    public String getRcv_time() {
	        return rcv_time;
	    }
	    public void setRcv_time(String rcv_time) {
	        this.rcv_time = rcv_time;
	    }
	    public String getMsg_type() {
	        return msg_type;
	    }
	    public void setMsg_type(String msg_type) {
	        this.msg_type = msg_type;
	    }
	    public String getContent() {
	        return content;
	    }
	    public void setContent(String content) {
	        this.content = content;
	    }	    
        public int getNoti_id() {
            return noti_id;
        }
        public void setNoti_id(int noti_id) {
            this.noti_id = noti_id;
        }
        public String getStatus() {
            return status;
        }
        public void setStatus(String status) {
            this.status = status;
        }
        public String getObj_id() {
            return obj_id;
        }
        public void setObj_id(String obj_id) {
            this.obj_id = obj_id;
        }        
        public int getReminder_id() {
			return reminder_id;
		}
		public void setReminder_id(int reminder_id) {
			this.reminder_id = reminder_id;
		}
		@Override
        public String toString() {
            return "SmsData [lat=" + lat + ", lon=" + lon + ", rcv_time="
                    + rcv_time + ", msg_type=" + msg_type + ", content="
                    + content + ", noti_id=" + noti_id + ", status=" + status
                    + ", obj_id=" + obj_id + "]";
        }        
	}
	

	
	private class SmsView{
		private NewAdapter newAdapter;
		private XListView lv_sms;
		private RelativeLayout rl_Note;
		private WaitLinearLayout ll_wait;
		private LinearLayout ll_wait_show;
		private List<SmsData> smsDatas;
		private String refresh;
		private String load;
		
		
		public NewAdapter getNewAdapter() {
			return newAdapter;
		}
		public void setNewAdapter(NewAdapter newAdapter) {
			this.newAdapter = newAdapter;
		}
		public String getRefresh() {
			return refresh;
		}
		public void setRefresh(String refresh) {
			this.refresh = refresh;
		}
		public String getLoad() {
			return load;
		}
		public void setLoad(String load) {
			this.load = load;
		}
		public XListView getLv_sms() {
			return lv_sms;
		}
		public void setLv_sms(XListView lv_sms) {
			this.lv_sms = lv_sms;
		}
		public RelativeLayout getRl_Note() {
			return rl_Note;
		}
		public void setRl_Note(RelativeLayout rl_Note) {
			this.rl_Note = rl_Note;
		}
		public WaitLinearLayout getLl_wait() {
			return ll_wait;
		}
		public void setLl_wait(WaitLinearLayout ll_wait) {
			this.ll_wait = ll_wait;
		}
		public LinearLayout getLl_wait_show() {
			return ll_wait_show;
		}
		public void setLl_wait_show(LinearLayout ll_wait_show) {
			this.ll_wait_show = ll_wait_show;
		}
		public List<SmsData> getSmsDatas() {
			return smsDatas;
		}
		public void setSmsDatas(List<SmsData> smsDatas) {
			this.smsDatas = smsDatas;
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