package com.wise.baba.ui.widget;

import java.util.ArrayList;
import com.wise.baba.R;
import com.wise.baba.entity.EnergyItem;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * 我的车况油耗曲线
 * 
 * @author honesty
 */
public class EnergyCurveView extends View {
	/** 日，周，月 **/
	int date = 2;
	/**3是平均油耗，判断到y轴数字为0是跳过**/
	int type = 1;
	Context context;
	/** x轴长 画布宽度(去掉边距) **/
	private float realWidth = 300;
	/** 画布高度 **/
	private float realHeight = 150;
	/** 长宽的比例 **/
	private double scale = 0.6;
	/** 字体大小，用来控制画布的边距 **/
	int fontSize;
	/** 边距 **/
	int padding = 20;
	boolean isNeedX = true;
	/** y轴最高刻度 距离 y轴绘制的间距 **/
	private static final float WEIGHT = 30;
	/** 距离x轴的边距 **/
	private static final float spacing_x = 30;
	/** 距离左边的间距 **/
	private static int SPACING = 60;
	private ArrayList<PointF> points = new ArrayList<PointF>(); // 有消耗的电量时间点
	private float spacingOfX; // X间距
	private float spacingOfY; // Y间距,每一度的间距
	private EnergyItem maxEnergy = new EnergyItem(); // y坐标最大的单元

	String[] week = { "一", "二", "三", "四", "五", "六", "日" };

