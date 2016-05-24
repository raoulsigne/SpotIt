package techafrkix.work.com.spot.bd;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by techafrkix0 on 27/04/2016.
 */
public class UtilisateurDBAdapteur {

    private static final int BASE_VERSION = 3;
    private static final String BASE_NOM = "spots.db";

    public static final int COLONNE_ID_ID = 0;
    public static final int COLONNE_EMAIL_ID = 1;
    public static final int COLONNE_PASSWORD_ID = 2;
    public static final int COLONNE_DATE_NAISSANCE_ID = 3;
    public static final int COLONNE_PSEUDO_ID = 4;
    public static final int COLONNE_NB_SPOT_ID = 5;
    public static final int COLONNE_NB_RESPOT_ID = 6;
    public static final int COLONNE_TYPECONNEXION_ID_ID = 7;
    public static final int COLONNE_PHOTO_PROFILE_ID = 8;
    public static final int COLONNE_CREATED_ID = 9;

    // L�instance de la base qui sera manipul�e au travers de cette classe.
    private SQLiteDatabase maBaseDonnees;
    private MaBaseOpenHelper baseHelper;

    public UtilisateurDBAdapteur(Context ctx) {
        baseHelper = new MaBaseOpenHelper(ctx, BASE_NOM, null, BASE_VERSION);
    }

    /**
     * Ouvre la base de donn�es en �criture.
     */
    public SQLiteDatabase open() {
        maBaseDonnees = baseHelper.getWritableDatabase();
        return maBaseDonnees;
    }

    /**
     * Ferme la base de donn�es.
     */
    public void close() {
        maBaseDonnees.close();
    }

    public SQLiteDatabase getBaseDonnees() {
        return maBaseDonnees;
    }

    /**
     * R�cup�re un spot en fonction de sa photo.
     */
    public Utilisateur getUtilisateur(String pseudo) {
        Cursor c = maBaseDonnees.query(MaBaseOpenHelper.TABLE_UTILISATEURS, new String[] {
                        MaBaseOpenHelper.COLONNE_USER_ID, MaBaseOpenHelper.COLONNE_EMAIL, MaBaseOpenHelper.COLONNE_PASSWORD, MaBaseOpenHelper.COLONNE_DATE_NAISSANCE,
                        MaBaseOpenHelper.COLONNE_PSEUDO, MaBaseOpenHelper.COLONNE_NB_SPOT, MaBaseOpenHelper.COLONNE_NB_RESPOT,
                        MaBaseOpenHelper.COLONNE_TYPECONNEXION_ID, MaBaseOpenHelper.COLONNE_PHOTO_PROFILE, MaBaseOpenHelper.COLONNE_CREATED}, null, null, null,
                MaBaseOpenHelper.COLONNE_PSEUDO + " LIKE " + pseudo, null);
        return cursorToUtilisateur(c);
    }

    /**
     * R�cup�re un spot en fonction de sa photo.
     */
    public Utilisateur getUtilisateur(String email, String password) {
        String whereClause = MaBaseOpenHelper.COLONNE_EMAIL+"=? AND " + MaBaseOpenHelper.COLONNE_PASSWORD+"=?";
        String [] whereArgs = {email,password};

        Cursor c = maBaseDonnees.query(MaBaseOpenHelper.TABLE_UTILISATEURS, new String[] {
                        MaBaseOpenHelper.COLONNE_USER_ID, MaBaseOpenHelper.COLONNE_EMAIL, MaBaseOpenHelper.COLONNE_PASSWORD, MaBaseOpenHelper.COLONNE_DATE_NAISSANCE,
                        MaBaseOpenHelper.COLONNE_PSEUDO, MaBaseOpenHelper.COLONNE_NB_SPOT, MaBaseOpenHelper.COLONNE_NB_RESPOT,
                        MaBaseOpenHelper.COLONNE_TYPECONNEXION_ID, MaBaseOpenHelper.COLONNE_PHOTO_PROFILE, MaBaseOpenHelper.COLONNE_CREATED}, whereClause, whereArgs, null,
                null, null);
        return cursorToUtilisateur(c);
    }

