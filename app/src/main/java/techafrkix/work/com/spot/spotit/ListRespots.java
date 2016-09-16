package techafrkix.work.com.spot.spotit;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;

import java.io.File;
import java.util.ArrayList;

import techafrkix.work.com.spot.bd.Spot;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.AWS_Tools;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.DBServer;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ListRespots.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ListRespots#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListRespots extends Fragment implements SpotAdapter.AdapterCallback{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private ArrayList<Spot> spots;
    private Spot s;
    //    private HashMap<String, Bitmap> spotsimages;
    private SpotAdapter adapter;
    private ListView listView;

    public ListRespots() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ListRespots.
     */
    // TODO: Rename and change types and number of parameters
    public static ListRespots newInstance(String param1, String param2) {
        ListRespots fragment = new ListRespots();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        spots = new ArrayList<>();

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            spots = (ArrayList<Spot>) getArguments().getSerializable("spots");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_list_respots, container, false);

        listView = (ListView) view.findViewById(R.id.listView);
        loadspots();

        return view;
    }

    private void loadspots(){
        final File folder = new File(DBServer.DOSSIER_IMAGE);
        if (!folder.exists())
            folder.mkdirs();

        Log.i("test", spots.toString());

        final ProgressDialog barProgressDialog = new ProgressDialog(getActivity());
        barProgressDialog.setTitle("Telechargement des spots ...");
        barProgressDialog.setMessage("Opération en progression ...");
        barProgressDialog.setProgressStyle(barProgressDialog.STYLE_HORIZONTAL);
        barProgressDialog.setProgress(0);
        barProgressDialog.setMax(spots.size());
        barProgressDialog.show();

        for (int i = 0; i < spots.size(); i++) {
            s = new Spot();
            s = spots.get(i);
            final File file = new File(DBServer.DOSSIER_IMAGE + File.separator + s.getPhotokey() + ".jpg");
            if (file.exists()) {
                barProgressDialog.setProgress(barProgressDialog.getProgress() + 1);

                if (barProgressDialog.getProgress() == barProgressDialog.getMax()) {
                    barProgressDialog.dismiss();
                    // Create the adapter to convert the array to views
                    adapter = new SpotAdapter(getActivity(), spots, ListRespots.this, 1);
                    // Attach the adapter to a ListView
                    listView.setAdapter(adapter);
                }
            } else {
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
                        if (bytesTotal != 0) {
                            rapport /= bytesTotal;
                            if (rapport == 100) {
                                barProgressDialog.setProgress(barProgressDialog.getProgress() + 1);
                            }
                        } else {
                            barProgressDialog.setProgress(barProgressDialog.getProgress() + 1);
                            spots.remove(s);
                        }
                        if (barProgressDialog.getProgress() == barProgressDialog.getMax()) {
                            barProgressDialog.dismiss();

                            // Create the adapter to convert the array to views
                            adapter = new SpotAdapter(getActivity(), spots, ListRespots.this, 1);
                            // Attach the adapter to a ListView
                            listView.setAdapter(adapter);
                        }
                    }

                    @Override
                    public void onError(int id, Exception ex) {
                        // do something
//                                barProgressDialog.setProgress(barProgressDialog.getProgress() + 1);
                    }

                });
            }
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
    }

    @Override
    public void detail(int position) {

    }

    @Override
    public void share(int position) {

    }

    @Override
    public void letsgo(int position) {

    }

    @Override
    public void delete(int position) {

    }
}
