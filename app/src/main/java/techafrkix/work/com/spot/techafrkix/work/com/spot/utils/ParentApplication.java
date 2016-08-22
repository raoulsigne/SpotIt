package techafrkix.work.com.spot.techafrkix.work.com.spot.utils;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

/**
 * Created by techafrkix0 on 24/05/2016.
 */
public class ParentApplication extends Application {

    protected static boolean isVisible = false;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

}
