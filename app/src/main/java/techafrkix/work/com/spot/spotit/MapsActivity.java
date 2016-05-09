package techafrkix.work.com.spot.spotit;

import android.Manifest;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.support.v4.app.Fragment;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;

import techafrkix.work.com.spot.bd.Spot;
import techafrkix.work.com.spot.bd.SpotsDBAdapteur;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.MyMarker;

public class MapsActivity extends Fragment implements OnMapReadyCallback {

    public static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1;

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    LocationRequest locationRequest;
    ArrayList<Spot> spots;
    private SpotsDBAdapteur dbAdapteur;
    SQLiteDatabase db;

    private HashMap<Marker, MyMarker> mMarkersHashMap;
    private ArrayList<MyMarker> mMyMarkersArray = new ArrayList<MyMarker>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_maps, container, false);
        spots = new ArrayList<Spot>();
        dbAdapteur = new SpotsDBAdapteur(getActivity());
        mMarkersHashMap = new HashMap<Marker, MyMarker>();

        buildGoogleApiClient();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        } else
            Log.i("Map", "Not connected...");

        SupportMapFragment m = ((SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map));

        m.getMapAsync(this);

        return view;
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
        settings.setIndoorLevelPickerEnabled(true);
        settings.setMapToolbarEnabled(true);
        settings.setAllGesturesEnabled(true);
        settings.setMyLocationButtonEnabled(true);

        //try to position the location button in the sreen bottom
        int width = this.getResources().getDisplayMetrics().widthPixels;
        int height = this.getResources().getDisplayMetrics().heightPixels;
        Log.i("size",width+ " " + height);
        mMap.setPadding(0, height - 220, 0, 0);

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_FINE_LOCATION);
            return;
        }
        else
        {
            mMap.setMyLocationEnabled(true);
        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener()
        {
            @Override
            public boolean onMarkerClick(com.google.android.gms.maps.model.Marker marker)
            {
                marker.showInfoWindow();
                return true;
            }
        });

        displaySpotOnMap();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        final FragmentManager fragManager = this.getFragmentManager();
        final Fragment fragment = fragManager.findFragmentById(R.id.map);
        if(fragment!=null){
            fragManager.beginTransaction().remove(fragment).commit();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(12000);
        locationRequest.setFastestInterval(30000);
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * afficher les spots sur la carte en utilisant leur coordonnées
     */
    private void displaySpotOnMap(){
        db = dbAdapteur.open();
        spots = dbAdapteur.getAllSpots();
        LatLng coordonnees;
        if (spots != null)
            for (Spot s:
                 spots) {
                // Add a marker to spot position
                coordonnees = new LatLng(Double.valueOf(s.getLatitude()), Double.valueOf(s.getLongitude()));
                //mMap.addMarker(new MarkerOptions().position(coordonnees).title("Spot "+ s.getId() + " : " +s.getGeohash()));
                Log.i("map", "marker du spot " + s.getGeohash());
                mMyMarkersArray.add(new MyMarker("Spot "+ s.getId(), s.getPhoto(), Double.valueOf(s.getLatitude()), Double.valueOf(s.getLongitude())));
            }
        db.close();

        plotMarkers(mMyMarkersArray);
    }

    private void plotMarkers(ArrayList<MyMarker> markers)
    {
        if(markers.size() > 0)
        {
            for (MyMarker myMarker : markers)
            {

                // Create user marker with custom icon and other options
                MarkerOptions markerOption = new MarkerOptions().position(new LatLng(myMarker.getmLatitude(), myMarker.getmLongitude()));
                markerOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.currentlocation_icon));

                Marker currentMarker = mMap.addMarker(markerOption);
                mMarkersHashMap.put(currentMarker, myMarker);

                mMap.setInfoWindowAdapter(new MarkerInfoWindowAdapter());
            }
        }
    }

    public class MarkerInfoWindowAdapter implements GoogleMap.InfoWindowAdapter
    {
        public MarkerInfoWindowAdapter()
        {
        }

        @Override
        public View getInfoWindow(Marker marker)
        {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker)
        {
            View v  = getActivity().getLayoutInflater().inflate(R.layout.infowindow_layout, null);

            MyMarker myMarker = mMarkersHashMap.get(marker);

            ImageView markerIcon = (ImageView) v.findViewById(R.id.marker_icon);

            TextView markerLabel = (TextView)v.findViewById(R.id.marker_label);

            markerIcon.setImageBitmap(BitmapFactory.decodeFile(myMarker.getmIcon()));

            markerLabel.setText(myMarker.getmLabel());

            return v;
        }
    }
}

