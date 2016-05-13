package techafrkix.work.com.spot.spotit;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;

import java.io.File;
import java.util.ArrayList;

import techafrkix.work.com.spot.bd.Spot;
import techafrkix.work.com.spot.bd.SpotsDBAdapteur;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.AWS_Tools;

public class Welcome extends AppCompatActivity {

    private ProgressBar bar = null;
    private int mProgressStatus = 0;
    private SpotsDBAdapteur dbAdapteur;
    private SQLiteDatabase db;
    private ArrayList<Spot> spots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        bar = (ProgressBar)findViewById(R.id.progressBar);
        bar.getProgressDrawable().setColorFilter(
                Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);

        loadSpots();
    }

    private void loadSpots() {
        dbAdapteur = new SpotsDBAdapteur(getApplicationContext());
        spots = new ArrayList<>();
        db = dbAdapteur.open();
        if (db != null) {
            spots = dbAdapteur.getAllSpots();
            Log.i("spots", spots.toString());
            db.close();
        }

        bar.setVisibility(View.VISIBLE);
        bar.setMax(spots.size());

        File folder = new File(getApplicationContext().getFilesDir().getPath()+"/Images/");
        if (!folder.exists())
            folder.mkdirs();

        if(spots != null & spots.size() > 0) {
            for (final Spot s : spots) {
                final File file = new File(getApplicationContext().getFilesDir().getPath() + "/Images/" + s.getPhotokey() + ".jpg");
                AWS_Tools aws_tools = new AWS_Tools(getApplicationContext());
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
                            mProgressStatus++;
                            bar.setProgress(mProgressStatus);
                        }
                        if (bar.getProgress() == spots.size()) {
                            Intent itacceuil = new Intent(getApplicationContext(), Accueil.class);
                            finish();
                            startActivity(itacceuil);
                        }
                    }

                    @Override
                    public void onError(int id, Exception ex) {
                        // do something
                    }

                });
            }
        }
        else{
            final Intent itacceuil = new Intent(getApplicationContext(), Accueil.class);
            bar.setMax(10);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    bar.setProgress(2);
                }
            }, 3000);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    bar.setProgress(4);
                }
            }, 6000);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    bar.setProgress(6);
                }
            }, 9000);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    bar.setProgress(8);
                }
            }, 12000);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    bar.setProgress(10);
                }
            }, 15000);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                    startActivity(itacceuil);
                }
            }, 18000);
        }
    }
}
