package techafrkix.work.com.spot.spotit;

import android.Manifest;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.IntentCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import techafrkix.work.com.spot.bd.Spot;
import techafrkix.work.com.spot.bd.Utilisateur;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.AWS_Tools;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.DBServer;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.GeoHash;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.SessionManager;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, FragmentAccueil.OnFragmentInteractionListener, Account.OnFragmentInteractionListener,
        MapsActivity.OnFragmentInteractionListener, ListeSpots.OnFragmentInteractionListener, DetailSpot.OnFragmentInteractionListener,
        Search.OnFragmentInteractionListener, Add_Friend.OnFragmentInteractionListener, Account_Friend.OnFragmentInteractionListener,
        ListeSpots_Friend.OnFragmentInteractionListener, NotificationActivity.OnFragmentInteractionListener, SpotUser.OnFragmentInteractionListener,
        UserSettings.OnFragmentInteractionListener{

    static final int REQUEST_IMAGE_CAPTURE = 10;

    private static Context context;

    MapsActivity fgAccueil;
    ListeSpots fgSpots;
    Account fgAccount;
    DetailSpot fgDetailspot;
    Search fgSearch;
    UserSettings fgUserSettings;
//    Add_Friend fgAddfrient;
    Account_Friend fgFriendAcount;
    ListeSpots_Friend fgSpots_friend;
//    NotificationActivity fgNotificationActivity;
    FragmentTransaction ft;

    static MainActivity instance;

    public static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 3;
    public static final int REQUEST_ACTION = 11;

    public static final int MENU_ACTIF_HOME = 1;
    public static final int MENU_ACTIF_SOCIAL = 2;
    public static final int MENU_ACTIF_NEW = 3;
    public static final int MENU_ACTIF_NOTIFICATION = 4;
    public static final int MENU_ACTIF_ACCOUNT = 5;

    private static final int CAMERA_REQUEST = 1;
    private static final int WRITE_REQUEST = 2;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest locationRequest;
    String selectedImagePath;
    Uri mCapturedImageURI;
    private GeoHash geoHash;

    private SessionManager session;
    private HashMap<String, String> profile;
    private DBServer server;

    ImageButton imgHome, imgNew, imgAccount;
    TextView txtHome, txtNew, txtAccount;
//    TextView notif_count;
    LinearLayout groupeHome, groupeNewspot, groupeAccount;

    int menuactif;
    int resultat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Register to receive messages.
        // We are registering an observer (mMessageReceiver) to receive Intents
        // with actions named "custom-event-name".
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("custom-event-name"));

        instance = this;

        // Session class instance
        session = new SessionManager(getApplicationContext());

        MainActivity.context = getApplicationContext();
        // Session class instance
        session = new SessionManager(getApplicationContext());
        profile = new HashMap<>();
        server = new DBServer(getApplicationContext());
        profile = session.getUserDetails();

        imgHome = (ImageButton) findViewById(R.id.imgHome);
        imgNew = (ImageButton) findViewById(R.id.imgAdd);
        imgAccount = (ImageButton) findViewById(R.id.imgAccount);
        txtHome = (TextView) findViewById(R.id.txtHome);
        txtNew = (TextView) findViewById(R.id.txtAdd);
        txtAccount = (TextView) findViewById(R.id.txtAccount);
        groupeHome = (LinearLayout)findViewById(R.id.groupeHome);
        groupeNewspot = (LinearLayout)findViewById(R.id.groupeNewspot);
        groupeAccount = (LinearLayout)findViewById(R.id.groupeAccount);

//        notif_count = (TextView) findViewById(R.id.notif_count);
//        if (Integer.parseInt(notif_count.getText().toString()) == 0)
//            notif_count.setVisibility(View.INVISIBLE);

        fgAccueil = new MapsActivity();
        fgSpots = new ListeSpots();
        fgAccount = new Account();
        fgDetailspot = new DetailSpot();
        fgSearch = new Search();
        fgUserSettings = new UserSettings();
//        fgAddfrient = new Add_Friend();
        fgFriendAcount = new Account_Friend();
        fgSpots_friend = new ListeSpots_Friend();
