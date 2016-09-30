package techafrkix.work.com.spot.spotit;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import techafrkix.work.com.spot.bd.Spot;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.AWS_Tools;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.DBServer;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.SessionManager;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ListRespots.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ListRespots#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListRespots extends Fragment implements SpotAdapter.AdapterCallback {
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
    private int count;
    //    private HashMap<String, Bitmap> spotsimages;
    private SpotAdapter adapter;
    private ListView listView;

    private SessionManager session;
    private HashMap<String, String> profile;
    private DBServer server;
    private int retour, type;

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

        // Session class instance
        session = new SessionManager(getActivity());
        profile = new HashMap<>();
        server = new DBServer(getActivity());
        profile = session.getUserDetails();
        type = 1;

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            spots = (ArrayList<Spot>) getArguments().getSerializable("spots");
            type = getArguments().getInt("type");
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

    private void loadspots() {
        final File folder = new File(DBServer.DOSSIER_IMAGE);
        if (!folder.exists())
            folder.mkdirs();

        Log.i("test", spots.toString());

        final ProgressDialog dialog = ProgressDialog.show(getActivity(), "",
                "Loading. Please wait...", true);
        dialog.show();
        count = 0;

        for (int i = 0; i < spots.size(); i++) {
            s = new Spot();
            s = spots.get(i);
            final File file = new File(DBServer.DOSSIER_IMAGE + File.separator + s.getPhotokey() + ".jpg");
            if (file.exists()) {
                count++;
                if (count == spots.size()) {
                    // Create the adapter to convert the array to views
                    adapter = new SpotAdapter(getActivity(), spots, ListRespots.this, type);
                    // Attach the adapter to a ListView
                    listView.setAdapter(adapter);
                    dialog.dismiss();
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
                                count++;
                            }
                        } else {
                            count++;
                            spots.remove(s);
                        }
                        if (count == spots.size()) {
                            // Create the adapter to convert the array to views
                            adapter = new SpotAdapter(getActivity(), spots, ListRespots.this, type);
                            // Attach the adapter to a ListView
                            listView.setAdapter(adapter);
                            dialog.dismiss();
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
        void onDetailSpot(ArrayList<Spot> spots, Spot spot);
        void onDetailSpot_user(ArrayList<Spot> spots, Spot spot);
    }

    @Override
    public void detail(int position) {
        if (type == 1) {
            mListener.onDetailSpot_user(spots, spots.get(position));
        } else {
            mListener.onDetailSpot(spots, spots.get(position));
        }
    }

    @Override
    public void share(int position) {
        Uri uriToImage;
        uriToImage = ListeSpots.getImageContentUri(getActivity(), new File(DBServer.DOSSIER_IMAGE + File.separator + spots.get(position).getPhotokey() + ".jpg"));

        List<Intent> targetedShareIntents = new ArrayList<Intent>();

        Intent facebookIntent = getShareIntent("facebook", "spot it", uriToImage);
        if (facebookIntent != null)
            targetedShareIntents.add(facebookIntent);

        Intent twitterIntent = getShareIntent("twitter", "spot it", uriToImage);
        if (twitterIntent != null)
            targetedShareIntents.add(twitterIntent);

        Intent instagramIntent = getShareIntent("instagram", "spot it", uriToImage);
        if (instagramIntent != null)
            targetedShareIntents.add(instagramIntent);

        Intent whatsappIntent = getShareIntent("whatsapp", "spot it", uriToImage);
        if (instagramIntent != null)
            targetedShareIntents.add(whatsappIntent);

        Intent gmailIntent = getShareIntent("gmail", "spot it", uriToImage);
        if (instagramIntent != null)
            targetedShareIntents.add(gmailIntent);

        Intent chooser = Intent.createChooser(targetedShareIntents.remove(0), getResources().getText(R.string.send_to));

        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(new Parcelable[]{}));

        startActivity(chooser);
    }

    @Override
    public void letsgo(int position) {
        String uri = "";
        uri = String.format(Locale.ENGLISH, "google.navigation:q=%f,%f&mode=d,w,b", Double.valueOf(spots.get(position).getLatitude()),
                Double.valueOf(spots.get(position).getLongitude()));

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        getActivity().startActivity(intent);
    }

    @Override
    public void delete(final int position) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                retour = server.delete_spot(spots.get(position).getId());

            }
        });

        t.start(); // spawn thread
        try {
            t.join();
            if (retour == 1) {
                Toast.makeText(getActivity(), "Spot deleted", Toast.LENGTH_SHORT).show();
                if (spots.get(position).getRespot() == 1)
                    session.decrement_nbrespot();
                else
                    session.decrement_nbspot();
                adapter.remove(spots.get(position));

            } else {
                Toast.makeText(getActivity(), "unable to delete spot", Toast.LENGTH_SHORT).show();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Intent getShareIntent(String type, String subject, Uri uri) {
        boolean found = false;
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("image/*");

        // gets the list of intents that can be loaded.
        List<ResolveInfo> resInfo = getActivity().getPackageManager().queryIntentActivities(share, 0);
        System.out.println("resinfo: " + resInfo);
        if (!resInfo.isEmpty()) {
            for (ResolveInfo info : resInfo) {
                if (info.activityInfo.packageName.toLowerCase().contains(type) ||
                        info.activityInfo.name.toLowerCase().contains(type)) {
                    share.putExtra(Intent.EXTRA_SUBJECT, subject);
                    share.putExtra(Intent.EXTRA_STREAM, uri);
                    share.setPackage(info.activityInfo.packageName);
                    found = true;
                    break;
                }
            }
            if (!found)
                return null;

            return share;
        }
        return null;
    }
}
