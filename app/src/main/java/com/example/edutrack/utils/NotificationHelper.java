package com.example.edutrack.utils;

import android.app.*;
import android.content.Context;
import androidx.core.app.NotificationCompat;
import com.example.edutrack.R;

public class NotificationHelper {
    public static void show(Context c, String text) {
        NotificationManager nm = (NotificationManager)
                c.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder b =
                new NotificationCompat.Builder(c, "edutrack")
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle("EduTrack")
                        .setContentText(text)
                        .setAutoCancel(true);

        nm.notify(1, b.build());
    }
}
