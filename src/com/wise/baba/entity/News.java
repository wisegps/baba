/**
 * 
 */
package com.wise.baba.entity;

import java.io.Serializable;

/**
 *
 * @author c
 * @desc   baba
 * @date   2015-4-22
 *
 */
public class News implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String newsId;
	private String city;
	private String title ;
	private String content ;
	private String url ;
	private String createTime ;
	
	public String getNewsId() {
		return newsId;
	}
	public void setNewsId(String newsId) {
		this.newsId = newsId;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	
	
}
