package techafrkix.work.com.spot.spotit;

import android.Manifest;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.io.File;
import java.util.HashMap;

import techafrkix.work.com.spot.bd.SpotsDBAdapteur;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.AWS_Tools;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.MyMarker;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, FragmentAccueil.OnFragmentInteractionListener{

    private static Context context;

    MapsActivity fgAccueil;
    ListeSpots fgSpots;
    FragmentTransaction ft;

    public static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 3;
    public static final int MENU_ACTIF_ACCUEIL = 1;
    public static final int MENU_ACTIF_LIST = 2;
    public static final int MENU_ACTIF_ADD = 3;
    public static final int MENU_ACTIF_DECONNECT = 4;

    private static final int CAMERA_REQUEST = 1;
    private static final int WRITE_REQUEST = 2;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest locationRequest;
    String selectedImagePath;
    Uri mCapturedImageURI;
    private GeoHash geoHash;
    private SpotsDBAdapteur dbAdapteur;
    SQLiteDatabase db;

    ImageButton imgHome, imgList, imgAdd, imgDisconnect;
    TextView txtHome, txtList, txtAdd, txtDisconnect;

    int menuactif;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        MainActivity.context = getApplicationContext();

        imgHome = (ImageButton)findViewById(R.id.imgHome);
        imgList = (ImageButton)findViewById(R.id.imgList);
        imgAdd = (ImageButton)findViewById(R.id.imgAdd);
        imgDisconnect = (ImageButton)findViewById(R.id.imgDisconnect);
        txtHome = (TextView)findViewById(R.id.txtHome);
        txtList = (TextView)findViewById(R.id.txtSpots);
        txtAdd = (TextView)findViewById(R.id.txtAdd);
        txtDisconnect = (TextView)findViewById(R.id.txtLogout);


        fgAccueil = new MapsActivity();
        fgSpots = new ListeSpots();

        FragmentTransaction ft;
        dbAdapteur = new SpotsDBAdapteur(getApplicationContext());
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
        menuactif = MENU_ACTIF_ACCUEIL;

        CheckEnableGPS();

        imgHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //changement des couleurs des widgets
                imgHome.setBackground(getResources().getDrawable(R.drawable.icon_home_blanc));
                imgAdd.setBackground(getResources().getDrawable(R.drawable.icon_add_gris));
                imgList.setBackground(getResources().getDrawable(R.drawable.icon_list_gris));
                imgDisconnect.setBackground(getResources().getDrawable(R.drawable.icon_deconnexion_gris));
                txtHome.setTextColor(getResources().getColor(R.color.blanc));
                txtList.setTextColor(getResources().getColor(R.color.fond_detail));
                txtAdd.setTextColor(getResources().getColor(R.color.fond_detail));
                txtDisconnect.setTextColor(getResources().getColor(R.color.fond_detail));

                //traitement de l'action lors du click
                if (menuactif != MENU_ACTIF_ACCUEIL) {
                    try {
                        //remove all others fragments if there exists
                        getSupportFragmentManager().beginTransaction().remove(fgSpots).commit();
                        getSupportFragmentManager().beginTransaction().remove(fgAccueil).commit();
                        // add the new fragment containing the main map
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, fgAccueil, "ACCUEIL")
                                .commit();
                        menuactif = MENU_ACTIF_ACCUEIL;
                    } catch (Exception e) {
                        Log.e("fragment", e.getMessage());
                    }
                }
            }
        });
        imgList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //changement des couleurs des widgets
                imgHome.setBackground(getResources().getDrawable(R.drawable.icon_home_gris));
                imgAdd.setBackground(getResources().getDrawable(R.drawable.icon_add_gris));
                imgList.setBackground(getResources().getDrawable(R.drawable.icon_list_blanc));
                imgDisconnect.setBackground(getResources().getDrawable(R.drawable.icon_deconnexion_gris));
                txtHome.setTextColor(getResources().getColor(R.color.fond_detail));
                txtList.setTextColor(getResources().getColor(R.color.blanc));
                txtAdd.setTextColor(getResources().getColor(R.color.fond_detail));
                txtDisconnect.setTextColor(getResources().getColor(R.color.fond_detail));

                //traitement de l'action lors du click
                if (menuactif != MENU_ACTIF_LIST) {
                    try {
                        //remove all others fragments if there exists
                        getSupportFragmentManager().beginTransaction().remove(fgSpots).commit();
                        getSupportFragmentManager().beginTransaction().remove(fgAccueil).commit();
                        // add the new fragment containing the list of spots
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, fgSpots, "SPOTS")
                                .commit();
                        menuactif = MENU_ACTIF_LIST;
                    } catch (Exception e) {
                        Log.e("fragment", e.getMessage());
                    }
                }
            }
        });
        imgAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //changement des couleurs des widgets
                imgHome.setBackground(getResources().getDrawable(R.drawable.icon_home_gris));
                imgAdd.setBackground(getResources().getDrawable(R.drawable.icon_add_blanc));
                imgList.setBackground(getResources().getDrawable(R.drawable.icon_list_gris));
                imgDisconnect.setBackground(getResources().getDrawable(R.drawable.icon_deconnexion_gris));
                txtHome.setTextColor(getResources().getColor(R.color.fond_detail));
                txtList.setTextColor(getResources().getColor(R.color.fond_detail));
                txtAdd.setTextColor(getResources().getColor(R.color.blanc));
                txtDisconnect.setTextColor(getResources().getColor(R.color.fond_detail));

                //traitement de l'action lors du click
                if (menuactif != MENU_ACTIF_ADD) {
                    menuactif = MENU_ACTIF_ADD;
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
                    startActivity(imagepreview);
                }
            }
        });
        imgDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //changement des couleurs des widgets
                imgHome.setBackground(getResources().getDrawable(R.drawable.icon_home_gris));
                imgAdd.setBackground(getResources().getDrawable(R.drawable.icon_add_gris));
                imgList.setBackground(getResources().getDrawable(R.drawable.icon_list_gris));
                imgDisconnect.setBackground(getResources().getDrawable(R.drawable.icon_deconnexion_blanc));
                txtHome.setTextColor(getResources().getColor(R.color.fond_detail));
                txtList.setTextColor(getResources().getColor(R.color.fond_detail));
                txtAdd.setTextColor(getResources().getColor(R.color.fond_detail));
                txtDisconnect.setTextColor(getResources().getColor(R.color.blanc));

                //traitement de l'action lors du click
                if (menuactif != MENU_ACTIF_DECONNECT) {
                    Intent itdeconnect = new Intent(getApplicationContext(), Connexion.class);
                    itdeconnect.putExtra("caller","Main");
                    itdeconnect.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK); //add flags to spot all others activities
                    finish();
                    startActivity(itdeconnect);
                }
            }
        });
    }

    public static Context getAppContext() {
        return MainActivity.context;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            // photo = (Bitmap) data.getExtras().get("data");
            selectedImagePath = getRealPathFromURI(mCapturedImageURI);
            Log.v("log", "filePath is : " + selectedImagePath);

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

            Intent itDetailSpot = new Intent(getApplicationContext(),DetailSpot_New.class);
            itDetailSpot.putExtras(bundle);
            startActivity(itDetailSpot);
        }
        else {
            Log.i("camera", "retour d'un code d'erreur");
            //on relance l'activité principale
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            finish();
            startActivity(intent);
        }
    }

    public String getRealPathFromURI(Uri contentUri)
    {
        try
        {
            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor cursor = managedQuery(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        catch (Exception e)
        {
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

    public void onFragmentInteraction(Uri uri){
        //you can leave it empty
    }

    /**
     * methode qui vérifie si le gps and network est activé dans le cas contraire demande à l'utilisateur de l'activer
     */
    private void CheckEnableGPS(){
        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if(!provider.equals("")){
            //GPS Enabled
            Log.i("GPS", "GPS Enabled: ");
        }else{
            new AlertDialog.Builder(this).setTitle("Notification").setMessage("Vous devez activé le GPS!")
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
                    new AlertDialog.Builder(this).setTitle("Notification").setMessage("Vous ne pouvez faire des spots que lorsque la camera est accessible!")
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
                    new AlertDialog.Builder(this).setTitle("Notification").setMessage("Nous avons besoin de stocker les photos!")
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
}
