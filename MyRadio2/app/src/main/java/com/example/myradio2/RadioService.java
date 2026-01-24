package com.example.myradio2;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;

public class RadioService extends Service {

    public static final String ACTION_PLAY = "ACTION_PLAY";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String EXTRA_URL = "URL";

    private ExoPlayer player;

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            player = new ExoPlayer.Builder(this).build();

            startForeground(
                    1,
                    createNotification("초기화 중")
            );

        } catch (Exception e) {
            e.printStackTrace();
            stopSelf(); // 문제 생기면 즉시 종료
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null && ACTION_PLAY.equals(intent.getAction())) {
            String url = intent.getStringExtra(EXTRA_URL);
            playRadio(url);
        }

        return START_STICKY;
    }

    private void playRadio(String url) {

        if (player == null || url == null) return;

        try {
            DefaultHttpDataSource.Factory factory =
                    new DefaultHttpDataSource.Factory()
                            .setUserAgent("Mozilla/5.0");

            MediaSource mediaSource =
                    new ProgressiveMediaSource.Factory(factory)
                            .createMediaSource(MediaItem.fromUri(url));

            player.setMediaSource(mediaSource);
            player.prepare();
            player.play();

            startForeground(1, createNotification("라디오 재생 중"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Notification createNotification(String text) {

        String channelId = "radio_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel =
                    new NotificationChannel(
                            channelId,
                            "Radio Playback",
                            NotificationManager.IMPORTANCE_LOW
                    );

            NotificationManager manager =
                    getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        return new NotificationCompat.Builder(this, channelId)
                .setContentTitle("My Radio")
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setOngoing(true)
                .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}