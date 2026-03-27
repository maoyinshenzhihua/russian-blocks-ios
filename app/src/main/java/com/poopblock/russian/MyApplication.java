package com.poopblock.russian;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;

public class MyApplication extends Application {

    // 跟踪当前可见的Activity数量
    private int activeActivityCount = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        
        // 注册Activity生命周期回调
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(android.app.Activity activity, Bundle savedInstanceState) {
            }

            @Override
            public void onActivityStarted(android.app.Activity activity) {
            }

            @Override
            public void onActivityResumed(android.app.Activity activity) {
                activeActivityCount++;
                // 应用从后台回到前台，不自动启动音乐，由Activity自己处理
            }

            @Override
            public void onActivityPaused(android.app.Activity activity) {
            }

            @Override
            public void onActivityStopped(android.app.Activity activity) {
                activeActivityCount--;
                // 当所有Activity都不可见时，说明应用进入后台
                if (activeActivityCount == 0) {
                    // 停止音乐服务
                    stopService(new Intent(MyApplication.this, MusicService.class));
                }
            }

            @Override
            public void onActivitySaveInstanceState(android.app.Activity activity, Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(android.app.Activity activity) {
            }
        });
    }
}