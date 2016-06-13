package techafrkix.work.com.spot.bd;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import techafrkix.work.com.spot.spotit.DetailSpot_New;

public class Spot implements Serializable{

	private int id;
	private String longitude;
	private String latitude;
    private String visibilite;
	private String photokey;
    private String photouser;
	private String geohash;
	private String date;
	private int respot;
	private int user_id;
    private int visibilite_id;
	private ArrayList<String> tags;

    public Spot(String longitude, String latitude, String visibilite, String photokey,
			String geohash, String date) {
		super();
		this.longitude = longitude;
		this.latitude = latitude;
        this.visibilite = visibilite;
		this.photokey = photokey;
		this.geohash = geohash;
		this.date = date;
		this.respot = 0;
        this.tags = new ArrayList<>();
	}

    public Spot(String longitude, String latitude, String visibilite, String photokey, String geohash,
                String date, int respot, int user_id, String photouser, ArrayList<String> tags) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.visibilite = visibilite;
        this.photokey = photokey;
        this.geohash = geohash;
        this.date = date;
        this.respot = respot;
        this.user_id = user_id;
        this.tags = tags;
        this.photouser = photouser;
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

	public int getRespot() {
		return respot;
	}

	public void setRespot(int respot) {
		this.respot = respot;
	}

	public String getVisibilite() {
        return visibilite;
    }

    public void setVisibilite(String visibilite) {
        this.visibilite = visibilite;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getVisibilite_id() {
        return visibilite_id;
    }

    public void setVisibilite_id(int visibilite_id) {
        this.visibilite_id = visibilite_id;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }

    public String getPhotouser() {
        return photouser;
    }

    public void setPhotouser(String photouser) {
        this.photouser = photouser;
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
		this.respot = 0;
        this.user_id = 0;
        this.photouser = "";
        this.tags = new ArrayList<>();
	}

	public int getvisibiliteId(){
		if (visibilite == DetailSpot_New.V_MOI)
			return 21;
		else if (visibilite == DetailSpot_New.V_FRIEND)
			return 11;
		else
			return 1;
	}

    @Override
    public String toString() {
        return "Spot{" +
                "id=" + id +
                ", longitude='" + longitude + '\'' +
                ", latitude='" + latitude + '\'' +
                ", visibilite='" + visibilite + '\'' +
                ", photokey='" + photokey + '\'' +
                ", geohash='" + geohash + '\'' +
                ", date='" + date + '\'' +
                ", respot=" + respot +
                ", user_id=" + user_id +
                ", visibilite_id=" + visibilite_id +
                ", tags=" + tags +
                '}';
    }
}
