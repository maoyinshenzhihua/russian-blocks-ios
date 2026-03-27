package com.poopblock.russian;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;



public class SettingsActivity extends AppCompatActivity {

    private Switch animationSwitch;
    private Switch musicSwitch;
    private Switch controllerSwitch;
    private Switch gameSoundSwitch;
    private Switch smallScreenSwitch;
    private Button backButton;
    
    // SharedPreferences键名
    private static final String PREF_NAME = "game_settings";
    private static final String KEY_ANIMATION_ENABLED = "animation_enabled";
    private static final String KEY_MUSIC_ENABLED = "music_enabled";
    private static final String KEY_CONTROLLER_ENABLED = "controller_enabled";
    private static final String KEY_GAME_SOUND_ENABLED = "game_sound_enabled";
    private static final String KEY_SMALL_SCREEN_ENABLED = "small_screen_enabled";
    
    private SharedPreferences sharedPreferences;
    // 默认DPI值，用于恢复
    private float originalDensity;
    private int originalDensityDpi;
    private float originalScaledDensity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        
        // 初始化SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        
        // 获取原始DPI值
        android.util.DisplayMetrics metrics = getResources().getDisplayMetrics();
        originalDensity = metrics.density;
        originalDensityDpi = metrics.densityDpi;
        originalScaledDensity = metrics.scaledDensity;
        
        // 绑定控件
        animationSwitch = findViewById(R.id.animation_switch);
        musicSwitch = findViewById(R.id.music_switch);
        controllerSwitch = findViewById(R.id.controller_switch);
        gameSoundSwitch = findViewById(R.id.game_sound_switch);
        smallScreenSwitch = findViewById(R.id.small_screen_switch);
        backButton = findViewById(R.id.back_button);
        
        // 从SharedPreferences加载动画开关状态
        boolean isAnimationEnabled = sharedPreferences.getBoolean(KEY_ANIMATION_ENABLED, false);
        animationSwitch.setChecked(isAnimationEnabled);
        
        // 从SharedPreferences加载音乐开关状态
        boolean isMusicEnabled = sharedPreferences.getBoolean(KEY_MUSIC_ENABLED, true);
        musicSwitch.setChecked(isMusicEnabled);
        
        // 从SharedPreferences加载控制器开关状态
        boolean isControllerEnabled = sharedPreferences.getBoolean(KEY_CONTROLLER_ENABLED, false);
        controllerSwitch.setChecked(isControllerEnabled);
        
        // 从SharedPreferences加载游戏音效开关状态
        boolean isGameSoundEnabled = sharedPreferences.getBoolean(KEY_GAME_SOUND_ENABLED, true);
        gameSoundSwitch.setChecked(isGameSoundEnabled);
        
        // 初始化音效管理器
        SoundManager.getInstance().init(this, isGameSoundEnabled);
        
        // 从SharedPreferences加载小屏模式开关状态
        boolean isSmallScreenEnabled = sharedPreferences.getBoolean(KEY_SMALL_SCREEN_ENABLED, false);
        smallScreenSwitch.setChecked(isSmallScreenEnabled);
        
