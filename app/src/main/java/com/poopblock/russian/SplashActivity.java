package com.poopblock.russian;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.TextView;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY = 2000; // 2秒延迟

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 保存原始DPI值到SharedPreferences（只在首次启动时保存）
        saveOriginalDPI();
        
        // 检查并应用小屏模式设置
        checkAndApplySmallScreenMode();
        
        setContentView(R.layout.splash_activity);

        // 显示加载提示文字
        TextView loadingText = findViewById(R.id.loading_text);
        loadingText.setText("粑粑块正在集结...");

        // 延迟后跳转到主菜单
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MainMenuActivity.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_DELAY);
    }
    
    /**
     * 检查并应用小屏模式设置
     */
    private void checkAndApplySmallScreenMode() {
        // 获取SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("game_settings", MODE_PRIVATE);
        boolean isSmallScreenEnabled = sharedPreferences.getBoolean("small_screen_enabled", false);
        
        if (isSmallScreenEnabled) {
            // 应用小屏模式DPI设置
            applySmallScreenDPI();
        }
    }
    
    /**
     * 应用小屏模式DPI设置
     */
    private void applySmallScreenDPI() {
        try {
            // 获取SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("game_settings", MODE_PRIVATE);
            
            // 获取DisplayMetrics对象
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            
            // 小屏模式：降低DPI值，使界面元素变小
            float scaleFactor = 0.8f; // 缩小比例
            // 始终基于原始DPI值计算，而不是当前DPI值
            float originalDensity = sharedPreferences.getFloat("original_density", metrics.density);
            int originalDensityDpi = sharedPreferences.getInt("original_density_dpi", metrics.densityDpi);
            float originalScaledDensity = sharedPreferences.getFloat("original_scaled_density", metrics.scaledDensity);
            
            float newDensity = originalDensity * scaleFactor;
            int newDensityDpi = (int) (originalDensityDpi * scaleFactor);
            float newScaledDensity = originalScaledDensity * scaleFactor;
            
            // 更新DisplayMetrics
            metrics.density = newDensity;
            metrics.densityDpi = newDensityDpi;
            metrics.scaledDensity = newScaledDensity;
            
            // 更新资源配置
            android.content.res.Configuration configuration = getResources().getConfiguration();
            configuration.densityDpi = newDensityDpi;
            getResources().updateConfiguration(configuration, metrics);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 保存原始DPI值到SharedPreferences（只在首次启动时保存）
     */
    private void saveOriginalDPI() {
        SharedPreferences sharedPreferences = getSharedPreferences("game_settings", MODE_PRIVATE);
        
        // 检查是否已经保存过原始DPI值
        if (!sharedPreferences.contains("original_density")) {
            // 获取DisplayMetrics对象
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            
            // 保存原始DPI值
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putFloat("original_density", metrics.density);
            editor.putInt("original_density_dpi", metrics.densityDpi);
            editor.putFloat("original_scaled_density", metrics.scaledDensity);
            editor.apply();
        }
    }
}
