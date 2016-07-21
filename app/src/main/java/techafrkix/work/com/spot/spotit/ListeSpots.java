package techafrkix.work.com.spot.spotit;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
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
import java.util.Locale;

import techafrkix.work.com.spot.bd.Spot;
import techafrkix.work.com.spot.bd.Utilisateur;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.AWS_Tools;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.DBServer;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.GeoHash;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.SessionManager;

public class ListeSpots extends Fragment implements SpotAdapter.AdapterCallback {

    public static final int PORTION_TELECHARGEABLE = 10;
    private OnFragmentInteractionListener mListener;

    private ArrayList<Spot> spots;
    private Spot s;
//    private HashMap<String, Bitmap> spotsimages;
    private SpotAdapter adapter;
    private ListView listView;

    private SessionManager session;
    private HashMap<String, String> profile;
    private DBServer server;

    ProgressDialog pDialog;
    private ArrayList<Spot> tampon;
    private int offset;

    private int preLast;
    private int type;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        spots = new ArrayList<>();

        if (getArguments() != null) {
            type = (int) getArguments().getInt("type");
            Log.i("teste", type + " ");
            if (type == 0) {
                spots = (ArrayList<Spot>) getArguments().getSerializable("spots");
                Log.i("teste", "test " + spots.toString());
            }
        }
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_liste_spots, container, false);
        listView = (ListView) view.findViewById(R.id.listView);

        offset = 0;
        preLast = 0;

        // Creating a button - Load More
        Button btnLoadMore = new Button(getActivity());
        btnLoadMore.setText("Load More");
        btnLoadMore.setBackground(getResources().getDrawable(R.drawable.button_blue));

        // Adding button to listview at footer
        listView.addFooterView(btnLoadMore);

        // Session class instance
        session = new SessionManager(getActivity());
        profile = new HashMap<>();
        server = new DBServer(getActivity());

