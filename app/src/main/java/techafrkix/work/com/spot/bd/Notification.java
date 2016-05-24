package techafrkix.work.com.spot.bd;

/**
 * Created by techafrkix0 on 24/05/2016.
 */
public class Notification {

    private int id;
    private int typenotification_id;
    private int user_id;
    private String message;
    private String created;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTypenotification_id() {
        return typenotification_id;
    }

    public void setTypenotification_id(int typenotification_id) {
        this.typenotification_id = typenotification_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", typenotification_id=" + typenotification_id +
                ", user_id=" + user_id +
                ", message='" + message + '\'' +
                ", created='" + created + '\'' +
                '}';
    }

    public Notification(int id, int typenotification_id, int user_id, String message, String created) {
        this.id = id;
        this.typenotification_id = typenotification_id;
        this.user_id = user_id;
        this.message = message;
        this.created = created;
    }

    public Notification() {
    }
}
