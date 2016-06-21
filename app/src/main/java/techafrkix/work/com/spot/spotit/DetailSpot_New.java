package techafrkix.work.com.spot.spotit;

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

    AWS_Tools aws_tools;
    private SessionManager session;
    private HashMap<String, String> profile;
    private DBServer server;
    private  int cle;
    private String visibilite, imagepath;
    private double longitude, latitude;

    public static final String V_MOI = "moi";
    public static final String V_FRIEND = "amis";
    public static final String V_PUBLIC = "public";

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
        final ImageButton vMoi = (ImageButton)findViewById(R.id.visibiliteMoi);
        final ImageButton vFriend = (ImageButton)findViewById(R.id.visibiliteFriend);
        final ImageButton vPublic = (ImageButton)findViewById(R.id.visibilitePublic);
        txtMoi = (TextView)findViewById(R.id.txtMoi);
        txtAmis = (TextView)findViewById(R.id.txtAmis);
        txtPublic = (TextView)findViewById(R.id.txtPublic);
        imgspot = (ImageView)findViewById(R.id.imgspot);
        edtTags = (EditText)findViewById(R.id.edtTags);

        imgspot.setImageBitmap(BitmapFactory.decodeFile(imagepath));

        edtTags.addTextChangedListener(new MyTextWatcher(edtTags));

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
                if (visibilite != null & visibilite != "") {
                    GeoHash geoHash = new GeoHash();
                    geoHash.setLatitude(latitude);
                    geoHash.setLongitude(longitude);
                    geoHash.encoder();
                    String temps = profile.get(SessionManager.KEY_ID) + "_" + String.valueOf(System.currentTimeMillis());

                    String chaine = edtTags.getText().toString();
                    final String[] tags = chaine.split(" ");

                    final Spot spot = new Spot();
                    spot.setLongitude(String.valueOf(longitude));
                    spot.setLatitude(String.valueOf(latitude));
                    spot.setVisibilite(visibilite);
                    spot.setGeohash(geoHash.getHash());
                    spot.setPhotokey(temps);
                    spot.setUser_id(Integer.valueOf(profile.get(SessionManager.KEY_ID)));

                    //stockage du spot dans la BD embarqué
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            cle = server.enregistre_spot(spot, tags);
                        }});

                    t.start(); // spawn thread
                    try {
                        t.join();
                        if (cle == DBServer.SUCCESS) {
                            session.increment_nbspot(); // increment the number of spots

                            //stockage de la photo sur le serveur amazon
                            try {
                                File folder = new File(getApplicationContext().getFilesDir().getPath()+"/SpotItPictures/");
                                if (!folder.exists())
                                    folder.mkdirs();
                                File file = new File(getApplicationContext().getFilesDir().getPath()+"/SpotItPictures/"+temps+".jpg");
                                OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
                                Bitmap bitmap = BitmapFactory.decodeFile(imagepath);
                                Bitmap resized = Bitmap.createScaledBitmap(bitmap, 800, 800, true);
                                resized.compress(Bitmap.CompressFormat.JPEG, 50, os);
                                os.close();
                                aws_tools = new AWS_Tools(DetailSpot_New.this);
                                aws_tools.uploadPhoto(file, temps);
                            }catch (Exception e)
                            {
                                Log.e("file", e.getMessage());
                            }

                            vMoi.setBackgroundDrawable(getResources().getDrawable(R.drawable.moi));
                            vFriend.setBackgroundDrawable(getResources().getDrawable(R.drawable.friend));
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
                    Toast.makeText(getApplicationContext(), "Formulaire non conforme", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public class MyTextWatcher implements TextWatcher {
        private EditText et;
        private char lastchar;
        private CharSequence chaine;

        // Pass the EditText instance to TextWatcher by constructor
        public MyTextWatcher(EditText et) {
            this.et = et;
            this.lastchar = ' ';
            this.chaine = "";
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            // Unregister self before setText
            et.removeTextChangedListener(this);
            chaine = et.getText().toString();
            if (!TextUtils.isEmpty(chaine)) {
                if (!chaine.equals("#")) {
                    lastchar = chaine.charAt(chaine.length() - 1);

                    Log.i("test", chaine.toString() + " " + lastchar);
                    if (lastchar == '#') {
                        final SpannableStringBuilder sb = new SpannableStringBuilder(chaine.subSequence(0, chaine.length() - 1));

                        // Span to set text color to some RGB value
                        final ForegroundColorSpan fcs = new ForegroundColorSpan(getResources().getColor(R.color.mainblue));
                        // Span to make text bold
                        final StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD);

                        // Set the text color for first 4 characters
                        sb.setSpan(fcs, 0, sb.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

                        // make them also bold
                        sb.setSpan(bss, 0, sb.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

                        et.setText(sb + " ");

                        //et.setText(chaine.subSequence(0, chaine.length() - 1) + " ");
                        et.setSelection(et.getText().length());
                    }
                }else
                    et.setText("");
            }
            // Re-register self after setText
            et.addTextChangedListener(this);
        }

    }
}
