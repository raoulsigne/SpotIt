package techafrkix.work.com.spot.spotit;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;

import techafrkix.work.com.spot.bd.Spot;
import techafrkix.work.com.spot.bd.SpotsDBAdapteur;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.AWS_Tools;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.DBServer;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.GeoHash;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.SessionManager;

public class DetailSpot_New extends AppCompatActivity {

    TextView txtMoi, txtAmis, txtPublic;
    EditText edtTags;
    ImageView imgspot;
    LinearLayout liste;
    Button buttonAdd;

    AWS_Tools aws_tools;
    private SessionManager session;
    private HashMap<String, String> profile;
    private DBServer server;
    private  int cle;
    private String visibilite, imagepath;
    private double longitude, latitude;

    public static final String V_MOI = "moi";
    public static final String V_FRIEND = "amis";
    public static final String V_PUBLIC = "publics";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_detail_spot);

        // Session class instance
        session = new SessionManager(getApplicationContext());
        profile = new HashMap<>();
        server = new DBServer(getApplicationContext());
        profile = session.getUserDetails();

        Bundle extras = getIntent().getExtras();

        imagepath = extras.getString("image");
        longitude = extras.getDouble("longitude");
        latitude = extras.getDouble("latitude");
        aws_tools = new AWS_Tools(getApplicationContext());

        Log.i("Photo", imagepath);

        Button valider = (Button)findViewById(R.id.btnValider);
        Button annuler = (Button)findViewById(R.id.btnAnnuler);
        final ImageButton vMoi = (ImageButton)findViewById(R.id.visibiliteMoi);
        final ImageButton vFriend = (ImageButton)findViewById(R.id.visibiliteFriend);
        final ImageButton vPublic = (ImageButton)findViewById(R.id.visibilitePublic);
        txtMoi = (TextView)findViewById(R.id.txtMoi);
        txtAmis = (TextView)findViewById(R.id.txtAmis);
        txtPublic = (TextView)findViewById(R.id.txtPublic);
        imgspot = (ImageView)findViewById(R.id.imgspot);
        edtTags = (EditText)findViewById(R.id.edtTags);
        liste = (LinearLayout) findViewById(R.id.listes);
        buttonAdd = (Button) findViewById(R.id.btnAdd);

        imgspot.setImageBitmap(BitmapFactory.decodeFile(imagepath));

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String chaine = edtTags.getText().toString();
                View child = getLayoutInflater().inflate(R.layout.btntag, null);
                ((Button)child.findViewById(R.id.button)).setText(chaine);
                liste.addView(child);

                child.requestFocus(); //change the position of the visible element inside a list

                edtTags.setText("");

                child.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        liste.removeView(view); //remove the view element inside the linearlayout
                    }
                });
            }
        });

        vMoi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visibilite = V_MOI;
                txtMoi.setTextColor(getResources().getColor(R.color.myblue));
                txtAmis.setTextColor(getResources().getColor(R.color.titre_menu));
                txtPublic.setTextColor(getResources().getColor(R.color.titre_menu));

                vMoi.setBackgroundDrawable(getResources().getDrawable(R.drawable.moi_selected));
                vFriend.setBackgroundDrawable(getResources().getDrawable(R.drawable.friend_clicked));
                vPublic.setBackgroundDrawable(getResources().getDrawable(R.drawable.publics));
            }
        });
        vFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visibilite = V_FRIEND;
                txtMoi.setTextColor(getResources().getColor(R.color.titre_menu));
                txtAmis.setTextColor(getResources().getColor(R.color.myblue));
                txtPublic.setTextColor(getResources().getColor(R.color.titre_menu));

                vMoi.setBackgroundDrawable(getResources().getDrawable(R.drawable.moi_));
                vFriend.setBackgroundDrawable(getResources().getDrawable(R.drawable.friends_clicked));
                vPublic.setBackgroundDrawable(getResources().getDrawable(R.drawable.publics));
            }
        });
        vPublic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visibilite = V_PUBLIC;
                txtMoi.setTextColor(getResources().getColor(R.color.titre_menu));
                txtAmis.setTextColor(getResources().getColor(R.color.titre_menu));
                txtPublic.setTextColor(getResources().getColor(R.color.myblue));

                vMoi.setBackgroundDrawable(getResources().getDrawable(R.drawable.moi_));
                vFriend.setBackgroundDrawable(getResources().getDrawable(R.drawable.friend_clicked));
                vPublic.setBackgroundDrawable(getResources().getDrawable(R.drawable.public_clicked));
            }
        });

        annuler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainintent = new Intent(getApplicationContext(), MainActivity.class);
                finish();
                startActivity(mainintent);
            }
        });

        valider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((visibilite != null) & (visibilite != "") & (liste.getChildCount() > 0)) {
                    GeoHash geoHash = new GeoHash();
                    geoHash.setLatitude(latitude);
                    geoHash.setLongitude(longitude);
                    geoHash.encoder();
                    String temps = profile.get(SessionManager.KEY_ID) + "_" + String.valueOf(System.currentTimeMillis());

                    int childcount = liste.getChildCount();
                    final String[] tags = new String[childcount];
                    for (int i=0; i < childcount; i++){
                        View view = liste.getChildAt(i);
                        tags[i] = ((Button)view.findViewById(R.id.button)).getText().toString();
                        Log.i("tags", tags[i]);
                    }

                    final Spot spot = new Spot();
                    spot.setLongitude(String.valueOf(longitude));
                    spot.setLatitude(String.valueOf(latitude));
                    spot.setVisibilite(visibilite);
                    spot.setGeohash(geoHash.getHash());
                    spot.setPhotokey(temps);
                    spot.setUser_id(Integer.valueOf(profile.get(SessionManager.KEY_ID)));
                    spot.setPhotouser(profile.get(SessionManager.KEY_PHOTO));

                    //stockage du spot dans la BD embarqué
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            cle = server.enregistre_spot(spot, tags);
                        }});

                    t.start(); // spawn thread
                    try {
                        t.join();
                        if (cle > -1) {
                            spot.setId(cle);
                            session.increment_nbspot(); // increment the number of spots

                            //stockage de la photo sur le serveur amazon
                            try {
                                File folder = new File(DBServer.DOSSIER_IMAGE);
                                if (!folder.exists())
                                    folder.mkdirs();
                                File file = new File(DBServer.DOSSIER_IMAGE + temps + ".jpg");
                                OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
                                Bitmap bitmap = BitmapFactory.decodeFile(imagepath);
                                Bitmap resized = Bitmap.createScaledBitmap(bitmap, 800, 800, true);
                                resized.compress(Bitmap.CompressFormat.JPEG, 50, os);
                                os.close();
                                aws_tools = new AWS_Tools(DetailSpot_New.this);
                                aws_tools.uploadPhoto(file, temps, spot);
                            }catch (Exception e)
                            {
                                Log.e("file", e.getMessage());
                            }

                            vMoi.setBackgroundDrawable(getResources().getDrawable(R.drawable.moi));
                            vFriend.setBackgroundDrawable(getResources().getDrawable(R.drawable.friend_clicked));
                            vPublic.setBackgroundDrawable(getResources().getDrawable(R.drawable.publics));
                        }
                        else {
                            Log.i("BD", "nouveau spot non enregistré");
                            Toast.makeText(getApplicationContext(), "Nouveau spot non enregistré", Toast.LENGTH_SHORT).show();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else
                    Toast.makeText(getApplicationContext(), "please describe your spot!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
