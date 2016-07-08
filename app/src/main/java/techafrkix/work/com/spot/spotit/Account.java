package techafrkix.work.com.spot.spotit;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.google.android.gms.common.api.GoogleApiClient;
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
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.AWS_Tools;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.DBServer;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.MyMarker;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.SessionManager;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Account.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class Account extends Fragment implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleMap.OnMapLoadedCallback {
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
    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    LocationRequest locationRequest;

    private DBServer server;
    private SessionManager session;
    private HashMap<String, String> profile;
    ArrayList<Spot> spots;
    private HashMap<Marker, MyMarker> mMarkersHashMap;
    private ArrayList<MyMarker> mMyMarkersArray = new ArrayList<MyMarker>();

    TextView txtModify, txtLogout, txtPseudo, txtSpots, txtFriends, txtmySpots;
    private ImageButton myspot;
    private de.hdodenhof.circleimageview.CircleImageView imageprofile;

    private int total_spot;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        // Session class instance
        session = new SessionManager(getActivity());
        profile = new HashMap<>();
        mMarkersHashMap = new HashMap<Marker, MyMarker>();
        server = new DBServer(getActivity());
        spots = new ArrayList<Spot>();

        myspot = (ImageButton) view.findViewById(R.id.imgMySpots);
        myspot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onLoadSpot();
            }
        });

        txtModify = (TextView) view.findViewById(R.id.txtmodify);
        txtLogout = (TextView) view.findViewById(R.id.txtlogout);
        txtPseudo = (TextView) view.findViewById(R.id.txtPseudo);
        txtSpots = (TextView) view.findViewById(R.id.txtSpots);
        txtFriends = (TextView) view.findViewById(R.id.txtFriends);
        txtmySpots = (TextView) view.findViewById(R.id.txtMySpot);
        imageprofile = (de.hdodenhof.circleimageview.CircleImageView) view.findViewById(R.id.profile_image);

        txtModify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onSetPhoto();
            }
        });
        txtLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onDisconnect();
            }
        });

        profile = session.getUserDetails();
        txtPseudo.setText(profile.get(SessionManager.KEY_NAME));
        txtSpots.setText(profile.get(SessionManager.KEY_SPOT) + " spots" + " | " + profile.get(SessionManager.KEY_RESPOT) + " respots");
        txtFriends.setText(profile.get(SessionManager.KEY_FRIENDS) + " Friends");
        total_spot = Integer.valueOf(profile.get(SessionManager.KEY_SPOT)) + Integer.valueOf(profile.get(SessionManager.KEY_RESPOT));
        if (total_spot <= 1)
            txtmySpots.setText(total_spot + " Spot");
        else
            txtmySpots.setText(total_spot + " Spots");

        if (profile.get(SessionManager.KEY_PHOTO) != null & profile.get(SessionManager.KEY_PHOTO) != "") {
            Log.i("file", profile.get(SessionManager.KEY_PHOTO));
            String dossier = getActivity().getApplicationContext().getFilesDir().getPath() + DBServer.DOSSIER_IMAGE;
            final File file = new File(dossier + File.separator + profile.get(SessionManager.KEY_PHOTO) + ".jpg");

            if (file.exists()) {
                // marker.showInfoWindow();
                imageprofile.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
            } else {
                if (MapsActivity.isNetworkAvailable(MainActivity.getAppContext())) {
                    Log.i("file", "file not exists");
                    AWS_Tools aws_tools = new AWS_Tools(MainActivity.getAppContext());
                    final ProgressDialog barProgressDialog = new ProgressDialog(getActivity());
                    barProgressDialog.setTitle("Telechargement du spot ...");
                    barProgressDialog.setMessage("Opération en progression ...");
                    barProgressDialog.setProgressStyle(barProgressDialog.STYLE_HORIZONTAL);
                    barProgressDialog.setProgress(0);
                    barProgressDialog.setMax(100);
                    barProgressDialog.show();
                    int transfertId = aws_tools.download(file, profile.get(SessionManager.KEY_PHOTO));
                    TransferUtility transferUtility = aws_tools.getTransferUtility();
                    TransferObserver observer = transferUtility.getTransferById(transfertId);
                    observer.setTransferListener(new TransferListener() {

                        @Override
                        public void onStateChanged(int id, TransferState state) {
                            // do something
                        }

                        @Override
                        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                            int rapport = (int) (bytesCurrent * 100);
                            if (bytesTotal != 0) {
                                rapport /= bytesTotal;
                                if (rapport == 100) {
                                    barProgressDialog.dismiss();
                                    imageprofile.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
                                }
                                barProgressDialog.setProgress(rapport);
                            }else{
                                barProgressDialog.dismiss();
                            }
                        }

                        @Override
                        public void onError(int id, Exception ex) {
                            // do something
                            barProgressDialog.dismiss();
                        }

                    });
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Spot It:Information")
                            .setMessage("Vérifiez votre connexion Internet")
                            .setCancelable(false)
                            .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        }

        buildGoogleApiClient();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        } else
            Log.i("Map", "Not connected...");

        SupportMapFragment m = ((SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.mymap));

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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLoadedCallback(this);
        UiSettings settings = mMap.getUiSettings();
        settings.setCompassEnabled(true);
        settings.setIndoorLevelPickerEnabled(true);
        settings.setMapToolbarEnabled(true);
        settings.setAllGesturesEnabled(true);
        settings.setMyLocationButtonEnabled(false);

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
        } else {
            mMap.setMyLocationEnabled(true);
        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final com.google.android.gms.maps.model.Marker marker) {
                final MyMarker myMarker = mMarkersHashMap.get(marker);
                String dossier = getActivity().getApplicationContext().getFilesDir().getPath() + DBServer.DOSSIER_IMAGE;
                final File file = new File(dossier + File.separator + myMarker.getmIcon() + ".jpg");

                if (file.exists()) {
                    // marker.showInfoWindow();
                    showdialogMarker(myMarker, file);
                    Log.i("file", "file exists");
                } else {
                    if (MapsActivity.isNetworkAvailable(MainActivity.getAppContext())) {
                        Log.i("file", "file not exists");
                        AWS_Tools aws_tools = new AWS_Tools(MainActivity.getAppContext());
                        final ProgressDialog barProgressDialog = new ProgressDialog(getActivity());
                        barProgressDialog.setTitle("Telechargement du spot ...");
                        barProgressDialog.setMessage("Opération en progression ...");
                        barProgressDialog.setProgressStyle(barProgressDialog.STYLE_HORIZONTAL);
                        barProgressDialog.setProgress(0);
                        barProgressDialog.setMax(100);
                        barProgressDialog.show();
                        int transfertId = aws_tools.download(file, myMarker.getmIcon());
                        TransferUtility transferUtility = aws_tools.getTransferUtility();
                        TransferObserver observer = transferUtility.getTransferById(transfertId);
                        observer.setTransferListener(new TransferListener() {

                            @Override
                            public void onStateChanged(int id, TransferState state) {
                                // do something
                            }

                            @Override
                            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                                int rapport = (int) (bytesCurrent * 100);
                                rapport /= bytesTotal;
                                barProgressDialog.setProgress(rapport);
                                if (rapport == 100) {
                                    barProgressDialog.dismiss();
                                    // marker.showInfoWindow();
                                    //display a dialog bout spot detail
                                    showdialogMarker(myMarker, file);
                                }
                            }

                            @Override
                            public void onError(int id, Exception ex) {
                                // do something
                                barProgressDialog.dismiss();
                            }

                        });
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("Spot It:Information")
                                .setMessage("Vérifiez votre connexion Internet")
                                .setCancelable(false)
                                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                }
                return true;
            }
        });

        mMap.clear();
        displaySpotOnMap();
    }

    @Override
    public void onMapLoaded() {
        if (mMap != null) {
            LatLng pos = mMap.getCameraPosition().target;
            Log.i("map", "Pos : " + pos.toString());
        }else
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
        void onLoadSpot();
        void onDisconnect();
        void onSetPhoto();
    }

    protected synchronized void buildGoogleApiClient() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(12000);
        locationRequest.setFastestInterval(30000);
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();
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
    public void onConnected(Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    /**
     * afficher les spots sur la carte en utilisant leur coordonnées
     */
    private void displaySpotOnMap(){
        //clear other markers on map and inside the list before adding new one
        mMap.clear();
        mMyMarkersArray.clear();

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                spots = server.find_spot_user(Integer.valueOf(profile.get(SessionManager.KEY_ID)), 0, total_spot);
            }});

        t.start(); // spawn thread
        try {
            t.join();
            if (spots != null) {
                for (Spot s : spots) {
                    mMyMarkersArray.add(new MyMarker(s.getDate(), s.getGeohash(), s.getPhotokey(), Double.valueOf(s.getLatitude()), Double.valueOf(s.getLongitude())));
                }
                plotMarkers(mMyMarkersArray);
            }
        }catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void plotMarkers(ArrayList<MyMarker> markers)
    {
        if(markers.size() > 0)
        {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(markers.get(0).getmLatitude(), markers.get(0).getmLongitude()), 10));
            for (MyMarker myMarker : markers)
            {
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

    public class MarkerInfoWindowAdapter implements GoogleMap.InfoWindowAdapter
    {

        public MarkerInfoWindowAdapter()
        {}

        @Override
        public View getInfoWindow(Marker marker)
        {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker)
        {
            View v  = getActivity().getLayoutInflater().inflate(R.layout.infomarker, null);

            final MyMarker myMarker = mMarkersHashMap.get(marker);

            final ImageView markerIcon = (ImageView) v.findViewById(R.id.marker_icon);
            final TextView markerDate = (TextView)v.findViewById(R.id.marker_date);
            final TextView markerGeohash = (TextView)v.findViewById(R.id.marker_geohash);


            markerIcon.setImageBitmap(BitmapFactory.decodeFile(getActivity().getFilesDir().getPath()+"/Images/"+myMarker.getmIcon()+".jpg"));
            markerDate.setText(myMarker.getmDate());
            markerGeohash.setText(myMarker.getmGeohash());

            return v;
        }
    }

    private void showdialogMarker(MyMarker marker, File file){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View promptView = layoutInflater.inflate(R.layout.detail_spot, null);
        alertDialogBuilder.setView(promptView);

        final TextView latitude = (TextView) promptView.findViewById(R.id.latitude);
        final TextView longitude = (TextView) promptView.findViewById(R.id.longitude);
        final TextView date = (TextView) promptView.findViewById(R.id.dateSpot);
        final ImageView imgspot = (ImageView)promptView.findViewById(R.id.imgspot);
        final Button fermer = (Button)promptView.findViewById(R.id.btnFermer);
        final Button respoter = (Button)promptView.findViewById(R.id.btnRespoter);

        latitude.setText(String.valueOf(marker.getmLatitude()));
        longitude.setText(String.valueOf(marker.getmLongitude()));
        date.setText(marker.getmDate());
        imgspot.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));

        // create an alert dialog
        final AlertDialog alert = alertDialogBuilder.create();
        alert.show();

        fermer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
            }
        });

    }
}
