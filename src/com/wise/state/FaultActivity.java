package com.wise.state;

import com.wise.baba.R;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.ScrollView;
/**
 * 车况信息
 * @author honesty
 *
 */
public class FaultActivity extends Activity{
	private ScrollView scrollview;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_state);
		scrollview = (ScrollView) findViewById(R.id.scrollView);
        scrollview.setVerticalScrollBarEnabled(false);
        LooperThread mClockThread = new LooperThread();
        mClockThread.start();
	}
	
	class TestHandler extends Handler {
        public TestHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {

            Bundle bundle = msg.getData();
            int loop = bundle.getInt("loop");
            System.out.println("loop:" + loop);
            if (loop == 4)
                scrollview.scrollTo(0, 0);// 改变滚动条的位置
            else
                scrollview.scrollTo(loop * 29, loop * 29 + 30 + loop * 1);// 改变滚动条的位置
            super.handleMessage(msg);
        }
    }
	
	class LooperThread extends Thread {

        public void run() {
            super.run();
            try {
                int loop = 0;
                while (true) {
                    Thread.sleep(3000);
                    Message msg = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putInt("loop", loop);
                    msg.setData(bundle);
                    new TestHandler(Looper.getMainLooper()).sendMessage(msg);
                    loop++;
                    if (loop == 5) {
                        loop = 0;
                    }
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
	
}