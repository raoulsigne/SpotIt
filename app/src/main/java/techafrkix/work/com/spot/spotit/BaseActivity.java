package techafrkix.work.com.spot.spotit;

import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabSelectedListener;

public class BaseActivity extends AppCompatActivity {

    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.base_activity);

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
    }
}
