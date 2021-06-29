package com.modcom.meditest;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;

import helpers.StoreDatabase;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit_api.IRetrofit;
import services.ServiceGenerator;

//THIS IS THE PAYMENT ACTIVITY
public class Payment extends AppCompatActivity {
SharedPreferences shared;
    AlertDialog.Builder builder;
    private StoreDatabase dbHelper;
    EditText phone_to_pay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        shared = getSharedPreferences("mediprefs", MODE_PRIVATE);
        String amount = shared.getString("amount","");
        EditText amount_to_pay = findViewById(R.id.amount_to_pay);
        phone_to_pay = findViewById(R.id.phone_to_pay);

        TextView paylater = findViewById(R.id.paylater);
        paylater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), Updated.class));
                finish();
            }
        });
        amount_to_pay.setText(amount+" KES");

        Button payment = findViewById(R.id.btn_makepayment);
        payment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(phone_to_pay.getText().toString().equalsIgnoreCase("")){
                    phone_to_pay.setError("Please Phone Number 2547xxx");
                }

                else {
                    // THIS METHOD WILL MAKE PAYMENT TO MPESA PAYMENT ENDPOINT
                    onPayment();
                }
            }
        });
    }

   //THIS METHOD HITS THE MPESA PAYMENT GATEWAY
   ProgressDialog dialog;
    public void onPayment(){
        dialog = new ProgressDialog(Payment.this);
        try {
        dialog.setTitle("Payment");
        dialog.setMessage("Please wait..");
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setMax(100);
        dialog.show();

        //Get location
        String total_amount = shared.getString("amount", "");
        String booking_id = shared.getString("booking_id", "");
        String phone = phone_to_pay.getText().toString();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("amount",  total_amount);
        jsonObject.addProperty("party_a",  phone);
        jsonObject.addProperty("account_ref",  "Meditest");
        jsonObject.addProperty("trans_desc",  "Fine Payment");
        jsonObject.addProperty("remarks",  "Pay via mpesa");
        jsonObject.addProperty("booking_id",  booking_id);


        ServiceGenerator serviceGenerator  = new ServiceGenerator(getApplicationContext());
        IRetrofit jsonPostService = serviceGenerator.createService(IRetrofit.class, IRetrofit.BASE_URL);
        Call<String> call = jsonPostService.postRawPayment(jsonObject);
        call.enqueue(new Callback<String>() {

            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {

                try {
                    dialog.dismiss();
                   // Toast.makeText(Payment.this, "OK"+response.body().toString(), Toast.LENGTH_SHORT).show();
                    //assert response.body() != null;
                 //   try {
                            android.app.AlertDialog alert = new android.app.AlertDialog.Builder(Payment.this)
                                    .setTitle("Please complete transaction on your phone, Press OK")
                                    .setPositiveButton("Ok",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog,
                                                                    int whichButton) {

                                                    startActivity(new Intent(getApplicationContext(), Updated.class));
                                                    finish();
                                                }
                                            }).show();


//                        //Log.d("hey", response.message());
//                        JSONObject obj = new JSONObject(response.body().toString());
                       // Toast.makeText(Payment.this, "", Toast.LENGTH_SHORT).show();
//                        int code = Integer.parseInt((obj.getString("status_code")));
//                        if (code == 400) {
//                            //Toast.makeText(UserrReg.this, ""+code, Toast.LENGTH_SHORT).show();
//                            JSONObject dataArray = obj.getJSONObject("message");
//
//
//                        } else if (code == 200) {
//
//                            android.app.AlertDialog alert = new android.app.AlertDialog.Builder(Payment.this)
//                                    .setTitle("Booking Successful, Press OK")
//                                    .setPositiveButton("Ok",
//                                            new DialogInterface.OnClickListener() {
//                                                public void onClick(DialogInterface dialog,
//                                                                    int whichButton) {
//
//
//                                                    startActivity(new Intent(getApplicationContext(), NewMainPage.class));
//                                                    dialog.dismiss();
//                                                    finish();
//
//                                                }
//                                            }).show();
//                        } else {
//                            //Toast.makeText(Payment.this, "Error Occurred! Check your connection and try Again", Toast.LENGTH_LONG).show();
//                            //Uncomment the below code to Set the message and title from the strings.xml file
//                            builder.setMessage("Booking Status.").setTitle("Error");
//
//                            //Setting message manually and performing action on button click
//                            builder.setMessage("Error. Booking Failed, check Your Internet Connection")
//                                    .setCancelable(false)
//                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                                        public void onClick(DialogInterface dialog, int id) {
//                                            startActivity(new Intent(getApplicationContext(), NewMainPage.class));
//                                        }
//                                    });
//                            //Creating dialog box
//                            AlertDialog alert = builder.create();
//                            //Setting the title manually
//                            alert.setTitle("Success");
//                            alert.show();
//
//                        }


//                    } catch (JSONException e) {
//                        Toast.makeText(Payment.this, "No Response from server", Toast.LENGTH_SHORT).show();
//                    }

                }
                catch (NullPointerException e){
                    Toast.makeText(Payment.this, "No Response from server, Booking Failed", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }

            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.d("response-failure", call.toString());
                Toast.makeText(Payment.this, "Server Error, Check Your internet Connection and try again", Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });

        }
        catch (Exception e){
            Toast.makeText(this, "Server Error!, try again", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        }
    }

    public void onPostClicked(){
        try {
        ProgressDialog dialog = new ProgressDialog(Payment.this);
        dialog.setTitle("Sending");
        dialog.setMessage("Please wait..");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMax(100);
        dialog.show();

        //Get location
        //creating the json object to send
        String user_id = shared.getString("id","");
        String dependant_id = shared.getString("dependant_id","");

        boolean paid = false;
        boolean self = shared.getBoolean("self",false);
        String scheduled_date = shared.getString("scheduled_date","");
        String scheduled_time = shared.getString("scheduled_time","");
       // Toast.makeText(Payment.this, "Date: "+scheduled_date, Toast.LENGTH_LONG).show();
        //Toast.makeText(Payment.this, "Time: "+scheduled_time, Toast.LENGTH_LONG).show();
        String total_amount = shared.getString("amount", "");
        String phone = shared.getString("phone", "");
        String lat = shared.getString("lat", "");
        String lon = shared.getString("lon", "");
        String locality = shared.getString("locality", "");
        String admin_area = shared.getString("admin_area", "");
        String sub_admin_area = shared.getString("sub_admin_area", "");
        String book_select = shared.getString("book_select", "");
        String phone_select = shared.getString("phone_select", "");
        dbHelper = new StoreDatabase(getApplicationContext());
        try {
            dbHelper.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Cursor cursor = dbHelper.fetchAllItems();
        JsonArray citiesArray = new JsonArray();
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            citiesArray.add(Integer.valueOf(cursor.getString(cursor.getColumnIndex("_id"))));

        }

        JsonArray depArray = new JsonArray();
        if(dependant_id.length()!=0) {
            depArray.add(Integer.parseInt(dependant_id));
        }


        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("user_id",user_id);
        jsonObject.add("dependant_id",depArray);
        jsonObject.add("service_id", citiesArray);
        Log.d("array", citiesArray.toString());
       // Toast.makeText(this, "Array: "+citiesArray.toString(), Toast.LENGTH_SHORT).show();
        jsonObject.addProperty("self",self);
        jsonObject.addProperty("paid",paid);
        jsonObject.addProperty("scheduled_date",scheduled_date);
        jsonObject.addProperty("scheduled_time",scheduled_time);
        jsonObject.addProperty("address_desc",book_select);
        jsonObject.addProperty("phone",phone_select);
        jsonObject.addProperty("total_amount",  total_amount);
        jsonObject.addProperty("phone",  phone);
        jsonObject.addProperty("lat",  lat);
        jsonObject.addProperty("lon",  lon);
        jsonObject.addProperty("locality",  locality);
        jsonObject.addProperty("admin_area",  admin_area);
        jsonObject.addProperty("sub_admin_area",  sub_admin_area);

        ServiceGenerator serviceGenerator  = new ServiceGenerator(getApplicationContext());
        IRetrofit jsonPostService = serviceGenerator.createService(IRetrofit.class, IRetrofit.BASE_URL);
        Call<JsonObject> call = jsonPostService.postRawBook(jsonObject);
        call.enqueue(new Callback<JsonObject>() {

            @Override
            public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
                try {
                    dialog.dismiss();
                    //Toast.makeText(Payment.this, "OK", Toast.LENGTH_SHORT).show();
                    //assert response.body() != null;
                    try {

                        if (response.isSuccessful()) {
                            if (response.body() != null) {

                        //Log.d("hey", response.message());
                        JSONObject obj = new JSONObject(response.body().toString());
                        //Toast.makeText(Payment.this, response.body().toString(), Toast.LENGTH_SHORT).show();
                        int code = Integer.parseInt((obj.getString("status_code")));
                        if (code == 400) {
                            //Toast.makeText(UserrReg.this, ""+code, Toast.LENGTH_SHORT).show();
                            JSONObject dataArray = obj.getJSONObject("message");

                            for (int i = 0; i < dataArray.length(); i++) {
                                // JSONArray x = dataArray.getJSONArray("first_name");
                                if (dataArray.has("scheduled_date")) {
                                    Toast.makeText(Payment.this, dataArray.getJSONArray("scheduled_date").toString(), Toast.LENGTH_LONG).show();
                                }

                                if (dataArray.has("scheduled_time")) {
                                    Toast.makeText(Payment.this, dataArray.getJSONArray("scheduled_time").toString(), Toast.LENGTH_LONG).show();
                                }

                                if (dataArray.has("total_amount")) {
                                    Toast.makeText(Payment.this, dataArray.getJSONArray("total_amount").toString(), Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(getApplicationContext(), ConfirmBooking.class));
                                    finish();
                                }
                                if (dataArray.has("lat")) {
                                    Toast.makeText(Payment.this, dataArray.getJSONArray("lat").toString(), Toast.LENGTH_LONG).show();
                                }

                                if (dataArray.has("lon")) {
                                    Toast.makeText(Payment.this, dataArray.getJSONArray("lon").toString(), Toast.LENGTH_LONG).show();
                                }

                                if (dataArray.has("lon")) {
                                    Toast.makeText(Payment.this, dataArray.getJSONArray("lon").toString(), Toast.LENGTH_LONG).show();
                                }

                                if (dataArray.has("service_id")) {
                                    Toast.makeText(Payment.this, dataArray.getJSONArray("service_id").toString(), Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(getApplicationContext(), ConfirmBooking.class));
                                    finish();
                                }


                            }
                        } else if (code == 200) {

                            android.app.AlertDialog alert = new android.app.AlertDialog.Builder(Payment.this)
                                    .setTitle("Booking Successful, Press OK")
                                    .setPositiveButton("Ok",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog,
                                                                    int whichButton) {

                                                    dbHelper.deleteAllItems();
                                                    startActivity(new Intent(getApplicationContext(), Updated.class));
                                                    dialog.dismiss();
                                                    finish();

                                                }
                                            }).show();
                        } else {
                            //Toast.makeText(Payment.this, "Error Occurred! Check your connection and try Again", Toast.LENGTH_LONG).show();
                            //Uncomment the below code to Set the message and title from the strings.xml file
                            builder.setMessage("Booking Status.").setTitle("Error");

                            //Setting message manually and performing action on button click
                            builder.setMessage("Error. Booking Failed, check Your Internet Connection")
                                    .setCancelable(false)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            startActivity(new Intent(getApplicationContext(), Updated.class));
                                        }
                                    });
                            //Creating dialog box
                            AlertDialog alert = builder.create();
                            //Setting the title manually
                            alert.setTitle("Success");
                            alert.show();

                        }
                                //here
                            } else {
                                dialog.dismiss();
                                Toast.makeText(Payment.this, "Server Error!, Try again", Toast.LENGTH_SHORT).show();
                                // Log.i("onEmptyResponse", "Returned empty response");//Toast.makeText(getContext(),"Nothing returned",Toast.LENGTH_LONG).show();
                                //startActivity(new Intent(getApplicationContext(), NewMainPage.class));
                                //finish();

                            }
                        }
                        else {
                            dialog.dismiss();
                            Toast.makeText(Payment.this, "No Server Response, Try Again", Toast.LENGTH_SHORT).show();
                            //startActivity(new Intent(getApplicationContext(), NewMainPage.class));
                            //finish();
                        }

                    } catch (JSONException e) {
                        Toast.makeText(Payment.this, "No Response from server", Toast.LENGTH_SHORT).show();
                    }

                }
               catch (NullPointerException e){
                   Toast.makeText(Payment.this, "No Response from server, Booking Failed", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
               // Log.d("response-failure", call.toString());
                Toast.makeText(Payment.this, "Server Error, Check Your internet Connection and try again", Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });
        }
        catch (Exception e){
            Toast.makeText(this, "Server Error!, try again", Toast.LENGTH_SHORT).show();
        }
    }


}