//        fgNotificationActivity = new NotificationActivity();

        FragmentTransaction ft;
        geoHash = new GeoHash();

        buildGoogleApiClient();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        } else
            Log.i("Map", "Not connected...");

        //add the main map fragment
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, fgAccueil, "ACCUEIL")
                .commit();
        menuactif = MENU_ACTIF_HOME;
        setAciveTab(MENU_ACTIF_HOME);

        CheckEnableGPS();

        imgHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //changement des couleurs des widgets
                setAciveTab(MENU_ACTIF_HOME);

                //traitement de l'action lors du click
                startFragment(MENU_ACTIF_HOME);

            }
        });
        groupeHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //changement des couleurs des widgets
                setAciveTab(MENU_ACTIF_HOME);

                //traitement de l'action lors du click
                startFragment(MENU_ACTIF_HOME);

            }
        });

        imgNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //changement des couleurs des widgets
                setAciveTab(MENU_ACTIF_NEW);

                //traitement de l'action lors du click
                startFragment(MENU_ACTIF_NEW);
            }
        });
        groupeNewspot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //changement des couleurs des widgets
                setAciveTab(MENU_ACTIF_NEW);

                //traitement de l'action lors du click
                startFragment(MENU_ACTIF_NEW);
            }
        });

        imgAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //changement des couleurs des widgets
                setAciveTab(MENU_ACTIF_ACCOUNT);

                //traitement de l'action lors du click
                startFragment(MENU_ACTIF_ACCOUNT);
            }

        });
        groupeAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //changement des couleurs des widgets
                setAciveTab(MENU_ACTIF_ACCOUNT);

                //traitement de l'action lors du click
                startFragment(MENU_ACTIF_ACCOUNT);
            }

        });
    }

    public static Context getAppContext() {
        return MainActivity.context;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CAMERA_REQUEST:
                    // photo = (Bitmap) data.getExtras().get("data");
                    selectedImagePath = getRealPathFromURI(mCapturedImageURI);
                    Log.v("log", "filePath is : " + selectedImagePath);

                    Bundle bundle = new Bundle();
                    bundle.putString("image", selectedImagePath);
                    if (mLastLocation != null) {
                        Log.i("location", "location not null");
                        bundle.putDouble("longitude", mLastLocation.getLongitude());
                        bundle.putDouble("latitude", mLastLocation.getLatitude());
                    } else {
                        Log.i("location", "your location is null");
                        bundle.putDouble("longitude", 0);
                        bundle.putDouble("latitude", 0);
                    }

                    Intent itDetailSpot = new Intent(getApplicationContext(), DetailSpot_New.class);
                    itDetailSpot.putExtras(bundle);
                    startActivity(itDetailSpot);

                    break;
                case REQUEST_IMAGE_CAPTURE:
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    //imageBitmap = Bitmap.createScaledBitmap(imageBitmap, 400, 400, true);
                    final String temps = profile.get(SessionManager.KEY_ID) + "_" + String.valueOf(System.currentTimeMillis());
                    File file = new File(getApplicationContext().getFilesDir().getPath() + "/SpotItPictures/" + temps + ".jpg");
                    try {
                        OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
                        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
                        os.close();

                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                resultat = server.set_profile_picture(Integer.valueOf(profile.get(SessionManager.KEY_ID)), temps);
                            }});

                        t.start(); // spawn thread
                        try{
                            t.join();
                            if (resultat > -1) {
                                AWS_Tools aws_tools = new AWS_Tools(MainActivity.this);
                                aws_tools.uploadPhoto(file, temps);
                                session.store_photo_profile(temps);
                            }
                        }catch (Exception e){

                        }

                        try {
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.container, fgAccount, "ACCOUNT")
                                    .commit();
                        } catch (Exception e) {
                            Log.e("fragment", e.getMessage());
                        }
                    } catch (Exception e) {
                        Log.e("file", e.getMessage());
                    }

                    break;
            }
        } else {
            Log.i("camera", "retour d'un code d'erreur");
            //on relance l'activité principale
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            finish();
            startActivity(intent);
        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor cursor = managedQuery(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch (Exception e) {
            Log.e("path", e.getMessage());
            return contentUri.getPath();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        Toast.makeText(this, "Failed to connect...", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onConnected(Bundle arg0) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_FINE_LOCATION);
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

            }
        });
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

    }

    @Override
    public void onConnectionSuspended(int arg0) {
        Toast.makeText(this, "Connection suspended...", Toast.LENGTH_SHORT).show();

    }

    protected synchronized void buildGoogleApiClient() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(12000);
        locationRequest.setFastestInterval(30000);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    public void onFragmentInteraction(Uri uri) {
        //you can leave it empty
    }

    /**
     * methode qui vérifie si le gps and network est activé dans le cas contraire demande à l'utilisateur de l'activer
     */
    private void CheckEnableGPS() {
        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if (!provider.equals("")) {
            //GPS Enabled
            Log.i("GPS", "GPS Enabled: ");
        } else {
            new AlertDialog.Builder(this).setTitle("NotificationActivity").setMessage("Vous devez activé le GPS!")
                    .setPositiveButton("D'accord", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    })
                    .show();
        }

    }

    /**
     * gestion des retours de l'activité demandant les permissions à l'utilisateur
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.i("camera", "permission granted");
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.TITLE, "img-" + System.currentTimeMillis() + ".jpg");
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                WRITE_REQUEST);
                    } else {
                        mCapturedImageURI = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
                        Log.i("camera", "debut de l'activité");
                        startActivityForResult(cameraIntent, CAMERA_REQUEST);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    new AlertDialog.Builder(this).setTitle("NotificationActivity").setMessage("Vous ne pouvez faire des spots que lorsque la camera est accessible!")
                            .setPositiveButton("D'accord", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA},
                                            CAMERA_REQUEST);
                                }
                            })
                            .show();
                }
                return;
            }

            case WRITE_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    new AlertDialog.Builder(this).setTitle("NotificationActivity").setMessage("Nous avons besoin de stocker les photos!")
                            .setPositiveButton("D'accord", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA},
                                            WRITE_REQUEST);
                                }
                            })
                            .show();
                }
                return;
            }
        }
    }

    private void vidercache() {
        // Your directory with files to be deleted
        String dossier = getApplicationContext().getFilesDir().getPath() + DBServer.DOSSIER_IMAGE;

        // go to your directory
        File fileList = new File(dossier);

        //check if dir is not null
        if (fileList != null) {

            // so we can list all files
            File[] filenames = fileList.listFiles();

            // loop through each file and delete
            for (File tmpf : filenames) {
                tmpf.delete();
                Log.i("suppression", "file " + tmpf + " deleted");
            }
        }
    }

    @Override
    public void onLoadSpot() {
        try {
            Bundle args = new Bundle();
            args.putInt("type", 1);
            fgSpots.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fgSpots, "SPOTS")
                    .commit();
        } catch (Exception e) {
            Log.e("fragment", e.getMessage());
        }
    }


    @Override
    public void onLoadSpot(ArrayList<Spot> spots) {
        try {
            Bundle args = new Bundle();
            args.putInt("type", 0);
            args.putSerializable("spots", spots);
            fgSpots.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fgSpots, "SPOTS")
                    .commit();
        } catch (Exception e) {
            Log.e("fragment", e.getMessage());
        }
    }

    @Override
    public void onDetailSpot(Spot spot) {
        try {

            Bundle args = new Bundle();
            args.putSerializable("spot", spot);
            fgDetailspot.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fgDetailspot, "DETAIL")
                    .commit();
        } catch (Exception e) {
            Log.e("fragment", e.getMessage());
        }
    }

    @Override
    public void onLetsGo() {

    }

    //@Override
    public void onSearchSpot() {
        try {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fgSearch, "SPOTS")
                    .commit();
        } catch (Exception e) {
            Log.e("fragment", e.getMessage());
        }
    }

    @Override
    public void onSetPhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onDisconnect() {
        session.logoutUser();
        Intent itdeconnect = new Intent(getApplicationContext(), Accueil.class);
        itdeconnect.putExtra("caller", "Main");
        itdeconnect.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK); //add flags to spot all others activities
        vidercache();
        finish();
        startActivity(itdeconnect);
    }

    @Override
    public void onLoadFriend(Utilisateur friend) {
        try {
            Bundle args = new Bundle();
            args.putSerializable("friend", friend);
            fgFriendAcount.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fgFriendAcount, "FRIEND_ACCOUNT")
                    .commit();
        } catch (Exception e) {
            Log.e("fragment", e.getMessage());
        }
    }

    @Override
    public void onListSpot_Friend(Utilisateur friend) {
        try {
            Bundle args = new Bundle();
            args.putSerializable("friend", friend);
            fgSpots_friend.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fgSpots_friend, "FRIEND_LIST_SPOT")
                    .commit();
        } catch (Exception e) {
            Log.e("fragment", e.getMessage());
        }
    }

    @Override
    public void onDetailSpot(Spot spot, Utilisateur utilisateur) {
        try {
            Bundle args = new Bundle();
            args.putSerializable("spot", spot);
            args.putSerializable("friend", utilisateur);
            fgDetailspot.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fgDetailspot, "DETAIL")
                    .commit();
        } catch (Exception e) {
            Log.e("fragment", e.getMessage());
        }
    }

    @Override
    public void onLoadOption() {
        try {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fgUserSettings, "SETTINGS")
                    .commit();
        } catch (Exception e) {
            Log.e("fragment", e.getMessage());
        }
    }

    public void startFragment(int menu){
        switch (menu){
            case MENU_ACTIF_HOME:
                try {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, fgAccueil, "ACCUEIL")
                            .commit();
                    menuactif = MENU_ACTIF_HOME;
                } catch (Exception e) {
                    Log.e("fragment", e.getMessage());
                }

                break;

            case MENU_ACTIF_NEW:

                Bundle bundle = new Bundle();
                bundle.putString("image", selectedImagePath);
                if (mLastLocation != null) {
                    Log.i("location", "location not null");
                    bundle.putDouble("longitude", mLastLocation.getLongitude());
                    bundle.putDouble("latitude", mLastLocation.getLatitude());
                }else {
                    Log.i("location", "your location is null");
                    bundle.putDouble("longitude", 0);
                    bundle.putDouble("latitude", 0);
                }

                Intent imagepreview = new Intent(MainActivity.this,TakeSnap.class);
                imagepreview.putExtras(bundle);
                // finish();
                startActivity(imagepreview);


                break;

            case MENU_ACTIF_ACCOUNT:
                try {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, fgAccount, "ACCOUNT")
                            .commit();
                    menuactif = MENU_ACTIF_ACCOUNT;
                } catch (Exception e) {
                    Log.e("fragment", e.getMessage());
                }

                break;
        }
    }


    public void setAciveTab(int menu){
        switch (menu){
            case MENU_ACTIF_HOME:

                imgHome.setBackground(getResources().getDrawable(R.drawable.world_clicked));
                imgNew.setBackground(getResources().getDrawable(R.drawable.spot));
                imgAccount.setBackground(getResources().getDrawable(R.drawable.setting));
                txtHome.setTextColor(getResources().getColor(R.color.myblue));
                txtNew.setTextColor(getResources().getColor(R.color.titre_menu));
                txtAccount.setTextColor(getResources().getColor(R.color.titre_menu));

                break;
            case MENU_ACTIF_SOCIAL:

                imgHome.setBackground(getResources().getDrawable(R.drawable.world));
                imgNew.setBackground(getResources().getDrawable(R.drawable.spot));
                imgAccount.setBackground(getResources().getDrawable(R.drawable.setting));
                txtHome.setTextColor(getResources().getColor(R.color.titre_menu));
                txtNew.setTextColor(getResources().getColor(R.color.titre_menu));
                txtAccount.setTextColor(getResources().getColor(R.color.titre_menu));

                break;

            case MENU_ACTIF_NEW:

                imgHome.setBackground(getResources().getDrawable(R.drawable.world));
                imgNew.setBackground(getResources().getDrawable(R.drawable.spot_clicked));
                imgAccount.setBackground(getResources().getDrawable(R.drawable.setting));
                txtHome.setTextColor(getResources().getColor(R.color.titre_menu));
                txtNew.setTextColor(getResources().getColor(R.color.myblue));
                txtAccount.setTextColor(getResources().getColor(R.color.titre_menu));

                break;
            case MENU_ACTIF_NOTIFICATION:

                imgHome.setBackground(getResources().getDrawable(R.drawable.world));
                imgNew.setBackground(getResources().getDrawable(R.drawable.spot));
                imgAccount.setBackground(getResources().getDrawable(R.drawable.setting));
                txtHome.setTextColor(getResources().getColor(R.color.titre_menu));
                txtNew.setTextColor(getResources().getColor(R.color.titre_menu));
                txtAccount.setTextColor(getResources().getColor(R.color.titre_menu));

                break;
            case MENU_ACTIF_ACCOUNT:

                imgHome.setBackground(getResources().getDrawable(R.drawable.world));
                imgNew.setBackground(getResources().getDrawable(R.drawable.spot));
                imgAccount.setBackground(getResources().getDrawable(R.drawable.setting_clicked));
                txtHome.setTextColor(getResources().getColor(R.color.titre_menu));
                txtNew.setTextColor(getResources().getColor(R.color.titre_menu));
                txtAccount.setTextColor(getResources().getColor(R.color.myblue));

                break;
        }
    }

    // Our handler for received Intents. This will be called whenever an Intent
    // with an action named "custom-event-name" is broadcasted.
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
            Log.i("localbroadcast", "Got notification: " + message);
//            updatenotif_count();
        }
    };

//    public void updatenotif_count() {
//        Log.i("localbroadcast", "new notif increment the number");
//
//        notif_count.setVisibility(View.VISIBLE);
//        notif_count.setText(String.valueOf(Integer.parseInt(notif_count.getText().toString()) + 1));
//
//        ShortcutBadger.applyCount(getApplicationContext(), Integer.parseInt(notif_count.getText().toString()) + 1);
//    }

    @Override
    protected void onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }
}
