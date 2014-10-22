package pubclas;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.navisdk.R.string;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class GetLocation {
	Context mContext;
	public LocationClient mLocationClient = null;
	public BDLocationListener myListener = new MyLocationListener();
	
	public GetLocation(){}
	
	public GetLocation(Context context){
		mContext = context;
		mLocationClient = new LocationClient(mContext); // 声明LocationClient类
		mLocationClient.registerLocationListener(myListener); // 注册监听函数
		SetOption();
		mLocationClient.start();
	}
	
	private void SetOption() {
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);
		option.setCoorType("all");// 返回的定位结果包含地址信息
		option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度,默认值gcj02
		option.setIsNeedAddress(true);
		option.setScanSpan(60000);// 设置发起定位请求的间隔时间为5000ms
		mLocationClient.setLocOption(option);
	}
	private static final String TAG = "MyLocationListener";
	class MyLocationListener implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null)
				return;
			Log.d(TAG, "定位成功");
			StringBuffer sb = new StringBuffer(256);
			sb.append("time : ");
			sb.append(location.getTime());
			sb.append("\nerror code : ");
			sb.append(location.getLocType());
			sb.append("\nlatitude : ");
			sb.append(location.getLatitude());
			sb.append("\nlontitude : ");
			sb.append(location.getLongitude());
			sb.append("\nradius : ");
			sb.append(location.getRadius());			
			Variable.Lat = location.getLatitude();
			Variable.Lon = location.getLongitude();
			if (location.getLocType() == BDLocation.TypeGpsLocation) {
				sb.append("\nspeed : ");
				sb.append(location.getSpeed());
				sb.append("\nsatellite : ");
				sb.append(location.getSatelliteNumber());
			} else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
				sb.append("\naddr : ");
				sb.append(location.getAddrStr());
				sb.append("\ncity: ");
				sb.append(location.getCity());
				sb.append("\nProvince: " + location.getProvince());
				Log.d(TAG, "定位成功 = " + sb.toString());
				String city = location.getCity();
				String province = location.getProvince();
				Variable.Adress = location.getAddrStr();
				Variable.City = city.substring(0, city.length() - 1);
				Variable.Province = province.substring(0, province.length() - 1);
	            //发送定位城市广播，选择城市用到
	            Intent intent = new Intent(Constant.A_City);
	            intent.putExtra("City", city.substring(0, city.length() - 1));
	            intent.putExtra("Province", province.substring(0, province.length() - 1));
	            intent.putExtra("AddrStr", location.getAddrStr());
	            intent.putExtra("Lat", String.valueOf(location.getLatitude()));
	            intent.putExtra("Lon", String.valueOf(location.getLongitude()));
	            mContext.sendBroadcast(intent);
			}
			Log.d("location", sb.toString());
			mLocationClient.stop();
			mLocationClient.unRegisterLocationListener(myListener);
		}
	}
}
