package data;

/**
 * 能耗 Bean
 * @author keven.cheng
 */
public class EnergyItem {
	public int date;	//时间值
	public float value;	//能量
	
	public EnergyItem() {
		super();
	}
	public EnergyItem(int date, float value) {
		super();
		this.date = date;
		this.value = value;
	}
	@Override
	public String toString() {
		return "EnergyItem [date=" + date + ", value=" + value + "]";
	}    	
}