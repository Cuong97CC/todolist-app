package lc.btl.Receiver;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import lc.btl.Service.NotificationService;

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
