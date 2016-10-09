package versionupdata;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class WUtils {

    public static void showToast(Context context, String content){
    	Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
    }


	/**
	 * @param context
	 * @param packString
	 * @return
	 */
	public static String getVersion(Context context, String packString) {
		PackageManager pm = context.getPackageManager();
		try {
			PackageInfo pi = pm.getPackageInfo(packString, 0);
			return pi.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}


	/**
	 * @param context
	 * @param className
	 * @return
	 */
	public static boolean isWorked(Context context, String className) {
		ActivityManager myManager = (ActivityManager)context.getApplicationContext().getSystemService(
						Context.ACTIVITY_SERVICE);
		ArrayList<RunningServiceInfo> runningService = (ArrayList<RunningServiceInfo>) myManager
				.getRunningServices(200);

		for (int i = 0; i < runningService.size(); i++) {
			String aa = runningService.get(i).service.getClassName().toString();
			Log.i("TAG", aa);
			if (runningService.get(i).service.getClassName().toString()
					.equals(className)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return
	 */
	public static boolean isSdCardExist() {
		return android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
	}


	/**
	 * @return
	 */
	public static String getCurrentTime(){
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		String date = sf.format(new Date());
		return date;
	}

	
	// 判断当前是否使用的是 WIFI网络  
	 public static boolean isWifiActive(Context icontext){
	        Context context = icontext.getApplicationContext();    
	         ConnectivityManager connectivity = (ConnectivityManager) context    
	                .getSystemService(Context.CONNECTIVITY_SERVICE);
	         NetworkInfo[] info;
	         if (connectivity != null) {    
	              info = connectivity.getAllNetworkInfo();    
	             if (info != null) {    
	                 for (int i = 0; i < info.length; i++) {    
	                     if (info[i].getTypeName().equals("WIFI") && info[i].isConnected()) {    
	                         return true;    
	                    }    
	                }    
	             }    
	        }    
	        return false;   
	  }

}
