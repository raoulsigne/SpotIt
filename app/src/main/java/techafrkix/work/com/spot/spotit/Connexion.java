package techafrkix.work.com.spot.spotit;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
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
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.util.HashMap;

import techafrkix.work.com.spot.bd.Utilisateur;
import techafrkix.work.com.spot.bd.UtilisateurDBAdapteur;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.DBServer;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.SessionManager;

public class Connexion extends AppCompatActivity {

    private static int USER_ID = 0;
    private AccessTokenTracker fbTracker;
    CallbackManager callbackManager;
    EditText email, password;

    GoogleCloudMessaging gcm;
    String regId;

    UtilisateurDBAdapteur dbAdapteur;
    SQLiteDatabase db;
    String pseudo, sdate;

    // Session Manager Class
    SessionManager session;
    DBServer server;
    private HashMap<String, String> profile;

    private static final String TAG = "Facebook";
    private String fbProfileName;

    private Utilisateur utilisateur;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        setContentView(R.layout.activity_connexion);
        callbackManager = CallbackManager.Factory.create();

        // Session class instance
        session = new SessionManager(getApplicationContext());
        server = new DBServer(getApplicationContext());
        profile = new HashMap<>();
        profile = session.getUserDetails();
        Log.i(TAG, "android id = " + regId);

        //recuperation des elements de l'activité
        email = (EditText)findViewById(R.id.editText2);
        password = (EditText)findViewById(R.id.editText3);
        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        Button btnLogin = (Button)findViewById(R.id.btnConnexion);

        final Intent itwelcome = new Intent(this,Welcome.class);
        dbAdapteur = new UtilisateurDBAdapteur(getApplicationContext());

        String parent = getIntent().getExtras().getString("caller");

        registerDevice_2();

        if (isLoggedIn()) {
            if (parent.compareTo("Accueil") == 0) {
                registerDevice();
                startActivity(itwelcome);
                finish();
            }
            else {
                session.logoutUser();
                LoginManager.getInstance().logOut();
            }
        }

