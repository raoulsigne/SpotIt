package techafrkix.work.com.spot.spotit;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

public class Test extends AppCompatActivity {

    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        editText = (EditText)findViewById(R.id.editText5);
        editText.addTextChangedListener(new MyTextWatcher(editText));

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
            chaine = s.subSequence(0, count);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            lastchar = s.charAt(count - 1);
        }

        @Override
        public void afterTextChanged(Editable s) {
            // Unregister self before setText
            et.removeTextChangedListener(this);

            if (lastchar == '#'){
                Log.i("test", chaine.toString());
                et.setText(chaine + " ");
                et.setSelection(et.getText().length());
            }

            // Re-register self after setText
            et.addTextChangedListener(this);
        }

    }
}
