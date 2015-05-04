package com.wise.baba.ui.widget;

import com.wise.baba.R;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**滚动等待效果**/
public class WaitLinearLayout extends LinearLayout{
	
	ImageView iv_wheel,iv_shadow;
	int leftWidth = 100;
	/**滚动到一定位置后暂停**/
	int wait = 50;
	Context mContext;
	
	public WaitLinearLayout(Context context){
		this(context,null);
		init();
	}
	public WaitLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}
	private void init(){
		LayoutInflater.from(mContext).inflate(R.layout.view_wait, this, true);
		iv_wheel = (ImageView)findViewById(R.id.iv_wheel);
		iv_shadow = (ImageView)findViewById(R.id.iv_shadow);	
	}
	/**设置轮子图片**/
	public void setWheelImage(int resId){
		iv_wheel.setImageResource(resId);
	}
	/**设置阴影图片**/
	public void setShadowImage(int resId){
		iv_shadow.setImageResource(resId);
	}
	/**开始滚动**/
	public void startWheel(){
		Scoll();
	}
	int index = 0;
	/**开始滚动**/
	public void startWheel(int index){
		this.index = index;
		Scoll();
	}
	private void Scoll(){
		Animation operatingAnim = AnimationUtils.loadAnimation(mContext, R.anim.tip);  
        LinearInterpolator lin = new LinearInterpolator();  
        operatingAnim.setInterpolator(lin); 
        if (operatingAnim != null) { 
        	if (isInEditMode()) { return; }
        	iv_wheel.startAnimation(operatingAnim);  
        }
        isFirst = true;
        isOk = false;
        isRun = true;
		new MyThread().start();
	}
	/**数据为加载完毕**/
	boolean isOk = false;
	public void runFast(){
		Animation operatingAnim = AnimationUtils.loadAnimation(mContext, R.anim.tips);  
        LinearInterpolator lin = new LinearInterpolator();  
        operatingAnim.setInterpolator(lin); 
        if (operatingAnim != null) {  
        	iv_wheel.startAnimation(operatingAnim);  
        }
		spanTime = 10;
		isOk = true;
		isSend = true;
		
	}
	public void runFast(int index){
		this.index = index;
		Animation operatingAnim = AnimationUtils.loadAnimation(mContext, R.anim.tips);  
        LinearInterpolator lin = new LinearInterpolator();  
        operatingAnim.setInterpolator(lin); 
        if (operatingAnim != null) {  
        	iv_wheel.startAnimation(operatingAnim);  
        }
		spanTime = 10;
		isOk = true;
		isSend = true;
		
	}
	/**重置view**/
	public void refreshView(){
		i = 0;
		MarginLayoutParams params = (MarginLayoutParams) iv_wheel.getLayoutParams();
		params.leftMargin = 0;
		iv_wheel.setLayoutParams(params);
	}
	/**线程不能及时关闭，会发触发3个同样事件**/
	boolean isFirst = true;
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				if(isOk){
					if(i > leftWidth){
						iv_wheel.clearAnimation();
						isRun = false;
						if(onFinishListener != null){
							if(isFirst){
								onFinishListener.OnFinish(index);
								isFirst = false;
							}
						}
					}else{
						i ++;
						MarginLayoutParams params = (MarginLayoutParams) iv_wheel.getLayoutParams();
						params.leftMargin = i;
						iv_wheel.setLayoutParams(params);
					}
				}else{
					if(i > wait){
						isSend = false;
					}else{
						i ++;
						MarginLayoutParams params = (MarginLayoutParams) iv_wheel.getLayoutParams();
						params.leftMargin = i;
						iv_wheel.setLayoutParams(params);
					}
				}				
				break;
			}
		}		
	};
	boolean isSend = true;
	/**当前位置**/
	int i = 0 ;
	/**移动的间隔时间*/
	int spanTime = 100;
	boolean isRun = true;
	class MyThread extends Thread{
		@Override
		public void run() {
			super.run();
			while (isRun) {
				try {
					if(isSend){
						Message message = new Message();
						message.what = 1;
						handler.sendMessage(message);
					}
					sleep(spanTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	OnFinishListener onFinishListener;
	public void setOnFinishListener(OnFinishListener onFinishListener){
		this.onFinishListener = onFinishListener;
	}
	public interface OnFinishListener{
		public void OnFinish(int index);
	}
}