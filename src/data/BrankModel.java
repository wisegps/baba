package data;

public class BrankModel {
	private String vehicleBrank;
	private String vehicleLetter;
	private String brankId;
	private String logoUrl;
	
	public String getLogoUrl() {
		return logoUrl;
	}
	public void setLogoUrl(String logoUrl) {
		this.logoUrl = logoUrl;
	}
	public String getBrankId() {
		return brankId;
	}
	public void setBrankId(String brankId) {
		this.brankId = brankId;
	}
	public String getVehicleBrank() {
		return vehicleBrank;
	}
	public void setVehicleBrank(String vehicleBrank) {
		this.vehicleBrank = vehicleBrank;
	}
	public String getVehicleLetter() {
		return vehicleLetter;
	}
	public void setVehicleLetter(String vehicleLetter) {
		this.vehicleLetter = vehicleLetter;
	}
    @Override
    public String toString() {
        return "BrankModel [vehicleBrank=" + vehicleBrank + ", vehicleLetter="
                + vehicleLetter + "]";
    }
	
}
