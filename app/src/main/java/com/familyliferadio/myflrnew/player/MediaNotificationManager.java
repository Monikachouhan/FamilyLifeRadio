package com.familyliferadio.myflrnew.player;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.familyliferadio.myflrnew.PlayerActivity;
import com.familyliferadio.myflrnew.R;

import saschpe.exoplayer2.ext.icy.IcyHttpDataSource;

public class MediaNotificationManager {

    public static final int NOTIFICATION_ID = 555;
    private final String PRIMARY_CHANNEL = "PRIMARY_CHANNEL_ID";
    private final String PRIMARY_CHANNEL_NAME = "PRIMARY";

    private RadioService service;

    private String strAppName, strLiveBroadcast;

    private Resources resources;
    private NotificationCompat.Builder builder;
    private Bitmap bitmap = null;

    private NotificationManagerCompat notificationManager;

    public MediaNotificationManager(RadioService service) {

        this.service = service;
        this.resources = service.getResources();


        strAppName = resources.getString(R.string.app_name);
        strLiveBroadcast = resources.getString(R.string.live_broadcast);

        notificationManager = NotificationManagerCompat.from(service);
    }

    public void startNotify(String playbackStatus, IcyHttpDataSource.IcyMetadata icyMetadata) {

        Log.d("startNotify", " startNotify " + icyMetadata);
        Bitmap largeIcon;
        if (bitmap == null)
            largeIcon = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher);
        else {
            largeIcon = bitmap;
        }

        if (icyMetadata != null)
            strLiveBroadcast = icyMetadata.getStreamTitle();


        int icon = R.drawable.ic_pause_30dp;
        Intent playbackAction = new Intent(service, RadioService.class);
        playbackAction.setAction(RadioService.ACTION_PAUSE);
        PendingIntent action = PendingIntent.getService(service, 1, playbackAction, 0);

        if (playbackStatus.equals(PlaybackStatus.PAUSED)) {

            icon = R.drawable.ic_play;
            playbackAction.setAction(RadioService.ACTION_PLAY);
            action = PendingIntent.getService(service, 2, playbackAction, 0);

        }

        Intent stopIntent = new Intent(service, RadioService.class);
        stopIntent.setAction(RadioService.ACTION_STOP);
        PendingIntent stopAction = PendingIntent.getService(service, 3, stopIntent, 0);

        Intent intent = new Intent(service, PlayerActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent = PendingIntent.getActivity(service, 0, intent, 0);

        notificationManager.cancel(NOTIFICATION_ID);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(PRIMARY_CHANNEL, PRIMARY_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        builder = new NotificationCompat.Builder(service, PRIMARY_CHANNEL)
                .setAutoCancel(false)
                .setContentTitle(strLiveBroadcast)
                .setContentText(strAppName)
                .setLargeIcon(largeIcon)
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(android.R.drawable.stat_sys_headset)
                .addAction(icon, "pause", action)
                .addAction(R.drawable.ic_close_black_24dp, "stop", stopAction)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setWhen(System.currentTimeMillis())
                .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(service.getMediaSession().getSessionToken())
                        .setShowActionsInCompactView(0, 1)
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(stopAction));

        service.startForeground(NOTIFICATION_ID, builder.build());


    }

    public void updatNotification(String title, String iconUrl) {
        if (builder != null) {
           if (title!=null)
            builder.setContentTitle(title);
            loadImage(iconUrl);
            service.startForeground(NOTIFICATION_ID, builder.build());

        }
    }

    private void loadImage(final String url) {
        Glide.with(service.getApplicationContext())
                .asBitmap()
                .load(url)
                .apply(new RequestOptions()
                        .fitCenter())
                .into(new SimpleTarget<Bitmap>(200, 200) {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        bitmap = resource;
                        builder.setLargeIcon(resource);
                        service.startForeground(NOTIFICATION_ID, builder.build());

                    }
                });
    }

    public void cancelNotify() {

        service.stopForeground(true);
    }

}
