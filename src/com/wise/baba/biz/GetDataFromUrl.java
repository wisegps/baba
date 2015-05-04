package com.wise.baba.biz;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *@author honesty
 **/
public class GetDataFromUrl {
	private static String TAG = "GetDataFromUrl";
	/**获取数据(非线程)**/
	public static String getData(String url){
		try {
			GetSystem.myLog(TAG, url);
			URL myURL = new URL(url);
			HttpURLConnection httpsConn = (HttpURLConnection) myURL.openConnection();
			if (httpsConn != null) {
				httpsConn.setConnectTimeout(5*1000);
				httpsConn.setReadTimeout(5*1000);
		        ByteArrayOutputStream baos = new ByteArrayOutputStream();
				InputStream inputStream = httpsConn.getInputStream();
				byte[] buffer = new byte[1024];
				int len = -1;
				while((len = inputStream.read(buffer))!= -1){
				    baos.write(buffer, 0, len);
				}
				String data = new String(baos.toByteArray(), "UTF-8");				
				inputStream.close();
				return data;			
			}else{
				return "";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
		
	}
}
