package com.poopblock.russian;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_activity);

        // 初始化音效管理器
        android.content.SharedPreferences sharedPreferences = getSharedPreferences("game_settings", MODE_PRIVATE);
        boolean isGameSoundEnabled = sharedPreferences.getBoolean("game_sound_enabled", true);
        SoundManager.getInstance().init(this, isGameSoundEnabled);

        // 设置开发者信息
        TextView developerText = findViewById(R.id.developer_text);
        developerText.setText("开发者：小花爱瞎剪");

        // 设置制作初衷
        TextView purposeText = findViewById(R.id.purpose_text);
        purposeText.setText("制作初衷：灵感来源于 S-Venti 制作的《俄罗斯粑粑块》鬼畜视频，特此将其改编为安卓应用，供大家娱乐体验");

        // 设置视频链接按钮
        Button videoLinkBtn = findViewById(R.id.video_link_btn);
        videoLinkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 播放点击音效
                SoundManager.getInstance().playValidClickSound();
                // B站视频URL
                String videoUrl = "https://www.bilibili.com/video/BV1maVTzDEYr";
                
                try {
                    // 创建Intent，添加标准的浏览类别
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                    
                    // 直接使用createChooser，不添加NEW_TASK标志，确保选择器显示
                    Intent chooserIntent = Intent.createChooser(intent, "选择打开方式");
                    startActivity(chooserIntent);
                } catch (Exception e) {
                    // 处理异常，直接使用浏览器打开
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
                    browserIntent.addCategory(Intent.CATEGORY_BROWSABLE);
                    startActivity(browserIntent);
                    e.printStackTrace();
                }
            }
        });
        
        // 为视频链接按钮添加焦点监听，实现手柄导航高亮
        videoLinkBtn.setOnFocusChangeListener(new View.OnFocusChangeListener() {
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
        });
        
        // 设置初始焦点
        videoLinkBtn.requestFocus();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // 进入关于页面时，根据用户设置决定是否启动音乐
        android.content.SharedPreferences sharedPreferences = getSharedPreferences("game_settings", MODE_PRIVATE);
        boolean isMusicEnabled = sharedPreferences.getBoolean("music_enabled", true);
        if (isMusicEnabled) {
            startService(new Intent(this, MusicService.class));
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // 离开关于页面时不停止音乐，让返回的页面处理
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
    public void onBackPressed() {
        super.onBackPressed();
        // 检查并应用转场动画
        applyTransitionAnimation();
    }
}
