package techafrkix.work.com.spot.spotit;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import techafrkix.work.com.spot.bd.Spot;
import techafrkix.work.com.spot.bd.SpotsDBAdapteur;
import techafrkix.work.com.spot.bd.Utilisateur;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.AWS_Tools;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.DBServer;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.SessionManager;

public class Welcome extends AppCompatActivity {

    private static int RAPPORT_PROGRESSION = 0;
    private ProgressBar bar = null;
    private int mProgressStatus = 0;
    private ArrayList<Spot> spots;

    private SessionManager session;
    private HashMap<String, String> profile;
    private DBServer server;
    private int friends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Session class instance
        session = new SessionManager(getApplicationContext());
        profile = new HashMap<>();
        server = new DBServer(getApplicationContext());

        RAPPORT_PROGRESSION = 0;
        bar = (ProgressBar) findViewById(R.id.progressBar);
        bar.getProgressDrawable().setColorFilter(
                Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);

        profile = session.getUserDetails();

        if (MapsActivity.isNetworkAvailable(this)) {
            loadSpots();  //chargement des spots
            loadAdditionnalUserInformation(); // chargement  des infos additionnelles
        } else {
            Toast.makeText(getApplicationContext(), "Check your internet connexion", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadAdditionnalUserInformation() {
        String dossier = getApplicationContext().getFilesDir().getPath() + DBServer.DOSSIER_IMAGE;
        final File file = new File(dossier + File.separator + profile.get(SessionManager.KEY_PHOTO) + ".jpg");
        Log.i("moi", profile.get(SessionManager.KEY_PHOTO));
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                friends = server.friends_count(Integer.valueOf(profile.get(SessionManager.KEY_ID)));
                if (profile.get(SessionManager.KEY_PHOTO) != null & profile.get(SessionManager.KEY_PHOTO) != "") {
                    AWS_Tools aws_tools = new AWS_Tools(getApplicationContext());
                    aws_tools.download(file, profile.get(SessionManager.KEY_PHOTO));
                }

                //set device id
                if (profile.get(SessionManager.KEY_REGISTRATION_ID) != null)
                    server.set_device_id(Integer.valueOf(profile.get(SessionManager.KEY_ID)), profile.get(SessionManager.KEY_REGISTRATION_ID));
                else
                    Log.e("GCM", "registration id null");
            }
        });

        t.start(); // spawn thread
        try {
            t.join();
            session.store_friend_number(friends);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * this version is running asynchronously to prevent the system to stuck on it
     */
    private void loadSpots() {

        spots = new ArrayList<>();

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                spots = server.find_spot_user(Integer.valueOf(profile.get(SessionManager.KEY_ID)), 0, 10);

                server.send_notification(Integer.valueOf(profile.get(SessionManager.KEY_ID)));
            }
        });

        t.start(); // spawn thread

        Intent itmain = new Intent(getApplicationContext(), MainActivity.class);
        finish();
        startActivity(itmain);
    }

//    private void loadSpots() {
//
//        spots = new ArrayList<>();
//
//        Thread t = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                spots = server.find_spot_user(Integer.valueOf(profile.get(SessionManager.KEY_ID)), 0,
//                        Integer.valueOf(profile.get(SessionManager.KEY_SPOT)));
//            }});
//
//        t.start(); // spawn thread
//        try{
//            t.join();
//            if (spots != null) {
//                if (spots.size() > 0) {
//                    bar.setVisibility(View.VISIBLE);
//                    bar.setMax(spots.size());
//                    String dossier = getApplicationContext().getFilesDir().getPath() + DBServer.DOSSIER_IMAGE;
//                    Log.i("Photo", dossier);
//                    File folder = new File(dossier);
//                    if (!folder.exists())
//                        folder.mkdirs();
//
//
//                    for (final Spot s : spots) {
//                        final File file = new File(dossier + File.separator + s.getPhotokey() + ".jpg");
//                        Log.i("test", file.getAbsolutePath());
//                        AWS_Tools aws_tools = new AWS_Tools(getApplicationContext());
//                        int transfertId = aws_tools.download(file, s.getPhotokey());
//                        TransferUtility transferUtility = aws_tools.getTransferUtility();
//                        TransferObserver observer = transferUtility.getTransferById(transfertId);
//                        observer.setTransferListener(new TransferListener() {
//
//                            @Override
//                            public void onStateChanged(int id, TransferState state) {
//                                // do something
//                            }
//
//                            @Override
//                            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
//                                int rapport = (int) (bytesCurrent * 100);
//                                try {
//                                    rapport /= bytesTotal;
//                                }catch (Exception e){
//                                    rapport= 100;
//                                }
//                                if (rapport == 100) {
//                                    mProgressStatus++;
//                                    bar.setProgress(mProgressStatus);
//                                }
//                                if (bar.getProgress() == spots.size()) {
//                                    if (RAPPORT_PROGRESSION == 0) {
//                                        RAPPORT_PROGRESSION = 1;
//                                        Intent itmain = new Intent(getApplicationContext(), MainActivity.class);
//                                        finish();
//                                        startActivity(itmain);
//                                    }
//                                }
//                            }
//
//                            @Override
//                            public void onError(int id, Exception ex) {
//                                // do something
//                                Log.e("error chargement", ex.getMessage());
//                                mProgressStatus++;
//                                bar.setProgress(mProgressStatus);
//                            }
//
//                        });
//                    }
//                }
//                else{
//                    final Intent itmain = new Intent(getApplicationContext(), MainActivity.class);
//                    bar.setMax(10);
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            bar.setProgress(2);
//                        }
//                    }, 3000);
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            bar.setProgress(4);
//                        }
//                    }, 6000);
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            bar.setProgress(6);
//                        }
//                    }, 9000);
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            bar.setProgress(8);
//                        }
//                    }, 12000);
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            bar.setProgress(10);
//                        }
//                    }, 15000);
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            finish();
//                            startActivity(itmain);
//                        }
//                    }, 15000);
//                }
//            }
//            else{
//                final Intent itmain = new Intent(getApplicationContext(), MainActivity.class);
//                bar.setMax(10);
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        bar.setProgress(2);
//                    }
//                }, 3000);
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        bar.setProgress(4);
//                    }
//                }, 6000);
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        bar.setProgress(6);
//                    }
//                }, 9000);
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        bar.setProgress(8);
//                    }
//                }, 12000);
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        bar.setProgress(10);
//                    }
//                }, 15000);
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        finish();
//                        startActivity(itmain);
//                    }
//                }, 15000);
//            }
//        }catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
}
