package techafrkix.work.com.spot.spotit;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

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

/**
 * Created by techafrkix0 on 25/04/2016.
 */
public class SpotAdapter extends ArrayAdapter<Spot> {

    HashMap<String, Bitmap> mapimages;
    private AdapterCallback mAdapterCallback;
    private Context context;

    public SpotAdapter(Context context, ArrayList<Spot> spots, Fragment fg) {
        super(context, 0, spots);
        this.context = context;
        try {
            this.mAdapterCallback = ((AdapterCallback) fg);
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement AdapterCallback.");
        }
    }

    public SpotAdapter(Context context, ArrayList<Spot> spots, HashMap<String, Bitmap> spotsimages, Fragment fg) {
        super(context, 0, spots);
        mapimages = new HashMap<String, Bitmap>();
        mapimages = spotsimages;
        this.context = context;
        try {
            this.mAdapterCallback = ((AdapterCallback) fg);
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement AdapterCallback.");
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final TextView spotDate, spotTag;
        final TextView txtshare, txtcomment, txtletsgo;
        final ImageView spotPhoto;
        final ImageView photoprofile;

        // Get the data item for this position
        Spot spot = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_spot, parent, false);
        }

        // Lookup view for data population
        spotDate = (TextView)convertView.findViewById(R.id.txtDate);
        spotTag = (TextView)convertView.findViewById(R.id.txtTag);
        spotPhoto = (ImageView)convertView.findViewById(R.id.imgSpot);
        photoprofile = (ImageView)convertView.findViewById(R.id.profile_image);

        txtcomment = (TextView)convertView.findViewById(R.id.txtComments);
        txtletsgo = (TextView)convertView.findViewById(R.id.txtLetsgo);
        txtshare = (TextView)convertView.findViewById(R.id.txtShare);

        txtcomment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapterCallback.detail(position);
            }
        });
        txtletsgo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapterCallback.letsgo(position);
            }
        });
        txtshare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapterCallback.share(position);
            }
        });

        // Populate the data into the template view using the data object
        try {
            spotDate.setText(spot.getDate());
            StringBuilder chainetag = new StringBuilder();
            if (spot.getNbcomment() > 1)
                txtcomment.setText(spot.getNbcomment() + " comments");
            else
                txtcomment.setText(spot.getNbcomment() + " comment");
            if (spot.getTags().size() == 0)
                spotTag.setText("No tag");
            else {
                for (String s :
                        spot.getTags()) {
                    chainetag.append("#" + s + " ");
                }
                spotTag.setText(chainetag.toString());
            }
            Bitmap bitmap = mapimages.get(spot.getPhotokey()); //BitmapFactory.decodeFile(spot.getPhotokey());

            // Get height or width of screen at runtime
            Display display = ((Activity) getContext()).getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;

            //reduce the photo dimension keeping the ratio so that it'll fit in the imageview
            int nh = (int) ( bitmap.getHeight() * (Double.valueOf(width) / bitmap.getWidth()) );
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, width, nh, true);

            //define the image source of the imageview
            spotPhoto.setImageBitmap(scaled);
        }catch (Exception e){
            Log.e("spot", e.getMessage());}

        SessionManager session = new SessionManager(context);
        HashMap<String, String> profile = session.getUserDetails();

        if (profile.get(SessionManager.KEY_PHOTO) != null & profile.get(SessionManager.KEY_PHOTO) != "") {
            String dossier = context.getApplicationContext().getFilesDir().getPath() + DBServer.DOSSIER_IMAGE;
            final File file = new File(dossier + File.separator + profile.get(SessionManager.KEY_PHOTO) + ".jpg");

            if (file.exists()) {
                // marker.showInfoWindow();
                photoprofile.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
                Log.i("file", "file exists");
            } else {
                if (MapsActivity.isNetworkAvailable(MainActivity.getAppContext())) {
                    Log.i("file", "file not exists");
                    AWS_Tools aws_tools = new AWS_Tools(MainActivity.getAppContext());
                    final ProgressDialog barProgressDialog = new ProgressDialog(context);
                    barProgressDialog.setTitle("Telechargement du spot ...");
                    barProgressDialog.setMessage("Opération en progression ...");
                    barProgressDialog.setProgressStyle(barProgressDialog.STYLE_HORIZONTAL);
                    barProgressDialog.setProgress(0);
                    barProgressDialog.setMax(100);
                    barProgressDialog.show();
                    int transfertId = aws_tools.download(file, profile.get(SessionManager.KEY_PHOTO));
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
                            barProgressDialog.setProgress(rapport);
                            if (rapport == 100) {
                                barProgressDialog.dismiss();
                                photoprofile.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
                            }
                        }

                        @Override
                        public void onError(int id, Exception ex) {
                            // do something
                            barProgressDialog.dismiss();
                        }

                    });
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Spot It:Information")
                            .setMessage("Vérifiez votre connexion Internet")
                            .setCancelable(false)
                            .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        }

        // Return the completed view to render on screen
        return convertView;
    }

    // Scale and maintain aspect ratio given a desired width
    // BitmapScaler.scaleToFitWidth(bitmap, 100);
    public static Bitmap scaleToFitWidth(Bitmap b, int width)

    {

        float factor = width / (float) b.getWidth();
        return Bitmap.createScaledBitmap(b, width, (int) (b.getHeight() * factor), true);

    }

    // Scale and maintain aspect ratio given a desired height
    // BitmapScaler.scaleToFitHeight(bitmap, 100);
    public static Bitmap scaleToFitHeight(Bitmap b, int height)

    {

        float factor = height / (float) b.getHeight();
        return Bitmap.createScaledBitmap(b, (int) (b.getWidth() * factor), height, true);

    }

    public interface AdapterCallback{
        public void detail(int position);
        public void share(int position);
        public void letsgo(int position);
    }
}