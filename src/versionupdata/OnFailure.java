package versionupdata;

import android.os.Handler;
import android.os.Looper;

import com.android.volley.Response;
import com.android.volley.VolleyError;

public abstract class OnFailure implements Response.ErrorListener{

	protected abstract void onFailure(VolleyError error);
	private static Handler mHandler = new Handler(Looper.getMainLooper());
	
	@Override
	public void onErrorResponse(final VolleyError error) {
		// TODO Auto-generated method stub
		mHandler.post(new Runnable() {
			
			@Override
			public void run() {
				onFailure(error);
			}
		});
	}

}
