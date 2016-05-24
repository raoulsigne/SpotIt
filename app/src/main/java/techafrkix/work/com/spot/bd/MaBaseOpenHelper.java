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

	public static final String TABLE_UTILISATEURS = "table_utilisateurs";
    public static final String COLONNE_USER_ID = "id";
    public static final String COLONNE_EMAIL = "email";
    public static final String COLONNE_PASSWORD = "password";
    public static final String COLONNE_DATE_NAISSANCE = "birth";
    public static final String COLONNE_PSEUDO = "pseudo";
    public static final String COLONNE_NB_SPOT = "nbspot";
    public static final String COLONNE_NB_RESPOT = "nbrespot";
    public static final String COLONNE_TYPECONNEXION_ID = "typeconnexion_id";
    public static final String COLONNE_PHOTO_PROFILE = "photo";
    public static final String COLONNE_CREATED = "created";

	private static final String REQUETE_CREATION_TABLE_UTILISATEURS = " create table "
			+ TABLE_UTILISATEURS + " (" + COLONNE_USER_ID + " integer primary key autoincrement, "
            + COLONNE_EMAIL + " text not null, " + COLONNE_PASSWORD + " text not null, "
			+ COLONNE_DATE_NAISSANCE + " text not null, " + COLONNE_PSEUDO + " text not null, " + COLONNE_NB_SPOT + " integer not null, "
            + COLONNE_NB_RESPOT + " integer not null, " + COLONNE_TYPECONNEXION_ID + " integer not null, " + COLONNE_PHOTO_PROFILE + " text not null, "
            + COLONNE_CREATED + " text not null);";
	
	public MaBaseOpenHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
        db.execSQL(REQUETE_CREATION_TABLE_UTILISATEURS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Dans notre cas, nous supprimons la base et les donn�es pour en cr�er
		// une nouvelle ensuite. Vous pouvez cr�er une logique de mise � jour
		// propre � votre base permettant de garder les donn�es � la place.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_UTILISATEURS);
		// Cr�ation de la nouvelle structure.
		onCreate(db);
	}
	

}
