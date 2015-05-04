package com.wise.baba.ui.adapter;

import java.util.Calendar;
import wheel.widget.OnWheelScrollListener;
import wheel.widget.WheelView;
import widget.adapters.ArrayWheelAdapter;
import widget.adapters.NumericWheelAdapter;
import com.wise.baba.R;
import com.wise.baba.biz.GetSystem;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * 自定义日期弹出框
 * @author honesty
 */
public class OpenDateDialog {
	
	static Context mContext;
    static OpenDateDialogListener listener;
    
	public static void ShowDate(Context context,final int index){
		mContext = context;
		View v = LayoutInflater.from(context).inflate(R.layout.data_wheel, null);
		Calendar calendar = Calendar.getInstance();
    	final int curYear = calendar.get(Calendar.YEAR);
        month = (WheelView) v.findViewById(R.id.data_month);
        year = (WheelView) v.findViewById(R.id.data_year);
        day = (WheelView) v.findViewById(R.id.data_day);
        // month
        int curMonth = calendar.get(Calendar.MONTH);
        String months[] = new String[] {"1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月"};
        month.setViewAdapter(new DateArrayAdapter(context, months, curMonth));
        month.setCurrentItem(curMonth);    
        // year
        year.setViewAdapter(new DateNumericAdapter(context, curYear - 80, curYear + 10, 0));
        year.setCurrentItem(80);
        //day
        updateDays(year, month, day,0);
        day.setCurrentItem(calendar.get(Calendar.DAY_OF_MONTH - 1));
		
		AlertDialog.Builder addHoldBuilder = new AlertDialog.Builder(context);
		addHoldBuilder.setTitle("请输入日期");
		addHoldBuilder.setView(v);
		addHoldBuilder.setPositiveButton("确定",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				String trueTime = (curYear - 80 + year.getCurrentItem()) + "-" + GetSystem.ChangeTime(month.getCurrentItem()+1) + "-" + GetSystem.ChangeTime(day.getCurrentItem()+1);
				if(listener != null){
					listener.OnDateChange(trueTime,index);
				}
			}
		});
		addHoldBuilder.setNegativeButton("取消",null);
		addHoldBuilder.show();
	}
	static WheelView year;
	static WheelView month;
	static WheelView day;
	public static void ShowDate(Context context,final int index,String Date){
		int i_year = Integer.valueOf(Date.substring(0, 4));
		int i_Month = Integer.valueOf(Date.substring(5, 7)) - 1;
		int i_Date = Integer.valueOf(Date.substring(8, 10)) - 1;
		mContext = context;
		View v = LayoutInflater.from(context).inflate(R.layout.data_wheel, null);
		Calendar calendar = Calendar.getInstance();
    	final int curYear = calendar.get(Calendar.YEAR);
        month = (WheelView) v.findViewById(R.id.data_month);
        year = (WheelView) v.findViewById(R.id.data_year);
        day = (WheelView) v.findViewById(R.id.data_day);
        // month
        int curMonth = calendar.get(Calendar.MONTH);
        String months[] = new String[] {"1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月"};
        month.setViewAdapter(new DateArrayAdapter(context, months, curMonth));
        month.addScrollingListener(onWheelScrollListener);
        //onWheelScrollListener
        month.setCurrentItem(i_Month);    
        // year
        year.setViewAdapter(new DateNumericAdapter(context, curYear - 80, curYear + 10, 0));
        year.setCurrentItem(i_year - (curYear - 80));
        year.addScrollingListener(onWheelScrollListener);
        //day
        updateDays(year, month, day,i_Date);
		
		AlertDialog.Builder addHoldBuilder = new AlertDialog.Builder(context);
		addHoldBuilder.setTitle("请输入日期");
		addHoldBuilder.setView(v);
		addHoldBuilder.setPositiveButton("确定",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				String trueTime = (curYear - 80 + year.getCurrentItem()) + "-" + GetSystem.ChangeTime(month.getCurrentItem()+1) + "-" + GetSystem.ChangeTime(day.getCurrentItem()+1);
				if(listener != null){
					listener.OnDateChange(trueTime,index);
				}
			}
		});
		addHoldBuilder.setNegativeButton("取消",null);
		addHoldBuilder.show();
	}

	/**
     * Updates day wheel. Sets max days according to selected month and year
     */
	private static void updateDays(WheelView year, WheelView month, WheelView day ,int i_Date) {
		Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR) + year.getCurrentItem(), month.getCurrentItem(), 1);
        int maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        day.setViewAdapter(new DateNumericAdapter(mContext, 1, maxDays, calendar.get(Calendar.DAY_OF_MONTH) - 1));
        int curDay = Math.min(maxDays, i_Date);
        day.setCurrentItem(curDay);
    }
	private static void updateDays(WheelView year, WheelView month, WheelView day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR) + year.getCurrentItem(), month.getCurrentItem(), 1);
        int maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        day.setViewAdapter(new DateNumericAdapter(mContext, 1, maxDays, calendar.get(Calendar.DAY_OF_MONTH) - 1));
        int curDay = Math.min(maxDays, day.getCurrentItem() + 1);
        day.setCurrentItem(curDay - 1);
    }
	static OnWheelScrollListener onWheelScrollListener = new OnWheelScrollListener() {		
		@Override
		public void onScrollingStarted(WheelView wheel) {
			updateDays(year, month, day);
		}		
		@Override
		public void onScrollingFinished(WheelView wheel) {}
	};
	/**
     * Adapter for numeric wheels. Highlights the current value.
     */
    private static class DateNumericAdapter extends NumericWheelAdapter {
        // Index of current item
        int currentItem;
        // Index of item to be highlighted
        int currentValue;        
        /**
         * Constructor
         */
        public DateNumericAdapter(Context context, int minValue, int maxValue, int current) {
            super(context, minValue, maxValue);
            this.currentValue = current;
            setTextSize(20);
        }        
        @Override
        protected void configureTextView(TextView view) {
            super.configureTextView(view);
            if (currentItem == currentValue) {
                //view.setTextColor(0xFF0000F0);
            }
            view.setTypeface(Typeface.SANS_SERIF);
        }        
        @Override
        public View getItem(int index, View cachedView, ViewGroup parent) {
            currentItem = index;
            return super.getItem(index, cachedView, parent);
        }
    }    
    /**
     * Adapter for string based wheel. Highlights the current value.
     */
    private static class DateArrayAdapter extends ArrayWheelAdapter<String> {
        // Index of current item
        int currentItem;
        // Index of item to be highlighted
        int currentValue;        
        /**
         * Constructor
         */
        public DateArrayAdapter(Context context, String[] items, int current) {
            super(context, items);
            this.currentValue = current;
            setTextSize(20);
        }        
        @Override
        protected void configureTextView(TextView view) {
            super.configureTextView(view);
            if (currentItem == currentValue) {
                //view.setTextColor(0xFF0000F0);
            }
            view.setTypeface(Typeface.SANS_SERIF);
        }        
        @Override
        public View getItem(int index, View cachedView, ViewGroup parent) {
            currentItem = index;
            return super.getItem(index, cachedView, parent);
        }
    }
    public static void SetCustomDateListener(OpenDateDialogListener openDateDialogListener){
    	listener = openDateDialogListener;
    }
}
