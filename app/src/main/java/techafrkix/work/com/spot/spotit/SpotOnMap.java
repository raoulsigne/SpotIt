package techafrkix.work.com.spot.spotit;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import techafrkix.work.com.spot.bd.Spot;

public class SpotOnMap extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    ArrayList<Spot> spots;

    public static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spot_on_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
//        settings.setZoomControlsEnabled(true);
        settings.setIndoorLevelPickerEnabled(true);
        settings.setMapToolbarEnabled(true);
        settings.setMyLocationButtonEnabled(true);

        //try to position the location button in the sreen bottom
        int width = this.getResources().getDisplayMetrics().widthPixels;
        int height = this.getResources().getDisplayMetrics().heightPixels;
        Log.i("size", width + " " + height);
        mMap.setPadding(0, height - 220, 0, 0);

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

        spots = new ArrayList<Spot>();
        LatLng coordonnees;
        Intent intent = getIntent();
        if (intent.getExtras()!=null){
            spots = (ArrayList<Spot>)intent.getExtras().getSerializable("spots");
            for (Spot s :
                    spots) {
                // Add a marker in Sydney and move the camera
                coordonnees = new LatLng(Double.valueOf(s.getLatitude()),Double.valueOf(s.getLongitude()));
                mMap.addMarker(new MarkerOptions().position(coordonnees).title("Spot "+s.getId()+" : Hash = "+s.getGeohash()));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(coordonnees));
            }
        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                String hash = marker.getTitle().split("=")[1].toLowerCase().replace(" ", "");
                Spot s = new Spot();
                int i = 0;
                for (i = 0; i < spots.size(); i++) {
                    Log.i("test ", spots.get(i).getGeohash().toLowerCase().replace(" ", "") + " , " + hash);
                    if (spots.get(i).getGeohash().toLowerCase().replace(" ", "").equals(hash))
                        s = spots.get(i);
                }
                showdialog(s);
                return false;
            }
        });
    }

    private void showdialog(Spot s){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.detail_spot, null);
        alertDialogBuilder.setView(promptView);

        final TextView titre = (TextView) promptView.findViewById(R.id.titre);
        final EditText latitude = (EditText) promptView.findViewById(R.id.latitude);
        final EditText longitude = (EditText) promptView.findViewById(R.id.longitude);
        final ImageView image = (ImageView) promptView.findViewById(R.id.imgspot);
        final Spinner visibilite = (Spinner) promptView.findViewById(R.id.visibilite);

        Shader shader = new LinearGradient(
                0, 0, 0, titre.getTextSize(),
                Color.RED, Color.BLUE,
                Shader.TileMode.CLAMP);
        titre.getPaint().setShader(shader);

        ArrayAdapter<CharSequence> dataAdapter = ArrayAdapter.createFromResource(this,
                R.array.visibility, android.R.layout.simple_spinner_item);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        visibilite.setAdapter(dataAdapter);
        List<String> items = Arrays.asList(getResources().getStringArray(R.array.visibility));

        image.setImageBitmap(BitmapFactory.decodeFile(s.getPhotokey()));
        latitude.setText(s.getLatitude());
        latitude.setEnabled(false);
        longitude.setText(s.getLongitude());
        longitude.setEnabled(false);
        visibilite.setSelection(items.indexOf(s.getVisibilite()));
        visibilite.setEnabled(false);
        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        switch (requestCode){
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION :
                break;
        }
    }
}