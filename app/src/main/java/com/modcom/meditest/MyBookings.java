package com.modcom.meditest;



import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import adapters.BookingAdapter;
import models.BookingModel;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit_api.RecyclerInterface;

import android.widget.EditText;

import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.widget.SearchView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MyBookings extends AppCompatActivity {
    private EditText etsearch;
    private RecyclerView recyclerView;
    SearchView searchView;
    SharedPreferences shared;
    SwipeRefreshLayout swipeRefreshLayout;
    TextView dep_status, tv_booking;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bookings);

        recyclerView = findViewById(R.id.recycler);

        // etsearch = (EditText) findViewById(R.id.editText);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        shared = getSharedPreferences("mediprefs", MODE_PRIVATE);
        tv_booking  = findViewById(R.id.my_bookings);
       // dep_status = findViewById(R.id.dep_status);
       // dep_status.setVisibility(View.GONE);
        fetchJSON();
    }

    private void fetchJSON(){

        try {
        ProgressDialog dialog = new ProgressDialog(MyBookings.this);
        dialog.setTitle("Retrieving Your Bookings..");
        dialog.setMessage("Please wait..");
        dialog.setMax(100);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();

        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
            @NotNull
            @Override
            public okhttp3.Response intercept(@NotNull Chain chain) throws IOException {
                String token = shared.getString("token","");
                Request newRequest  = chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer "+token)
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

        String id = shared.getString("id","");
        RecyclerInterface api = retrofit.create(RecyclerInterface.class);
        Call<String> call = api.getStringMyBookings(Integer.parseInt(id));
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
              //  Log.i("Responsestring", response.body().toString());
                //Toast.makeText()
                dialog.dismiss();
                try {


                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            dialog.dismiss();
                            tv_booking.setText("Your Bookings");
                            // Toast.makeText(ServicesPage.this, "Success", Toast.LENGTH_SHORT).show();
                           // Log.i("onSuccess", response.body().toString());
                            String jsonresponse = response.body().toString();
                            writeRecycler(jsonresponse);

                        } else {
                            dialog.dismiss();
                            Toast.makeText(MyBookings.this, "Error!, Try again", Toast.LENGTH_SHORT).show();
                            // Log.i("onEmptyResponse", "Returned empty response");//Toast.makeText(getContext(),"Nothing returned",Toast.LENGTH_LONG).show();
                        }
                    }

                    else {
                        dialog.dismiss();
                        Toast.makeText(MyBookings.this, "No Response, Try Again", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), NewMainPage.class));
                        finish();
                    }






                }catch (Exception s){
                    AlertDialog alert = new AlertDialog.Builder(MyBookings.this)
                            .setTitle("Please Check Your Network connections. Try again")
                            .setPositiveButton("Ok",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int whichButton) {
                                            //System.exit(0);
                                        }
                                    }).show();
                    alert.setCanceledOnTouchOutside(false);
                }

            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                dialog.dismiss();

                // Creating a new alert dialog to confirm the delete
                AlertDialog alert = new AlertDialog.Builder(MyBookings.this)
                        .setTitle("Please Check Your Network connections. Try Again")
                        .setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
//                                        startActivity(new Intent(getApplicationContext(), Home2.class));
//                                        finish();
                                        tv_booking.setText("Please Check Your Network connections, Try again");
                                    }
                                }).show();


                Toast.makeText(MyBookings.this, "There was a server error, try again", Toast.LENGTH_SHORT).show();


            }
        });

        }
        catch (Exception e){
            Toast.makeText(this, "Server Error!, try again", Toast.LENGTH_SHORT).show();
        }
    }

    private void writeRecycler(String response){

        try {
            //getting the whole json object from the response
            JSONObject obj = new JSONObject(response);
            //if(obj.optString("status").equals("true")){
            ArrayList<BookingModel> modelRecyclerArrayList = new ArrayList<>();
            // Log.d("hey", obj.toString());

            //else {

                JSONArray dataArray  = obj.getJSONArray("data");

            if (dataArray.isNull(0)) {
                tv_booking.setText("No Bookings, Press Back");
                //Toast.makeText(getActivity(), "Empty", Toast.LENGTH_SHORT).show();
                android.app.AlertDialog alert = new android.app.AlertDialog.Builder(MyBookings.this)
                        .setTitle("No Bookings, Press OK")
                        .setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {

                                        dialog.dismiss();


                                    }
                                }).show();

            }

            else {

                for (int i = 0; i < dataArray.length(); i++) {
                    BookingModel modelRecycler = new BookingModel();
                    JSONObject dataobj = dataArray.getJSONObject(i);
                    modelRecycler.setDate(dataobj.getString("scheduled_date"));
                    modelRecycler.setTime(dataobj.getString("scheduled_time"));
                    modelRecycler.setPaid(dataobj.getString("paid"));
                    modelRecycler.setTotal_amount(dataobj.getString("total_amount"));
                    modelRecycler.setStatus(dataobj.getString("status"));
                    modelRecycler.setBooking_id(dataobj.getString("id"));
                    modelRecyclerArrayList.add(modelRecycler);
                }
//
                BookingAdapter retrofitAdapter = new BookingAdapter(this, modelRecyclerArrayList);
                recyclerView.setAdapter(retrofitAdapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
                swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        swipeRefreshLayout.setRefreshing(false);
                        fetchJSON();
                    }
                });
            }
//            }else {
//                Toast.makeText(ServicesPage.this, obj.optString("message")+"", Toast.LENGTH_SHORT).show();
//            }

        } catch (JSONException e) {
            AlertDialog alert = new AlertDialog.Builder(MyBookings.this)
                    .setTitle("Please Check Your Network connections. Try Again")
                    .setPositiveButton("Ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    tv_booking.setText("Please Check Your Network connections");
                                    //System.exit(0);
                                }
                            }).show();
            alert.setCanceledOnTouchOutside(false);
        }

    }


}