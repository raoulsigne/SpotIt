package techafrkix.work.com.spot.spotit;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.mindrot.jbcrypt.BCrypt;

import java.util.HashMap;

import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.DBServer;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.SessionManager;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ChangePassword.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ChangePassword#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChangePassword extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private EditText oldpassword, newpassword1, newpassword2;
    private Button valider;
    private ImageView leftarrow;

    private SessionManager session;
    private HashMap<String, String> profile;
    private DBServer server;

    private int retour;

    public ChangePassword() {
        // Required empty publics constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChangePassword.
     */
    // TODO: Rename and change types and number of parameters
    public static ChangePassword newInstance(String param1, String param2) {
        ChangePassword fragment = new ChangePassword();
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
        View view = inflater.inflate(R.layout.fragment_change_password, container, false);

        // Session class instance
        session = new SessionManager(getActivity());
        profile = new HashMap<>();
        server = new DBServer(getActivity());
        profile = session.getUserDetails();

        oldpassword = (EditText)view.findViewById(R.id.edtold);
        newpassword1 = (EditText)view.findViewById(R.id.edtnew1);
        newpassword2 = (EditText)view.findViewById(R.id.edtnew2);
        valider = (Button) view.findViewById(R.id.btnDone);
        leftarrow = (ImageView)view.findViewById(R.id.leftarrow);

        leftarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onLoadAccount();
            }
        });

        valider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String oldpass = BCrypt.hashpw(oldpassword.getText().toString() + Inscription._TO_CONCAT, BCrypt.gensalt(12)).toString();
                Log.i("repon", oldpass);
                if (oldpass.equals(profile.get(SessionManager.KEY_PASSWORD))) {
                    if (newpassword1.getText().toString().equals(newpassword2.getText().toString())) {
                        Log.i("test", "match");
                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String pass = BCrypt.hashpw(newpassword1.getText().toString() + Inscription._TO_CONCAT, BCrypt.gensalt(12)).toString();
                                retour = server.change_password(Integer.valueOf(profile.get(SessionManager.KEY_ID)),
                                        pass);
                            }
                        });

                        t.start(); // spawn thread
                        try {
                            t.join();
                            if (retour == 1) {
                                Toast.makeText(getActivity(), "succefully change", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity(), "unable to change your password", Toast.LENGTH_SHORT).show();
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else
                        Toast.makeText(getActivity(), "retype new pasword", Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(getActivity(), "the password you entered doesn't match", Toast.LENGTH_SHORT).show();
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
        void onLoadAccount();
    }
}
