package techafrkix.work.com.spot.spotit;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import techafrkix.work.com.spot.bd.Spot;
import techafrkix.work.com.spot.bd.SpotsDBAdapteur;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.AWS_Tools;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.DBServer;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.GeoHash;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.MyMarker;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.SessionManager;

public class MapsActivity extends Fragment implements OnMapReadyCallback, android.location.LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleMap.OnMapLoadedCallback, GoogleApiClient.OnConnectionFailedListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    static final int REQUEST_IMAGE_CAPTURE = 1;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    private OnFragmentInteractionListener mListener;

    public static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1;
    public static final int CALIBRAGE_MAX = 8;
    private static final int MARKER_DIALOG = 2;
    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    LocationRequest locationRequest;
    Location mLastLocation;
    ArrayList<Spot> spots;

    private HashMap<Marker, MyMarker> mMarkersHashMap;
    private HashMap<MyMarker, Spot> mappage;
    private ArrayList<MyMarker> mMyMarkersArray = new ArrayList<MyMarker>();

    private SessionManager session;
    private HashMap<String, String> profile;
    private DBServer server;
    private GeoHash geohash;
    private LatLng middle;
    private int zoomlevel;

    private ImageView locateme, myspot;
    private TextView txtmyspot;
    private LinearLayout findspot;

    DownloadSpotsTask task;

    int resultat;
    int current_spot_id;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_maps, container, false);
        spots = new ArrayList<Spot>();
        mMarkersHashMap = new HashMap<Marker, MyMarker>();
        mappage = new HashMap<>();

        locateme = (ImageView) view.findViewById(R.id.imgLocateMe);
        myspot = (ImageView) view.findViewById(R.id.imgMySpots);
        txtmyspot = (TextView) view.findViewById(R.id.txtMySpot);
        findspot = (LinearLayout) view.findViewById(R.id.findspot);

        findspot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onSearchSpot();
            }
        });

        locateme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
                Criteria criteria = new Criteria();

                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   publics void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSIONS_REQUEST_FINE_LOCATION);
                    return;
                } else {
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                        }
                    });
                    Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                    if (location != null) {
                        int maxZoomLevel = (int) mMap.getMaxZoomLevel();
                        if (maxZoomLevel > 0)
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), maxZoomLevel));
                        else
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));

