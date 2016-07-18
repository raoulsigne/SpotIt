package techafrkix.work.com.spot.bd;

import java.io.Serializable;

/**
 * Created by techafrkix0 on 24/05/2016.
 */
public class Notification implements Serializable{

    private int id;
    private int user_id;
    private int typenotification_id;
    private String created;
    private String data;
    private int sender_id;
    private String description;
    private String photosender;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getTypenotification_id() {
        return typenotification_id;
    }

    public void setTypenotification_id(int typenotification_id) {
        this.typenotification_id = typenotification_id;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getSender_id() {
        return sender_id;
    }

    public void setSender_id(int sender_id) {
        this.sender_id = sender_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhotosender() {
        return photosender;
    }

    public void setPhotosender(String photosender) {
        this.photosender = photosender;
    }

    public Notification(int id, int user_id, int typenotification_id, String created, String data, int sender_id, String descrription,
                        String photosender) {
        this.id = id;
        this.user_id = user_id;
        this.typenotification_id = typenotification_id;
        this.created = created;
        this.data = data;
        this.sender_id = sender_id;
        this.description = descrription;
        this.photosender = photosender;
    }

    public Notification() {
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", user_id=" + user_id +
                ", typenotification_id=" + typenotification_id +
                ", created='" + created + '\'' +
                ", data='" + data + '\'' +
                ", sender_id=" + sender_id +
                ", descrription='" + description + '\'' +
                ", photosender='" + photosender + '\'' +
                '}';
    }
}
