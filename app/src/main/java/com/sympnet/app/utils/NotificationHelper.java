package com.sympnet.app.utils;
import com.sympnet.app.activities.notification.NotificationDetailsActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.sympnet.app.R;

public class NotificationHelper {
    private static final String CHANNEL_ID = "sympnet_notif_channel";
    private static final String CHANNEL_NAME = "SympNet Notifications";

    public static void showNotification(Context context, String title, String message) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }

        Intent intent;
        String lowerTitle = title != null ? title.toLowerCase() : "";
        String lowerMessage = message != null ? message.toLowerCase() : "";
        
        if (lowerTitle.contains("ordonnance") || lowerMessage.contains("ordonnance") || lowerTitle.contains("prescription") || lowerMessage.contains("prescription")) {
            intent = new Intent(context, com.sympnet.app.activities.prescription.PrescriptionsActivity.class);
        } else if (lowerTitle.contains("rendez-vous") || lowerMessage.contains("rendez-vous") || lowerTitle.contains("rdv")) {
            intent = new Intent(context, com.sympnet.app.activities.MainActivity.class);
            intent.putExtra("TARGET_FRAGMENT", "SCHEDULE");
        } else {
            intent = new Intent(context, NotificationDetailsActivity.class);
            intent.putExtra("title", title);
            intent.putExtra("message", message);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
