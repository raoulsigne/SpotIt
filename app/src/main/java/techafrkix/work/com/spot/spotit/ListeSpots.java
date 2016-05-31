package techafrkix.work.com.spot.spotit;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import techafrkix.work.com.spot.bd.Spot;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.AWS_Tools;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.DBServer;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.SessionManager;

public class ListeSpots extends Fragment {

    private ArrayList<Spot> spots;
    private HashMap<String, Bitmap> spotsimages;
    private SpotAdapter adapter;
    private ListView listView;

    private SessionManager session;
    private HashMap<String, String> profile;
    private DBServer server;

    ProgressDialog pDialog;
    private ArrayList<Spot> tampon;
    private int offset;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_liste_spots, container, false);
        listView = (ListView) view.findViewById(R.id.listView);

        spots = new ArrayList<>();
        offset = 0;

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

        spotsimages = new HashMap<String, Bitmap>();
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

        return view;
    }

    private void loadSpots() {
        // Showing progress dialog before sending http request
        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage("Please wait..");
        pDialog.setIndeterminate(true);
        pDialog.setCancelable(false);
        pDialog.show();

        profile = session.getUserDetails();
        tampon = new ArrayList<>();

        Log.i("offset", offset+"");
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                tampon = server.find_spot_user(Integer.valueOf(profile.get(SessionManager.KEY_ID)), offset, offset + 10);
                for (Spot s :
                        tampon) {
                    spots.add(s);
                }
                //set the value of the offset that will be use next time
                offset += 10; //session.putOffset(offset + 10);
                Log.i("test", spots.toString());
            }});

        t.start(); // spawn thread
        try {
            t.join();
            Log.i("spots", spots.toString());
            String dossier = getActivity().getApplicationContext().getFilesDir().getPath()+DBServer.DOSSIER_IMAGE;
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

                for (final Spot s : spots) {
                    final File file = new File(dossier+ File.separator  + s.getPhotokey() + ".jpg");
                    if (file.exists()) {
                        Log.i("file", "file exists " + dossier + s.getPhotokey() + ".jpg");
                        spotsimages.put(s.getPhotokey(), BitmapFactory.decodeFile(file.getAbsolutePath()));
                        barProgressDialog.setProgress(barProgressDialog.getProgress() + 1);
                        if (barProgressDialog.getProgress() == spots.size()) {
                            barProgressDialog.dismiss();
                            // Create the adapter to convert the array to views
                            adapter = new SpotAdapter(getActivity(), spots, spotsimages);
                            // Attach the adapter to a ListView
                            listView.setAdapter(adapter);
                        }
                    } else {
                        Log.i("file", "file doesn't exists");

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
                                rapport /= bytesTotal;
                                if (rapport == 100) {
                                    barProgressDialog.setProgress(barProgressDialog.getProgress() + 1);
                                    spotsimages.put(s.getPhotokey(), BitmapFactory.decodeFile(file.getAbsolutePath()));
                                }
                                if (barProgressDialog.getProgress() == spots.size()) {
                                    barProgressDialog.dismiss();
                                    // get listview current position - used to maintain scroll position
                                    int currentPosition = listView.getFirstVisiblePosition();

                                    // Create the adapter to convert the array to views
                                    adapter = new SpotAdapter(getActivity(), spots, spotsimages);
                                    // Attach the adapter to a ListView
                                    listView.setAdapter(adapter);

                                    // Setting new scroll position
                                    listView.setSelectionFromTop(currentPosition + 1, 0);
                                }
                            }

                            @Override
                            public void onError(int id, Exception ex) {
                                // do something
                                barProgressDialog.dismiss();
                            }

                        });
                    }
                }
            }
        }catch (InterruptedException e) {
            e.printStackTrace();
        }

        // closing progress dialog
        pDialog.dismiss();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        final FragmentManager fragManager = this.getFragmentManager();
        final Fragment fragment = fragManager.findFragmentById(R.id.vg_list_spot);
        if(fragment!=null){
            fragManager.beginTransaction().remove(fragment).commit();
        }
    }
}
