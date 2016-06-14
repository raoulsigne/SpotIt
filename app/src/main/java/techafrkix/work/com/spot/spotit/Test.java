package techafrkix.work.com.spot.spotit;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
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

        Button button = (Button)findViewById(R.id.button3);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String chaine = editText.getText().toString();
                String[] tab = chaine.split(" ");
                for (String s:
                     tab) {
                    Log.i("test", s);
                }
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
