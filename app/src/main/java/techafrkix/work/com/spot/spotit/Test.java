package techafrkix.work.com.spot.spotit;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.Calendar;

public class Test extends AppCompatActivity implements AdapterCallback {

    EditText edtdate, edtpseudo;
    Context _context;
    LinearLayout liste;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        _context = getApplicationContext();

        Button valider = (Button) findViewById(R.id.btnValider);


        valider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Test.this);
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.test, null);
                dialogBuilder.setView(dialogView);

                edtpseudo = (EditText) dialogView.findViewById(R.id.edtpseudo);
                edtdate = (EditText) dialogView.findViewById(R.id.edtdate);
                Button btnvalider = (Button) dialogView.findViewById(R.id.btnvalider);

                edtdate.setOnTouchListener(new View.OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        int action = event.getActionMasked();
                        if (action == MotionEvent.ACTION_DOWN && action != MotionEvent.ACTION_CANCEL) {
                            DialogFragment newFragment = new DatePickerFragment();
                            newFragment.show(getSupportFragmentManager(), "datePicker");
                        }
                        return true;
                    }
                });

                AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.show();
            }
        });
    }

    @Override
    public void changedate(String date) {
        edtdate.setText(date);
    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        private AdapterCallback mAdapterCallback;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            try {
                this.mAdapterCallback = ((AdapterCallback) getActivity());
            } catch (ClassCastException e) {
                throw new ClassCastException("Activity must implement AdapterCallback.");
            }

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
            String mois = String.valueOf(month), jour = String.valueOf(day);
            if (month < 10) mois = "0"+month;
            if (day < 10) jour = "0"+day;
            mAdapterCallback.changedate(year + "-" + mois + "-" + jour);
        }
    }
}

interface AdapterCallback{
    public void changedate(String date);
}