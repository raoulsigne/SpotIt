package techafrkix.work.com.spot.spotit;

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
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;

import java.io.File;

import techafrkix.work.com.spot.bd.Utilisateur;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.AWS_Tools;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.DBServer;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.SessionManager;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Account_Friend.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Account_Friend#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Account_Friend extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private  Utilisateur friend;

    private OnFragmentInteractionListener mListener;

    public Account_Friend() {
        // Required empty public constructor
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account__friend, container, false);
        final ImageView item_profile = (ImageView) view.findViewById(R.id.imgprofile_friend);
        ImageView play_spot = (ImageView) view.findViewById(R.id.imgNbSpots);
        TextView txtPseudo = (TextView) view.findViewById(R.id.txtPseudo_friend);
        TextView txtSpot = (TextView) view.findViewById(R.id.txtSpots_friend);
        TextView txtNbSpot = (TextView) view.findViewById(R.id.txtNbSpot);
        TextView txtNbFriend = (TextView) view.findViewById(R.id.txtFriends_friend);

        if (friend != null){
            txtPseudo.setText(friend.getPseudo());
            txtSpot.setText(friend.getNbspot() + " spots | " + friend.getNbrespot() + " respots");
            txtNbSpot.setText(friend.getNbspot() + " Spots");
            txtNbFriend.setText(friend.getNbfriends() + " friends");
        }

        play_spot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onListSpot_Friend(friend);
            }
        });

        if (friend.getPhoto() != "") {
            String dossier = getActivity().getApplicationContext().getFilesDir().getPath() + DBServer.DOSSIER_IMAGE;
            final File file = new File(dossier + File.separator + friend.getPhoto() + ".jpg");

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
        void onListSpot_Friend(Utilisateur friend);
    }
}
