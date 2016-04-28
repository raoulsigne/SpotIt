package techafrkix.work.com.spot.spotit;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import techafrkix.work.com.spot.bd.Spot;
import techafrkix.work.com.spot.bd.SpotsDBAdapteur;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DetailSpot.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DetailSpot#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailSpot extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    public static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1;
    LocationManager locationManager;
    Location mLastLocation;
    LocationRequest locationRequest;
    GoogleApiClient mGoogleApiClient;

    EditText edtLat, edtLong;
    ImageButton imgMoi, imgFriend, imgPublic;
    Button btnvalider;
    TextView txtMoi, txtAmis, txtPublic;

    private SpotsDBAdapteur dbAdapteur;
    SQLiteDatabase db;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String V_MOI = "moi";
    private static final String V_FRIEND = "amis";
    private static final String V_PUBLIC = "public";

    private String visibilite, imagepath;
    private double longitude, latitude;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public DetailSpot() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DetailSpot.
     */
    // TODO: Rename and change types and number of parameters
    public static DetailSpot newInstance(String param1, String param2) {
        DetailSpot fragment = new DetailSpot();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        imagepath = getArguments().getString("image");
        longitude = getArguments().getDouble("longitude");
        latitude = getArguments().getDouble("latitude");
        dbAdapteur = new SpotsDBAdapteur(getActivity());
        buildGoogleApiClient();
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_detail_spot, container, false);

        edtLat = (EditText)view.findViewById(R.id.edtLatitude);
        edtLong = (EditText)view.findViewById(R.id.edtLongitude);
        Button valider = (Button)view.findViewById(R.id.btnValider);
        final ImageButton vMoi = (ImageButton)view.findViewById(R.id.visibiliteMoi);
        final ImageButton vFriend = (ImageButton)view.findViewById(R.id.visibiliteFriend);
        final ImageButton vPublic = (ImageButton)view.findViewById(R.id.visibilitePublic);
        txtMoi = (TextView)view.findViewById(R.id.txtMoi);
        txtAmis = (TextView)view.findViewById(R.id.txtAmis);
        txtPublic = (TextView)view.findViewById(R.id.txtPublic);

        if (longitude != 0)
            edtLong.setText(String.valueOf(longitude));
        if (latitude != 0)
            edtLat.setText(String.valueOf(latitude));

        vMoi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visibilite = V_MOI;
                txtMoi.setTextColor(getResources().getColor(R.color.myblue));
                txtAmis.setTextColor(getResources().getColor(R.color.noir));
                txtPublic.setTextColor(getResources().getColor(R.color.noir));

                vMoi.setBackgroundDrawable(getResources().getDrawable(R.drawable.moi_clicked));
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
                vFriend.setBackgroundDrawable(getResources().getDrawable(R.drawable.friend_clicked));
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
                vPublic.setBackgroundDrawable(getResources().getDrawable(R.drawable.public_clicked));
            }
        });

        valider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (visibilite != "" & edtLong.getText().toString()!="" & edtLat.getText().toString()!="") {
                    GeoHash geoHash = new GeoHash();
                    geoHash.setLatitude(Double.valueOf(edtLat.getText().toString()));
                    geoHash.setLongitude(Double.valueOf(edtLong.getText().toString()));
                    geoHash.encoder();

                    Spot spot = new Spot();
                    spot.setLongitude(edtLong.getText().toString());
                    spot.setLatitude(edtLat.getText().toString());
                    spot.setVisibilite(visibilite);
                    spot.setGeohash(geoHash.getHash());
                    spot.setPhoto(imagepath);

                    db = dbAdapteur.open();
                    long cle = dbAdapteur.insertSpot(spot);
                    if (cle != -1)
                        Log.i("BD", "nouveau spot enregistré");
                    else
                        Log.i("BD", "nouveau spot non enregistré");
                    db.close();

                    edtLat.setText("");
                    edtLong.setText("");
                    vMoi.setBackgroundDrawable(getResources().getDrawable(R.drawable.moi));
                    vFriend.setBackgroundDrawable(getResources().getDrawable(R.drawable.friend));
                    vPublic.setBackgroundDrawable(getResources().getDrawable(R.drawable.publics));
                }
                else
                    Toast.makeText(getActivity(),"Formulaire non conforme",Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
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
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        Toast.makeText(getActivity(), "Failed to connect...", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onConnected(Bundle arg0) {

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
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

            }
        });
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

    }

    @Override
    public void onConnectionSuspended(int arg0) {
        Toast.makeText(getActivity(), "Connection suspended...", Toast.LENGTH_SHORT).show();

    }

    protected synchronized void buildGoogleApiClient() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(12000);
        locationRequest.setFastestInterval(30000);
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

}
