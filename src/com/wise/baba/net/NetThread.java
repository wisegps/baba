package com.wise.baba.net;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.wise.baba.biz.GetSystem;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * 多线程操作类
 * @author honesty
 */
public class NetThread {
	static String TAG = "NetThread";
	/**
	 * Get获取数据
	 * @author honesty
	 */
	public static class GetDataThread extends Thread{
		Handler handler;
		String url;
		int what;
		int index = 0;
		/**
		 * Get获取数据
		 * @param handler
		 * @param url
		 * @param what
		 */
		public GetDataThread(Handler handler,String url,int what){
			this.handler = handler;
			this.url = url;
			this.what =what;
		}
		public GetDataThread(Handler handler,String url,int what,int index){
			this.handler = handler;
			this.url = url;
			this.what = what;
			this.index = index;
		}
		@Override
		public void run() {
			super.run();
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
					
					Message message = new Message();
					message.what = what;
					message.obj = data;
					message.arg1 = index;
					handler.sendMessage(message);
				}else{
					Log.d(TAG, "URLConnection");
					Message message = new Message();
					message.what = what;
					message.obj = "";
					message.arg1 = index;
					handler.sendMessage(message);
				}
			}catch (Exception e) {
				e.printStackTrace();
				Message message = new Message();
				message.what = what;
				message.obj = "";
				message.arg1 = index;
				handler.sendMessage(message);
			}
		}
	}
	
	public static class putDataThread extends Thread{
		Handler handler;
		String url;
		int what;
		int index = 0;
		
		List<NameValuePair> parms;
		public putDataThread(Handler handler,String url,List<NameValuePair> parms,int what){
			this.handler = handler;
			this.url = url;
			this.what =what;
			this.parms = parms;
		}
		public putDataThread(Handler handler,String url,List<NameValuePair> parms,int what,int index){
			this.handler = handler;
			this.url = url;
			this.what = what;
			this.index = index;
			this.parms = parms;
		}
		@Override
		public void run() {
			super.run();
			try {
				GetSystem.myLog(TAG, url);
				BasicHttpParams httpParams = new BasicHttpParams();  
			    HttpConnectionParams.setConnectionTimeout(httpParams, 10000);  
			    HttpConnectionParams.setSoTimeout(httpParams, 10000); 
				HttpClient client = new DefaultHttpClient(httpParams);
		        HttpPut httpPut = new HttpPut(url);	
		        if(parms != null){
		        	httpPut.setEntity(new UrlEncodedFormEntity(parms,HTTP.UTF_8));
		        }
		        HttpResponse response = client.execute(httpPut); 
		        if (response.getStatusLine().getStatusCode()  == 200){
		        	HttpEntity entity = response.getEntity();
					BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
					StringBuilder sb = new StringBuilder();
					String line = "";
					while ((line = reader.readLine()) != null) {
						sb.append(line);
					}
					Message message = new Message();
					message.what = what;
					message.arg1 = index;
					message.obj = sb.toString();
					handler.sendMessage(message);
		        }else{
		        	Message message = new Message();
					message.what = what;
					message.arg1 = index;
					message.obj = "";
					handler.sendMessage(message);
		        }
			} catch (Exception e) {
				e.printStackTrace();	
				Message message = new Message();
				message.what = what;
				message.arg1 = index;
				message.obj = "";
				handler.sendMessage(message);
			}
		}
	}
	
	public static class DeleteThread extends Thread{
		Handler handler;
		String url;
		int what;		
		public DeleteThread(Handler handler,String url,int what){
			this.handler = handler;
			this.url = url;
			this.what =what;
		}
		@Override
		public void run() {
			super.run();
			try {
				HttpClient client = new DefaultHttpClient();
				HttpDelete httpDelete = new HttpDelete(url);
		        HttpResponse response = client.execute(httpDelete); 
		        if (response.getStatusLine().getStatusCode()  == 200){
		        	HttpEntity entity = response.getEntity();
					BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
					StringBuilder sb = new StringBuilder();
					String line = null;
					while ((line = reader.readLine()) != null) {
						sb.append(line);
					}
					Message message = new Message();
					message.what = what;
					message.obj = sb.toString();
					handler.sendMessage(message);
		        }else{
		        	Message message = new Message();
					message.what = what;
					message.obj = "";
					handler.sendMessage(message);
		        }
			} catch (Exception e) {
				e.printStackTrace();	
				Message message = new Message();
				message.what = what;
				message.obj = "";
				handler.sendMessage(message);
			}
		}
	}
	
	public static class postDataThread extends Thread{
		Handler handler;
		String url;
		int what;
		int index = 0 ;
		List<NameValuePair> params;
		public postDataThread(Handler handler,String url,List<NameValuePair> params,int what,int index){
		    this.handler = handler;
			this.url = url;
			this.what = what;
			this.params = params;
			this.index = index;
		}
		public postDataThread(Handler handler,String url,List<NameValuePair> params,int what){
			this.handler = handler;
			this.url = url;
			this.what = what;
			this.params = params;
		}
		@Override
		public void run() {
			super.run();
			GetSystem.myLog(TAG, url);
			HttpPost httpPost = new HttpPost(url);
			try {
				 httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
				 HttpClient client = new DefaultHttpClient();
				 client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 20000);
				 client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 20000);
				 HttpResponse httpResponse = client.execute(httpPost);
				 Log.d(TAG, "状态" +httpResponse.getStatusLine().getStatusCode());
				 if(httpResponse.getStatusLine().getStatusCode() == 200){
					 String strResult = EntityUtils.toString(httpResponse.getEntity());
					 Message message = new Message();
					 message.what = what;
					 message.arg1 = index;
					 message.obj = strResult;
					 handler.sendMessage(message);	
				 }else{					 
					 Message message = new Message();
					 message.what = what;
					 message.arg1 = index;
					 message.obj = "";
					 handler.sendMessage(message);
				 }
			} catch (Exception e) {
				Message message = new Message();
				message.what = what;
				message.arg1 = index;
				message.obj = "";
				handler.sendMessage(message);			
				e.printStackTrace();
			}
		}
	}
	/**post(非线程)**/
	public static String postData(String url,List<NameValuePair> params){
		GetSystem.myLog(TAG, url);
		HttpPost httpPost = new HttpPost(url);
		try {
			 httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			 HttpClient client = new DefaultHttpClient();
			 client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 20000);
			 client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 20000);
			 HttpResponse httpResponse = client.execute(httpPost);
			 Log.d(TAG, "状态" +httpResponse.getStatusLine().getStatusCode());
			 if(httpResponse.getStatusLine().getStatusCode() == 200){
				 String strResult = EntityUtils.toString(httpResponse.getEntity());
				 return strResult;	
			 }else{					 
				 return "";
			 }
		} catch (Exception e) {
			return "";
		}
	}
}