package com.wise.baba.entity;

public class CardsData {
	private int icon;
	private String title;
	private String content;
	private String cardName;

	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
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

	public String getCardName() {
		return cardName;
	}

	public void setCardName(String cardName) {
		this.cardName = cardName;
	}

	@Override
	public String toString() {
		return "CardsData [icon=" + icon + ", title=" + title + ", content="
				+ content + ", cardName=" + cardName + "]";
	}

}
