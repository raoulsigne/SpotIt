package techafrkix.work.com.spot.spotit;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;

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

        final String pass = BCrypt.hashpw("raoul", BCrypt.gensalt()).toString();
        final DBServer server = new DBServer(getApplicationContext());
        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    // server.register("raoul.signe@yahoo.fr", "raoul", pass, 1, "1991-02-01");
                    // Log.i("server", server.getUsers_by_pseudo("raoul").toString());
                    // server.enregistre_spot(new Spot("3.15", "18.2563", DetailSpot_New.V_MOI, "1542655844", "fqufgkertqkgfqk", "2016-05-23", 0, 131));
                    // Log.i("server", server.login("raoul.signe@yahoo.fr", 1, pass).toString());
                    // server.add_friend(131, 1);
                    // Log.i("server", server.getAllFriends(131).toString());
                    // Log.i("server", server.find_spot("fqufgkertqkgfqk").toString());
                    // server.add_comment(51, 131, "je quiff la plage grave particulieremment celle de kribi");
                    // Log.i("server", server.commentaires_spot(51).toString());
                    // String[] tab = {"plage", "conges", "fun"};
                    // server.add_tag(tab, 51);
                    // Log.i("server", server.tags_spot(51).toString());
                    // Log.i("server", server.find_spot_tag("fun", "").toString());
                    // Log.i("server", server.getUser_by_pseudo("raoul").toString());
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
