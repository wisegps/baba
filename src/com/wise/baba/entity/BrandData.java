package com.wise.baba.entity;

public class BrandData {
	private String brand;
	private String letter;
	private String id;
	private String logoUrl;
	
	public String getBrand() {
		return brand;
	}
	public void setBrand(String brand) {
		this.brand = brand;
	}
	public String getLetter() {
		return letter;
	}
	public void setLetter(String letter) {
		this.letter = letter;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getLogoUrl() {
		return logoUrl;
	}
	public void setLogoUrl(String logoUrl) {
		this.logoUrl = logoUrl;
	}
	@Override
	public String toString() {
		return "BrandData [brand=" + brand + ", letter=" + letter + ", id="
				+ id + ", logoUrl=" + logoUrl + "]";
	}			
}