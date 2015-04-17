package com.wise.baba.entity;

import com.wise.baba.ui.widget.DialView;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CarView {
	LinearLayout ll_adress;
	TextView tv_current_distance;
	TextView tv_distance;
	TextView tv_fee;
	TextView tv_fuel;
	TextView tv_score;
	TextView tv_title;
	TextView tv_xx;
	TextView tv_drive;
	
	TextView tv_location;
	
	
	public TextView getTv_location() {
		return tv_location;
	}
	public void setTv_location(TextView tv_location) {
		this.tv_location = tv_location;
	}
	DialView dialHealthScore;
	DialView dialDriveScore;
	
	public DialView getDialDriveScore() {
		return dialDriveScore;
	}
	public void setDialDriveScore(DialView dialDriveScore) {
		this.dialDriveScore = dialDriveScore;
	}
	public DialView getDialHealthScore() {
		return dialHealthScore;
	}
	public void setDialHealthScore(DialView dialHealthScore) {
		this.dialHealthScore = dialHealthScore;
	}
	public LinearLayout getLl_adress() {
		return ll_adress;
	}
	public void setLl_adress(LinearLayout ll_adress) {
		this.ll_adress = ll_adress;
	}
	public TextView getTv_current_distance() {
		return tv_current_distance;
	}
	public void setTv_current_distance(TextView tv_current_distance) {
		this.tv_current_distance = tv_current_distance;
	}
	public TextView getTv_distance() {
		return tv_distance;
	}
	public void setTv_distance(TextView tv_distance) {
		this.tv_distance = tv_distance;
	}
	public TextView getTv_fee() {
		return tv_fee;
	}
	public void setTv_fee(TextView tv_fee) {
		this.tv_fee = tv_fee;
	}
	public TextView getTv_fuel() {
		return tv_fuel;
	}
	public void setTv_fuel(TextView tv_fuel) {
		this.tv_fuel = tv_fuel;
	}
	public TextView getTv_score() {
		return tv_score;
	}
	public void setTv_score(TextView tv_score) {
		this.tv_score = tv_score;
	}
	public TextView getTv_title() {
		return tv_title;
	}
	public void setTv_title(TextView tv_title) {
		this.tv_title = tv_title;
	}
	public TextView getTv_xx() {
		return tv_xx;
	}
	public void setTv_xx(TextView tv_xx) {
		this.tv_xx = tv_xx;
	}
	public TextView getTv_drive() {
		return tv_drive;
	}
	public void setTv_drive(TextView tv_drive) {
		this.tv_drive = tv_drive;
	}
	
	
}
