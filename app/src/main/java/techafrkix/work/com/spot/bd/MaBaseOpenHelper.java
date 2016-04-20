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
	private static final String REQUETE_CREATION_BD = "create table "
	+ TABLE_SPOTS + " (" + COLONNE_ID
	+ " integer primary key autoincrement, " + COLONNE_LONGITUDE
	+ " text not null, " + COLONNE_LATITUDE + " text not null, " + COLONNE_VISIBILITE + " text not null, " + COLONNE_PHOTO + " text not null, "
	+ COLONNE_GEOHASH + " text not null, " + COLONNE_DATE + " text not null);";
	
	public MaBaseOpenHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(REQUETE_CREATION_BD);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Dans notre cas, nous supprimons la base et les donn�es pour en cr�er
		// une nouvelle ensuite. Vous pouvez cr�er une logique de mise � jour
		// propre � votre base permettant de garder les donn�es � la place.
		db.execSQL("DROP TABLE" + TABLE_SPOTS + ";");
		// Cr�ation de la nouvelle structure.
		onCreate(db);
	}
	

}
