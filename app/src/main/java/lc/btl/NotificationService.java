package lc.btl;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

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
        SharedPreferences sp = context.getSharedPreferences("currentUser", MODE_PRIVATE);
        String email = sp.getString("email","");
        if(!email.trim().equals("")) {
            String status = intent.getExtras().getString("status");
            if (status != null && status.equals("on")) {
                String userEmail = intent.getExtras().getString("userEmail");
                if(userEmail != null && userEmail.equals(email)) {
                    String cardName = intent.getExtras().getString("cardName");
                    String cardId = intent.getExtras().getString("cardId");
                    String boardId = intent.getExtras().getString("boardId");
                    String boardName = intent.getExtras().getString("boardName");
                    String listName = intent.getExtras().getString("listName");
                    int is_owner = intent.getExtras().getInt("is_owner");

                    SoundControl.getInstance(context).playMusic();

                    NotificationManager notificationManager = (NotificationManager)
                            context.getSystemService(NOTIFICATION_SERVICE);
                    Intent intent_card_detail = new Intent(context, CardDetailsActivity.class);
                    Bundle extras = new Bundle();
                    extras.putString("cardId", cardId);
                    extras.putString("boardId", boardId);
                    extras.putString("boardName", boardName);
                    extras.putString("listName", listName);
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
                            .build();

                    notificationManager.notify(Integer.valueOf(cardId), notification);
                }
            } else if (status != null && status.equals("off")) {
                SoundControl.getInstance(context).stopMusic();
            }
        }
        return START_NOT_STICKY;
    }
}
