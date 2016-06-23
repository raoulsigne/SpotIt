package techafrkix.work.com.spot.spotit;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;

import techafrkix.work.com.spot.bd.Commentaire;
import techafrkix.work.com.spot.bd.Spot;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.DBServer;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.GeoHash;

public class Accueil extends AppCompatActivity {

    Button login, signin;
    ArrayList<Spot> spots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accueil);

        //recuperation des boutons à partir du xml
        login = (Button)findViewById(R.id.btnLogin);
        signin = (Button)findViewById(R.id.btnSignin);

        //actions des boutons qui consistent à ouvrir les activités respectivent

        //fenetre de connexion
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent itLogin = new Intent(getApplicationContext(),Connexion.class);
                itLogin.putExtra("caller","Accueil");
                startActivity(itLogin);
            }
        });

        //fenetre d'enregistrement
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent itSignin = new Intent(getApplicationContext(), Inscription.class);
                itSignin.putExtra("caller", "Accueil");
                startActivity(itSignin);
            }
        });

        final DBServer server = new DBServer(getApplicationContext());

//        Thread t = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                spots = server.find_spot("");
//            }});
//
//        t.start(); // spawn thread
//        try{
//            t.join();
//            if (spots != null){
//                GeoHash geohash;
//                for (Spot s :
//                        spots) {
//                    geohash = new GeoHash(Double.valueOf(s.getLatitude()), Double.valueOf(s.getLongitude()));
//                    geohash.encoder();
//                    server.set_spot_hash(s.getId(), geohash.getHash());
//                }
//            }
//        }catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }
}
