package techafrkix.work.com.spot.spotit;

import android.content.Context;
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
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.TypefaceProvider;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import java.util.HashMap;
import techafrkix.work.com.spot.bd.Notification;
import techafrkix.work.com.spot.techafrkix.work.com.spot.utils.SessionManager;

public class Test extends AppCompatActivity {

    EditText editText;
    Context _context;
    LinearLayout liste;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        _context = getApplicationContext();

        editText = (EditText) findViewById(R.id.editText5);
        liste = (LinearLayout) findViewById(R.id.listes);

        Button button = (Button) findViewById(R.id.button3);
        Button valider = (Button) findViewById(R.id.btnValider);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String chaine = editText.getText().toString();
                View child = getLayoutInflater().inflate(R.layout.btntag, null);
                ((Button)child.findViewById(R.id.button)).setText(chaine);
                liste.addView(child);

                child.requestFocus(); //change the position of the visible element inside a list

                child.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        liste.removeView(view); //remove the view element inside the linearlayout
                    }
                });
            }
        });

        valider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int childcount = liste.getChildCount();
                for (int i=0; i < childcount; i++){
                    View v = liste.getChildAt(i);
                    Log.i("test", ((Button)v.findViewById(R.id.button)).getText().toString());
                }
            }
        });
    }
}
