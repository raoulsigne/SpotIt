package techafrkix.work.com.spot.spotit;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.mindrot.jbcrypt.BCrypt;

import techafrkix.work.com.spot.bd.Spot;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.DBServer;

public class Accueil extends AppCompatActivity {

    Button login, signin;

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
                startActivity(itSignin);
            }
        });
//
        final String pass = BCrypt.hashpw("raoul", BCrypt.gensalt()).toString();

        if (BCrypt.checkpw("wearedev16", pass))
            Log.i("bcrypt", "It matches");
        else
            Log.i("bcrypt", "It does not match");

        final DBServer server = new DBServer(getApplicationContext());

        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    // server.inscription("raoul.signe@yahoo.fr", "raoul", pass, 1);
                    // server.getUser_by_pseudo("raoul");
                    server.enregistre_spot(new Spot("3.15", "18.2563", DetailSpot_New.V_MOI, "1542655844", "fqufgkertqkgfqk", "2016-05-23"));
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }
}
