package com.wise.baba.biz;

import com.wise.baba.util.FileUtil;

import android.content.Context;

public class FileVoilation {
	private FileUtil fileUtil = null;
	private String fileName = "voilation";

	public FileVoilation(Context context) {
		super();
		fileUtil= new FileUtil(context);
	}

	/**
	 * 获取违章数据
	 */
	public String getVoilation(int id) {
		return fileUtil.getJson(fileName+id);
	}
	
	/**
	 * 保存违章数据
	 */
	public void putVoilation(int id,String json) {
		fileUtil.putJson(fileName+id, json);
	}

}