	public EnergyCurveView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		fontSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				14, getResources().getDisplayMetrics());
		SPACING = fontSize * 3;

		// invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Paint paint = new Paint();
		// 初始化绘制
		initDraw(canvas, paint);
	}

	int start = 1;
	int stop = 23;

	/**
	 * 初始化绘制
	 * 
	 * @param canvas
	 * @param paint
	 */
	private void initDraw(Canvas canvas, Paint paint) {
		paint.setColor(getResources().getColor(R.color.Green));
		paint.setAntiAlias(true);
		// 绘制
		float value = maxEnergy.value / 5;
		// y轴
		canvas.drawLine(SPACING + padding, 0, SPACING + padding, realHeight,
				paint);
		// x轴
		canvas.drawLine(SPACING + padding, realHeight, SPACING + padding
				+ realWidth, realHeight, paint);

		paint.setTextSize(fontSize);
		if (isNeedX) {
			if (date == 1) {// 周
				float xSpacing = (realWidth - fontSize - spacing_x) / 6;
				for (int i = 0; i < 7; i++) {
					float x = SPACING + padding + spacing_x + i * xSpacing - fontSize / 2;
					canvas.drawText(week[i], x, realHeight
							+ (int) (fontSize * 1.5), paint);
				}
			} else {
				if ((stop - start) < 7) {
					float xSpacing = (realWidth - fontSize - spacing_x)
							/ (stop - start);
					for (int i = 0; i < (stop - start + 1); i++) {
						// TODO 文字
						float x = SPACING + padding + spacing_x + i * xSpacing - fontSize / 2;
						canvas.drawText("" + (start + i), x, realHeight
								+ (int) (fontSize * 1.5), paint);
					}
				} else {
					float xSpacing = (realWidth - fontSize - spacing_x) / 7;
					double span_x = ((double) (stop - start)) / 7;
					for (int i = 0; i <= 7; i++) {
						float x = SPACING + padding + spacing_x + i * xSpacing - fontSize / 2;
						canvas.drawText("" + (int) (start + i * span_x), x,
								realHeight + (int) (fontSize * 1.5), paint);
					}
				}
			}
		}
		/* 竖直线和文字 */
		for (int i = 0; i <= 5; i++) {
			paint.setStyle(Paint.Style.FILL);// 设置填满
			paint.setStrokeWidth(3);
			float y = realHeight - (value * i) * spacingOfY; // 竖字Y轴坐标
			if (value < 1) {
				canvas.drawText(String.format("%.1f", (value * i)), padding
						+ fontSize / 2, y + fontSize / 2, paint);// Y轴坐标
			} else {
				canvas.drawText("" + (int) (value * i), padding + fontSize / 2,
						y + fontSize / 2, paint);// Y轴坐标
			}

			Path path = new Path();
			path.moveTo(padding + SPACING, y);
			path.lineTo(padding + SPACING + realWidth, y);
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(1);
			PathEffect effects = new DashPathEffect(new float[] { 4, 8 }, 1);
			paint.setPathEffect(effects);
			canvas.drawPath(path, paint);
		}
		if(type == 3){
			PointF lastPoint = null;
			/* 绘制曲线 覆盖 剪切后的锯齿 */
			for (int i = 0; i < points.size() - 1; i++) {
				paint.setStrokeWidth(3);
				PointF startPoint = points.get(i);
				PointF endPoint = points.get(i + 1);				
				if(endPoint.y == realHeight){
					if(startPoint.y != realHeight){
						lastPoint = startPoint;
					}
				}else{
					if(startPoint.y != realHeight){
						lastPoint = startPoint;
					}
					if(lastPoint != null){
						// 画阴影
						paint.setColor(getResources().getColor(R.color.Green_curve_bg));
						paint.setStyle(Paint.Style.FILL);// 设置填满
						Path path = new Path();
						path.moveTo(lastPoint.x, realHeight);// 此点为多边形的起点
						path.lineTo(lastPoint.x, lastPoint.y);
						path.lineTo(endPoint.x, endPoint.y);
						path.lineTo(endPoint.x, realHeight);
						path.close(); // 使这些点构成封闭的多边形
						canvas.drawPath(path, paint);
						// 绘制曲线，并且覆盖剪切后的锯齿
						paint.setColor(getResources().getColor(R.color.Green_curve_line));
						canvas.drawLine(lastPoint.x, lastPoint.y, endPoint.x, endPoint.y,
								paint);
					}
				}			
			}
		}else{
			for (int i = 0; i < points.size() - 1; i++) {
				paint.setStrokeWidth(3);
				PointF startPoint = points.get(i);
				PointF endPoint = points.get(i + 1);
				// 画阴影
				paint.setColor(getResources().getColor(R.color.Green_curve_bg));
				paint.setStyle(Paint.Style.FILL);// 设置填满
				Path path = new Path();
				path.moveTo(startPoint.x, realHeight);// 此点为多边形的起点
				path.lineTo(startPoint.x, startPoint.y);
				path.lineTo(endPoint.x, endPoint.y);
				path.lineTo(endPoint.x, realHeight);
				path.close(); // 使这些点构成封闭的多边形
				canvas.drawPath(path, paint);
				// 绘制曲线，并且覆盖剪切后的锯齿
				paint.setColor(getResources().getColor(R.color.Green_curve_line));
				canvas.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y,
						paint);			
			}
		}		
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension((int) width, (int) (width * scale));
	}

	int width = 500;

	/** 传入手机的分辨率 **/
	public void setViewWidth(int width, boolean isNeedX) {
		this.isNeedX = isNeedX;
		this.width = width;
		padding = fontSize;
		realWidth = (int) (width - fontSize * 3 - padding * 2);
		realHeight = (float) (width * scale) - padding * 2;
	}

	/**
	 * 通过数据 预先存入需要绘制的 连线点
	 * 
	 * @param energys
	 * @param Type
	 *            周or月
	 */
	public void initPoints(ArrayList<EnergyItem> energys, int date,int type) {
		
		this.date = date;
		this.type = type;
		getSpacingOfXY(energys);
		points = new ArrayList<PointF>();
		for (int i = 0; i < energys.size(); i++) {
			float f = energys.get(i).value;
			float y = (realHeight - f * spacingOfY);

			float x = (i * spacingOfX + SPACING + spacing_x + padding);
			PointF point = new PointF(x, y);
			points.add(point);
		}
		if (energys.size() == 0) {
			start = 0;
			stop = 0;
		} else {
			start = energys.get(0).date;
			stop = energys.get(energys.size() - 1).date;
			System.out.println("start = " + start + " , stop = " + stop);
			System.out.println("-------------------");
		}
	}

	/**
	 * 获取X的间距 以及 Y的间距
	 * 
	 * @param powers
	 * @param date
	 */
	private void getSpacingOfXY(ArrayList<EnergyItem> energys) {
		maxEnergy = findMaxPowers(energys);
		spacingOfX = (realWidth - fontSize - spacing_x) / (energys.size() - 1);
		spacingOfY = (realHeight - WEIGHT) / maxEnergy.value;
	}

	/**
	 * 找到 数据集合中 最高能量 对应的脚标
	 * 
	 * @param powers
	 * @return
	 */
	private static EnergyItem findMaxPowers(ArrayList<EnergyItem> energys) {
		EnergyItem energy = new EnergyItem();
		energy.value = 0;
		for (int i = 0; i < energys.size(); i++) {
			if (energys.get(i).value > energy.value) {
				energy = energys.get(i);
			}
		}
		return energy;
	}

	public void RefreshView() {
		invalidate();
	}
}