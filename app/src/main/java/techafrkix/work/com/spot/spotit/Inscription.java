package techafrkix.work.com.spot.spotit;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
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
import com.facebook.Profile;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import techafrkix.work.com.spot.bd.Utilisateur;
import techafrkix.work.com.spot.bd.UtilisateurDBAdapteur;

public class Inscription extends AppCompatActivity {

    EditText email, password, date;
    LoginButton fbSignin;
    Button signin;
    ProgressDialog progress;
    private String facebook_id,f_name, m_name, l_name, gender, profile_image, full_name, email_id;

    UtilisateurDBAdapteur dbAdapteur;
    SQLiteDatabase db;
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

        fbSignin.setReadPermissions(Arrays.asList(
                "public_profile", "email", "user_birthday", "user_friends"));
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Toast.makeText(Inscription.this, "connexion reussi",
                                Toast.LENGTH_LONG).show();

                        progress.show();
                        Profile profile = Profile.getCurrentProfile();
                        if (profile != null) {
                            facebook_id = profile.getId();
                            f_name = profile.getFirstName();
                            m_name = profile.getMiddleName();
                            l_name = profile.getLastName();
                            full_name = profile.getName();
                            profile_image = profile.getProfilePictureUri(400, 400).toString();
                            Log.i("FB", profile.toString());
                        }
                        Toast.makeText(Inscription.this, "Wait...", Toast.LENGTH_SHORT).show();
                        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject object, GraphResponse response) {
                                        try {
                                            email_id = object.getString("email");
                                            gender = object.getString("gender");
                                            Log.i("FB", email_id + " " + gender);
                                            //Record user informations into database
                                            Intent i = new Intent();
                                            i.putExtra("type", "facebook");
                                            i.putExtra("facebook_id", facebook_id);
                                            i.putExtra("f_name", f_name);
                                            i.putExtra("m_name", m_name);
                                            i.putExtra("l_name", l_name);
                                            i.putExtra("full_name", full_name);
                                            i.putExtra("profile_image", profile_image);
                                            i.putExtra("email_id", email_id);
                                            i.putExtra("gender", gender);

                                            progress.dismiss();
                                            startActivity(mainintent);
                                            finish();
                                        } catch (JSONException e) {
                                            // TODO Auto-generated catch block
                                            //  e.printStackTrace();
                                        }

                                    }

                                });
                        request.executeAsync();
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(Inscription.this, "connexion echoue",
                                Toast.LENGTH_LONG).show();
                        progress.dismiss();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Toast.makeText(Inscription.this, "erreur survenue",
                                Toast.LENGTH_LONG).show();
                        progress.dismiss();
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
