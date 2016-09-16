package techafrkix.work.com.spot.spotit;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import techafrkix.work.com.spot.bd.Commentaire;
import techafrkix.work.com.spot.bd.Spot;
import techafrkix.work.com.spot.bd.Utilisateur;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.AWS_Tools;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.DBServer;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.SessionManager;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DetailSpot.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class DetailSpot extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "spots";
    private static final String ARG_PARAM2 = "friend";
    private static final String ARG_PARAM3 = "spot";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private ImageButton share, comment, letsgo, like, respot;
    Button btnpost;
    EditText edtComment;
    ImageView imgspot, imgprofile;
    ListView listView;
    TextView txttag, txtdate;
    LinearLayout retour;
    int resultat, type;

    private Spot spot;
    private ArrayList<Spot> spots;
    private SessionManager session;
    private HashMap<String, String> profile;
    private Utilisateur friend;

    private ArrayList<Commentaire> commentaires;
    private DBServer server;
    private CommentAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        spot = new Spot();
        spots = new ArrayList<>();

        // Session class instance
        session = new SessionManager(getActivity());
        profile = new HashMap<>();
        profile = session.getUserDetails();
        if (getArguments() != null) {
            spot = (Spot) getArguments().getSerializable(ARG_PARAM3);
            type = getArguments().getInt("type");
            try {
                friend = (Utilisateur) getArguments().getSerializable(ARG_PARAM2);
            } catch (Exception e) {
                friend = null;
            }

            try {
                spots = (ArrayList<Spot>) getArguments().getSerializable(ARG_PARAM1);
            } catch (Exception e) {
                spots = null;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.detailspot, container, false);

        server = new DBServer(getActivity());
        commentaires = new ArrayList<>();

        like = (ImageButton) view.findViewById(R.id.imglike);
        comment = (ImageButton) view.findViewById(R.id.imgchat);
        respot = (ImageButton) view.findViewById(R.id.imgrespot);
        share = (ImageButton) view.findViewById(R.id.imgshare);
        letsgo = (ImageButton) view.findViewById(R.id.imgNavigation);

        txtdate = (TextView) view.findViewById(R.id.txtDate);
        txttag = (TextView) view.findViewById(R.id.txtTag);
        imgprofile = (ImageView) view.findViewById(R.id.profile_image);
        imgspot = (ImageView) view.findViewById(R.id.imgSpot);
        listView = (ListView) view.findViewById(R.id.listComments);
        btnpost = (Button) view.findViewById(R.id.btnPost);
        edtComment = (EditText) view.findViewById(R.id.edtComment);
        retour = (LinearLayout) view.findViewById(R.id.retour);

        retour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (type){
                    case 1:
                        mListener.onLoadSpot();
                        break;
                    case 2:
                        mListener.onListSpot_Friend(friend);
                        break;
                    case 3:
                        mListener.onLoadSpot(spots);
                        break;
                }
            }
        });

        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                like.setBackground(getActivity().getResources().getDrawable(R.drawable.liked));
            }
        });

        respot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                            Toast.makeText(getActivity(), "Operation succeed!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else
                    Toast.makeText(getActivity(), "You cannot respot your own spot!", Toast.LENGTH_SHORT).show();
            }
        });
        btnpost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!edtComment.getText().toString().matches("")) {
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            resultat = server.add_comment(spot.getId(), Integer.valueOf(profile.get(SessionManager.KEY_ID)), edtComment.getText().toString());
                        }
                    });

                    t.start(); // spawn thread
                    try {
                        t.join();
                        edtComment.setText("");
                        View view = getActivity().getCurrentFocus();
                        if (view != null) {
                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }
                        if (resultat > 0) {
                            loadComment();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else
                    Toast.makeText(getActivity(), "Specify the comment please!", Toast.LENGTH_SHORT).show();
            }
        });

        letsgo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                letsgo();
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uriToImage = ListeSpots.getImageContentUri(getActivity(), new File(DBServer.DOSSIER_IMAGE + File.separator + spot.getPhotokey() + ".jpg"));

                List<Intent> targetedShareIntents = new ArrayList<Intent>();

                Intent facebookIntent = getShareIntent("facebook", "spot it", uriToImage);
                if(facebookIntent != null)
                    targetedShareIntents.add(facebookIntent);

                Intent twitterIntent = getShareIntent("twitter", "spot it", uriToImage);
                if(twitterIntent != null)
                    targetedShareIntents.add(twitterIntent);

                Intent instagramIntent = getShareIntent("instagram", "spot it", uriToImage);
                if(instagramIntent != null)
                    targetedShareIntents.add(instagramIntent);

                Intent whatsappIntent = getShareIntent("whatsapp", "spot it", uriToImage);
                if(instagramIntent != null)
                    targetedShareIntents.add(whatsappIntent);

                Intent gmailIntent = getShareIntent("gmail", "spot it", uriToImage);
                if(gmailIntent != null)
                    targetedShareIntents.add(gmailIntent);

                Intent chooser = Intent.createChooser(targetedShareIntents.remove(0), getResources().getText(R.string.send_to));

                chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(new Parcelable[]{}));

                startActivity(chooser);
            }
        });

        // Populate the data into the template view using the data object
        try {
            txtdate.setText(spot.getDate());
            StringBuilder chainetag = new StringBuilder();
            if (spot.getTags().size() == 0)
                txttag.setText("No tag");
            else {
                for (String s :
                        spot.getTags()) {
                    chainetag.append("#" + s + " ");
                }
                txttag.setText(chainetag.toString());
            }

            final File file = new File(DBServer.DOSSIER_IMAGE + File.separator + spot.getPhotokey() + ".jpg");

            if (!file.exists())
                chargement_image(spot.getPhotokey());

            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

            // Get height or width of screen at runtime
            Display display = ((Activity) getContext()).getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            //reduce the photo dimension keeping the ratio so that it'll fit in the imageview
            int nh = (int) (bitmap.getHeight() * (Double.valueOf(width) / bitmap.getWidth()));
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, width, nh, true);
            //define the image source of the imageview
            imgspot.setImageBitmap(scaled);
        } catch (Exception e) {
            Log.e("spot", e.getMessage());
        }

        if (friend != null) {
            if (friend.getPhoto() != null & friend.getPhoto() != "") {
                final File file = new File(DBServer.DOSSIER_IMAGE + File.separator + friend.getPhoto() + ".jpg");

                if (file.exists()) {
                    // marker.showInfoWindow();
                    imgprofile.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
                    Log.i("file", "file exists");
                } else {
                    if (MapsActivity.isNetworkAvailable(MainActivity.getAppContext())) {
                        Log.i("file", "file not exists");
                        AWS_Tools aws_tools = new AWS_Tools(MainActivity.getAppContext());
                        final ProgressDialog barProgressDialog = new ProgressDialog(getActivity());
                        barProgressDialog.setTitle("Telechargement du spot ...");
                        barProgressDialog.setMessage("Opération en progression ...");
                        barProgressDialog.setProgressStyle(barProgressDialog.STYLE_HORIZONTAL);
                        barProgressDialog.setProgress(0);
                        barProgressDialog.setMax(100);
                        barProgressDialog.show();
                        int transfertId = aws_tools.download(file, friend.getPhoto());
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
                                    barProgressDialog.setProgress(rapport);
                                    if (rapport == 100) {
                                        barProgressDialog.dismiss();
                                        imgprofile.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
                                    }
                                }else
                                    barProgressDialog.dismiss();
                            }

                            @Override
                            public void onError(int id, Exception ex) {
                                // do something
                                barProgressDialog.dismiss();
                            }

                        });
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
        }
        else if (spot.getPhotouser() != null & spot.getPhotouser() != "") {
            final File file = new File(DBServer.DOSSIER_IMAGE + File.separator + spot.getPhotouser() + ".jpg");

            if (file.exists()) {
                // marker.showInfoWindow();
                imgprofile.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
                Log.i("file", "file exists");
            } else {
                if (MapsActivity.isNetworkAvailable(MainActivity.getAppContext())) {
                    Log.i("file", "file not exists");
                    AWS_Tools aws_tools = new AWS_Tools(MainActivity.getAppContext());
                    final ProgressDialog barProgressDialog = new ProgressDialog(getActivity());
                    barProgressDialog.setTitle("Telechargement du spot ...");
                    barProgressDialog.setMessage("Opération en progression ...");
                    barProgressDialog.setProgressStyle(barProgressDialog.STYLE_HORIZONTAL);
                    barProgressDialog.setProgress(0);
                    barProgressDialog.setMax(100);
                    barProgressDialog.show();
                    int transfertId = aws_tools.download(file, spot.getPhotouser());
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
                                barProgressDialog.setProgress(rapport);
                                if (rapport == 100) {
                                    barProgressDialog.dismiss();
                                    imgprofile.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
                                }
                            }else
                                barProgressDialog.dismiss();
                        }

                        @Override
                        public void onError(int id, Exception ex) {
                            // do something
                            barProgressDialog.dismiss();
                        }

                    });
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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

        loadComment();

        return view;
    }

    private Intent getShareIntent(String type, String subject, Uri uri)
    {
        boolean found = false;
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("image/*");

        // gets the list of intents that can be loaded.
        List<ResolveInfo> resInfo = getActivity().getPackageManager().queryIntentActivities(share, 0);
        System.out.println("resinfo: " + resInfo);
        if (!resInfo.isEmpty()){
            for (ResolveInfo info : resInfo) {
                if (info.activityInfo.packageName.toLowerCase().contains(type) ||
                        info.activityInfo.name.toLowerCase().contains(type) ) {
                    share.putExtra(Intent.EXTRA_SUBJECT,  subject);
                    share.putExtra(Intent.EXTRA_STREAM,     uri);
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
        void onLoadSpot();
        void onListSpot_Friend(Utilisateur friend);
        void onLoadSpot(ArrayList<Spot> spots);
    }

    private void loadComment() {
        final ProgressDialog barProgressDialog = new ProgressDialog(getActivity());
        barProgressDialog.setProgressStyle(barProgressDialog.STYLE_SPINNER);

        commentaires = new ArrayList<>();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                commentaires = server.commentaires_spot(spot.getId());
            }
        });

        t.start(); // spawn thread
        try {
            t.join();
            if (commentaires != null) {
                Log.i("comments", commentaires.toString());
                adapter = new CommentAdapter(getActivity(), commentaires);
                listView.setAdapter(adapter);
            } else {
                // Setting new scroll position
                listView.setSelectionFromTop(0, 0);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // closing progress dialog
        barProgressDialog.dismiss();
    }

    public void letsgo() {
        String uri = String.format(Locale.ENGLISH, "google.navigation:q=%f,%f&mode=d,w,b", Double.valueOf(spot.getLatitude()),
                Double.valueOf(spot.getLongitude()));
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");
        getActivity().startActivity(intent);
    }

    public void chargement_image(String photokey){
        final File file = new File(DBServer.DOSSIER_IMAGE + File.separator + photokey + ".jpg");

        AWS_Tools aws_tools = new AWS_Tools(MainActivity.getAppContext());
        final ProgressDialog barProgressDialog = new ProgressDialog(getActivity());
        barProgressDialog.setTitle("Telechargement du spot ...");
        barProgressDialog.setMessage("Opération en progression ...");
        barProgressDialog.setProgressStyle(barProgressDialog.STYLE_HORIZONTAL);
        barProgressDialog.setProgress(0);
        barProgressDialog.setMax(100);
        barProgressDialog.show();
        int transfertId = aws_tools.download(file, photokey);
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
                    barProgressDialog.setProgress(rapport);
                    if (rapport == 100) {
                        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

                        // Get height or width of screen at runtime
                        Display display = ((Activity) getContext()).getWindowManager().getDefaultDisplay();
                        Point size = new Point();
                        display.getSize(size);
                        int width = size.x;
                        //reduce the photo dimension keeping the ratio so that it'll fit in the imageview
                        int nh = (int) (bitmap.getHeight() * (Double.valueOf(width) / bitmap.getWidth()));
                        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, width, nh, true);
                        //define the image source of the imageview
                        imgspot.setImageBitmap(scaled);
                        barProgressDialog.dismiss();
                    }
                }else
                    barProgressDialog.dismiss();
            }

            @Override
            public void onError(int id, Exception ex) {
                // do something
                barProgressDialog.dismiss();
            }

        });
    }
}