    /**
     * R�cup�re un spot en fonction de son id.
     */
    public Utilisateur getUtilisateur(int id) {
        Cursor c = maBaseDonnees.query(MaBaseOpenHelper.TABLE_UTILISATEURS, new String[] {
                        MaBaseOpenHelper.COLONNE_USER_ID, MaBaseOpenHelper.COLONNE_EMAIL, MaBaseOpenHelper.COLONNE_PASSWORD, MaBaseOpenHelper.COLONNE_DATE_NAISSANCE,
                        MaBaseOpenHelper.COLONNE_PSEUDO, MaBaseOpenHelper.COLONNE_NB_SPOT, MaBaseOpenHelper.COLONNE_NB_RESPOT,
                        MaBaseOpenHelper.COLONNE_TYPECONNEXION_ID, MaBaseOpenHelper.COLONNE_PHOTO_PROFILE, MaBaseOpenHelper.COLONNE_CREATED}, null, null, null,
                MaBaseOpenHelper.COLONNE_USER_ID + " = " + id, null);
        return cursorToUtilisateur(c);
    }
    /**
     * Retourne tous les spots de la base de donn�es.
     */
    public ArrayList<Utilisateur> getUtilisateurs() {
        Cursor c = maBaseDonnees.query(MaBaseOpenHelper.TABLE_UTILISATEURS, new String[] {
                        MaBaseOpenHelper.COLONNE_USER_ID, MaBaseOpenHelper.COLONNE_EMAIL, MaBaseOpenHelper.COLONNE_PASSWORD, MaBaseOpenHelper.COLONNE_DATE_NAISSANCE,
                        MaBaseOpenHelper.COLONNE_PSEUDO, MaBaseOpenHelper.COLONNE_NB_SPOT, MaBaseOpenHelper.COLONNE_NB_RESPOT,
                        MaBaseOpenHelper.COLONNE_TYPECONNEXION_ID, MaBaseOpenHelper.COLONNE_PHOTO_PROFILE, MaBaseOpenHelper.COLONNE_CREATED}, null, null, null,
                null, null);
        return cursorToUtilisateurs(c);
    }

    /**
     * @param c
     * Le curseur � utiliser pour r�cup�rer les donn�es de utilisateur.
     * @return Une instance d�un utilisateur avec les valeurs du curseur.
     */
    private Utilisateur cursorToUtilisateur(Cursor c) {
        // Si la requ�te ne renvoie pas de r�sultat.
        if (c.getCount() == 0)
            return null;
        Utilisateur utilisateur = new Utilisateur();
        // Extraction des valeurs depuis le curseur.
        c.moveToFirst();
        utilisateur.setId(c.getInt(COLONNE_ID_ID));
        utilisateur.setEmail(c.getString(COLONNE_EMAIL_ID));
        utilisateur.setPassword(c.getString(COLONNE_PASSWORD_ID));
        utilisateur.setDate_naissance(c.getString(COLONNE_DATE_NAISSANCE_ID));
        utilisateur.setPseudo(c.getString(COLONNE_PSEUDO_ID));
        utilisateur.setNbspot(c.getInt(COLONNE_NB_SPOT_ID));
        utilisateur.setNbrespot(c.getInt(COLONNE_NB_RESPOT_ID));
        utilisateur.setTypeconnexion_id(c.getInt(COLONNE_TYPECONNEXION_ID_ID));
        utilisateur.setPhoto(c.getString(COLONNE_PHOTO_PROFILE_ID));
        utilisateur.setCreated(c.getString(COLONNE_CREATED_ID));
        // Ferme le curseur pour lib�rer les ressources.
        c.close();
        return utilisateur;
    }

    private ArrayList<Utilisateur> cursorToUtilisateurs(Cursor c) {
        // Si la requ�te ne renvoie pas de r�sultat.
        if (c.getCount() == 0)
            return new ArrayList<Utilisateur>(0);
        ArrayList<Utilisateur> utilisateurs = new ArrayList<Utilisateur>(c.getCount());
        c.moveToFirst();

        do {
            Utilisateur utilisateur = new Utilisateur();
            utilisateur.setId(c.getInt(COLONNE_ID_ID));
            utilisateur.setEmail(c.getString(COLONNE_EMAIL_ID));
            utilisateur.setPassword(c.getString(COLONNE_PASSWORD_ID));
            utilisateur.setDate_naissance(c.getString(COLONNE_DATE_NAISSANCE_ID));
            utilisateur.setPseudo(c.getString(COLONNE_PSEUDO_ID));
            utilisateur.setNbspot(c.getInt(COLONNE_NB_SPOT_ID));
            utilisateur.setNbrespot(c.getInt(COLONNE_NB_RESPOT_ID));
            utilisateur.setTypeconnexion_id(c.getInt(COLONNE_TYPECONNEXION_ID_ID));
            utilisateur.setPhoto(c.getString(COLONNE_PHOTO_PROFILE_ID));
            utilisateur.setCreated(c.getString(COLONNE_CREATED_ID));
            utilisateurs.add(utilisateur);
        } while (c.moveToNext());

        // Ferme le curseur pour lib�rer les ressources.
        c.close();
        return utilisateurs;
    }

