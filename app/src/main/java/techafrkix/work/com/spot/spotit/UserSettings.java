package techafrkix.work.com.spot.spotit;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.HashMap;

import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.DBServer;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.SessionManager;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UserSettings.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UserSettings#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserSettings extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private TextView txtlogout, txtprivacy, txtconfidentiality, txtpublicity, txtchangepassword;
    private ImageView leftarrow;
    private ScrollView content;

    private SessionManager session;
    private HashMap<String, String> profile;

    public UserSettings() {
        // Required empty publics constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UserSettings.
     */
    // TODO: Rename and change types and number of parameters
    public static UserSettings newInstance(String param1, String param2) {
        UserSettings fragment = new UserSettings();
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

        session = new SessionManager(getActivity());
        profile = new HashMap<>();
        profile = session.getUserDetails();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        txtlogout = (TextView)view.findViewById(R.id.txtDeconnexion);
        leftarrow = (ImageView)view.findViewById(R.id.leftarrow);
        txtpublicity = (TextView)view.findViewById(R.id.txtPublicity);
        txtconfidentiality = (TextView)view.findViewById(R.id.txtConfidentiality);
        txtprivacy = (TextView)view.findViewById(R.id.txtPrivacy);
        txtchangepassword=(TextView)view.findViewById(R.id.txtChangePassword);

        content = (ScrollView)view.findViewById(R.id.scrollcontent);

        txtlogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onDisconnect();
            }
        });

        leftarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onLoadAccount();
            }
        });

        txtpublicity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onLoadInformation(0);
            }
        });

        txtconfidentiality.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onLoadInformation(1);
            }
        });

        txtprivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onLoadInformation(2);
            }
        });

        txtchangepassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onChangePassword();
            }
        });

        if (Integer.valueOf(profile.get(SessionManager.KEY_TYPE_CONNEXION_ID)) != DBServer.CONNEXION_NORMAL){
            txtchangepassword.setEnabled(false);
            txtchangepassword.setTextColor(getActivity().getResources().getColor(R.color.titre_menu));
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
        void onDisconnect();
        void onLoadAccount();
        void onLoadInformation(int i);
        void onChangePassword();
    }
}