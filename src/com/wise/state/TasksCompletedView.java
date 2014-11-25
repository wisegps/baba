package com.wise.state;

import com.wise.baba.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author naiyu(http://snailws.com)
 * @version 1.0
 */
public class TasksCompletedView extends View {
	private Paint p;
	// 画圆环的画笔
	private Paint mRingPaint;
	// 画圆环背景的画笔
	private Paint mRingBgPaint;
	// 圆形颜色
	private int mCircleColor;
	// 圆环颜色
	private int mRingColor;
	// 半径
	private float mRadius;
	// 圆环半径
	private float mRingRadius;
	// 圆环宽度
	private float mStrokeWidth;
	// 圆心x坐标
	private int mXCenter;
	// 圆心y坐标
	private int mYCenter;
	// 总进度
	private int mTotalProgress = 100;
	// 当前进度
	private int mProgress;
	
	boolean isGreen = false;

	public TasksCompletedView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// 获取自定义的属性
		initAttrs(context, attrs);
		initVariable();
	}

	private void initAttrs(Context context, AttributeSet attrs) {
		TypedArray typeArray = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.TasksCompletedView, 0, 0);
		mRadius = typeArray.getDimension(R.styleable.TasksCompletedView_radius, 80);
		mStrokeWidth = typeArray.getDimension(R.styleable.TasksCompletedView_strokeWidth, 10);
		mCircleColor = typeArray.getColor(R.styleable.TasksCompletedView_circleColor, 0xFFFFFFFF);
		mRingColor = typeArray.getColor(R.styleable.TasksCompletedView_ringColor, 0xFFFFFFFF);
		
		mRingRadius = mRadius + mStrokeWidth / 2;
	}

	private void initVariable() {
		p = new Paint();
		p.setAntiAlias(true);
		
		mRingBgPaint = new Paint();
		mRingBgPaint.setAntiAlias(true);
		mRingBgPaint.setColor(mCircleColor);
		mRingBgPaint.setStyle(Paint.Style.STROKE);
		mRingBgPaint.setStrokeWidth(mStrokeWidth);
		
		mRingPaint = new Paint();
		mRingPaint.setAntiAlias(true);
		mRingPaint.setColor(mRingColor);
		mRingPaint.setStyle(Paint.Style.STROKE);
		mRingPaint.setStrokeWidth(mStrokeWidth);	
	}
	public void setRingColor(int resId,boolean isGreen){
		isGreen = true;
		mRingColor = resId;
		initVariable();
		postInvalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {

		mXCenter = getWidth() / 2;
		mYCenter = getHeight() / 2;
		
		if (mProgress >= 0 ) {
			RectF oval = new RectF();
			oval.left = (mXCenter - mRingRadius);
			oval.top = (mYCenter - mRingRadius);
			oval.right = mRingRadius * 2 + (mXCenter - mRingRadius);
			oval.bottom = mRingRadius * 2 + (mYCenter - mRingRadius);
			if(!isGreen){
				int[] color = colors[indexOf(mProgress)];
				mRingBgPaint.setColor(Color.argb(76,color[0], color[1], color[2]));
				mRingPaint.setColor(Color.rgb(color[0], color[1], color[2]));
			}
			canvas.drawCircle(mXCenter, mYCenter, mRingRadius, mRingBgPaint);
			canvas.drawArc(oval, -90, ((float)mProgress / mTotalProgress) * 360, false, mRingPaint);
			if(isPress){
				p.setColor(0x4cd8d9e1);
			}else{
				p.setColor(Color.WHITE);
			}		
			canvas.drawCircle(mXCenter, mYCenter, mRadius, p);	
		}
	}	
	public void setProgress(int progress,boolean is) {
		isGreen = is;
		mProgress = progress;
		postInvalidate();
	}
	
	public void setProgress(int progress) {
		isGreen = false;
		mProgress = progress;
		postInvalidate();
	}
	
	private int indexOf(int progress){
		return (mTotalProgress - progress)/3;
	}
	private boolean isPress = false;
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			isPress = true;
			break;
		case MotionEvent.ACTION_UP:
			isPress = false;
			break;
		case MotionEvent.ACTION_CANCEL:
			isPress = false;
			break;
		}
		postInvalidate();
		return super.onTouchEvent(event);
	}
	
	
	int[][] colors = {
			{83,181,220},
			{87,185,201},
			{91,189,185},
			{99,192,163},
			{106,195,149},
			{111,197,134},
			{116,199,117},
			{122,203,100},
			{126,206,85},
			{133,208,66},
			{142,205,65},
			{156,197,69},
			{171,188,73},
			{185,182,79},			
			{206,170,82},
			{221,165,86},			
			{235,156,90},
			{241,152,92},
			{245,149,91},
			{245,145,93},			
			{245,143,94},
			{247,140,94},
			{247,138,97},
			{248,136,98},			
			{247,133,96},
			{249,130,96},
			{248,127,96},
			{250,126,98},			
			{252,123,101},
			{251,120,102},
			{251,118,101},
			{252,116,102},
			{254,106,104},
			{255,105,105}
	};
	int[][] colors1 = {
			{83,181,220},
			{85,183,218},
			{87,185,201},
			{89,187,193},
			{91,189,185},			
			{98,190,175},
			{99,192,163},
			{102,194,157},
			{106,195,149},			
			{108,195,140},
			{111,197,134},
			{113,199,124},
			{116,199,117},			
			{118,201,109},
			{122,203,100},
			{123,202,93},
			{126,206,85},			
			{130,207,77},
			{133,208,66},
			{135,208,65},
			{142,205,65},			
			{152,199,67},
			{156,197,69},
			{163,193,71},
			{171,188,73},			
			{177,185,76},
			{185,182,79},
			{193,177,79},
			{199,174,81},			
			{206,170,82},
			{216,165,84},
			{221,165,86},
			{228,159,90},			
			{235,156,90},
			{241,152,92},
			{245,149,91},
			{245,145,93},			
			{245,143,94},
			{247,140,94},
			{247,138,97},
			{248,136,98},			
			{247,133,96},
			{249,130,96},
			{248,127,96},
			{250,126,98},			
			{252,123,101},
			{251,120,102},
			{251,118,101},
			{252,116,102},
			{254,106,104},
			{255,105,105}
	};
}