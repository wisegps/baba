package com.wise.baba.entity;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 说说数据
 * @author Mr.Wang
 */
public class Article implements Serializable{
	private String data;   //好友主页处理同天文章合并用
	private String create_time;
	private String userLogo;
	private String city;
	private String lat;
	private String lon;
	private String content;
	private String title;
	private String name;
	private int cust_id;
	private int blog_id;
	private String _id;
	private Map<String,String> praisesList;
	private List<String[]> commentList;
	private List<Map<String,String>> imageList;
	private int _v;
	private String updateTime;
	
	private String JSONDatas;   //一篇文章的所有数据
	public String getCreate_time() {
		return create_time;
	}
	public void setCreate_time(String create_time) {
		this.create_time = create_time;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getLat() {
		return lat;
	}
	public void setLat(String lat) {
		this.lat = lat;
	}
	public String getLon() {
		return lon;
	}
	public void setLon(String lon) {
		this.lon = lon;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getCust_id() {
		return cust_id;
	}
	public void setCust_id(int cust_id) {
		this.cust_id = cust_id;
	}
	public int getBlog_id() {
		return blog_id;
	}
	public void setBlog_id(int blog_id) {
		this.blog_id = blog_id;
	}
	public Map<String,String> getPraisesList() {
		return praisesList;
	}
	public void setPraisesList(Map<String,String> praisesList) {
		this.praisesList = praisesList;
	}
	public List<String[]> getCommentList() {
		return commentList;
	}
	public void setCommentList(List<String[]> commentList) {
		this.commentList = commentList;
	}
	public List<Map<String, String>> getImageList() {
		return imageList;
	}
	public void setImageList(List<Map<String, String>> imageList) {
		this.imageList = imageList;
	}
	public int get_v() {
		return _v;
	}
	public void set_v(int _v) {
		this._v = _v;
	}
	public String getJSONDatas() {
		return JSONDatas;
	}
	public void setJSONDatas(String jSONDatas) {
		JSONDatas = jSONDatas;
	}
	public String get_id() {
		return _id;
	}
	public void set_id(String _id) {
		this._id = _id;
	}
	public String getUserLogo() {
		return userLogo;
	}
	public void setUserLogo(String userLogo) {
		this.userLogo = userLogo;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public String getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}
	@Override
	public String toString() {
		return "Article [data=" + data + ", create_time=" + create_time
				+ ", userLogo=" + userLogo + ", city=" + city + ", lat=" + lat
				+ ", lon=" + lon + ", content=" + content + ", title=" + title
				+ ", name=" + name + ", cust_id=" + cust_id + ", blog_id="
				+ blog_id + ", _id=" + _id + ", praisesList=" + praisesList
				+ ", commentList=" + commentList + ", imageList=" + imageList
				+ ", _v=" + _v + ", updateTime=" + updateTime + ", JSONDatas="
				+ JSONDatas + "]";
	}	
}