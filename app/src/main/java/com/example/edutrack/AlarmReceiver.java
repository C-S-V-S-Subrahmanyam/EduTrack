package com.example.edutrack;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;

import androidx.core.app.NotificationCompat;

import com.example.edutrack.R;
import com.example.edutrack.activities.TaskListActivity;
import com.example.edutrack.db.AppDatabase;
import com.example.edutrack.db.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AlarmReceiver extends BroadcastReceiver {
    private static MediaPlayer mediaPlayer;
    @Override
    public void onReceive(Context context, Intent intent) {
        // Acquire wake lock to ensure device wakes up
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "EduTrack:AlarmWakeLock"
        );
        wakeLock.acquire(60000); // Hold for 1 minute
        
        String taskTitle = intent.getStringExtra("taskTitle");
        String taskDesc = intent.getStringExtra("taskDesc");
        int taskId = intent.getIntExtra("taskId", 0);
        boolean isRecurring = intent.getBooleanExtra("isRecurring", false);

        // Check if task is still incomplete
        Task task = AppDatabase.getInstance(context).taskDao().getById(taskId);
        
        if (task != null && !task.isCompleted) {
            // Play alarm sound
            playAlarmSound(context);
            
            // Create notification
            createNotification(context, taskTitle, taskDesc, taskId, isRecurring);

            // Send email for overdue tasks
            if (isRecurring) {
                sendEmailReminder(context, taskTitle, taskDesc);
            }
        }
        
        // Release wake lock
        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
    }
    
    private void playAlarmSound(Context context) {
        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }
            
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmSound == null) {
                alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
            
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(context, alarmSound);
            
            AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
            mediaPlayer.setAudioAttributes(attributes);
            
            mediaPlayer.setLooping(false);
            mediaPlayer.prepare();
            mediaPlayer.start();
            
            // Stop after 30 seconds
            mediaPlayer.setOnCompletionListener(mp -> {
                if (mp != null) {
                    mp.release();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createNotification(Context context, String title, String desc, int taskId, boolean isRecurring) {
        String channelId = "task_reminder_channel";
        NotificationManager notificationManager = 
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Task Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for task reminders");
            channel.enableVibration(true);
            channel.enableLights(true);
            channel.setSound(
                android.provider.Settings.System.DEFAULT_NOTIFICATION_URI,
                new android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            );
            notificationManager.createNotificationChannel(channel);
        }

        // Intent to open TaskListActivity when notification is clicked
        Intent intent = new Intent(context, TaskListActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                taskId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        String notifTitle = isRecurring ? "⚠️ OVERDUE: " + title : "🔔 Task Reminder: " + title;
        String notifText = isRecurring ? "Task is overdue! Please complete it." : (desc != null ? desc : "You have a pending task");

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(notifTitle)
                .setContentText(notifText)
                .setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(notifText))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(new long[]{0, 1000, 500, 1000, 500, 1000})
                .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setOnlyAlertOnce(false);

        notificationManager.notify(taskId, builder.build());
    }

    private void sendEmailReminder(Context context, String taskTitle, String taskDesc) {
        try {
            // Get logged-in user's email
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            String userEmail = currentUser != null ? currentUser.getEmail() : "";
            
            if (userEmail.isEmpty()) {
                return;
            }
            
            String emailSubject = "⚠️ EduTrack: OVERDUE TASK - " + taskTitle;
            String emailBody = "Dear Student,\n\n" +
                             "You have an OVERDUE task in EduTrack!\n\n" +
                             "Task Title: " + taskTitle + "\n" +
                             "Description: " + (taskDesc != null ? taskDesc : "No description") + "\n\n" +
                             "Please complete this task as soon as possible!\n\n" +
                             "Best regards,\n" +
                             "EduTrack Team\n" +
                             "subrahmanyam310308@gmail.com";
            
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{userEmail});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
            emailIntent.putExtra(Intent.EXTRA_TEXT, emailBody);
            emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            
            try {
                context.startActivity(Intent.createChooser(emailIntent, "Send reminder email").setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
