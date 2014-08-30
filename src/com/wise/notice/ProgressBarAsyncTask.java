package com.wise.notice;

import android.os.AsyncTask;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ProgressBarAsyncTask extends AsyncTask<Integer, Integer, String> {
	
	 private TextView textView;  
	 private ProgressBar progressBar; 
	
	public ProgressBarAsyncTask(TextView textView, ProgressBar progressBar) {
		super();
		this.textView = textView;
		this.progressBar = progressBar;
	}

	@Override
	protected String doInBackground(Integer... params) {
		// TODO Auto-generated method stub
		return null;
	}
}
