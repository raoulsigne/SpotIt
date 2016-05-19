package techafrkix.work.com.spot.spotit;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.Profile;

import org.json.JSONException;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Arrays;

import techafrkix.work.com.spot.bd.Utilisateur;
import techafrkix.work.com.spot.bd.UtilisateurDBAdapteur;

public class Inscription extends AppCompatActivity {

    public static  final  String _TO_CONCAT = "cs457syu89iuer8poier787";
    EditText email, password, date;
    LoginButton fbSignin;
    Button signin;
    ProgressDialog progress;

    UtilisateurDBAdapteur dbAdapteur;
    SQLiteDatabase db;
    CallbackManager callbackManager;
    private static final String TAG = "Facebook";

    private String fbUserID;
    private String fbAuthToken;
    private String fbProfileName;
    private ProfileTracker profileTracker;
    private AccessTokenTracker accessTokenTracker;

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

        dbAdapteur = new UtilisateurDBAdapteur(getApplicationContext());
        final Intent mainintent = new Intent(this,MainActivity.class);

        progress=new ProgressDialog(Inscription.this);
        progress.setMessage("Please wait response from facebook...");
        progress.setIndeterminate(false);
        progress.setCancelable(false);

        //traitement des actions des boutons
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utilisateur user = new Utilisateur();
                if (email.getText().toString() != "" & password.getText().toString() != "" & date.getText().toString() != "") {
                    user.setDate_naissance(date.getText().toString());
                    user.setEmail(email.getText().toString());
                    user.setPassword(password.getText().toString());
                    db = dbAdapteur.open();
                    long cle = dbAdapteur.insertUtilisateur(user);
                    if (cle != -1) {
                        Log.i("BD", "nouvel utilisateur enregistré");
                        startActivity(mainintent);
                    } else
                        Log.i("BD", "nouvel utilisateur non enregistré");
                    db.close();
                }
            }
        });

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {
                fbAuthToken = currentAccessToken.getToken();
                fbUserID = currentAccessToken.getUserId();

                Log.d(TAG, "User id: " + fbUserID);
                Log.d(TAG, "Access token tracker: " + currentAccessToken.toString());

            }
        };
        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(
                    Profile oldProfile,
                    Profile currentProfile) {
                fbProfileName = currentProfile.getName();

                Log.d(TAG, "User name: " + fbProfileName + " " + currentProfile.toString());
            }
        };

        fbSignin.setReadPermissions(Arrays.asList(
                "public_profile", "email", "user_birthday", "user_friends"));
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Toast.makeText(Inscription.this, "connexion reussi",
                                Toast.LENGTH_LONG).show();
                        final String token = loginResult.getAccessToken().getToken();

                        //prepare fields with email
                        String[] requiredFields = new String[]{"email"};
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", TextUtils.join(",", requiredFields));

                        GraphRequest requestEmail = new GraphRequest(loginResult.getAccessToken(), "me", parameters, null, new GraphRequest.Callback()
                        {
                            @Override
                            public void onCompleted (GraphResponse response)
                            {
                                if (response != null)
                                {
                                    GraphRequest.GraphJSONObjectCallback callbackEmail = new GraphRequest.GraphJSONObjectCallback()
                                    {
                                        @Override
                                        public void onCompleted (JSONObject me, GraphResponse response)
                                        {
                                            if (response.getError() != null)
                                            {
                                                Log.d(TAG, "FB: cannot parse email");
                                            }
                                            else
                                            {
                                                String email = me.optString("email");
                                                Utilisateur user = new Utilisateur();
                                                user.setDate_naissance("");
                                                String pass = BCrypt.hashpw(email + Inscription._TO_CONCAT, BCrypt.gensalt()).toString();
                                                user.setEmail(pass);
                                                user.setPassword(fbProfileName.replaceAll("\\s","").toLowerCase());
                                                db = dbAdapteur.open();
                                                long cle = dbAdapteur.insertUtilisateur(user);
                                                if (cle != -1) {
                                                    Log.i("BD", "nouvel utilisateur enregistré");
                                                    startActivity(mainintent);
                                                } else
                                                    Log.i("BD", "nouvel utilisateur non enregistré");
                                                db.close();
                                                Log.i(TAG, email);
                                                startActivity(mainintent);
                                                Toast.makeText(getApplicationContext(),"Vos informations ont été enregistrées",Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    };

                                    callbackEmail.onCompleted(response.getJSONObject(), response);
                                }
                            }
                        });

                        requestEmail.executeAsync();
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
