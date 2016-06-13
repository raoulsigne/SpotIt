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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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

import techafrkix.work.com.spot.bd.Utilisateur;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.AWS_Tools;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.DBServer;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.SessionManager;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Add_Friend.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Add_Friend#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Add_Friend extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ListView lvfriends;
    private EditText edtFindspot;
    private Button btnLaunch;

    private DBServer server;
    private Utilisateur[] friends;
    private ArrayList<Utilisateur> users;
    private SessionManager session;
    private HashMap<String, String> profile;

    private OnFragmentInteractionListener mListener;

    public Add_Friend() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Add_Friend.
     */
    // TODO: Rename and change types and number of parameters
    public static Add_Friend newInstance(String param1, String param2) {
        Add_Friend fragment = new Add_Friend();
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
        View view = inflater.inflate(R.layout.fragment_add__friend, container, false);
        edtFindspot = (EditText) view.findViewById(R.id.edtFindspot);
        lvfriends = (ListView) view.findViewById(R.id.friends);
        btnLaunch = (Button)view.findViewById(R.id.btnLaunch);

        // Session class instance
        session = new SessionManager(getActivity());
        profile = new HashMap<>();
        server = new DBServer(getActivity());
        profile = session.getUserDetails();
        users = new ArrayList<>();

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                users = server.getAllFriends(Integer.valueOf(profile.get(SessionManager.KEY_ID)));
            }});

        t.start(); // spawn thread
        try{
            t.join();
            if (users != null & users.size()>0){
                friends = new Utilisateur[users.size()];
                for (int i = 0; i < users.size(); i++)
                    friends[i] = users.get(i);
                Log.i("friend", users.toString());
                CustomList adapter = new CustomList(getActivity(), friends);
                lvfriends.setAdapter(adapter);
            }
        }catch (InterruptedException e) {
            e.printStackTrace();
        }

        btnLaunch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String cle = edtFindspot.getText().toString();
                if (!TextUtils.isEmpty(cle)) {
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            users = server.getUsers_by_pseudo(cle);
                        }
                    });

                    t.start(); // spawn thread
                    try {
                        t.join();
                        if (users != null & users.size() > 0) {
                            String[] items = new String[users.size()];
                            for (int i = 0; i < users.size(); i++)
                                items[i] = users.get(i).getPseudo();
                            Log.i("test", items.toString());
                            CustomList_Search adapter = new CustomList_Search(getActivity(), items, cle);
                            lvfriends.invalidate();
                            lvfriends.setAdapter(adapter);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        lvfriends.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mListener.onLoadFriend(users.get(position));
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
        void onLoadFriend(Utilisateur friend);
    }

    public class CustomList extends ArrayAdapter<Utilisateur> {

        private final Activity context;
        private final Utilisateur[] utilisateurs;

        public CustomList(Activity context, Utilisateur[] utilisateurs) {
            super(context, R.layout.item_friend, utilisateurs);
            this.context = context;
            this.utilisateurs = utilisateurs;

        }
        @Override
        public View getView(int position, View view, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView= inflater.inflate(R.layout.item_friend, null, true);

            final ImageView imgProfile = (ImageView) rowView.findViewById(R.id.item_profile);
            TextView txtPseudo = (TextView) rowView.findViewById(R.id.txtPseudo_friend);
            TextView txtSpot = (TextView) rowView.findViewById(R.id.txtSpots_friend);
            TextView txtFriend = (TextView) rowView.findViewById(R.id.txtFriends_friend);

            if (utilisateurs[position].getPhoto() != "") {
                String dossier = getActivity().getApplicationContext().getFilesDir().getPath() + DBServer.DOSSIER_IMAGE;
                final File file = new File(dossier + File.separator + utilisateurs[position].getPhoto() + ".jpg");

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
                        int transfertId = aws_tools.download(file, utilisateurs[position].getPhoto());
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

            txtPseudo.setText(utilisateurs[position].getPseudo());
            txtSpot.setText(utilisateurs[position].getNbspot() + " spots | " + utilisateurs[position].getNbrespot() + " respots");
            txtFriend.setText(utilisateurs[position].getNbfriends() + " Friends");

            return rowView;
        }
    }

    public class CustomList_Search extends ArrayAdapter<String> {

        private final Activity context;
        private final String[] items;
        private final String cle;

        public CustomList_Search(Activity context, String[] items, String cle) {
            super(context, R.layout.item_search, items);
            this.context = context;
            this.items = items;
            this.cle = cle;
        }
        @Override
        public View getView(int position, View view, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView= inflater.inflate(R.layout.item_search, null, true);

            final ImageView imgaction = (ImageView) rowView.findViewById(R.id.imgaction);
            TextView txtPseudo = (TextView) rowView.findViewById(R.id.txtPseudo_friend);
            TextView txtcle = (TextView) rowView.findViewById(R.id.txtcle);

            txtPseudo.setText(items[position]);
            txtcle.setText(cle);
            imgaction.setBackground(getResources().getDrawable(R.drawable.group_button));

            return rowView;
        }
    }
}
