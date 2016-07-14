package techafrkix.work.com.spot.bd;

import java.io.Serializable;

/**
 * Created by techafrkix0 on 27/04/2016.
 */
public class Utilisateur implements Serializable {

    //attributs
    private int id;
    private String email;
    private String password;
    private String date_naissance;
    private String pseudo;
    private int nbspot;
    private int nbrespot;
    private int spot;
    private int nbfriends;
    private int typeconnexion_id;
    private String photo;
    private String created;

    private int friendship_id;
    private int statut;
    private String androidid;

    public Utilisateur(String email, String password, String date_naissance) {
        this.email = email;
        this.password = password;
        this.date_naissance = date_naissance;
        this.photo = "";
    }

    public Utilisateur(String email, String password, String date_naissance, String pseudo) {
        this.email = email;
        this.password = password;
        this.date_naissance = date_naissance;
        this.pseudo = pseudo;
        this.photo = "";
    }

    public Utilisateur(int id) {
        this.id = id;
    }

    public Utilisateur() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDate_naissance() {
        return date_naissance;
    }

    public void setDate_naissance(String date_naissance) {
        this.date_naissance = date_naissance;
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public int getNbspot() {
        return nbspot;
    }

    public void setNbspot(int nbspot) {
        this.nbspot = nbspot;
    }

    public int getNbrespot() {
        return nbrespot;
    }

    public void setNbrespot(int nbrespot) {
        this.nbrespot = nbrespot;
    }

    public int getSpot(){
        return this.nbspot + this.nbrespot;
    }

    public int getTypeconnexion_id() {
        return typeconnexion_id;
    }

    public void setTypeconnexion_id(int typeconnexion_id) {
        this.typeconnexion_id = typeconnexion_id;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public int getNbfriends() {
        return nbfriends;
    }

    public void setNbfriends(int nbfriends) {
        this.nbfriends = nbfriends;
    }

    public int getFriendship_id() {
        return friendship_id;
    }

    public void setFriendship_id(int friendship_id) {
        this.friendship_id = friendship_id;
    }

    public int getStatut() {
        return statut;
    }

    public void setStatut(int statut) {
        this.statut = statut;
    }

    public String getAndroidid() {
        return androidid;
    }

    public void setAndroidid(String androidid) {
        this.androidid = androidid;
    }

    @Override
    public String toString() {
        return "Utilisateur{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", date_naissance='" + date_naissance + '\'' +
                ", pseudo='" + pseudo + '\'' +
                ", nbspot=" + nbspot +
                ", nbrespot=" + nbrespot +
                ", nbfriends=" + nbfriends +
                ", typeconnexion_id=" + typeconnexion_id +
                ", photo='" + photo + '\'' +
                ", created='" + created + '\'' +
                '}';
    }
}
