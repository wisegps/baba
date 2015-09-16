package com.wise.baba.util;

import android.text.Html;
import android.text.Spanned;

public class ColorText {

	public static Spanned getAirQuality(String quality){
		String color = "";
		if(quality.contains("优")){
			color = "#6eb720";
		}else if(quality.contains("良")){
			color = "#d6c60f";
		}else if(quality.contains("轻度污染")){
			color = "#ec7e22";
		}else if(quality.contains("中度污染")){
			color = "#df2d00";
		}else if(quality.contains("重度污染")){
			color = "#b414bb";
		}else if(quality.contains("严重污染")){
			color = "#6f0474";
			
		}
		
		Spanned html = Html.fromHtml("空气质量:    "+"<font color = "+color+">"+quality+"</font>");
		
		return html;
		
	}
}
