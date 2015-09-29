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
import com.wise.baba.util.DateUtil;

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

			// 数据源
			chart.setCategories(labels);
			chart.setDataSource(chartData);

			// 坐标系
			// 数据轴最大值
			chart.getDataAxis().setAxisMax(100);
			chart.getDataAxis().setAxisMin(0);
			// 数据轴刻度间隔
			chart.getDataAxis().setAxisSteps(25);

			// 标签轴最大值
			chart.setCategoryAxisMax(100);
			// 标签轴最小值
			chart.setCategoryAxisMin(0);

			if (listAQI.size() > 0) {
				chart.setCategoryAxisMax(listAQI.size());
			}

			// 调轴线与网络线风格

			chart.getCategoryAxis().getTickLabelPaint().setColor(Color.WHITE);
			chart.getCategoryAxis().getTickLabelPaint().setAlpha(150);
			chart.getCategoryAxis().setTickLabelMargin(15);
			chart.getCategoryAxis().getTickLabelPaint().setTextSize(25);

			
			chart.getCategoryAxis().hideTickMarks();
			chart.getDataAxis().hideTickMarks();
			chart.getDataAxis().hideAxisLabels();
			chart.getDataAxis().hideAxisLine();
			chart.getPlotGrid().showHorizontalLines();
			chart.getPlotGrid().hideVerticalLines();

			chart.getPlotGrid().getHorizontalLinePaint().setColor(Color.GRAY);
			chart.getCategoryAxis().getAxisPaint().setColor(Color.GRAY);
			chart.getCategoryAxis().getAxisPaint().setStrokeWidth(1);
			chart.getPlotGrid().getHorizontalLinePaint().setStrokeWidth(1);
			// 显示平滑曲线
			chart.setCrurveLineStyle(XEnum.CrurveLineStyle.BEZIERCURVE);


		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 把空气值，转变为Y轴值
	 */
	public Double toYValue(double value) {

		int max = 100;
		Double v = 0d;
		if (value <= 1300) {
			max = 25;
			v = (double) (25 * (value / 1300));

		} else if (value > 1300 && value <= 1500) {
			max = 50;
			v = (double) 25 + (25 * ((value - 1300) / (1500 - 1300)));

		} else if (value > 1500 && value <= 2000) {

			max = 75;
			v = (double) 50 + (25 * ((value - 1500) / (2000 - 1500)));
		} else if (value > 2000) {
			max = 100;
			v = (double) 75 + (25 * ((value - 2000) / (5000 - 2000)));
		}

		return v;

	}

	private void chartDataSet() {

		// 线2的数据集
		List<PointD> linePoint2 = new ArrayList<PointD>();
		for (int i = 0; i < listAQI.size(); i++) {

			if (i % 5 == 0  || i == listAQI.size()-1 ) {
				linePoint2
						.add(new PointD(i, toYValue(listAQI.get(i).getAir())));
			}
		}

		SplineData dataSeries2 = new SplineData("线二", linePoint2, Color.rgb(40,
				129, 138));

		// 把线弄细点
		dataSeries2.getLinePaint().setStrokeWidth(5);
		dataSeries2.setLineStyle(XEnum.LineStyle.SOLID);
		dataSeries2.setDotStyle(XEnum.DotStyle.HIDE);

		// dataSeries2.setLabelVisible(false);
		// dataSeries2.setDotStyle(XEnum.DotStyle.HIDE);
		// dataSeries2.getLinePaint().setStrokeWidth(4);
		chartData.add(dataSeries2);
	}

	private void chartLabels() {

		if (listAQI.size() <= 0) {
			
			String time0 =  DateUtil.getCurrentTime(3).substring(11, 16);
			String time1 =  DateUtil.getCurrentTime(2.5f).substring(11, 16);
			String time2 =  DateUtil.getCurrentTime(2).substring(11, 16);
			String time3 =  DateUtil.getCurrentTime(1.5f).substring(11, 16);
			String time4 =  DateUtil.getCurrentTime(1).substring(11, 16);
			String time5 =  DateUtil.getCurrentTime(0.5f).substring(11, 16);
			String time6 =  DateUtil.getCurrentTime(0).substring(11, 16);
			labels.add(time0);
			labels.add(time1);
			labels.add(time2);
			labels.add(time3);
			labels.add(time4);
			labels.add(time5);
			labels.add(time6);
			
			return;
		}

		for (int i = 0; i < listAQI.size(); i++) {
			String time = listAQI.get(i).getTime();
			time = time.substring(11, 16);
			
			
			if (i % 30 == 0) {
				labels.add(time);
			} else {
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

}
