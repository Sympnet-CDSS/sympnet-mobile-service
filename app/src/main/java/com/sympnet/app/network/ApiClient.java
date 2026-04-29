package com.sympnet.app.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class ApiClient {

    private static final String BASE_URL = "https://faster-say-trimmer.ngrok-free.dev/";
    private static Retrofit retrofit;

    public static Retrofit getClient() {
        if (retrofit == null) {

            // ✅ Ajoute le header qui bypass le warning ngrok
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request request = chain.request().newBuilder()
                                .addHeader("ngrok-skip-browser-warning", "true")
                                .addHeader("Content-Type", "application/json")
                                .build();
                        return chain.proceed(request);
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}