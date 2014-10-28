package com.wise.car;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import com.baidu.lbsapi.panoramaview.*;
import com.baidu.lbsapi.BMapManager;
import com.baidu.lbsapi.MKGeneralListener;
import com.wise.baba.R;

/**
 * 全景Demo主Activity
 */
public class PanoramaDemoActivityMain extends Activity{
   
    private PanoramaView mPanoView;
    BMapManager manager;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        manager = new BMapManager(getApplicationContext());
		manager.init(new MKGeneralListener() {			
			@Override
			public void onGetPermissionState(int iError) {
				System.out.println("manager初始化");
			}
		});
        setContentView(R.layout.activity_panorama_main);
        mPanoView = (PanoramaView) findViewById(R.id.panorama);
        mPanoView.setShowTopoLink(true);
        mPanoView.setPanoramaLevel(1);
        
        Intent intent = getIntent();
        double lon = intent.getDoubleExtra("lon", 0);
        double lat = intent.getDoubleExtra("lat", 0);
        System.out.println("lat = " + lat + " , lon = " + lon);
		mPanoView.setPanorama(lon, lat);   
    }    
    @Override
    protected void onPause() {
        super.onPause();
        mPanoView.onPause();
    }
    @Override
    protected void onResume() {
        super.onResume();
        mPanoView.onResume();
    }
    @Override
    protected void onDestroy() {
        mPanoView.destroy();
        super.onDestroy();
    } 
}