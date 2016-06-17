package techafrkix.work.com.spot.bd;

import android.util.Log;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

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

    public static String difftime(String date){
        Calendar c = Calendar.getInstance();
        int jour, moi, annee, heure, min, sec;
        int jour1 = 0, moi1 = 0, annee1 = 0, heure1 = 0, min1 = 0, sec1 = 0;
        String retour = "";


        TimeZone z = c.getTimeZone();
        int offset = z.getRawOffset();
        if(z.inDaylightTime(new Date())){
            offset = offset + z.getDSTSavings();
        }
        int offsetHrs = offset / 1000 / 60 / 60;
        int offsetMins = offset / 1000 / 60 % 60;
        c.add(Calendar.HOUR_OF_DAY, (-offsetHrs));
        c.add(Calendar.MINUTE, (-offsetMins));

        Log.i("date","GMT Time: "+c.getTime());

        String[] tableau = date.split("T");
        String[] tab1 = tableau[1].split("\\.");
        String[] tab2 = tableau[0].split("-");
        String[] tab3 = tab1[0].split(":");
        annee1 = Integer.valueOf(tab2[0]);
        moi1 = Integer.valueOf(tab2[1]);
        jour1 = Integer.valueOf(tab2[2]);
        heure1 = Integer.valueOf(tab3[0]);
        min1 = Integer.valueOf(tab3[1]);
        sec1 = Integer.valueOf(tab1[1].split("Z")[0]);

        jour = c.get(Calendar.DAY_OF_MONTH);
        moi = c.get(Calendar.MONTH) + 1;
        annee = c.get(Calendar.YEAR);
        heure = c.get(Calendar.HOUR_OF_DAY);
        min = c.get(Calendar.MINUTE);
        sec = c.get(Calendar.SECOND);

        retour = jour1 + " " + monthinleter(moi1) + " " + annee1 + " at " + heure1 + "h" + min1;

        int diffday = jour - jour1;
        int diffmin = min - min1;
        int diffsec = sec - sec1;
        int diffhour = heure - heure1;

        if ((annee == annee1) & (moi == moi1)){
            if (diffday == 0){
                if (diffhour == 0){
                    if (diffmin == 0)
                        if (diffsec > 1)
                            retour = String.valueOf(diffsec) + " seconds ago";
                        else
                            retour = String.valueOf(diffsec) + " second ago";
                    else {
                        if (diffmin > 1)
                            retour = String.valueOf(diffmin) + " minutes ago";
                        else
                            retour = String.valueOf(diffmin) + " minute ago";
                    }
                }
                else {
                    if (diffhour > 1)
                        retour = String.valueOf(diffhour) + " hours ago";
                    else
                        retour = String.valueOf(diffhour) + " hour ago";
                }
            }else if (diffday == 1){
                retour = "yesterday at " + heure1 + "h" + min1;
            }
        }

        Log.i("date", retour);

        return retour;
    }

    private static String monthinleter(int month){
        switch (month){
            case 1:
                return "January";
            case 2:
                return "Febuary";
            case 3:
                return "March";
            case 4:
                return "April";
            case 5:
                return "May";
            case 6:
                return "June";
            case 7:
                return "July";
            case 8:
                return "August";
            case 9:
                return "September";
            case 10:
                return "October";
            case 11:
                return "November";
            case 12:
                return "December";
        }
        return " ";
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
