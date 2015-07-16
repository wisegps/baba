package com.wise.baba.db.dao;

import java.io.Serializable;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table FRIEND_DATA.
 */
public class FriendData implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String create_time;
    private Integer sex;
    private String logo;
    private String friend_name;
    private Integer friend_type;
    private Integer friend_id;
    private Integer user_id;
    private Integer friend_relat_id;
    private String Group_letter;

    public FriendData() {
    }

    public FriendData(String create_time, Integer sex, String logo, String friend_name, Integer friend_type, Integer friend_id, Integer user_id, Integer friend_relat_id, String Group_letter) {
        this.create_time = create_time;
        this.sex = sex;
        this.logo = logo;
        this.friend_name = friend_name;
        this.friend_type = friend_type;
        this.friend_id = friend_id;
        this.user_id = user_id;
        this.friend_relat_id = friend_relat_id;
        this.Group_letter = Group_letter;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getFriend_name() {
        return friend_name;
    }

    public void setFriend_name(String friend_name) {
        this.friend_name = friend_name;
    }

    public Integer getFriend_type() {
        return friend_type;
    }

    public void setFriend_type(Integer friend_type) {
        this.friend_type = friend_type;
    }

    public Integer getFriend_id() {
        return friend_id;
    }

    public void setFriend_id(Integer friend_id) {
        this.friend_id = friend_id;
    }

    public Integer getUser_id() {
        return user_id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    public Integer getFriend_relat_id() {
        return friend_relat_id;
    }

    public void setFriend_relat_id(Integer friend_relat_id) {
        this.friend_relat_id = friend_relat_id;
    }

    public String getGroup_letter() {
        return Group_letter;
    }

    public void setGroup_letter(String Group_letter) {
        this.Group_letter = Group_letter;
    }

}
