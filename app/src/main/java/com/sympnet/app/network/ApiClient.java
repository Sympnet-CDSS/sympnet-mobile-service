package com.sympnet.app.network;

import android.content.Context;
import android.content.SharedPreferences;

import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class ApiClient {

    private static final String BASE_URL = "https://faster-say-trimmer.ngrok-free.dev/";
    private static Context appContext;

    public static void init(Context context) {
        appContext = context.getApplicationContext();
    }

    public static Retrofit getClient() {
        String token = "";
        if (appContext != null) {
            SharedPreferences prefs = appContext
                    .getSharedPreferences("SympNetPrefs", Context.MODE_PRIVATE);
            token = prefs.getString("userToken", "");
        }

        final String finalToken = token;

        // Ajout du logger pour voir le JSON brut dans Logcat
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging) // <--- ICI : Affiche tout le contenu JSON
                .addInterceptor(chain -> {
                    Request.Builder builder = chain.request().newBuilder()
                            .header("ngrok-skip-browser-warning", "true")
                            .header("Content-Type", "application/json");

                    if (!finalToken.isEmpty()) {
                        builder.header("Authorization", "Bearer " + finalToken);
                    }

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
