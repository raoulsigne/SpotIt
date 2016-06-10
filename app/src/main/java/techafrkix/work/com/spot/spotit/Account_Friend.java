package techafrkix.work.com.spot.spotit;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import techafrkix.work.com.spot.bd.Utilisateur;


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
        ImageView item_profile = (ImageView) view.findViewById(R.id.imgprofile_friend);
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
