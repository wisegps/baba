package com.wise.baba.biz;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import cn.sharesdk.onekeyshare.OnekeyShare;

import com.baidu.mapapi.model.LatLng;
import com.baidu.navisdk.BNaviPoint;
import com.baidu.navisdk.BaiduNaviManager;
import com.baidu.navisdk.BaiduNaviManager.OnStartNavigationListener;
import com.baidu.navisdk.comapi.routeplan.RoutePlanParams.NE_RoutePlan_Mode;
import com.wise.baba.BNavigatorActivity;
import com.wise.baba.R;
import com.wise.baba.app.Constant;
import com.wise.baba.entity.TimeData;


public class GetSystem {
	private static final String TAG = "GetSystem";

	public static void myLog(String TAG, String Message) {
		if (Constant.isLog) {
			Log.d(TAG, Message);
		}
	}

	/** 获取当前时间 **/
	public static String GetNowTime() {
		Time time = new Time();
		time.setToNow();
		String year = ChangeTime(time.year);
		String month = ChangeTime(time.month + 1);
		String day = ChangeTime(time.monthDay);
		String minute = ChangeTime(time.minute);
		String hour = ChangeTime(time.hour);
		String sec = ChangeTime(time.second);
		String str = year + "-" + month + "-" + day + " " + hour + ":" + minute
				+ ":" + sec;
		return str;
	}

	/** 返回当前日期 **/
	public static String GetNowDay() {
		Time time = new Time();
		time.setToNow();
		String year = ChangeTime(time.year);
		String month = ChangeTime(time.month + 1);
		String day = ChangeTime(time.monthDay);
		String str = year + "-" + month + "-" + day;
		return str;
	}

	/**
	 * 返回当前月份
	 * 
	 * @return
	 */
	public static TimeData GetNowMonth() {
		Time time = new Time();
		time.setToNow();
		String year = ChangeTime(time.year);
		String month = ChangeTime(time.month + 1);
		String day = ChangeTime(time.monthDay);
		String s_month = year + "-" + month;
		String s_day = year + "-" + month + "-" + day;
		TimeData timeData = new TimeData();
		timeData.setYear(year);
		timeData.setMonth(s_month);
		timeData.setDay(s_day);
		return timeData;
	}

	/**
	 * 调整时间格式
	 * 
	 * @param 9
	 * @return 09
	 */
	public static String ChangeTime(int i) {
		String str = null;
		if (i < 10) {
			str = "0" + i;
		} else {
			str = "" + i;
		}
		return str;
	}

