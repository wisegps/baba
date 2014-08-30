package nadapter;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import pubclas.Constant;
import pubclas.GetSystem;
import pubclas.NetThread;
import pubclas.Variable;
import sql.DBExcute;

import com.baidu.mapapi.model.LatLng;
import com.wise.baba.R;
import data.AdressData;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
/**
 * 地点信息
 * @author honesty
 */
public class AdressAdapter extends BaseAdapter{
	private static final String TAG = "AdressAdapter";
	Context context;
	Activity mActivity;
	List<AdressData> adressDatas;
	LayoutInflater mInflater;
	ProgressDialog myDialog = null;
	private MyHandler myHandler = null;
	private static final int addFavorite = 1;
	int index;

    ForegroundColorSpan blackSpan;
    ForegroundColorSpan graySpan;
    
	public AdressAdapter(Context context,List<AdressData> adressDatas,Activity mActivity){
		this.context = context;
		this.adressDatas = adressDatas;
		this.mActivity = mActivity;
		mInflater = LayoutInflater.from(context);
		myHandler = new MyHandler();
		blackSpan = new ForegroundColorSpan(context.getResources().getColor(R.color.common));
		graySpan = new ForegroundColorSpan(context.getResources().getColor(R.color.common_inactive));
	}
	@Override
	public int getCount() {
		return adressDatas.size();
	}
	@Override
	public Object getItem(int arg0) {
		return adressDatas.get(arg0);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.item_dealadress, null);
			holder = new ViewHolder();
			holder.tv_item_dealadress_name = (TextView) convertView.findViewById(R.id.tv_item_dealadress_name);
			holder.tv_item_dealadress_adress = (TextView)convertView.findViewById(R.id.tv_item_dealadress_adress);
			holder.tv_item_dealadress_phone = (TextView)convertView.findViewById(R.id.tv_item_dealadress_phone);
			holder.iv_Collect = (ImageView)convertView.findViewById(R.id.iv_Collect);
			holder.iv_location = (ImageView)convertView.findViewById(R.id.iv_location);
			holder.iv_tel = (ImageView)convertView.findViewById(R.id.iv_tel);
			holder.iv_share = (ImageView)convertView.findViewById(R.id.iv_share);
			holder.ll_adress_tel = (RelativeLayout)convertView.findViewById(R.id.ll_adress_tel);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		final AdressData adressData = adressDatas.get(position);
		String distance = "";
		if(adressData.getDistance() != -1){
		    distance = d(adressData.getDistance());
		    String str = adressData.getName() + distance;
	        SpannableStringBuilder builder = new SpannableStringBuilder(str);
	        builder.setSpan(blackSpan, 0, adressData.getName().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	        builder.setSpan(graySpan, adressData.getName().length(), str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	        holder.tv_item_dealadress_name.setText(builder);
		}else{
		    holder.tv_item_dealadress_name.setText(adressData.getName());
		}        
        
		holder.tv_item_dealadress_adress.setText("地址：" + adressData.getAdress());
		holder.tv_item_dealadress_phone.setText("电话：" +adressData.getPhone());
		if(adressData.getPhone() == null || adressData.getPhone().equals("")){
			holder.ll_adress_tel.setVisibility(View.GONE);
		}else{
			holder.ll_adress_tel.setVisibility(View.VISIBLE);
		}
		if(adressData.isIs_collect()){
		    holder.iv_Collect.setImageResource(R.drawable.body_icon_collect_press);
		}else{
		    holder.iv_Collect.setImageResource(R.drawable.body_icon_collect);
		}
		holder.iv_share.setOnClickListener(new OnClickListener() {            
            @Override
            public void onClick(View v) {
                if(onCollectListener != null){
                    onCollectListener.OnShare(index);
                }
            }
        });
		//收藏
		holder.iv_Collect.setOnClickListener(new OnClickListener() {				
			@Override
			public void onClick(View v) {				
				//TODO  更新服务器   成功之后再操作 数据库				
				if("".equals(Variable.auth_code)){
					Toast.makeText(context, "请登录",Toast.LENGTH_SHORT).show();
					return;
				}else{
				    if(!adressData.isIs_collect()){
				        index = position;
	                    myDialog = ProgressDialog.show(context,"提示", "收藏中...");
	                    myDialog.setCancelable(true);
	                    List<NameValuePair> params = new ArrayList<NameValuePair>();
	                    params.add(new BasicNameValuePair("cust_id", Variable.cust_id));
	                    params.add(new BasicNameValuePair("name", adressData.getName()));
	                    params.add(new BasicNameValuePair("address", adressData.getAdress()));
	                    params.add(new BasicNameValuePair("tel", adressData.getPhone()));
	                    params.add(new BasicNameValuePair("lon", String.valueOf(adressData.getLon())));
	                    params.add(new BasicNameValuePair("lat", String.valueOf(adressData.getLat())));
	                    new Thread(new NetThread.postDataThread(myHandler, Constant.BaseUrl + "favorite?auth_code=" + Variable.auth_code, params, addFavorite,position)).start();
				    }				    
				}
			}
		});
        //导航
		holder.iv_location.setOnClickListener(new OnClickListener() {				
			@Override
			public void onClick(View v) {
				LatLng pt1 = new LatLng(Variable.Lat, Variable.Lon);
				LatLng pt2 = new LatLng(adressData.getLat(), adressData.getLon());
				//GeoPoint point = new GeoPoint((int) (Variable.Lat * 1E6),(int) (Variable.Lon * 1E6));
				//LatLng point1 = new GeoPoint((int) (adressData.getLat() * 1E6),(int) (adressData.getLon() * 1E6));
                GetSystem.FindCar(mActivity, pt1, pt2, "point", "point1");
			}
		});
        //拨打电话
		holder.iv_tel.setOnClickListener(new OnClickListener() {				
			@Override
			public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL,Uri.parse("tel:"+ adressData.getPhone()));  
                mActivity.startActivity(intent);
			}
		});
		return convertView;
	}
	private class ViewHolder {
		TextView tv_item_dealadress_name,tv_item_dealadress_adress,tv_item_dealadress_phone;
		ImageView iv_Collect,iv_location,iv_tel,iv_share;
		RelativeLayout ll_adress_tel;
	}
	
	class MyHandler extends Handler{
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch(msg.what){
			case addFavorite:
				myDialog.dismiss();
				Log.e("执行添加的结果：",msg.obj.toString());
				try {
					JSONObject jsonObject = new JSONObject(msg.obj.toString());
					if(jsonObject.getString("status_code").equals("0")){
					    AdressData adressData = adressDatas.get(msg.arg1);
						DBExcute dbExcute = new DBExcute();
				        ContentValues values = new ContentValues();
				        values.put("Cust_id", Variable.cust_id);
				        values.put("favorite_id", jsonObject.getString("favorite_id"));
				        values.put("name", adressData.getName());
				        values.put("address", adressData.getAdress());
				        values.put("tel", adressData.getPhone());
				        values.put("lon", adressData.getLon());
				        values.put("lat", adressData.getLat());
				        dbExcute.InsertDB(mActivity, values, Constant.TB_Collection);
						Toast.makeText(mActivity, "添加成功", Toast.LENGTH_SHORT).show();
						if(onCollectListener != null){
						    onCollectListener.OnCollect(index);
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				break;
			}
		}
	}
	private String d(int Distance){
	    if(Distance > 1000){
	        return " (" +(int)Distance/1000 + "km)";
	    }else{
	        return " (" +Distance + "m)";
	    }
	}
	OnCollectListener onCollectListener;
	public void setOnCollectListener(OnCollectListener onCollectListener){
        this.onCollectListener = onCollectListener;
    }
	public interface OnCollectListener {
	    public abstract void OnCollect(int index);
        public abstract void OnShare(int index);
	}
}
