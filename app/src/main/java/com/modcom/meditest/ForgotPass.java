package com.modcom.meditest;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit_api.IRetrofit;
import services.ServiceGenerator;

public class ForgotPass extends AppCompatActivity {
    SharedPreferences shared;
    EditText input_phone, input_otp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        shared = getSharedPreferences("mediprefs", MODE_PRIVATE);


        input_phone =  findViewById(R.id.user_reset_phone);
        Button send_otp = findViewById(R.id.btn_get_otp);
        send_otp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (input_phone.getText().toString().length()==0){
                    input_phone.setError("Enter Phone No 2547XXXXXXXX");
                }
                else {
                    requestOTP(input_phone.getText().toString());
                }
            }
        });


        input_otp =  findViewById(R.id.user_reset_otp);
        Button submit_otp = findViewById(R.id.btn_submit_otp);
        submit_otp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (input_otp.getText().toString().length()==0){
                    input_otp.setError("Enter OTP Received in your inbox");
                }

                else {
                    submitOTP(input_otp.getText().toString());
                }
            }
        });




    }//end

    public void requestOTP(String phone_input) {
        try {
            if (phone_input.equals("")) {

            } else {

                ProgressDialog dialog = new ProgressDialog(ForgotPass.this);
                dialog.setTitle("Processing..");
                dialog.setMessage("Please wait..");
                dialog.setMax(100);
                dialog.show();

                //prepare json object to post
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("phone", phone_input);


                //here we use Iretrofit to post to BASE URL Located to Iretrofit

                ServiceGenerator serviceGenerator = new ServiceGenerator(getApplicationContext());
                IRetrofit jsonPostService = serviceGenerator.createService(IRetrofit.class, IRetrofit.BASE_URL);
                //This is the POst services
                Call<JsonObject> call = jsonPostService.postRawRequestOTP(jsonObject);
                call.enqueue(new Callback<JsonObject>() {

                    @Override
                    public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
                        try {
                            dialog.dismiss();
                            //here we handle the responses in JSOn
                            //Toast.makeText(UserrReg.this, "OK"+response.body().toString(), Toast.LENGTH_SHORT).show();

                            if (response.isSuccessful()) {
                                if (response.body() != null) {

                                    // Toast.makeText(ServicesPage.this, "Success", Toast.LENGTH_SHORT).show();
                                    //  Log.i("onSuccess", response.body().toString());
                                    JSONObject obj = new JSONObject(response.body().toString());
                                        //Toast.makeText(UserrReg.this, ""+code, Toast.LENGTH_SHORT).show();
                                    if (obj.has("phone")) {
                                        JSONArray dataArray = obj.getJSONArray("phone");
                                        //input_phone.setError(dataArray.toString());
                                        Toast.makeText(ForgotPass.this, dataArray.toString(), Toast.LENGTH_SHORT).show();
                                    }

                                    if (obj.has("otp")) {
                                        int otp = Integer.parseInt((obj.getString("otp")));
                                        Toast.makeText(ForgotPass.this, ""+otp, Toast.LENGTH_SHORT).show();
                                    }

                                    //here
                                } else {
                                    dialog.dismiss();
                                    Toast.makeText(ForgotPass.this, "Server Error!, Try again", Toast.LENGTH_SHORT).show();
                                    // Log.i("onEmptyResponse", "Returned empty response");//Toast.makeText(getContext(),"Nothing returned",Toast.LENGTH_LONG).show();
                                    //startActivity(new Intent(getApplicationContext(), NewMainPage.class));
                                    //finish();

                                }
                            }
                            else {
                                dialog.dismiss();
                                Toast.makeText(ForgotPass.this, "No Server Response, Try Again", Toast.LENGTH_SHORT).show();
                                //startActivity(new Intent(getApplicationContext(), NewMainPage.class));
                                //finish();
                            }

                        } catch (JSONException e) {
                            //Toast.makeText(UserLogin.this, "Server Error, Check Your internet Connection and try again", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                            AlertDialog alert = new AlertDialog.Builder(ForgotPass.this)
                                    .setTitle("Server Error! Try again."+e.getMessage())
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
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        //  Log.d("response-failure", call.toString());
                        //Toast.makeText(UserLogin.this, "Server Error, Check Your internet Connection and try again", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                        AlertDialog alert = new AlertDialog.Builder(ForgotPass.this)
                                .setTitle("Please Check Your Network connections.Try Again")
                                .setPositiveButton("Ok",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog,
                                                                int whichButton) {
                                                //System.exit(0);
                                            }
                                        }).show();
                        alert.setCanceledOnTouchOutside(false);
                    }
                });
            }

        }
        catch (Exception e){
            Toast.makeText(this, "Server Error!, try again", Toast.LENGTH_SHORT).show();
        }
    }


    public void submitOTP(String otp_input) {
        try {
            if (otp_input.equals("")) {

            } else {

                ProgressDialog dialog = new ProgressDialog(ForgotPass.this);
                dialog.setTitle("Processing..");
                dialog.setMessage("Please wait..");
                dialog.setMax(100);
                dialog.show();

                //prepare json object to post
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("otp", otp_input);


                //here we use Iretrofit to post to BASE URL Located to Iretrofit

                ServiceGenerator serviceGenerator = new ServiceGenerator(getApplicationContext());
                IRetrofit jsonPostService = serviceGenerator.createService(IRetrofit.class, IRetrofit.BASE_URL);
                //This is the POst services
                Call<JsonObject> call = jsonPostService.postRawSubmitOTP(jsonObject);
                call.enqueue(new Callback<JsonObject>() {

                    @Override
                    public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
                        try {
                            dialog.dismiss();
                            //here we handle the responses in JSOn
                            //Toast.makeText(UserrReg.this, "OK"+response.body().toString(), Toast.LENGTH_SHORT).show();

                            if (response.isSuccessful()) {
                                if (response.body() != null) {
                                    // Toast.makeText(ServicesPage.this, "Success", Toast.LENGTH_SHORT).show();
                                    //  Log.i("onSuccess", response.body().toString());
                                    JSONObject obj = new JSONObject(response.body().toString());
                                    //Toast.makeText(UserrReg.this, ""+code, Toast.LENGTH_SHORT).show();
                                    if (obj.has("phone")) {
                                        JSONArray dataArray = obj.getJSONArray("phone");
                                        //input_otp.setError(dataArray.toString());
                                        input_otp.setError(dataArray.toString());
                                        //Toast.makeText(ForgotPass.this, "" + dataArray.toString(), Toast.LENGTH_SHORT).show();
                                    }

                                    if (obj.has("otp")) {
                                        String dataArray1 = obj.getString("otp");
                                        if(!dataArray1.equals(otp_input)){
                                            //Toast.makeText(ForgotPass.this, "" + dataArray1, Toast.LENGTH_SHORT).show();
                                            input_otp.setError(dataArray1.toString());
                                        }

                                        else if(dataArray1.equalsIgnoreCase(otp_input)){
                                            SharedPreferences.Editor editor = shared.edit();
                                            editor.putString("otp", otp_input);
                                            editor.apply();
                                            startActivity(new Intent(getApplicationContext(), ChangePassword.class));
                                        }

                                    }

                                    //here
                                } else {
                                    dialog.dismiss();
                                    Toast.makeText(ForgotPass.this, "Server Error!, Try again", Toast.LENGTH_SHORT).show();
                                    // Log.i("onEmptyResponse", "Returned empty response");//Toast.makeText(getContext(),"Nothing returned",Toast.LENGTH_LONG).show();
                                    //startActivity(new Intent(getApplicationContext(), NewMainPage.class));
                                    //finish();

                                }
                            }
                            else {
                                dialog.dismiss();
                                Toast.makeText(ForgotPass.this, "No Server Response, Try Again", Toast.LENGTH_SHORT).show();
                                //startActivity(new Intent(getApplicationContext(), NewMainPage.class));
                                //finish();
                            }

                        } catch (JSONException e) {
                            //Toast.makeText(UserLogin.this, "Server Error, Check Your internet Connection and try again", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                            AlertDialog alert = new AlertDialog.Builder(ForgotPass.this)
                                    .setTitle("Server Error! Try again."+e.getMessage())
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
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        //  Log.d("response-failure", call.toString());
                        //Toast.makeText(UserLogin.this, "Server Error, Check Your internet Connection and try again", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                        AlertDialog alert = new AlertDialog.Builder(ForgotPass.this)
                                .setTitle("Please Check Your Network connections.Try Again")
                                .setPositiveButton("Ok",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog,
                                                                int whichButton) {
                                                //System.exit(0);
                                            }
                                        }).show();
                        alert.setCanceledOnTouchOutside(false);
                    }
                });
            }

        }
        catch (Exception e){
            Toast.makeText(this, "Server Error!, try again", Toast.LENGTH_SHORT).show();
        }
    }






}