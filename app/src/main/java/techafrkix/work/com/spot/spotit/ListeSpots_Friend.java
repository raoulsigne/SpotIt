package techafrkix.work.com.spot.spotit;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import techafrkix.work.com.spot.bd.Spot;
import techafrkix.work.com.spot.bd.Utilisateur;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.AWS_Tools;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.DBServer;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.SessionManager;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ListeSpots_Friend.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ListeSpots_Friend#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListeSpots_Friend extends Fragment implements SpotFriendAdapter.AdapterCallback  {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "friend";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Utilisateur friend;

    private ImageView imgprofile;
    private TextView txtpseudo;
    private TextView txtspots;
    private TextView txtfriend;
    private ListView listspots;

    private ArrayList<Spot> spots;
    private HashMap<String, Bitmap> spotsimages;
    private SpotFriendAdapter adapter;

    private OnFragmentInteractionListener mListener;

    ProgressDialog pDialog;
    private ArrayList<Spot> tampon;
    private int offset;
    private DBServer server;

    private int preLast;

    public ListeSpots_Friend() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ListeSpots_Friend.
     */
    // TODO: Rename and change types and number of parameters
    public static ListeSpots_Friend newInstance(String param1, String param2) {
        ListeSpots_Friend fragment = new ListeSpots_Friend();
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
            friend = (Utilisateur) getArguments().getSerializable(ARG_PARAM3);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        spots = new ArrayList<>();
        offset = 0;
        preLast = 0;
        server = new DBServer(getActivity());

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_liste_spots__friend, container, false);

        imgprofile = (ImageView) view.findViewById(R.id.item_profile);
        txtpseudo = (TextView)view.findViewById(R.id.txtPseudo_friend);
        txtspots = (TextView)view.findViewById(R.id.txtSpots_friend);
        txtfriend = (TextView)view.findViewById(R.id.txtFriends_friend);
        listspots = (ListView) view.findViewById(R.id.listView);

        if (friend != null){
            txtpseudo.setText(friend.getPseudo());
            txtspots.setText(friend.getNbspot() + " spots | " + friend.getNbrespot() + " respots");
            txtfriend.setText(friend.getNbfriends() + " friends");
            if (friend.getPhoto() != "") {
                String dossier = getActivity().getApplicationContext().getFilesDir().getPath() + DBServer.DOSSIER_IMAGE;
                final File file = new File(dossier + File.separator + friend.getPhoto() + ".jpg");

                if (file.exists()) {
                    imgprofile.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
                    Log.i("file", "file exists");
                } else {
                    if (MapsActivity.isNetworkAvailable(MainActivity.getAppContext())) {
                        Log.i("file", "file not exists");
                        AWS_Tools aws_tools = new AWS_Tools(MainActivity.getAppContext());
                        final ProgressDialog barProgressDialog = new ProgressDialog(getActivity());
                        barProgressDialog.setTitle("Download from server ...");
                        barProgressDialog.setMessage("In progress ...");
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
                                        imgprofile.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
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
        }

        // Creating a button - Load More
        Button btnLoadMore = new Button(getActivity());
        btnLoadMore.setText("Load More");
        btnLoadMore.setBackground(getResources().getDrawable(R.drawable.button_blue));
        // Adding button to listview at footer
        listspots.addFooterView(btnLoadMore);

        spotsimages = new HashMap<String, Bitmap>();
        loadSpots();

        /**
         * Listening to Load More button click event
         * */
        btnLoadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // Starting a new async task
                loadSpots();
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

    private void loadSpots() {
        // Showing progress dialog before sending http request
        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage("Please wait..");
        pDialog.setIndeterminate(true);
        pDialog.setCancelable(false);
        pDialog.show();

        tampon = new ArrayList<>();

        Log.i("offset", offset + "");
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                tampon = server.find_spot_user(friend.getId(), offset, offset + 10);
                for (Spot s :
                        tampon) {
                    spots.add(s);
                }
                //set the value of the offset that will be use next time
                offset += 10; //session.putOffset(offset + 10);
                Log.i("test", spots.toString());
            }});

        t.start(); // spawn thread
        try {
            t.join();
            Log.i("spots", spots.toString());
            String dossier = getActivity().getApplicationContext().getFilesDir().getPath()+ DBServer.DOSSIER_IMAGE;
            File folder = new File(dossier);
            if (!folder.exists())
                folder.mkdirs();

            if (spots != null & spots.size() > 0) {
                final ProgressDialog barProgressDialog = new ProgressDialog(getActivity());
                barProgressDialog.setTitle("Telechargement des spots ...");
                barProgressDialog.setMessage("Opération en progression ...");
                barProgressDialog.setProgressStyle(barProgressDialog.STYLE_HORIZONTAL);
                barProgressDialog.setProgress(0);
                barProgressDialog.setMax(spots.size());
                barProgressDialog.show();

                for (final Spot s : spots) {
                    final File file = new File(dossier+ File.separator  + s.getPhotokey() + ".jpg");
                    if (file.exists()) {
                        Log.i("file", "file exists " + dossier + s.getPhotokey() + ".jpg");
                        spotsimages.put(s.getPhotokey(), BitmapFactory.decodeFile(file.getAbsolutePath()));
                        barProgressDialog.setProgress(barProgressDialog.getProgress() + 1);
                        if (barProgressDialog.getProgress() == spots.size()) {
                            barProgressDialog.dismiss();
                            // Create the adapter to convert the array to views
                            adapter = new SpotFriendAdapter(getActivity(), spots, spotsimages, this);
                            // Attach the adapter to a ListView
                            listspots.setAdapter(adapter);
                        }
                    } else {
                        Log.i("file", "file doesn't exists");

                        AWS_Tools aws_tools = new AWS_Tools(MainActivity.getAppContext());
                        int transfertId = aws_tools.download(file, s.getPhotokey());
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
                                if (rapport == 100) {
                                    barProgressDialog.setProgress(barProgressDialog.getProgress() + 1);
                                    spotsimages.put(s.getPhotokey(), BitmapFactory.decodeFile(file.getAbsolutePath()));
                                }
                                if (barProgressDialog.getProgress() == spots.size()) {
                                    barProgressDialog.dismiss();
                                    // get listview current position - used to maintain scroll position
                                    int currentPosition = listspots.getFirstVisiblePosition();

                                    // Create the adapter to convert the array to views
                                    adapter = new SpotFriendAdapter(getActivity(), spots, spotsimages, ListeSpots_Friend.this);
                                    // Attach the adapter to a ListView
                                    listspots.setAdapter(adapter);

                                    // Setting new scroll position
                                    listspots.setSelectionFromTop(currentPosition + 1, 0);
                                }
                            }

                            @Override
                            public void onError(int id, Exception ex) {
                                // do something
                                barProgressDialog.dismiss();
                            }

                        });
                    }
                }
            }
        }catch (InterruptedException e) {
            e.printStackTrace();
        }

        // closing progress dialog
        pDialog.dismiss();
    }

    @Override
    public void detail(int position) {
        Toast.makeText(getActivity(), "Detail " + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void share(int position) {
        Toast.makeText(getActivity(),"Share " + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void letsgo(int position) {
        Toast.makeText(getActivity(),"Let's go " + position, Toast.LENGTH_SHORT).show();
    }
}

class SpotFriendAdapter extends ArrayAdapter<Spot> {

    HashMap<String, Bitmap> mapimages;
    private AdapterCallback mAdapterCallback;

    public SpotFriendAdapter(Context context, ArrayList<Spot> spots, Fragment fg) {
        super(context, 0, spots);
        try {
            this.mAdapterCallback = ((AdapterCallback) fg);
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement AdapterCallback.");
        }
    }

    public SpotFriendAdapter(Context context, ArrayList<Spot> spots, HashMap<String, Bitmap> spotsimages, Fragment fg) {
        super(context, 0, spots);
        mapimages = new HashMap<String, Bitmap>();
        mapimages = spotsimages;
        try {
            this.mAdapterCallback = ((AdapterCallback) fg);
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement AdapterCallback.");
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        TextView spotDate, spotTag;
        TextView txtshare, txtcomment, txtletsgo;
        ImageView spotPhoto;

        // Get the data item for this position
        Spot spot = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_spot_friend, parent, false);
        }

        // Lookup view for data population
        spotDate = (TextView)convertView.findViewById(R.id.txtDate);
        spotTag = (TextView)convertView.findViewById(R.id.txtTag);
        spotPhoto = (ImageView)convertView.findViewById(R.id.imgSpot);

        txtcomment = (TextView)convertView.findViewById(R.id.txtComments);
        txtletsgo = (TextView)convertView.findViewById(R.id.txtLetsgo);
        txtshare = (TextView)convertView.findViewById(R.id.txtShare);
        txtcomment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapterCallback.detail(position);
            }
        });
        txtletsgo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapterCallback.letsgo(position);
            }
        });
        txtshare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapterCallback.share(position);
            }
        });

        // Populate the data into the template view using the data object
        try {
            spotDate.setText(spot.getDate());
            StringBuilder chainetag = new StringBuilder();
            if (spot.getTags().size() == 0)
                spotTag.setText("No tag");
            else {
                for (String s :
                        spot.getTags()) {
                    chainetag.append("#" + s + " ");
                }
                spotTag.setText(chainetag.toString());
            }
            Bitmap bitmap = mapimages.get(spot.getPhotokey()); //BitmapFactory.decodeFile(spot.getPhotokey());

            // Get height or width of screen at runtime
            Display display = ((Activity) getContext()).getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;

            //reduce the photo dimension keeping the ratio so that it'll fit in the imageview
            int nh = (int) ( bitmap.getHeight() * (Double.valueOf(width) / bitmap.getWidth()) );
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, width, nh, true);

            //define the image source of the imageview
            spotPhoto.setImageBitmap(scaled);
        }catch (Exception e){
            Log.e("spot", e.getMessage());}



        // Return the completed view to render on screen
        return convertView;
    }

    public interface AdapterCallback{
        public void detail(int position);
        public void share(int position);
        public void letsgo(int position);
    }
}
