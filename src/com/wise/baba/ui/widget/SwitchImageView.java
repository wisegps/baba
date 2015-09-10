package com.wise.baba.ui.widget;

import com.wise.baba.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

public class SwitchImageView extends ImageView {

	
	private boolean isChecked =false;
	
	private int default_img_id;
	private int checked_img_id;
	public SwitchImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initImgById();
	}

	public SwitchImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initImgById();
	}

	

	public SwitchImageView(Context context) {
		super(context);
		initImgById();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return super.onTouchEvent(event);
	}
	
	public boolean isChecked() {
		return isChecked;
	}
	
	

	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
		if(isChecked){
			this.setImageResource(checked_img_id);
		}else{
			this.setImageResource(default_img_id);
		}
	}
	
	
	public void initImgById(){
		
		switch(this.getId()){
		
		case R.id.iv_air_power:
			default_img_id = R.drawable.ico_air_power_off;
			checked_img_id = R.drawable.ico_air_power;
			break;
		
		case R.id.iv_air_auto:
			default_img_id = R.drawable.ico_air_auto_off;
			checked_img_id = R.drawable.ico_air_auto_on;
			break;
		
		case R.id.iv_air_setting:
			default_img_id = R.drawable.ico_air_setting_off;
			checked_img_id = R.drawable.ico_air_setting_on;
			break;
		case R.id.iv_air_level:
			default_img_id = R.drawable.ico_air_level_off;
			checked_img_id = R.drawable.ico_air_level_on;
			break;
		}
	}
	

}
