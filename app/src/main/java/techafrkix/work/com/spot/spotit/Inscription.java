package techafrkix.work.com.spot.spotit;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
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
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.Profile;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;

import techafrkix.work.com.spot.bd.Utilisateur;
import techafrkix.work.com.spot.bd.UtilisateurDBAdapteur;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.DBServer;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.SessionManager;

public class Inscription extends AppCompatActivity {

    public static  final  String _TO_CONCAT = "cs457syu89iuer8poier787";
    protected EditText pseudo, email, password, date;
    Switch switch_pwd;
    LoginButton fbSignin;
    Button signin;
    ProgressDialog progress;

    GoogleCloudMessaging gcm;
    String regId;

    // Session Manager Class
    SessionManager session;
    DBServer server;
    UtilisateurDBAdapteur dbAdapteur;
    SQLiteDatabase db;
    CallbackManager callbackManager;
    private static final String TAG = "Facebook";

    private String fbUserID;
    private String fbAuthToken;
    private String fbProfileName;
    private ProfileTracker profileTracker;
    private AccessTokenTracker accessTokenTracker;
    int USER_ID;
    private Utilisateur utilisateur;
    String s_pseudo, sdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_inscription);

        // Session class instance
        session = new SessionManager(getApplicationContext());
        server = new DBServer(getApplicationContext());
        final Intent itmain = new Intent(this,MainActivity.class);
        registerDevice();
        Log.i(TAG, "android id = " + regId);

        //recuperation des elements graphiques
        fbSignin = (LoginButton)findViewById(R.id.login_button);
        signin = (Button)findViewById(R.id.button);
        pseudo = (EditText)findViewById(R.id.pseudo);
        email = (EditText)findViewById(R.id.emailadress);
        password = (EditText)findViewById(R.id.password);
        date = (EditText)findViewById(R.id.editText);
        switch_pwd = (Switch)findViewById(R.id.switch_pwd);

        switch_pwd.setChecked(false);
        password.setTransformationMethod(new PasswordTransformationMethod());

        dbAdapteur = new UtilisateurDBAdapteur(getApplicationContext());
        final Intent mainintent = new Intent(this,MainActivity.class);

        progress=new ProgressDialog(Inscription.this);
        progress.setMessage("Please wait response from facebook...");
        progress.setIndeterminate(false);
        progress.setCancelable(false);
        switch_pwd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    password.setTransformationMethod(null);
                    switch_pwd.setText("Hide");
                } else {
                    // The toggle is disabled
                    password.setTransformationMethod(new PasswordTransformationMethod());
                    switch_pwd.setText("Show");
                }
            }
        });

        date.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getActionMasked();
                if (action == MotionEvent.ACTION_DOWN && action != MotionEvent.ACTION_CANCEL) {
                    DialogFragment newFragment = new DatePickerFragment();
                    newFragment.show(getSupportFragmentManager(), "datePicker");
                }
                return true;
            }
        });
        pseudo.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getActionMasked();
                if (action == MotionEvent.ACTION_UP && action!=MotionEvent.ACTION_CANCEL) {
                    if (pseudo.getText().toString() == "")
                        pseudo.setTextColor(getResources().getColor(R.color.noir));
                }
                pseudo.requestFocus();  // request focus
                //open keyboard inside edittext pseudo
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(pseudo, InputMethodManager.SHOW_IMPLICIT);
                return true;
            }
        });

        //traitement des actions des boutons
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Utilisateur user = new Utilisateur();
                if (pseudo.getText().toString() != "" & email.getText().toString() != "" & password.getText().toString() != "" &
                        date.getText().toString() != "") {
                    user.setPseudo(pseudo.getText().toString());
                    user.setDate_naissance(date.getText().toString());
                    user.setEmail(email.getText().toString());
                    user.setPassword(password.getText().toString());

                    Thread t1 = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            utilisateur = server.getUser_by_pseudo(user.getPseudo());
                        }});

                    t1.start(); // spawn thread
                    // wait for thread to finish
                    try {
                        t1.join();
                        if (utilisateur == null) {
                            Thread t = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    String pass = BCrypt.hashpw(user.getPassword()+_TO_CONCAT, BCrypt.gensalt(12)).toString();
                                    USER_ID = server.register(user.getEmail(), user.getPseudo(),
                                            pass, DBServer.CONNEXION_NORMAL, user.getDate_naissance(), regId);
                                }});

                            t.start(); // spawn thread
                            try{
                                t.join();
                                if (USER_ID != -1) {
                                    Log.i("BD", "nouvel utilisateur enregistré");
                                    // Creating user login session
                                    // For testing i am stroing name, email as follow
                                    // Use user real data
                                    session.createLoginSession(user.getPseudo(), user.getEmail(), USER_ID);
                                    startActivity(itmain);
                                } else {
                                    Log.i("BD", "nouvel utilisateur non enregistré");
                                    Toast.makeText(getApplicationContext(),"Echec de l'enregistrement! vérifier la connexion",Toast.LENGTH_SHORT).show();
                                }
                            }catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }else{
                            pseudo.setText(pseudo.getText().toString() + " existe déjà!");
                            pseudo.setTextColor(getResources().getColor(R.color.pink));
                        }

                    }catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
        String parent = getIntent().getExtras().getString("caller");
        if (parent.compareTo("Main") == 0) {
            accessTokenTracker = new AccessTokenTracker() {
                @Override
                protected void onCurrentAccessTokenChanged(
                        AccessToken oldAccessToken,
                        AccessToken currentAccessToken) {
                    fbAuthToken = currentAccessToken.getToken();
                    fbUserID = currentAccessToken.getUserId();

                    Log.i(TAG, "User id: " + fbUserID);
                    Log.i(TAG, "Access token tracker: " + currentAccessToken.toString());

                }
            };
            profileTracker = new ProfileTracker() {
                @Override
                protected void onCurrentProfileChanged(
                        Profile oldProfile,
                        Profile currentProfile) {
                    fbProfileName = currentProfile.getName();

                    Log.i(TAG, "User name: " + fbProfileName + " " + currentProfile.toString());
                }
            };
        }

        final EditText txtPseudo = new EditText(Inscription.this);
        txtPseudo.setHint("Pseudo");
        final EditText txtDate = new EditText(Inscription.this);
        txtDate.setHint("Date de naissance");
        txtDate.setInputType(InputType.TYPE_CLASS_DATETIME);
        final Button btnValider = new Button(Inscription.this);
        btnValider.setText("Valider");
        btnValider.setHeight(20);
        btnValider.setBackground(getResources().getDrawable(R.drawable.button_blue));
        final LinearLayout layout = new LinearLayout(Inscription.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(10, 0, 10, 0);
        layout.addView(txtPseudo);
        layout.addView(txtDate);
        layout.addView(btnValider);

        fbSignin.setReadPermissions(Arrays.asList(
                "public_profile", "email", "user_birthday", "user_friends"));
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(final LoginResult loginResult) {
                        Toast.makeText(Inscription.this, "connexion reussi",
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
                                                Log.d(TAG, "FB: cannot parse email");
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
                                                                utilisateur.getNbspot(), utilisateur.getNbrespot(), 0, utilisateur.getPhoto());
                                                        session.storeRegistrationId(utilisateur.getAndroidid());

                                                        startActivity(itmain); // déjà enregistré on démarre l'activité Welcome
                                                    }
                                                    else { // on demande à l'utilisateur d'entrer ses identifiants pour l'en créer un compte
                                                        new AlertDialog.Builder(Inscription.this)
                                                                .setTitle("Vos Information")
                                                                .setView(layout)
                                                                .show();

                                                        btnValider.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                s_pseudo = txtPseudo.getText().toString();
                                                                sdate = txtDate.getText().toString();
                                                                utilisateur = new Utilisateur();



                                                                Thread t2 = new Thread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        utilisateur = server.getUser_by_pseudo(s_pseudo); // on teste si le pseudo est déjà utilisé dans l'application
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
                                                                        user.setPseudo(s_pseudo);
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
                                                                                session.createLoginSession(utilisateur.getPseudo(), utilisateur.getEmail(), USER_ID);
                                                                                session.registerGCM();

                                                                                startActivity(itmain);
                                                                            } else
                                                                                Log.i("BD", "nouvel utilisateur non enregistré");
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

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            EditText date = (EditText)getActivity().findViewById(R.id.editText);
            String mois = String.valueOf(month), jour = String.valueOf(day);
            if (month < 10) mois = "0"+month;
            if (day < 10) jour = "0"+day;
            date.setText(year + "-" + mois + "-" + jour);
        }
    }

    /** * Cette méthode permet l'enregistrement du terminal */
    private void registerDevice() {
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
