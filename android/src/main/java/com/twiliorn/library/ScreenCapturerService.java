package com.twiliorn.library;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;

@TargetApi(29)
public class ScreenCapturerService extends Service {
    private static final String CHANNEL_ID = "screen_capture";
    private static final String CHANNEL_NAME = "Screen_Capture";

    private ServiceCallbacks serviceCallbacks;

    // Binder given to clients
    private final IBinder binder = new LocalBinder();

    /**
     * Class used for the client Binder.  We know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public ScreenCapturerService getService() {
            // Return this instance of ScreenCapturerService so clients can call public methods
            return ScreenCapturerService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if ("STOP_SHARING".equals(intent.getAction())) {
            serviceCallbacks.stopScreenShare();
        }
        return START_NOT_STICKY;
    }

    public void startForeground(Intent stopSharingIntent) {
        NotificationChannel chan = new NotificationChannel(CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_NONE);
        chan.setDescription("Channel for screen share");
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        PendingIntent stopSharingPendingIntent = PendingIntent.getService(this, 0, stopSharingIntent, 0);

        Intent doNothingIntent = new Intent(this, ScreenCapturerService.class);
        doNothingIntent.setAction("DO_NOTHING");

        PendingIntent doNothingPendingIntent =  PendingIntent.getService(this, 1, doNothingIntent, 0);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

        inboxStyle.setBigContentTitle("You are sharing your screen");
        inboxStyle.setSummaryText("Screen Shared");

        final int notificationId = (int) System.currentTimeMillis();

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("Pulse")
                .setContentText("You are sharing your screen")
                .setSmallIcon(R.drawable.ic_stop_notification_screenshare)
                .setContentIntent(doNothingPendingIntent)
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .addAction(R.drawable.ic_stop_notification_screenshare, "Stop Screen Share", stopSharingPendingIntent)
                .setChannelId(CHANNEL_ID)
                .setStyle(inboxStyle)
                .build();
        startForeground(notificationId, notification);
    }

    public void endForeground() {
        stopForeground(true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void setCallbacks(ServiceCallbacks callbacks) {
        serviceCallbacks = callbacks;
    }
}
