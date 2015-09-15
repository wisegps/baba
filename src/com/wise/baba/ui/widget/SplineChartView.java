package com.wise.baba.ui.widget;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.xclcharts.chart.CustomLineData;
import org.xclcharts.chart.PointD;
import org.xclcharts.chart.SplineChart;
import org.xclcharts.chart.SplineData;
import org.xclcharts.common.DensityUtil;
import org.xclcharts.common.IFormatterTextCallBack;
import org.xclcharts.event.click.PointPosition;
import org.xclcharts.renderer.XEnum;
import org.xclcharts.view.ChartView;

import com.wise.baba.R;
import com.wise.baba.entity.AQIEntity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

public class SplineChartView extends ChartView {

	private SplineChart chart = new SplineChart();
	// 分类轴标签集合
	private LinkedList<String> labels = new LinkedList<String>();
	private LinkedList<SplineData> chartData = new LinkedList<SplineData>();

	private Paint mPaintTooltips = new Paint(Paint.ANTI_ALIAS_FLAG);

	private List<AQIEntity> listAQI = new ArrayList<AQIEntity>();

	public SplineChartView(Context context) {
		super(context);
		initView();
	}

	public SplineChartView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	public SplineChartView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView();
	}

	private void initView() {

		chartLabels();
		chartDataSet();
		chartRender();

		List<AQIEntity> listAQI = new ArrayList<AQIEntity>();
		AQIEntity a = new AQIEntity();

		// 綁定手势滑动事件
		// this.bindTouch(this,chart);
	}

	public void setDataSet(List<AQIEntity> listAQI) {
		labels.clear();
		chartData.clear();
		this.listAQI = listAQI;
		chartLabels();
		chartDataSet();
		chartRender();
	}

	

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		// 图所占范围大小
		chart.setChartRange(w, h);

	}

	private void chartRender() {
		try {

			int bottom = DensityUtil.dip2px(getContext(), 20); // bottom
			// 设置绘图区默认缩进px值,留置空间显示Axis,Axistitle....
			int left = DensityUtil.dip2px(getContext(), 47); // bottom
			Log.i("SplineChatView", bottom + "");
			chart.setPadding(left, 0, bottom, bottom);

			// 显示边框
			// chart.showRoundBorder();

			// 数据源
			chart.setCategories(labels);
			chart.setDataSource(chartData);

			// 坐标系
			// 数据轴最大值
			chart.getDataAxis().setAxisMax(100);
			chart.getDataAxis().setAxisMin(0);

			// 数据轴刻度间隔
			chart.getDataAxis().setAxisSteps(25);
			// chart.setCustomLines(mYCustomLineDataset); //y轴

			// 标签轴最大值
			chart.setCategoryAxisMax(100);
			// 标签轴最小值
			chart.setCategoryAxisMin(0);

			if (listAQI.size() > 0) {
				chart.setCategoryAxisMax(listAQI.size());
			}

			// 调轴线与网络线风格

			chart.getCategoryAxis().getTickLabelPaint().setColor(Color.GRAY);
			chart.getCategoryAxis().setTickLabelMargin(15);
			chart.getCategoryAxis().getTickLabelPaint().setTextSize(25);

			chart.getCategoryAxis().hideTickMarks();
			chart.getDataAxis().hideAxisLine();
			chart.getDataAxis().hideTickMarks();
			chart.getPlotGrid().showHorizontalLines();
			// chart.hideTopAxis();
			// chart.hideRightAxis();

			chart.getPlotGrid().getHorizontalLinePaint().setColor(Color.WHITE);
			chart.getCategoryAxis()
					.getAxisPaint()
					.setColor(
							chart.getPlotGrid().getHorizontalLinePaint()
									.getColor());
			chart.getCategoryAxis().getAxisPaint().setStrokeWidth(1);
			chart.getPlotGrid().getHorizontalLinePaint().setStrokeWidth(1);

			// 定义交叉点标签显示格式,特别备注,因曲线图的特殊性，所以返回格式为: x值,y值
			// 请自行分析定制
			chart.setDotLabelFormatter(new IFormatterTextCallBack() {

				@Override
				public String textFormatter(String value) {
					// TODO Auto-generated method stub
					String label = "[" + value + "]";
					return (label);
				}

			});
			// 标题
			chart.setTitle("Spline Chart");
			chart.addSubtitle("(XCL-Charts Demo)");

			// 激活点击监听
			chart.ActiveListenItemClick();
			// 为了让触发更灵敏，可以扩大5px的点击监听范围
			chart.extPointClickRange(5);
			chart.showClikedFocus();

			// 显示平滑曲线
			chart.setCrurveLineStyle(XEnum.CrurveLineStyle.BEZIERCURVE);

			// 图例显示在正下方
			// chart.getPlotLegend().setVerticalAlign(XEnum.VerticalAlign.BOTTOM);
			// chart.getPlotLegend().setHorizontalAlign(XEnum.HorizontalAlign.CENTER);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	/**
	 * 把空气值，转变为Y轴值
	 */
	public void toYValue(int value) {

	}

	private void chartDataSet() {

		
		for (int i = 0; i < listAQI.size(); i++) {
			String time = listAQI.get(i).getTime();
			time = time.substring(11, 16);
			if(i%5 == 0){
				labels.add(time);
			}else{
				labels.add("");
			}
		}

		
		// 线2的数据集
		List<PointD> linePoint2 = new ArrayList<PointD>();
		linePoint2.add(new PointD(40d, 50d));
		linePoint2.add(new PointD(55d, 55d));

		linePoint2.add(new PointD(60d, 65d));
		linePoint2.add(new PointD(65d, 85d));

		linePoint2.add(new PointD(72d, 70d));
		linePoint2.add(new PointD(85d, 68d));

		SplineData dataSeries2 = new SplineData("线二", linePoint2, Color.rgb(40,
				129, 138));

		//dataSeries2.setDotStyle(XEnum.DotStyle.RING);
		chartData.add(dataSeries2);
	}

	private void chartLabels() {

		if (listAQI.size() < 0) {
			labels.add("10:00");
			labels.add("10:10");
			labels.add("10:20");
			labels.add("10:30");
			labels.add("10:40");
			labels.add("10:50");
			return;
		}

		for (int i = 0; i < listAQI.size(); i++) {
			String time = listAQI.get(i).getTime();
			time = time.substring(11, 16);
			if(i%5 == 0){
				labels.add(time);
			}else{
				labels.add("");
			}
		}

	}

	@Override
	public void render(Canvas canvas) {
		try {
			chart.render(canvas);
		} catch (Exception e) {
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub

		super.onTouchEvent(event);

		if (event.getAction() == MotionEvent.ACTION_UP) {
			triggerClick(event.getX(), event.getY());
		}
		return true;
	}

	// 触发监听
	private void triggerClick(float x, float y) {
		if (!chart.getListenItemClickStatus())
			return;

		PointPosition record = chart.getPositionRecord(x, y);
		if (null == record)
			return;

		if (record.getDataID() >= chartData.size())
			return;
		SplineData lData = chartData.get(record.getDataID());
		List<PointD> linePoint = lData.getLineDataSet();
		int pos = record.getDataChildID();
		int i = 0;
		Iterator it = linePoint.iterator();
		while (it.hasNext()) {
			PointD entry = (PointD) it.next();

			if (pos == i) {
				Double xValue = entry.x;
				Double yValue = entry.y;

				float r = record.getRadius();
				chart.showFocusPointF(record.getPosition(), r + r * 0.8f);
				chart.getFocusPaint().setStyle(Style.FILL);
				chart.getFocusPaint().setStrokeWidth(3);
				if (record.getDataID() >= 2) {
					chart.getFocusPaint().setColor(Color.BLUE);
				} else {
					chart.getFocusPaint().setColor(Color.RED);
				}
				// 在点击处显示tooltip
				mPaintTooltips.setColor(Color.RED);
				chart.getToolTip().setCurrentXY(x, y);
				chart.getToolTip().addToolTip(" Key:" + lData.getLineKey(),
						mPaintTooltips);
				chart.getToolTip().addToolTip(
						" Current Value:" + Double.toString(xValue) + ","
								+ Double.toString(yValue), mPaintTooltips);
				chart.getToolTip().getBackgroundPaint().setAlpha(100);
				this.invalidate();

				break;
			}
			i++;
		}// end while

	}

}
