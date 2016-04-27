package techafrkix.work.com.spot.bd;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class SpotsDBAdapteur {

	private static final int BASE_VERSION = 3;
	private static final String BASE_NOM = "spots.db";
	
	private static final String TABLE_SPOTS = "table_spots";
	
	private static final String COLONNE_ID = "id";
	public static final int COLONNE_ID_ID = 0;
	private static final String COLONNE_LONGITUDE = "longitude";
	public static final int COLONNE_LONGITUDE_ID = 1;
	private static final String COLONNE_LATITUDE = "latitude";
	public static final int COLONNE_LATITUDE_ID = 2;
    private static final String COLONNE_VISIBILITE = "visibilite";
    public static final int COLONNE_VISIBILITE_ID = 3;
	private static final String COLONNE_PHOTO = "photo";
	public static final int COLONNE_PHOTO_ID = 4;
	private static final String COLONNE_GEOHASH = "geohash";
	public static final int COLONNE_GEOHASH_ID = 5;
	private static final String COLONNE_DATE = "date";
	public static final int COLONNE_DATE_ID = 6;

	// L�instance de la base qui sera manipul�e au travers de cette classe.
	private SQLiteDatabase maBaseDonnees;
	private MaBaseOpenHelper baseHelper;
	
	public SpotsDBAdapteur(Context ctx) {
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
	public Spot getSpot(String photo) {
		Cursor c = maBaseDonnees.query(TABLE_SPOTS, new String[] {
				COLONNE_ID, COLONNE_LONGITUDE, COLONNE_LATITUDE, COLONNE_VISIBILITE, COLONNE_PHOTO, COLONNE_GEOHASH, COLONNE_DATE }, null, null, null,
				COLONNE_PHOTO + " LIKE " + photo, null);
				return cursorToSpot(c);
	}
	
	/**
	* R�cup�re un spot en fonction de son id.
	*/
	public Spot getSpot(int id) {
		Cursor c = maBaseDonnees.query(TABLE_SPOTS, new String[] {
				COLONNE_ID, COLONNE_LONGITUDE, COLONNE_LATITUDE, COLONNE_VISIBILITE, COLONNE_PHOTO, COLONNE_GEOHASH, COLONNE_DATE }, null, null, null,
				COLONNE_ID + " = " + id, null);
				return cursorToSpot(c);
	}
	/**
	* Retourne tous les spots de la base de donn�es.
	*/
	public ArrayList<Spot> getAllSpots() {
		Cursor c = maBaseDonnees.query(TABLE_SPOTS, new String[] {
				COLONNE_ID, COLONNE_LONGITUDE, COLONNE_LATITUDE, COLONNE_VISIBILITE, COLONNE_PHOTO, COLONNE_GEOHASH, COLONNE_DATE }, null, null, null,
				null, null);
		return cursorToSpots(c);
	}
	
	/**
	* @param c
	* Le curseur � utiliser pour r�cup�rer les donn�es du spot.
	* @return Une instance d�un spot avec les valeurs du curseur.
	*/
	private Spot cursorToSpot(Cursor c) {
		// Si la requ�te ne renvoie pas de r�sultat.
		if (c.getCount() == 0)
			return null;
		Spot retSpot = new Spot();
		// Extraction des valeurs depuis le curseur.
		
		retSpot.setId(c.getInt(COLONNE_ID_ID));
		retSpot.setLongitude(c.getString(COLONNE_LONGITUDE_ID));
		retSpot.setLatitude(c.getString(COLONNE_LATITUDE_ID));
        retSpot.setVisibilite(c.getString(COLONNE_VISIBILITE_ID));
		retSpot.setPhoto(c.getString(COLONNE_PHOTO_ID));
		retSpot.setGeohash(c.getString(COLONNE_GEOHASH_ID));
		retSpot.setDate(c.getString(COLONNE_DATE_ID));

		// Ferme le curseur pour lib�rer les ressources.
		c.close();
		return retSpot;
	}
	
	private ArrayList<Spot> cursorToSpots(Cursor c) {
		// Si la requ�te ne renvoie pas de r�sultat.
		if (c.getCount() == 0)
		return new ArrayList<Spot>(0);
		ArrayList<Spot> retSpots = new ArrayList<Spot>(c.getCount());
		c.moveToFirst();
		
		do {
			Spot retSpot = new Spot();
			retSpot.setId(c.getInt(COLONNE_ID_ID));
			retSpot.setLongitude(c.getString(COLONNE_LONGITUDE_ID));
			retSpot.setLatitude(c.getString(COLONNE_LATITUDE_ID));
            retSpot.setVisibilite(c.getString(COLONNE_VISIBILITE_ID));
			retSpot.setPhoto(c.getString(COLONNE_PHOTO_ID));
			retSpot.setGeohash(c.getString(COLONNE_GEOHASH_ID));
			retSpot.setDate(c.getString(COLONNE_DATE_ID));
			retSpots.add(retSpot);
		} while (c.moveToNext());
		
		// Ferme le curseur pour lib�rer les ressources.
		c.close();
		return retSpots;
	}
	
	/**
	* Ins�re un spot dans la table des spots.
	*/
	public long insertSpot(Spot spot) {
		ContentValues valeurs = new ContentValues();
		valeurs.put(COLONNE_LONGITUDE, spot.getLongitude());
		valeurs.put(COLONNE_LATITUDE, spot.getLatitude());
        valeurs.put(COLONNE_VISIBILITE, spot.getVisibilite());
		valeurs.put(COLONNE_PHOTO, spot.getPhoto());
		valeurs.put(COLONNE_GEOHASH, spot.getGeohash());
		valeurs.put(COLONNE_DATE, spot.getDate());
        if (maBaseDonnees == null)
            Log.i("insertion du spot ", " null");
		return maBaseDonnees.insert(TABLE_SPOTS, null, valeurs);
	}
	
	public long insertSpot(ContentValues valeurs) {
		return maBaseDonnees.insert(TABLE_SPOTS, null, valeurs);
	}
	
	/**
	* Met � jour un spot dans la table des spots.
	*/
	public int updateSpot(int id, Spot spotToUpdate) {
		ContentValues valeurs = new ContentValues();
		valeurs.put(COLONNE_LONGITUDE, spotToUpdate.getLongitude());
		valeurs.put(COLONNE_LATITUDE, spotToUpdate.getLatitude());
        valeurs.put(COLONNE_VISIBILITE, spotToUpdate.getVisibilite());
		valeurs.put(COLONNE_PHOTO, spotToUpdate.getPhoto());
		valeurs.put(COLONNE_GEOHASH, spotToUpdate.getGeohash());
		valeurs.put(COLONNE_DATE, spotToUpdate.getDate());
		return maBaseDonnees.update(TABLE_SPOTS, valeurs, COLONNE_ID + " = "
				+ id, null);
	}
	
	public int updateSpot(ContentValues valeurs, String where,
			String[] whereArgs) {
		return maBaseDonnees.update(TABLE_SPOTS, valeurs, where, whereArgs);
	}
	/**
	* Supprime un spot � partir de son hash.
	*/
	public int removeSpot(String geohash) {
		return maBaseDonnees.delete(TABLE_SPOTS, COLONNE_GEOHASH + " LIKE "
				+ geohash, null);
	}
	/**
	* Supprime un spot � partir de sa photo.
	*/
	public int removeSpot2(String photo) {
		return maBaseDonnees.delete(TABLE_SPOTS, COLONNE_PHOTO + " LIKE "
				+ photo, null);
	}
	/**
	* Supprime un spot � partir de son id.
	*/
	public int removeSpot(int id) {
		return maBaseDonnees.delete(TABLE_SPOTS, COLONNE_ID + " = " + id,
				null);
	}
	/**
	 * compte le nombre de spots dans la bd
	 */
	public int countSpot() {
		return getAllSpots().size();
	}
}
