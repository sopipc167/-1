package comnet;

public class ResultImage {
	public String title;
	public String imgUrl;
	public String HEXcode;

	public ResultImage(String title, String imgUrl, String HEXcode) {
		this.title = title;
		this.imgUrl = imgUrl;
		this.HEXcode = HEXcode;
	}

	public String getTitle() {
		return title;
	}

	public String getImgUrl() {
		return imgUrl;
	}

	public String getHEXcode() {
		return HEXcode;
	}
}
