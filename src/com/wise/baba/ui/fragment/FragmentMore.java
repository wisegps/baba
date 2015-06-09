package com.wise.baba.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wise.baba.AppApplication;
import com.wise.baba.CollectionActivity;
import com.wise.baba.R;
import com.wise.baba.biz.Judge;
import com.wise.baba.entity.Info;
import com.wise.baba.ui.adapter.OnFinishListener;
import com.wise.notice.NoticeActivity;
import com.wise.remind.RemindListActivity;
import com.wise.setting.LoginActivity;
import com.wise.setting.SetActivity;
import com.wise.violation.TrafficActivity;

/**
 * @author honesty
 **/
public class FragmentMore extends Fragment {

	ImageView iv_noti, iv_vio;
	public static final int SMS = 1;// 传递信息页面跳转类型
	public static final int COLLCETION = 2;// 收藏
	public static final int TRAFFIC = 3;// 违章
	public static final int REMIND = 4;// 提醒
	
	AppApplication app;
	private View rootView;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if(rootView == null){
			rootView = inflater.inflate(R.layout.fragment_more, container,
					false);
		}else{
			ViewGroup parent = (ViewGroup) rootView.getParent();
			if (null != parent) {
				parent.removeView(rootView);
			}
		}
		return rootView;
		
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		app = (AppApplication)getActivity().getApplication();
		TextView tv_finish = (TextView)getActivity().findViewById(R.id.tv_finish);
		tv_finish.setOnClickListener(onClickListener);
		TextView tv_set = (TextView)getActivity().findViewById(R.id.tv_set);
		tv_set.setOnClickListener(onClickListener);
		TextView tv_sms = (TextView)getActivity().findViewById(R.id.tv_sms);
		tv_sms.setOnClickListener(onClickListener);
		TextView tv_collection = (TextView)getActivity().findViewById(R.id.tv_collection);
		tv_collection.setOnClickListener(onClickListener);
		TextView tv_remind = (TextView)getActivity().findViewById(R.id.tv_remind);
		tv_remind.setOnClickListener(onClickListener);
		TextView tv_traffic = (TextView)getActivity().findViewById(R.id.tv_traffic);
		tv_traffic.setOnClickListener(onClickListener);
		ImageView iv_back = (ImageView)getActivity().findViewById(R.id.iv_back);
		iv_back.setVisibility(View.GONE);
		iv_back.setOnClickListener(onClickListener);
		iv_noti = (ImageView)getActivity().findViewById(R.id.iv_noti);
		iv_vio = (ImageView)getActivity().findViewById(R.id.iv_vio);
	}
	OnClickListener onClickListener = new OnClickListener() {	
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				
				break;
			case R.id.tv_finish:
				//退出系统
				if(onFinishListener != null){
					onFinishListener.onFinish();
				}
				break;
			case R.id.tv_sms:				
				if(Judge.isLogin(app)){
					app.noti_count = 0;
					startActivity(new Intent(getActivity(), NoticeActivity.class));
				}else{
					//TODO 传送类型跳转类型
					Intent intent = new Intent(getActivity(), LoginActivity.class);
					intent.putExtra("ActivityState", SMS);
					startActivity(intent);
				}
				break;
			case R.id.tv_collection:
				if(Judge.isLogin(app)){
					startActivity(new Intent(getActivity(), CollectionActivity.class));
				}else{
					Intent intent = new Intent(getActivity(), LoginActivity.class);
					intent.putExtra("ActivityState", COLLCETION);
					startActivity(intent);
				}
				break;
			case R.id.tv_remind:
				if(Judge.isLogin(app)){
					startActivity(new Intent(getActivity(), RemindListActivity.class));
				}else{
					Intent intent = new Intent(getActivity(), LoginActivity.class);
					intent.putExtra("ActivityState", REMIND);
					startActivity(intent);
				}
				break;
			case R.id.tv_traffic:
				if(Judge.isLogin(app)){
					app.vio_count = 0;
					Intent intent = new Intent(getActivity(), TrafficActivity.class);
					if(app.cust_type == Info.ServiceProvider){
						intent.putExtra("isService", true);
					}else{
						intent.putExtra("isService", false);
					}
					
					startActivity(intent);
				}else{
					Intent intent = new Intent(getActivity(), LoginActivity.class);
					intent.putExtra("ActivityState", TRAFFIC);
					startActivity(intent);
				}
				break;
			case R.id.tv_set:
				startActivityForResult(new Intent(getActivity(), SetActivity.class), 1);
				break;
			}
		}
	};

	@Override
	public void onResume() {
		super.onResume();
		setNotiView();
	}
	/**设置提醒**/
	private void setNotiView(){
		if(app.noti_count == 0){
			iv_noti.setVisibility(View.GONE);
		}else{
			iv_noti.setVisibility(View.VISIBLE);
		}
		if(app.vio_count == 0){
			iv_vio.setVisibility(View.GONE);
		}else{
			iv_vio.setVisibility(View.VISIBLE);
		}
	}
	OnFinishListener onFinishListener;
	public void setOnFinishListener(OnFinishListener onFinishListener){
		this.onFinishListener = onFinishListener;
	}
}
