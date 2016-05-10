package techafrkix.work.com.spot.techafrkix.work.com.spot.utils;

/**
 * Created by techafrkix0 on 09/05/2016.
 */
public class MyMarker {

    private String mDate;
    private String mGeohash;
    private String mIcon;
    private Double mLatitude;
    private Double mLongitude;

    public MyMarker(String date, String geohash, String icon, Double latitude, Double longitude)
    {
        this.mDate = date;
        this.mGeohash = geohash;
        this.mLatitude = latitude;
        this.mLongitude = longitude;
        this.mIcon = icon;
    }

    public String getmDate() {
        return mDate;
    }

    public void setmDate(String mDate) {
        this.mDate = mDate;
    }

    public String getmGeohash() {
        return mGeohash;
    }

    public void setmGeohash(String mGeohash) {
        this.mGeohash = mGeohash;
    }

    public String getmIcon() {
        return mIcon;
    }

    public void setmIcon(String mIcon) {
        this.mIcon = mIcon;
    }

    public Double getmLatitude() {
        return mLatitude;
    }

    public void setmLatitude(Double mLatitude) {
        this.mLatitude = mLatitude;
    }

    public Double getmLongitude() {
        return mLongitude;
    }

    public void setmLongitude(Double mLongitude) {
        this.mLongitude = mLongitude;
    }
}
