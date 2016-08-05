package techafrkix.work.com.spot.spotit;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
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

import techafrkix.work.com.spot.bd.NotificationEntity;
import techafrkix.work.com.spot.bd.Spot;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.AWS_Tools;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.DBServer;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.SessionManager;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NotificationActivity.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NotificationActivity#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NotificationActivity extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public static final int TYPE_FRIENDSHIP = 1;
    public static final int TYPE_NEW_SPOT = 11;
    public static final int TYPE_RESPOT = 12;
    public static final int TYPE_COMMENT = 22;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ListView listview_notif;

    private DBServer server;
    private ArrayList<NotificationEntity> list_notificationEntities;
    private NotificationEntity[] notificationEntities;
    private NotificationEntity notificationEntity;
    private SessionManager session;
    private HashMap<String, String> profile;
    private int response;
    private Spot spot;

    private OnFragmentInteractionListener mListener;

    public NotificationActivity() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NotificationActivity.
     */
    // TODO: Rename and change types and number of parameters
    public static NotificationActivity newInstance(String param1, String param2) {
        NotificationActivity fragment = new NotificationActivity();
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
        list_notificationEntities = new ArrayList<>();
        notificationEntity = new NotificationEntity();

        listview_notif = (ListView)view.findViewById(R.id.listnotifications);
        listview_notif.setDivider(null);
        listview_notif.setDividerHeight(0);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                list_notificationEntities = server.notifications_user(Integer.valueOf(profile.get(SessionManager.KEY_ID))); // amis confirmés
            }});

        t.start(); // spawn thread
        try{
            t.join();

            if (list_notificationEntities != null){
                notificationEntities = new NotificationEntity[list_notificationEntities.size()];
                int i = 0;
                for (i = 0; i < list_notificationEntities.size(); i++) {
                    notificationEntities[i] = list_notificationEntities.get(i);
                }

                CustomList_Notification adapter = new CustomList_Notification(getActivity(), notificationEntities, NotificationActivity.this);
                listview_notif.setAdapter(adapter);
            }
        }catch (InterruptedException e) {
            e.printStackTrace();
        }

        listview_notif.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                notificationEntity = list_notificationEntities.get(i);
                switch (notificationEntity.getTypenotification_id()){
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
                                spot = server.find_spot(notificationEntity.getIdspot());
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

    public class CustomList_Notification extends ArrayAdapter<NotificationEntity> {

        private final Activity context;
        private final NotificationEntity[] notificationEntities;
        private FriendCallback mAdapterCallback;

        public CustomList_Notification(Activity context, NotificationEntity[] notificationEntities, Fragment fg) {
            super(context, R.layout.item_friend, notificationEntities);
            this.context = context;
            this.notificationEntities = notificationEntities;
        }
        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView= inflater.inflate(R.layout.notification_item, null, true);

            final ImageView imgProfile = (ImageView) rowView.findViewById(R.id.item_profile);
            final ImageView icon = (ImageView) rowView.findViewById(R.id.icon_notif);
            TextView txtdescription = (TextView) rowView.findViewById(R.id.txtDescription_notif);
            TextView txtdate = (TextView) rowView.findViewById(R.id.txtDate_notif);

            if (notificationEntities[position].getPhotosender() != "" & notificationEntities[position].getPhotosender() != null) {
                String dossier = getActivity().getApplicationContext().getFilesDir().getPath() + DBServer.DOSSIER_IMAGE;
                final File file = new File(dossier + File.separator + notificationEntities[position].getPhotosender() + ".jpg");

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
                        int transfertId = aws_tools.download(file, notificationEntities[position].getPhotosender());
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

            String[] chaines = notificationEntities[position].getDescription().split(" ");
            StringBuilder string = new StringBuilder();
            string.append("<b>" + chaines[0] + "</b>");
            for (int i=1; i<chaines.length; i++)
                string.append(" " + chaines[i]);

            txtdescription.setText(Html.fromHtml(string.toString()));
            txtdate.setText(notificationEntities[position].getCreated());

            switch (notificationEntities[position].getTypenotification_id()){
                case TYPE_NEW_SPOT:
                    icon.setImageResource(R.drawable.notif_spot);
                    break;

                case TYPE_FRIENDSHIP:
                    icon.setImageResource(R.drawable.notif_friendrequest);
                    break;

                case TYPE_COMMENT:
                    icon.setImageResource(R.drawable.notif_comment);
                    break;

                case TYPE_RESPOT:
                    icon.setImageResource(R.drawable.notif_respot);
                    break;
            }

            return rowView;
        }
    }
}