//        spotsimages = new HashMap<String, Bitmap>();
        tampon = new ArrayList<>();
        loadSpots();

        /**
         * Listening to Load More button click event
         * */
        btnLoadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // Starting a new async task
                loadSpots();
            }
        });

        /**
         * Handle when reaching the end of the list
         */
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                final int lastItem = firstVisibleItem + visibleItemCount;
                if (lastItem == totalItemCount) {
                    if (preLast != lastItem) { //to avoid multiple calls for last item
                        Log.i("Last", "Last");
                        preLast = lastItem;
                    }
                }
            }
        });

        return view;
    }

    private void loadSpots() {
        profile = session.getUserDetails();

        if (type == 1) {
            tampon = new ArrayList<>();
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    tampon = server.find_spot_user(Integer.valueOf(profile.get(SessionManager.KEY_ID)), offset, offset + PORTION_TELECHARGEABLE);
                }
            });

            t.start(); // spawn thread
            try {
                t.join();
                if (tampon != null) {
                    if (tampon.size() > 0) {
                        for (Spot s : tampon) {
                            spots.add(s);
                        }
                        //set the value of the offset that will be use next time
                        offset += PORTION_TELECHARGEABLE; //session.putOffset(offset + PORTION_TELECHARGEABLE);
                        String dossier = getActivity().getApplicationContext().getFilesDir().getPath() + DBServer.DOSSIER_IMAGE;
                        File folder = new File(dossier);
                        if (!folder.exists())
                            folder.mkdirs();

                        if (spots != null & spots.size() > 0) {
                            final ProgressDialog barProgressDialog = new ProgressDialog(getActivity());
                            barProgressDialog.setTitle("Telechargement des spots ...");
                            barProgressDialog.setMessage("Opération en progression ...");
                            barProgressDialog.setProgressStyle(barProgressDialog.STYLE_HORIZONTAL);
                            barProgressDialog.setProgress(0);
                            barProgressDialog.setMax(spots.size());
                            barProgressDialog.show();
                            // get listview current position - used to maintain scroll position
                            final int currentPosition = offset - PORTION_TELECHARGEABLE;

                            for (final Spot s : spots) {
                                final File file = new File(dossier + File.separator + s.getPhotokey() + ".jpg");
                                if (file.exists()) {
//                                    spotsimages.put(s.getPhotokey(), BitmapFactory.decodeFile(file.getAbsolutePath()));
                                    barProgressDialog.setProgress(barProgressDialog.getProgress() + 1);
                                    if (barProgressDialog.getProgress() == spots.size()) {
                                        barProgressDialog.dismiss();
                                        // Create the adapter to convert the array to views
                                        adapter = new SpotAdapter(getActivity(), spots, ListeSpots.this);
                                        // Attach the adapter to a ListView
                                        listView.setAdapter(adapter);
                                        // Setting new scroll position
                                        listView.setSelectionFromTop(currentPosition, 0);
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
//                                                    spotsimages.put(s.getPhotokey(), BitmapFactory.decodeFile(file.getAbsolutePath()));
                                                }
                                            }else{
                                                barProgressDialog.setProgress(barProgressDialog.getProgress() + 1);
                                            }
                                            if (barProgressDialog.getProgress() == spots.size()) {
                                                barProgressDialog.dismiss();
                                                // Create the adapter to convert the array to views
                                                adapter = new SpotAdapter(getActivity(), spots, ListeSpots.this);
                                                // Attach the adapter to a ListView
                                                listView.setAdapter(adapter);
                                                // Setting new scroll position
                                                listView.setSelectionFromTop(currentPosition, 0);
                                            }
                                        }

                                        @Override
                                        public void onError(int id, Exception ex) {
                                            // do something
                                            // barProgressDialog.dismiss();
                                        }

                                    });
                                }
                            }
                        }
                    } else {
                        // Setting new scroll position
                        listView.setSelectionFromTop(0, 0);
                    }
                } else {
                    // Setting new scroll position
                    listView.setSelectionFromTop(0, 0);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            final String dossier = getActivity().getApplicationContext().getFilesDir().getPath() + DBServer.DOSSIER_IMAGE;
            final File folder = new File(dossier);
            if (!folder.exists())
                folder.mkdirs();

            if (offset < spots.size()) {
                final ProgressDialog barProgressDialog = new ProgressDialog(getActivity());
                barProgressDialog.setTitle("Telechargement des spots ...");
                barProgressDialog.setMessage("Opération en progression ...");
                barProgressDialog.setProgressStyle(barProgressDialog.STYLE_HORIZONTAL);
                barProgressDialog.setProgress(0);
                barProgressDialog.show();

                int max = offset + PORTION_TELECHARGEABLE;
                if (max > spots.size())
                    max = spots.size();
                barProgressDialog.setMax(2 * (max - offset));
                // get listview current position - used to maintain scroll position
                final int currentPosition = offset;

                for (int i = offset; i < max; i++) {
                    s = new Spot();
                    s = spots.get(i);
                    tampon.add(s);

                    //photo du spot
                    final File file = new File(dossier + File.separator + s.getPhotokey() + ".jpg");
                    if (file.exists()) {
//                        spotsimages.put(s.getPhotokey(), BitmapFactory.decodeFile(file.getAbsolutePath()));
                        barProgressDialog.setProgress(barProgressDialog.getProgress() + 1);

                        if (barProgressDialog.getProgress() == barProgressDialog.getMax()) {
                            barProgressDialog.dismiss();
                            // Create the adapter to convert the array to views
                            adapter = new SpotAdapter(getActivity(), tampon, ListeSpots.this);
                            // Attach the adapter to a ListView
                            listView.setAdapter(adapter);
                            // Setting new scroll position
                            listView.setSelectionFromTop(currentPosition, 0);
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
//                                        spotsimages.put(s.getPhotokey(), BitmapFactory.decodeFile(file.getAbsolutePath()));
                                    }
                                }else{
                                    barProgressDialog.setProgress(barProgressDialog.getProgress() + 1);
                                    tampon.remove(s);
                                }
                                if (barProgressDialog.getProgress() == barProgressDialog.getMax()) {
                                    barProgressDialog.dismiss();

                                    // Create the adapter to convert the array to views
                                    adapter = new SpotAdapter(getActivity(), tampon, ListeSpots.this);
                                    // Attach the adapter to a ListView
                                    listView.setAdapter(adapter);
                                    // Setting new scroll position
                                    listView.setSelectionFromTop(currentPosition, 0);
                                }
                            }

                            @Override
                            public void onError(int id, Exception ex) {
                                // do something
//                                barProgressDialog.setProgress(barProgressDialog.getProgress() + 1);
//                                spotsimages.put(s.getPhotokey(), null);
                            }

                        });
                    }


                    //photo de profile du spoteur
                    final File file1 = new File(dossier + File.separator + s.getPhotouser() + ".jpg");
                    if (!file1.exists()) {
                        AWS_Tools aws_tools = new AWS_Tools(MainActivity.getAppContext());
                        int transfertId = aws_tools.download(file1, s.getPhotouser());
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
//                                        spotsimages.put(s.getPhotokey(), BitmapFactory.decodeFile(file.getAbsolutePath()));
                                    }
                                }else{
                                    barProgressDialog.setProgress(barProgressDialog.getProgress() + 1);
                                }
                                if (barProgressDialog.getProgress() == barProgressDialog.getMax()) {
                                    barProgressDialog.dismiss();

                                    // Create the adapter to convert the array to views
                                    adapter = new SpotAdapter(getActivity(), tampon, ListeSpots.this);
                                    // Attach the adapter to a ListView
                                    listView.setAdapter(adapter);
                                    // Setting new scroll position
                                    listView.setSelectionFromTop(currentPosition, 0);
                                }
                            }

                            @Override
                            public void onError(int id, Exception ex) {
                                // do something
                            }

                        });
                    }
                    else {
                        barProgressDialog.setProgress(barProgressDialog.getProgress() + 1);
                        if (barProgressDialog.getProgress() == barProgressDialog.getMax()) {
                            barProgressDialog.dismiss();
                            // Create the adapter to convert the array to views
                            adapter = new SpotAdapter(getActivity(), tampon, ListeSpots.this);
                            // Attach the adapter to a ListView
                            listView.setAdapter(adapter);
                            // Setting new scroll position
                            listView.setSelectionFromTop(currentPosition, 0);
                        }
                    }
                }
            }
            offset += PORTION_TELECHARGEABLE;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        final FragmentManager fragManager = this.getFragmentManager();
        final Fragment fragment = fragManager.findFragmentById(R.id.vg_list_spot);
        if (fragment != null) {
            fragManager.beginTransaction().remove(fragment).commit();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void detail(int position) {
        if (type == 1) {
            mListener.onDetailSpot(spots.get(position));
        }else {
            mListener.onDetailSpot(tampon.get(position));
        }
    }

    @Override
    public void share(int position) {
        Toast.makeText(getActivity(), "Share " + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void letsgo(int position) {

        String uri = "";
        if (type == 1) {
            uri = String.format(Locale.ENGLISH, "google.navigation:q=%f,%f&mode=d,w,b", Double.valueOf(spots.get(position).getLatitude()),
                    Double.valueOf(spots.get(position).getLongitude()));
        }else {
            uri = String.format(Locale.ENGLISH, "google.navigation:q=%f,%f&mode=d,w,b", Double.valueOf(tampon.get(position).getLatitude()),
                    Double.valueOf(tampon.get(position).getLongitude()));
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        getActivity().startActivity(intent);

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
        void onDetailSpot(Spot spot);
        void onLetsGo();
    }
}
