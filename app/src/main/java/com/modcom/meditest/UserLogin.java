package com.modcom.meditest;


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
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit_api.IRetrofit;
import services.ServiceGenerator;
//this is the login page
public class UserLogin extends AppCompatActivity {

    EditText password, email;
    Button btn_signin;
    SharedPreferences shared;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        //get shared/create prefeerences
        shared = getSharedPreferences("mediprefs", MODE_PRIVATE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //when you click back arraow
                Intent intent = new Intent(getApplicationContext(), NewMainPage.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

            email  = findViewById(R.id.user_email);
            password  = findViewById(R.id.user_password);


            btn_signin = findViewById(R.id.btn_sigin);
            btn_signin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //call onclick post clicked
                    String email_form = email.getText().toString();
                    String password_form = password.getText().toString();
                    onPostClicked(email_form, password_form, UserLogin.this);

                }
            });

        TextView link_register = findViewById(R.id.link_register);
        link_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //go to user reg
                startActivity(new Intent(getApplicationContext(), UserrReg.class));
            }
        });


        TextView link_forgot = findViewById(R.id.link_forgot);
        link_forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //go to user reg
                startActivity(new Intent(getApplicationContext(), ForgotPass.class));
            }
        });

    }

    public void onPostClicked(String email_input, String password_input, Activity context) {
        try {
        if (email_input.equals("") || password_input.equals("")) {
            startActivity(new Intent(getApplicationContext(), UserLogin.class));
            finish();
        } else {

            ProgressDialog dialog = new ProgressDialog(UserLogin.this);
            dialog.setTitle("Logging in..");
            dialog.setMessage("Please wait..");
            dialog.setMax(100);
            dialog.show();

            //prepare json object to post
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("email", email_input);
            jsonObject.addProperty("password", password_input);

            //here we use Iretrofit to post to BASE URL Located to Iretrofit

            ServiceGenerator serviceGenerator = new ServiceGenerator(getApplicationContext());
            IRetrofit jsonPostService = serviceGenerator.createService(IRetrofit.class, IRetrofit.BASE_URL);
            //This is the POst services
            Call<JsonObject> call = jsonPostService.postRawLogin(jsonObject);
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
                        int code = Integer.parseInt((obj.getString("status_code")));

                        if (code == 400) {
                            //Toast.makeText(UserrReg.this, ""+code, Toast.LENGTH_SHORT).show();
                            JSONObject dataArray = obj.getJSONObject("message");
                            for (int i = 0; i < dataArray.length(); i++) {

                                if (dataArray.has("email")) {
                                    email.setError(dataArray.getJSONArray("email").toString());
                                }
                                if (dataArray.has("password")) {
                                    password.setError(dataArray.getJSONArray("password").toString());
                                }

                            }

                        } else if (code == 500) {

                            Toast.makeText(UserLogin.this, obj.getString("message"), Toast.LENGTH_SHORT).show();
                        } else if (code == 200) {


                            SharedPreferences.Editor editor = shared.edit();
                            String token = obj.getString("token");

                            JSONObject dataArray1 = obj.getJSONObject("user");
                            for (int i = 0; i < dataArray1.length(); i++) {
                                editor.putString("id", dataArray1.getString("id"));
                                editor.putString("first_name", dataArray1.getString("first_name"));
                                editor.putString("last_name", dataArray1.getString("last_name"));
                                editor.putString("sirname", dataArray1.getString("sirname"));
                                editor.putString("phone", dataArray1.getString("phone"));
                                editor.putString("email", dataArray1.getString("email"));
                                editor.putString("gender", dataArray1.getString("gender"));
                                editor.putString("password", password.getText().toString());
                                //Toast.makeText(UserLogin.this, "ID" + dataArray1.getString("id"), Toast.LENGTH_SHORT).show();
                                // Toast.makeText(UserLogin.this, "User name" + dataArray1.getString("first_name"), Toast.LENGTH_SHORT).show();
                            }
                            //Store token in prefferences
                            editor.putString("token", token);
                            editor.apply();
                            //proceed to new HOme activity
                            startActivity(new Intent(getApplicationContext(), NewHome.class));
                            //Toast.makeText(UserLogin.this, "Login  Successfully", Toast.LENGTH_SHORT).show();
                            email.setText("");
                            password.setText("");
                            finish();
                        } else {
                            Toast.makeText(UserLogin.this, "Error Occurred! try Again", Toast.LENGTH_SHORT).show();
                            email.setText("");
                            password.setText("");

                        }

                        //here
                            } else {
                                dialog.dismiss();
                                Toast.makeText(UserLogin.this, "Server Error!, Try again", Toast.LENGTH_SHORT).show();
                                // Log.i("onEmptyResponse", "Returned empty response");//Toast.makeText(getContext(),"Nothing returned",Toast.LENGTH_LONG).show();
                                //startActivity(new Intent(getApplicationContext(), NewMainPage.class));
                                //finish();

                            }
                        }
                        else {
                            dialog.dismiss();
                            Toast.makeText(UserLogin.this, "No Server Response, Try Again", Toast.LENGTH_SHORT).show();
                            //startActivity(new Intent(getApplicationContext(), NewMainPage.class));
                            //finish();
                        }

                    } catch (JSONException e) {
                        //Toast.makeText(UserLogin.this, "Server Error, Check Your internet Connection and try again", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                        AlertDialog alert = new AlertDialog.Builder(UserLogin.this)
                                .setTitle("Server Error! Try again.")
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
                    AlertDialog alert = new AlertDialog.Builder(UserLogin.this)
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

