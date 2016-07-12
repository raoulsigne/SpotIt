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
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import android.widget.Toast;

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
public class Add_Friend extends Fragment implements FriendCallback{
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
    private ArrayList<String> friends;
    private ArrayList<Utilisateur> users, waiting_friends;
    private Utilisateur[] tampons;
    private SessionManager session;
    private HashMap<String, String> profile;
    private int response;

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

        registerForContextMenu(lvfriends);

        // Session class instance
        session = new SessionManager(getActivity());
        profile = new HashMap<>();
        server = new DBServer(getActivity());
        profile = session.getUserDetails();
        users = new ArrayList<>();
        waiting_friends = new ArrayList<>();
        friends = new ArrayList<>();

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                users = server.getAllFriends(Integer.valueOf(profile.get(SessionManager.KEY_ID))); // amis confirmés
            }});

        t.start(); // spawn thread
        try{
            t.join();

            Thread t1 = new Thread(new Runnable() {
                @Override
                public void run() {
                    waiting_friends = server.waiting_friend(Integer.valueOf(profile.get(SessionManager.KEY_ID))); // amis en attente
                }});

            t1.start(); // spawn thread
            try{
                t1.join();

                if (waiting_friends == null)
                    waiting_friends = new ArrayList<>();
                if (users == null)
                    users = new ArrayList<>();
                if (users != null){
                    friends = new ArrayList<>();
                    tampons = new Utilisateur[users.size() + waiting_friends.size()];
                    int i = 0;
                    for (i = 0; i < users.size(); i++) {
                        tampons[i] = users.get(i);
                        friends.add(users.get(i).getPseudo());
                    }
                    for (int j = 0; j < waiting_friends.size(); j++) {
                        tampons[i+j] = waiting_friends.get(j);
                    }

                    for (i = 0; i < tampons.length; i++)
                        Log.i("friend", tampons[i].toString());
                    CustomList adapter = new CustomList(getActivity(), tampons, Add_Friend.this);
                    lvfriends.setAdapter(adapter);
                }
            }catch (InterruptedException e) {
                e.printStackTrace();
            }
        }catch (InterruptedException e) {
            e.printStackTrace();
        }

        btnLaunch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String cle = edtFindspot.getText().toString();
                View view = getActivity().getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
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
                        if (users != null) {
                            String[] items = new String[users.size()];
                            for (int i = 0; i < users.size(); i++)
                                items[i] = users.get(i).getPseudo();
                            Log.i("test", items.toString());
                            CustomList_Search adapter = new CustomList_Search(getActivity(), items, cle, Add_Friend.this);
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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int index = info.position;
        switch (item.getItemId()) {
            case R.id.sendrequest:
                sendrequest(index);
                return true;
            case R.id.viewprofile:
                mListener.onLoadFriend(users.get(index));
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void sendrequest(final int index){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                response = server.send_friend_request(Integer.valueOf(profile.get(SessionManager.KEY_ID)), users.get(index).getId());
            }
        });

        t.start(); // spawn thread
        try {
            t.join();
            if (response > 0) {
                Toast.makeText(getActivity(), "your request has been sent", Toast.LENGTH_LONG).show();
            }else if (response == -5){
                Toast.makeText(getActivity(), "Request already sent! wait for user confirmation", Toast.LENGTH_LONG).show();
            }else
                Log.e("server", "problem when sending request");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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


    @Override
    public void confirm_request(final int position) {
        Log.i("testons", Integer.valueOf(profile.get(SessionManager.KEY_ID)) + " " + tampons[position].getId() + " " +tampons[position].getFriendship_id() );
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                response = server.confirmer_friendship(Integer.valueOf(profile.get(SessionManager.KEY_ID)), tampons[position].getId(), tampons[position].getFriendship_id());
            }
        });

        t.start(); // spawn thread
        try {
            t.join();
            if (response > 0) {
                Toast.makeText(getActivity(), "Confirmation succeed", Toast.LENGTH_LONG).show();
            }else if (response == -5){
                Toast.makeText(getActivity(), "Failed to confirm", Toast.LENGTH_LONG).show();
            }else
                Log.e("server", "problem when sending request");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void send_request(final int position) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                response = server.send_friend_request(Integer.valueOf(profile.get(SessionManager.KEY_ID)), tampons[position].getId());
            }
        });

        t.start(); // spawn thread
        try {
            t.join();
            if (response > 0) {
                Toast.makeText(getActivity(), "your request has been sent", Toast.LENGTH_LONG).show();
            }else if (response == -5){
                Toast.makeText(getActivity(), "Request already sent! wait for user confirmation", Toast.LENGTH_LONG).show();
            }else
                Log.e("server", "problem when sending request");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public class CustomList extends ArrayAdapter<Utilisateur> {

        private final Activity context;
        private final Utilisateur[] utilisateurs;
        private FriendCallback mAdapterCallback;

        public CustomList(Activity context, Utilisateur[] utilisateurs, Fragment fg) {
            super(context, R.layout.item_friend, utilisateurs);
            this.context = context;
            this.utilisateurs = utilisateurs;
            for (int i = 0; i < utilisateurs.length; i++)
                Log.i("friend", utilisateurs[i].toString());

            try {
                this.mAdapterCallback = ((FriendCallback) fg);
            } catch (ClassCastException e) {
                throw new ClassCastException("Activity must implement AdapterCallback.");
            }
        }
        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView= inflater.inflate(R.layout.item_friend, null, true);

            final ImageView imgProfile = (ImageView) rowView.findViewById(R.id.item_profile);
            TextView txtPseudo = (TextView) rowView.findViewById(R.id.txtPseudo_friend);
            TextView txtSpot = (TextView) rowView.findViewById(R.id.txtSpots_friend);
            TextView txtFriend = (TextView) rowView.findViewById(R.id.txtFriends_friend);
            final ImageView imgaction = (ImageView) rowView.findViewById(R.id.imgaction);

            imgaction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imgaction.setBackground(null);
                    mAdapterCallback.confirm_request(position);
                }
            });

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

            if (friends != null){
                if (!friends.contains(utilisateurs[position].getPseudo()))
                    imgaction.setBackground(getResources().getDrawable(R.drawable.plus));
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
        private FriendCallback mAdapterCallback;

        public CustomList_Search(Activity context, String[] items, String cle, Fragment fg) {
            super(context, R.layout.item_search, items);
            this.context = context;
            this.items = items;
            this.cle = cle;

            try {
                this.mAdapterCallback = ((FriendCallback) fg);
            } catch (ClassCastException e) {
                throw new ClassCastException("Activity must implement AdapterCallback.");
            }
        }
        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView= inflater.inflate(R.layout.item_search, null, true);

            final ImageView imgaction = (ImageView) rowView.findViewById(R.id.imgaction);
            TextView txtPseudo = (TextView) rowView.findViewById(R.id.txtPseudo_friend);
            TextView txtcle = (TextView) rowView.findViewById(R.id.txtcle);

            imgaction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!friends.contains(items[position]))
                        mAdapterCallback.send_request(position);
                    else
                        Toast.makeText(getActivity(), "You are already friend", Toast.LENGTH_SHORT).show();
                }
            });

            txtPseudo.setText(items[position]);
            txtcle.setText(cle);
            if (friends != null){
                if (friends.contains(items[position]))
                    imgaction.setBackground(getResources().getDrawable(R.drawable.group_button));
                else
                    imgaction.setBackground(getResources().getDrawable(R.drawable.plus));
            }
            else
                imgaction.setBackground(getResources().getDrawable(R.drawable.plus));

            return rowView;
        }
    }
}

interface FriendCallback{
    public void confirm_request(int position);
    public void send_request(int position);
}