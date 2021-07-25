package services;


import android.content.Context;
import android.content.SharedPreferences;

import androidx.constraintlayout.solver.widgets.Chain;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

import models.AccessToken;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by AsifMoinul on 12/31/2016.
 */

public class ServiceGenerator {

    static Context context;
    public ServiceGenerator(Context context){
        context = context;
        shared = context.getSharedPreferences("mediprefs", MODE_PRIVATE);
    }
    //private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
     static SharedPreferences shared;


    static OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
        @NotNull
        @Override
        public Response intercept(@NotNull Chain chain) throws IOException {

            String token = shared.getString("token","");
            Request newRequest  = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer "+token)
                    .build();
            return chain.proceed(newRequest);
        }
    })
            .connectTimeout(30, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();


    public  <S> S createService(Class<S> serviceClass, String baseUrl) {
        Retrofit builder = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(new NullOnEmptyConverterFactory())
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        return builder.create(serviceClass);
    }


    public class NullOnEmptyConverterFactory extends Converter.Factory {
        @Override
        public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
            final Converter<ResponseBody, ?> delegate = retrofit.nextResponseBodyConverter(this, type, annotations);
            return new Converter<ResponseBody, Object>() {
                @Override
                public Object convert(ResponseBody body) throws IOException {
                    long contentLength = body.contentLength();
                    if (contentLength == 0) {
                        return null;
                    }
                    return delegate.convert(body);
                }
            };
        }
    }



}
