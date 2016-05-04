package techafrkix.work.com.spot.spotit;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import techafrkix.work.com.spot.bd.Spot;
import techafrkix.work.com.spot.bd.SpotsDBAdapteur;
import techafrkix.work.com.spot.techafrkix.work.com.spot.test.Main3Activity;

public class DetailSpot_New extends AppCompatActivity {

    EditText edtLat, edtLong;
    TextView txtMoi, txtAmis, txtPublic;

    private SpotsDBAdapteur dbAdapteur;
    SQLiteDatabase db;

    private String visibilite, imagepath;

    private static final String V_MOI = "moi";
    private static final String V_FRIEND = "amis";
    private static final String V_PUBLIC = "public";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_detail_spot);
        double longitude, latitude;

        Bundle extras = getIntent().getExtras();

        imagepath = extras.getString("image");
        longitude = extras.getDouble("longitude");
        latitude = extras.getDouble("latitude");
        Log.i("parametre : image=", imagepath + " longitude=" + longitude + "; latitude=" + latitude);
        dbAdapteur = new SpotsDBAdapteur(getApplicationContext());

        edtLat = (EditText)findViewById(R.id.edtLatitude);
        edtLong = (EditText)findViewById(R.id.edtLongitude);
        Button valider = (Button)findViewById(R.id.btnValider);
        final ImageButton vMoi = (ImageButton)findViewById(R.id.visibiliteMoi);
        final ImageButton vFriend = (ImageButton)findViewById(R.id.visibiliteFriend);
        final ImageButton vPublic = (ImageButton)findViewById(R.id.visibilitePublic);
        txtMoi = (TextView)findViewById(R.id.txtMoi);
        txtAmis = (TextView)findViewById(R.id.txtAmis);
        txtPublic = (TextView)findViewById(R.id.txtPublic);

        if (longitude != 0)
            edtLong.setText(String.valueOf(longitude));
        if (latitude != 0)
            edtLat.setText(String.valueOf(latitude));

        vMoi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visibilite = V_MOI;
                txtMoi.setTextColor(getResources().getColor(R.color.myblue));
                txtAmis.setTextColor(getResources().getColor(R.color.noir));
                txtPublic.setTextColor(getResources().getColor(R.color.noir));

                vMoi.setBackgroundDrawable(getResources().getDrawable(R.drawable.moi_active));
                vFriend.setBackgroundDrawable(getResources().getDrawable(R.drawable.friend));
                vPublic.setBackgroundDrawable(getResources().getDrawable(R.drawable.publics));
            }
        });
        vFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visibilite = V_FRIEND;
                txtMoi.setTextColor(getResources().getColor(R.color.noir));
                txtAmis.setTextColor(getResources().getColor(R.color.myblue));
                txtPublic.setTextColor(getResources().getColor(R.color.noir));
                vMoi.setBackgroundDrawable(getResources().getDrawable(R.drawable.moi));
                vFriend.setBackgroundDrawable(getResources().getDrawable(R.drawable.friend_active));
                vPublic.setBackgroundDrawable(getResources().getDrawable(R.drawable.publics));
            }
        });
        vPublic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visibilite = V_PUBLIC;
                txtMoi.setTextColor(getResources().getColor(R.color.noir));
                txtAmis.setTextColor(getResources().getColor(R.color.noir));
                txtPublic.setTextColor(getResources().getColor(R.color.myblue));
                vMoi.setBackgroundDrawable(getResources().getDrawable(R.drawable.moi));
                vFriend.setBackgroundDrawable(getResources().getDrawable(R.drawable.friend));
                vPublic.setBackgroundDrawable(getResources().getDrawable(R.drawable.publics_active));
            }
        });

        valider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (visibilite != null & visibilite != "" & edtLong.getText().toString()!="" & edtLat.getText().toString()!="") {
                    GeoHash geoHash = new GeoHash();
                    geoHash.setLatitude(Double.valueOf(edtLat.getText().toString()));
                    geoHash.setLongitude(Double.valueOf(edtLong.getText().toString()));
                    geoHash.encoder();

                    Spot spot = new Spot();
                    spot.setLongitude(edtLong.getText().toString());
                    spot.setLatitude(edtLat.getText().toString());
                    spot.setVisibilite(visibilite);
                    spot.setGeohash(geoHash.getHash());
                    spot.setPhoto(imagepath);

                    db = dbAdapteur.open();
                    long cle = dbAdapteur.insertSpot(spot);

                    if (cle != -1) {
                        Log.i("BD", "nouveau spot enregistré");
                        Toast.makeText(getApplicationContext(),"Nouveau spot enregistré!",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Log.i("BD", "nouveau spot non enregistré");
                        Toast.makeText(getApplicationContext(),"Nouveau non spot enregistré!",Toast.LENGTH_SHORT).show();
                    }
                    db.close();

                    edtLat.setText("");
                    edtLong.setText("");
                    vMoi.setBackgroundDrawable(getResources().getDrawable(R.drawable.moi));
                    vFriend.setBackgroundDrawable(getResources().getDrawable(R.drawable.friend));
                    vPublic.setBackgroundDrawable(getResources().getDrawable(R.drawable.publics));

                    String release = Build.VERSION.RELEASE;
                    int sdkVersion = Build.VERSION.SDK_INT;
                    Log.i("test", "Android SDK: " + sdkVersion + " (" + release + ")");
                    Intent mainintent = new Intent(getApplicationContext(),Main3Activity.class);
                    finish();
                    startActivity(mainintent);
                }
                else
                    Toast.makeText(getApplicationContext(), "Formulaire non conforme", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
