package com.attsinghua.dwf;

/*
 * 本类定义了图表的样式、数据集等定义内容，并实现了图标数据的绘制（数据获取由ResourceForecast类实现）
 * *注意：大量被注释掉的内容是使用“多条曲线”时才需要的，目前的需求只需要单曲线+直方图。需求变更时可打开注释，并注释掉当前运行的内容
 * **注意：若仅需要“双”曲线，不需要直方图，请将getMyGraphicalView中那条注释打开
 */

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.chart.LineChart;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.text.format.Time;
import android.util.Log;

public class ControlAChart {

//	private static final String TAG = "ControlAChart";
	private GraphicalView resChartView;
	private XYMultipleSeriesRenderer resChartRenderer;
	private XYMultipleSeriesDataset dataset;
	private XYSeries waterSeries;
	private XYSeriesRenderer waterRenderer;
	private String[] curveTitle;
	private List<double[]> axisXTime;
	private List<double[]> axisYValue;
	private String[] chartTypes = new String[] { BarChart.TYPE, LineChart.TYPE };
	private int[] lineColors = new int[] { Color.GREEN, Color.rgb(200, 150, 0) };
	private PointStyle[] pointRealStyles = new PointStyle[] { PointStyle.CIRCLE };
	
	/** 
	 * ####################################################################
	 * 
	 * 初始化方法
	 * 
	 * 提供接口,以供其它类调用图表初始化方法
	 * 注意传递上下文哦
	 * 
	 * ####################################################################
	 */
	public GraphicalView getMyGraphicalView(Context c, double[] avgRating, double[] avgAPLoad) {
		initChart();
		insertChartData(avgRating, avgAPLoad);
		resChartView = ChartFactory.getCombinedXYChartView(c, dataset, resChartRenderer, chartTypes);
		//resChartView = ChartFactory.getCubeLineChartView(getActivity(), dataset, resChartRenderer, 0.3f); // **如果只用“双”曲线不用直方图，则打开此注释并注释掉上面一行
		return resChartView;
	}
	
	
	/** 
	 * ####################################################################
	 * 
	 * 继承方法
	 * 
	 * 用于构造有符合式的曲线图表,原例子为避免代码重复抽象了一个类专门用来构造曲线的Series数据与Renderer
	 * 
	 * 对于复合式图表而言,每添加一种图形,就有对应的Series（即某个数据系列,用于数据,显示图例等）、XYSeriesRenderer（系列渲染器,用于效果的定义。例如颜色、粗细等）
	 * 如果是只用（单/多）曲线式图表,最后的 GraphicView 方法中直接用 mChart = ChartFactory.getCubeLineChartView(getActivity(), mDataset, mRenderer,//0.3f); 这样的方式
	 * 如果是要用复合式图表,最后的方法则是 resChartView = ChartFactory.getCombinedXYChartView(getActivity(), dataset, resChartRenderer, chartTypes);
	 * 本类实现中,曲线图：定义与使用是直接用继承来的方法实现的；
	 * 本类实现中,直方图：是直接手写了 XYSeries 与 XYSeriesRenderer,并在 Renderer 及 Dataset 定义完成后按序号予以添加（上面变量中声明的Types即是这一序列,从0开始）
	 * 渲染器+数据集对应后方可显示,缺一不可
	 * 
	 * P01 - MultipleSeriesRenderer(复合图形渲染器)的初始化
	 * P02 - MultipleSeriesRenderer初始化调用到的方法
	 * P03 - 图表参数设置
	 * P04 - MultipleSeriesRenderer数据源(值集)的初始化
	 * P05 - MultipleSeriesRenderer数据源(值集)初始化调用到的方法
	 * P06 - 向值集添加曲线系列的方法
	 * 
	 * ####################################################################
	 */

	// P01
	protected XYMultipleSeriesRenderer buildRenderer(int[] colors, PointStyle[] styles) {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		setRenderer(renderer, colors, styles);
		return renderer;
	}

	// P02
	protected void setRenderer(XYMultipleSeriesRenderer renderer, int[] colors, PointStyle[] styles) {
		renderer.setAxisTitleTextSize(16);
		renderer.setChartTitleTextSize(20);
		renderer.setAntialiasing(true);
		renderer.setZoomEnabled(true, true);
		renderer.setZoomRate(3);
		renderer.setXAxisMax(25.0);
		renderer.setXAxisMin(0);
		renderer.setLabelsTextSize(15);
		renderer.setLegendTextSize(15);
		renderer.setPointSize(5f);
		renderer.setMargins(new int[] { 20, 30, 15, 20 });
		XYSeriesRenderer r = new XYSeriesRenderer();
		r.setColor(colors[0]);
		r.setPointStyle(styles[0]);																					// 控制线的粗细
		renderer.addSeriesRenderer(r);
	}

