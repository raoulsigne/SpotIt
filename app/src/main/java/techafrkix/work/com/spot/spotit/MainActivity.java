package techafrkix.work.com.spot.spotit;

import android.Manifest;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.BottomBarBadge;
import com.roughike.bottombar.OnMenuTabSelectedListener;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import techafrkix.work.com.spot.bd.Spot;
import techafrkix.work.com.spot.bd.SpotsDBAdapteur;

public class MainActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, DetailSpot.OnFragmentInteractionListener, FragmentAccueil.OnFragmentInteractionListener{

    MapsActivity fgAccueil;
    ListeSpots fgSpots;
    DetailSpot fgSpot;
    FragmentTransaction ft;

    public static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1;
    private static final int CAMERA_REQUEST = 1;
    private static final int WRITE_REQUEST = 2;
    private int NB_SPOTS;
    Bitmap photo;
    LocationManager locationManager;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest locationRequest;
    String selectedImagePath;
    Uri mCapturedImageURI;
    private GeoHash geoHash;
    private SpotsDBAdapteur dbAdapteur;
    SQLiteDatabase db;

    BottomBarBadge nbspots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fgAccueil = new MapsActivity();
        fgSpots = new ListeSpots();
        fgSpot = new DetailSpot();

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
                .add(R.id.fragment_container, fgAccueil, "ACCUEIL")
                .commit();

        BottomBar bottomBar = BottomBar.attach(this, savedInstanceState);
        // Show all titles even when there's more than three tabs.
        bottomBar.useFixedMode();

//        bottomBar.setBackgroundColor(getResources().getColor(R.color.fbButton));
        bottomBar.setItemsFromMenu(R.menu.main_menu, new OnMenuTabSelectedListener() {
            @Override
            public void onMenuItemSelected(int itemId) {
                switch (itemId) {
                    case R.id.accueil_item:
                        try {
                            //remove all others fragments if there exists
                            getSupportFragmentManager().beginTransaction().remove(fgSpots).commit();
                            getSupportFragmentManager().beginTransaction().remove(fgAccueil).commit();
                            getSupportFragmentManager().beginTransaction().remove(fgSpot).commit();
                            // add the new fragment containing the main map
                            getSupportFragmentManager().beginTransaction()
                                    .add(R.id.fragment_container, fgAccueil, "ACCUEIL")
                                    .commit();
                        } catch (Exception e) {
                            Log.e("fragment", e.getMessage());
                        }

                        break;
                    case R.id.spots_item:
                        try {
                            //remove all others fragments if there exists
                            getSupportFragmentManager().beginTransaction().remove(fgSpots).commit();
                            getSupportFragmentManager().beginTransaction().remove(fgAccueil).commit();
                            getSupportFragmentManager().beginTransaction().remove(fgSpot).commit();
                            // add the new fragment containing the list of spots
                            getSupportFragmentManager().beginTransaction()
                                    .add(R.id.fragment_container, fgSpots, "SPOTS")
                                    .commit();
                        } catch (Exception e) {
                            Log.e("fragment", e.getMessage());
                        }
                        break;
                    case R.id.spot_item:
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            // Should we show an explanation?
                            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.CAMERA)) {

                            } else {

                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA},
                                        CAMERA_REQUEST);
                            }
                        } else {
                            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            ContentValues values = new ContentValues();
                            values.put(MediaStore.Images.Media.TITLE, "img-" + System.currentTimeMillis() + ".jpg");
                            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                // Should we show an explanation?
                                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                                } else {

                                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                            WRITE_REQUEST);
                                }
                            } else {
                                mCapturedImageURI = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
                                Log.i("camera", "debut de l'activité");
                                startActivityForResult(cameraIntent, CAMERA_REQUEST);
                            }

                        }

                        break;
                    case R.id.deconnection_item:
                        Intent itdeconnect = new Intent(getApplicationContext(), Connexion.class);
                        finish();
                        startActivity(itdeconnect);
                        break;
                }
            }
        });

        // Set the color for the active tab.
        bottomBar.setActiveTabColor("#E91E63");
        // Use the dark theme.
        // bottomBar.useDarkTheme();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            // photo = (Bitmap) data.getExtras().get("data");
            selectedImagePath = getRealPathFromURI(mCapturedImageURI);
            Log.v("log", "filePath is : " + selectedImagePath);

            Bundle bundle = new Bundle();
            bundle.putString("image", selectedImagePath);
            //showInputDialog(CAMERA_REQUEST);

            super.onPostResume();
            try {
                //remove all others fragments if there exists
                getSupportFragmentManager().beginTransaction().remove(fgSpots).commit();
                getSupportFragmentManager().beginTransaction().remove(fgAccueil).commit();
                getSupportFragmentManager().beginTransaction().remove(fgSpot).commit();
                getSupportFragmentManager().executePendingTransactions();
                // add the new fragment containing the list of spots
                fgSpot.setArguments(bundle);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, fgSpot, "SPOT")
                        .commit();
            } catch (Exception e) {
                Log.e("fragment", e.getMessage());
            }
        }
        else
            Log.i("camera","retour d'un code d'erreur");
    }

    protected void showInputDialog(int code)
    {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        if (code == CAMERA_REQUEST)
        {
            LayoutInflater layoutInflater = LayoutInflater.from(this);
            View promptView = layoutInflater.inflate(R.layout.input_spot, null);
            alertDialogBuilder.setView(promptView);

            final TextView titre = (TextView) promptView.findViewById(R.id.titre);
            final EditText latitude = (EditText) promptView.findViewById(R.id.latitude);
            final EditText longitude = (EditText) promptView.findViewById(R.id.longitude);
            final Spinner visibilite = (Spinner) promptView.findViewById(R.id.visibilite);

            Shader shader = new LinearGradient(
                    0, 0, 0, titre.getTextSize(),
                    Color.RED, Color.BLUE,
                    Shader.TileMode.CLAMP);
            titre.getPaint().setShader(shader);

            if (mLastLocation != null) {
                    longitude.setText(String.valueOf(mLastLocation.getLongitude()));
                latitude.setText(String.valueOf(mLastLocation.getLatitude()));
            }else
                Toast.makeText(getApplicationContext(), "your location is null", Toast.LENGTH_SHORT).show();
            ArrayAdapter<CharSequence> dataAdapter = ArrayAdapter.createFromResource(this,
                    R.array.visibility, android.R.layout.simple_spinner_item);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            visibilite.setAdapter(dataAdapter);

            //rechercher les coordonnées et mettre dans les editview

            // setup a dialog window
            alertDialogBuilder
                    .setCancelable(true)
                    .setPositiveButton("Sauvegarder", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            geoHash.setLatitude(Double.valueOf(latitude.getText().toString()));
                            geoHash.setLongitude(Double.valueOf(longitude.getText().toString()));
                            geoHash.encoder();

                            Spot spot = new Spot();
                            spot.setLongitude(longitude.getText().toString());
                            spot.setLatitude(latitude.getText().toString());
                            spot.setVisibilite(String.valueOf(visibilite.getSelectedItem()));
                            spot.setGeohash(geoHash.getHash());
                            spot.setPhoto(selectedImagePath);

                            db = dbAdapteur.open();
                            long cle = dbAdapteur.insertSpot(spot);
                            if (cle != -1) {
                                // Change the displayed count for this badge.
                                NB_SPOTS ++;
                                nbspots.setCount(NB_SPOTS);
                            }
                            db.close();
                        }
                    })
                    .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //annulation de la création du spot
                        }
                    });
        }
        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();

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
}
