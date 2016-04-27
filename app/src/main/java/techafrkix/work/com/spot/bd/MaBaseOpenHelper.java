/**
 * 
 */
package techafrkix.work.com.spot.bd;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author Raoul
 *
 */
public class MaBaseOpenHelper extends SQLiteOpenHelper{

	private static final String TABLE_SPOTS = "table_spots";
	private static final String COLONNE_ID = "id";
	private static final String COLONNE_LONGITUDE = "longitude";
	private static final String COLONNE_LATITUDE = "latitude";
	private static final String COLONNE_VISIBILITE = "visibilite";
	private static final String COLONNE_PHOTO = "photo";
	private static final String COLONNE_GEOHASH = "geohash";
	private static final String COLONNE_DATE = "date";

	private static final String TABLE_UTILISATEURS = "table_utilisateurs";
	private static final String COLONNE_USER_ID = "id";
	private static final String COLONNE_EMAIL = "email";
	private static final String COLONNE_PASSWORD = "password";
	private static final String COLONNE_DATE_NAISSANCE = "date_naissance";

	private static final String REQUETE_CREATION_TABLE_SPOTS = "create table "
			+ TABLE_SPOTS + " (" + COLONNE_ID
			+ " integer primary key autoincrement, " + COLONNE_LONGITUDE
			+ " text not null, " + COLONNE_LATITUDE + " text not null, " + COLONNE_VISIBILITE + " text not null, " + COLONNE_PHOTO + " text not null, "
			+ COLONNE_GEOHASH + " text not null, " + COLONNE_DATE + " text not null);";
    private static final String REQUETE_CREATION_TABLE_UTILISATEURS = " create table "
			+ TABLE_UTILISATEURS + " (" + COLONNE_USER_ID
			+ " integer primary key autoincrement, " + COLONNE_EMAIL
			+ " text not null, " + COLONNE_PASSWORD + " text not null, " + COLONNE_DATE_NAISSANCE + " text not null);";
	
	public MaBaseOpenHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
        db.execSQL(REQUETE_CREATION_TABLE_SPOTS);
        db.execSQL(REQUETE_CREATION_TABLE_UTILISATEURS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Dans notre cas, nous supprimons la base et les donn�es pour en cr�er
		// une nouvelle ensuite. Vous pouvez cr�er une logique de mise � jour
		// propre � votre base permettant de garder les donn�es � la place.
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SPOTS + ";");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_UTILISATEURS);
		// Cr�ation de la nouvelle structure.
		onCreate(db);
	}
	

}