	// P03
	//@param renderer the renderer to set the properties to
	//@param title the chart title
	//@param xTitle the title for the X axis
	//@param yTitle the title for the Y axis
	//@param xMin the minimum value on the X axis
	//@param xMax the maximum value on the X axis
	//@param yMin the minimum value on the Y axis
	//@param yMax the maximum value on the Y axis
	//@param axesColor the axes color
	//@param labelsColor the labels color
	//
	protected void setChartSettings(XYMultipleSeriesRenderer renderer, String title, String xTitle, String yTitle, double xMin,
			double xMax, double yMin, double yMax, int axesColor, int labelsColor) {
		renderer.setChartTitle(title);
		renderer.setXTitle(xTitle);
		renderer.setYTitle(yTitle);
		renderer.setXAxisMin(xMin);
		renderer.setXAxisMax(xMax);
		renderer.setYAxisMin(yMin);
		renderer.setYAxisMax(yMax);
		renderer.setAxesColor(axesColor);
		renderer.setLabelsColor(labelsColor);
	}

	// P04 
	protected XYMultipleSeriesDataset buildDataset(String[] titles, List<double[]> xValues, List<double[]> yValues) {
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		addXYSeries(dataset, titles, xValues, yValues, 0);
		return dataset;
	}

	// P05
	//@param titles the series titles
	//@param xValues the values for the X axis
	//@param yValues the values for the Y axis
	//@return the XY multiple time dataset
	protected XYMultipleSeriesDataset buildDateDataset(String[] titles, List<Date[]> xValues, List<double[]> yValues) {
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		TimeSeries series = new TimeSeries(titles[0]);
		Date[] xV = xValues.get(0);
		double[] yV = yValues.get(0);
		int seriesLength = xV.length;
		for (int k = 0; k < seriesLength; k++) {
			series.add(xV[k], yV[k]);
		}
		dataset.addSeries(series);
		return dataset;
	}

	// P06
	public void addXYSeries(XYMultipleSeriesDataset dataset, String[] titles, List<double[]> xValues, List<double[]> yValues, int scale) {
		XYSeries series = new XYSeries(titles[0], scale);
		double[] xV = xValues.get(0);
		double[] yV = yValues.get(0);
		int seriesLength = xV.length;
		for (int k = 0; k < seriesLength; k++) {
			series.add(xV[k], yV[k]);
		}
		dataset.addSeries(series);
	}

	
	/** 
	 * ####################################################################
	 * 
	 * Chart表初始化
	 * 
	 * M01 - Chart表初始化的具体定义
	 * M02 - Chart表数据插入
	 * M03 - Chart表数手动数据插入范例
	 * 
	 * ####################################################################
	 */
	// M01
	private void initChart() {
		
		double[] hoursSet = new double[24];
		
		Time time = new Time("GMT+8");
		time.setToNow();
		int myMinute = time.minute;
		int myHour = time.hour+8;
		
		final Calendar c = Calendar.getInstance(); 
		double mHour = c.get(Calendar.HOUR_OF_DAY);									// 获取当前的小时数
		for (int i = 0; i < hoursSet.length; i++) {									// 初始化X轴数据		
			hoursSet[i] = (mHour + 1 + 24)%24;										// 横轴时间值
		}
		
		curveTitle = new String[] { "当前关联AP评价指数" };
		axisXTime = new ArrayList<double[]>();
		axisYValue = new ArrayList<double[]>();
		
		
		resChartRenderer = new XYMultipleSeriesRenderer();
		resChartRenderer = buildRenderer(lineColors, pointRealStyles);
		resChartRenderer.setPointSize(3.0f);
		XYSeriesRenderer r = (XYSeriesRenderer) resChartRenderer.getSeriesRendererAt(0);
		r.setLineWidth(2);
		r.setFillPoints(true);
		resChartRenderer.setXLabels(0);												// 不显示默认的X轴时间坐标（0-25）
//		resChartRenderer.setYLabels(10);
		resChartRenderer.setShowGrid(true);
		resChartRenderer.setXLabelsAlign(Align.RIGHT);
		resChartRenderer.setYLabelsAlign(Align.RIGHT);
//		resChartRenderer.addXTextLabel(0, myHour-4 + ":" + myMinute);
		resChartRenderer.addXTextLabel(6, myHour-3 + ":" + myMinute);
		resChartRenderer.addXTextLabel(12, myHour-2 + ":" + myMinute);
		resChartRenderer.addXTextLabel(18, myHour-1 + ":" + myMinute);
		resChartRenderer.addXTextLabel(24, myHour + ":" + myMinute);
		resChartRenderer.setClickEnabled(false);
		resChartRenderer.setPanEnabled(false);
		resChartRenderer.setBarSpacing(0.5);
		resChartRenderer.setApplyBackgroundColor(true);								// 设置这个之后针对渲染器的颜色设置才有效
		resChartRenderer.setMarginsColor(Color.parseColor("#FFFFED")); 				// 外框颜色
		resChartRenderer.setBackgroundColor(Color.parseColor("#FFFFED")); 			// 有曲线部分的颜色
		resChartRenderer.setAxisTitleTextSize(30);
		
		waterSeries = new XYSeries("AP用户数");
		waterRenderer = new XYSeriesRenderer();
		waterRenderer.setColor(Color.argb(250, 0, 210, 250)); 						// 柱状图颜色
		waterRenderer.setDisplayChartValues(true);
		waterRenderer.setChartValuesTextSize(10);
		resChartRenderer.addSeriesRenderer(0, waterRenderer); 						// 添加直方图渲染器以对应直方图数据集——非常重要	
//		setChartSettings(resChartRenderer, "横轴间隔为10分钟", " ",				// 可选的渲染器方法——显示图表标题、横纵轴内容说明
//		" ", 0.5, 12.5, 0, 40, Color.LTGRAY, Color.LTGRAY);
//		resChartRenderer.setZoomButtonsVisible(true);							// 可选的渲染器方法——以下代码可显示图标缩放工具
//		resChartRenderer.setPanLimits(new double[] { -10, 20, 0, 0 });
//		resChartRenderer.setZoomLimits(new double[] { -10, 20, -10, 40 });

	}
	
