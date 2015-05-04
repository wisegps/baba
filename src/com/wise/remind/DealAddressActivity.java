package com.wise.remind;

import java.net.URLEncoder;
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
import com.wise.baba.entity.AdressData;
import com.wise.baba.net.NetThread;
import com.wise.baba.ui.adapter.AdressAdapter;
import com.wise.baba.ui.adapter.AdressAdapter.OnCollectListener;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;

/**
 * 处理地点
 * @author honesty
 */
public class DealAddressActivity extends Activity {
    private static final String TAG = "DealAddressActivity";

    private static final int get_deal = 1;
    ListView lv_activity_dealadress;
    List<AdressData> adressDatas = new ArrayList<AdressData>();
    AdressAdapter adressAdapter;
    int Type;
    AppApplication app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_dealadress);
        app = (AppApplication)getApplication();
        lv_activity_dealadress = (ListView) findViewById(R.id.lv_activity_dealadress);
        adressAdapter = new AdressAdapter(DealAddressActivity.this,
                adressDatas, DealAddressActivity.this);
        adressAdapter.setOnCollectListener(new OnCollectListener() {
            @Override
            public void OnCollect(int index) {
                adressDatas.get(index).setIs_collect(true);
                adressAdapter.notifyDataSetChanged();
            }

            @Override
            public void OnShare(int index) {
                AdressData adressData = adressDatas.get(index);
                String url = "http://api.map.baidu.com/geocoder?location="
                        + adressData.getLat() + "," + adressData.getLon()
                        + "&coord_type=bd09ll&output=html";
                StringBuffer sb = new StringBuffer();
                sb.append("【地点】 ");
                sb.append(adressData.getName());
                sb.append(" 地址: " + adressData.getAdress());
                sb.append(" 电话: " + adressData.getPhone());
                sb.append(" " + url);
                GetSystem.share(DealAddressActivity.this, sb.toString(), "",
                        (float) adressData.getLat(),
                        (float) adressData.getLon(),"地点",url);
            }
        });
        lv_activity_dealadress.setAdapter(adressAdapter);

        ImageView iv_activity_dealadress_back = (ImageView) findViewById(R.id.iv_activity_dealadress_back);
        iv_activity_dealadress_back.setOnClickListener(onClickListener);
        Type = getIntent().getIntExtra("Type", 1);
        String city = getIntent().getStringExtra("city");
        GetDealAdress(city);
    }

    OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.iv_activity_dealadress_back:
                finish();
                break;
            }
        }
    };
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case get_deal:
                jsonDealAdress(msg.obj.toString());
                adressAdapter.notifyDataSetChanged();
                break;
            }
        }
    };

    /**
     * 获取处理地点
     */
    private void GetDealAdress(String city) {
        String LocationCity;
        if(city == null || city.equals("")){
            LocationCity = app.City;
        }else{
            LocationCity = city;
        }
        try {
            String url;
            if(Type == 3){//违章不需要经纬度
                url = Constant.BaseUrl + "location?auth_code="
                        + app.auth_code + "&city="
                        + URLEncoder.encode(LocationCity, "UTF-8") + "&type="
                        + Type + "&cust_id=" + app.cust_id;
            }else{
                url = Constant.BaseUrl + "location?auth_code="
                        + app.auth_code + "&city="
                        + URLEncoder.encode(LocationCity, "UTF-8") + "&type="
                        + Type + "&cust_id=" + app.cust_id + "&lat=" + app.Lat + "&lon=" + app.Lon;
            }            
            new Thread(new NetThread.GetDataThread(handler, url, get_deal))
                    .start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 解析地点
     * @param result
     */
    private void jsonDealAdress(String result) {
        try {
            JSONArray jsonArray = new JSONArray(result);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                AdressData adressData = new AdressData();
                adressData.setAdress(jsonObject.getString("address"));
                adressData.setName(jsonObject.getString("name"));
                adressData.setPhone(jsonObject.getString("tel"));
                adressData.setLat(jsonObject.getDouble("lat"));
                adressData.setLon(jsonObject.getDouble("lon"));
                if (jsonObject.getString("is_collect").equals("1")) {
                    // 收藏
                    adressData.setIs_collect(true);
                } else {
                    // 未收藏
                    adressData.setIs_collect(false);
                }
                if(jsonObject.opt("distance") == null){
                    adressData.setDistance(-1);
                }else{
                    adressData.setDistance(jsonObject.getInt("distance"));
                }
                adressDatas.add(adressData);
            }
        } catch (JSONException e) {
            e.printStackTrace();
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