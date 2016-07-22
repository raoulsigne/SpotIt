package techafrkix.work.com.spot.spotit;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import techafrkix.work.com.spot.bd.Spot;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.AWS_Tools;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.DBServer;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.SessionManager;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Notification.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Notification#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Notification extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public static final int TYPE_FRIENDSHIP = 1;
    public static final int TYPE_NEW_SPOT = 11;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ListView listview_notif;

    private DBServer server;
    private ArrayList<techafrkix.work.com.spot.bd.Notification> list_notifications;
    private techafrkix.work.com.spot.bd.Notification[] notifications;
    private techafrkix.work.com.spot.bd.Notification notification;
    private SessionManager session;
    private HashMap<String, String> profile;
    private int response;
    private Spot spot;

    private OnFragmentInteractionListener mListener;

    public Notification() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Notification.
     */
    // TODO: Rename and change types and number of parameters
    public static Notification newInstance(String param1, String param2) {
        Notification fragment = new Notification();
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        // Session class instance
        session = new SessionManager(getActivity());
        profile = new HashMap<>();
        server = new DBServer(getActivity());
        profile = session.getUserDetails();
        list_notifications = new ArrayList<>();
        notification = new techafrkix.work.com.spot.bd.Notification();

        listview_notif = (ListView)view.findViewById(R.id.listnotifications);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                list_notifications = server.notifications_user(Integer.valueOf(profile.get(SessionManager.KEY_ID))); // amis confirmés
            }});

        t.start(); // spawn thread
        try{
            t.join();

            if (list_notifications != null){
                notifications = new techafrkix.work.com.spot.bd.Notification[list_notifications.size()];
                int i = 0;
                for (i = 0; i < list_notifications.size(); i++) {
                    notifications[i] = list_notifications.get(i);
                }

                CustomList_Notification adapter = new CustomList_Notification(getActivity(), notifications, Notification.this);
                listview_notif.setAdapter(adapter);
            }
        }catch (InterruptedException e) {
            e.printStackTrace();
        }

        listview_notif.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                notification = list_notifications.get(i);
                switch (notification.getTypenotification_id()){
                    case TYPE_FRIENDSHIP:
                        Add_Friend fgFriend = new Add_Friend();
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, fgFriend, "ADD_FRIEND")
                                .commit();
                        ((MainActivity)getActivity()).setAciveTab(MainActivity.MENU_ACTIF_SOCIAL);
                        break;

                    case TYPE_NEW_SPOT:
                        DetailSpot fgDetailspot = new DetailSpot();
                        Bundle args = new Bundle();
                        spot = new Spot();
                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                spot = server.find_spot(notification.getIdspot());
                            }});

                        t.start(); // spawn thread
                        try{
                            t.join();

                            if (spot != null){
                                args.putSerializable("spot", spot);
                                fgDetailspot.setArguments(args);
                                getActivity().getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.container, fgDetailspot, "DETAIL")
                                        .commit();

                            }
                        }catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        break;
                }

            }
        });

        return view;
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
        // void onFragmentInteraction(Uri uri);
    }

    public class CustomList_Notification extends ArrayAdapter<techafrkix.work.com.spot.bd.Notification> {

        private final Activity context;
        private final techafrkix.work.com.spot.bd.Notification[] notifications;
        private FriendCallback mAdapterCallback;

        public CustomList_Notification(Activity context, techafrkix.work.com.spot.bd.Notification[] notifications, Fragment fg) {
            super(context, R.layout.item_friend, notifications);
            this.context = context;
            this.notifications = notifications;
        }
        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView= inflater.inflate(R.layout.notification_item, null, true);

            final ImageView imgProfile = (ImageView) rowView.findViewById(R.id.item_profile);
            TextView txttitre = (TextView) rowView.findViewById(R.id.txtTitre_notif);
            TextView txtdescription = (TextView) rowView.findViewById(R.id.txtDescription_notif);
            TextView txtdate = (TextView) rowView.findViewById(R.id.txtDate_notif);

            if (notifications[position].getPhotosender() != "" & notifications[position].getPhotosender() != null) {
                String dossier = getActivity().getApplicationContext().getFilesDir().getPath() + DBServer.DOSSIER_IMAGE;
                final File file = new File(dossier + File.separator + notifications[position].getPhotosender() + ".jpg");

                if (file.exists()) {
                    // marker.showInfoWindow();
                    imgProfile.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
                    Log.i("file", "file exists");
                } else {
                    if (MapsActivity.isNetworkAvailable(MainActivity.getAppContext())) {
                        Log.i("file", "file not exists");
                        AWS_Tools aws_tools = new AWS_Tools(MainActivity.getAppContext());
                        final ProgressDialog barProgressDialog = new ProgressDialog(getActivity());
                        barProgressDialog.setTitle("Chargement des profiles ...");
                        barProgressDialog.setMessage("Opération en progression ...");
                        barProgressDialog.setProgressStyle(barProgressDialog.STYLE_HORIZONTAL);
                        barProgressDialog.setProgress(0);
                        barProgressDialog.setMax(100);
                        barProgressDialog.show();
                        int transfertId = aws_tools.download(file, notifications[position].getPhotosender());
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
                                try {
                                    Log.i("test", bytesCurrent + " " + bytesTotal);
                                    rapport /= bytesTotal;
                                    barProgressDialog.setProgress(rapport);
                                    if (rapport == 100) {
                                        barProgressDialog.dismiss();
                                        imgProfile.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
                                    }
                                }catch (Exception e){
                                    Log.e("chargement", e.getMessage());
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

            txttitre.setText("Titre");
            txtdescription.setText(notifications[position].getDescription());
            txtdate.setText(notifications[position].getCreated());

            return rowView;
        }
    }
}