//                        CameraPosition cameraPosition = new CameraPosition.Builder()
//                                .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
//                                .zoom(17)                   // Sets the zoom
//                                //.bearing(90)                // Sets the orientation of the camera to east
//                                //.tilt(40)                   // Sets the tilt of the camera to 30 degrees
//                                .build();                   // Creates a CameraPosition from the builder
//                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                    } else
                        Log.i("dialog", "location null");
                }
            }
        });
        myspot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onLoadSpot(spots);
            }
        });

        // Session class instance
        session = new SessionManager(getActivity());
        profile = new HashMap<>();
        server = new DBServer(getActivity());
        profile = session.getUserDetails();

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

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
        mMap.setOnMapLoadedCallback(this);
        UiSettings settings = mMap.getUiSettings();
        settings.setMyLocationButtonEnabled(false);

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   publics void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_FINE_LOCATION);
            return;
        } else {
            mMap.setMyLocationEnabled(true);
        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final com.google.android.gms.maps.model.Marker marker) {
                MyMarker myMarker = mMarkersHashMap.get(marker);
                current_spot_id = myMarker.getmSpot_ID();

                Spot spot = mappage.get(myMarker);
                if (spot != null) {
                    Log.i("spot detail", spot.toString());
                    mListener.onDetailSpot(spot, 1);
                }

                return true;
            }
        });

        mMap.setOnCameraChangeListener(getCameraChangeListener());
        mMap.clear();
    }


    @Override
    public void onMapLoaded() {
//        mMap.animateCamera( CameraUpdateFactory.zoomTo( 11.0f ) );

        if (mMap != null) {
            middle = mMap.getCameraPosition().target;
            Log.i("map", "Pos : " + middle.toString());
//            displaySpotOnMap(0);
        } else
            Log.i("map", "Map is nulle ");
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onLoadSpot(ArrayList<Spot> spots);
        void onDetailSpot(Spot spot, int i);
        void onSearchSpot();
    }

    protected synchronized void buildGoogleApiClient() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(12000);
        locationRequest.setFastestInterval(30000);
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * afficher les spots sur la carte en utilisant leur coordonnées
     */
    private void displaySpotOnMap() {
        //clear other markers on map and inside the list before adding new one
        mMyMarkersArray.clear();
        profile = session.getUserDetails();

        if (middle != null) {
            if (middle.latitude != 0) {
                geohash = new GeoHash(middle.latitude, middle.longitude);
                int maxZoomLevel = (int) mMap.getMaxZoomLevel();
                int long_hash = (int) ((zoomlevel * CALIBRAGE_MAX) / maxZoomLevel);
                Log.i("map", "longueur geohash = " + long_hash);
                geohash.setLong_hash(long_hash);
                geohash.setLong_bits(geohash.getLong_hash() * geohash.LONG_DIGIT);
                geohash.encoder();

                task = new DownloadSpotsTask();
                task.execute(geohash.neighbours_1(geohash.getHash()));
            }
        }
    }

    private void plotMarkers(ArrayList<MyMarker> markers) {
        mMap.clear();
        if (markers.size() > 0) {
            for (MyMarker myMarker : markers) {
                // Create user marker with custom icon and other options
                MarkerOptions markerOption = new MarkerOptions().position(new LatLng(myMarker.getmLatitude(), myMarker.getmLongitude()));
                markerOption.icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_ROSE));

                Marker currentMarker = mMap.addMarker(markerOption);
                mMarkersHashMap.put(currentMarker, myMarker);

                mMap.setInfoWindowAdapter(new MarkerInfoWindowAdapter());
            }
        }
    }

    public GoogleMap.OnCameraChangeListener getCameraChangeListener() {
        return new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
                LatLng pos = mMap.getCameraPosition().target;
                int zoom =  (int)position.zoom;
                Log.i("teste", pos.toString());
                if (zoom != zoomlevel) {
                    zoomlevel = zoom;
                    displaySpotOnMap();
                }else if (pos != middle){
                    middle = pos;
                    displaySpotOnMap();
                }
            }
        };
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   publics void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_FINE_LOCATION);
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        if (mLastLocation != null) {
            Log.i("map", "Latitude: " + String.valueOf(mLastLocation.getLatitude()) + " Longitude: " +
                    String.valueOf(mLastLocation.getLongitude()));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())));
            middle = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
//            displaySpotOnMap(0);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("map", "connexion suspend");
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i("map", "connexion failed");
    }

    public static boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    public class MarkerInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        public MarkerInfoWindowAdapter() {
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            View v = getActivity().getLayoutInflater().inflate(R.layout.infomarker, null);

            final MyMarker myMarker = mMarkersHashMap.get(marker);

            final ImageView markerIcon = (ImageView) v.findViewById(R.id.marker_icon);
            final TextView markerDate = (TextView) v.findViewById(R.id.marker_date);
            final TextView markerGeohash = (TextView) v.findViewById(R.id.marker_geohash);


            markerIcon.setImageBitmap(BitmapFactory.decodeFile(getActivity().getFilesDir().getPath() + "/Images/" + myMarker.getmIcon() + ".jpg"));
            markerDate.setText(myMarker.getmDate());
            markerGeohash.setText(myMarker.getmGeohash());

            return v;
        }
    }

    private class DownloadSpotsTask extends AsyncTask<String[], Integer, Long> {

        @Override
        protected Long doInBackground(String[]... hashs) {
            int count = hashs.length;
            long totalSize = 0;
            for (int i = 0; i < count; i++) {
                spots = server.find_spots(hashs[i]);

                // Escape early if cancel() is called
                if (isCancelled()) break;
            }
            return totalSize;
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Long result) {
            if (spots != null) {
                mappage = new HashMap<>();
                //afficher le nombre de spots
                int n = spots.size();
                if (n <= 1)
                    txtmyspot.setText(n + " Spot");
                else
                    txtmyspot.setText(n + " Spots");
                for (Spot s : spots) {
                    MyMarker m = new MyMarker(s.getDate(), s.getGeohash(), s.getPhotokey(), Double.valueOf(s.getLatitude()),
                            Double.valueOf(s.getLongitude()), s.getId());
                    mMyMarkersArray.add(m);
                    mappage.put(m, s);
                }
            } else
                Log.i("dialog", "spot null");
            plotMarkers(mMyMarkersArray);
        }
    }
}

