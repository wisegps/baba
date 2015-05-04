package com.wise.baba.ui.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class FanView extends View {

	// 跳转类型
	public static final int DISTANCE = 1;// 里程
	public static final int FEE = 2;// 费用
	public static final int FUEL = 3;// 油耗

	private int Width = 250; // 总大小
	private final int RingWidth = 20; // 圆环大小
	private int CenterWidth = 100; // 圆心大小

	int[] Colors = { Color.argb(255, 81, 206, 181), Color.argb(255, 248, 220, 92), Color.argb(255, 235, 130, 99), Color.argb(255, 139, 207, 233),
			Color.argb(255, 58, 137, 184), Color.argb(255, 248, 220, 92), Color.argb(255, 235, 130, 99), Color.argb(255, 139, 207, 233) };
	/** 每次旋转的度数 **/
	int AveRotate = 3;

	Rect mBounds = new Rect();
	/** 通过RecordAngle来控制角度,在-90 到 270 之间 **/
	int RecordAngle = -90;
	/** 当前旋转角度 **/
	int currentRotateRanges = 0;

	public FanView(Context context) {
		super(context);
	}

	public FanView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FanView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(Width * 2, Width * 2);
	}

	int startRange = 0;

	/**
	 * 
	 * @param Datas
	 * @param position
	 *            指定那个位置
	 */
	public void setDatas(List<com.wise.state.FuelActivity.RangeData> Datas, int position, int type) {
		rangeDatas.clear();
		RecordAngle = -90;
		currentRotateRanges = 0;
		startRange = 0;

		// 把百分比转成对应的度数
		for (int i = 0; i < Datas.size(); i++) {
			RangeData rangeData = new RangeData();
			String text = "";
			if (type == FUEL) {
				text = subData(Datas.get(i).getAvg_fuel());
			} else if (type == DISTANCE) {
				text = Datas.get(i).getDistance();
			} else {
				text = Datas.get(i).getFee();
			}
			if (i == (Datas.size() - 1)) {
				rangeData.setStartRanges(startRange);
				rangeData.setStartRotateRanges(startRange);
				rangeData.setAnges(360 - startRange);
				rangeData.setText(text);
				rangeDatas.add(rangeData);
			} else {
				int range = Datas.get(i).getPercent() * 360 / 100;
				rangeData.setStartRanges(startRange);
				rangeData.setStartRotateRanges(startRange);
				rangeData.setAnges(range);
				rangeData.setText(text);
				rangeDatas.add(rangeData);
				startRange += range;
			}
		}
		if (Datas.size() > 0) {
			this.OnTouchIndex = position;
			RangeData rangeData = rangeDatas.get(OnTouchIndex);
			int centerRanges = (rangeData.getAnges() / 2 + rangeData.getStartRanges()) % 360;
			if (centerRanges > 180) {
				rotateRanges = 180 - centerRanges;
			} else {
				rotateRanges = 180 - centerRanges;
			}
			// 重新计算位置
			jsRotate(rotateRanges);
			RecordAngle = (RecordAngle + rotateRanges) % 360;
		}
		postInvalidate();
	}

	/** 处理数据 30L/100KM , 30L/hr 改成 30L **/
	private String subData(String result) {
		int position = result.indexOf("/");
		if (position == -1) {
			return result;
		} else {
			return result.substring(0, position);
		}
	}

	List<RangeData> rangeDatas = new ArrayList<RangeData>();

	class RangeData {
		int startRanges;
		int startRotateRanges;
		int anges;
		String text;

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}

		public int getStartRotateRanges() {
			return startRotateRanges;
		}

		public void setStartRotateRanges(int startRotateRanges) {
			this.startRotateRanges = startRotateRanges;
		}

		public int getStartRanges() {
			return startRanges;
		}

		public void setStartRanges(int startRanges) {
			this.startRanges = startRanges;
		}

		public int getAnges() {
			return anges;
		}

		public void setAnges(int anges) {
			this.anges = anges;
		}

		@Override
		public String toString() {
			return "RangeData [startRanges=" + startRanges + ", anges=" + anges + "]";
		}
	}

	/**
	 * 设置半径
	 * 
	 * @param Size
	 *            半径
	 */
	public void setViewSize(int Size) {
		Width = Size;
		CenterWidth = Size / 3;
		postInvalidate();
	}

	Paint p = new Paint();

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		p.setColor(Color.argb(255, 229, 244, 250));
		p.setAntiAlias(true);
		canvas.drawCircle(Width, Width, Width, p); // 画圆

		RectF oval = new RectF(RingWidth, RingWidth, Width * 2 - RingWidth, Width * 2 - RingWidth);
		p.setColor(Color.BLUE);

		if (RecordAngle < -90) {
			RecordAngle = RecordAngle + 360;
		} else if (RecordAngle > 270) {
			RecordAngle = RecordAngle - 360;
		}
		int startAngle = (RecordAngle + currentRotateRanges) % 360;
		// 重新计算角度
		for (int i = 0; i < rangeDatas.size(); i++) {
			p.setColor(Colors[i]);
			RangeData rangeData = rangeDatas.get(i);
			if (B >= 0) {
				if ((rangeData.getAnges() + rangeData.getStartRanges()) >= 360) {
					int endRanges = (rangeData.getAnges() + rangeData.getStartRanges()) - 360;
					if (B < endRanges || B >= rangeData.getStartRanges()) {
						p.setColor(Color.GRAY);
						OnTouchIndex = i;
						if (onViewRotateListener != null) {
							onViewRotateListener.viewRotate(OnTouchIndex);
						}
					}
				} else if (B >= rangeData.getStartRanges() && B < (rangeData.getAnges() + rangeData.getStartRanges())) {
					p.setColor(Color.GRAY);
					OnTouchIndex = i;
					if (onViewRotateListener != null) {
						onViewRotateListener.viewRotate(OnTouchIndex);
					}
				}
			}
			canvas.drawArc(oval, startAngle, rangeData.getAnges(), true, p);
			// TODO 写字
			int nowCenterRanges = (rangeData.getAnges() / 2 + startAngle + 90) % 360;
			double nowX = Width + Width * Math.sin(nowCenterRanges * Math.PI / 180) * 0.6;
			double nowY = Width - Width * Math.cos(nowCenterRanges * Math.PI / 180) * 0.6;
			p.setColor(Color.WHITE);
			p.setTextSize(30);
			String str = rangeData.getText();
			int w = (int) p.measureText(str) / 2;
			canvas.drawText(str, (float) nowX - w, (float) nowY, p);
			startAngle += rangeData.getAnges();
		}
		p.setColor(Color.WHITE);
		canvas.drawCircle(Width, Width, CenterWidth, p); // 画圆
	}

	int OnTouchIndex = -1;
	double B = -1;
	double LastB = -1;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (isRotate) {
			return true;
		}
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			OnTouchIndex = -1;
			currentRotateRanges = 0;
			// 判断是否点在圆上 x*x + y*y>=r*r
			float w = event.getX() - Width;
			float h = event.getY() - Width;
			if ((w * w + h * h) > Width * Width || (w * w + h * h) < CenterWidth * CenterWidth) {
				// 点在圆外
			} else {
				// 点在圆内
				// 得到角度
				int a = Width;
				double b = Math.sqrt(w * w + event.getY() * event.getY());
				double c = Math.sqrt(w * w + h * h);
				java.text.DecimalFormat df = new java.text.DecimalFormat("#0.00");
				// 计算弧度表示的角
				B = Math.acos((a * a + c * c - b * b) / (2.0 * a * c));
				// 用角度表示的角
				B = Math.toDegrees(B);
				if (w < 0) {
					B = 360 - B;
				}
				// 格式化数据，保留两位小数
				String temp = df.format(B);
				postInvalidate();
			}
			break;

		case MotionEvent.ACTION_UP:
			LastB = B;
			B = -1;
			postInvalidate();
			if (OnTouchIndex != -1) {
				RangeData rangeData = rangeDatas.get(OnTouchIndex);
				int centerRanges = (rangeData.getAnges() / 2 + rangeData.getStartRanges()) % 360;
				if (centerRanges > 180) {
					rotateRanges = 180 - centerRanges;
				} else {
					rotateRanges = 180 - centerRanges;
				}
				// 重新计算位置
				jsRotate(rotateRanges);
				isRotate = true;
				new RotateThread().start();
			}
			break;
		case MotionEvent.ACTION_CANCEL:
			LastB = B;
			B = -1;
			postInvalidate();
			if (OnTouchIndex != -1) {
				RangeData rangeData = rangeDatas.get(OnTouchIndex);
				int centerRanges = (rangeData.getAnges() / 2 + rangeData.getStartRanges()) % 360;
				if (centerRanges > 180) {
					rotateRanges = 180 - centerRanges;
				} else {
					rotateRanges = 180 - centerRanges;
				}
				// 重新计算位置
				jsRotate(rotateRanges);
				isRotate = true;
				new RotateThread().start();
			}
			break;
		}
		return true;
	}

	/** 计算旋转后的角度 **/
	private void jsRotate(int rotate) {
		for (int i = 0; i < rangeDatas.size(); i++) {
			RangeData rangeData = rangeDatas.get(i);
			int newRanges = rangeData.getStartRanges() + rotate;
			if (newRanges >= 360) {
				newRanges -= 360;
			} else if (newRanges < 0) {
				newRanges += 360;
			}
			rangeData.setStartRanges(newRanges);
		}
	}

	/** 重置旋转中的角度 **/
	private void resetRotate() {
		for (int i = 0; i < rangeDatas.size(); i++) {
			RangeData rangeData = rangeDatas.get(i);
			rangeData.setStartRotateRanges(rangeData.getStartRanges());
		}
	}

	/** 旋转的角度 **/
	int rotateRanges;
	boolean isRotate = false;

	class RotateThread extends Thread {
		@Override
		public void run() {
			super.run();
			while (isRotate) {
				try {
					sleep(20);
					if (rotateRanges >= 0) {// 顺时针旋转 旋转 45 到 180 旋转 135
						// 判断下次旋转角度是否超过规定角度
						if ((currentRotateRanges + AveRotate) >= rotateRanges) {
							// 最后一次旋转
							currentRotateRanges = rotateRanges;
							isRotate = false;
							RecordAngle = (RecordAngle + rotateRanges) % 360;
							currentRotateRanges = 0;
							resetRotate();
						} else {
							currentRotateRanges += AveRotate;
						}
						handler.sendMessage(new Message());
					} else {
						// 逆时针旋转
						if ((currentRotateRanges - AveRotate) <= rotateRanges) {
							currentRotateRanges = rotateRanges;
							isRotate = false;
							// TODO 矫正下
							RecordAngle = (RecordAngle + rotateRanges) % 360;
							currentRotateRanges = 0;
							resetRotate();
						} else {
							currentRotateRanges -= AveRotate; // 每次旋转1读
						}
						handler.sendMessage(new Message());
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			postInvalidate();
		}
	};

	OnViewRotateListener onViewRotateListener;

	public void setOnViewRotateListener(OnViewRotateListener onViewRotateListener) {
		this.onViewRotateListener = onViewRotateListener;
	}

	public interface OnViewRotateListener {
		public void viewRotate(int rotateRanges);
	}
}