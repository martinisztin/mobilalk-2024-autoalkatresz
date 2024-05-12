package com.bkyzsa.autoalkatresz;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class NotificationHelper {
    private static final String CHANNEL_ID = "com.bkyzsa.autoalkatresz";
    private static final CharSequence CHANNEL_NAME = "OrderNotification";
    private static final String CHANNEL_DESCRIPTION = "This is my notification channel";

    public static void showNotification(Context context, String title, String content) {
        Log.d("NotificationHelper", "showNotification: called");
        // Create a notification builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Create a notification manager
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager == null) {
            Log.e("NotificationHelper", "NotificationManager is null");
            return;
        }

        // Check if the Android version is Oreo or higher, as notification channels are required for Oreo and higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the notification channel
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(CHANNEL_DESCRIPTION);

            // Register the channel with the system
            notificationManager.createNotificationChannel(channel);
        }

        // Issue the notification
        notificationManager.notify(/* notification id */ 1, builder.build());
        Log.d("NotificationHelper", "Notification issued");
    }
}