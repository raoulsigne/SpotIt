package techafrkix.work.com.spot.spotit;

import android.support.v4.app.Fragment;
import android.app.ListActivity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import techafrkix.work.com.spot.bd.Spot;
import techafrkix.work.com.spot.bd.SpotsDBAdapteur;

public class ListeSpots extends Fragment {

    private SpotsDBAdapteur dbAdapteur;
    private SQLiteDatabase db;
    private ArrayList<Spot> spots;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_liste_spots, container, false);

        loadSpots();
        ListView listView = (ListView) view.findViewById(R.id.listView);
        // Create the adapter to convert the array to views
        SpotAdapter adapter = new SpotAdapter(getActivity(), spots);
        // Attach the adapter to a ListView
        listView.setAdapter(adapter);

        return view;
    }

    private void loadSpots() {
        dbAdapteur = new SpotsDBAdapteur(getActivity());
        spots = new ArrayList<>();
        db = dbAdapteur.open();
        if (db != null) {
            spots = dbAdapteur.getAllSpots();
            Log.i("spots",spots.toString());
            db.close();
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
