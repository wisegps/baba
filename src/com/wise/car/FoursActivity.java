package com.wise.car;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;
import com.umeng.analytics.MobclickAgent;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.app.Constant;
import com.wise.baba.entity.BaseData;
import com.wise.baba.net.NetThread;

import android.app.Activity;
import android.app.ProgressDialog;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 4s店
 * 
 * @author
 */
public class FoursActivity extends Activity {

	private static final int get_4s = 1;
	RelativeLayout rl_Note;
	private ImageView iv_back;
	private ListView lv_4s;
	private FoursAdapter foursAdapter;
	AppApplication app;
	private String brank = "";
	private String city = "";

	ProgressDialog progressDialog = null;
	private List<String[]> maintains = new ArrayList<String[]>();
	boolean isGetDB = false;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_4s);
		app = (AppApplication)getApplication();
		rl_Note = (RelativeLayout)findViewById(R.id.rl_Note);
		lv_4s = (ListView) findViewById(R.id.lv_4s);
		foursAdapter = new FoursAdapter();
		lv_4s.setAdapter(foursAdapter);
		lv_4s.setOnItemClickListener(onItemClickListener);
		iv_back = (ImageView) findViewById(R.id.iv_back);
		iv_back.setOnClickListener(onClickListener);

		brank = (String) getIntent().getStringExtra("brank");
		city = (String) getIntent().getStringExtra("city");
		
		List<BaseData> baseDatas = DataSupport.where("Title = ?",Constant.Maintain + "/"
				+ city + "/" + brank).find(BaseData.class);
		if(baseDatas.size() == 0 || baseDatas.get(0).getContent() == null || baseDatas.get(0).getContent().equals("")){
			progressDialog = ProgressDialog.show(FoursActivity.this,
					getString(R.string.dialog_title),
					getString(R.string.dialog_message));
			progressDialog.setCancelable(true);
		}else{
			isGetDB = true;
			parseJSON(baseDatas.get(0).getContent());
		}
		
		String urlCity = "";
		String urlBrank = "";
		
		try {
			urlBrank = URLEncoder.encode(brank, "UTF-8");
			urlCity = URLEncoder.encode(city, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String url = Constant.BaseUrl
				+ "base/dealer?city=" + urlCity + "&brand=" + urlBrank
				+ "&cust_id=" + app.cust_id;
		new NetThread.GetDataThread(handler,url , get_4s).start();
	}

	OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_back:
				FoursActivity.this.finish();
				break;
			}
		}
	};
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case get_4s:
				if(progressDialog != null){
					progressDialog.dismiss();
				}
				if(msg.obj.toString() == null || msg.obj.toString().equals("")){
					rl_Note.setVisibility(View.VISIBLE);
					lv_4s.setVisibility(View.GONE);
				}else{
					if(isGetDB){
						BaseData baseData  = new BaseData();
				        baseData.setContent(msg.obj.toString());
				        baseData.updateAll("Title=?" , Constant.Maintain + "/" + city + "/"+ brank);
					}else{
						BaseData baseData = new BaseData();
						baseData.setTitle(Constant.Maintain + "/" + city + "/"+ brank);
						baseData.setContent(msg.obj.toString());
						baseData.save();
					}					
					parseJSON(msg.obj.toString());
				}
				break;
			}
		}
	};

	OnItemClickListener onItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			Intent intents = new Intent();
			intents.putExtra("maintain_name", maintains.get(arg2)[0]);
			intents.putExtra("maintain_phone", maintains.get(arg2)[1]);
			FoursActivity.this.setResult(5, intents);
			FoursActivity.this.finish();
		}
	};

	public void parseJSON(String jsonData) {
		maintains.clear();
		try {
			JSONArray jsonArray = new JSONArray(jsonData);
			for (int i = 0; i < jsonArray.length(); i++) {
				String[] str = new String[2];
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				str[0] = jsonObject.getString("name");
				str[1] = jsonObject.getString("tel");
				maintains.add(str);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if(maintains.size() > 0){
			rl_Note.setVisibility(View.GONE);
			lv_4s.setVisibility(View.VISIBLE);
		}else{
			rl_Note.setVisibility(View.VISIBLE);
			lv_4s.setVisibility(View.GONE);
		}
		foursAdapter.notifyDataSetChanged();
	}

	class FoursAdapter extends BaseAdapter {
		private LayoutInflater inflater = LayoutInflater
				.from(FoursActivity.this);

		public int getCount() {
			return maintains.size();
		}

		public Object getItem(int position) {
			return maintains.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = inflater.inflate(R.layout.item_4s, null);
				holder.tv_name = (TextView) convertView
						.findViewById(R.id.tv_name);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.tv_name.setText(maintains.get(position)[0]);
			return convertView;
		}

		class ViewHolder {
			TextView tv_name;
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