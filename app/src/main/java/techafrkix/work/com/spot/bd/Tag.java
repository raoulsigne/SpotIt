package techafrkix.work.com.spot.bd;

/**
 * Created by techafrkix0 on 24/05/2016.
 */
public class Tag {

    private int id;
    private int spot_id;
    private String tag;
    private String created;

    public Tag(int id, int spot_id, String tag, String created) {
        this.id = id;
        this.spot_id = spot_id;
        this.tag = tag;
        this.created = created;
    }

    public Tag() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSpot_id() {
        return spot_id;
    }

    public void setSpot_id(int spot_id) {
        this.spot_id = spot_id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    @Override
    public String toString() {
        return "Tag{" +
                "id=" + id +
                ", spot_id=" + spot_id +
                ", tag='" + tag + '\'' +
                ", created='" + created + '\'' +
                '}';
    }
}
