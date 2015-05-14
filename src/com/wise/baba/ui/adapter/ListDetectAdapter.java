/**
 * 
 */
package com.wise.baba.ui.adapter;

import java.util.ArrayList;

import com.wise.baba.R;
import com.wise.baba.app.Const;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 
 * @author c
 * @desc baba
 * @date 2015-5-7
 * 
 */
public class ListDetectAdapter extends BaseAdapter {

	private String title[] = { "故障检测系统", "电源系统", "进气系统", "怠速控制系统", "冷却系统",
			"排放系统" };
	int[] faults = {0,0,0,0,0,0};
	private String desc[] = { "未检测", "未检测", "未检测", "未检测", "未检测", "未检测" };// 故障描术
	private Context context;

	private int detectionFlag = -1;

	public ListDetectAdapter(Context context) {
		super();
		this.context = context;
	}

	
	public void changeDetectionFlag(int flag){
		this.detectionFlag = flag;
	}
	/**
	 * 
	 * @param fults
	 *            是否有异常
	 */
	public void change(int detectionFlag, int[] faults) {
		Log.i("FaultDetectionActivity", "是否有异常");
		this.detectionFlag = detectionFlag;
		this.faults = faults;

		String normal[] = { "无故障码", "蓄电池状态良好", "节气门开度良好", "怠速稳定", "水温稳定",
				"三元催化器状态良好" };

		String fault[] = { "有0个故障", "蓄电池状态异常", "节气门开度异常", "怠速异常", "水温异常",
				"三元催化器状态异常" };

		String unbound[] = { "未绑定", "未绑定", "未绑定", "未绑定", "未绑定", "未绑定" };

		String not[] = { "未检测", "未检测", "未检测", "未检测", "未检测", "未检测" };// 故障描术
		
		String inProgress[] = { "故障检测中...", "蓄电池检测中...", "节气门开度检测中...",
				"怠速检测中...", "水温检测中...", "三元催化剂检测中..." };

		if (detectionFlag == Const.DETECT_NO_DEVICE) {
			desc = unbound;
			return;
		} else if (detectionFlag == Const.DETECT_IN_PROGRESS) {
			desc = inProgress;
			return;
		}else if (detectionFlag == Const.DETECT_NOT_DETECTED) {
			desc = not;
			return;
		}

		// 设置所有故障信息
		for (int i = 0; i < faults.length; i++) {
			if (faults[i] == 0) { // 良好
				desc[i] = normal[i];
			} else if(faults[i] == 1){// 异常
				desc[i] = fault[i];
				if (i == 0) {
					desc[i] = "有" + faults[i] + "个故障";
				}
			}else{
				//还是原来的故障描述
			}
		}
	}

	/**
	 * 改变一个状态值
	 * 
	 * @param position
	 * @param faultCode
	 */
	public void changeItem(int position, int faultCode) {
		this.faults[position] = faultCode;
		change(Const.DETECT_RESULT,this.faults);
		String f = "";
		String d = "";
		for(int i=0;i<faults.length;i++){
			f+=faults[i]+" ";
			d+= desc[i]+" ";
		}
		Log.i("FaultDetectionActivity", "faults"+f);
		Log.i("FaultDetectionActivity", "desc"+d);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 6;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.item_list_detect, null);
		}
		ImageView iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);
		ImageView iv_border = (ImageView) convertView.findViewById(R.id.iv_border);
		TextView tv_title = (TextView) convertView.findViewById(R.id.tv_title);
		TextView tv_desc = (TextView) convertView.findViewById(R.id.tv_desc);

		tv_title.setText(title[position]);
		tv_desc.setText(desc[position]);
		
		//// 未绑定终端，检测中 字体样式
		tv_title.setTextColor(context.getResources().getColor(
				R.color.txt_item_title_black));
		tv_desc.setTextColor(context.getResources().getColor(
				R.color.txt_detect_normal_gray));
		iv_icon.setImageResource(R.drawable.ico_detect_def);
		iv_border.setVisibility(View.INVISIBLE);
		if (detectionFlag == Const.DETECT_NO_DEVICE  || detectionFlag == Const.DETECT_IN_PROGRESS ||detectionFlag == Const.DETECT_NOT_DETECTED) {
			//如果未绑定设备，或检测中 不用再核对故障码  直接返回
			return convertView;	
		} 
		
		//检测结果
//		if (detectionFlag == Const.DETECT_HISTORY && faults[position] == 0) {// 历史检测良好
//			iv_icon.setImageResource(R.drawable.ico_detect_def);
//		} else if (detectionFlag == Const.DETECT_RESULT && faults[position] == 0) {// 本次检测良好
//			iv_icon.setImageResource(R.drawable.ico_detect_ok);
//		}
		
		if (faults[position] == 0) {// 检测良好
			iv_icon.setImageResource(R.drawable.ico_detect_ok);
		}
		
		
		if (faults[position] > 0) {// 有故障
			tv_title.setTextColor(context.getResources().getColor(
					R.color.txt_detect_fault_orange));
			tv_desc.setTextColor(context.getResources().getColor(
					R.color.txt_detect_fault_orange));
			iv_icon.setImageResource(R.drawable.ico_detect_fault);
			iv_border.setVisibility(View.VISIBLE);
		}
		return convertView;
	}
}
