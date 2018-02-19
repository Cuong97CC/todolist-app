package lc.btl;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import java.io.Serializable;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by THHNt on 2/8/2018.
 */

public class AlarmReciever extends BroadcastReceiver{
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intentService = new Intent(context, NotificationService.class);
        intentService.putExtras(intent.getExtras());
        context.startService(intentService);
    }
}
