package com.modcom.meditest;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit_api.RecyclerInterface;

public class PhleboDetails extends AppCompatActivity {

    LinearLayout linearLayout;
    SharedPreferences shared;
    TextView txt_status, txt_phlebo_first_name, txt_phlebo_last_name;
    TextView txt_phlebo_surname, txt_phlebo_gender, txt_phlebo_qualifications;
    TextView txt_phlebo_phone, txt_view_phlebo_location;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phlebo_details);
        txt_view_phlebo_location = findViewById(R.id.view_phlebo_location);
        txt_view_phlebo_location.setVisibility(View.GONE);
        shared = getSharedPreferences("mediprefs", MODE_PRIVATE);

        String status = shared.getString("status","");

        if (status.equalsIgnoreCase("active")){
            txt_view_phlebo_location.setVisibility(View.VISIBLE);
        }

        txt_phlebo_first_name = findViewById(R.id.txt_phlebo_first_name);
        txt_status = findViewById(R.id.txt_status);
        txt_status.setText(status);
        txt_phlebo_gender= findViewById(R.id.txt_phlebo_gender);
        txt_phlebo_qualifications= findViewById(R.id.txt_phlebo_qualifications);
        txt_phlebo_phone = findViewById(R.id.txt_phlebo_phone);


        txt_view_phlebo_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), Track_Phlebo.class));
            }
        });

        linearLayout  = findViewById(R.id.linearLayout);
        linearLayout.setVisibility(View.GONE);

        fetchJSON();
    }

    String phone;
    private void fetchJSON(){

        try {
            //Bundle b = getIntent().getExtras();
            //String id = b.getString("id");
            ProgressDialog dialog = new ProgressDialog(PhleboDetails.this);
            dialog.setTitle("Retrieving Details..");
            dialog.setMessage("Please wait..");
            dialog.setMax(100);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();

            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
                @NotNull
                @Override
                public okhttp3.Response intercept(@NotNull Chain chain) throws IOException {

                    String token = shared.getString("token", "");
                    Request newRequest = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer " + token)
                            //  .addHeader("Authorization", "Bearer 107|4fRJslzbfcbPyu9hlhKnTeWHRgY7MHK5PVpkFsUL")
                            .build();
                    return chain.proceed(newRequest);
                }
            }).callTimeout(7, TimeUnit.SECONDS)
                    .connectTimeout(7, TimeUnit.SECONDS)
                    .readTimeout(7, TimeUnit.SECONDS)
                    .writeTimeout(7, TimeUnit.SECONDS)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(RecyclerInterface.JSONURL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .client(client)
                    .build();

            RecyclerInterface api = retrofit.create(RecyclerInterface.class);
            String booking_id = shared.getString("booking_id", "");
            Call<String> call = api.getStringSinglePhlebo(Integer.parseInt(booking_id));
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    //  Log.i("Responsestring", response.body().toString());
                    dialog.dismiss();
                    //Toast.makeText()
                    if (response.isSuccessful()) {
                        if (response.body() != null) {

                            dialog.dismiss();
                            linearLayout.setVisibility(View.VISIBLE);

                            //Log.i("onSuccess", response.body().toString());
                            String jsonresponse = response.body().toString();

                            try {
                                JSONArray jsonArray = new JSONArray(jsonresponse);
                                if (jsonArray.isNull(0)) {
                                    Toast.makeText(PhleboDetails.this, "No Details", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(), SingleBooking.class));
                                    finish();
                                }

                                try {
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject obj = jsonArray.getJSONObject(i);
                                        txt_phlebo_first_name.setText(obj.getString("first_name") + " " + obj.getString("last_name") + " " + obj.getString("sirname"));
                                        txt_phlebo_gender.setText(obj.getString("gender"));
                                        txt_phlebo_qualifications.setText(obj.getString("qualifications"));
                                        txt_phlebo_phone.setText(obj.getString("id"));
                                        SharedPreferences.Editor editor = shared.edit();
                                        editor.putString("phlebo_id", obj.getString("id"));
                                        editor.apply();
                                        // SharedPreferences.Editor editor = shared.edit();
                                        //editor.putString("phlebo_id", obj.getString("id"));
                                        // Toast.makeText(PhleboDetails.this, "Okay"+obj.getString("id"), Toast.LENGTH_SHORT).show();
                                        //editor.apply();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(PhleboDetails.this, "There was a server error, try again", Toast.LENGTH_SHORT).show();

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(PhleboDetails.this, "There was a server error, try again", Toast.LENGTH_SHORT).show();
                            }

                        }

                    } else {
                        dialog.dismiss();
                        android.app.AlertDialog alert = new android.app.AlertDialog.Builder(PhleboDetails.this)
                                .setTitle("No Records, Press OK")
                                .setPositiveButton("Ok",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog,
                                                                int whichButton) {


                                                startActivity(new Intent(getApplicationContext(), SingleBooking.class));
                                                finish();

                                            }
                                        }).show();
//                        startActivity(new Intent(getApplicationContext(), SingleBooking.class));
                      //  Log.i("onEmptyResponse", "Returned empty response");//Toast.makeText(getContext(),"Nothing returned",Toast.LENGTH_LONG).show();
                    }

                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    dialog.dismiss();
                    Toast.makeText(PhleboDetails.this, "There was a server error, try again", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), SingleBooking.class));
                    finish();
                }
            });


        }
        catch (Exception e){
            Toast.makeText(this, "Server Error", Toast.LENGTH_SHORT).show();
        }
    }


    public static String formatDate(String fromFormat, String toFormat, String dateToFormat) {
        SimpleDateFormat inFormat = new SimpleDateFormat(fromFormat);
        Date date = null;
        try {
            date = inFormat.parse(dateToFormat);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat outFormat = new SimpleDateFormat(toFormat);

        return outFormat.format(date);
    }
}