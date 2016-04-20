package techafrkix.work.com.spot.techafrkix.work.com.spot.test;

import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabSelectedListener;

import techafrkix.work.com.spot.spotit.R;

public class ThreeButtonsActivity extends AppCompatActivity {

    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_three_buttons);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.three_buttons_activity);

        BottomBar bottomBar = BottomBar.attach(this, savedInstanceState);
        bottomBar.setItemsFromMenu(R.menu.main_menu, new OnMenuTabSelectedListener() {
            @Override
            public void onMenuItemSelected(int itemId) {
                switch (itemId) {
                    case R.id.accueil_item:
                        Snackbar.make(coordinatorLayout, "accueil Item Selected", Snackbar.LENGTH_LONG).show();
                        break;
                    case R.id.spots_item:
                        Snackbar.make(coordinatorLayout, "list of spots Item Selected", Snackbar.LENGTH_LONG).show();
                        break;
                    case R.id.spot_item:
                        Snackbar.make(coordinatorLayout, "new spot Item Selected", Snackbar.LENGTH_LONG).show();
                        break;
                    case R.id.deconnection_item:
                        Snackbar.make(coordinatorLayout, "disconnect Item Selected", Snackbar.LENGTH_LONG).show();
                        break;
                }
            }
        });

        // Set the color for the active tab. Ignored on mobile when there are more than three tabs.
        bottomBar.setActiveTabColor("#C2185B");

        // Use the dark theme. Ignored on mobile when there are more than three tabs.
        //bottomBar.useDarkTheme(true);

        // Use custom text appearance in tab titles.
        //bottomBar.setTextAppearance(R.style.MyTextAppearance);

        // Use custom typeface that's located at the "/src/main/assets" directory. If using with
        // custom text appearance, set the text appearance first.
        //bottomBar.setTypeFace("MyFont.ttf");
    }
}
