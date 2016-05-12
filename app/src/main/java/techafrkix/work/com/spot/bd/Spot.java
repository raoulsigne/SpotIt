package techafrkix.work.com.spot.bd;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Spot implements Serializable{

	private int id;
	private String longitude;
	private String latitude;
    private String visibilite;
	private String photokey;
	private String geohash;
	private String date;

    @Override
    public String toString() {
        return "Spot{" +
                "longitude='" + longitude + '\'' +
                ", latitude='" + latitude + '\'' +
                ", visibilite='" + visibilite + '\'' +
                ", photo='" + photokey + '\'' +
                ", geohash='" + geohash + '\'' +
                ", date='" + date + '\'' +
                '}';
    }

    public Spot(String longitude, String latitude, String visibilite, String photokey,
			String geohash, String date) {
		super();
		this.longitude = longitude;
		this.latitude = latitude;
        this.visibilite = visibilite;
		this.photokey = photokey;
		this.geohash = geohash;
		this.date = date;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getLongitude() {
		return longitude;
	}
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	public String getLatitude() {
		return latitude;
	}
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	public String getPhotokey() {
		return photokey;
	}
	public void setPhotokey(String photokey) {
		this.photokey = photokey;
	}
	public String getGeohash() {
		return geohash;
	}
	public void setGeohash(String geohash) {
		this.geohash = geohash;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}

    public String getVisibilite() {
        return visibilite;
    }

    public void setVisibilite(String visibilite) {
        this.visibilite = visibilite;
    }

    public Spot() {
		super();
		this.longitude = "";
		this.latitude = "";
        this.visibilite = "";
		this.photokey = "";
		this.geohash = "";
		Calendar c = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy à HH:mm");
		this.date = sdf.format(c.getTime());
	}
}
