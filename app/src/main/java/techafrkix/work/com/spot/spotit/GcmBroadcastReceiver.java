package techafrkix.work.com.spot.spotit;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

/**
 * Created by techafrkix0 on 29/06/2016.
 */
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("GCM", "Nouvelle notification du serveur GCM");
        ComponentName comp = new ComponentName(context.getPackageName(),
                GcmIntentService.class.getName());
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
    }
}
