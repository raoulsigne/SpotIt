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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

    SessionManager session;
    private HashMap<String, String> profile;
    private DBServer server;
    private int resultat;

    public SpotAdapter(Context context, ArrayList<Spot> spots, Fragment fg) {
        super(context, 0, spots);
        this.context = context;
        try {
            this.mAdapterCallback = ((AdapterCallback) fg);
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement AdapterCallback.");
        }

        session = new SessionManager(context);
        profile = new HashMap<>();
        profile = session.getUserDetails();
        server = new DBServer(context);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final TextView spotDate, spotTag;
        final ImageButton share, comment, letsgo, like, respot;
        final ImageView spotPhoto;
        final ImageView photoprofile;

        // Get the data item for this position
        final Spot spot = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_spot, parent, false);
        }

        // Lookup view for data population
        spotDate = (TextView)convertView.findViewById(R.id.txtDate);
        spotTag = (TextView)convertView.findViewById(R.id.txtTag);
        spotPhoto = (ImageView)convertView.findViewById(R.id.imgSpot);
        photoprofile = (ImageView)convertView.findViewById(R.id.profile_image);

        like = (ImageButton) convertView.findViewById(R.id.imglike);
        comment = (ImageButton) convertView.findViewById(R.id.imgchat);
        respot = (ImageButton) convertView.findViewById(R.id.imgrespot);
        share = (ImageButton) convertView.findViewById(R.id.imgshare);
        letsgo = (ImageButton) convertView.findViewById(R.id.imgNavigation);

        comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapterCallback.detail(position);
            }
        });

        spotPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapterCallback.detail(position);
            }
        });
        letsgo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapterCallback.letsgo(position);
            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAdapterCallback.share(position);
            }
        });
        respot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (spot.getUser_id() != Integer.valueOf(profile.get(SessionManager.KEY_ID))) {
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            resultat = server.enregistrer_respot(Integer.valueOf(profile.get(SessionManager.KEY_ID)), spot.getId());
                        }
                    });

                    t.start(); // spawn thread
                    try {
                        t.join();
                        if (resultat > 0) {
                            session.increment_nbrespot(); // incremente le nombre de respots d'un utilisateur
                            Toast.makeText(context, "Operation succeed!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else
                    Toast.makeText(context, "You cannot respot your own spot!", Toast.LENGTH_SHORT).show();
            }
        });
        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        Log.i("teste", spot.getId() + " " + spot.getPhotokey());
        // Populate the data into the template view using the data object
        try {
            spotDate.setText(spot.getDate());
            StringBuilder chainetag = new StringBuilder();
//            if (spot.getNbcomment() > 1)
//                txtcomment.setText(spot.getNbcomment() + " comments");
//            else
//                txtcomment.setText(spot.getNbcomment() + " comment");
            if (spot.getTags().size() == 0)
                spotTag.setText("No tag");
            else {
                for (String s :
                        spot.getTags()) {
                    chainetag.append("#" + s + " ");
                }
                spotTag.setText(chainetag.toString());
            }

            //photo du spot
            File file = new File(DBServer.DOSSIER_IMAGE + File.separator + spot.getPhotokey() + ".jpg");
            if (file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

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
            }

            //photo de profile du spoteur
            final File file1 = new File(DBServer.DOSSIER_IMAGE + File.separator + spot.getPhotouser() + ".jpg");
            if (file1.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(file1.getAbsolutePath());
                // marker.showInfoWindow();
                photoprofile.setImageBitmap(bitmap);
            }
        }catch (Exception e){
            Log.e("spot", e.getMessage());}

        SessionManager session = new SessionManager(context);
        HashMap<String, String> profile = session.getUserDetails();

        // Return the completed view to render on screen
        return convertView;
    }

    public interface AdapterCallback{
        public void detail(int position);
        public void share(int position);
        public void letsgo(int position);
    }
}