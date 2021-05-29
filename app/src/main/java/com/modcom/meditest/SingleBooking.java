package com.modcom.meditest;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

public class SingleBooking extends AppCompatActivity {
    TextView txt_scheduled;
    TextView txt_status;
    TextView txt_single_date;
    TextView    txt_single_time;
    TextView txt_test_name;
    TextView   txt_test_cost;
    TextView txt_patient_name;
    TextView   txt_patient_relationship;
    TextView txt_contacts;
    TextView txt_exact_location, view_phlebo, txt_total_amount;
    LinearLayout linearLayout;
    SharedPreferences shared;
    TextView btn_pay_now;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_booking);
        shared = getSharedPreferences("mediprefs", MODE_PRIVATE);
        String booking_id = shared.getString("booking_id","");
        Toast.makeText(this, ""+booking_id, Toast.LENGTH_SHORT).show();

        btn_pay_now = findViewById(R.id.btn_make_pay_now);
        btn_pay_now.setVisibility(View.GONE);
        btn_pay_now.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), Payment.class));
                finish();
            }
        });

        txt_scheduled = findViewById(R.id.txt_scheduled);
                txt_status = findViewById(R.id.txt_status);
        txt_single_date = findViewById(R.id.txt_single_date);
                txt_single_time = findViewById(R.id.txt_single_time);
        txt_test_name= findViewById(R.id.txt_test_name);
             //   txt_test_cost= findViewById(R.id.txt_test_cost);
        txt_patient_name = findViewById(R.id.txt_patient_name);
                txt_patient_relationship = findViewById(R.id.txt_patient_relationship);
        txt_contacts = findViewById(R.id.txt_contacts);
                txt_exact_location= findViewById(R.id.txt_exact_location);
        view_phlebo= findViewById(R.id.view_phlebo);
        view_phlebo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), PhleboDetails.class);
                SharedPreferences.Editor editor = shared.edit();
                editor.putString("status", txt_status.getText().toString());
                editor.apply();
                startActivity(i);

            }
        });
        txt_total_amount= findViewById(R.id.txt_total_amount);
        linearLayout  = findViewById(R.id.linearLayout);
        linearLayout.setVisibility(View.GONE);

        String status = shared.getString("status","");
        String paid = shared.getString("paid","");
        if (paid.equalsIgnoreCase("1")) {
            setTitle("Paid");
        }

        if (paid.equalsIgnoreCase("0")) {
            setTitle("Cash on Collection");
            btn_pay_now.setVisibility(View.VISIBLE);

        }

        txt_status.setText(status);
        if (status.equalsIgnoreCase("pending")){
            view_phlebo.setVisibility(View.GONE);

        }

        else if (status.equalsIgnoreCase("active")){
            view_phlebo.setVisibility(View.VISIBLE);
        }

        else if (status.equalsIgnoreCase("assigned")){
            view_phlebo.setVisibility(View.VISIBLE);
        }

        else {
            view_phlebo.setVisibility(View.VISIBLE);
        }
        //location button
        fetchJSON();
    }

    String phone;
    private void fetchJSON(){

        try {
        String id = shared.getString("booking_id","");

        ProgressDialog dialog = new ProgressDialog(SingleBooking.this);
        dialog.setTitle("Retrieving Details..");
        dialog.setMessage("Please wait..");
        dialog.setMax(100);
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

        RecyclerInterface api = retrofit.create(RecyclerInterface.class);

        Call<String> call = api.getStringSingleBooking(Integer.parseInt(id));
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                //Log.i("Responsestring", response.body().toString());
                //Toast.makeText()
                dialog.dismiss();
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        try {
                            dialog.dismiss();
                            linearLayout.setVisibility(View.VISIBLE);

                            Log.i("onSuccess", response.body().toString());

                            String jsonresponse = response.body().toString();

                            JSONObject obj = new JSONObject(jsonresponse);
                            try {
                                JSONArray dataArray  = obj.getJSONArray("booking_details");
                                for (int i = 0; i < dataArray.length(); i++) {
                                    JSONObject myArray = dataArray.getJSONObject(i);
                                    String formatted_date = formatDate("yyyy-MM-dd", "EEEE, dd MMMM yyyy",myArray.getString("scheduled_date"));
                                    txt_single_date.setText(formatted_date);
                                    txt_single_time.setText(myArray.getString("scheduled_time"));
                                    txt_contacts.setText(myArray.getString("phone"));
                                    txt_exact_location.setText(myArray.getString("address_desc"));
                                    txt_total_amount.setText("TOTAL KES. "+myArray.getString("total_amount"));
                               }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(SingleBooking.this, "There was a server error, try again", Toast.LENGTH_SHORT).show();
                            }

                            // Patient details
                            try {
                                JSONArray dataArray = obj.getJSONArray("patients");
                                for (int i = 0; i < dataArray.length(); i++) {
                                    JSONObject myArray = dataArray.getJSONObject(i);
                                    txt_patient_name.append(myArray.getString("first_name")+" "+myArray.getString("last_name")+"\n");
                                    txt_patient_relationship.append("");
                                }
                            } catch (JSONException e) {
                                AlertDialog alert = new AlertDialog.Builder(SingleBooking.this)
                                        .setTitle("Server response error, try again.")
                                        .setPositiveButton("Ok",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog,
                                                                        int whichButton) {
                                                        //System.exit(0);
                                                    }
                                                }).show();
                                alert.setCanceledOnTouchOutside(false);
                            }

                            // Patient details

                            try {
                                JSONArray dataArray = obj.getJSONArray("services");
                                for (int i = 0; i < dataArray.length(); i++) {
                                    JSONObject myArray = dataArray.getJSONObject(i);
                                    txt_test_name.append(myArray.getString("test_name")+" @Ksh."+myArray.getString("test_price")+"\n\n");


                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(SingleBooking.this, "There was a server error, try again", Toast.LENGTH_SHORT).show();
                            }



                            //phones
                            try {
                                JSONArray dataArray = obj.getJSONArray("booker_phone");
                                for (int i = 0; i < dataArray.length(); i++) {
                                    JSONObject myArray = dataArray.getJSONObject(i);
                                    txt_contacts.setText(myArray.getString("phone"));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(SingleBooking.this, "There was a server error, try again", Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(SingleBooking.this, "There was a server error, try again", Toast.LENGTH_SHORT).show();
                        }


                    } else {
                        dialog.dismiss();
                       // Toast.makeText(SingleBooking.this, "Error!, Try again", Toast.LENGTH_SHORT).show();
                        AlertDialog alert = new AlertDialog.Builder(SingleBooking.this)
                                .setTitle("Server Error, try again.")
                                .setPositiveButton("Ok",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog,
                                                                int whichButton) {
                                                //System.exit(0);
                                            }
                                        }).show();
                        alert.setCanceledOnTouchOutside(false);
                        startActivity(new Intent(getApplicationContext(), NewMainPage.class));
                   //     Log.i("onEmptyResponse", "Returned empty response");//Toast.makeText(getContext(),"Nothing returned",Toast.LENGTH_LONG).show();
                    }
                }

                else{
                    dialog.dismiss();
                    Toast.makeText(SingleBooking.this, "No Response", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), MyBookings.class));
                    finish();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                dialog.dismiss();
                Toast.makeText(SingleBooking.this, "There was a server error, try again", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), NewMainPage.class));
                finish();
            }
        });

        }
        catch (Exception e){
            Toast.makeText(this, "Server Error!, try again", Toast.LENGTH_SHORT).show();
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