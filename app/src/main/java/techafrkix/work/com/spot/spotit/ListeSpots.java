package techafrkix.work.com.spot.spotit;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
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
import java.util.List;
import java.util.Locale;

import techafrkix.work.com.spot.bd.Spot;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.AWS_Tools;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.DBServer;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.RealPathUtil;
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
    private int type, retour;

//    private ImageView imgprofile;
    private TextView txtpseudo;
//    private TextView txtspots;
//    private TextView txtfriend;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        spots = new ArrayList<>();

        // Session class instance
        session = new SessionManager(getActivity());
        profile = new HashMap<>();
        server = new DBServer(getActivity());
        profile = session.getUserDetails();

        Log.i("spot-name", session.get_list_spot_name().toString());

        if (getArguments() != null) {
            type = (int) getArguments().getInt("type");
            if (type == 0) {
                spots = (ArrayList<Spot>) getArguments().getSerializable("spots");
                Log.i("teste", "dialog " + spots.toString());
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
        View view;
        type = (int) getArguments().getInt("type");
        if (type == 1) {
            view = inflater.inflate(R.layout.activity_liste_spots_user, container, false);
            txtpseudo = (TextView)view.findViewById(R.id.txtPseudo_friend);
            txtpseudo.setText(profile.get(SessionManager.KEY_NAME));
        }
        else
            view = inflater.inflate(R.layout.activity_liste_spots, container, false);

        listView = (ListView) view.findViewById(R.id.listView);

        offset = 0;
        preLast = 0;

        tampon = new ArrayList<>();
        loadSpots();

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

                        loadSpots();
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
                        File folder = new File(DBServer.DOSSIER_IMAGE);
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
                                final File file = new File(DBServer.DOSSIER_IMAGE + File.separator + s.getPhotokey() + ".jpg");
                                if (file.exists()) {
//                                    spotsimages.put(s.getPhotokey(), BitmapFactory.decodeFile(file.getAbsolutePath()));
                                    barProgressDialog.setProgress(barProgressDialog.getProgress() + 1);
                                    if (barProgressDialog.getProgress() == spots.size()) {
                                        barProgressDialog.dismiss();
                                        // Create the adapter to convert the array to views
                                        adapter = new SpotAdapter(getActivity(), spots, ListeSpots.this, type);
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
                                            } else {
                                                barProgressDialog.setProgress(barProgressDialog.getProgress() + 1);
                                            }
                                            if (barProgressDialog.getProgress() == spots.size()) {
                                                barProgressDialog.dismiss();
                                                // Create the adapter to convert the array to views
                                                adapter = new SpotAdapter(getActivity(), spots, ListeSpots.this, type);
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
            final File folder = new File(DBServer.DOSSIER_IMAGE);
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
                final int currentPosition = offset;
                for (int i = offset; i < max; i++) {
                    s = new Spot();
                    s = spots.get(i);
                    tampon.add(s);
                    final File file = new File(DBServer.DOSSIER_IMAGE + File.separator + s.getPhotokey() + ".jpg");
                    if (file.exists()) {
                        barProgressDialog.setProgress(barProgressDialog.getProgress() + 1);

                        if (barProgressDialog.getProgress() == barProgressDialog.getMax()) {
                            barProgressDialog.dismiss();
                            // Create the adapter to convert the array to views
                            adapter = new SpotAdapter(getActivity(), tampon, ListeSpots.this, type);
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
                                    }
                                } else {
                                    barProgressDialog.setProgress(barProgressDialog.getProgress() + 1);
                                    tampon.remove(s);
                                }
                                if (barProgressDialog.getProgress() == barProgressDialog.getMax()) {
                                    barProgressDialog.dismiss();

                                    // Create the adapter to convert the array to views
                                    adapter = new SpotAdapter(getActivity(), tampon, ListeSpots.this, type);
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
                            }

                        });
                    }


                    //photo de profile du spoteur
                    final File file1 = new File(DBServer.DOSSIER_IMAGE + File.separator + s.getPhotouser() + ".jpg");
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
                                } else {
                                    barProgressDialog.setProgress(barProgressDialog.getProgress() + 1);
                                }
                                if (barProgressDialog.getProgress() == barProgressDialog.getMax()) {
                                    barProgressDialog.dismiss();

                                    // Create the adapter to convert the array to views
                                    adapter = new SpotAdapter(getActivity(), tampon, ListeSpots.this, type);
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
                    } else {
                        barProgressDialog.setProgress(barProgressDialog.getProgress() + 1);
                        if (barProgressDialog.getProgress() == barProgressDialog.getMax()) {
                            barProgressDialog.dismiss();
                            // Create the adapter to convert the array to views
                            adapter = new SpotAdapter(getActivity(), tampon, ListeSpots.this, type);
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
            mListener.onDetailSpot_user(spots, spots.get(position));
        } else {
            mListener.onDetailSpot(tampon, tampon.get(position));
        }
    }

    @Override
    public void share(int position) {
        Uri uriToImage;
        if (type == 1) {
            uriToImage = getImageContentUri(getActivity(), new File(DBServer.DOSSIER_IMAGE + File.separator + spots.get(position).getPhotokey() + ".jpg"));
        } else {
            uriToImage = getImageContentUri(getActivity(), new File(DBServer.DOSSIER_IMAGE + File.separator + tampon.get(position).getPhotokey() + ".jpg"));
        }

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
        if(instagramIntent != null)
            targetedShareIntents.add(gmailIntent);

        Intent chooser = Intent.createChooser(targetedShareIntents.remove(0), getResources().getText(R.string.send_to));

        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(new Parcelable[]{}));

        startActivity(chooser);

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
    public void letsgo(int position) {

        String uri = "";
        if (type == 1) {
            uri = String.format(Locale.ENGLISH, "google.navigation:q=%f,%f&mode=d,w,b", Double.valueOf(spots.get(position).getLatitude()),
                    Double.valueOf(spots.get(position).getLongitude()));
        } else {
            uri = String.format(Locale.ENGLISH, "google.navigation:q=%f,%f&mode=d,w,b", Double.valueOf(tampon.get(position).getLatitude()),
                    Double.valueOf(tampon.get(position).getLongitude()));
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        getActivity().startActivity(intent);

    }

    @Override
    public void delete(final int position) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                if (type == 1) {
                    retour = server.delete_spot(spots.get(position).getId());
                } else {
                    retour = server.delete_spot(tampon.get(position).getId());
                }
            }});

        t.start(); // spawn thread
        try{
            t.join();
            if (retour == 1) {
                Toast.makeText(getActivity(), "Spot deleted", Toast.LENGTH_SHORT).show();
                if (type == 1) {
                    if (spots.get(position).getRespot() == 1)
                        session.decrement_nbrespot();
                    else
                        session.decrement_nbspot();
                    adapter.remove(spots.get(position));
                } else {
                    if (tampon.get(position).getRespot() == 1)
                        session.decrement_nbrespot();
                    else
                        session.decrement_nbspot();
                    adapter.remove(tampon.get(position));
                }
            } else {
                Toast.makeText(getActivity(), "unable to delete spot", Toast.LENGTH_SHORT).show();
            }
        }catch (InterruptedException e) {
            e.printStackTrace();
        }
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
        void onDetailSpot(ArrayList<Spot> spots, Spot spot);
        void onDetailSpot_user(ArrayList<Spot> spots, Spot spot);
        void onLetsGo();
    }


    /**
     * funcction which help to get uri from image file
     *
     * @param context
     * @param imageFile image to get uri
     * @return uri representing the requested image uri
     */
    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            cursor.close();
            return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }
}
