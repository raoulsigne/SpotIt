package techafrkix.work.com.spot.spotit;

import android.Manifest;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import techafrkix.work.com.spot.bd.SpotsDBAdapteur;

public class Main2Activity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, DetailSpot.OnFragmentInteractionListener, FragmentAccueil.OnFragmentInteractionListener {

    MapsActivity fgAccueil;
    ListeSpots fgSpots;
    DetailSpot fgSpot;
    FragmentTransaction ft;

    public static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

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
                .add(R.id.fragment_container2, fgAccueil, "ACCUEIL")
                .commit();

        CheckEnableGPS();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
            case R.id.accueil_item:
                try {
                    //remove all others fragments if there exists
                    getSupportFragmentManager().beginTransaction().remove(fgSpots).commit();
                    getSupportFragmentManager().beginTransaction().remove(fgAccueil).commit();
                    getSupportFragmentManager().beginTransaction().remove(fgSpot).commit();
                    // add the new fragment containing the main map
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container2, fgAccueil, "ACCUEIL")
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
                            .replace(R.id.fragment_container2, fgSpots, "SPOTS")
                            .commit();
                } catch (Exception e) {
                    Log.e("fragment", e.getMessage());
                }
                break;
            case R.id.spot_item:
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(Main2Activity.this, Manifest.permission.CAMERA)) {

                    } else {

                        ActivityCompat.requestPermissions(Main2Activity.this, new String[]{Manifest.permission.CAMERA},
                                CAMERA_REQUEST);
                    }
                } else {
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.TITLE, "img-" + System.currentTimeMillis() + ".jpg");
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        // Should we show an explanation?
                        if (ActivityCompat.shouldShowRequestPermissionRationale(Main2Activity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                        } else {

                            ActivityCompat.requestPermissions(Main2Activity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
        else{
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
        //restart();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
        //restart();
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
                        ActivityCompat.requestPermissions(Main2Activity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
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
                                    ActivityCompat.requestPermissions(Main2Activity.this, new String[]{Manifest.permission.CAMERA},
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
                                    ActivityCompat.requestPermissions(Main2Activity.this, new String[]{Manifest.permission.CAMERA},
                                            WRITE_REQUEST);
                                }
                            })
                            .show();
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void restart(){
        if (Build.VERSION.SDK_INT >= 11) {
            recreate();
        } else {
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }
    }
}