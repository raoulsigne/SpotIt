package techafrkix.work.com.spot.spotit;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;

import techafrkix.work.com.spot.bd.Utilisateur;
import techafrkix.work.com.spot.bd.UtilisateurDBAdapteur;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.DBServer;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.SessionManager;

public class Connexion extends AppCompatActivity {

    private static int USER_ID = 0;
    private AccessTokenTracker fbTracker;
    CallbackManager callbackManager;
    EditText email, password;

    UtilisateurDBAdapteur dbAdapteur;
    SQLiteDatabase db;
    String pseudo;

    // Session Manager Class
    SessionManager session;
    int cle;

    private static final String TAG = "Facebook";
    private String fbProfileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        setContentView(R.layout.activity_connexion);
        callbackManager = CallbackManager.Factory.create();

        // Session class instance
        session = new SessionManager(getApplicationContext());

        final Intent itwelcome = new Intent(this,Welcome.class);
        dbAdapteur = new UtilisateurDBAdapteur(getApplicationContext());

        //recuperation des elements de l'activité
        email = (EditText)findViewById(R.id.editText2);
        password = (EditText)findViewById(R.id.editText3);
        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        Button btnLogin = (Button)findViewById(R.id.btnConnexion);

//        fbTracker = new AccessTokenTracker() {
//            @Override
//            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
//                if (currentAccessToken != null) {
//                    Log.i("LOGINACTIVITY", "token tracker, current token valid");
//                    AccessToken token = AccessToken.getCurrentAccessToken();
//                } else {
//                    Log.i("LOGINACTIVITY", "token tracker, current token is null");
//                    Intent itaccueil = new Intent(getApplicationContext(), Accueil.class);
//                    itaccueil.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK); //add flags to spot all others activities
//                    finish();
//                    startActivity(itaccueil);
//                }
//            }
//        };
//
//        fbTracker.startTracking();

        String parent = getIntent().getExtras().getString("caller");
        if (isLoggedIn()) {
            if (parent.compareTo("Accueil") == 0) {
                startActivity(itwelcome);
            }
            else {
                session.logoutUser();
                LoginManager.getInstance().logOut();
            }
        }
        final EditText txtPseudo = new EditText(Connexion.this);
        txtPseudo.setHint("pseudo");
        final EditText txtDate = new EditText(Connexion.this);
        txtDate.setHint("date de naissance jj/mm/AAAA");
        final LinearLayout layout = new LinearLayout(Connexion.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(txtPseudo);
        layout.addView(txtDate);
        txtDate.setInputType(InputType.TYPE_CLASS_DATETIME);
        final DBServer server = new DBServer(getApplicationContext());
        //bout de code pour gérer le bouton de connexion via facebook à l'application
        loginButton.setReadPermissions("user_friends");
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(final LoginResult loginResult) {
                        final AlertDialog d = new AlertDialog.Builder(Connexion.this)
                                .setTitle("Information")
                                .setMessage("Entrez vos informations!")
                                .setView(layout)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {

                                    }
                                })
                                .show();
                        d.setOnShowListener(new DialogInterface.OnShowListener() {

                            @Override
                            public void onShow(DialogInterface dialog) {

                                Button b = d.getButton(AlertDialog.BUTTON_POSITIVE);
                                b.setOnClickListener(new View.OnClickListener() {

                                    @Override
                                    public void onClick(View view) {
                                        pseudo = txtPseudo.getText().toString();
                                        Thread thread = new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    Utilisateur utilisateur = server.getUser_by_pseudo(pseudo);
                                                    if (utilisateur != null){
                                                        txtPseudo.setTextColor(getResources().getColor(R.color.pink));
                                                    }
                                                    else {
                                                        //contacting facebook to get user email

                                                        //prepare fields with email
                                                        String[] requiredFields = new String[]{"email"};
                                                        Bundle parameters = new Bundle();
                                                        parameters.putString("fields", TextUtils.join(",", requiredFields));

                                                        GraphRequest requestEmail = new GraphRequest(loginResult.getAccessToken(), "me", parameters, null, new GraphRequest.Callback() {
                                                            @Override
                                                            public void onCompleted(GraphResponse response) {
                                                                if (response != null) {
                                                                    GraphRequest.GraphJSONObjectCallback callbackEmail = new GraphRequest.GraphJSONObjectCallback() {
                                                                        @Override
                                                                        public void onCompleted(JSONObject me, GraphResponse response) {
                                                                            if (response.getError() != null) {
                                                                                Log.d(TAG, "FB: cannot parse email");
                                                                            } else {
                                                                                final String email = me.optString("email");
                                                                                final Utilisateur user = new Utilisateur();
                                                                                user.setDate_naissance(txtDate.getText().toString());
                                                                                user.setEmail(email);
                                                                                final String pass = BCrypt.hashpw(email + Inscription._TO_CONCAT, BCrypt.gensalt()).toString();
                                                                                user.setPassword(pass);
                                                                                user.setPseudo(pseudo);

                                                                                Thread thread = new Thread(new Runnable() {
                                                                                    @Override
                                                                                    public void run() {
                                                                                        try {
                                                                                            Utilisateur u = server.login(user.getEmail(), DBServer.CONNEXION_FB, user.getEmail());
                                                                                            if (u == null)
                                                                                                cle = server.register(user.getEmail(), user.getPseudo(), pass, 1, user.getDate_naissance());
                                                                                            else
                                                                                                cle = u.getId();
                                                                                        } catch (Exception e) {
                                                                                            e.printStackTrace();
                                                                                        }
                                                                                    }
                                                                                });

                                                                                thread.start();
                                                                                // Creating user login session
                                                                                // For testing i am stroing name, email as follow
                                                                                // Use user real data
                                                                                session.createLoginSession(pseudo, user.getEmail(), (int) cle);

                                                                                Log.i(TAG, email);
                                                                            }
                                                                        }
                                                                    };

                                                                    callbackEmail.onCompleted(response.getJSONObject(), response);
                                                                }
                                                            }
                                                        });

                                                        requestEmail.executeAsync();

                                                        startActivity(itwelcome);
                                                    }
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                        thread.start();
                                    }
                                });
                            }
                        });
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(Connexion.this, "connexion echoue",
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Toast.makeText(Connexion.this, "erreur survenue",
                                Toast.LENGTH_LONG).show();
                    }
                });

        // gestion du bouton login
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ecrire le code correspondant au traitement à affectuer sur le click du bouton login
                Utilisateur utilisateur = new Utilisateur();
                if (email.getText().toString() != " " & password.getText().toString() != ""){
                    db = dbAdapteur.open();
                    utilisateur = dbAdapteur.getUtilisateur(email.getText().toString(),password.getText().toString());
                    if (utilisateur != null) {
                        Log.i("BD", "utilisateur connecté");

                        // Creating user login session
                        // For testing i am stroing name, email as follow
                        // Use user real data
                        session.createLoginSession("name", utilisateur.getEmail(), utilisateur.getId());

                        startActivity(itwelcome);
                    } else {
                        Log.i("BD", "utilisateur non connecté");
                        password.setText("");
                        Toast.makeText(getApplicationContext(),"Echec vérifier les informations entrées", Toast.LENGTH_SHORT).show();
                    }
                    db.close();
                }
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

    public boolean isLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }
}
