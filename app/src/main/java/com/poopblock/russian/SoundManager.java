package com.poopblock.russian;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.util.SparseIntArray;

/**
 * 音效管理器，用于统一管理应用中的音效播放
 */
public class SoundManager {

    private static SoundManager instance;
    
    private SoundPool soundPool;
    private SparseIntArray soundIds;
    private boolean isSoundEnabled;
    
    // 音效资源ID
    private static final int SOUND_VALID_CLICK = 1;
    private static final int SOUND_INVALID_OPERATION = 2;
    
    /**
     * 私有构造函数，使用单例模式
     */
    private SoundManager() {
        soundIds = new SparseIntArray();
    }
    
    /**
     * 获取SoundManager实例
     */
    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }
    
    /**
     * 初始化SoundPool
     */
    public void init(Context context, boolean soundEnabled) {
        isSoundEnabled = soundEnabled;
        
        // 检查是否已经初始化
        if (soundPool != null) {
            release();
        }
        
        // 创建SoundPool
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        
        soundPool = new SoundPool.Builder()
                .setAudioAttributes(audioAttributes)
                .setMaxStreams(5)
                .build();
        
        // 加载音效
        soundIds.put(SOUND_VALID_CLICK, soundPool.load(context, R.raw.validclick, 1));
        soundIds.put(SOUND_INVALID_OPERATION, soundPool.load(context, R.raw.invalidoperation, 1));
    }
    
    /**
     * 播放有效点击音效
     */
    public void playValidClickSound() {
        if (isSoundEnabled && soundPool != null) {
            int soundId = soundIds.get(SOUND_VALID_CLICK);
            if (soundId != 0) {
                soundPool.play(soundId, 1.0f, 1.0f, 0, 0, 1.0f);
            }
        }
    }
    
    /**
     * 播放无效操作音效
     */
    public void playInvalidOperationSound() {
        if (isSoundEnabled && soundPool != null) {
            int soundId = soundIds.get(SOUND_INVALID_OPERATION);
            if (soundId != 0) {
                soundPool.play(soundId, 1.0f, 1.0f, 0, 0, 1.0f);
            }
        }
    }
    
    /**
     * 设置音效开关状态
     */
    public void setSoundEnabled(boolean enabled) {
        isSoundEnabled = enabled;
    }
    
    /**
     * 释放资源
     */
    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        soundIds.clear();
    }
}