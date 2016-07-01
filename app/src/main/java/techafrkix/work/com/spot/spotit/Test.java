package techafrkix.work.com.spot.spotit;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import java.util.HashMap;
import techafrkix.work.com.spot.bd.Notification;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.SessionManager;

public class Test extends AppCompatActivity {

    EditText editText;
    private SessionManager session;
    private HashMap<String, String> profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        // Session class instance
        session = new SessionManager(getApplicationContext());
        profile = new HashMap<>();
        profile = session.getUserDetails();

        final Notification messageAEnvoyer = new Notification(1, 12, 131, "Nelson has comment your spot", "30 juin 2016 à 12h00");

        editText = (EditText)findViewById(R.id.editText5);

        Button button = (Button)findViewById(R.id.button3);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String chaine = editText.getText().toString();
                Message androidMessage = new Message.Builder()
                        .collapseKey("1")
                        .delayWhileIdle(true)
                        .timeToLive(5)
                        .addData(GcmIntentService.MESSAGE_ID, String.valueOf(messageAEnvoyer.getId()))
                        .addData(GcmIntentService.MESSAGE_TITRE, "Notification")
                        .addData(GcmIntentService.MESSAGE_DATE_CREATION, messageAEnvoyer.getCreated())
                        .addData(GcmIntentService.MESSAGE_TEXTE, messageAEnvoyer.getMessage())
                        .build();

                Sender sender = new Sender(getResources().getString(R.string.GCM_API_KEY)); // l'apikey qui a été générée précédemment
                Result resultat = null;
                try {
                    resultat = sender.send(androidMessage, profile.get(SessionManager.KEY_REGISTRATION_ID), 3);
                }catch (Exception e){

                }
                Intent intent = new Intent(getApplicationContext(), Accueil.class);
                startActivity(intent);
            }
        });
    }

    public class MyTextWatcher implements TextWatcher {
        private EditText et;
        private char lastchar;
        private CharSequence chaine;

        // Pass the EditText instance to TextWatcher by constructor
        public MyTextWatcher(EditText et) {
            this.et = et;
            this.lastchar = ' ';
            this.chaine = "";
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            // Unregister self before setText
            et.removeTextChangedListener(this);
            chaine = et.getText().toString();
            if (!TextUtils.isEmpty(chaine)) {
                if (!chaine.equals("#")) {
                    lastchar = chaine.charAt(chaine.length() - 1);

                    Log.i("test", chaine.toString() + " " + lastchar);
                    if (lastchar == '#') {
                        final SpannableStringBuilder sb = new SpannableStringBuilder(chaine.subSequence(0, chaine.length() - 1));

                        // Span to set text color to some RGB value
                        final ForegroundColorSpan fcs = new ForegroundColorSpan(getResources().getColor(R.color.mainblue));
                        // Span to make text bold
                        final StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD);

                        // Set the text color for first 4 characters
                        sb.setSpan(fcs, 0, sb.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

                        // make them also bold
                        sb.setSpan(bss, 0, sb.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

                        et.setText(sb + " ");

                        //et.setText(chaine.subSequence(0, chaine.length() - 1) + " ");
                        et.setSelection(et.getText().length());
                    }
                }else
                    et.setText("");
            }
            // Re-register self after setText
            et.addTextChangedListener(this);
        }

    }
}
