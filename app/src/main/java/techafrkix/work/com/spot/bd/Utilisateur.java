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

    public Utilisateur(String email, String password, String date_naissance) {
        this.email = email;
        this.password = password;
        this.date_naissance = date_naissance;
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

    @Override
    public String toString() {
        return "Utilisateur{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", date_naissance='" + date_naissance + '\'' +
                '}';
    }
}
