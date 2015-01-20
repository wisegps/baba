package data;

public class CardsData {
	private int icon;
	private String title;
	private String content;
	private String cardName;
	private boolean isAdd;
	private int cardPosition;

	public int getCardPosition() {
		return cardPosition;
	}

	public void setCardPosition(int cardPosition) {
		this.cardPosition = cardPosition;
	}

	public boolean isAdd() {
		return isAdd;
	}

	public void setAdd(boolean isAdd) {
		this.isAdd = isAdd;
	}

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
