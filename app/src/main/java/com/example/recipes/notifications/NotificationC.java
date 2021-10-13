package com.example.recipes.notifications;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import androidx.core.app.NotificationCompat;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
public class NotificationC extends ContextWrapper {

    private static final String ID = "channel1";
    private static final String NAME = "FirebaseRecepies";

    private NotificationManager notificationManager;

    public NotificationC(Context base) {
        super(base);
        if (Build.VERSION.SDK_INT >= 26){
            createChannel();
        }

    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel notificationChannel = new NotificationChannel(ID,NAME,NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.enableLights(true);
        notificationChannel.enableVibration(true);
        notificationChannel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PRIVATE);
        getManager().createNotificationChannel(notificationChannel);
    }

    public NotificationManager getManager(){
        if (notificationManager == null){
            notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return notificationManager;
    }

    public NotificationCompat.Builder getONotifications(String title, String body, PendingIntent pendingIntent, Uri soundUri, String icon){
        return new NotificationCompat.Builder(getApplicationContext(), ID)
                .setContentIntent(pendingIntent).setContentTitle(title)
                .setContentText(body).setSound(soundUri).setAutoCancel(true)
                .setSmallIcon(Integer.parseInt(icon));
    }

}
