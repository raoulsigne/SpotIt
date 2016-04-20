package techafrkix.work.com.spot.spotit;

import android.Manifest;
import android.app.AlertDialog;
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
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabSelectedListener;

import techafrkix.work.com.spot.bd.Spot;
import techafrkix.work.com.spot.bd.SpotsDBAdapteur;

public class MapsActivity extends BaseActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    public static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1;

    private GoogleMap mMap;
    private GeoHash geoHash;
    private SpotsDBAdapteur dbAdapteur;
    SQLiteDatabase db;
    private static final int CAMERA_REQUEST = 1;
    private static final int WRITE_REQUEST = 2;
    Bitmap photo;

    String selectedImagePath;
    Uri mCapturedImageURI;

    LocationManager locationManager;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest locationRequest;

    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        buildGoogleApiClient();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        } else
            Toast.makeText(this, "Not connected...", Toast.LENGTH_SHORT).show();

        dbAdapteur = new SpotsDBAdapteur(getApplicationContext());
        geoHash = new GeoHash();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.base_activity);

        BottomBar bottomBar = BottomBar.attach(this, savedInstanceState);
        bottomBar.setItemsFromMenu(R.menu.main_menu, new OnMenuTabSelectedListener() {
            @Override
            public void onMenuItemSelected(int itemId) {
                switch (itemId) {
                    case R.id.accueil_item:
                        Intent itAccueil = new Intent(getApplicationContext(), MapsActivity.class);
                        finish();
                        startActivity(itAccueil);
                        break;
                    case R.id.spots_item:
                        Intent itSpots = new Intent(getApplicationContext(), ListeSpots.class);
                        finish();
                        startActivity(itSpots);
                        break;
                    case R.id.spot_item:
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            // Should we show an explanation?
                            if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this, Manifest.permission.CAMERA)) {

                            } else {

                                ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.CAMERA},
                                        CAMERA_REQUEST);
                            }
                        }
                        else {
                            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            ContentValues values = new ContentValues();
                            values.put(MediaStore.Images.Media.TITLE, "img-" + System.currentTimeMillis() + ".jpg");
                            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                // Should we show an explanation?
                                if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                                } else {

                                    ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                            WRITE_REQUEST);
                                }
                            }
                            else {
                                mCapturedImageURI  = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
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

        // Set the color for the active tab. Ignored on mobile when there are more than three tabs.
        bottomBar.setActiveTabColor("#C2185B");
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        UiSettings settings = mMap.getUiSettings();
        settings.setCompassEnabled(true);
        settings.setZoomControlsEnabled(true);
        settings.setIndoorLevelPickerEnabled(true);
        settings.setMapToolbarEnabled(true);
//        settings.setMyLocationButtonEnabled(true);

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
        else
        {
            mMap.setMyLocationEnabled(true);
        }

//        // Add a marker in Sydney and move the camera
//        if (mLastLocation == null) {
////            LatLng sydney = new LatLng(-34, 151);
////            mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
////            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
//        }
//        else {
//            LatLng sydney = new LatLng(mLastLocation.getLongitude(), mLastLocation.getLatitude());
//            mMap.addMarker(new MarkerOptions().position(sydney).title("Your location"));
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.map_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
//        switch (item.getItemId()) {
//            case R.id.accueil:
//
//                return true;
//            case R.id.create_new:
//                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//                    // Should we show an explanation?
//                    if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this, Manifest.permission.CAMERA)) {
//
//                    } else {
//
//                        ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.CAMERA},
//                                CAMERA_REQUEST);
//                    }
//                }
//                else {
//                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//                    ContentValues values = new ContentValues();
//                    values.put(MediaStore.Images.Media.TITLE, "img-"+System.currentTimeMillis()+".jpg");
//
//                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                        // Should we show an explanation?
//                        if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//
//                        } else {
//
//                            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                                    WRITE_REQUEST);
//                        }
//                    }
//                    else {
//                        mCapturedImageURI  = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
//                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
//                        startActivityForResult(cameraIntent, CAMERA_REQUEST);
//                    }
//
//                }
//                return true;
//            case R.id.spot:
//                Toast.makeText(getApplicationContext(),"list des spots", Toast.LENGTH_SHORT).show();
//                Intent itSpots = new Intent(getApplicationContext(), ListeSpots.class);
//                startActivity(itSpots);
//                return true;
//            case R.id.disconnect:
//                Intent itdeconnect = new Intent(getApplicationContext(), Connexion.class);
//                finish();
//                startActivity(itdeconnect);
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            // photo = (Bitmap) data.getExtras().get("data");
            selectedImagePath = getRealPathFromURI(mCapturedImageURI);
            Log.v("log","filePath is : "+selectedImagePath);

            showInputDialog(CAMERA_REQUEST);
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

            final EditText latitude = (EditText) promptView.findViewById(R.id.latitude);
            final EditText longitude = (EditText) promptView.findViewById(R.id.longitude);
            final Spinner visibilite = (Spinner) promptView.findViewById(R.id.visibilite);

            if (mLastLocation != null) {
                longitude.setText(String.valueOf(mLastLocation.getLongitude()));
                latitude.setText(String.valueOf(mLastLocation.getLatitude()));
            }else
                Toast.makeText(getApplicationContext(),"your location is null", Toast.LENGTH_SHORT).show();
            ArrayAdapter<CharSequence> dataAdapter = ArrayAdapter.createFromResource(this,
                    R.array.visibility, android.R.layout.simple_spinner_item);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            visibilite.setAdapter(dataAdapter);

            //rechercher les coordonnées et mettre dans les editview

            // setup a dialog window
            alertDialogBuilder
                    .setTitle("DETAIL DU SPOT")
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
                            if (cle != -1)
                                Toast.makeText(getApplicationContext(),"Enregistrement effectué avec succès",Toast.LENGTH_SHORT).show();
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
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
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

//        if (mLastLocation != null) {
//            String location = "Latitude: "+ String.valueOf(mLastLocation.getLatitude())+"Longitude: "+
//                    String.valueOf(mLastLocation.getLongitude());
//            Toast.makeText(getApplicationContext(),location,Toast.LENGTH_LONG).show();
//            LatLng sydney = new LatLng(mLastLocation.getLongitude(), mLastLocation.getLatitude());
//            mMap.addMarker(new MarkerOptions().position(sydney).title("Your location"));
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
//        }
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

}
