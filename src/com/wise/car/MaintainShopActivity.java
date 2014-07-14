package com.wise.car;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pubclas.Constant;
import pubclas.NetThread;
import pubclas.Variable;
import sql.DBExcute;
import sql.DBHelper;
import com.wise.baba.R;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
/**
 * 4s店
 * @author Mr'wang
 */
public class MaintainShopActivity extends Activity {
	private ListView maintainList = null;
	private MaintainAdapter maintainAdapter = null;
	private Intent intent = null;
	
	private String brank = "";
	private String city = "";
	ProgressDialog progressDialog = null;
	private MyHandler myHandler = null;
	private static final int getMaintainShopCode = 2;
	private List<String[]> MaintainList = new ArrayList<String[]>();
	private ImageView choiceMaintainCancle = null;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choice_maintain);
		maintainList = (ListView) findViewById(R.id.maintain_list);
		choiceMaintainCancle = (ImageView) findViewById(R.id.choice_maintain_cancle);
		choiceMaintainCancle.setOnClickListener(new ClickListener());
		intent = getIntent();
		brank = (String) intent.getStringExtra("brank");
		city = (String) intent.getStringExtra("city");
		myHandler = new MyHandler();
		progressDialog = ProgressDialog.show(MaintainShopActivity.this, getString(R.string.dialog_title), getString(R.string.dialog_message));
		progressDialog.setCancelable(true);
//			查询数据库
			DBHelper dBHelper = new DBHelper(MaintainShopActivity.this);
			SQLiteDatabase reader = dBHelper.getReadableDatabase();
			Cursor cursor = reader.rawQuery("select * from " + Constant.TB_Base + " where Title = ?", new String[]{Constant.Maintain + "/" + city + "/" + brank});
			if(cursor.moveToFirst()){
				parseJSON(cursor.getString(cursor.getColumnIndex("Content")));
				progressDialog.dismiss();
			}else{
				String urlCity = "";
				String urlBrank = "";
				Log.e("MaintainShopActivity  city" , city);
				Log.e("MaintainShopActivity  brank" , brank);
				try {
					urlBrank = URLEncoder.encode(brank, "UTF-8");
					urlCity = URLEncoder.encode(city, "UTF-8");
					
					
					Log.e("MaintainShopActivity  urlCity" , urlCity);
					Log.e("MaintainShopActivity  urlBrank" , urlBrank);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				new Thread(new NetThread.GetDataThread(myHandler, Constant.BaseUrl + "base/dealer?city=" + urlCity + "&brand=" + urlBrank + "&cust_id=" + Variable.cust_id, getMaintainShopCode)).start();
			}
			cursor.close();
			reader.close();		
	}
	
	class MyHandler extends Handler{
		public void handleMessage(Message msg) {
			switch(msg.what){
			case getMaintainShopCode:
				progressDialog.dismiss();
				//存在数据库
				if(!"[]".equals(msg.obj.toString())){
					DBExcute dBExcute = new DBExcute();
					ContentValues values = new ContentValues();
					values.put("Cust_id", Variable.cust_id);
					values.put("Title", Constant.Maintain + "/" + city + "/" + brank);
					values.put("Content", msg.obj.toString());
					dBExcute.InsertDB(MaintainShopActivity.this, values, Constant.TB_Base);
					parseJSON(msg.obj.toString());
				}
				break;
			default:
				return;
			}
			super.handleMessage(msg);
		}
	}
	class ClickListener implements OnClickListener{
		public void onClick(View v) {
			switch(v.getId()){
			case R.id.choice_maintain_cancle:
				MaintainShopActivity.this.finish();
				break;
			}
		}
	}
	public void parseJSON(String jsonData){
		try {
			MaintainList.clear();
			JSONArray jsonArray = new JSONArray(jsonData);
			for(int i = 0 ; i < jsonArray.length() ; i ++){
				String[] str = new String[2];
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				str[0] = jsonObject.getString("name");
				str[1] = jsonObject.getString("tel");
				MaintainList.add(str);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		maintainAdapter = new MaintainAdapter(MaintainShopActivity.this,MaintainList);
		maintainList.setAdapter(maintainAdapter);
		maintainList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
				Intent intents = new Intent();
				intents.putExtra("maintain_name", MaintainList.get(arg2)[0]);
				intents.putExtra("maintain_phone", MaintainList.get(arg2)[1]);
				MaintainShopActivity.this.setResult(5, intents);
				MaintainShopActivity.this.finish();
			}
		});
	}
}

