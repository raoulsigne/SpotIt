package techafrkix.work.com.spot.spotit;

import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import techafrkix.work.com.spot.bd.Spot;
import techafrkix.work.com.spot.bd.SpotsDBAdapteur;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.AWS_Tools;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.GeoHash;

public class DetailSpot_New extends AppCompatActivity {

    EditText edtLat, edtLong;
    TextView txtMoi, txtAmis, txtPublic;

    private SpotsDBAdapteur dbAdapteur;
    SQLiteDatabase db;
    AWS_Tools aws_tools;

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
        dbAdapteur = new SpotsDBAdapteur(getApplicationContext());
        aws_tools = new AWS_Tools(getApplicationContext());

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
                    String temps = String.valueOf(System.currentTimeMillis());

                    Spot spot = new Spot();
                    spot.setLongitude(edtLong.getText().toString());
                    spot.setLatitude(edtLat.getText().toString());
                    spot.setVisibilite(visibilite);
                    spot.setGeohash(geoHash.getHash());
                    spot.setPhotokey(temps);

                    //stockage du spot dans la BD embarqué
                    db = dbAdapteur.open();
                    long cle = dbAdapteur.insertSpot(spot);

                    if (cle != -1) {
                        Log.i("BD", "nouveau spot enregistré");
                    }
                    else {
                        Log.i("BD", "nouveau spot non enregistré");
                    }
                    db.close();

                    //stockage de la photo sur le serveur amazon
                    try {
                        File folder = new File(getApplicationContext().getFilesDir().getPath()+"/Images/");
                        if (!folder.exists())
                            folder.mkdirs();
                        File file = new File(getApplicationContext().getFilesDir().getPath()+"/Images/"+temps+".jpg");
                        OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
                        Bitmap bitmap = BitmapFactory.decodeFile(imagepath);
                        Bitmap resized = Bitmap.createScaledBitmap(bitmap, 800, 800, true);
                        resized.compress(Bitmap.CompressFormat.JPEG, 50, os);
                        os.close();
                        aws_tools = new AWS_Tools(DetailSpot_New.this);
                        aws_tools.uploadPhoto(file,temps);
                    }catch (Exception e)
                    {
                        Log.e("file",e.getMessage());
                    }


                    edtLat.setText("");
                    edtLong.setText("");
                    vMoi.setBackgroundDrawable(getResources().getDrawable(R.drawable.moi));
                    vFriend.setBackgroundDrawable(getResources().getDrawable(R.drawable.friend));
                    vPublic.setBackgroundDrawable(getResources().getDrawable(R.drawable.publics));
                }
                else
                    Toast.makeText(getApplicationContext(), "Formulaire non conforme", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
