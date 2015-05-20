package com.wise.baba.ui.adapter;

import java.util.List;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.navi.BaiduMapNavigation;
import com.baidu.mapapi.navi.BaiduMapAppNotSupportNaviException;
import com.wise.baba.AppApplication;
import com.wise.baba.R;
import com.wise.baba.biz.GetSystem;
import com.wise.baba.entity.AdressData;
import com.wise.baba.ui.widget.SlidingView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 收藏
 */
public class CollectionAdapter extends BaseAdapter {
    private static final String TAG = "CollectionAdapter";
    private Context context;
    private LayoutInflater layoutInflater;
    
    List<AdressData> adressDatas = null;

    CollectionItemListener collectionItemListener = null;
    AppApplication app;
    //double currentLat;
    //double currentLon;

    public CollectionAdapter(Context context, List<AdressData> adrDataList) {
        this.context = context;
        app = (AppApplication)((Activity)context).getApplication();
        //currentLat = app.Lat;
        //currentLon = app.Lon;
        layoutInflater = LayoutInflater.from(context);
        this.adressDatas = adrDataList;
    }

    public int getCount() {
        return this.adressDatas.size();
    }

    public Object getItem(int position) {
        return this.adressDatas.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView == null){
            convertView = layoutInflater.inflate(R.layout.item_collection, null);
            holder = new ViewHolder();
            holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            holder.tv_del = (TextView) convertView.findViewById(R.id.tv_del);
            holder.tv_address = (TextView) convertView.findViewById(R.id.tv_address);
            holder.tv_tel = (TextView) convertView.findViewById(R.id.tv_tel);
            holder.tv_distance = (TextView) convertView.findViewById(R.id.tv_distance);
            holder.iv_location = (ImageView) convertView.findViewById(R.id.iv_location);
            holder.iv_tel = (ImageView) convertView.findViewById(R.id.iv_tel);
            holder.iv_share = (ImageView) convertView.findViewById(R.id.iv_share);
            holder.rl_tel = (RelativeLayout) convertView.findViewById(R.id.rl_tel);
            holder.slidingView = (SlidingView)convertView.findViewById(R.id.sv);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        final AdressData adressData = adressDatas.get(position);
        holder.slidingView.ScorllRestFast();
        holder.tv_name.setText(adressData.getName());
        holder.tv_address.setText("地址：" + adressData.getAdress());
        holder.tv_distance.setVisibility(View.GONE);
        if(adressData.getPhone() == null || adressData.getPhone().equals("")){
            holder.rl_tel.setVisibility(View.GONE);
        }else{
            holder.rl_tel.setVisibility(View.VISIBLE);
            holder.tv_tel.setText("电话： " + adressData.getPhone());
        }
        holder.iv_location.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // TODO 切换到导航页面
                startNavi(adressData.getLat(),adressData.getLon());
            }
        });
        holder.iv_tel.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	try {
            		Intent intent = new Intent(Intent.ACTION_DIAL,Uri.parse("tel:"+ adressData.getPhone()));  
                    context.startActivity(intent);
				} catch (Exception e) {}                
            }
        });
        holder.tv_del.setOnClickListener(new OnClickListener() {            
            @Override
            public void onClick(View v) {
                if(collectionItemListener != null){
                    collectionItemListener.Delete(position);
                }
            }
        });
        holder.iv_share.setOnClickListener(new OnClickListener() {            
            @Override
            public void onClick(View v) {
                if(collectionItemListener != null){
                    collectionItemListener.share(position);
                }
            }
        });
        return convertView;
    }
    private class ViewHolder {
        TextView tv_name,tv_address,tv_tel,tv_distance,tv_del;
        ImageView iv_location,iv_tel,iv_share;
        RelativeLayout rl_tel;
        SlidingView slidingView;
    }
    public void setCollectionItem(CollectionItemListener collectionItemListener){
        this.collectionItemListener = collectionItemListener;
    }
    
    public interface CollectionItemListener{
        /**
         * 删除触发
         * @param position
         */
        public void Delete(int position);
        public void share(int position);
    }

    public void startNavi(double goLat, double goLon) {
        LatLng pt1 = new LatLng(app.Lat, app.Lon);
        LatLng pt2 = new LatLng(goLat, goLon);
		GetSystem.FindCar((Activity) context, pt1, pt2, "", "");
    }
}
