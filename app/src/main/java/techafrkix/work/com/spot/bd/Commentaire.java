package techafrkix.work.com.spot.bd;

import java.io.Serializable;

/**
 * Created by techafrkix0 on 24/05/2016.
 */
public class Commentaire implements Serializable{

    private int spot_id;
    private int user_id;
    private String pseudo;
    private String photokey;
    private String commentaire;
    private String created;

    public Commentaire() {
    }

    public Commentaire(int spot_id, int user_id, String pseudo, String photokey, String commentaire, String created) {
        this.spot_id = spot_id;
        this.user_id = user_id;
        this.pseudo = pseudo;
        this.photokey = photokey;
        this.commentaire = commentaire;
        this.created = created;
    }

    public Commentaire(int spot_id, int user_id, String commentaire, String created) {
        this.spot_id = spot_id;
        this.user_id = user_id;
        this.commentaire = commentaire;
        this.created = created;
    }

    public int getSpot_id() {
        return spot_id;
    }

    public void setSpot_id(int spot_id) {
        this.spot_id = spot_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public String getPhotokey() {
        return photokey;
    }

    public void setPhotokey(String photokey) {
        this.photokey = photokey;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    @Override
    public String toString() {
        return "Commentaire{" +
                "spot_id=" + spot_id +
                ", user_id=" + user_id +
                ", pseudo='" + pseudo + '\'' +
                ", photokey='" + photokey + '\'' +
                ", commentaire='" + commentaire + '\'' +
                ", created='" + created + '\'' +
                '}';
    }
}
