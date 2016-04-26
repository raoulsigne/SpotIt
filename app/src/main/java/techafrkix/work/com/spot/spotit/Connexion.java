package techafrkix.work.com.spot.spotit;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.io.Console;

public class Connexion extends AppCompatActivity {

    CallbackManager callbackManager;
    EditText email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        setContentView(R.layout.activity_connexion);
        callbackManager = CallbackManager.Factory.create();

        final Intent mapintent = new Intent(this,MainActivity.class);

        //recuperation des elements de l'activité
        email = (EditText)findViewById(R.id.editText2);
        password = (EditText)findViewById(R.id.editText3);
        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        Button btnLogin = (Button)findViewById(R.id.btnConnexion);

        //bout de code pour gérer le bouton de connexion via facebook à l'application
        loginButton.setReadPermissions("user_friends");
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Toast.makeText(Connexion.this, "connexion reussi",
                                Toast.LENGTH_LONG).show();
                        startActivity(mapintent);
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(Connexion.this, "connexion echoue",
                                Toast.LENGTH_LONG).show();
                        startActivity(mapintent);
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Toast.makeText(Connexion.this, "erreur survenue",
                                Toast.LENGTH_LONG).show();
                        startActivity(mapintent);
                    }
                });

        // gestion du bouton login
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ecrire le code correspondant au traitement à affectuer sur le click du bouton login

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
