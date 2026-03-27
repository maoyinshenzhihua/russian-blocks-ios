package com.poopblock.russian;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.Handler;

public class MusicService extends Service {

    private MediaPlayer mediaPlayer;
    private Handler handler;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        initializeMediaPlayer();
    }

    private void initializeMediaPlayer() {
        try {
            // 使用项目资源中的音乐文件
            mediaPlayer = MediaPlayer.create(this, R.raw.game_music);
            
            // 设置循环播放
            mediaPlayer.setLooping(true);
            // 设置初始音量
            mediaPlayer.setVolume(1.0f, 1.0f);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            // 实现音乐淡出效果
            fadeOutAndStop();
        }
    }

    private void fadeOutAndStop() {
        final float[] volume = {1.0f};
        final float decreaseStep = 0.1f;
        final long fadeOutDuration = 1000; // 1秒
        final long interval = fadeOutDuration / 10;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (volume[0] > 0) {
                    volume[0] -= decreaseStep;
                    if (volume[0] < 0) volume[0] = 0;
                    mediaPlayer.setVolume(volume[0], volume[0]);
                    handler.postDelayed(this, interval);
                } else {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
            }
        }, interval);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
