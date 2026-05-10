package com.sympnet.app.network;

import android.content.Context;
import android.content.SharedPreferences;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class ApiClient {

    private static final String BASE_URL = " https://faster-say-trimmer.ngrok-free.dev/";
    private static Context appContext;

    public static void init(Context context) {
        appContext = context.getApplicationContext();
    }

    // Pas de singleton — on recrée à chaque appel pour avoir le token frais
    public static Retrofit getClient() {
        String token = "";
        if (appContext != null) {
            SharedPreferences prefs = appContext
                    .getSharedPreferences("SympNetPrefs", Context.MODE_PRIVATE);
            token = prefs.getString("userToken", "");
        }

        final String finalToken = token;

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request.Builder builder = chain.request().newBuilder()
                            .addHeader("ngrok-skip-browser-warning", "true")
                            .addHeader("Content-Type", "application/json");


                    return chain.proceed(builder.build());
                })
                .build();

        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}