package data;
/**
 *@author honesty
 **/
public class FriendData {
	private int sex;
	private String logo;
	private String friend_name;		
	private int friend_type;		
	private int friend_id;		
	private int user_id;		
	private int friend_relat_id;
	public int getSex() {
		return sex;
	}
	public void setSex(int sex) {
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
	public int getFriend_type() {
		return friend_type;
	}
	public void setFriend_type(int friend_type) {
		this.friend_type = friend_type;
	}
	public int getFriend_id() {
		return friend_id;
	}
	public void setFriend_id(int friend_id) {
		this.friend_id = friend_id;
	}
	public int getUser_id() {
		return user_id;
	}
	public void setUser_id(int user_id) {
		this.user_id = user_id;
	}
	public int getFriend_relat_id() {
		return friend_relat_id;
	}
	public void setFriend_relat_id(int friend_relat_id) {
		this.friend_relat_id = friend_relat_id;
	}
	@Override
	public String toString() {
		return "FriendData [sex=" + sex + ", logo=" + logo + ", friend_name="
				+ friend_name + ", friend_type=" + friend_type + ", friend_id="
				+ friend_id + ", user_id=" + user_id + ", friend_relat_id="
				+ friend_relat_id + "]";
	}	
}