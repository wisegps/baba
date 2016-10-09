package versionupdata;

import android.os.Handler;
import android.os.Looper;

import com.android.volley.Response;

public abstract class OnSuccess implements Response.Listener<String>{
	
	protected abstract void onSuccess(String response);
	private static Handler mHandler = new Handler(Looper.getMainLooper());
	
	@Override
	public void onResponse(final String response) {
		mHandler.post(new Runnable() {
			
			@Override
			public void run() {
				onSuccess(response);
			}
		});
	}

}