	// M02 赋值算法及赋值
	/* 
	 * ***********************************************
	 * 1. 柱状图最大值是折线图最小值
	 * 2. 折线图乘以放大因子:(折线图最大值一半)*2、5、8
	 * ***********************************************
	 */
	private void insertChartData(double[] avgRating, double[] avgAPLoad) {

		double[] emptyYValueSet = new double[24];
		// 没取到值时采用默认值
		if (avgRating == null || avgAPLoad == null) {
			for (int i = 1; i < emptyYValueSet.length; i++) {
				emptyYValueSet[i - 1] = 0;
				waterSeries.add(i, 0);
			}
			axisYValue.add(emptyYValueSet);
			dataset = buildDataset(curveTitle, axisXTime, axisYValue);
			dataset.addSeries(0, waterSeries);
		// 取到值时赋值
		} else {
			// 数值处理
			double apLoadMax = 0.0;
			double mulFactor = 1.0;
			for (int i = 0; i < avgAPLoad.length; i++) {
				if (avgAPLoad[i] > apLoadMax) {
					apLoadMax = avgAPLoad[i];
				}
			}
			Log.e("目前最大的值为", Double.toString(apLoadMax));
			mulFactor = apLoadMax/15;
			
//			if (apLoadMax <50) {
//				mulFactor = (apLoadMax/8); 
//			} else if (apLoadMax >= 50 && apLoadMax < 100) {
//				mulFactor = (apLoadMax/5);
//			} else if (apLoadMax >= 100) {
//				mulFactor = (apLoadMax/2);
//			}
			
			// 曲线图赋值
			for (int i = 0; i < emptyYValueSet.length; i++) {
				avgRating[i] = avgRating[i]*mulFactor + apLoadMax/1.5;
			}
			axisXTime.add(new double[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24 });
			axisYValue.add(avgRating);												// 绿色曲线图数值
			dataset = buildDataset(curveTitle, axisXTime, axisYValue);
			// 直方图赋值
			for (int i = 0; i < avgAPLoad.length; i++) {
				waterSeries.add(i + 1, avgAPLoad[i]);
			}
			dataset.addSeries(0, waterSeries);										// 添加直方图数据集
		}
	}
	
	// M03
//	private void insertChartData() {
//		
//		for (int i = 0; i < curveTitle.length; i++) {								//初始化X轴数据		
//			axisXTime.add(new double[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 });	//X0 - 横轴时间值
//		}
//		axisYValue.add(new double[] { 12.3, 12.5, 13.8, 16.8, 20.4, 24.4, 26.4,		//初始化Y轴数据		
//				26.1, 23.6, 20.3, 17.2, 13.9 });									//Y1 - 绿色曲线图数值
//		axisYValue.add(new double[] { 9, 10, 11, 15, 19, 23, 26, 25, 22, 18,		//Y2 - 棕色曲线图数值
//				13, 10 });
//		dataset = buildDataset(curveTitle, axisXTime, axisYValue);					//完成曲线数据集的构造
//		waterSeries.add(1, 16);														//Y3 - 直方图数值
//		waterSeries.add(2, 15);
//		waterSeries.add(3, 16);
//		waterSeries.add(4, 17);
//		waterSeries.add(5, 20);
//		waterSeries.add(6, 23);
//		waterSeries.add(7, 25);
//		waterSeries.add(8, 25.5);
//		waterSeries.add(9, 26.5);
//		waterSeries.add(10, 24);
//		waterSeries.add(11, 22);
//		waterSeries.add(12, 18);
//		dataset.addSeries(0, waterSeries);											//添加直方图数据集
//	}
}
