package techafrkix.work.com.spot.spotit;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import techafrkix.work.com.spot.bd.Spot;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.DBServer;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.GeoHash;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Search.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Search#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Search extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public static final String V_MOI = "moi";
    public static final String V_FRIEND = "amis";
    public static final String V_PUBLIC = "publics";

    private String visibilite1, visibilite2, visibilite3;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private TextView txtMoi, txtAmis, txtPublic;
    private ImageButton vMoi, vFriend, vPublic;
    private AutoCompleteTextView textView;
    private EditText edttag;
    private Button btnSearch;

    private ArrayList<String> listes;
    private DBServer server;
    private LatLng coordones;
    private GeoHash geoHash;
    private ArrayList<Spot> spots;

    public Search() {
        // Required empty publics constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Search.
     */
    // TODO: Rename and change types and number of parameters
    public static Search newInstance(String param1, String param2) {
        Search fragment = new Search();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        server = new DBServer(getActivity());
        listes = new ArrayList<>();
        spots = new ArrayList<>();
        coordones = new LatLng(0,0);
        geoHash = new GeoHash();

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        vMoi = (ImageButton) view.findViewById(R.id.visibiliteMoi);
        vFriend = (ImageButton) view.findViewById(R.id.visibiliteFriend);
        vPublic = (ImageButton) view.findViewById(R.id.visibilitePublic);
        txtMoi = (TextView) view.findViewById(R.id.txtMoi);
        txtAmis = (TextView) view.findViewById(R.id.txtAmis);
        txtPublic = (TextView) view.findViewById(R.id.txtPublic);
        textView = (AutoCompleteTextView) view.findViewById(R.id.autocomplete_city);
        edttag = (EditText) view.findViewById(R.id.edtTag);
        btnSearch = (Button) view.findViewById(R.id.btnSearch);

        visibilite1 = "";
        visibilite2 = "";
        visibilite3 = "";

        vMoi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (visibilite1.equals(V_MOI)){
                    visibilite1 = "";
                    vMoi.setBackgroundDrawable(getResources().getDrawable(R.drawable.moi_));
                    txtMoi.setTextColor(getResources().getColor(R.color.titre_menu));
                }else {
                    visibilite1 = V_MOI;
                    vMoi.setBackgroundDrawable(getResources().getDrawable(R.drawable.moi_selected));
                    txtMoi.setTextColor(getResources().getColor(R.color.myblue));
                }
            }
        });
        vFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (visibilite1.equals(V_FRIEND)){
                    visibilite1 = "";
                    vFriend.setBackgroundDrawable(getResources().getDrawable(R.drawable.friendsaccount));
                    txtAmis.setTextColor(getResources().getColor(R.color.titre_menu));
                }else {
                    visibilite1 = V_FRIEND;
                    vFriend.setBackgroundDrawable(getResources().getDrawable(R.drawable.friends_clicked));
                    txtAmis.setTextColor(getResources().getColor(R.color.myblue));
                }
            }
        });
        vPublic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (visibilite3.equals(V_PUBLIC)){
                    visibilite3 = "";
                    vPublic.setBackgroundDrawable(getResources().getDrawable(R.drawable.publics));
                    txtPublic.setTextColor(getResources().getColor(R.color.titre_menu));
                }else {
                    visibilite3 = V_PUBLIC;
                    vPublic.setBackgroundDrawable(getResources().getDrawable(R.drawable.public_clicked));
                    txtPublic.setTextColor(getResources().getColor(R.color.myblue));
                }
            }
        });

        textView.addTextChangedListener(tw);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(edttag.getText().toString()) || TextUtils.isEmpty(textView.getText().toString()))
                    Toast.makeText(getActivity(), "Renseigner le tag et le lieu", Toast.LENGTH_SHORT).show();
                else {
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            coordones = server.get_coordinates(textView.getText().toString());
                        }
                    });
                    t.start(); // spawn thread
                    try {
                        t.join();
                        geoHash.setLatitude(coordones.latitude);
                        geoHash.setLongitude(coordones.longitude);
                        final String[] hashs = geoHash.neighbours_1(geoHash.encoder());
                        final String[] tags = edttag.getText().toString().split(";");
                        final int[] visibilities = new int[3];
                        int n = 0;
                        if (!visibilite1.isEmpty()) visibilities[n++] = getvisibiliteId(visibilite1);
                        if (!visibilite2.isEmpty()) visibilities[n++] = getvisibiliteId(visibilite2);
                        if (!visibilite3.isEmpty()) visibilities[n++] = getvisibiliteId(visibilite3);


                        Thread t2 = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                spots = server.find_spot_tag_hash_visibility(tags, hashs, visibilities, 0, 0);
                            }
                        });
                        t2.start(); // spawn thread
                        try {
                            t2.join();
                            if (spots != null){
                                mListener.onLoadSpot(spots);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
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
        void onLoadSpot(ArrayList<Spot> spots);
    }

    private TextWatcher tw = new TextWatcher() {
        public void afterTextChanged(Editable s){
        }
        public void  beforeTextChanged(CharSequence s, int start, int count, int after){
            // you can check for enter key here
        }
        public void  onTextChanged (final CharSequence s, int start, int before, int count) {
            listes = new ArrayList<>();
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    listes = server.google_autocompletion(s.toString());
                }});

            t.start(); // spawn thread
            try{
                t.join();
                if (listes != null) {
                    Log.i("auto complete", listes.toString());
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                            android.R.layout.simple_list_item_1, listes);
                    textView.setAdapter(adapter);
                } else {
                    Log.i("auto complete", "liste null");
                }
            }catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    public int getvisibiliteId(String visibilite){
        if (visibilite == DetailSpot_New.V_MOI)
            return 21;
        else if (visibilite == DetailSpot_New.V_FRIEND)
            return 11;
        else
            return 1;
    }
}