    /**
     * Ins�re un spot dans la table des spots.
     */
    public long insertUtilisateur(Utilisateur utilisateur) {
        ContentValues valeurs = new ContentValues();
        valeurs.put(MaBaseOpenHelper.COLONNE_EMAIL, utilisateur.getEmail());
        valeurs.put(MaBaseOpenHelper.COLONNE_PASSWORD, utilisateur.getPassword());
        valeurs.put(MaBaseOpenHelper.COLONNE_DATE_NAISSANCE, utilisateur.getDate_naissance());
        valeurs.put(MaBaseOpenHelper.COLONNE_PSEUDO, utilisateur.getPseudo());
        valeurs.put(MaBaseOpenHelper.COLONNE_NB_SPOT, utilisateur.getNbspot());
        valeurs.put(MaBaseOpenHelper.COLONNE_NB_RESPOT, utilisateur.getNbrespot());
        valeurs.put(MaBaseOpenHelper.COLONNE_TYPECONNEXION_ID, utilisateur.getTypeconnexion_id());
        valeurs.put(MaBaseOpenHelper.COLONNE_PHOTO_PROFILE, utilisateur.getPhoto());
        valeurs.put(MaBaseOpenHelper.COLONNE_CREATED, utilisateur.getCreated());
        if (maBaseDonnees == null)
            Log.i("insertion du spot ", " null");
        return maBaseDonnees.insert(MaBaseOpenHelper.TABLE_UTILISATEURS, null, valeurs);
    }

    public long insertUtilisateur(ContentValues valeurs) {
        return maBaseDonnees.insert(MaBaseOpenHelper.TABLE_UTILISATEURS, null, valeurs);
    }

    /**
     * Met � jour un spot dans la table des spots.
     */
    public int updateUtilisateur(int id, Utilisateur utilisateur) {
        ContentValues valeurs = new ContentValues();
        valeurs.put(MaBaseOpenHelper.COLONNE_EMAIL, utilisateur.getEmail());
        valeurs.put(MaBaseOpenHelper.COLONNE_PASSWORD, utilisateur.getPassword());
        valeurs.put(MaBaseOpenHelper.COLONNE_DATE_NAISSANCE, utilisateur.getDate_naissance());
        valeurs.put(MaBaseOpenHelper.COLONNE_PSEUDO, utilisateur.getPseudo());
        valeurs.put(MaBaseOpenHelper.COLONNE_NB_SPOT, utilisateur.getNbspot());
        valeurs.put(MaBaseOpenHelper.COLONNE_NB_RESPOT, utilisateur.getNbrespot());
        valeurs.put(MaBaseOpenHelper.COLONNE_TYPECONNEXION_ID, utilisateur.getTypeconnexion_id());
        valeurs.put(MaBaseOpenHelper.COLONNE_PHOTO_PROFILE, utilisateur.getPhoto());
        valeurs.put(MaBaseOpenHelper.COLONNE_CREATED, utilisateur.getCreated());
        return maBaseDonnees.update(MaBaseOpenHelper.TABLE_UTILISATEURS, valeurs, MaBaseOpenHelper.COLONNE_USER_ID + " = "
                + id, null);
    }

    public int updateUtilisateur(ContentValues valeurs, String where,
                          String[] whereArgs) {
        return maBaseDonnees.update(MaBaseOpenHelper.TABLE_UTILISATEURS, valeurs, where, whereArgs);
    }
    /**
     * Supprime un spot � partir de son hash.
     */
    public int removeUtilisateur(String pseudo) {
        return maBaseDonnees.delete(MaBaseOpenHelper.TABLE_UTILISATEURS, MaBaseOpenHelper.COLONNE_PSEUDO + " LIKE "
                + pseudo, null);
    }
    /**
     * Supprime un spot � partir de sa photo.
     */
    public int removeUtilisateur2(String password) {
        return maBaseDonnees.delete(MaBaseOpenHelper.TABLE_UTILISATEURS, MaBaseOpenHelper.COLONNE_PASSWORD + " LIKE "
                + password, null);
    }
    /**
     * Supprime un spot � partir de son id.
     */
    public int removeUtilisateur(int id) {
        return maBaseDonnees.delete(MaBaseOpenHelper.TABLE_UTILISATEURS, MaBaseOpenHelper.COLONNE_USER_ID + " = " + id,
                null);
    }
    /**
     * compte le nombre de spots dans la bd
     */
    public int countUtilisateurs() {
        return getUtilisateurs().size();
    }
}
