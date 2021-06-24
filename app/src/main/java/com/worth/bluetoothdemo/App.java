package com.worth.bluetoothdemo;

import android.app.Application;

import com.worth.framework.base.core.helper.SdkCoreHelper;


/**
 * Author:  LiuHao
 * Email:   114650501@qq.com
 * TIME:    6/14/21 --> 1:52 PM
 * Description: This is App
 */
public class App extends Application {
    public static Application context;
    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        SdkCoreHelper.Companion.getInstance().initCore(this, true);
    }
}
