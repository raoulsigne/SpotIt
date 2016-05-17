package techafrkix.work.com.spot.techafrkix.work.com.spot.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.mobileconnectors.cognito.Dataset;
import com.amazonaws.mobileconnectors.cognito.DefaultSyncCallback;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import techafrkix.work.com.spot.spotit.MainActivity;

/**
 * Created by techafrkix0 on 11/05/2016.
 */
public class AWS_Tools {

    private static int RAPPORT_PROGRESSION = 0;
    private final int MAX_VALUE = 100;
    private final String MY_BUCKET = "bucketspotit";
    private final String DATE_KEY = "date";
    private final String LONGITUDE_KEY = "longitude";
    private final String LATITUDE_KEY = "latitude";
    private final String VISIBILITE_KEY = "visibilite";

    private CognitoCachingCredentialsProvider credentialsProvider;
    private CognitoSyncManager syncClient;
    private Dataset dataset;
    private AmazonS3 s3;
    private TransferUtility transferUtility;
    private Context context;

    public TransferUtility getTransferUtility() {
        return transferUtility;
    }

    public AWS_Tools(Context mcontext){
        RAPPORT_PROGRESSION = 0;
        context = mcontext;
        // Initialize the Amazon Cognito credentials provider
        credentialsProvider = new CognitoCachingCredentialsProvider(
                context,
                "eu-west-1:4d19cccb-cf8a-4fb7-bb44-0e8a80430c5a", // Identity Pool ID
                Regions.EU_WEST_1 // Region
        );
        // Create an S3 client
        s3 = new AmazonS3Client(credentialsProvider);

        // Set the region of your S3 bucket
        s3.setRegion(Region.getRegion(Regions.EU_WEST_1));

        //Instantiate TransferUtility
        transferUtility = new TransferUtility(s3, context);
    }

    public void uploadPhoto(File photo, String OBJECT_KEY){
        ObjectMetadata myObjectMetadata = new ObjectMetadata();
        final ProgressDialog barProgressDialog = new ProgressDialog(context);
        barProgressDialog.setTitle("Transfert du spot ...");
        barProgressDialog.setMessage("Opération en progression ...");
        barProgressDialog.setProgressStyle(barProgressDialog.STYLE_HORIZONTAL);
        barProgressDialog.setProgress(0);
        barProgressDialog.setMax(MAX_VALUE);
        barProgressDialog.show();
        //create a map to store user metadata
        Map<String, String> userMetadata = new HashMap<String,String>();

        //call setUserMetadata on our ObjectMetadata object, passing it our map
        myObjectMetadata.setUserMetadata(userMetadata);
        TransferObserver observer = transferUtility.upload(
                MY_BUCKET,     /* The bucket to upload to */
                OBJECT_KEY,    /* The key for the uploaded object */
                photo,        /* The file where the data to upload exists */
                myObjectMetadata  /* The ObjectMetadata associated with the object*/
        );
        observer.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {

            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                int rapport = (int)(bytesCurrent * 100);
                rapport /= bytesTotal;
                Log.i("upload","pourcentage "+rapport+ " variable statique "+RAPPORT_PROGRESSION);
                //Display percentage transfered to user
                barProgressDialog.setProgress(rapport);
                if (rapport == MAX_VALUE) {
                    if (RAPPORT_PROGRESSION == 0) {
                        RAPPORT_PROGRESSION = 1;
                        barProgressDialog.dismiss();
                        Toast.makeText(context, "Opération terminée", Toast.LENGTH_SHORT).show();
                        Intent mainintent = new Intent(context, MainActivity.class);
                        ((Activity) context).finish();
                        context.startActivity(mainintent);
                    }
                }
            }

            @Override
            public void onError(int id, Exception ex) {
                // do something
                barProgressDialog.dismiss();
                Toast.makeText(context, "Une erreur est survenue", Toast.LENGTH_SHORT).show();
            }

        });
    }

    public int download(File photo, String OBJECT_KEY){
        TransferObserver observer = transferUtility.download(
                MY_BUCKET,     /* The bucket to download from */
                OBJECT_KEY,    /* The key for the object to download */
                photo        /* The file to download the object to */
        );

        return observer.getId();
    }

}