class CommentAdapter extends ArrayAdapter<Commentaire> {

    ArrayList<Commentaire> commentaires;
    Context context;

    public CommentAdapter(Context context, ArrayList<Commentaire> commentaires) {
        super(context, 0, commentaires);
        this.context = context;
        this.commentaires = commentaires;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final TextView txtnom, txtcomment, txttemps;
        final ImageView photoprofile;

        // Get the data item for this position
        Commentaire commentaire = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.comment, parent, false);
        }

        // Lookup view for data population
        photoprofile = (ImageView) convertView.findViewById(R.id.profile_image);
        txtcomment = (TextView) convertView.findViewById(R.id.txtcomment);
        txtnom = (TextView) convertView.findViewById(R.id.txtnom);
        txttemps = (TextView) convertView.findViewById(R.id.txttemps);

        // Populate the data into the template view using the data object
        txtcomment.setText(commentaire.getCommentaire());
        txtnom.setText(commentaire.getPseudo());
        txttemps.setText(commentaire.getCreated());
        if (commentaire.getPhotokey() != "") {
            final File file = new File(DBServer.DOSSIER_IMAGE + File.separator + commentaire.getPhotokey() + ".jpg");

            if (file.exists()) {
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
                    int transfertId = aws_tools.download(file, commentaire.getPhotokey());
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
                                barProgressDialog.setProgress(rapport);
                                if (rapport == 100) {
                                    barProgressDialog.dismiss();
                                    photoprofile.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
                                }
                            }else
                                barProgressDialog.dismiss();
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
}
