package techafrkix.work.com.spot.spotit;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.facebook.login.LoginManager;

import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;

import techafrkix.work.com.spot.bd.Commentaire;
import techafrkix.work.com.spot.bd.Spot;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.DBServer;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.GeoHash;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.SessionManager;

public class Accueil extends AppCompatActivity {

    Button login, signin;
    ArrayList<Spot> spots;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accueil);

        session = new SessionManager(getApplicationContext());
        if (session.isLogin()){
            startActivity(new Intent(Accueil.this, Welcome.class));
            finish();
        }

        //recuperation des boutons à partir du xml
        login = (Button)findViewById(R.id.btnLogin);
        signin = (Button)findViewById(R.id.btnSignin);

        //actions des boutons qui consistent à ouvrir les activités respectivent
        try {
            String parent = getIntent().getExtras().getString("caller");
            if (parent.compareTo("Main") == 0) {
                LoginManager.getInstance().logOut();
            }
        }catch (Exception e){
            Log.e("error", e.getMessage());
        }

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
    }
}