        // 设置动画开关点击事件
        animationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // 保存设置到SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_ANIMATION_ENABLED, isChecked);
            editor.apply();
        });
        
        // 设置音乐开关点击事件
        musicSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // 保存设置到SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_MUSIC_ENABLED, isChecked);
            editor.apply();
            
            // 根据开关状态控制音乐服务
            if (isChecked) {
                // 启动音乐服务
                startService(new Intent(SettingsActivity.this, MusicService.class));
            } else {
                // 停止音乐服务
                stopService(new Intent(SettingsActivity.this, MusicService.class));
            }
        });
        
        // 设置控制器开关点击事件
        controllerSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // 保存设置到SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_CONTROLLER_ENABLED, isChecked);
            editor.apply();
        });
        
        // 设置游戏音效开关点击事件
        gameSoundSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // 保存设置到SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_GAME_SOUND_ENABLED, isChecked);
            editor.apply();
            
            // 更新音效管理器的状态
            SoundManager.getInstance().setSoundEnabled(isChecked);
        });
        
        // 设置小屏模式开关点击事件
        smallScreenSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // 播放点击音效
            SoundManager.getInstance().playValidClickSound();
            // 保存设置到SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_SMALL_SCREEN_ENABLED, isChecked);
            editor.apply();
            
            // 调整DPI值
            adjustDPI(isChecked);
        });
        
        // 设置返回按钮点击事件
        backButton.setOnClickListener(v -> {
            // 播放点击音效
            SoundManager.getInstance().playValidClickSound();
            finish();
            // 检查并应用转场动画
            applyTransitionAnimation();
        });
        
        // 为所有按钮添加焦点监听，实现手柄导航高亮
        addButtonFocusListeners();
    }
    
    /**
     * 为所有按钮添加焦点监听
     */
    private void addButtonFocusListeners() {
        View.OnFocusChangeListener focusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // 获得焦点，显示蓝色边框
                    v.setBackgroundResource(R.drawable.btn_blue_border);
                    // 播放点击音效，提供听觉反馈
                    SoundManager.getInstance().playValidClickSound();
                } else {
                    // 失去焦点，恢复默认样式
                    v.setBackgroundResource(R.drawable.btn_white_black_border);
                }
            }
        };
        
        // 为返回按钮添加焦点监听
        backButton.setOnFocusChangeListener(focusChangeListener);
        
        // 设置初始焦点
        backButton.requestFocus();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // 进入设置页面时，根据用户设置决定是否启动音乐
        boolean isMusicEnabled = sharedPreferences.getBoolean(KEY_MUSIC_ENABLED, true);
        if (isMusicEnabled) {
            startService(new Intent(this, MusicService.class));
        }
        
        // 恢复动画开关状态
        boolean isAnimationEnabled = sharedPreferences.getBoolean(KEY_ANIMATION_ENABLED, false);
        animationSwitch.setChecked(isAnimationEnabled);
        
        // 恢复音乐开关状态
        musicSwitch.setChecked(isMusicEnabled);
        
        // 恢复控制器开关状态
        boolean isControllerEnabled = sharedPreferences.getBoolean(KEY_CONTROLLER_ENABLED, false);
        controllerSwitch.setChecked(isControllerEnabled);
        
        // 恢复游戏音效开关状态
        boolean isGameSoundEnabled = sharedPreferences.getBoolean(KEY_GAME_SOUND_ENABLED, true);
        gameSoundSwitch.setChecked(isGameSoundEnabled);
        
        // 恢复小屏模式开关状态
        boolean isSmallScreenEnabled = sharedPreferences.getBoolean(KEY_SMALL_SCREEN_ENABLED, false);
        smallScreenSwitch.setChecked(isSmallScreenEnabled);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // 离开设置页面时不停止音乐，让返回的页面处理
    }
    

    
    /**
     * 检查并应用转场动画
     */
    private void applyTransitionAnimation() {
        // 从SharedPreferences获取动画设置
        boolean isAnimationEnabled = sharedPreferences.getBoolean(KEY_ANIMATION_ENABLED, false);
        if (isAnimationEnabled) {
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        } else {
            // 明确指定无动画
            overridePendingTransition(0, 0);
        }
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // 检查并应用转场动画
        applyTransitionAnimation();
    }
    

    
    /**
     * 调整DPI值
     */
    private void adjustDPI(boolean isSmallScreenEnabled) {
        try {
            // 获取SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("game_settings", MODE_PRIVATE);
            
            // 获取DisplayMetrics对象
            android.util.DisplayMetrics metrics = getResources().getDisplayMetrics();
            
            // 获取原始DPI值（优先从SharedPreferences获取，否则使用当前值）
            float originalDensity = sharedPreferences.getFloat("original_density", metrics.density);
            int originalDensityDpi = sharedPreferences.getInt("original_density_dpi", metrics.densityDpi);
            float originalScaledDensity = sharedPreferences.getFloat("original_scaled_density", metrics.scaledDensity);
            
            if (isSmallScreenEnabled) {
                // 小屏模式：降低DPI值，使界面元素变小
                float scaleFactor = 0.8f; // 缩小比例
                metrics.density = originalDensity * scaleFactor;
                metrics.densityDpi = (int) (originalDensityDpi * scaleFactor);
                metrics.scaledDensity = originalScaledDensity * scaleFactor;
            } else {
                // 恢复原始DPI值
                metrics.density = originalDensity;
                metrics.densityDpi = originalDensityDpi;
                metrics.scaledDensity = originalScaledDensity;
            }
            
            // 更新资源配置
            android.content.res.Configuration configuration = getResources().getConfiguration();
            configuration.densityDpi = metrics.densityDpi;
            getResources().updateConfiguration(configuration, metrics);
            
            // 重新创建当前Activity以应用新的DPI设置
            recreate();
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "DPI调整失败", Toast.LENGTH_SHORT).show();
        }
    }
    

}