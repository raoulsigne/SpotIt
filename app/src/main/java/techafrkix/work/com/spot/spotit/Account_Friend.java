package techafrkix.work.com.spot.spotit;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import techafrkix.work.com.spot.bd.Spot;
import techafrkix.work.com.spot.bd.Utilisateur;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.AWS_Tools;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.DBServer;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.MyMarker;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.SessionManager;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Account_Friend.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Account_Friend#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Account_Friend extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    LocationRequest locationRequest;

    private  Utilisateur friend;
    ArrayList<Spot> spots, respots;
    private HashMap<Marker, MyMarker> mMarkersHashMap;
    private ArrayList<MyMarker> mMyMarkersArray = new ArrayList<MyMarker>();
    private DBServer server;

    private int total_spot;

    private OnFragmentInteractionListener mListener;
    private ImageView imgmap, imgspots, imgfriends, imgretour;

    private SpotUser fgSpotuser;
    private ListRespots fgRespot;
    private Add_Friend fgAddfrient;

    public Account_Friend() {
        // Required empty publics constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Account_Friend.
     */
    // TODO: Rename and change types and number of parameters
    public static Account_Friend newInstance(String param1, String param2) {
        Account_Friend fragment = new Account_Friend();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        friend = new Utilisateur();
        if (getArguments() != null) {
            friend = (Utilisateur) getArguments().getSerializable("friend");
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        spots = new ArrayList<Spot>();
        respots = new ArrayList<>();
        mMarkersHashMap = new HashMap<Marker, MyMarker>();
        server = new DBServer(getActivity());

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account__friend, container, false);
        final ImageView item_profile = (ImageView) view.findViewById(R.id.imgprofile_friend);

        imgmap = (ImageView) view.findViewById(R.id.imgMapHome);
        imgspots = (ImageView) view.findViewById(R.id.imgListSpot);
        imgfriends = (ImageView) view.findViewById(R.id.imgNotification);
        imgretour = (ImageView) view.findViewById(R.id.imgretour);

        fgSpotuser = new SpotUser();
        fgRespot = new ListRespots();
        fgAddfrient = new Add_Friend();

        ImageView play_spot = (ImageView) view.findViewById(R.id.imgMySpots_friends);
        TextView txtPseudo = (TextView) view.findViewById(R.id.txtPseudo_friend);
        TextView txtSpot = (TextView) view.findViewById(R.id.txtSpots_friend);
        TextView txtRespot = (TextView) view.findViewById(R.id.txtRespots_friend);
        TextView txtNbSpot = (TextView) view.findViewById(R.id.txtMySpots_friends);
        TextView txtNbFriend = (TextView) view.findViewById(R.id.txtFriends_friend);

        if (friend != null){
            txtPseudo.setText(friend.getPseudo());
            txtSpot.setText(String.valueOf(friend.getNbspot()));
            txtRespot.setText(String.valueOf(friend.getNbrespot()));
            txtNbSpot.setText((friend.getSpot()) + " Spots");
            txtNbFriend.setText(String.valueOf(friend.getNbfriends()));
            total_spot = friend.getNbspot() + friend.getNbrespot();
        }

        play_spot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onListSpot_Friend(friend);
            }
        });

        //default active tab
        setAciveTab(1);

        imgmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAciveTab(1);
                getChildFragmentManager().beginTransaction().remove(fgSpotuser).commit();
                fgSpotuser = new SpotUser();
                Bundle bundle = new Bundle();
                bundle.putSerializable("friend", friend);
                fgSpotuser.setArguments(bundle);
                getChildFragmentManager().beginTransaction()
                        .replace(R.id.friendmap, fgSpotuser, "SPOT")
                        .commit();
            }
        });
        imgspots.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        respots = server.find_respot_user(friend.getId(), 0, friend.getSpot());
                    }
                });

                t.start(); // spawn thread
                try {
                    t.join();
                    if (respots != null) {
                        getChildFragmentManager().beginTransaction().remove(fgRespot).commit();
                        fgRespot = new ListRespots();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("spots", respots);
                        fgRespot.setArguments(bundle);
                        getChildFragmentManager().beginTransaction()
                                .replace(R.id.friendmap, fgRespot, "RESPOT")
                                .commit();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                setAciveTab(2);
            }
        });
        imgfriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAciveTab(3);
                getChildFragmentManager().beginTransaction().remove(fgAddfrient).commit();
                fgAddfrient = new Add_Friend();
                Bundle bundle = new Bundle();
                bundle.putSerializable("friend", friend);
                fgAddfrient.setArguments(bundle);
                getChildFragmentManager().beginTransaction()
                        .replace(R.id.friendmap, fgAddfrient, "FRIEND")
                        .commit();
            }
        });
        imgretour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onLoadAccount(2);
            }
        });

        if (friend.getPhoto() != "") {
            final File file = new File(DBServer.DOSSIER_IMAGE + File.separator + friend.getPhoto() + ".jpg");

            if (file.exists()) {
                // marker.showInfoWindow();
                item_profile.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
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
                    int transfertId = aws_tools.download(file, friend.getPhoto());
                    TransferUtility transferUtility = aws_tools.getTransferUtility();
                    TransferObserver observer = transferUtility.getTransferById(transfertId);
                    observer.setTransferListener(new TransferListener() {

                        @Override
                        public void onStateChanged(int id, TransferState state) {
                            // do something
                        }

                        @Override
                        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                            try {
                                int rapport = (int) (bytesCurrent * 100);
                                rapport /= bytesTotal;
                                barProgressDialog.setProgress(rapport);
                                if (rapport == 100) {
                                    barProgressDialog.dismiss();
                                    item_profile.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
                                }
                            }catch (Exception e){
                                Log.e("erreur", e.getMessage());
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
                .findFragmentById(R.id.friendmap));

        m.getMapAsync(this);

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
    public void onDestroy() {
        super.onDestroy();
        final FragmentManager fragManager = this.getFragmentManager();
        final Fragment fragment = fragManager.findFragmentById(R.id.map);
        if(fragment!=null){
            fragManager.beginTransaction().remove(fragment).commit();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final com.google.android.gms.maps.model.Marker marker) {
                final MyMarker myMarker = mMarkersHashMap.get(marker);
                final File file = new File(DBServer.DOSSIER_IMAGE + File.separator + myMarker.getmIcon() + ".jpg");

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
                                if (bytesTotal != 0) {
                                    rapport /= bytesTotal;
                                    barProgressDialog.setProgress(rapport);
                                    if (rapport == 100) {
                                        barProgressDialog.dismiss();
                                        // marker.showInfoWindow();
                                        //display a dialog bout spot detail
                                        showdialogMarker(myMarker, file);
                                    }
                                }else
                                    barProgressDialog.dismiss();
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

        displaySpotOnMap();
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnected(Bundle bundle) {

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
        void onListSpot_Friend(Utilisateur friend);
        void onLoadAccount(int i);
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
                spots = server.find_spot_user(friend.getId(), 0, total_spot);
            }});

        t.start(); // spawn thread
        try {
            t.join();
            if (spots != null) {
                for (Spot s : spots) {
                    mMyMarkersArray.add(new MyMarker(s.getDate(), s.getGeohash(), s.getPhotokey(), Double.valueOf(s.getLatitude()),
                            Double.valueOf(s.getLongitude()), s.getId()));
                }
                plotMarkers(mMyMarkersArray);
            }
        }catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void plotMarkers(ArrayList<MyMarker> markers)
    {
        mMap.clear();
        if(markers.size() > 0)
        {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(markers.get(0).getmLatitude(), markers.get(0).getmLongitude()), 14));
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

    public void setAciveTab(int menu) {
        switch (menu) {
            case 0:

                imgmap.setBackground(getResources().getDrawable(R.drawable.mapfriends));
                imgspots.setBackground(getResources().getDrawable(R.drawable.spots));
                imgfriends.setBackground(getResources().getDrawable(R.drawable.friendsaccount));
                break;

            case 1:

                imgmap.setBackground(getResources().getDrawable(R.drawable.mapfriends_clicked));
                imgspots.setBackground(getResources().getDrawable(R.drawable.spots));
                imgfriends.setBackground(getResources().getDrawable(R.drawable.friendsaccount));

                break;
            case 2:

                imgmap.setBackground(getResources().getDrawable(R.drawable.mapfriends));
                imgspots.setBackground(getResources().getDrawable(R.drawable.spots_clicked));
                imgfriends.setBackground(getResources().getDrawable(R.drawable.friendsaccount));
                break;

            case 3:

                imgmap.setBackground(getResources().getDrawable(R.drawable.mapfriends));
                imgspots.setBackground(getResources().getDrawable(R.drawable.spots));
                imgfriends.setBackground(getResources().getDrawable(R.drawable.friends_clicked));
                break;

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
