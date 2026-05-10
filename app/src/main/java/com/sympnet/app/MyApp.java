package com.sympnet.app;

import android.app.Application;
import com.sympnet.app.api.RetrofitClient;
import com.sympnet.app.network.ApiClient;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ApiClient.init(this);
        RetrofitClient.init(this);
    }
}