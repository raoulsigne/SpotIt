package techafrkix.work.com.spot.spotit;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
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

public class Inscription extends AppCompatActivity {

    EditText email, password, date;
    LoginButton fbSignin;
    Button signin;

    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_inscription);

        //recuperation des elements graphiques
        fbSignin = (LoginButton)findViewById(R.id.login_button);
        signin = (Button)findViewById(R.id.button);
        email = (EditText)findViewById(R.id.emailadress);
        password = (EditText)findViewById(R.id.password);
        date = (EditText)findViewById(R.id.editText);

        //traitement des actions des boutons
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        fbSignin.setReadPermissions("user_friends");
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Toast.makeText(Inscription.this, "connexion reussi",
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(Inscription.this, "connexion echoue",
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Toast.makeText(Inscription.this, "erreur survenue",
                                Toast.LENGTH_LONG).show();
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
