package techafrkix.work.com.spot.spotit;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
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
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "friend";
    private static final String ARG_PARAM3 = "spot";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    ImageButton imgrespot, imgshare, imgcomment, imgletgo;
    Button btnpost;
    EditText edtComment;
    ImageView imgspot, imgprofile;
    ListView listView;
    TextView txttag, txtdate, txtNbcomments;
    int resultat;

    Spot spot;
    SessionManager session;
    private HashMap<String, String> profile;
    private Utilisateur friend;

    private ArrayList<Commentaire> commentaires;
    private DBServer server;
    private CommentAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        spot = new Spot();
        // Session class instance
        session = new SessionManager(getActivity());
        profile = new HashMap<>();
        profile = session.getUserDetails();
        if (getArguments() != null) {
            spot = (Spot) getArguments().getSerializable(ARG_PARAM3);
            try {
                friend = (Utilisateur) getArguments().getSerializable(ARG_PARAM2);
            } catch (Exception e) {
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

        txtdate = (TextView) view.findViewById(R.id.txtDate);
        txttag = (TextView) view.findViewById(R.id.txtTag);
        txtNbcomments = (TextView) view.findViewById(R.id.txtSpots);
        imgprofile = (ImageView) view.findViewById(R.id.profile_image);
        imgspot = (ImageView) view.findViewById(R.id.imgSpot);
        imgrespot = (ImageButton) view.findViewById(R.id.imgRespot);
        imgletgo = (ImageButton) view.findViewById(R.id.imgNavigation);
        imgcomment = (ImageButton) view.findViewById(R.id.imgchat);
        imgshare = (ImageButton) view.findViewById(R.id.imgshare);
        listView = (ListView) view.findViewById(R.id.listComments);
        btnpost = (Button) view.findViewById(R.id.btnPost);
        edtComment = (EditText) view.findViewById(R.id.edtComment);

        if (spot.getNbcomment() > 1)
            txtNbcomments.setText(spot.getNbcomment() + " comments");
        else
            txtNbcomments.setText(spot.getNbcomment() + " comment");

        imgrespot.setOnClickListener(new View.OnClickListener() {
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
                    Toast.makeText(getActivity(), "You cannot comment your own spot!", Toast.LENGTH_SHORT).show();
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
            ;
            String dossier = getActivity().getApplicationContext().getFilesDir().getPath() + DBServer.DOSSIER_IMAGE;
            final File file = new File(dossier + File.separator + spot.getPhotokey() + ".jpg");
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

        if (profile.get(SessionManager.KEY_PHOTO) != null & profile.get(SessionManager.KEY_PHOTO) != "") {
            String dossier = getActivity().getApplicationContext().getFilesDir().getPath() + DBServer.DOSSIER_IMAGE;
            final File file = new File(dossier + File.separator + profile.get(SessionManager.KEY_PHOTO) + ".jpg");

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
                if (commentaires.size() > 1)
                    txtNbcomments.setText(commentaires.size() + " comments");
                else
                    txtNbcomments.setText(commentaires.size() + " comment");
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
            String dossier = context.getApplicationContext().getFilesDir().getPath() + DBServer.DOSSIER_IMAGE;
            final File file = new File(dossier + File.separator + commentaire.getPhotokey() + ".jpg");

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
