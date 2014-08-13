package com.wise.notice;

import org.json.JSONArray;
import org.json.JSONObject;
import pubclas.Constant;
import pubclas.GetSystem;
import pubclas.NetThread;
import pubclas.Variable;
import com.wise.baba.R;
import com.wise.setting.LoginActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


public class NoticeFragment extends Fragment{
	
	private final int GET_SMS = 1;
	TextView tv_content,tv_time,tv_noti_number;
	BtnListener btnListener;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {		
		return inflater.inflate(R.layout.fragement_notice, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		ImageView iv_fm_back = (ImageView)getActivity().findViewById(R.id.iv_fm_back);
		iv_fm_back.setOnClickListener(onClickListener);
		RelativeLayout ll_fm_notice = (RelativeLayout)getActivity().findViewById(R.id.ll_fm_notice);
		ll_fm_notice.setOnClickListener(onClickListener);
		tv_content = (TextView)getActivity().findViewById(R.id.tv_content);
		tv_time = (TextView)getActivity().findViewById(R.id.tv_time);
		tv_noti_number = (TextView)getActivity().findViewById(R.id.tv_noti_number);
		tv_noti_number.setVisibility(View.GONE);
		getData();
	}
	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_fm_back:
				btnListener.Back();
				break;
			case R.id.ll_fm_notice:
				if(isLogin()){
					startActivity(new Intent(getActivity(), SmsActivity.class));
				}
				break;
			}
		}
	};
	public void SetBtnListener(BtnListener btnListener){
		this.btnListener = btnListener;
	}
	public interface BtnListener{
		public void Back();
	}
	private boolean isLogin(){
		if(Variable.cust_id == null){
			startActivity(new Intent(getActivity(), LoginActivity.class));
			return false;
		}
		return true;
	}
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case GET_SMS:
				jsonData(msg.obj.toString());
				break;

			default:
				break;
			}
		}		
	};
	private void getData(){
		String url = Constant.BaseUrl +  "customer/" + Variable.cust_id + "/notification?auth_code=" + Variable.auth_code;
	    new Thread(new NetThread.GetDataThread(handler, url, GET_SMS)).start();
	}
	private void jsonData(String result){		
	    try {
            JSONArray jsonArray = new JSONArray(result);
            for(int i = 0 ; i < jsonArray.length() ; i++){
                if(i == 0){
                	JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String content = jsonObject.getString("content");
                    String Rcv_time = GetSystem.ChangeTimeZone(jsonObject.getString("rcv_time").replace("T", " ").substring(0, 19));
                    tv_content.setText(content);
                    String day = GetSystem.GetNowDay();
                    if(Rcv_time.indexOf(day) >= 0){
                    	//当天消息
                    	tv_time.setText(Rcv_time.subSequence(11, 16));
                    }else{
                    	tv_time.setText(Rcv_time.subSequence(5, 16));
                    }
					break;
                }
                
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
}