	/** 车务提醒 **/
	public static int getWeekOfDate(String Date) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(sdf.parse(Date));
			int intWeek = calendar.get(Calendar.DAY_OF_WEEK) - 2;
			if (intWeek == -2) {
				return 5;
			} else if (intWeek == -1) {
				return 6;
			} else {
				return intWeek;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 解决时区问题
	 * 
	 * @param Date
	 * @return
	 */
	public static String ChangeTimeZone(String Date) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Calendar nowDate = Calendar.getInstance();
			nowDate.setTime(sdf.parse(Date));
			nowDate.add(Calendar.HOUR_OF_DAY, 8);
			String Date1 = sdf.format(nowDate.getTime());
			return Date1;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	/** 获取某一天在这个星期的起始和结束时间 **/
	public static String[] getWeek(String str) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Calendar cal = Calendar.getInstance();
			cal.setTime(sdf.parse(str));
			int dayofweek = cal.get(Calendar.DAY_OF_WEEK) - 1;
			if (dayofweek == 0)
				dayofweek = 7;
			cal.add(Calendar.DATE, -dayofweek + 1);
			String[] week = new String[7];
			week[0] = sdf.format(cal.getTime());
			cal.add(Calendar.DATE, 1);
			week[1] = sdf.format(cal.getTime());
			cal.add(Calendar.DATE, 1);
			week[2] = sdf.format(cal.getTime());
			cal.add(Calendar.DATE, 1);
			week[3] = sdf.format(cal.getTime());
			cal.add(Calendar.DATE, 1);
			week[4] = sdf.format(cal.getTime());
			cal.add(Calendar.DATE, 1);
			week[5] = sdf.format(cal.getTime());
			cal.add(Calendar.DATE, 1);
			week[6] = sdf.format(cal.getTime());

			// WeekData weekData = new WeekData();
			// weekData.setFristDay(sdf.format(cal.getTime()));
			// cal.add(Calendar.DATE, 6);
			// weekData.setLastDay(sdf.format(cal.getTime()));
			return week;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取指定月份
	 * 
	 * @param Month
	 *            2013-12
	 * @param number
	 *            上个月填-1 ,下个月填1
	 * @return
	 */
	public static TimeData GetNextMonth(String Month, int number) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
			Calendar nowDate = Calendar.getInstance();
			nowDate.setTime(sdf.parse(Month));
			nowDate.add(Calendar.MONTH, number);
			String Date = sdf.format(nowDate.getTime());
			TimeData timeData = new TimeData();
			timeData.setYear("" + nowDate.get(Calendar.YEAR));
			timeData.setMonth(Date);
			return timeData;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 获取某月最后一天
	 * 
	 * @param Month
	 * @return
	 */
	public static String getMonthLastDay(String Month) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
			Calendar nowDate = Calendar.getInstance();
			nowDate.setTime(sdf.parse(Month));
			nowDate.get(Calendar.YEAR);

			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.YEAR, nowDate.get(Calendar.YEAR));
			calendar.set(Calendar.MONTH, nowDate.get(Calendar.MONTH));
			int endday = calendar.getActualMaximum(calendar.DAY_OF_MONTH);
			return Month + "-" + endday;
		} catch (Exception e) {
			// TODO: handle exception
		}

		return Month + "-31";
	}

	/**
	 * 获取指定天
	 * 
	 * @param Date
	 *            2013-12-01
	 * @param number
	 *            前一天填-1 ,后一天填1
	 * @return
	 */
	public static String GetNextData(String Date, int number) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Calendar nowDate = Calendar.getInstance();
			nowDate.setTime(sdf.parse(Date));
			nowDate.add(Calendar.DATE, number);
			String newDate = sdf.format(nowDate.getTime());
			return newDate;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String GetNextYear(String Date, int year) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Calendar nowDate = Calendar.getInstance();
			nowDate.setTime(sdf.parse(Date));
			nowDate.add(Calendar.YEAR, year);
			String newDate = sdf.format(nowDate.getTime());
			return newDate;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 格式化显示时间，今天，昨天，日期
	 * 
	 * @param time
	 *            2014-02-10 06:17
	 * @return 数组：0 日期，1 时间
	 */
	public static String[] formatDateTime(String time) {
		SimpleDateFormat format = new java.text.SimpleDateFormat(
				"yyyy-MM-dd HH:mm");
		if (time == null || "".equals(time)) {
			return null;
		}
		Date date = null;
		try {
			date = format.parse(time);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		Calendar current = Calendar.getInstance();

		Calendar today = Calendar.getInstance(); // 今天

		today.set(Calendar.YEAR, current.get(Calendar.YEAR));
		today.set(Calendar.MONTH, current.get(Calendar.MONTH));
		today.set(Calendar.DAY_OF_MONTH, current.get(Calendar.DAY_OF_MONTH));
		// Calendar.HOUR——12小时制的小时数 Calendar.HOUR_OF_DAY——24小时制的小时数
		today.set(Calendar.HOUR_OF_DAY, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND, 0);

		Calendar yesterday = Calendar.getInstance(); // 昨天

		yesterday.set(Calendar.YEAR, current.get(Calendar.YEAR));
		yesterday.set(Calendar.MONTH, current.get(Calendar.MONTH));
		yesterday.set(Calendar.DAY_OF_MONTH,
				current.get(Calendar.DAY_OF_MONTH) - 1);
		yesterday.set(Calendar.HOUR_OF_DAY, 0);
		yesterday.set(Calendar.MINUTE, 0);
		yesterday.set(Calendar.SECOND, 0);

		current.setTime(date);
		String[] myDate = new String[3];
		if (current.after(today)) {
			myDate[0] = "今天";
			myDate[1] = time.split(" ")[1];
			myDate[2] = "今天";
		} else if (current.before(today) && current.after(yesterday)) {
			myDate[0] = "昨天";
			myDate[1] = time.split(" ")[1];
			myDate[2] = "昨天";
		} else {
			myDate[0] = time.substring(0, 10);
			myDate[1] = time.split(" ")[1];
			myDate[2] = time.substring(5, 10);
		}
		return myDate;
	}

	public static String jsTime(int Second) {
		if (Second > 60 * 60 * 24) {
			return "" + Second / (60 * 60 * 24);
		} else {
			return "1";
		}
	}

	/**
	 * 计算间隔时间
	 * 
	 * @param Second
	 * @return
	 */
	public static String ProcessTime(int Second) {
		if (Second < 60) {
			// 小于60秒
			return "1分";
		} else if (Second < 60 * 60) {
			// 小于一个小时
			return (Second / 60) + "分钟";
		} else {
			return (Second / 3600) + "小时";
		}
	}

	public static int spacingTime(String lastTime, String nextTime) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			java.util.Date begin = sdf.parse(lastTime);
			java.util.Date end = sdf.parse(nextTime);
			int l = (int) ((end.getTime() - begin.getTime()) / 1000);
			return l;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/** 获取距当前时间间隙 **/
	public static int spacingNowTime(String Data) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			java.util.Date begin = sdf.parse(Data);
			java.util.Date end = sdf.parse(GetNowTime());
			int l = (int) ((end.getTime() - begin.getTime()) / 1000);
			return l;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 60;
	}

	/** 实际显示的时间 **/
	public static String showData(int Second, String Data) {
		if (Second < 60) {// 小于60秒
			return Second + "秒前";
		} else if (Second < 60 * 60) {// 小于一个小时
			return (Second / 60) + "分钟前";
		} else if (Second < 60 * 60 * 24) {
			return (Second / 3600) + "小时前";
		} else if (Second < 60 * 60 * 24 * 30) {
			return (Second / 86400) + "天前";
		}
		return Data.substring(0, 10);
	}

	/**
	 * 首页时间显示
	 * 
	 * @param time
	 * @return
	 */
	public static String sortHomeTime1(String time) {
		if (time == null || time.equals("")) {
			return "";
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			java.util.Date begin = sdf.parse(time);
			java.util.Date end = sdf.parse(GetNowTime());
			int l = (int) ((end.getTime() - begin.getTime()) / 1000);
			if (l > (60 * 60 * 24)) {
				return "更新于" + time.substring(0, 10);
			} else {
				return ProcessTime(l) + "前更新";
			}
		} catch (ParseException e) {
			e.printStackTrace();
			return "";
		}
	}

	public static String getTime(String sk_time) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			Time time = new Time();
			time.setToNow();
			String minute = ChangeTime(time.minute);
			String hour = ChangeTime(time.hour);
			java.util.Date begin = sdf.parse("2014-08-06 " + sk_time + ":00");
			java.util.Date end = sdf.parse("2014-08-06 " + hour + ":" + minute
					+ ":00");
			int l = (int) ((end.getTime() - begin.getTime()) / 1000);
			return ProcessTime(Math.abs(l)) + "前更新";
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 调用百度地图导航
	 * 
	 * @param mActivity
	 * @param pt1
	 * @param pt2
	 * @param str1
	 * @param str2
	 */
	public static void FindCar(final Activity mActivity, LatLng pt1,
			final LatLng pt2, String str1, String str2) {
		if (pt1 == null || pt2 == null) {
			Toast.makeText(mActivity, "坐标错误,无法开启导航", Toast.LENGTH_SHORT).show();
			return;
		}
		BNaviPoint startPoint = new BNaviPoint(pt1.longitude, pt1.latitude, "",
				BNaviPoint.CoordinateType.BD09_MC);
		BNaviPoint endPoint = new BNaviPoint(pt2.longitude, pt2.latitude, "",
				BNaviPoint.CoordinateType.BD09_MC);
		BaiduNaviManager.getInstance().launchNavigator(mActivity, startPoint,
				endPoint, NE_RoutePlan_Mode.ROUTE_PLAN_MOD_MIN_TIME, true,
				BaiduNaviManager.STRATEGY_FORCE_ONLINE_PRIORITY,
				new OnStartNavigationListener() {

					@Override
					public void onJumpToNavigator(Bundle arg0) {
						Intent intent = new Intent(mActivity,
								BNavigatorActivity.class);
						arg0.putDouble("navLatitude", pt2.latitude);
						arg0.putDouble("navLongitude", pt2.longitude);
						intent.putExtras(arg0);
						mActivity.startActivity(intent);
					}

					@Override
					public void onJumpToDownloader() {
						// TODO Auto-generated method stub

					}
				});
	}

	/**
	 * 获取版本信息，判断时候有更新
	 * 
	 * @param context
	 * @param 包名称
	 * @return versionName，版本名称，如1.2
	 */
	public static String GetVersion(Context context, String packString) {
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
	 * 从服务器读取图片
	 * 
	 * @param src
	 * @return
	 */
	public static Bitmap getBitmapFromURL(String Path) {
		try {
			GetSystem.myLog(TAG, Path);
			URL url = new URL(Path);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			Bitmap myBitmap = BitmapFactory.decodeStream(input);
			return myBitmap;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 从服务器读取文件
	 * 
	 * @param url
	 * @return
	 */
	public static String getStringFromURL(String url) {
		try {
			URL myURL = new URL(url);
			URLConnection httpsConn = myURL.openConnection();
			httpsConn.setConnectTimeout(20 * 1000);
			httpsConn.setReadTimeout(20 * 1000);
			InputStreamReader insr = new InputStreamReader(
					httpsConn.getInputStream(), "UTF-8");
			BufferedReader br = new BufferedReader(insr, 1024);
			String data = "";
			String line = "";
			while ((line = br.readLine()) != null) {
				data += line;
			}
			insr.close();
			return data;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * 保存图片在sd卡上(jgp格式用到)
	 * 
	 * @param bitmap
	 * @param name
	 * @param quality
	 *            压缩比例
	 */
	public static void saveImageSD(Bitmap bitmap, String path, String name,
			int quality) {
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();// 创建文件夹
		}
		String fileName = path + name;
		FileOutputStream b = null;
		try {
			b = new FileOutputStream(fileName);
			if (name.substring(name.lastIndexOf(".") + 1, name.length())
					.equals("png")) {
				bitmap.compress(Bitmap.CompressFormat.PNG, quality, b);// 把数据写入文件
			} else {
				bitmap.compress(Bitmap.CompressFormat.JPEG, quality, b);// 把数据写入文件
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				b.flush();
				b.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void displayBriefMemory(Context mContext) {
		Log.e("tag", "内存" + Runtime.getRuntime().totalMemory() / 1024 / 1024
				+ "M");
	}

	/**
	 * 经纬度格式转换,把服务器得到的string转成int类型
	 * 
	 * @param string
	 *            116.000000
	 * @return 116000000
	 */
	public static int StringToInt(String str) {
		try {
			Double point_doub = Double.parseDouble(str);
			return (int) (point_doub * 1000000);
		} catch (NumberFormatException e) {
			Log.d("GetSystem", "经纬度格式转换异常：NumberFormatException");
			return 0;
		}
	}

	/**
	 * 选出最大更新时间
	 * 
	 * @param maxTime
	 * @param Time
	 * @return true maxTime大
	 */
	public static boolean maxTime(String maxTime, String Time) {
		if (maxTime.equals("")) {
			return false;
		} else {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				java.util.Date max = sdf.parse(maxTime);
				java.util.Date time = sdf.parse(Time);
				if (time.getTime() > max.getTime()) {
					return false;
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	/**
	 * 判断是否提醒
	 * 
	 * @param time
	 * @return
	 */
	public static int isTimeOut(String time) {
		if (time == null || time.equals("")) {
			return 0;
		}
		if (time.length() == 10) {
			time = time + " 00:00:00";
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			java.util.Date begin = sdf.parse(time);
			java.util.Date end = sdf.parse(GetNowTime());
			return (int) ((begin.getTime() - end.getTime()) / 1000);
		} catch (ParseException e) {
			e.printStackTrace();
			return 0;
		}
	}

	/**
	 * 分享
	 * 
	 * @param mContext
	 * @param Content
	 * @param imagePath
	 * @param Lat
	 * @param Lon
	 * @param Title
	 * @param mapUrl
	 */
	public static void share(Context mContext, String Content,
			String imagePath, float Lat, float Lon, String title,
			String titleUrl) {
		final OnekeyShare oks = new OnekeyShare();
		oks.disableSSOWhenAuthorize();
		oks.setNotification(R.drawable.ic_launcher, "app_name");
		oks.setAddress("");
		oks.setTitle(title);
		oks.setTitleUrl(titleUrl);
		oks.setText(Content + " (来自@叭叭,点击下载http://dl.bibibaba.cn/ )");
		oks.setImagePath(imagePath);
		// oks.setImageUrl("http://img.appgo.cn/imgs/sharesdk/content/2013/07/25/1374723172663.jpg");
		oks.setUrl(titleUrl);
		oks.setFilePath(imagePath);
		System.out.println("titleUrl = " + titleUrl);
		System.out.println("title = " + title);
		// qq share params must have titleUrl and (title or summary or imageUrl)

		// oks.setComment("share");
		// oks.setSite("wise");
		// oks.setSiteUrl("http://sharesdk.cn");
		// oks.setVenueName("Share SDK");
		// oks.setVenueDescription("This is a beautiful place!");
		// oks.setLatitude(Lat);
		// oks.setLongitude(Lon);
		oks.setSilent(true);
		oks.show(mContext);
	}

	// 转换时区
	public static String transform(String from) {
		String to = "";
		SimpleDateFormat simple = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// 本地时区
		Calendar nowCal = Calendar.getInstance();
		TimeZone localZone = nowCal.getTimeZone();
		// 设定SDF的时区为本地
		simple.setTimeZone(localZone);

		SimpleDateFormat simple1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// 设置 DateFormat的时间区域为GMT
		simple1.setTimeZone(TimeZone.getTimeZone("GMT"));

		// 把字符串转化为Date对象，然后格式化输出这个Date
		Date fromDate = new Date();
		try {
			// 时间string解析成GMT时间
			fromDate = simple1.parse(from);
			// GMT时间转成当前时区的时间
			to = simple.format(fromDate);
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		return to;
	}

	public static String getM5DEndo(String s) {
		if (s == null) {
			return "";
		}
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (Exception e) {
			e.printStackTrace();
			return s;
		}
		char[] charArray = s.toCharArray();
		byte[] byteArray = new byte[charArray.length];
		for (int i = 0; i < charArray.length; i++) {
			byteArray[i] = (byte) charArray[i];
		}
		byte[] md5Bytes = md5.digest(byteArray);
		StringBuffer hexValue = new StringBuffer();
		for (int i = 0; i < md5Bytes.length; i++) {
			int val = (md5Bytes[i]) & 0xff;
			if (val < 16) {
				hexValue.append("0");
			}
			hexValue.append(Integer.toHexString(val));
		}
		return hexValue.toString();
	}

	/** 把view里的内容映射城图片，地图不行 **/
	public static Bitmap getViewBitmap(View v) {
		v.clearFocus(); //
		v.setPressed(false); //
		// 能画缓存就返回false
		boolean willNotCache = v.willNotCacheDrawing();
		v.setWillNotCacheDrawing(false);
		int color = v.getDrawingCacheBackgroundColor();
		v.setDrawingCacheBackgroundColor(0);
		if (color != 0) {
			v.destroyDrawingCache();
		}
		v.buildDrawingCache();
		Bitmap cacheBitmap = v.getDrawingCache();
		if (cacheBitmap == null) {
			return null;
		}
		Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);
		v.destroyDrawingCache();
		v.setWillNotCacheDrawing(willNotCache);
		v.setDrawingCacheBackgroundColor(color);
		return bitmap;
	}

	/**
	 * 判断网络是否可用
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm == null) {
		} else {
			NetworkInfo[] info = cm.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * 判断当前网络是否wifi
	 * 
	 * @param mContext
	 * @return
	 */
	public static boolean isWifi(Context mContext) {
		ConnectivityManager connectivityManager = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetInfo != null
				&& activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
			return true;
		}
		return false;
	}
}
