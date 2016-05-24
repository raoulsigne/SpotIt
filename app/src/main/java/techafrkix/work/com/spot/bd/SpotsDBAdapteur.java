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

	public static final String COLONNE_ID = "id";
	public static final String COLONNE_LONGITUDE = "long";
	public static final String COLONNE_LATITUDE = "lat";
	public static final String COLONNE_VISIBILITE = "visibilite_id";
	public static final String COLONNE_GEOHASH = "hash";
	public static final String COLONNE_PHOTO = "photo";
	public static final String COLONNE_DATE = "created";
	public static final String COLONNE_USER_ID = "user_id";
	public static final String COLONNE_RESPOT = "respot";

	public static final int COLONNE_ID_ID = 0;
	public static final int COLONNE_LONGITUDE_ID = 1;
	public static final int COLONNE_LATITUDE_ID = 2;
    public static final int COLONNE_VISIBILITE_ID = 3;
	public static final int COLONNE_PHOTO_ID = 4;
	public static final int COLONNE_GEOHASH_ID = 5;
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
		retSpot.setPhotokey(c.getString(COLONNE_PHOTO_ID));
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
			retSpot.setPhotokey(c.getString(COLONNE_PHOTO_ID));
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
		valeurs.put(COLONNE_PHOTO, spot.getPhotokey());
		valeurs.put(COLONNE_GEOHASH, spot.getGeohash());
		valeurs.put(COLONNE_DATE, spot.getDate());
        if (maBaseDonnees == null)
            Log.i("insertion du spot ", " null");
		return maBaseDonnees.insert(TABLE_SPOTS, null, valeurs);
	}
}