        final EditText txtPseudo = new EditText(Connexion.this);
        txtPseudo.setHint("Pseudo");
        final EditText txtDate = new EditText(Connexion.this);
        txtDate.setHint("Date de naissance");
        txtDate.setInputType(InputType.TYPE_CLASS_DATETIME);
        final Button btnValider = new Button(Connexion.this);
        btnValider.setText("Valider");
        btnValider.setHeight(20);
        btnValider.setBackground(getResources().getDrawable(R.drawable.button_blue));
        final LinearLayout layout = new LinearLayout(Connexion.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(10, 0, 10, 0);
        layout.addView(txtPseudo);
        layout.addView(txtDate);
        layout.addView(btnValider);
        //bout de code pour gérer le bouton de connexion via facebook à l'application
        loginButton.setReadPermissions("user_friends");
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(final LoginResult loginResult) {
                        Toast.makeText(Connexion.this, "connexion reussi",
                                Toast.LENGTH_LONG).show();

                        //contacting facebook to get user email
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
                                                Log.i(TAG, "FB: cannot parse email");
                                            }
                                            else
                                            {
                                                final String email = me.optString("email");  // recupération de l'email via facebook api
                                                Thread t1 = new Thread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        utilisateur = server.login(email, DBServer.CONNEXION_FB, " "); // tester si l'utilisateur a déjà été enrgistré
                                                    }});

                                                t1.start(); // spawn thread
                                                // wait for thread to finish
                                                try {
                                                    t1.join();
                                                    if (utilisateur != null){
                                                        // Creating user login session
                                                        // For testing i am stroing name, email as follow
                                                        // Use user real data
                                                        session.createLoginSession(utilisateur.getPseudo(), utilisateur.getEmail(), utilisateur.getId(),
                                                                utilisateur.getNbspot(), utilisateur.getNbrespot(), 0, utilisateur.getPhoto(), DBServer.CONNEXION_FB);

                                                        if (regId == utilisateur.getAndroidid())
                                                            session.storeRegistrationId(utilisateur.getAndroidid());
                                                        else if (regId != null & regId != " "){
                                                            session.storeRegistrationId(regId);
                                                            Thread t2 = new Thread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    server.set_device_id(utilisateur.getId(), regId);
                                                                }});

                                                            t2.start(); // spawn thread
                                                        }

                                                        startActivity(itwelcome); // déjà enregistré on démarre l'activité Welcome
                                                        finish();
                                                    }
                                                    else { // on demande à l'utilisateur d'entrer ses identifiants pour l'en créer un compte
                                                        new AlertDialog.Builder(Connexion.this)
                                                                .setTitle("Vos Informations")
                                                                .setView(layout)
                                                                .show();

                                                        btnValider.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                pseudo = txtPseudo.getText().toString();
                                                                sdate = txtDate.getText().toString();
                                                                utilisateur = new Utilisateur();



                                                                Thread t2 = new Thread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        utilisateur = server.getUser_by_pseudo(pseudo); // on teste si le pseudo est déjà utilisé dans l'application
                                                                    }});

                                                                t2.start(); // spawn thread

                                                                // wait for thread to finish
                                                                try {
                                                                    t2.join();
                                                                    if (utilisateur != null){
                                                                        Log.i("Connexion", "Utilisateur avec " + pseudo + " existant");
                                                                        txtPseudo.setTextColor(getResources().getColor(R.color.pink));
                                                                        txtPseudo.setText(txtPseudo.getText().toString() + " existant!");
                                                                    }
                                                                    else {
                                                                        final Utilisateur user = new Utilisateur();
                                                                        user.setDate_naissance(txtDate.getText().toString());
                                                                        user.setEmail(email);
                                                                        String pass = BCrypt.hashpw(email+Inscription._TO_CONCAT, BCrypt.gensalt()).toString();
                                                                        user.setPassword(pass);
                                                                        user.setPseudo(pseudo);
                                                                        Thread t3 = new Thread(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                USER_ID = server.register(user.getEmail(), user.getPseudo(),
                                                                                        user.getPassword(), DBServer.CONNEXION_FB, user.getDate_naissance(), regId);

                                                                            }});

                                                                        t3.start(); // spawn thread
                                                                        try{
                                                                            t3.join();
                                                                            if (USER_ID != -1) {
                                                                                Log.i("BD", "nouvel utilisateur enregistré");
                                                                                // Creating user login session
                                                                                // For testing i am stroing name, email as follow
                                                                                // Use user real data
                                                                                session.createLoginSession(pseudo, user.getEmail(), USER_ID);

                                                                                session.createLoginSession(user.getPseudo(), user.getEmail(), USER_ID,
                                                                                        0, 0, 0, "", DBServer.CONNEXION_FB);

                                                                                startActivity(itwelcome);
                                                                                finish();
                                                                            } else {
                                                                                Log.i("BD", "nouvel utilisateur non enregistré");
                                                                            }
                                                                            Log.i(TAG, email);
                                                                        }catch (InterruptedException e) {
                                                                            e.printStackTrace();
                                                                        }
                                                                    }
                                                                } catch (InterruptedException e) {
                                                                    e.printStackTrace();
                                                                }
                                                            }
                                                        });
                                                    }
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                }
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
                if (email.getText().toString() != " " & password.getText().toString() != ""){
                    Thread t1 = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            utilisateur = server.login(email.getText().toString(), DBServer.CONNEXION_NORMAL, password.getText().toString());
                        }});

                    t1.start(); // spawn thread
                    // wait for thread to finish
                    try {
                        t1.join();

                        if (utilisateur != null) {
                            Log.i("BD", "utilisateur connecté");

                            // Creating user login session
                            // For testing i am stroing name, email as follow
                            // Use user real data
                            session.createLoginSession(utilisateur.getPseudo(), utilisateur.getEmail(), utilisateur.getId(),
                                    utilisateur.getNbspot(), utilisateur.getNbrespot(), 0, utilisateur.getPhoto(), DBServer.CONNEXION_NORMAL);
                            session.storeRegistrationId(utilisateur.getAndroidid());

                            startActivity(itwelcome);
                            finish();
                        } else {
                            Log.i("BD", "utilisateur non connecté");
                            password.setText("");
                            Toast.makeText(getApplicationContext(),"Echec vérifier les informations entrées", Toast.LENGTH_SHORT).show();
                        }
                    }catch (InterruptedException e){
                        Log.e(TAG, e.getMessage());
                    }
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
        if (session.isLogin())
            return true;
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }

    /** * Cette méthode permet l'enregistrement du terminal */
    private void registerDevice() {
        Log.i("GCM: ", "Device registration");
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    }
                    regId = gcm.register(getApplicationContext().getResources().getString(R.string.GCM_PROJECT_NUMBER_NUMBER));
                    Log.i("GCM: ", regId);
                    if (!TextUtils.equals(regId, profile.get(SessionManager.KEY_REGISTRATION_ID))){
                        Log.i("GCM: ", "Differentes valeurs de android id ");
                        server.set_device_id(Integer.valueOf(profile.get(SessionManager.KEY_ID)), regId);
                        session.storeRegistrationId(regId);
                    }else
                        Log.i("GCM: ", "Identiques valeurs de android id");

                    Log.i("GCM: ", "1 " + regId);
                    Log.i("GCM: ", "2 " + profile.get(SessionManager.KEY_REGISTRATION_ID));

                    msg = "Terminal enregistré, register ID=" + regId;
                    // On enregistre le registerId dans les SharedPreferences
                    Log.i("GCM: ", msg);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    Log.e("GCM Error: ", msg);
                }
                return msg;
            }
        }.execute(null, null, null);
    }

    /** * Cette méthode permet l'enregistrement du terminal */
    private void registerDevice_2() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    }
                    regId = gcm.register(getApplicationContext().getResources().getString(R.string.GCM_PROJECT_NUMBER_NUMBER));
                    msg = "Terminal enregistré, register ID=" + regId;
                    // On enregistre le registerId dans les SharedPreferences
                    Log.i("GCM: ", msg);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    Log.e("GCM Error: ", msg);
                }
                return msg;
            }
        }.execute(null, null, null);
    }
}