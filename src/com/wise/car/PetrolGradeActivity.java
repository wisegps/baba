package com.wise.car;

import java.util.ArrayList;
import java.util.List;

import com.umeng.analytics.MobclickAgent;
import com.wise.baba.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
/**
 * 汽油标号选择
 * @author Mr.Wang
 */
public class PetrolGradeActivity extends Activity {
	ListView petrolGrade = null;
	List<String> petrolGradeList = new ArrayList<String>();
	MyAdapter myAdapter = null;
	LayoutInflater inflater = null;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.petrol_grade);
		petrolGrade = (ListView) findViewById(R.id.petrol_grade_lv);
		ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
		petrolGradeList.add("0#");
		petrolGradeList.add("90#");
		petrolGradeList.add("93#(92#)");
		petrolGradeList.add("97#(95#)");
		myAdapter = new MyAdapter();
		petrolGrade.setAdapter(myAdapter);
		petrolGrade.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
				String chickItem = petrolGradeList.get(arg2);
				Intent intent = new Intent();
				intent.putExtra("result", chickItem);
				intent.putExtra("position", arg2);
				PetrolGradeActivity.this.setResult(3, intent);
				PetrolGradeActivity.this.finish();
			}
		});
		
		iv_back.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				PetrolGradeActivity.this.finish();
			}
		});
	}
	
	
	class MyAdapter extends BaseAdapter{
		ViewHolder viewHolder = null;
		public int getCount() {
			return petrolGradeList.size();
		}
		public Object getItem(int position) {
			return petrolGradeList.get(position);
		}
		public long getItemId(int position) {
			return position;
		}
		public View getView(int position, View convertView, ViewGroup parent) {
			inflater = LayoutInflater.from(PetrolGradeActivity.this);
			if(convertView == null){
				convertView = inflater.inflate(R.layout.item_petrol, null);
				viewHolder = new ViewHolder();
				viewHolder.petRolName = (TextView) convertView.findViewById(R.id.petrol_name);
				viewHolder.petRolName.setText(petrolGradeList.get(position));
				convertView.setTag(viewHolder);
			}else{
				viewHolder = (ViewHolder) convertView.getTag();
			}
			return convertView;
		}
		class ViewHolder{
			TextView petRolName = null;
		}
	}
	@Override
	protected void onRestart() {
		super.onRestart();
		MobclickAgent.onResume(this);
	}
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
}
