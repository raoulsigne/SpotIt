package techafrkix.work.com.spot.spotit;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
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

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_liste_spots, container, false);
        listView = (ListView) view.findViewById(R.id.listView);

        // Session class instance
        session = new SessionManager(getActivity());
        profile = new HashMap<>();
        server = new DBServer(getActivity());

        spotsimages = new HashMap<String, Bitmap>();
        loadSpots();

        return view;
    }

    private void loadSpots() {
        profile = session.getUserDetails();

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                spots = server.find_spot_user(Integer.valueOf(profile.get(SessionManager.KEY_ID)), 0, 10);
            }});

        t.start(); // spawn thread
        try {
            t.join();
            Log.i("spots", spots.toString());

            File folder = new File(getActivity().getFilesDir().getPath()+"/Images/");
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
                    final File file = new File(getActivity().getFilesDir().getPath() + "/Images/" + s.getPhotokey() + ".jpg");
                    if (file.exists()) {
                        Log.i("file", "file exists");
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
                                    // Create the adapter to convert the array to views
                                    adapter = new SpotAdapter(getActivity(), spots, spotsimages);
                                    // Attach the adapter to a ListView
                                    listView.setAdapter(adapter);
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
