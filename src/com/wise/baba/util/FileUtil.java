package com.wise.baba.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class FileUtil {
	public Context context;
	public static String externalCacheDir = null;
	public static String JSON_PATH = "/";

	public FileUtil(Context context) {
		super();
		this.context = context;
	}

	/**
	 * @return 获取外部存储器缓存目录
	 */
	public String getSDCacheDir() {
		if (externalCacheDir != null) {
			return externalCacheDir;
		}
		String sdState = Environment.getExternalStorageState();
		if (sdState.equals(Environment.MEDIA_MOUNTED)) {
			File cacheDir = context.getExternalCacheDir();
			externalCacheDir = cacheDir.getAbsolutePath();
		}
		return externalCacheDir;
	}

	/**
	 * 存储json字符串
	 * 
	 * @param fileName
	 * @param json
	 */
	public void putJson(String fileName, String json) {
		String filePath = getSDCacheDir() + JSON_PATH + fileName;
		Log.i("TrafficActivity", "putJson json " + filePath);
		File file = new File(filePath);
		
		FileOutputStream fos = null;
		OutputStreamWriter osw = null;
		if (file.exists()) {
			file.delete();
		}
		try {
			file.createNewFile();
			fos = new FileOutputStream(file);
			osw =  new OutputStreamWriter(fos,"utf-8");
			osw.write(json);
			osw.flush();
			osw.close();
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取本地数据
	 * @param fileName
	 * @return
	 */
	public String getJson(String fileName) {
		String filePath = getSDCacheDir() + JSON_PATH + fileName;
		Log.i("TrafficActivity", "getJson json " + filePath);
		File file = new File(filePath);
		if (!file.exists()) {
			return "";
		}
		try {
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis,"utf-8");
			
			BufferedReader bufReader =new BufferedReader(isr); 
			StringBuffer strBuffer = new StringBuffer(); 
			String strRead = "";
			while((strRead = bufReader.readLine()) != null){
				strBuffer.append(strRead);
			};
			fis.close();
			return strBuffer.toString();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "";
	}

}
