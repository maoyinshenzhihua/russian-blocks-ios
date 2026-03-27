package com.poopblock.russian;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainMenuActivity extends AppCompatActivity {

    // 类成员变量，方便在其他方法中访问
    private Button startGameBtn;
    private Button gameRecordBtn;
    private Button gameSettingsBtn;
    private Button aboutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu_activity);

        // 初始化音效管理器
        SharedPreferences sharedPreferences = getSharedPreferences("game_settings", MODE_PRIVATE);
        boolean isGameSoundEnabled = sharedPreferences.getBoolean("game_sound_enabled", true);
        SoundManager.getInstance().init(this, isGameSoundEnabled);

        // 绑定按钮点击事件
        startGameBtn = findViewById(R.id.start_game_btn);
        gameRecordBtn = findViewById(R.id.game_record_btn);
        gameSettingsBtn = findViewById(R.id.game_settings_btn);
        aboutBtn = findViewById(R.id.about_btn);

        startGameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 播放点击音效
                SoundManager.getInstance().playValidClickSound();
                // 跳转到游戏界面
                Intent intent = new Intent(MainMenuActivity.this, GameActivity.class);
                startActivity(intent);
                // 检查并应用转场动画
                applyTransitionAnimation();
            }
        });

        gameRecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 播放点击音效
                SoundManager.getInstance().playValidClickSound();
                // 跳转到游戏记录页面
                Intent intent = new Intent(MainMenuActivity.this, GameRecordActivity.class);
                startActivity(intent);
                // 检查并应用转场动画
                applyTransitionAnimation();
            }
        });

        gameSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 播放点击音效
                SoundManager.getInstance().playValidClickSound();
                // 跳转到设置页面
                Intent intent = new Intent(MainMenuActivity.this, SettingsActivity.class);
                startActivity(intent);
                // 检查并应用转场动画
                applyTransitionAnimation();
            }
        });

        aboutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 播放点击音效
                SoundManager.getInstance().playValidClickSound();
                // 跳转到关于页面
                Intent intent = new Intent(MainMenuActivity.this, AboutActivity.class);
                startActivity(intent);
                // 检查并应用转场动画
                applyTransitionAnimation();
            }
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
        
        // 为所有按钮添加焦点监听
        startGameBtn.setOnFocusChangeListener(focusChangeListener);
        gameRecordBtn.setOnFocusChangeListener(focusChangeListener);
        gameSettingsBtn.setOnFocusChangeListener(focusChangeListener);
        aboutBtn.setOnFocusChangeListener(focusChangeListener);
        
        // 设置初始焦点
        startGameBtn.requestFocus();
    }
    
    /**
     * 检查并应用转场动画
     */
    private void applyTransitionAnimation() {
        // 从SharedPreferences获取动画设置
        android.content.SharedPreferences sharedPreferences = getSharedPreferences("game_settings", MODE_PRIVATE);
        boolean isAnimationEnabled = sharedPreferences.getBoolean("animation_enabled", false);
        if (isAnimationEnabled) {
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        } else {
            // 明确指定无动画
            overridePendingTransition(0, 0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 进入或返回主菜单时，根据用户设置决定是否启动音乐
        android.content.SharedPreferences sharedPreferences = getSharedPreferences("game_settings", MODE_PRIVATE);
        boolean isMusicEnabled = sharedPreferences.getBoolean("music_enabled", true);
        // 只有在不是从GameActivity返回时才启动音乐服务
        // 因为GameActivity已经在onDestroy中停止了音乐服务
        if (isMusicEnabled) {
            startService(new Intent(this, MusicService.class));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 正常的界面跳转时不停止音乐
        // 包括进入游戏记录、游戏设置和应用关于界面
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        // 不在onStop中停止音乐服务，让目标Activity自己处理
        // 这样跳转到其他界面时音乐不会中断
    }

    private void stopMusicAndNavigate() {
        // 停止音乐服务
        stopService(new Intent(this, MusicService.class));
        // 这里可以添加跳转逻辑
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 确保退出时停止音乐
        stopService(new Intent(this, MusicService.class));
    }
}
