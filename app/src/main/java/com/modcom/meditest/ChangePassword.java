package com.modcom.meditest;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
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

public class ChangePassword extends AppCompatActivity {
    SharedPreferences shared;
    String otp;
    TextInputLayout textInputLayout1, textInputLayout2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        textInputLayout1 = findViewById(R.id.text_Password1);
        textInputLayout2 = findViewById(R.id.text_Password2);

        shared = getSharedPreferences("mediprefs", MODE_PRIVATE);
        otp = shared.getString("otp","");


        EditText password =  findViewById(R.id.user_reset_password1);
        EditText confirm =  findViewById(R.id.user_reset_password2);
        Button change_password = findViewById(R.id.btn_change);
        change_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Change_Password(password.getText().toString(), confirm.getText().toString(), otp);
            }
        });
    }

    public void Change_Password(String password, String confirm, String otp) {
        try {
            if (otp.equals("")) {
                Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
            } else {
                ProgressDialog dialog = new ProgressDialog(ChangePassword.this);
                dialog.setTitle("Processing..");
                dialog.setMessage("Please wait..");
                dialog.setMax(100);
                dialog.show();

                //prepare json object to post
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("password", password);
                jsonObject.addProperty("password_confirmation", confirm);
                jsonObject.addProperty("otp", otp);

                //here we use Iretrofit to post to BASE URL Located to Iretrofit
                ServiceGenerator serviceGenerator = new ServiceGenerator(getApplicationContext());
                IRetrofit jsonPostService = serviceGenerator.createService(IRetrofit.class, IRetrofit.BASE_URL);
                //This is the POst services
                Call<JsonObject> call = jsonPostService.postRawChangePassword(jsonObject);
                call.enqueue(new Callback<JsonObject>() {

                    @Override
                    public void onResponse(@NotNull Call<JsonObject> call, @NotNull Response<JsonObject> response) {
                        try {
                            dialog.dismiss();
                            //here we handle the responses in JSOn
                            Toast.makeText(ChangePassword.this, "OK"+response.body().toString(), Toast.LENGTH_SHORT).show();
                            if (response.isSuccessful()) {
                                if (response.body() != null) {
                                    // Toast.makeText(ServicesPage.this, "Success", Toast.LENGTH_SHORT).show();
                                    //  Log.i("onSuccess", response.body().toString());
                                    JSONObject obj = new JSONObject(response.body().toString());
                                    //Toast.makeText(UserrReg.this, ""+code, Toast.LENGTH_SHORT).show();

                                    if (obj.has("password")) {
                                        JSONArray dataArray = obj.getJSONArray("password");
                                        textInputLayout1.setError(dataArray.toString());
                                        //Toast.makeText(ChangePassword.this, ""+dataArray.toString(), Toast.LENGTH_SHORT).show();

                                    }

                                    else if (obj.has("phone")) {
                                        JSONArray dataArray = obj.getJSONArray("phone");
                                        textInputLayout1.setError(dataArray.toString());
                                       // Toast.makeText(ChangePassword.this, ""+dataArray.toString(), Toast.LENGTH_SHORT).show();
                                    }

                                    else if (obj.has("otp")) {
                                        JSONArray dataArray = obj.getJSONArray("otp");
                                        textInputLayout1.setError(dataArray.toString());
                                       // Toast.makeText(ChangePassword.this, ""+dataArray.toString(), Toast.LENGTH_SHORT).show();
                                    }

                                    else {
                                        dialog.dismiss();
                                        Toast.makeText(ChangePassword.this, "Password Changed", Toast.LENGTH_SHORT).show();
//
                                    }
                                    //here
                                } else {
                                    dialog.dismiss();
                                    Toast.makeText(ChangePassword.this, "Server Error!, Try again", Toast.LENGTH_SHORT).show();
                                    // Log.i("onEmptyResponse", "Returned empty response");//Toast.makeText(getContext(),"Nothing returned",Toast.LENGTH_LONG).show();
                                    //startActivity(new Intent(getApplicationContext(), NewMainPage.class));
                                    //finish();

                                }
                            }
                            else {
                                dialog.dismiss();
                                Toast.makeText(ChangePassword.this, "No Server Response, Try Again", Toast.LENGTH_SHORT).show();
                                //startActivity(new Intent(getApplicationContext(), NewMainPage.class));
                                //finish();
                            }

                        } catch (JSONException e) {
                            //Toast.makeText(UserLogin.this, "Server Error, Check Your internet Connection and try again", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                            AlertDialog alert = new AlertDialog.Builder(        ChangePassword.this)
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

                        AlertDialog alert = new AlertDialog.Builder(ChangePassword.this)
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