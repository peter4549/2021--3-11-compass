package com.flow.android.java.compass.lock_screen;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;

import com.flow.android.java.compass.MainActivity;
import com.flow.android.java.compass.R;

public class LockScreenService extends JobIntentService {

    static final int JOB_ID = 1713;

    private static final String CHANNEL_NAME = "com.flow.android.java.compass.lock_screen.channel_name";
    private static final String CHANNEL_DESCRIPTION = "com.flow.android.java.compass.lock_screen.channel_description";
    private static final String CHANNEL_ID = "com.flow.android.java.compass.lock_screen.channel_id";

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Intent.ACTION_SCREEN_OFF:
                case Intent.ACTION_USER_PRESENT:
                    Intent newIntent = new Intent(context, MainActivity.class);
                    newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(newIntent);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(broadcastReceiver, intentFilter);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
            channel.setDescription(CHANNEL_DESCRIPTION);
            notificationManager.createNotificationChannel(channel);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                new Intent(this, MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_compass_24px)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.notification_content_text))
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Notification notification = builder.build();

        // https://stackoverflow.com/questions/44658923/android-foreground-service-notification-not-showing/51908073
        startForeground(1, notification);
    }

    public void enqueueWork(Context context, Intent work) {
        enqueueWork(context, LockScreenService.class, JOB_ID, work);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }
}
