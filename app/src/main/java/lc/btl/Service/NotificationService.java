package lc.btl.Service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.annotation.Nullable;

import lc.btl.CardDetailsActivity;
import lc.btl.LocalDatabse;
import lc.btl.R;
import lc.btl.Object.SoundControl;

/**
 * Created by THHNt on 2/12/2018.
 */

public class NotificationService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Context context = this.getApplicationContext();
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        SharedPreferences sp = context.getSharedPreferences("currentUser", MODE_PRIVATE);
        String email = sp.getString("email","");
        String cardId = intent.getExtras().getString("cardId");
        String status = intent.getExtras().getString("status");
        LocalDatabse database = new LocalDatabse(context, "todolist.sql", null, 1);
        Cursor cursor = database.getData("SELECT * FROM card WHERE id = " + cardId);
        if (cursor.moveToFirst()) {
            if (!email.trim().equals("")) {
                if (status != null && status.equals("on")) {
                    String userEmail = intent.getExtras().getString("userEmail");
                    if (userEmail != null && userEmail.equals(email)) {
                        String cardName = intent.getExtras().getString("cardName");
                        String boardId = intent.getExtras().getString("boardId");
                        String boardName = intent.getExtras().getString("boardName");
                        int is_owner = intent.getExtras().getInt("is_owner");

                        SoundControl.getInstance(context).playMusic();
                        long[] pattern = { 0, 1000, 500 }; //0 to start now, 1000 to vibrate 1000 ms, 0 to sleep for 500 ms.
                        v.vibrate(pattern, 0); // 0 to repeat endlessly.

                        NotificationManager notificationManager = (NotificationManager)
                                context.getSystemService(NOTIFICATION_SERVICE);
                        Intent intent_card_detail = new Intent(context, CardDetailsActivity.class);
                        Bundle extras = new Bundle();
                        extras.putString("cardId", cardId);
                        extras.putString("boardId", boardId);
                        extras.putString("boardName", boardName);
                        extras.putInt("is_owner", is_owner);
                        extras.putString("status", "off");
                        intent_card_detail.putExtras(extras);

                        PendingIntent pendingIntent = PendingIntent.getActivity(context, Integer.valueOf(cardId), intent_card_detail, PendingIntent.FLAG_ONE_SHOT);

                        Notification notification = new Notification.Builder(context)
                                .setContentTitle(cardName)
                                .setContentText(getString(R.string.notification_content))
                                .setSmallIcon(R.drawable.alarm)
                                .setContentIntent(pendingIntent)
                                .setAutoCancel(true)
                                .setOngoing(true)
                                .build();

                        notificationManager.notify(Integer.valueOf(cardId), notification);
                        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "Tag");
                        wakeLock.acquire();
                        wakeLock.release();
                    }
                }
            }
        }
        if (status != null && status.equals("off")) {
            SoundControl.getInstance(context).stopMusic();
            v.cancel();
        }
        return START_NOT_STICKY;
    }
}
